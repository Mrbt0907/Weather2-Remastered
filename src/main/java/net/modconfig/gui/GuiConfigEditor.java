package net.modconfig.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.CoroUtil.forge.CULog;
import net.CoroUtil.packet.PacketHelper;
import net.modconfig.ConfigEntryInfo;
import net.modconfig.ConfigMod;
import net.modconfig.ModConfigData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiConfigEditor extends Screen
{
    public int ySize;
    public int xSize;
    public int xOffset;

    public static int G_RESET      = 0;
    public static int G_SAVE       = 1;
    public static int G_MODNEXT    = 2;
    public static int G_MODPREV    = 3;
    public static int G_CONFIGMODE = 4;
    public static int G_CLOSE      = 9;

    public static int curIndex = 0;

    public GuiConfigScrollPanel scrollPane;

    public static boolean clientMode = false;

    public ResourceLocation resGUI = new ResourceLocation("modconfig:textures/gui/gui512.png");

    public GuiConfigEditor()
    {
        super(new StringTextComponent("Config Editor"));
    }

    public ModConfigData getConfigData(String modID)
    {
        return ConfigMod.configLookup.get(modID);
    }

    public String getCategory()
    {
        if (ConfigMod.liveEditConfigs.size() <= 0) return "<NULL>";
        return ConfigMod.liveEditConfigs.get(curIndex).configID;
    }

    public ModConfigData getData()
    {
        return getConfigData(getCategory());
    }



    @Override
    public void tick()
    {
        try {
            updateStates();

            if (ConfigMod.liveEditConfigs.size() > 0) {
                for (int i = 0; i < getData().configData.size(); i++) {
                    if (getData().configData.get(i).editBox != null
                            && getData().configData.get(i).editBox.isFocused()) {
                        getData().configData.get(i).editBox.tick();
                    }
                }
            }
        } catch (Exception ex) {

        }
        super.tick();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        try {
            int startX = (this.width  - this.xSize) / 2;
            int startY = (this.height - this.ySize) / 2;

            drawGuiContainerBackgroundLayer(matrixStack, 0, 0, 0);

            scrollPane.render(matrixStack, mouseX, mouseY, partialTicks);

            drawGuiContainerClippingScrollLayer(matrixStack, 0, 0, 0);


            drawString(matrixStack, this.font, "Config for: " + getCategory(),
                    startX + 10, startY + 10, 16777215);
            drawString(matrixStack, this.font,
                    (curIndex + 1) + "/" + ConfigMod.liveEditConfigs.size(),
                    startX + xSize - 60, startY + 10, 16777215);
        } catch (Exception ex) {

        }


        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }







    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        return scrollPane.mouseScrolled(mouseX, mouseY, delta);
    }

    protected void drawGuiContainerClippingScrollLayer(MatrixStack matrixStack,
                                                       float var1, int var2, int var3)
    {
        int startX = 4 + (this.width  - this.xSize) / 2;
        int startY = 4 + (this.height - this.ySize) / 2;
        int x1     = xSize - 8;





        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();

        Tessellator   tessellator = Tessellator.getInstance();
        BufferBuilder buffer      = tessellator.getBuilder();



        int y1 = startY;
        int y2 = startY + 23;
        drawFadeQuad(matrixStack, buffer, tessellator, startX, x1, y1, y2);


        y1 = startY + ySize - 14 - 23;
        y2 = startY + ySize - 14;
        drawFadeQuad(matrixStack, buffer, tessellator, startX, x1, y1, y2);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }



    private void drawFadeQuad(MatrixStack matrixStack, BufferBuilder buffer,
                              Tessellator tessellator, int startX, int x1, int y1, int y2)
    {
        Matrix4f matrix = matrixStack.last().pose();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.vertex(matrix, startX + x1, y1, 0.0F).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
        buffer.vertex(matrix, startX,      y1, 0.0F).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
        buffer.vertex(matrix, startX,      y2, 0.0F).uv(1.0F, 0.0F).color(0, 0, 0,   0).endVertex();
        buffer.vertex(matrix, startX + x1, y2, 0.0F).uv(0.0F, 0.0F).color(0, 0, 0,   0).endVertex();
        tessellator.end();
    }

    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack,
                                                   float var1, int var2, int var3)
    {
        this.xSize = 372;
        this.ySize = 250;

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(resGUI);

        int startX = (this.width  - this.xSize) / 2;
        int startY = (this.height - this.ySize) / 2;



        drawBackgroundTexture(matrixStack, startX, startY, 0, 0, 512, 512);
    }









    private void drawBackgroundTexture(MatrixStack matrixStack,
                                       int x, int y, int texX, int texY, int width, int height)
    {
        float f = 0.00390625F / 2F;
        Matrix4f matrix = matrixStack.last().pose();
        Tessellator   tessellator = Tessellator.getInstance();
        BufferBuilder buffer      = tessellator.getBuilder();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.vertex(matrix, x,         y + height, getBlitOffset()).uv((texX)          * f, (texY + height) * f).endVertex();
        buffer.vertex(matrix, x + width, y + height, getBlitOffset()).uv((texX + width)  * f, (texY + height) * f).endVertex();
        buffer.vertex(matrix, x + width, y,          getBlitOffset()).uv((texX + width)  * f,  texY            * f).endVertex();
        buffer.vertex(matrix, x,         y,          getBlitOffset()).uv( texX            * f,  texY            * f).endVertex();
        tessellator.end();
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }


    public void updateChangedValues()
    {
        for (int i = 0; i < getData().configData.size(); i++) {
            ConfigEntryInfo info = getData().configData.get(i);

            if (!getData().configData.get(i).editBox.getValue()
                    .equals(getData().configData.get(i).value.toString())) {

                if (!clientMode) {
                    ConfigMod.eventChannel.sendToServer(
                            PacketHelper.getModConfigPacketForClientToServer(
                                    "set " + getCategory() + " "
                                            + getData().configData.get(i).name + " "
                                            + getData().configData.get(i).editBox.getValue()));
                } else {
                    if (ConfigMod.updateField(getCategory(),
                            getData().configData.get(i).name,
                            getData().configData.get(i).editBox.getValue())) {
                        CULog.dbg("Updated config settings in client mode");
                    }
                }
            }
        }
    }







    @Override
    protected void init()
    {
        xSize = 372;
        ySize = 250;
        int startX = (this.width  - this.xSize) / 2;
        int startY = (this.height - this.ySize) / 2;

        scrollPane = new GuiConfigScrollPanel(this, this.minecraft, startX, startY,
                startY + ySize - 50, 20);
        scrollPane.registerScrollButtons(null, 7, 8);

        if (!clientMode) {
            if (this.minecraft.player != null) {
                this.minecraft.player.chat("/config update " + getCategory());
            }
        } else {
            ConfigMod.populateData(getCategory());
        }

        int buttonWidth  = 90;
        int buttonHeight = 20;
        int paddingSize  = 8;
        int navWidth     = 20;
        int navHeight    = 20;


        this.buttons.clear();
        this.children.clear();

        this.addButton(new Button(
                startX + xSize - (navWidth + 22) * 2, startY + paddingSize - 3,
                navWidth, navHeight,
                new StringTextComponent("<"),
                button -> actionPerformed(G_MODPREV)));

        this.addButton(new Button(
                startX + xSize - (navWidth + paddingSize), startY + paddingSize - 3,
                navWidth, navHeight,
                new StringTextComponent(">"),
                button -> actionPerformed(G_MODNEXT)));

        this.addButton(new Button(
                startX + xSize - (buttonWidth + paddingSize) * 2,
                startY + ySize - buttonHeight - paddingSize,
                buttonWidth, buttonHeight,
                new StringTextComponent("Save"),
                button -> actionPerformed(G_SAVE)));

        this.addButton(new Button(
                startX + xSize - (buttonWidth + paddingSize),
                startY + ySize - buttonHeight - paddingSize,
                buttonWidth, buttonHeight,
                new StringTextComponent("Close"),
                button -> actionPerformed(G_CLOSE)));


        if (this.minecraft.hasSingleplayerServer()) {
            clientMode = false;
        } else {
            this.addButton(new Button(
                    startX + xSize - (buttonWidth + paddingSize) * 3,
                    startY + ySize - buttonHeight - paddingSize,
                    buttonWidth, buttonHeight,
                    new StringTextComponent("Mode: " + (clientMode ? "Local" : "Remote")),
                    button -> actionPerformed(G_CONFIGMODE)));
        }
    }


    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        try {
            if (scrollPane.charTyped(codePoint, modifiers)) {
                return super.charTyped(codePoint, modifiers);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        try {
            if (scrollPane.keyPressed(keyCode, scanCode, modifiers)) {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        try {
            super.mouseClicked(mouseX, mouseY, button);
            scrollPane.mouseClicked(mouseX, mouseY, button);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }




    protected void actionPerformed(int buttonId)
    {
        if (buttonId == G_MODPREV) {
            curIndex--;
            if (curIndex < 0) curIndex = ConfigMod.liveEditConfigs.size() - 1;
        } else if (buttonId == G_MODNEXT) {
            curIndex++;
            if (curIndex >= ConfigMod.liveEditConfigs.size()) curIndex = 0;
        } else if (buttonId == G_SAVE) {
            updateChangedValues();
        } else if (buttonId == G_CONFIGMODE) {
            clientMode = !clientMode;
        } else if (buttonId == G_CLOSE) {
            this.minecraft.setScreen(null);
        }
        init();
    }

    public void updateStates()
    {

    }
}