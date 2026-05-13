package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition;

import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemInSlotCondition extends SkillCondition implements IEntityCondition {

    private static final int MAX_SLOT = 40;

    private final boolean invert;
    private final String mmid;
    private String operator = ">=";
    private int requiredCount = 1;
    private final List<Integer> slotsToCheck;

    public ItemInSlotCondition(MythicLineConfig config) {
        super(config.getLine());

        this.mmid = config.getString(new String[]{"mmid", "id"}, "null");
        this.invert = config.getBoolean(new String[]{"invert", "i", "逆転"}, false);
        String countRaw = config.getString(new String[]{"count", "c"}, ">=1");
        parseCount(countRaw);
        String slotsString = config.getString(new String[]{"slots", "s"});
        this.slotsToCheck = parseSlots(slotsString);
    }

    private void parseCount(String raw) {
        if (raw == null || raw.isEmpty()) return;

        if (raw.startsWith(">=")) {
            this.operator = ">=";
            this.requiredCount = Integer.parseInt(raw.substring(2));
        } else if (raw.startsWith("<=")) {
            this.operator = "<=";
            this.requiredCount = Integer.parseInt(raw.substring(2));
        } else if (raw.startsWith("==")) {
            this.operator = "==";
            this.requiredCount = Integer.parseInt(raw.substring(2));
        } else if (raw.startsWith("!=")) {
            this.operator = "!=";
            this.requiredCount = Integer.parseInt(raw.substring(2));
        } else if (raw.startsWith(">")) {
            this.operator = ">";
            this.requiredCount = Integer.parseInt(raw.substring(1));
        } else if (raw.startsWith("<")) {
            this.operator = "<";
            this.requiredCount = Integer.parseInt(raw.substring(1));
        } else if (raw.startsWith("=")) {
            this.operator = "==";
            this.requiredCount = Integer.parseInt(raw.substring(1));
        } else {
            this.operator = ">=";
            try {
                this.requiredCount = Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                this.requiredCount = 1;
            }
        }
    }

    @Override
    public boolean check(AbstractEntity target) {
        if (!target.isPlayer()) {
            return this.invert;
        }

        Player player = (Player) target.getBukkitEntity();
        int foundCount = 0;

        for (int slot : this.slotsToCheck) {
            if (slot < 0 || slot > MAX_SLOT) continue;

            ItemStack item = player.getInventory().getItem(slot);

            if (item != null && item.getAmount() > 0) {
                String itemMMID = getMMIDFromNBT(item);

                if (this.mmid.equalsIgnoreCase(itemMMID)) {
                    foundCount += item.getAmount();
                }
            }
        }

        boolean result = compare(foundCount);
        return this.invert != result;
    }

    private boolean compare(int found) {
        switch (this.operator) {
            case ">=": return found >= this.requiredCount;
            case "<=": return found <= this.requiredCount;
            case ">":  return found > this.requiredCount;
            case "<":  return found < this.requiredCount;
            case "==": return found == this.requiredCount;
            case "!=": return found != this.requiredCount;
            default:   return found >= this.requiredCount;
        }
    }

    private String getMMIDFromNBT(ItemStack item) {
        try {
            net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
            if (!nmsItem.hasTag()) return null;
            NBTTagCompound tag = nmsItem.getTag();
            if (tag == null || !tag.hasKey("MYTHIC_TYPE")) return null;

            return tag.getString("MYTHIC_TYPE");
        } catch (Exception e) {
            return null;
        }
    }

    private List<Integer> parseSlots(String slotsString) {
        List<Integer> slots = new ArrayList<>();
        if (slotsString == null || slotsString.isEmpty()) return slots;

        String[] parts = slotsString.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.contains("-")) {
                String[] range = part.split("-");
                if (range.length == 2) {
                    try {
                        int start = Integer.parseInt(range[0].trim());
                        int end = Integer.parseInt(range[1].trim());

                        for (int i = Math.min(start, end); i <= Math.max(start, end); i++) {
                            if (i >= 0 && i <= MAX_SLOT) {
                                slots.add(i);
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } else {
                try {
                    int slot = Integer.parseInt(part);
                    if (slot >= 0 && slot <= MAX_SLOT) {
                        slots.add(slot);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return slots.stream().distinct().collect(Collectors.toList());
    }
}