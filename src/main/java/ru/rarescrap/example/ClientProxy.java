package ru.rarescrap.example;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ru.rarescrap.example.gui.BagContainer;
import ru.rarescrap.example.gui.BagGui;

public class ClientProxy extends CommonProxy {
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case TabInventoryExample.BAG_GUI_CODE:
                return new BagGui(new BagContainer(player, player.getHeldItem()));
        }

        return null;
    }
}
