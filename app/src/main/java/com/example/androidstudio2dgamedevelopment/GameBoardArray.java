package com.example.androidstudio2dgamedevelopment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class GameBoardArray {

    private static final int MAX_PATH_LEN = 0xffffff;

    private final int width;
    private final int height;

    private final int[][] gameBoardContent;
    private final int[][] gameBoardBackup;
    private final int[][] searchBoard;
    private final Random rand;

    private boolean gameBoardModified;
    private boolean triggerBackupBeforeNextModification;


    public GameBoardArray(int width, int height) {
        if (width <= 0)  width  = 1;
        if (height <= 0) height = 1;
        this.width  = width;
        this.height = height;
        rand = new Random();

        gameBoardContent = new int[width][height];
        gameBoardBackup  = new int[width][height];
        searchBoard      = new int[width][height];
        clear();
    }

    public void clear() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                set(x, y, -1); // -1 means empty
                searchBoard[x][y] = 0;
            }
        }
    }

    public void initCells(int numFields) {
        if (numFields == -1) {
            initCellsForDebugging();
        } else {
            int freeCells = getNumFreeCells();
            if (numFields > freeCells) {
                numFields = freeCells;
            }
            for (int field = 0; field < numFields; field++) {
                int x, y;
                do {
                    x = rand.nextInt(width);
                    y = rand.nextInt(height);
                } while (!isFree(x, y));
                set(x, y, field);
            }
        }
    }

    private void initCellsForDebugging() {
        int[][] a = {
            { -1,  1,  2, -1, -1 },
            { -1,  1,  2, -1, -1 },
            { -1,  1,  2, -1, -1 },
            {  3,  2,  1,  4, -1 },
            {  3,  2, -1, -1, -1 },
            { -1,  1, -1,  4, -1 },
            { -1,  2, -1,  4, -1 }};

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                set(x, y, a[y][x]);
            }
        }
    }

    public Coord randomlyAddCell(int minIndex, int maxIndex) {
        int x, y, index;
        do {
            x     = rand.nextInt(width);
            y     = rand.nextInt(height);
            index = rand.nextInt(maxIndex - minIndex + 1) + minIndex;
        } while (!isFree(x, y));
        set(x, y, index);
        return new Coord(x, y);
    }

    public int getNumFreeCells() {
        int freeCells = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isFree(x, y)) freeCells++;
            }
        }
        return freeCells;
    }

    public int get(int x, int y) {
        x = clampX(x);
        y = clampY(y);
        return gameBoardContent[x][y];
    }

    public void set(int x, int y, int val) {
        x = clampX(x);
        y = clampY(y);

        if (triggerBackupBeforeNextModification) {
            backupCurrentGameBoard();
            triggerBackupBeforeNextModification = false;
        }

        gameBoardContent[x][y] = val;
        gameBoardModified = true;
    }

    public boolean isFree(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        return gameBoardContent[x][y] == -1;
    }

    /** Clamps x to valid column range. */
    private int clampX(int x) {
        return Math.max(0, Math.min(x, width - 1));
    }

    /** Clamps y to valid row range. */
    private int clampY(int y) {
        return Math.max(0, Math.min(y, height - 1));
    }

    public List<GameBoard.directionT> findPath(int startPositionX, int startPositionY,
                                               int targetPositionX, int targetPositionY,
                                               boolean allowTargetPosCollision) {
        // Initialize search board for BFS
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                searchBoard[x][y] = MAX_PATH_LEN;
            }
        }
        searchBoard[startPositionX][startPositionY] = 0;

        Coord start  = new Coord(startPositionX, startPositionY);
        Coord target = new Coord(targetPositionX, targetPositionY);

        List<Coord> startList = new ArrayList<>();
        startList.add(new Coord(startPositionX, startPositionY));

        int tempTargetValue = get(targetPositionX, targetPositionY);
        if (allowTargetPosCollision) {
            set(targetPositionX, targetPositionY, -1);
        }

        boolean pathExists = buildSearchBoard(startList, target, 0);

        List<GameBoard.directionT> path;
        if (pathExists) {
            path = findShortestPath(target, start);
        } else {
            path = new ArrayList<>(); // empty path means target is not reachable
        }

        if (allowTargetPosCollision) {
            set(targetPositionX, targetPositionY, tempTargetValue);
        }

        return path;
    }

    private List<GameBoard.directionT> findShortestPath(Coord pos, Coord start) {
        List<GameBoard.directionT> res = new ArrayList<>();

        if (pos.x == start.x && pos.y == start.y) {
            return res;
        }

        int x   = pos.x;
        int y   = pos.y;
        int min = MAX_PATH_LEN;
        GameBoard.directionT dir = GameBoard.directionT.LEFT;

        // Find min in all directions.
        // Since we search backwards from target to start, directions are flipped.
        if (x > 0 && searchBoard[x - 1][y] < min) {
            min   = searchBoard[x - 1][y];
            dir   = GameBoard.directionT.RIGHT;
            pos.x = x - 1;
            pos.y = y;
        }
        if (x < width - 1 && searchBoard[x + 1][y] < min) {
            min   = searchBoard[x + 1][y];
            dir   = GameBoard.directionT.LEFT;
            pos.x = x + 1;
            pos.y = y;
        }
        if (y > 0 && searchBoard[x][y - 1] < min) {
            min   = searchBoard[x][y - 1];
            dir   = GameBoard.directionT.DOWN;
            pos.x = x;
            pos.y = y - 1;
        }
        if (y < height - 1 && searchBoard[x][y + 1] < min) {
            dir   = GameBoard.directionT.UP;
            pos.x = x;
            pos.y = y + 1;
        }

        res.add(dir);
        res.addAll(findShortestPath(pos, start));
        return res;
    }

    private boolean buildSearchBoard(List<Coord> startList, Coord target, int depth) {
        boolean targetFound = false;
        depth++;
        List<Coord> newFront = new ArrayList<>();

        for (Coord curr : startList) {
            if (targetFound) break;
            int x = curr.x;
            int y = curr.y;

            if (target.x == x && target.y == y) {
                targetFound = true;
            } else {
                // try left
                if (x >= 1 && gameBoardContent[x - 1][y] == -1 && searchBoard[x - 1][y] > depth) {
                    newFront.add(new Coord(x - 1, y));
                    searchBoard[x - 1][y] = depth;
                }
                // try right
                if (x < width - 1 && gameBoardContent[x + 1][y] == -1 && searchBoard[x + 1][y] > depth) {
                    newFront.add(new Coord(x + 1, y));
                    searchBoard[x + 1][y] = depth;
                }
                // try up
                if (y >= 1 && gameBoardContent[x][y - 1] == -1 && searchBoard[x][y - 1] > depth) {
                    newFront.add(new Coord(x, y - 1));
                    searchBoard[x][y - 1] = depth;
                }
                // try down
                if (y < height - 1 && gameBoardContent[x][y + 1] == -1 && searchBoard[x][y + 1] > depth) {
                    newFront.add(new Coord(x, y + 1));
                    searchBoard[x][y + 1] = depth;
                }
            }
        }

        if (!targetFound) {
            if (!newFront.isEmpty()) {
                return buildSearchBoard(newFront, target, depth);
            }
            return false;
        }
        return true;
    }


    public Set<Coord> findMergeGroup(int x, int y) {
        int mergeValue = gameBoardContent[x][y];
        Set<Coord> currMergeSet = new TreeSet<>();
        currMergeSet.add(new Coord(x, y));
        findMergeGroupDFS(x, y, mergeValue, currMergeSet);
        return currMergeSet;
    }

    private void findMergeGroupDFS(int x, int y, int mergeValue, Set<Coord> currMergeSet) {
        // Try all 4 directions
        tryMergeDirection(x - 1, y, mergeValue, currMergeSet);
        tryMergeDirection(x + 1, y, mergeValue, currMergeSet);
        tryMergeDirection(x, y - 1, mergeValue, currMergeSet);
        tryMergeDirection(x, y + 1, mergeValue, currMergeSet);
    }

    private void tryMergeDirection(int nx, int ny, int mergeValue, Set<Coord> currMergeSet) {
        if (nx < 0 || nx >= width || ny < 0 || ny >= height) return;
        if (gameBoardContent[nx][ny] != mergeValue) return;
        Coord c = new Coord(nx, ny);
        if (currMergeSet.add(c)) { // add returns false if already present
            findMergeGroupDFS(nx, ny, mergeValue, currMergeSet);
        }
    }


    private void backupCurrentGameBoard() {
        if (gameBoardModified) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    gameBoardBackup[x][y] = gameBoardContent[x][y];
                }
            }
        }
    }

    public void unrollBackup() {
        if (gameBoardModified) {
            gameBoardModified = false;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    gameBoardContent[x][y] = gameBoardBackup[x][y];
                }
            }
        }
    }

    public void backupGameBoardWithNextModification() {
        triggerBackupBeforeNextModification = true;
    }

    public void removeAllCellsSmallerThan(int level) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (gameBoardContent[x][y] != -1 && gameBoardContent[x][y] < level - 1) {
                    set(x, y, -1);
                }
            }
        }
    }

    public boolean dissolveAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        if (gameBoardContent[x][y] == -1) {
            return false;
        }
        set(x, y, -1);
        return true;
    }

    public void shiftRowRight(int y) {
        if (y < 0 || y >= height || width <= 1) {
            return;
        }
        for (int x = width - 1; x >= 1; x--) {
            set(x, y, gameBoardContent[x - 1][y]);
        }
        set(0, y, -1);
    }

    public void shiftColDown(int x) {
        if (x < 0 || x >= width || height <= 1) {
            return;
        }
        for (int y = height - 1; y >= 1; y--) {
            set(x, y, gameBoardContent[x][y - 1]);
        }
        set(x, 0, -1);
    }
}
