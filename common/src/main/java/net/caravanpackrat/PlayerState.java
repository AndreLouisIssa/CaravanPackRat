package net.caravanpackrat;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class PlayerState {
    public BlockPos depot = null;
    public DefaultedList<ItemStack> items = DefaultedList.of();
}