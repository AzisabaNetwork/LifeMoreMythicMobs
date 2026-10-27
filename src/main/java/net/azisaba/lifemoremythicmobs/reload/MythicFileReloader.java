package net.azisaba.lifemoremythicmobs.reload;

import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.mobs.entities.MythicEntityType;
import io.lumine.mythic.api.packs.Pack;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitEntityType;
import io.lumine.mythic.bukkit.clock.MythicClock;
import io.lumine.mythic.bukkit.utils.config.properties.types.NodeListProp;
import io.lumine.mythic.core.config.MythicConfigImpl;
import io.lumine.mythic.core.constants.ConfigFileType;
import io.lumine.mythic.core.items.ItemExecutor;
import io.lumine.mythic.core.items.MythicItem;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import io.lumine.mythic.core.mobs.MobStack;
import io.lumine.mythic.core.mobs.MobType;
import io.lumine.mythic.core.skills.MetaSkill;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.utils.TemplateUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Reloads the mobs, skills and items defined in a single MythicMobs config file without a full /mm reload.
 * <p>
 * Entries that already exist keep their object identity (see {@link ObjectTransplant}), so every skill call,
 * summon, drop, aura, active mob and Crucible wrapper that references them picks up the new definition.
 * Must be called on the server thread.
 */
public final class MythicFileReloader {
    private static final Set<String> KEEP_SKILL_FIELDS = Set.of("uuid", "parents");
    private static final Set<String> KEEP_MOB_FIELDS = Set.of("skillLock");

    private final MythicBukkit mythic = MythicBukkit.inst();
    private final Set<SkillMechanic> staleMechanics = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<MobType> replacedMobs = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<MythicItem> replacedItems = new ArrayList<>();
    /** Live mob type -> discarded instance, for mobs whose Model is attached a tick after construction. */
    private final Map<MobType, MobType> modelSync = new IdentityHashMap<>();
    private final Report report = new Report();
    private final ObjectTransplant tx = new ObjectTransplant();
    private @Nullable CrucibleBridge crucible;

    public static final class Report {
        public final List<String> replaced = new ArrayList<>();
        public final List<String> added = new ArrayList<>();
        public final List<String> notInFile = new ArrayList<>();
        public final List<String> notes = new ArrayList<>();
        public final List<String> errors = new ArrayList<>();
        public final List<String> log = new ArrayList<>();
    }

    /** Config files of the given types in every loaded pack, keyed by canonical file. */
    public static @NotNull Map<File, Pack> knownFiles(@NotNull ConfigFileType type) {
        Map<File, Pack> files = new LinkedHashMap<>();
        for (Pack pack : MythicBukkit.inst().getPackManager().getPacks()) {
            for (File file : pack.getConfigFiles(type)) files.put(canonical(file), pack);
        }
        return files;
    }

    /** Whether the file is (or, once created, would be) read by MythicMobs as a mob, skill or item file. */
    public static boolean isConfigFile(@NotNull File target) {
        File file = canonical(target);
        for (ConfigFileType type : List.of(ConfigFileType.ITEM, ConfigFileType.SKILL, ConfigFileType.MOB)) {
            if (knownFiles(type).containsKey(file)) return true;
            for (Pack pack : MythicBukkit.inst().getPackManager().getPacks()) {
                for (File folder : pack.getPackFolders(type.info.dataFolder(), false, type.info.readMono())) {
                    if (file.toPath().startsWith(canonical(folder).toPath())) return true;
                }
            }
        }
        return false;
    }

    public @NotNull Report reload(@NotNull File target) throws IOException, InvalidConfigurationException {
        return reload(List.of(target));
    }

    /**
     * Reloads several files as one batch: all items first, then skills, then mobs, with a single second pass,
     * so files in the batch can reference names newly added in each other.
     */
    public @NotNull Report reload(@NotNull List<File> targets) throws IOException, InvalidConfigurationException {
        List<File> files = new ArrayList<>();
        for (File target : targets) {
            File file = canonical(target);
            if (!file.isFile()) throw new IOException("ファイルが見つかりません: " + file);
            try {
                new YamlConfiguration().load(file); // abort before touching anything if any YAML is broken
            } catch (InvalidConfigurationException e) {
                throw new InvalidConfigurationException(describe(file) + ": " + e.getMessage(), e);
            }
            if (!files.contains(file)) files.add(file);
        }
        Map<File, Map<ConfigFileType, Pack>> typesByFile = new LinkedHashMap<>();
        for (File file : files) {
            Map<ConfigFileType, Pack> types = resolveTypes(file);
            if (types.isEmpty()) {
                tx.undo();
                throw new IOException("MythicMobs の Mobs / Skills / Items フォルダ内のファイルではありません: " + file);
            }
            typesByFile.put(file, types);
        }

        crucible = CrucibleBridge.tryCreate();
        MythicClock clock = mythic.getClock();
        Logger logger = mythic.getLogger();
        Handler capture = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getLevel().intValue() < Level.WARNING.intValue()) return;
                String msg = record.getMessage();
                if (record.getParameters() != null && msg != null) {
                    try {
                        msg = java.text.MessageFormat.format(msg, record.getParameters());
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                if (msg != null) report.log.add(msg.replaceAll("\u001B\\[[;\\d]*m", "").trim());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        logger.addHandler(capture);
        boolean items = typesByFile.values().stream().anyMatch(t -> t.containsKey(ConfigFileType.ITEM));
        boolean crucibleWasComplete = false;
        try {
            clock.setReloadingPause(true);
            try {
                clearComponentCaches();
                // Same order as MythicMobs itself: items, skills, mobs
                if (items) {
                    if (crucible != null) crucibleWasComplete = crucible.beginLoad(tx);
                    reloadItems(filesOf(typesByFile, ConfigFileType.ITEM));
                    if (crucible != null) crucible.runSecondPass(); // Ammo bullet / empty-skill lookups
                }
                Map<File, Pack> skillFiles = filesOf(typesByFile, ConfigFileType.SKILL);
                if (!skillFiles.isEmpty()) reloadSkills(skillFiles);
                Map<File, Pack> mobFiles = filesOf(typesByFile, ConfigFileType.MOB);
                if (!mobFiles.isEmpty()) reloadMobs(mobFiles);
            } catch (ReflectiveOperationException | RuntimeException | Error e) {
                tx.undo();
                clearComponentCaches();
                if (items && crucible != null) crucible.endLoad(crucibleWasComplete);
                throw new IllegalStateException("リロード中にエラーが発生したため、変更を元に戻しました: " + e, e);
            } finally {
                clock.setReloadingPause(false);
            }
            clock.runSecondPass();   // resolves skill{s=...}, oT=, redirects of the new mechanics
            // Item enchantments and block conditions. The queued item tasks belong to the discarded new
            // instances (they share the enchantment map with the live item), so Crucible stays in load mode
            // until they ran and the live items rebuild their own caches afterwards.
            clock.runPostReload();
            if (items && crucible != null) crucible.endLoad(crucibleWasComplete);
            for (MythicItem item : replacedItems) {
                try {
                    item.buildItemCache();
                } catch (Exception | Error ex) {
                    report.errors.add("アイテム " + item.getInternalName() + " のキャッシュ再生成: " + ex);
                }
            }
            clearComponentCaches();
            pruneStaleParents();
            refreshStats(items);
            scheduleModelSync();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        } finally {
            logger.removeHandler(capture);
        }
        return report;
    }

    // ---------------------------------------------------------------- skills

    private void reloadSkills(Map<File, Pack> files) {
        SkillExecutor executor = mythic.getSkillManager();
        Map<String, Skill> registry = ObjectTransplant.getField(executor, "skills");
        Map<String, Skill> before = new HashMap<>(registry);
        tx.snapshot(registry);
        Set<String> nodes = new LinkedHashSet<>();
        for (Map.Entry<File, Pack> entry : files.entrySet()) {
            File file = entry.getKey();
            MythicConfigImpl fileConfig = new MythicConfigImpl(file);
            boolean mono = isMono(file);
            for (String node : NodeListProp.getNodes(file, "")) {
                nodes.add(node);
                try {
                    MythicConfig config = fileConfig.getNestedConfig(node);
                    if (mono && (config.isSet("Id") || config.isSet("Type"))) continue;
                    registry.put(node, new MetaSkill(executor, entry.getValue(), file, node, config));
                } catch (Exception | Error ex) {
                    report.errors.add("スキル " + node + ": " + ex);
                }
            }
        }
        restoreSkills(registry, before, nodes);
        for (Map.Entry<String, Skill> entry : before.entrySet()) {
            if (!entry.getKey().startsWith("mob:") && !nodes.contains(entry.getKey())
                    && entry.getValue().getConfig() != null && entry.getValue().getConfig().getFile() != null
                    && files.containsKey(canonical(entry.getValue().getConfig().getFile()))) {
                report.notInFile.add("スキル " + entry.getKey());
            }
        }
    }

    /** Puts the previously loaded instance back for every skill name whose registry entry was replaced. */
    private void restoreSkills(Map<String, Skill> registry, Map<String, Skill> before, Set<String> fileNodes) {
        for (Map.Entry<String, Skill> entry : new ArrayList<>(registry.entrySet())) {
            String name = entry.getKey();
            Skill now = entry.getValue();
            Skill old = before.get(name);
            if (old == now) continue;
            if (old instanceof MetaSkill oldSkill && now instanceof MetaSkill newSkill && old.getClass() == now.getClass()) {
                staleMechanics.addAll(ObjectTransplant.collectMechanics(oldSkill));
                tx.copyFields(newSkill, oldSkill, KEEP_SKILL_FIELDS);
                tx.rebind(oldSkill, newSkill, oldSkill, new ArrayList<>());
                registry.put(name, oldSkill);
                if (fileNodes.contains(name)) report.replaced.add("スキル " + name);
            } else if (old == null) {
                if (fileNodes.contains(name)) report.added.add("スキル " + name);
            } else {
                report.notes.add("スキル " + name + " は型が異なるため参照を引き継げませんでした(全体リロード推奨)");
            }
        }
    }

    // ---------------------------------------------------------------- mobs

    private void reloadMobs(Map<File, Pack> files) {
        MobExecutor executor = mythic.getMobManager();
        Map<String, MythicMob> mobs = ObjectTransplant.getField(executor, "mmList");
        Map<String, MythicMob> displayLookup = ObjectTransplant.getField(executor, "mmDisplayLookup");
        Map<MythicEntityType, MythicMob> vanilla = ObjectTransplant.getField(executor, "mmDefaultList");
        List<MobStack> stacks = ObjectTransplant.getField(executor, "listMobStacks");
        Map<String, Skill> skillRegistry = ObjectTransplant.getField(mythic.getSkillManager(), "skills");
        Map<String, Skill> skillsBefore = new HashMap<>(skillRegistry);
        Map<String, MythicMob> before = new HashMap<>(mobs);
        tx.snapshot(mobs);
        tx.snapshot(displayLookup);
        tx.snapshot(vanilla);
        tx.snapshot(stacks);
        tx.snapshot(skillRegistry);
        Map<MythicMob, String> displayKeys = new IdentityHashMap<>();
        displayLookup.forEach((display, mob) -> displayKeys.put(mob, display));

        Set<String> nodes = new LinkedHashSet<>();
        List<TemplateUtil.TemplateMob> templates = new ArrayList<>();
        for (Map.Entry<File, Pack> entry : files.entrySet()) {
            File file = entry.getKey();
            Pack pack = entry.getValue();
            MythicConfigImpl fileConfig = new MythicConfigImpl(file);
            boolean mono = isMono(file);
            for (String node : NodeListProp.getNodes(file, "")) {
                nodes.add(node);
                // Only construction errors are per-entry; a failure while installing aborts and rolls back everything
                MythicConfig config;
                MythicEntityType vanillaType = null;
                MobType created;
                try {
                    config = fileConfig.getNestedConfig(node);
                    if (mono && (config.isSet("Id") || config.isSet("Skills") && !config.isSet("Type") && !config.isSet("Id"))) continue;
                    if (BukkitEntityType.getMythicEntity(node) != null) {
                        vanillaType = MythicEntityType.get(node);
                    } else {
                        String stack = config.getString("MobStack");
                        if (stack != null) {
                            stacks.removeIf(s -> node.equals(s.getName()));
                            stacks.add(new MobStack(node, stack, file.getAbsolutePath()));
                            report.replaced.add("MobStack " + node);
                            continue;
                        }
                        String template = config.getString("Template");
                        if (template == null) template = config.getString("Templates");
                        if (template != null) {
                            templates.add(new TemplateUtil.TemplateMob(file, pack, node, config, template));
                            continue;
                        }
                    }
                    created = new MobType(executor, pack, file, node, config);
                } catch (Exception | Error ex) {
                    report.errors.add("Mob " + node + ": " + ex);
                    continue;
                }
                if (vanillaType != null) {
                    MythicMob installed = installMob(node, created, vanilla.get(vanillaType));
                    vanilla.put(vanillaType, installed);
                    mobs.put(vanillaType.toString(), installed);
                } else {
                    MythicMob installed = installMob(node, created, before.get(node));
                    mobs.put(node, installed);
                    updateDisplay(displayLookup, displayKeys, installed, config);
                }
            }
        }
        if (!templates.isEmpty()) {
            Map<String, Object> scratch = new HashMap<>(mobs);
            templates.forEach(t -> scratch.remove(t.id()));
            TemplateUtil.handleTemplates(templates, scratch);
            for (TemplateUtil.TemplateMob template : templates) {
                if (!(scratch.get(template.id()) instanceof MobType created)) continue;
                MythicMob installed = installMob(template.id(), created, before.get(template.id()));
                mobs.put(template.id(), installed);
                updateDisplay(displayLookup, displayKeys, installed, template.config());
            }
            report.notes.add("Template を使う Mob を含みます。リロード対象外のファイルにある子 Mob は更新されません");
        }
        restoreSkills(skillRegistry, skillsBefore, Collections.emptySet()); // Mob-local MetaSkills (mob:<name>:<skill>)
        Set<MythicMob> vanillaMobs = Collections.newSetFromMap(new IdentityHashMap<>());
        vanillaMobs.addAll(vanilla.values());
        for (Map.Entry<String, MythicMob> entry : before.entrySet()) {
            MythicMob mob = entry.getValue();
            if (!nodes.contains(entry.getKey()) && mob.getFile() != null
                    && files.containsKey(canonical(mob.getFile())) && !vanillaMobs.contains(mob)) {
                report.notInFile.add("Mob " + entry.getKey());
            }
        }
    }

    private MythicMob installMob(String name, MobType created, @Nullable MythicMob old) {
        if (old instanceof MobType oldType && old.getClass() == created.getClass()) {
            staleMechanics.addAll(ObjectTransplant.collectMechanics(oldType));
            removeTotems(oldType);
            tx.copyFields(created, oldType, KEEP_MOB_FIELDS);
            tx.rebind(oldType, created, oldType, new ArrayList<>());
            replacedMobs.add(oldType);
            if (created.getConfig() != null && created.getConfig().isSet("Model")) modelSync.put(oldType, created);
            report.replaced.add("Mob " + name);
            return oldType;
        }
        if (old != null) report.notes.add("Mob " + name + " は型が異なるため参照を引き継げませんでした(全体リロード推奨)");
        else report.added.add("Mob " + name);
        return created;
    }

    /**
     * MobType attaches ModelEngine / MiniaturePets models one tick after construction, onto the instance
     * that was just built. Copy the result onto the live instance once that has happened.
     */
    private void scheduleModelSync() {
        if (modelSync.isEmpty()) return;
        Map<MobType, MobType> pending = new IdentityHashMap<>(modelSync);
        Plugin plugin = Bukkit.getPluginManager().getPlugin("LifeMoreMythicMobs");
        if (plugin == null) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> pending.forEach((live, built) -> {
            ObjectTransplant sync = new ObjectTransplant();
            Object model = ObjectTransplant.getField(built, "model");
            sync.copyFields(built, live, ObjectTransplant.fields(MobType.class).stream()
                    .map(java.lang.reflect.Field::getName)
                    .filter(n -> !n.equals("model") && !n.equals("applyInvisibility"))
                    .collect(java.util.stream.Collectors.toSet()));
            if (model != null) sync.rebind(model, built, live, new ArrayList<>());
        }), 2L);
        report.notes.add("Model を使う Mob はモデルの再設定が 2tick 後に反映されます");
    }

    private void removeTotems(MobType old) {
        Map<?, Collection<?>> totems = ObjectTransplant.getField(mythic.getMobManager().getTotemManager(), "mobTotems");
        for (Collection<?> list : totems.values()) {
            tx.snapshot(list);
            list.removeIf(totem -> ObjectTransplant.getField(totem, "mobType") == old);
        }
    }

    private static void updateDisplay(Map<String, MythicMob> lookup, Map<MythicMob, String> keys, MythicMob mob, MythicConfig config) {
        String previous = keys.remove(mob);
        if (previous != null) lookup.remove(previous, mob);
        String display = config.getString(new String[]{"Display", "DisplayName"});
        if (display != null) {
            lookup.put(display, mob);
            keys.put(mob, display);
        }
    }

    // ---------------------------------------------------------------- items

    private void reloadItems(Map<File, Pack> files) {
        ItemExecutor executor = mythic.getItemManager();
        Map<String, MythicItem> items = ObjectTransplant.getField(executor, "items");
        Map<String, MythicItem> before = new HashMap<>(items);
        tx.snapshot(items);
        Set<String> nodes = new LinkedHashSet<>();
        List<TemplateUtil.TemplateItem> templates = new ArrayList<>();
        for (Map.Entry<File, Pack> entry : files.entrySet()) {
            File file = entry.getKey();
            Pack pack = entry.getValue();
            MythicConfigImpl fileConfig = new MythicConfigImpl(file);
            boolean mono = isMono(file);
            for (String node : NodeListProp.getNodes(file, "")) {
                nodes.add(node);
                MythicItem created;
                try {
                    MythicConfig config = fileConfig.getNestedConfig(node);
                    if (mono && (config.isSet("Type") && MythicEntityType.get(config.getString("Type")) != null
                            || config.isSet("Skills") && !config.isSet("Id"))) continue;
                    String template = config.getString("Template");
                    if (template == null) template = config.getString("Templates");
                    if (template != null) {
                        templates.add(new TemplateUtil.TemplateItem(file, pack, node, config, template));
                        continue;
                    }
                    created = new MythicItem(pack, file, node, config);
                } catch (Exception | Error ex) {
                    report.errors.add("アイテム " + node + ": " + ex);
                    continue;
                }
                installItem(items, pack, node, created, before.get(node));
            }
        }
        if (!templates.isEmpty()) {
            // TemplateUtil registers straight into the executor and refuses existing names
            templates.forEach(t -> items.remove(t.id()));
            TemplateUtil.handleTemplates(templates, items);
            for (TemplateUtil.TemplateItem template : templates) {
                MythicItem created = items.get(template.id());
                if (created != null) installItem(items, template.pack(), template.id(), created, before.get(template.id()));
            }
            report.notes.add("Template を使うアイテムを含みます。リロード対象外のファイルにある子アイテムは更新されません");
        }
        for (Map.Entry<String, MythicItem> entry : before.entrySet()) {
            if (!nodes.contains(entry.getKey()) && files.containsKey(canonical(entry.getValue().getFile()))) {
                report.notInFile.add("アイテム " + entry.getKey());
            }
        }
    }

    private void installItem(Map<String, MythicItem> items, Pack pack, String name, MythicItem created, @Nullable MythicItem old) {
        MythicItem installed;
        String previousDisplayKey = old != null ? CrucibleBridge.displayKey(old) : null;
        if (old != null && old.getClass() == created.getClass()) {
            staleMechanics.addAll(ObjectTransplant.collectMechanics(old));
            tx.copyFields(created, old, Collections.emptySet());
            tx.rebind(old, created, old, new ArrayList<>());
            installed = old;
            replacedItems.add(old);
            report.replaced.add("アイテム " + name);
        } else {
            installed = created;
            if (old == null) {
                pack.addItem();
                report.added.add("アイテム " + name);
            }
        }
        items.put(name, installed);
        if (crucible != null) {
            try {
                crucible.refresh(installed, old, previousDisplayKey, staleMechanics, tx);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Crucible アイテム " + name + " の再登録に失敗: " + ex, ex);
            }
        }
    }

    // ---------------------------------------------------------------- shared

    private static Map<File, Pack> filesOf(Map<File, Map<ConfigFileType, Pack>> typesByFile, ConfigFileType type) {
        Map<File, Pack> files = new LinkedHashMap<>();
        typesByFile.forEach((file, types) -> {
            Pack pack = types.get(type);
            if (pack != null) files.put(file, pack);
        });
        return files;
    }

    /** The Mobs / Skills / Items folders of every loaded pack. */
    public static @NotNull List<File> configRoots() {
        List<File> roots = new ArrayList<>();
        for (ConfigFileType type : List.of(ConfigFileType.ITEM, ConfigFileType.SKILL, ConfigFileType.MOB)) {
            for (Pack pack : MythicBukkit.inst().getPackManager().getPacks()) {
                for (File folder : pack.getPackFolders(type.info.dataFolder(), false, type.info.readMono())) {
                    roots.add(canonical(folder));
                }
            }
        }
        return roots;
    }

    private Map<ConfigFileType, Pack> resolveTypes(File file) {
        Map<ConfigFileType, Pack> types = new LinkedHashMap<>();
        for (ConfigFileType type : List.of(ConfigFileType.ITEM, ConfigFileType.SKILL, ConfigFileType.MOB)) {
            Pack pack = knownFiles(type).get(file);
            if (pack != null) {
                types.put(type, pack);
                continue;
            }
            // A file created after the last full reload: register it with the pack that owns its folder
            for (Pack candidate : mythic.getPackManager().getPacks()) {
                for (File folder : candidate.getPackFolders(type.info.dataFolder(), false, type.info.readMono())) {
                    if (file.toPath().startsWith(canonical(folder).toPath())) {
                        tx.snapshot(candidate.getConfigFiles(type));
                        candidate.getConfigFiles(type).add(file);
                        types.put(type, candidate);
                        report.notes.add(describe(file) + " を新規ファイルとして " + type.name() + " に登録しました");
                        break;
                    }
                }
                if (types.containsKey(type)) break;
            }
        }
        return types;
    }

    private void clearComponentCaches() {
        SkillExecutor executor = mythic.getSkillManager();
        for (String cache : List.of("mechanicCache", "targeterCache", "conditionCache")) {
            ((Map<?, ?>) ObjectTransplant.getField(executor, cache)).clear();
        }
    }

    /** Drops the replaced mechanics from the parent lists of every skill they used to call. */
    private void pruneStaleParents() {
        if (staleMechanics.isEmpty()) return;
        for (Skill skill : mythic.getSkillManager().getSkills()) {
            if (!(skill instanceof MetaSkill meta)) continue;
            Collection<?> parents = ObjectTransplant.getField(meta, "parents");
            Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
            parents.removeIf(p -> staleMechanics.contains(p) || !seen.add(p));
        }
    }

    private void refreshStats(boolean items) {
        if (!replacedMobs.isEmpty()) {
            for (ActiveMob mob : mythic.getMobManager().getActiveMobs()) {
                if (!replacedMobs.contains(mob.getType())) continue;
                try {
                    mob.getStatRegistry().refresh();
                } catch (Exception | Error ignored) {
                }
            }
        }
        if (items) {
            mythic.getPlayerManager().getKnownProfiles().forEach(profile -> {
                try {
                    profile.getStatRegistry().refresh();
                } catch (Exception | Error ignored) {
                }
            });
        }
    }

    private static boolean isMono(File file) {
        return file.getParentFile() != null && file.getParentFile().getName().equals("mono");
    }

    /** Canonical paths are slow to resolve (thousands of skills share a few hundred files), so they are cached. */
    private static final Map<String, File> CANONICAL_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    static @NotNull File canonical(@Nullable File file) {
        Objects.requireNonNull(file);
        return CANONICAL_CACHE.computeIfAbsent(file.getPath(), path -> {
            try {
                return file.getCanonicalFile();
            } catch (IOException e) {
                return file.getAbsoluteFile();
            }
        });
    }

    public static @NotNull String describe(@NotNull File file) {
        File data = canonical(MythicBukkit.inst().getDataFolder());
        return data.toPath().relativize(canonical(file).toPath()).toString().replace(File.separatorChar, '/');
    }
}
