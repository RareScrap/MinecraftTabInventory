package ru.rarescrap.example;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ru.rarescrap.example.gui.BagContainer;
import ru.rarescrap.example.network.TabHandler;
import ru.rarescrap.tabinventory.TabHostInventory;

import static ru.rarescrap.example.TabInventoryExample.instance;
import static ru.rarescrap.example.TabInventoryExample.proxy;

public class CommonProxy implements IGuiHandler {
    public static SimpleNetworkWrapper INSTANCE =
            NetworkRegistry.INSTANCE.newSimpleChannel(TabInventoryExample.MODID.toLowerCase());

    public void preInit(FMLPreInitializationEvent event) {
        GameRegistry.registerItem(BagItem.ITEM, BagItem.ITEM.getUnlocalizedName());
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
        TabHostInventory.registerHandler(INSTANCE, TabHandler.class, 0);
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {}

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case TabInventoryExample.BAG_GUI_CODE:
                return new BagContainer(player, player.getHeldItem());
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null; // Переопределяется в ClientProxy
    }
}
