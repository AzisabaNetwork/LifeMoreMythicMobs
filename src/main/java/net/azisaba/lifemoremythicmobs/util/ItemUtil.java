package net.azisaba.lifemoremythicmobs.util;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.variables.VariableRegistry;
import io.lumine.xikage.mythicmobs.skills.variables.VariableScope;
import io.lumine.xikage.mythicmobs.util.jnbt.CompoundTag;
import io.lumine.xikage.mythicmobs.util.jnbt.StringTag;
import io.lumine.xikage.mythicmobs.util.jnbt.Tag;
import net.minecraft.server.v1_15_R1.NBTBase;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemUtil {

    private static CompoundTag getCompoundTag(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return null;
        return MythicMobs.inst().getVolatileCodeHandler().getItemHandler().getNBTData(stack);
    }

    @Contract("null -> null")
    public static @Nullable String getMythicType(@Nullable ItemStack stack) {
        return getStringTag(stack, "MYTHIC_TYPE");
    }

    @Contract("null, _ -> null")
    public static @Nullable String getStringTag(@Nullable ItemStack stack, @NotNull String key) {
        CompoundTag tag = getCompoundTag(stack);
        if (tag == null || !tag.getValue().containsKey(key)) return null;
        return ((StringTag) tag.getValue().get(key)).getValue();
    }

    @Contract("null, _ -> null")
    public static @Nullable String resolveTagAsString(@Nullable ItemStack stack, @NotNull String keySeparatedByDot) {
        CompoundTag tag = getCompoundTag(stack);
        if (tag == null) return null;
        String[] keys = keySeparatedByDot.split("\\.");
        CompoundTag current = tag;
        for (int i = 0; i < keys.length - 1; i++) {
            Tag t = current.getValue().get(keys[i]);
            if (!(t instanceof CompoundTag)) return null;
            current = (CompoundTag) t;
        }
        Tag target = current.getValue().get(keys[keys.length - 1]);
        return (target != null) ? target.toString() : null;
    }

    public static String resolveVariable(SkillMetadata skillMetadata, String varName) {
        if (varName.startsWith("caster.")) {
            VariableRegistry registry = MythicMobs.inst().getVariableManager().getRegistry(VariableScope.CASTER, skillMetadata, skillMetadata.getCaster().getEntity());
            String name = varName.startsWith("caster.var.") ? varName.substring("caster.var.".length()) : varName.substring("caster.".length());
            return registry.getString(name);
        }
        if (varName.startsWith("target.")) {
            VariableRegistry registry = MythicMobs.inst().getVariableManager().getRegistry(VariableScope.CASTER, skillMetadata, skillMetadata.getTrigger());
            String name = varName.startsWith("target.var.") ? varName.substring("target.var.".length()) : varName.substring("target.".length());
            return registry.getString(name);
        }
        if (varName.startsWith("skill.")) {
            VariableRegistry registry = skillMetadata.getVariables();
            String name = varName.startsWith("skill.var.") ? varName.substring("skill.var.".length()) : varName.substring("skill.".length());
            return registry.getString(name);
        }
        if (varName.startsWith("global.")) {
            VariableRegistry registry = MythicMobs.inst().getVariableManager().getGlobalRegistry().get();
            String name = varName.startsWith("global.var.") ? varName.substring("global.var.".length()) : varName.substring("global.".length());
            return registry.getString(name);
        }
        return null;
    }

    public static void setVariable(SkillMetadata skillMetadata, String varName, String value) {
        if (varName.startsWith("caster.")) {
            VariableRegistry registry = MythicMobs.inst().getVariableManager().getRegistry(VariableScope.CASTER, skillMetadata, skillMetadata.getCaster().getEntity());
            String name = varName.startsWith("caster.var.") ? varName.substring("caster.var.".length()) : varName.substring("caster.".length());
            registry.putString(name, value);
        }
        if (varName.startsWith("target.")) {
            VariableRegistry registry = MythicMobs.inst().getVariableManager().getRegistry(VariableScope.CASTER, skillMetadata, skillMetadata.getTrigger());
            String name = varName.startsWith("target.var.") ? varName.substring("target.var.".length()) : varName.substring("target.".length());
            registry.putString(name, value);
        }
        if (varName.startsWith("skill.")) {
            VariableRegistry registry = skillMetadata.getVariables();
            String name = varName.startsWith("skill.var.") ? varName.substring("skill.var.".length()) : varName.substring("skill.".length());
            registry.putString(name, value);
        }
        if (varName.startsWith("global.")) {
            VariableRegistry registry = MythicMobs.inst().getVariableManager().getGlobalRegistry().get();
            String name = varName.startsWith("global.var.") ? varName.substring("global.var.".length()) : varName.substring("global.".length());
            registry.putString(name, value);
        }
    }

    @Contract("null, _ -> !null")
    public static String getLoreLine(@Nullable ItemStack stack, int lineNumber) {
        if (stack == null || stack.getType().isAir()) return "";
        if (!stack.hasItemMeta() || !stack.getItemMeta().hasLore()) return "";
        List<String> lore = stack.getItemMeta().getLore();
        if (lore == null) return "";
        if (lore.size() <= lineNumber) return "";
        return lore.get(lineNumber);
    }

    @Contract("null -> !null")
    public static String getDisplayName(@Nullable ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return "";
        if (!stack.hasItemMeta() || !stack.getItemMeta().hasDisplayName()) return "";
        return stack.getItemMeta().getDisplayName();
    }

    public static @NotNull VariableRegistry getPlayerVariable(@NotNull Player player) {
        return MythicMobs.inst().getPlayerManager().getPlayerData(BukkitAdapter.adapt(player)).getVariables();
    }
}
