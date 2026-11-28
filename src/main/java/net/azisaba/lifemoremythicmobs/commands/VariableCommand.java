package net.azisaba.lifemoremythicmobs.commands;

import io.lumine.mythic.core.skills.variables.Variable;
import io.lumine.mythic.core.skills.variables.VariableRegistry;
import io.lumine.mythic.core.skills.variables.VariableType;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.util.ItemUtil;
import net.azisaba.lifemoremythicmobs.util.LegacyText;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Reads and overwrites the MythicMobs variables (caster scope) of an online player while the server is running.
 * Editing data/players/*.json by hand does not work for online players because the in-memory registry wins.
 */
public class VariableCommand extends SubCommand {
    private static final List<String> ACTIONS = List.of("set", "get", "remove", "list");
    /** Types that can be written from a single command argument. */
    private static final List<VariableType> TYPES = List.of(
            VariableType.INTEGER, VariableType.FLOAT, VariableType.LONG,
            VariableType.DOUBLE, VariableType.BOOLEAN, VariableType.STRING);
    private static final int MAX_LISTED = 60;
    /** MythicMobs: 0 = saved to disk, 1 = kept in memory only. */
    private static final long EXPIRE_SAVED = 0L;
    private static final long EXPIRE_TEMP = 1L;

    public VariableCommand(LifeMoreMythicMobs plugin) {
    }

    @Override
    public @NotNull String getName() {
        return "var";
    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of("variable");
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String[] args) {
        execute((CommandSender) player, args);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2 || !ACTIONS.contains(args[0].toLowerCase(Locale.ROOT))) {
            sendUsage(sender);
            return;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(LegacyText.RED + "オンラインのプレイヤーが見つかりません: " + args[1]);
            return;
        }
        VariableRegistry registry = ItemUtil.getPlayerVariable(target);
        if (registry == null) {
            sender.sendMessage(LegacyText.RED + "MythicMobs のプレイヤーデータがまだ読み込まれていません: " + target.getName());
            return;
        }
        String[] rest = Arrays.copyOfRange(args, 2, args.length);
        switch (action) {
            case "set" -> set(sender, target, registry, rest);
            case "get" -> get(sender, target, registry, rest);
            case "remove" -> remove(sender, target, registry, rest);
            default -> list(sender, target, registry, rest);
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(LegacyText.RED + "使用法:");
        sender.sendMessage(LegacyText.GRAY + " /lmmm var set <プレイヤー> <変数名> <値> [type=INTEGER|FLOAT|LONG|DOUBLE|BOOLEAN|STRING] [save=true|false]");
        sender.sendMessage(LegacyText.GRAY + " /lmmm var get <プレイヤー> <変数名>");
        sender.sendMessage(LegacyText.GRAY + " /lmmm var remove <プレイヤー> <変数名>");
        sender.sendMessage(LegacyText.GRAY + " /lmmm var list <プレイヤー> [絞り込み]");
    }

    private void set(CommandSender sender, Player target, VariableRegistry registry, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }
        String name = args[0];
        VariableType type = null;
        Boolean save = null;
        // Options are read from the end so that a value may contain spaces.
        int end = args.length;
        while (end > 2) {
            String token = args[end - 1].toLowerCase(Locale.ROOT);
            if (token.startsWith("type=") && type == null) {
                type = parseType(token.substring(5));
                if (type == null) {
                    sender.sendMessage(LegacyText.RED + "使えない型です: " + args[end - 1].substring(5)
                            + LegacyText.GRAY + " (" + typeNames() + ")");
                    return;
                }
            } else if ((token.equals("save=true") || token.equals("save=false")) && save == null) {
                save = token.equals("save=true");
            } else {
                break;
            }
            end--;
        }
        String raw = String.join(" ", Arrays.copyOfRange(args, 1, end));

        Variable old = registry.get(name);
        if (type == null) {
            type = old != null && TYPES.contains(old.getType()) ? old.getType() : guessType(raw);
        }
        long expire = save != null ? (save ? EXPIRE_SAVED : EXPIRE_TEMP)
                : old != null ? old.getExpirationTime() : EXPIRE_TEMP;

        Object value;
        try {
            value = parseValue(type, raw);
        } catch (NumberFormatException e) {
            sender.sendMessage(LegacyText.RED + "値 \"" + raw + "\" を " + type + " として読めません。"
                    + LegacyText.GRAY + " 型を変えるときは type= を付けてください。");
            return;
        }
        Variable created = Variable.ofType(type, value, expire);
        if (created == null) {
            sender.sendMessage(LegacyText.RED + "変数を作れませんでした: " + type);
            return;
        }
        registry.put(name, created);
        sender.sendMessage(LegacyText.GREEN + target.getName() + " の " + name + " を設定しました: "
                + LegacyText.WHITE + (old != null ? describe(old) : "(なし)")
                + LegacyText.GRAY + " → " + LegacyText.WHITE + describe(created));
    }

    private void get(CommandSender sender, Player target, VariableRegistry registry, String[] args) {
        if (args.length != 1) {
            sendUsage(sender);
            return;
        }
        Variable variable = registry.get(args[0]);
        if (variable == null) {
            sender.sendMessage(LegacyText.YELLOW + target.getName() + " に変数 " + args[0] + " はありません。");
            return;
        }
        sender.sendMessage(LegacyText.AQUA + target.getName() + " の " + args[0] + ": " + LegacyText.WHITE + describe(variable));
    }

    private void remove(CommandSender sender, Player target, VariableRegistry registry, String[] args) {
        if (args.length != 1) {
            sendUsage(sender);
            return;
        }
        Variable old = registry.get(args[0]);
        if (old == null) {
            sender.sendMessage(LegacyText.YELLOW + target.getName() + " に変数 " + args[0] + " はありません。");
            return;
        }
        registry.remove(args[0]);
        sender.sendMessage(LegacyText.GREEN + target.getName() + " の " + args[0] + " を消しました: " + LegacyText.WHITE + describe(old));
    }

    private void list(CommandSender sender, Player target, VariableRegistry registry, String[] args) {
        String filter = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";
        Map<String, Variable> matched = new TreeMap<>();
        registry.asMap().forEach((key, variable) -> {
            if (key.toLowerCase(Locale.ROOT).contains(filter)) {
                matched.put(key, variable);
            }
        });
        sender.sendMessage(LegacyText.AQUA + target.getName() + " の変数: " + matched.size() + " 個"
                + (filter.isEmpty() ? "" : LegacyText.GRAY + " (\"" + args[0] + "\" を含む)"));
        matched.entrySet().stream().limit(MAX_LISTED).forEach(entry ->
                sender.sendMessage(LegacyText.GRAY + " " + entry.getKey() + " = " + LegacyText.WHITE + describe(entry.getValue())));
        if (matched.size() > MAX_LISTED) {
            sender.sendMessage(LegacyText.GRAY + " ...ほか " + (matched.size() - MAX_LISTED) + " 個 (絞り込みを付けてください)");
        }
    }

    private static String describe(Variable variable) {
        long expire = variable.getExpirationTime();
        String life = expire == EXPIRE_SAVED ? "保存" : expire == EXPIRE_TEMP ? "一時" : "期限付き";
        return variable + LegacyText.GRAY + " [" + variable.getType() + "・" + life + "]";
    }

    private static @Nullable VariableType parseType(String name) {
        return TYPES.stream().filter(type -> type.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    private static String typeNames() {
        return TYPES.stream().map(Enum::name).collect(Collectors.joining("|"));
    }

    private static VariableType guessType(String raw) {
        if (raw.matches("-?\\d{1,9}")) {
            return VariableType.INTEGER;
        }
        if (raw.matches("-?\\d+\\.\\d+")) {
            return VariableType.FLOAT;
        }
        return VariableType.STRING;
    }

    private static Object parseValue(VariableType type, String raw) {
        return switch (type) {
            case INTEGER -> Integer.parseInt(raw);
            case LONG -> Long.parseLong(raw);
            case FLOAT -> Float.parseFloat(raw);
            case DOUBLE -> Double.parseDouble(raw);
            case BOOLEAN -> {
                if (!raw.equalsIgnoreCase("true") && !raw.equalsIgnoreCase("false")) {
                    throw new NumberFormatException(raw);
                }
                yield Boolean.parseBoolean(raw);
            }
            default -> raw;
        };
    }

    @Override
    public @NotNull List<String> suggest(@NotNull CommandSender sender, @NotNull String[] args) {
        String last = args[args.length - 1].toLowerCase(Locale.ROOT);
        List<String> candidates = new ArrayList<>();
        if (args.length == 1) {
            candidates.addAll(ACTIONS);
        } else if (args.length == 2) {
            Bukkit.getOnlinePlayers().forEach(player -> candidates.add(player.getName()));
        } else if (args.length == 3) {
            Player target = Bukkit.getPlayerExact(args[1]);
            VariableRegistry registry = target != null ? ItemUtil.getPlayerVariable(target) : null;
            if (registry != null) {
                candidates.addAll(registry.asMap().keySet());
            }
        } else if (args[0].equalsIgnoreCase("set") && args.length >= 5) {
            TYPES.forEach(type -> candidates.add("type=" + type.name()));
            candidates.add("save=true");
            candidates.add("save=false");
        } else {
            return Collections.emptyList();
        }
        return candidates.stream()
                .filter(candidate -> candidate.toLowerCase(Locale.ROOT).startsWith(last))
                .sorted()
                .limit(50)
                .collect(Collectors.toList());
    }
}
