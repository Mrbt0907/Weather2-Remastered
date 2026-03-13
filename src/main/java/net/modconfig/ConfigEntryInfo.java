package net.modconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

public class ConfigEntryInfo {
    public int index;
    public String name;
    public Object value;

    
    public String comment;

    public boolean markForUpdate = false;

    @OnlyIn(Dist.CLIENT)
    public TextFieldWidget editBox;

    public ConfigEntryInfo(int parIndex, String parName, Object parVal, String parComment) {
        index = parIndex;
        name = parName;
        value = parVal;
        comment = parComment;

        if (getEffectiveSide() == LogicalSide.CLIENT) initButton();
    }

    @OnlyIn(Dist.CLIENT)
    public void initButton() {
        int buttonWidth = 130;
        int buttonHeight = 16;
        editBox = new TextFieldWidget(Minecraft.getInstance().font, 0, 0, buttonWidth, buttonHeight, null);
        editBox.setValue(value.toString());
    }


    public static LogicalSide getEffectiveSide() {
        Thread thr = Thread.currentThread();
        String threadName = thr.getName();


        if (threadName.contains("Server thread") ||
                threadName.contains("Netty Epoll Server IO") ||
                threadName.contains("Netty Server IO")) {
            return LogicalSide.SERVER;
        }


        if (threadName.contains("Client thread") ||
                threadName.contains("Render thread")) {
            return LogicalSide.CLIENT;
        }


        try {
            ThreadTaskExecutor<?> clientExecutor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT);
            if (clientExecutor != null && clientExecutor.isSameThread()) {
                return LogicalSide.CLIENT;
            }
        } catch (Exception e) {

        }


        return LogicalSide.SERVER;
    }
}