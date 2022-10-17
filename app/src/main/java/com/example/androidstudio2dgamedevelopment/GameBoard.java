package com.example.androidstudio2dgamedevelopment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameBoard {

    private static final int RECT_BOARDER = 12;
    private static final float TEXT_HEIGHT = 100.0f;
    private static final int STATUS_FIELD_HEIGHT = 300 ;
    private static final int STATUS_TEXT_SIZE = 80;
    private static final int MOTION_STEPS = 10;
    private List<directionT> motionPath;
    private boolean addedNewCells;
    private boolean merging;
    private Set<Coord> mergeGroup;
    private boolean motionFinished;

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

    private int startPositionX;
    private int startPositionY;
    private int startCellValue;
    private boolean startPositionSelected;

    private int targetPositionX;
    private int targetPositionY;
    private int targetCellValue;

    private String statusMessage;
    private Paint statusPaint;

    private Random rand;

    public GameBoard(Context context, int width, int height) {
        this.width = width;
        this.height = height;
        this.context = context;

        textPaint = new Paint();
        textPaint.setColor( 0xffffffff );
        textPaint.setTextSize(TEXT_HEIGHT);

        gameBoardArray = new GameBoardArray(width, height);
        gameBoardArray.initCells(4 );
        addedNewCells = true;

        rectArray = new Rect[width][height ];
        paintArray = new Paint[width][height];

        motionPaint = new Paint();
        statusPaint = new Paint();
        statusPaint.setColor(ContextCompat.getColor(context, R.color.status));
        statusPaint.setTextSize(STATUS_TEXT_SIZE);
        statusMessage = "---";
        rand = new Random(0);
        motionPath = new ArrayList<>();
    }

    private int getColor(int x, int y) {
        int val = gameBoardArray.get(x,y);
        if ( val == 0 ) {
            return getColor(0xff, 0x90, 0x90, 0x90);
        } else {
            return getColor( 0xff, 0xff-23*val, 0x90 + 77*val, 0x00 + 99*val);
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
                if (paintArray[x][y]==null) {
                    paintArray[x][y] = new Paint();
                }
                paintArray[x][y].setColor(getColor(x,y));
                drawCell(canvas, rectArray[x][y], paintArray[x][y], gameBoardArray.getText(x,y));
            }
        }

        if (inMotion) {
            drawCell(canvas, motionRect, motionPaint, motionText);
        }

        canvas.drawText(statusMessage, 20, canvasHeight-STATUS_FIELD_HEIGHT/2, statusPaint );
        canvas.drawCircle(20,canvasHeight-20, 10, motionPaint);
        canvas.drawCircle(80,canvasHeight-20, 10, statusPaint);
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
                motionRect.right = motionRect.right+motionIncrementX;
                motionRect.bottom = motionRect.bottom+motionIncrementY;
                motionRect.top = motionRect.top + motionIncrementY;
            }
        }

        if (!inMotion) {
            if (!motionPath.isEmpty()) {
                directionT dir = motionPath.get(motionPath.size()-1);
                motionPath.remove(motionPath.size()-1);
                if (motionPath.size()==0) {
                    motionFinished = true;
                }
                if ( dir == directionT.LEFT) {
                    moveLeft(startPositionX, startPositionY);
                    startPositionX--;
                } else if (dir==directionT.DOWN) {
                    moveDown(startPositionX, startPositionY);
                    startPositionY++;
                } else if (dir==directionT.RIGHT) {
                    moveRight(startPositionX,startPositionY);
                    startPositionX++;
                } else if (dir==directionT.UP) {
                    moveUp(startPositionX, startPositionY);
                    startPositionY--;
                }

            } else if (merging) {
                Iterator<Coord> it = mergeGroup.iterator();
                while (it.hasNext()) {
                    Coord c = it.next();
                    gameBoardArray.set(c.x,c.y,0);
                }
                gameBoardArray.set(targetPositionX, targetPositionY, gameBoardArrayValueAfterMotion+2);
                merging = false;
            } else if (motionFinished){
                motionFinished = false;
                mergeGroup = merge(targetPositionX, targetPositionY);
                if (mergeGroup.size()>=4) {
                    merging = true;
                }

                if (!addedNewCells && !merging) {
                    gameBoardArray.randomlyAddCells(3,4);
                    addedNewCells = true;
                }
            }
        }
    }

    private Set<Coord> merge(int x, int y) {
        return gameBoardArray.findMergeGroup(x,y);
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
            if (x < height-1 && gameBoardArray.get(x,y+1) == 0 && gameBoardArray.get(x,y) != 0) {
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
            if (x<width-1 && gameBoardArray.get(x+1,y) == 0 && gameBoardArray.get(x,y) != 0) {
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

    public void onTouchEvent(int x, int y) {

        int indexX = (int)(x / cellWidth);
        int indexY = (int)(y / cellHeight);

        if ( !inMotion && !merging ) {
            if (indexY < height ) {
                statusMessage = "--- " + Integer.toString(indexX) + " / " + Integer.toString(indexY);
                int cellValue = gameBoardArray.get(indexX, indexY);
                int oldCellValue = gameBoardArray.get(startPositionX, startPositionY);
                boolean cellValuesMatch = (cellValue == oldCellValue);

                if (!startPositionSelected && cellValue != 0) {
                    // first start cell selection
                    startPositionSelected = true;
                    startPositionX = indexX;
                    startPositionY = indexY;
                    highlightCell(startPositionX, startPositionY);
                } else if (startPositionSelected && cellValue != 0) {
                    // reset start position selection
                    resetCell(startPositionX, startPositionY);


                    startPositionSelected = false;
                } else if (startPositionSelected) {

                    targetPositionX = indexX;
                    targetPositionY = indexY;

                    // find path
                    motionPath = gameBoardArray.findPath(startPositionX, startPositionY, targetPositionX, targetPositionY);
                    resetCell(startPositionX, startPositionY);
                    if (!motionPath.isEmpty()) {
                        addedNewCells = false;
                        // move cell to target position (in update method)
                        startPositionSelected = false;
                    }
                }
            }
        }
    }

    public void resetCell(int x, int y) {
        paintArray[x][y].setAlpha(0xFF);
        rectArray[x][y].left =
                rectArray[x][y].left - RECT_BOARDER/2;
        rectArray[x][y].right =
                rectArray[x][y].right + RECT_BOARDER/2;
        rectArray[x][y].top =
                rectArray[x][y].top - RECT_BOARDER/2;
        rectArray[x][y].bottom =
                rectArray[x][y].bottom + RECT_BOARDER/2;
    }

    public void highlightCell(int x, int y) {
        paintArray[x][y].setAlpha(0x80);
        rectArray[x][y].left =
                rectArray[x][y].left + RECT_BOARDER/2;
        rectArray[x][y].right =
                rectArray[x][y].right - RECT_BOARDER/2;
        rectArray[x][y].top =
                rectArray[x][y].top + RECT_BOARDER/2;
        rectArray[x][y].bottom =
                rectArray[x][y].bottom - RECT_BOARDER/2;
    }
}
