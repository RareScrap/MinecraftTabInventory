package ru.rarescrap.tabinventory.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import ru.rarescrap.tabinventory.network.syns.Change;

public class StackAddToTabEvent extends PlayerEvent { // TODO: Название бы поменять
    public Change change;

    public StackAddToTabEvent(EntityPlayer player, Change change) {
        super(player);
        this.change = change;
    }
}
