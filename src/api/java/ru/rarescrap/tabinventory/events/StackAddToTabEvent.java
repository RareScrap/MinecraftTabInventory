package ru.rarescrap.tabinventory.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityEvent;

public class StackAddToTabEvent extends EntityEvent {
    public ItemStack previouStack;
    public ItemStack currentStack;
    public int slotIndex;

    public StackAddToTabEvent(Entity entity, ItemStack previous, ItemStack current, int slotIndex) {
        super(entity);
        previouStack = previous;
        currentStack = current;
        this.slotIndex = slotIndex;
    }

    public boolean isLivingEntity() {
        return entity instanceof EntityLivingBase;
    }

    public EntityLivingBase getLivingEntity() {
        return isLivingEntity() ? ((EntityLivingBase) entity) : null;
    }
}
