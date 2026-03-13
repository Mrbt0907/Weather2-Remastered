package net.modconfig.gui;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.modconfig.ConfigEntryInfo;
import net.modconfig.ConfigMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiConfigScrollPanel extends GuiBetterSlot
{
    private GuiConfigEditor config;
    private Minecraft mc;
    private int _mouseX;
    private int _mouseY;
    private int selected = -1;
    public ResourceLocation resGUI = new ResourceLocation("textures/gui/gui.png");

    public GuiConfigScrollPanel(GuiConfigEditor controls, Minecraft mc, int startX, int startY, int height, int slotSize)
    {
        super(mc, controls.width + 100, controls.height, startY + 8 + slotSize, height + slotSize - 2, slotSize);
        this.config = controls;
        this.mc = mc;
    }

    @Override
    protected int getSize()
    {
        net.modconfig.ModConfigData data = config.getData();
        return data.configData.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick)
    {
        if (!doubleClick)
        {
            selected = index;
            KeyBinding.resetMapping();
        }
    }


    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        boolean anyHasFocus = false;
        for (int i = 0; i < config.getData().configData.size(); i++)
        {
            try
            {
                config.getData().configData.get(i).editBox.mouseClicked(mouseX, mouseY, button);
                if (config.getData().configData.get(i).editBox.isFocused())
                {
                    anyHasFocus = true;
                }
            }
            catch (Exception ex)
            {

            }
        }
        return true;
    }

    @Override
    protected boolean isSelected(int index)
    {
        return false;
    }


    @Override
    protected void drawBackground(MatrixStack matrixStack) {}


    @Override
    protected void drawContainerBackground(MatrixStack matrixStack, Tessellator tessellator) {}

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        _mouseX = mouseX;
        _mouseY = mouseY;





        if (selected != -1 && GLFW.glfwGetMouseButton(
                this.mc.getWindow().getWindow(),
                GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_RELEASE)
        {
            selected = -1;
            KeyBinding.resetMapping();
        }

        try
        {
            super.render(matrixStack, mouseX, mouseY, partialTicks);
        }
        catch (Exception ex)
        {

        }
    }





    @Override
    protected void drawSlot(MatrixStack matrixStack, int index, int xPosition, int yPosition, int height, Tessellator tessellator)
    {
        int width = 70;
        xPosition -= 20;

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        List<ConfigEntryInfo> data = config.getData().configData;

        if (data.get(index) == null || data.get(index).editBox == null) return;

        int stringWidth = mc.font.width(data.get(index).name);
        mc.font.draw(matrixStack, data.get(index).name, xPosition - stringWidth + 15, yPosition + 3, 0xFFFFFFFF);

        boolean conflict = false;

        String value = data.get(index).value.toString();
        int maxWidth = (config.xSize / 2) - 45;
        value = mc.font.plainSubstrByWidth(value, maxWidth);



        String str = (conflict ? TextFormatting.RED : "") + value;
        str = (index == selected
                ? TextFormatting.WHITE + "> " + TextFormatting.YELLOW + "??? " + TextFormatting.WHITE + "<"
                : str);


        data.get(index).editBox.x = xPosition + 20;
        data.get(index).editBox.y = yPosition;

        data.get(index).editBox.render(matrixStack, _mouseX, _mouseY, 0);

        int hover_x_min = xPosition - stringWidth + 15;
        int hover_y_min = yPosition;
        int hover_x_max = xPosition - 15;
        int hover_y_max = yPosition + this.slotHeight;

        boolean hover_string = _mouseX >= hover_x_min && _mouseY >= hover_y_min
                && _mouseX <  hover_x_max && _mouseY <  hover_y_max;
        String comment = ConfigMod.getComment(config.getData().configID, data.get(index).name);

        if (hover_string && comment != null)
        {
            RenderSystem.disableLighting();
            RenderSystem.disableDepthTest();

            int textWidth = mc.font.width(comment);
            int tooltipX  = hover_x_min;
            int tooltipY  = hover_y_min - 10;


            drawGradientRect(matrixStack,
                    tooltipX - 3, tooltipY - 3,
                    tooltipX + textWidth + 3, tooltipY + 8 + 3,
                    0xc0000000, 0xc0000000);


            mc.font.drawShadow(matrixStack, comment, tooltipX, tooltipY, -1);

            RenderSystem.enableDepthTest();
        }
    }


    protected void drawGradientRect(MatrixStack matrixStack,
                                    int left, int top, int right, int bottom,
                                    int colorStart, int colorEnd)
    {
        float startAlpha = (float)(colorStart >> 24 & 255) / 255.0F;
        float startRed   = (float)(colorStart >> 16 & 255) / 255.0F;
        float startGreen = (float)(colorStart >>  8 & 255) / 255.0F;
        float startBlue  = (float)(colorStart       & 255) / 255.0F;
        float endAlpha   = (float)(colorEnd   >> 24 & 255) / 255.0F;
        float endRed     = (float)(colorEnd   >> 16 & 255) / 255.0F;
        float endGreen   = (float)(colorEnd   >>  8 & 255) / 255.0F;
        float endBlue    = (float)(colorEnd         & 255) / 255.0F;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);

        Tessellator    tessellator = Tessellator.getInstance();
        BufferBuilder  buffer      = tessellator.getBuilder();

        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.vertex((double)right, (double)top,    0.0D).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.vertex((double)left,  (double)top,    0.0D).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.vertex((double)left,  (double)bottom, 0.0D).color(endRed,   endGreen,   endBlue,   endAlpha  ).endVertex();
        buffer.vertex((double)right, (double)bottom, 0.0D).color(endRed,   endGreen,   endBlue,   endAlpha  ).endVertex();
        tessellator.end();

        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }


    public boolean charTyped(char codePoint, int modifiers)
    {
        if (selected != -1 && config.getData().configData.get(selected).editBox.isFocused())
        {
            config.getData().configData.get(selected).editBox.charTyped(codePoint, modifiers);
            return false;
        }
        return true;
    }



    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (selected != -1 && config.getData().configData.get(selected).editBox.isFocused())
        {
            config.getData().configData.get(selected).editBox.keyPressed(keyCode, scanCode, modifiers);
            if (keyCode == GLFW.GLFW_KEY_ENTER)
            {
                selected = -1;
                return true;
            }
            return false;
        }
        return true;
    }
}