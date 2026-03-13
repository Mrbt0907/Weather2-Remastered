package net.mrbt0907.weather2.api.interfaces;

public interface IRadio<T> {
    void setRadioFrequency(T obj, String frequency);

    String getRadioFrequency(T obj);
}
