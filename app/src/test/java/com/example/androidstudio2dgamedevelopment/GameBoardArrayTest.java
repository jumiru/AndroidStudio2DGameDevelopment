package com.example.androidstudio2dgamedevelopment;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameBoardArrayTest {

    @Test
    public void removeAllCellsBelowIndexRemovesOnlyValuesBelowNewMinimum() {
        GameBoardArray board = new GameBoardArray(4, 1);
        board.set(0, 0, 0);
        board.set(1, 0, 1);
        board.set(2, 0, 2);
        board.set(3, 0, 3);

        board.removeAllCellsBelowIndex(LevelProgression.getMinSpawnIndex(2));

        assertEquals(-1, board.get(0, 0));
        assertEquals(1, board.get(1, 0));
        assertEquals(2, board.get(2, 0));
        assertEquals(3, board.get(3, 0));
    }

    @Test
    public void removeAllCellsBelowIndexSupportsLaterLevels() {
        GameBoardArray board = new GameBoardArray(5, 1);
        board.set(0, 0, 1);
        board.set(1, 0, 2);
        board.set(2, 0, 3);
        board.set(3, 0, 4);
        board.set(4, 0, -1);

        board.removeAllCellsBelowIndex(LevelProgression.getMinSpawnIndex(4));

        assertEquals(-1, board.get(0, 0));
        assertEquals(-1, board.get(1, 0));
        assertEquals(3, board.get(2, 0));
        assertEquals(4, board.get(3, 0));
        assertEquals(-1, board.get(4, 0));
    }

    @Test
    public void randomlyAddCellRespectsInclusiveSpawnBounds() {
        GameBoardArray board = new GameBoardArray(2, 2);
        board.set(0, 0, 99);
        board.set(0, 1, 99);
        board.set(1, 0, 99);

        Coord added = board.randomlyAddCell(LevelProgression.getMinSpawnIndex(2), LevelProgression.getMaxSpawnIndex(2));

        assertEquals(1, added.x);
        assertEquals(1, added.y);
        int spawnedValue = board.get(1, 1);
        assertTrue(spawnedValue >= 1 && spawnedValue <= 4);
    }
}

