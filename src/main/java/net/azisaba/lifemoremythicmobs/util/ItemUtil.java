package net.azisaba.lifemoremythicmobs.util;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.variables.VariableRegistry;
import io.lumine.xikage.mythicmobs.skills.variables.VariableScope;
import net.minecraft.server.v1_15_R1.NBTBase;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemUtil {

    @Contract("null -> null")
    public static @Nullable String getMythicType(@Nullable ItemStack stack) {
        String type = getStringTag(stack, "MYTHIC_TYPE");
        if (type == null || type.isEmpty()) return null;
        return type;
    }

    @Contract("null, _ -> null")
    public static @Nullable String getStringTag(@Nullable ItemStack stack, @NotNull String key) {
        if (stack == null || stack.getType().isAir()) return null;
        NBTTagCompound tag = CraftItemStack.asNMSCopy(stack).getTag();
        if (tag == null) return null;
        return tag.getString(key);
    }

    @Contract("null, _ -> null")
    public static @Nullable String getTagAsString(@Nullable ItemStack stack, @NotNull String key) {
        if (stack == null || stack.getType().isAir()) return null;
        NBTTagCompound tag = CraftItemStack.asNMSCopy(stack).getTag();
        if (tag == null) return null;
        NBTBase base = tag.get(key);
        if (base == null) return null;
        return base.asString();
    }

    @Contract("null, _ -> null")
    public static @Nullable String resolveTagAsString(@Nullable ItemStack stack, @NotNull String keySeparatedByDot) {
        if (stack == null || stack.getType().isAir()) return null;
        NBTTagCompound tag = CraftItemStack.asNMSCopy(stack).getTag();
        if (tag == null) return null;
        List<String> keys = new ArrayList<>(Arrays.asList(keySeparatedByDot.split("\\.")));
        keys.remove(keys.size() - 1);
        for (String key : keys) {
            tag = tag.getCompound(key);
        }
        NBTBase base = tag.get(keySeparatedByDot.split("\\.")[keys.size()]);
        if (base == null) return null;
        return base.asString();
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
}
