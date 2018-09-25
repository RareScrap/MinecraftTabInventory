package ru.rarescrap.example;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import static ru.rarescrap.example.TabInventoryExample.MODID;

public class BagItem extends Item {
    public static final Item ITEM = new BagItem().setCreativeTab(CreativeTabs.tabTools);

    public BagItem() {
        setUnlocalizedName("bagItem");
        setTextureName(MODID + ":bag_item");
        setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer playerEntity) {
        if (!p_77659_2_.isRemote) // TODO !!!
            playerEntity.openGui(
                    TabInventoryExample.instance,
                    TabInventoryExample.BAG_GUI_CODE,
                    playerEntity.worldObj,
                    (int) playerEntity.posX,
                    (int) playerEntity.posY,
                    (int) playerEntity.posZ);
        return super.onItemRightClick(p_77659_1_, p_77659_2_, playerEntity);
    }

    // Without this method, your inventory will NOT work!!!
    // если 0, то получаю краш в пакете расположения блока. Хз как это связано. TODO: Разобраться
    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1; //1; // return any value greater than zero
    }


}
