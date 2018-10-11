package ru.rarescrap.example.gui;

import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.rarescrap.tabinventory.TabContainer;
import ru.rarescrap.tabinventory.TabGuiContainer;

import static ru.rarescrap.example.TabInventoryExample.MODID;

public class BagGui extends TabGuiContainer {
    private static final ResourceLocation background =
            new ResourceLocation(MODID,"textures/gui/bag_gui.png");

    public BagGui(TabContainer container) {
        super(container);

        // Высталяем размеры контейнера. Соответствует размерам GUI на текстуре.
        this.xSize = 176;
        this.ySize = 102;
        // Выставляем края контейнера (верхний и левый)
        this.guiLeft = this.width/2 - xSize/2;
        this.guiTop = this.height/2 - ySize/2;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);

        // Отрисовываем текстуру GUI
        drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize);
    }
}
