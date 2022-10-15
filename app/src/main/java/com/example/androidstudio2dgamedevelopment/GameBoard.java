package com.example.androidstudio2dgamedevelopment;

import android.content.Context;
import android.graphics.Paint;

public class GameBoard {

    private int height;
    private int width;
    private Paint paintArray[][];

    public GameBoard(Context context, int width, int height) {
        this.width = width;
        this.height = height;

        paintArray = new Paint[width-1][height-1];
        for ( int y = 0; y < height; y++) {
            for ( int x =0; x<width; x++) {
                paintArray[x][y] = new Paint()
            }
        }
    }


    public void draw() {
    }

    public void update() {
    }
}
