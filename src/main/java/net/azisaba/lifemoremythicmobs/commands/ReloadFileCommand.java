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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
    public void execute(@NotNull Player player, @NotNull String[] args) {
        execute((CommandSender) player, args);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(LegacyText.RED + "使用法: /lmmm reloadFile <skills/xxx.yml | ファイル名>");
            return;
        }
        String query = String.join(" ", args);
        List<File> matches = resolve(query);
        if (matches.isEmpty()) {
            sender.sendMessage(LegacyText.RED + "ファイルが見つかりません: " + query);
            return;
        }
        if (matches.size() > 1) {
            sender.sendMessage(LegacyText.RED + "同名のファイルが複数あります。パスで指定してください:");
            matches.forEach(f -> sender.sendMessage(LegacyText.GRAY + " - " + MythicFileReloader.describe(f)));
            return;
        }
        File file = matches.get(0);
        long start = System.currentTimeMillis();
        MythicFileReloader.Report report;
        try {
            report = new MythicFileReloader().reload(file);
        } catch (InvalidConfigurationException | IOException e) {
            // Checked before anything is touched
            sender.sendMessage(LegacyText.RED + "読み込みを中止しました(変更なし): " + e.getMessage());
            return;
        } catch (Exception e) {
            sender.sendMessage(LegacyText.RED + "リロードに失敗しました: " + e.getMessage());
            plugin.getLogger().log(Level.WARNING, "reloadFile " + file + " failed", e);
            return;
        }
        long took = System.currentTimeMillis() - start;
        sender.sendMessage(LegacyText.GREEN + MythicFileReloader.describe(file) + " をリロードしました (" + took + "ms)");
        sender.sendMessage(LegacyText.AQUA + "更新: " + report.replaced.size() + "件 / 追加: " + report.added.size() + "件");
        list(sender, LegacyText.YELLOW + "追加されたエントリ", report.added);
        list(sender, LegacyText.YELLOW + "ファイルから消えたエントリ(全体リロードまで登録されたまま)", report.notInFile);
        list(sender, LegacyText.YELLOW + "注意", report.notes);
        list(sender, LegacyText.RED + "読み込みエラー", report.errors);
        list(sender, LegacyText.RED + "MythicMobs の警告", report.log);
        if (!report.added.isEmpty()) {
            sender.sendMessage(LegacyText.GRAY + "※ 追加したエントリを他ファイルから呼んでいる場合、その呼び出し元のファイルもリロードしてください");
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

    private static TreeSet<File> allFiles() {
        TreeSet<File> files = new TreeSet<>();
        for (ConfigFileType type : List.of(ConfigFileType.MOB, ConfigFileType.SKILL, ConfigFileType.ITEM)) {
            files.addAll(MythicFileReloader.knownFiles(type).keySet());
        }
        return files;
    }

    private static List<File> resolve(String query) {
        String normalized = query.replace('\\', '/');
        if (normalized.contains("/")) {
            File file = new File(MythicBukkit.inst().getDataFolder(), normalized);
            return file.isFile() ? List.of(file) : List.of();
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        List<File> found = new ArrayList<>();
        for (File file : allFiles()) {
            String name = file.getName().toLowerCase(Locale.ROOT);
            if (name.equals(lower) || name.equals(lower + ".yml")) found.add(file);
        }
        return found;
    }

    @Override
    public @NotNull List<String> suggest(@NotNull Player player, @NotNull String[] args) {
        return suggest((CommandSender) player, args);
    }

    @Override
    public @NotNull List<String> suggest(@NotNull CommandSender sender, @NotNull String[] args) {
        String prefix = String.join(" ", args).replace('\\', '/').toLowerCase(Locale.ROOT);
        return allFiles().stream()
                .map(MythicFileReloader::describe)
                .filter(path -> path.toLowerCase(Locale.ROOT).startsWith(prefix)
                        || path.substring(path.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT).startsWith(prefix))
                .limit(100)
                .collect(Collectors.toList());
    }
}
