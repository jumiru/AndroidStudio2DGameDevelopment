package com.example.androidstudio2dgamedevelopment;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LevelProgressionTest {

    @Test
    public void levelOneSpawnsOneToEight() {
        assertEquals(0, LevelProgression.getMinSpawnIndex(1));
        assertEquals(3, LevelProgression.getMaxSpawnIndex(1));
    }

    @Test
    public void levelTwoRemovesOnesAndAddsSixteen() {
        assertEquals(1, LevelProgression.getMinSpawnIndex(2));
        assertEquals(4, LevelProgression.getMaxSpawnIndex(2));
    }

    @Test
    public void spawnWindowKeepsFourConsecutivePowersOfTwo() {
        assertEquals(4, LevelProgression.getMinSpawnIndex(5));
        assertEquals(7, LevelProgression.getMaxSpawnIndex(5));
    }
}

