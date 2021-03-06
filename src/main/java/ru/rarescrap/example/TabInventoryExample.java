package ru.rarescrap.example;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = TabInventoryExample.MODID, version = TabInventoryExample.VERSION)
public class TabInventoryExample
{
    public static final String MODID = "tabinventoryexample";
    public static final String VERSION = "1.0";
    public static final int BAG_GUI_CODE = 0;

    @Mod.Instance
    public static TabInventoryExample instance;

    public static ru.rarescrap.example.EventHandler eventHandler = new ru.rarescrap.example.EventHandler();

    @SidedProxy(clientSide = "ru.rarescrap.example.ClientProxy", serverSide = "ru.rarescrap.example.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        MinecraftForge.EVENT_BUS.register(eventHandler);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
