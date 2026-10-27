package net.azisaba.lifemoremythicmobs.commands;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.constants.ConfigFileType;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.reload.MythicFileReloader;
import net.azisaba.lifemoremythicmobs.util.LegacyText;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReloadFileCommand extends SubCommand {
    private static final int MAX_LISTED = 15;

    private final LifeMoreMythicMobs plugin;

    public ReloadFileCommand(LifeMoreMythicMobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "reloadFile";
    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of("r");
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String[] args) {
        execute((CommandSender) player, args);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(LegacyText.RED + "使用法: /lmmm reloadFile <skills/xxx.yml | ファイル名 | skills/フォルダ | フォルダ名>");
            return;
        }
        String query = String.join(" ", args);
        Target target = resolve(query);
        if (target.files.isEmpty()) {
            sender.sendMessage(LegacyText.RED + "ファイルまたはフォルダが見つかりません: " + query);
            return;
        }
        if (!target.folder && target.files.size() > 1) {
            sender.sendMessage(LegacyText.RED + "同名のファイルが複数あります。パスで指定してください:");
            target.files.forEach(f -> sender.sendMessage(LegacyText.GRAY + " - " + MythicFileReloader.describe(f)));
            return;
        }
        String label = target.folder
                ? String.join(", ", target.folders) + " (" + target.files.size() + "ファイル)"
                : MythicFileReloader.describe(target.files.get(0));
        long start = System.currentTimeMillis();
        MythicFileReloader.Report report;
        try {
            report = new MythicFileReloader().reload(target.files);
        } catch (InvalidConfigurationException | IOException e) {
            // Checked before anything is touched
            sender.sendMessage(LegacyText.RED + "読み込みを中止しました(変更なし): " + e.getMessage());
            return;
        } catch (Exception e) {
            sender.sendMessage(LegacyText.RED + "リロードに失敗しました: " + e.getMessage());
            plugin.getLogger().log(Level.WARNING, "reloadFile " + label + " failed", e);
            return;
        }
        long took = System.currentTimeMillis() - start;
        sender.sendMessage(LegacyText.GREEN + label + " をリロードしました (" + took + "ms)");
        sender.sendMessage(LegacyText.AQUA + "更新: " + report.replaced.size() + "件 / 追加: " + report.added.size() + "件");
        list(sender, LegacyText.YELLOW + "追加されたエントリ", report.added);
        list(sender, LegacyText.YELLOW + "ファイルから消えたエントリ(全体リロードまで登録されたまま)", report.notInFile);
        list(sender, LegacyText.YELLOW + "注意", report.notes);
        list(sender, LegacyText.RED + "読み込みエラー", report.errors);
        list(sender, LegacyText.RED + "MythicMobs の警告", report.log);
        if (!report.added.isEmpty()) {
            sender.sendMessage(LegacyText.GRAY + "※ 追加したエントリをリロード対象外のファイルから呼んでいる場合、その呼び出し元のファイルもリロードしてください");
        }
    }

    private static void list(CommandSender sender, String title, List<String> lines) {
        if (lines.isEmpty()) return;
        sender.sendMessage(title + " (" + lines.size() + "件):");
        lines.stream().limit(MAX_LISTED).forEach(line -> sender.sendMessage(LegacyText.GRAY + " - " + line));
        if (lines.size() > MAX_LISTED) sender.sendMessage(LegacyText.GRAY + " ... 他 " + (lines.size() - MAX_LISTED) + "件(コンソールに全件出力)");
        if (lines.size() > MAX_LISTED || !(sender instanceof org.bukkit.command.ConsoleCommandSender)) {
            lines.forEach(line -> MythicBukkit.inst().getLogger().info("[reloadFile] " + line));
        }
    }

    private record Target(List<File> files, boolean folder, List<String> folders) {
    }

    private static TreeSet<File> allFiles() {
        TreeSet<File> files = new TreeSet<>();
        for (ConfigFileType type : List.of(ConfigFileType.MOB, ConfigFileType.SKILL, ConfigFileType.ITEM)) {
            files.addAll(MythicFileReloader.knownFiles(type).keySet());
        }
        return files;
    }

    /** The Mobs / Skills / Items folders and every folder below them (including ones created since the last reload). */
    private static TreeSet<File> allFolders() {
        TreeSet<File> folders = new TreeSet<>();
        for (File root : MythicFileReloader.configRoots()) {
            try (Stream<Path> walk = Files.walk(root.toPath())) {
                walk.filter(Files::isDirectory).map(Path::toFile).forEach(folders::add);
            } catch (IOException ignored) {
            }
        }
        return folders;
    }

    private static File dataFolder() {
        try {
            return MythicBukkit.inst().getDataFolder().getCanonicalFile();
        } catch (IOException e) {
            return MythicBukkit.inst().getDataFolder().getAbsoluteFile();
        }
    }

    private static Target resolve(String query) {
        String normalized = query.replace('\\', '/');
        if (normalized.contains("/")) {
            File path = new File(dataFolder(), normalized);
            if (path.isFile()) return new Target(List.of(path), false, List.of());
            if (path.isDirectory()) {
                return new Target(filesIn(path), true, List.of(MythicFileReloader.describe(path) + "/"));
            }
            return new Target(List.of(), false, List.of());
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        List<File> found = new ArrayList<>();
        for (File file : allFiles()) {
            String name = file.getName().toLowerCase(Locale.ROOT);
            if (name.equals(lower) || name.equals(lower + ".yml")) found.add(file);
        }
        if (!found.isEmpty()) return new Target(found, false, List.of());
        // No such file: every folder with that name (e.g. Horologium under items/, mobs/ and skills/)
        List<String> folders = new ArrayList<>();
        for (File dir : allFolders()) {
            if (!dir.getName().equalsIgnoreCase(normalized)) continue;
            folders.add(MythicFileReloader.describe(dir) + "/");
            for (File file : filesIn(dir)) {
                if (!found.contains(file)) found.add(file);
            }
        }
        return new Target(found, true, folders);
    }

    private static List<File> filesIn(File dir) {
        try (Stream<Path> walk = Files.walk(dir.toPath())) {
            return walk.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(f -> f.getName().toLowerCase(Locale.ROOT).endsWith(".yml"))
                    .filter(MythicFileReloader::isConfigFile)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    @Override
    public @NotNull List<String> suggest(@NotNull Player player, @NotNull String[] args) {
        return suggest((CommandSender) player, args);
    }

    @Override
    public @NotNull List<String> suggest(@NotNull CommandSender sender, @NotNull String[] args) {
        String prefix = String.join(" ", args).replace('\\', '/').toLowerCase(Locale.ROOT);
        Stream<String> folders = allFolders().stream().map(dir -> MythicFileReloader.describe(dir) + "/");
        Stream<String> files = allFiles().stream().map(MythicFileReloader::describe);
        return Stream.concat(folders, files)
                .filter(path -> path.toLowerCase(Locale.ROOT).startsWith(prefix)
                        || lastSegment(path).toLowerCase(Locale.ROOT).startsWith(prefix))
                .sorted()
                .limit(100)
                .collect(Collectors.toList());
    }

    private static String lastSegment(String path) {
        String trimmed = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        return trimmed.substring(trimmed.lastIndexOf('/') + 1);
    }
}
