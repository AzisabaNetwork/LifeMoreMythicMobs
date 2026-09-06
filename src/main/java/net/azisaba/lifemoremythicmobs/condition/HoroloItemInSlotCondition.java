package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.conditions.ISkillMetaCondition;
import io.lumine.mythic.core.skills.SkillCondition;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class HoroloItemInSlotCondition extends SkillCondition implements IEntityCondition, ISkillMetaCondition {
  private final String mmid, operator;
  private final int required;
  private final boolean invert;
  private final List<Integer> slots;

  public HoroloItemInSlotCondition(MythicLineConfig config) {
    super(config.getLine());
    mmid = config.getString(new String[] {"mmid", "id"}, "null");
    invert = config.getBoolean(new String[] {"invert", "i", "逆転"}, false);
    String[] count = parseCount(config.getString(new String[] {"count", "c"}, ">=1"));
    operator = count[0];
    required = Integer.parseInt(count[1]);
    slots = parseSlots(config.getString(new String[] {"slots", "s"}));
  }

  @Override
  public boolean check(AbstractEntity target) {
    if (!target.isPlayer()) return invert;
    int found = count((Player) target.getBukkitEntity());
    boolean result = switch (operator) {
      case "<=" -> found <= required;
      case ">" -> found > required;
      case "<" -> found < required;
      case "==" -> found == required;
      case "!=" -> found != required;
      default -> found >= required;
    };
    return invert != result;
  }

  /**
   * Evaluates regular inline conditions ({@code ?horoloItemInSlot{...}})
   * against the skill caster. IEntityCondition remains implemented so the
   * condition can still be used explicitly as a target condition.
   */
  @Override
  public boolean check(SkillMetadata metadata) {
    return check(metadata.getCaster().getEntity());
  }

  private int count(Player player) {
    try {
      Plugin plugin = Bukkit.getPluginManager().getPlugin("HoroloCore");
      if (plugin == null || !plugin.isEnabled()) return 0;
      return ((Number) plugin.getClass().getMethod("countStoredMythicItem", java.util.UUID.class, String.class, java.util.Collection.class).invoke(plugin, player.getUniqueId(), mmid, slots)).intValue();
    } catch (ReflectiveOperationException exception) {
      return 0;
    }
  }

  private static String[] parseCount(String raw) {
    if (raw == null || raw.isBlank()) return new String[] {">=", "1"};
    for (String op : new String[] {">=", "<=", "==", "!=", ">", "<", "="}) if (raw.startsWith(op)) try { return new String[] {op.equals("=") ? "==" : op, Integer.toString(Integer.parseInt(raw.substring(op.length())))}; } catch (NumberFormatException ignored) { return new String[] {">=", "1"}; }
    try { return new String[] {">=", Integer.toString(Integer.parseInt(raw))}; } catch (NumberFormatException ignored) { return new String[] {">=", "1"}; }
  }

  private static List<Integer> parseSlots(String raw) {
    List<Integer> result = new ArrayList<>();
    if (raw == null || raw.isBlank()) return result;
    for (String part : raw.split(",")) try { if (part.contains("-")) { String[] range=part.split("-"); int a=Integer.parseInt(range[0].trim()), b=Integer.parseInt(range[1].trim()); for(int i=Math.min(a,b);i<=Math.max(a,b);i++) if(i>=0&&i<27) result.add(i); } else { int slot=Integer.parseInt(part.trim()); if(slot>=0&&slot<27) result.add(slot); } } catch (RuntimeException ignored) {}
    return result.stream().distinct().toList();
  }
}
