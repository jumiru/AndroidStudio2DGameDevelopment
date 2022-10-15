package com.example.androidstudio2dgamedevelopment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.core.content.ContextCompat;

public class GameBoard {

    private static final int RECT_BOARDER = 4;
    private static final float TEXT_HEIGHT = 100.0f;
    private Context context;
    private int height;
    private int width;
    private Paint paintArray[][];
    private GameBoardArray gameBoardArray;

    public GameBoard(Context context, int width, int height) {
        this.width = width;
        this.height = height;
        this.context = context;


        gameBoardArray = new GameBoardArray(width, height);
        gameBoardArray.randomInit(3 );

        paintArray = new Paint[width][height];
        for ( int y = 0; y < height; y++) {
            for ( int x =0; x<width; x++) {
                paintArray[x][y] = new Paint();
                paintArray[x][y].setColor(getColor(x,y));
            }
        }

    }

    private int getColor(int x, int y) {
        int val = gameBoardArray.get(x,y);
        if ( val == 0 ) {
            return  getColor(0xff, 0x90, 0x90, 0x90);
        } else {
            return getColor( 0xff, 0xff-33*val, 0x90 + 17*val, 0x00 + 55*val);
        }
    }

    private int getColor( int A, int R, int G, int B) {
        return (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
    }


    public void draw(Canvas canvas) {

        for ( int y =0; y<height; y++ ) {
            for (int x = 0; x < width; x++) {
                canvas.drawRect(getRect(canvas, x,y), paintArray[x][y]);
                Paint paint = new Paint();
                paint.setColor( 0xffffffff );
                paint.setTextSize(TEXT_HEIGHT);
                canvas.drawText(gameBoardArray.getText(x,y), getTextPosX(canvas, x), getTextPosY(canvas, y), paint);
            }
        }
    }

    private float getTextPosY(Canvas canvas, int y) {
        int canvasHeight = canvas.getHeight();
        float rectHeight = canvasHeight / height;
        float posY = rectHeight*y;
        return posY+(rectHeight-TEXT_HEIGHT)/2;
    }

    private float getTextPosX(Canvas canvas, int x) {
        int canvasWidth = canvas.getWidth();
        float posX = (canvasWidth / width)*x;
        return posX+2*RECT_BOARDER;
    }

    private Rect getRect(Canvas canvas, int x, int y) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        int rectWidth = canvasWidth / width;
        int rectHeight = canvasHeight / height;

        Rect rect = new Rect(
                x*rectWidth+RECT_BOARDER ,
                y*rectHeight+RECT_BOARDER,
                (x+1)*rectWidth-RECT_BOARDER,
                (y+1)*rectHeight-RECT_BOARDER
                );
        return rect;
    }

    public void update() {
    }
}
