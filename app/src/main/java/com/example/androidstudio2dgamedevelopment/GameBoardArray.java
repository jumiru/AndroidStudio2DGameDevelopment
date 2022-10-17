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
    private int searchBoard[][];
    private Random rand;


    public GameBoardArray(int width, int height) {
        if (width<=0) {
            width = 1;
        }
        if (height<=0) {
            height = 1;
        }
        this.width = width;
        this.height = height;
        rand = new Random(0);

        gameBoardContent = new int[width][height];
        searchBoard = new int[width][height];
        for ( int y =0; y<height; y++ ) {
            for ( int x=0; x<width; x++) {
                gameBoardContent[x][y]=0; // 0 means empty
                searchBoard[x][y] = 0;
            }
        }
    }

    public void initCells(int numFields ) {
        int freeCells = getNumFreeCells();
        if (numFields > width * height) {
            numFields = width*height;
        }
        if (numFields > freeCells) {
            numFields = freeCells;
        }
        int index = 1;
        for (int field = 0; field < numFields; field++) {
            int x = 0;
            int y = 0;
            do {
                x = rand.nextInt(width);
                y = rand.nextInt(height);
            } while (gameBoardContent[x][y]!=0);
            gameBoardContent[x][y] = index;
            index++;
        }
    }

    public void randomlyAddCells(int numFields, int maxIndex ) {
        int index = 0;
        int freeCells = getNumFreeCells();
        if (numFields > width * height) {
            numFields = width*height;
        }
        if (numFields > freeCells) {
            numFields = freeCells;
        }

        for (int field = 0; field < numFields; field++) {
            int x = 0;
            int y = 0;
            do {
                x = rand.nextInt(width);
                y = rand.nextInt(height);
                index = rand.nextInt(maxIndex-1)+1;
            } while (gameBoardContent[x][y]!=0);
            gameBoardContent[x][y] = index;
        }
    }

    private int getNumFreeCells() {
        int freeCells = 0;
        for ( int y =0; y<height; y++ ) {
            for (int x = 0; x < width; x++) {
                if (gameBoardContent[x][y] == 0) {
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
        gameBoardContent[x][y] = val;
    }


    public String getText(int x, int y) {
        int val = get(x,y);
        if ( val == 0) {
            return "";
        } else if (val < 10) {
            return Integer.toString(1<<val);
        } else if (val < 20 ) {
            return Integer.toString(1<<(val-10)) + "k";
        } else if (val < 30 ) {
            return Integer.toString(1<<(val-20)) + "M";
        }
        return "";
    }

    public List<GameBoard.directionT> findPath(int startPositionX, int startPositionY, int targetPositionX, int targetPositionY) {
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

        boolean pathExists = buildSearchBoard(startList, target, 0);
        List<GameBoard.directionT> path;
        if (pathExists) {
            path = findShortestPath(target, start);
        } else {
            path = new ArrayList<>(); // empty path indicates that target is not reachable
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
        // since we are searching backwards, we have to flop the direction in search path!!!
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
                if (x >= 1 && (gameBoardContent[x - 1][y] == 0) && (searchBoard[x - 1][y] > depth)) {
                    Coord c = new Coord(x - 1, y);
                    newFront.add(c);
                    searchBoard[x - 1][y] = depth;
                }

                // try right
                if (x < (width - 1) && (gameBoardContent[x + 1][y] == 0) && (searchBoard[x + 1][y] > depth)) {
                    Coord c = new Coord(x + 1, y);
                    newFront.add(c);
                    searchBoard[x + 1][y] = depth;
                }

                //try up
                if (y > 1 && (gameBoardContent[x][y - 1] == 0) && (searchBoard[x][y - 1] > depth)) {
                    Coord c = new Coord(x, y - 1);
                    newFront.add(c);
                    searchBoard[x][y - 1] = depth;
                }

                //try down
                if (y < height - 1 && (gameBoardContent[x][y + 1] == 0) && (searchBoard[x][y + 1] > depth)) {
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


    public boolean merge() {
        return false;
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
}
