package net.mrbt0907.weather2.api.interfaces;

import net.minecraft.util.ResourceLocation;

public interface IRadioTransmitter<T> extends IRadio<T> {
    void setRadioMessage(T obj, String message);

    String getRadioMessage(T obj);

    void setRadioSound(T obj, ResourceLocation sound);

    ResourceLocation getRadioSound(T obj);
}