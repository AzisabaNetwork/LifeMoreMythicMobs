package lifemoremythicmobs.org.example.lifemoremythicmobs.Util;

import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
}
