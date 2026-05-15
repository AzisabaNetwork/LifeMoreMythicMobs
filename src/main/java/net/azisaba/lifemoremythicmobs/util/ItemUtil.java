package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.core.skills.variables.VariableRegistry;
import io.lumine.mythic.core.skills.variables.VariableScope;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemUtil {

    @Contract("null -> null")
    @Nullable
    public static String getMythicType(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        return MythicBukkit.inst().getItemManager().getMythicTypeFromItem(item);
    }

    @Contract("null, _ -> null")
    public static @Nullable String resolveTagAsString(@Nullable ItemStack stack, @NotNull String keySeparatedByDot) {
        if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) return null;
        ItemMeta meta = stack.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String[] parts = keySeparatedByDot.split("\\.");
        if (parts.length < 2) return null;
        NamespacedKey key = new NamespacedKey(parts[0], parts[1]);
        if (pdc.has(key, PersistentDataType.STRING)) {
            return pdc.get(key, PersistentDataType.STRING);
        }
        return null;
    }

    public static String resolveVariable(SkillMetadata skillMetadata, String varName) {
        if (varName.startsWith("caster.")) {
            VariableRegistry registry = MythicBukkit.inst().getVariableManager().getRegistry(VariableScope.CASTER, skillMetadata, skillMetadata.getCaster().getEntity());
            String name = varName.startsWith("caster.var.") ? varName.substring("caster.var.".length()) : varName.substring("caster.".length());
            return registry.getString(name);
        }
        if (varName.startsWith("target.")) {
            VariableRegistry registry = MythicBukkit.inst().getVariableManager().getRegistry(VariableScope.CASTER, skillMetadata, skillMetadata.getTrigger());
            String name = varName.startsWith("target.var.") ? varName.substring("target.var.".length()) : varName.substring("target.".length());
            return registry.getString(name);
        }
        if (varName.startsWith("skill.")) {
            VariableRegistry registry = skillMetadata.getVariables();
            String name = varName.startsWith("skill.var.") ? varName.substring("skill.var.".length()) : varName.substring("skill.".length());
            return registry.getString(name);
        }
        if (varName.startsWith("global.")) {
            VariableRegistry registry = MythicBukkit.inst().getVariableManager().getGlobalRegistry();
            String name = varName.startsWith("global.var.") ? varName.substring("global.var.".length()) : varName.substring("global.".length());
            return registry.getString(name);
        }
        return null;
    }

    public static void setVariable(SkillMetadata skillMetadata, String varName, String value) {
        if (varName.startsWith("caster.")) {
            VariableRegistry registry = MythicBukkit.inst().getVariableManager().getRegistry(VariableScope.CASTER, skillMetadata, skillMetadata.getCaster().getEntity());
            String name = varName.startsWith("caster.var.") ? varName.substring("caster.var.".length()) : varName.substring("caster.".length());
            registry.putString(name, value);
        }
        if (varName.startsWith("target.")) {
            VariableRegistry registry = MythicBukkit.inst().getVariableManager().getRegistry(VariableScope.CASTER, skillMetadata, skillMetadata.getTrigger());
            String name = varName.startsWith("target.var.") ? varName.substring("target.var.".length()) : varName.substring("target.".length());
            registry.putString(name, value);
        }
        if (varName.startsWith("skill.")) {
            VariableRegistry registry = skillMetadata.getVariables();
            String name = varName.startsWith("skill.var.") ? varName.substring("skill.var.".length()) : varName.substring("skill.".length());
            registry.putString(name, value);
        }
        if (varName.startsWith("global.")) {
            VariableRegistry registry = MythicBukkit.inst().getVariableManager().getGlobalRegistry();
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
        return MythicBukkit.inst().getPlayerManager().getProfile(player).getVariables();
    }
}
