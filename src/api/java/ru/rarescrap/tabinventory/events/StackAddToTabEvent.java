package ru.rarescrap.tabinventory.events;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import ru.rarescrap.tabinventory.TabInventory;
import ru.rarescrap.tabinventory.network.syns.Change;

/**
 * StackAddToTabEvent выбрасывается когда в {@link TabInventory} изменяется содержимое слота.<br>
 * Если вам нужно изменять содержимое {@link TabInventory.Tab} без выбрасывания евента,
 * попробуйте изменять {@link TabInventory.Tab#stacks} напрямую.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public class StackAddToTabEvent extends PlayerEvent { // TODO: Название бы поменять
    public Change change;

    protected StackAddToTabEvent(EntityPlayer player, Change change) {
        super(player);
        this.change = change;
    }

    /**
     * Pre выбрасывается перед тем, как в {@link TabInventory} изменится содержимое слота.<br>
     * <br>
     * This event is not {@link Cancelable}.
     * <br>
     * This event does not have a result. {@link HasResult}
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     **/
    public static class Pre extends StackAddToTabEvent {

        public Pre(EntityPlayer player, Change change) {
            super(player, change);
        }
    }

    /**
     * Post выбрасывается после того, как в {@link TabInventory} изменится содержимое слота.<br>
     * <br>
     * This event is not {@link Cancelable}.
     * <br>
     * This event does not have a result. {@link HasResult}
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     **/
    public static class Post extends StackAddToTabEvent {

        public Post(EntityPlayer player, Change change) {
            super(player, change);
        }
    }
}
