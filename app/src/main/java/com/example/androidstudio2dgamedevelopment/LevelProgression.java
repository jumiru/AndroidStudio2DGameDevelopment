package com.example.androidstudio2dgamedevelopment;

final class LevelProgression {

    private static final int LEVEL_ONE_MIN_SPAWN_INDEX = 0;
    private static final int ACTIVE_SPAWN_VALUE_COUNT = 4;

    private LevelProgression() {
    }

    static int getMinSpawnIndex(int level) {
        return Math.max(LEVEL_ONE_MIN_SPAWN_INDEX, level - 1);
    }

    static int getMaxSpawnIndex(int level) {
        return getMinSpawnIndex(level) + ACTIVE_SPAWN_VALUE_COUNT - 1;
    }
}

