package com.example.androidstudio2dgamedevelopment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class GameBoardArray {


    private static final int MAX_PATH_LEN = 0xffffff;

    private int width;
    private int height;

    private int gameBoardContent[][];
    private int gameBoardBackup[][];
    private int searchBoard[][];
    private Random rand;

    private boolean gameBoardModified;
    private boolean triggerBackupBeforeNextModification;


    public GameBoardArray(int width, int height) {
        if (width<=0) {
            width = 1;
        }
        if (height<=0) {
            height = 1;
        }
        this.width = width;
        this.height = height;
        rand = new Random();

        gameBoardContent = new int[width][height];
        gameBoardBackup = new int[width][height];
        searchBoard = new int[width][height];
        clear();
    }

    public void clear() {
        for ( int y =0; y<height; y++ ) {
            for ( int x=0; x<width; x++) {
                set(x,y,-1); // 0 means empty
                searchBoard[x][y] = 0;
            }
        }
    }

    public void initCells(int numFields ) {


        if (numFields==-1) {
            initCellsForDebugging();
        } else {
            int freeCells = getNumFreeCells();
            if (numFields > freeCells) {
                numFields = freeCells;
            }
            int index = 0;
            for (int field = 0; field < numFields; field++) {
                int x = 0;
                int y = 0;
                do {
                    x = rand.nextInt(width);
                    y = rand.nextInt(height);
                } while (!isFree(x,y));
                set(x,y,index);
                index++;
            }
        }
    }

    public void initCellsForDebugging() {
        int[][] a = {
         {  -1,  1,  2,  3,  4 },
         {  5,  6,  7,  8,  9 },
         { -1, -1, -1, -1, -1 },
         { -1, -1, -1,  0, -1 },
         { -1, -1, -1, -1, -1 },
         { -1, -1, -1, -1, -1 },
         { -1, -1, -1, -1, -1 }};


        for ( int y =0; y<height; y++ ) {
            for ( int x=0; x<width; x++) {
                set(x,y,a[y][x]);
            }
        }
    }

    public Coord randomlyAddCell(int minIndex, int maxIndex) {
        int index = 0;

        int x = 0;
        int y = 0;
        do {
            x = rand.nextInt(width);
            y = rand.nextInt(height);
            index = rand.nextInt(maxIndex-minIndex+1) + minIndex;
        } while (!isFree(x,y));
        set(x,y, index);
        return new Coord(x,y);
    }

    public int getNumFreeCells() {
        int freeCells = 0;
        for ( int y =0; y<height; y++ ) {
            for (int x = 0; x < width; x++) {
                if (isFree(x,y)) {
                    freeCells++;
                }
            }
        }
        return freeCells;
    }

    private int valueFromIndex(int i) {
        return (1 << i);
    }

    public int get(int x, int y) {
        if (x<0) {
            x = 0;
        }
        else if (x>=width) {
            x = width-1;
        }
        if (y<0) {
            y = 0;
        }
        else if ( y>= height) {
            y = height-1;
        }
        return gameBoardContent[x][y];
    }

    public void set(int x, int y, int val) {
        if (x<0) {
            x = 0;
        }
        else if (x>=width) {
            x = width-1;
        }
        if (y<0) {
            y = 0;
        }
        else if ( y>= height) {
            y = height-1;
        }

        if (triggerBackupBeforeNextModification) {
            backupCurrentGameBoard();
            triggerBackupBeforeNextModification = false;
        }

        gameBoardContent[x][y] = val;
        gameBoardModified = true;
    }

    public boolean isFree(int x, int y) {

        if (x<0) {
            return false;
        }
        else if (x>=width) {
            return false;
        }
        if (y<0) {
            return false;
        }
        else if ( y>= height) {
            return false;
        }
        return (gameBoardContent[x][y] == -1);
    }

    public String getText(int x, int y) {
        int val = get(x,y);
        if ( val == -1) {
            return "";
        } else if (val < 10) {
            return Integer.toString(1<<val);
        } else if (val < 20 ) {
            return Integer.toString(1<<(val-10)) + "k";
        } else if (val < 30 ) {
            return Integer.toString(1<<(val-20)) + "M";
        } else if (val < 40 ) {
            return Integer.toString(1<<(val-30)) + "G";
        }
        return "";
    }

    public List<GameBoard.directionT> findPath(int startPositionX, int startPositionY, int targetPositionX, int targetPositionY, boolean allowTargetPosCollision ) {
        //clone field for BFS
        for ( int y =0; y<height; y++ ) {
            for (int x = 0; x < width; x++) {
                searchBoard[x][y] = MAX_PATH_LEN;
            }
        }
        searchBoard[startPositionX][startPositionY] = 0;

        Coord start  = new Coord(startPositionX, startPositionY);
        Coord target = new Coord(targetPositionX, targetPositionY);
        List<Coord> startList = new ArrayList<Coord>();
        startList.add(new Coord(startPositionX,startPositionY));

        int tempTargetValue = get(targetPositionX, targetPositionY);
        if (allowTargetPosCollision) {
            set(targetPositionX, targetPositionY, -1);
        }

        boolean pathExists = buildSearchBoard(startList, target, 0);


        List<GameBoard.directionT> path;
        if (pathExists) {
            path = findShortestPath(target, start);
        } else {
            path = new ArrayList<>(); // empty path indicates that target is not reachable
        }

        if (allowTargetPosCollision) {
            set(targetPositionX, targetPositionY, tempTargetValue);
        }

        return path;
    }

    private List<GameBoard.directionT> findShortestPath(Coord pos, Coord start) {

        List<GameBoard.directionT> res = new ArrayList<>();

        if (pos.x==start.x && pos.y== start.y) {
            return res;
        }

        int x = pos.x;
        int y = pos.y;
        int min = MAX_PATH_LEN;
        GameBoard.directionT dir = GameBoard.directionT.LEFT;

        //find min in all directions
        // since we are searching backwards, we have to flip the direction in search path!!!
        if ( x>0 ) {
            if (searchBoard[x-1][y] < min ) {
                min = searchBoard[x-1][y];
                dir = GameBoard.directionT.RIGHT; // we are later traversing backwards, therefor we have to flip directions
                pos.x = x-1;
                pos.y = y;
            }
        }
        if ( x<width-1 ) {
            if (searchBoard[x+1][y] < min ) {
                min = searchBoard[x+1][y];
                dir = GameBoard.directionT.LEFT;
                pos.x = x+1;
                pos.y = y;
            }
        }
        if ( y>0 ) {
            if (searchBoard[x][y-1] < min ) {
                min = searchBoard[x][y-1];
                dir = GameBoard.directionT.DOWN;
                pos.x = x;
                pos.y = y-1;
            }
        }
        if ( y<height-1 ) {
            if (searchBoard[x][y+1] < min ) {
                min = searchBoard[x][y+1];
                dir = GameBoard.directionT.UP;
                pos.x = x;
                pos.y = y+1;
            }
        }

        res.add(dir);
        res.addAll(findShortestPath(pos, start));

        return res;
    }

    private boolean buildSearchBoard(List<Coord> startList, Coord target, int depth) {

        boolean targetFound = false;

        depth++;
        List<Coord> newFront = new ArrayList<>();
        Iterator<Coord> iterator = startList.iterator();
        while ( iterator.hasNext() && !targetFound) {
            Coord curr = iterator.next();
            int x = curr.x;
            int y = curr.y;

            if ( target.x == x && target.y==y) {
                targetFound = true;
            } else {

                //try left
                if (x >= 1 && (gameBoardContent[x - 1][y] == -1) && (searchBoard[x - 1][y] > depth)) {
                    Coord c = new Coord(x - 1, y);
                    newFront.add(c);
                    searchBoard[x - 1][y] = depth;
                }

                // try right
                if (x < (width - 1) && (gameBoardContent[x + 1][y] == -1) && (searchBoard[x + 1][y] > depth)) {
                    Coord c = new Coord(x + 1, y);
                    newFront.add(c);
                    searchBoard[x + 1][y] = depth;
                }

                //try up
                if (y >= 1 && (gameBoardContent[x][y - 1] == -1) && (searchBoard[x][y - 1] > depth)) {
                    Coord c = new Coord(x, y - 1);
                    newFront.add(c);
                    searchBoard[x][y - 1] = depth;
                }

                //try down
                if (y < (height - 1) && (gameBoardContent[x][y + 1] == -1) && (searchBoard[x][y + 1] > depth)) {
                    Coord c = new Coord(x, y + 1);
                    newFront.add(c);
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
        Set<Coord> currMergeSet = new TreeSet<Coord>();
        currMergeSet.add(new Coord(x,y));
        findMergeGroupDFS(x,y,mergeValue, currMergeSet);
        return currMergeSet;
    }

    private void findMergeGroupDFS(int x, int y, int mergeValue, Set<Coord> currMergeSet) {

        Coord c = new Coord();
        // try all dirs
        if (x>0) {
            c.x = x - 1;
            c.y = y;
            if (!currMergeSet.contains(c) && gameBoardContent[x - 1][y] == mergeValue) {
                currMergeSet.add(new Coord(c));
                findMergeGroupDFS(c.x,c.y, mergeValue,currMergeSet);
            }
        }
        if (x<width-1) {
            c.x = x +1;
            c.y = y;
            if (!currMergeSet.contains(c) && gameBoardContent[x+1][y]==mergeValue) {
                currMergeSet.add(new Coord(c));
                findMergeGroupDFS(c.x,c.y, mergeValue,currMergeSet);
            }
        }
        if (y>0) {
            c.x = x;
            c.y = y-1;
            if (!currMergeSet.contains(c) && gameBoardContent[x][y-1]==mergeValue) {
                currMergeSet.add(new Coord(c));
                findMergeGroupDFS(c.x,c.y, mergeValue,currMergeSet);
            }
        }
        if (y<height-1) {
            c.x = x;
            c.y = y+1;
            if (!currMergeSet.contains(c)) {
                if (gameBoardContent[x][y+1]==mergeValue) {
                    currMergeSet.add(new Coord(c));
                    findMergeGroupDFS(c.x, c.y, mergeValue, currMergeSet);
                }
            }
        }
    }


    public void backupCurrentGameBoard() {
        if (gameBoardModified) {
            for ( int y =0; y<height; y++ ) {
                for (int x = 0; x < width; x++) {
                    gameBoardBackup[x][y] = gameBoardContent[x][y];
                }
            }
        }
    }

    public void unrollBackup() {
        if (gameBoardModified) {
            gameBoardModified = false;
            for ( int y =0; y<height; y++ ) {
                for (int x = 0; x < width; x++) {
                    gameBoardContent[x][y] = gameBoardBackup[x][y];
                }
            }
        }
    }

    public void backupGameBoardWithNextModification() {
        triggerBackupBeforeNextModification = true;
    }
}
