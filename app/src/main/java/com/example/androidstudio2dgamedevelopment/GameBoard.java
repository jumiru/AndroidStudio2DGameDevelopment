package com.example.androidstudio2dgamedevelopment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.core.content.ContextCompat;

import java.util.Random;

public class GameBoard {

    private static final int RECT_BOARDER = 8;
    private static final float TEXT_HEIGHT = 100.0f;
    private static final int STATUS_FIELD_HEIGHT = 200 ;
    private static final int MOTION_STEPS = 15;


    public enum directionT {
        UP, DOWN, LEFT, RIGHT
    }

    private Context context;
    private int height;
    private int width;
    private Paint paintArray[][];
    private Rect  rectArray[][];
    private Paint textPaint;
    private GameBoardArray gameBoardArray;

    // general dimension information
    private int canvasWidth;
    private int canvasHeight;
    private float cellWidth;
    private float cellHeight;

    // store information while one cell is in motion
    private boolean inMotion;
    private directionT motionDir;
    private Rect motionRect;
    private Paint motionPaint;
    private String motionText;
    private Rect finalPosition;
    private int xAfterMotion;
    private int yAfterMotion;
    private int gameBoardArrayValueAfterMotion;
    private int motionIncrementX;
    private int motionIncrementY;

    private Random rand;

    public GameBoard(Context context, int width, int height) {
        this.width = width;
        this.height = height;
        this.context = context;

        textPaint = new Paint();
        textPaint.setColor( 0xffffffff );
        textPaint.setTextSize(TEXT_HEIGHT);

        gameBoardArray = new GameBoardArray(width, height);
        gameBoardArray.randomInit(10 );

        rectArray = new Rect[width][height ];
        paintArray = new Paint[width][height];
        for ( int y = 0; y < height; y++) {
            for ( int x =0; x<width; x++) {
                paintArray[x][y] = new Paint();
                paintArray[x][y].setColor(getColor(x,y));
            }
        }
        motionPaint = new Paint();

        rand = new Random();
    }

    private int getColor(int x, int y) {
        int val = gameBoardArray.get(x,y);
        if ( val == 0 ) {
            return getColor(0xff, 0x90, 0x90, 0x90);
        } else {
            return getColor( 0xff, 0xff-99*val, 0x90 + 77*val, 0x00 + 99*val);
        }
    }

    private int getColor( int A, int R, int G, int B) {
        return (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
    }


    public void draw(Canvas canvas) {

        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        cellWidth = canvasWidth / width;
        cellHeight = (canvasHeight - STATUS_FIELD_HEIGHT ) / height;
        for ( int y =0; y<height; y++ ) {
            for (int x = 0; x < width; x++) {
                if (rectArray[x][y] == null) {
                    rectArray[x][y] = getRect(x,y);
                }
                drawCell(canvas, rectArray[x][y], paintArray[x][y], gameBoardArray.getText(x,y));
            }
        }

        if (inMotion) {
            drawCell(canvas, motionRect, motionPaint, motionText);
        }
    }

    private void drawCell(Canvas canvas, Rect rect, Paint paint, String text) {
        canvas.drawRect(rect, paint);
        canvas.drawText(text, getTextPosX(rect, text, textPaint), getTextPosY(rect), textPaint);
    }

    private float getTextPosY(Rect rect) {
        float posY = rect.bottom;
        return posY-(cellHeight-TEXT_HEIGHT)/2;
    }

    private float getTextPosX(Rect rect, String text, Paint paint) {
        float posX = rect.left;
        int offset = getApproxXToCenterText(text, paint, (int) cellWidth);
        return posX+offset-RECT_BOARDER;
    }

    public static int getApproxXToCenterText(String text, Paint p, int widthToFitStringInto) {
        float textWidth = p.measureText(text);
        int xOffset = (int)((widthToFitStringInto-textWidth)/2f);
        return xOffset;
    }

    private Rect getRect(int x, int y) {
        Rect rect = new Rect(
                x*(int)cellWidth+RECT_BOARDER ,
                y*(int)cellHeight+RECT_BOARDER,
                (x+1)*(int)cellWidth-RECT_BOARDER,
                (y+1)*(int)cellHeight-RECT_BOARDER
                );
        return rect;
    }

    public void update() {
        if (inMotion) {
            if ( reachedFinalPosition()) {
                gameBoardArray.set(xAfterMotion,yAfterMotion,gameBoardArrayValueAfterMotion);
                paintArray[xAfterMotion][yAfterMotion].setColor(motionPaint.getColor());
                inMotion = false;
            } else {
                motionRect.left = motionRect.left+motionIncrementX;
                motionRect.bottom = motionRect.bottom+motionIncrementY;
            }
        } else {
            randomMove();
        }
    }

    private boolean reachedFinalPosition() {

        boolean res = false;
        if ( inMotion && (
                (motionDir == directionT.LEFT && motionRect.left <= finalPosition.left) ||
                        (motionDir == directionT.RIGHT && motionRect.left >= finalPosition.left) ||
                        (motionDir == directionT.UP && motionRect.bottom <= finalPosition.bottom) ||
                        (motionDir == directionT.DOWN && motionRect.bottom >= finalPosition.bottom)
                )
            ) {
            res = true;
        }
        return res;
    }

    private void randomMove() {
        int x = rand.nextInt(width);
        int y = rand.nextInt(height);
        int dir = rand.nextInt(4);
        if ( dir == 0) {
            moveLeft(x,y);
        } else if (dir == 1) {
            moveRight(x,y);
        } else if (dir == 2) {
            moveUp(x,y);
        } else {
            moveDown(x,y);
        }
    }

    private void moveDown(int x, int y) {
        if (!inMotion) {
            if (x > 0 && gameBoardArray.get(x,y+1) == 0 && gameBoardArray.get(x,y) != 0) {
                applyCommonMotionSettings(x,y, x, y+1);
                motionDir = directionT.DOWN;
            }
        }
    }

    private void moveUp(int x, int y) {
        if (!inMotion) {
            if (x > 0 && gameBoardArray.get(x,y-1) == 0 && gameBoardArray.get(x,y) != 0) {
                applyCommonMotionSettings(x,y, x, y-1);
                motionDir = directionT.UP;
            }
        }
    }

    private void moveRight(int x, int y) {
        if (!inMotion) {
            if (x > 0 && gameBoardArray.get(x+1,y) == 0 && gameBoardArray.get(x,y) != 0) {
                applyCommonMotionSettings(x,y, x+1, y);
                motionDir = directionT.RIGHT;
            }
        }
    }

    // move the content of cell x,y one position to the left
    public void moveLeft(int x, int y) {
        if (!inMotion) {
            if (x > 0 && gameBoardArray.get(x-1,y) == 0 && gameBoardArray.get(x,y) != 0) {
                applyCommonMotionSettings(x,y, x-1, y);
                motionDir = directionT.LEFT;
            }
        }
    }

    public void applyCommonMotionSettings(int x, int y, int newX, int newY) {
        inMotion = true;
        motionRect = getRect(x,y);
        motionPaint.setColor(paintArray[x][y].getColor());
        motionText = gameBoardArray.getText(x,y);
        finalPosition = rectArray[newX][newY];
        gameBoardArrayValueAfterMotion = gameBoardArray.get(x,y);
        xAfterMotion = newX;
        yAfterMotion = newY;
        gameBoardArray.set(x,y,0);
        paintArray[x][y].setColor(getColor(x,y));
        motionIncrementX = (finalPosition.left-motionRect.left) / MOTION_STEPS;
        motionIncrementY = (finalPosition.bottom-motionRect.bottom) / MOTION_STEPS;

    }
}
