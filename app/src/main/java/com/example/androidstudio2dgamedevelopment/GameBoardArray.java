package com.example.androidstudio2dgamedevelopment;

import java.util.Random;

public class GameBoardArray {

    private int width;
    private int height;

    private int gameBoardContent[][];
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
        rand = new Random();

        gameBoardContent = new int[width][height];
        for ( int y =0; y<height; y++ ) {
            for ( int x=0; x<width; x++) {
                gameBoardContent[x][y]=0;
            }
        }
    }

    public void randomInit( int numFields ) {
        if (numFields > width * height) {
            numFields = width*height;
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
}
