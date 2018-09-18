package ru.rarescrap.tabinventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class StackAddEvent extends PlayerEvent {
    public ItemStack itemStack;

    public StackAddEvent(EntityPlayer player) {
        super(player);
    }
}
