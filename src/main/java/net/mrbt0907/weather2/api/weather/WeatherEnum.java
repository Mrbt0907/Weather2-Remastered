package net.mrbt0907.weather2.api.weather;

import net.mrbt0907.weather2.util.StringUtils;

public class WeatherEnum {
    public enum Type {
        CLOUD(false), RAIN(false), THUNDER(false), SUPERCELL(false), TORNADO(true),
        TROPICAL_DISTURBANCE(false), TROPICAL_DEPRESSION(false), TROPICAL_STORM(true), HURRICANE(true),
        SANDSTORM(true),
        BLIZZARD(true);

        private final boolean isDangerous;

        Type(boolean isDangerous) {
            this.isDangerous = isDangerous;
        }

        public static Type get(int id) {
            return values()[id];
        }

        public static int size() {
            return values().length;
        }

        public boolean isDangerous() {
            return isDangerous;
        }

        @Override
        public String toString() {
            return StringUtils.toUpperCaseAlt(super.toString().replaceAll("\\_", " ").toLowerCase());
        }
    }

    public enum Stage {
        NORMAL(0), RAIN(1), THUNDER(2), SEVERE(3), TROPICAL_DISTURBANCE(2), TROPICAL_DEPRESSION(3), TROPICAL_STORM(4), TORNADO(4), HURRICANE(5);

        private final int stage;

        Stage(int stage) {
            this.stage = stage;
        }

        public static Stage get(int id) {
            return values()[id];
        }

        public static int size() {
            return values().length;
        }

        public int getStage() {
            return stage;
        }

        @Override
        public String toString() {
            return StringUtils.toUpperCaseAlt(super.toString().replaceAll("\\_", " ").toLowerCase());
        }
    }
}