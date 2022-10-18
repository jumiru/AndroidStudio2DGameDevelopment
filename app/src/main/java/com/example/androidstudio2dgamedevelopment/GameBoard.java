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
    private static final int STATUS_FIELD_HEIGHT = 500 ;
    private static final int STATUS_TEXT_SIZE = 80;
    private static final int MOTION_STEPS = 10;
    private static final int DROP_INS_AFTER_MOTION = 3;
    private List<directionT> motionPath;
    private Set<Coord> mergeGroup;
    private boolean invalidPath;

    public enum directionT {
        UP, DOWN, LEFT, RIGHT
    }

    public enum statusT {
        SELECT_START_POSITION, SELECT_TARGET_POSITION,
        MOTION, MERGE, DROP_IN_NEW_CELLS, GAME_OVER
    }

    private statusT status;

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
    private directionT motionDir;
    private Rect motionRect;
    private Paint motionPaint;
    private String motionText;
    private Rect finalPosition;
    private Paint redPaint;
    private int xAfterMotion;
    private int yAfterMotion;
    private int gameBoardArrayValueAfterMotion;
    private int motionIncrementX;
    private int motionIncrementY;

    private int startPositionX;
    private int startPositionY;


    private int targetPositionX;
    private int targetPositionY;

    private String statusMessage;
    private Paint statusPaint;

    private Random rand;
    private int score;
    private int highScore;


    private boolean firstStatusMotionEntry;
    private int dropInCount;

    public GameBoard(Context context, int width, int height) {
        this.width = width;
        this.height = height;
        this.context = context;

        textPaint = new Paint();
        textPaint.setColor( 0xffffffff );
        textPaint.setTextSize(TEXT_HEIGHT);

        gameBoardArray = new GameBoardArray(width, height);

        rectArray = new Rect[width][height ];
        paintArray = new Paint[width][height];

        motionPaint = new Paint();
        statusPaint = new Paint();
        statusPaint.setColor(ContextCompat.getColor(context, R.color.status));
        statusPaint.setTextSize(STATUS_TEXT_SIZE);

        rand = new Random(0);
        motionPath = new ArrayList<>();
        redPaint = new Paint();
        redPaint.setColor(0xffff0000);

        gameInit();

    }

    private void gameInit() {
        gameBoardArray.clear();
        gameBoardArray.initCells(4 );
        status = statusT.SELECT_START_POSITION;
        firstStatusMotionEntry = true;
        score = 0;
    }

    private int getColor(int x, int y) {
        int val = gameBoardArray.get(x,y);
        if ( val == -1 ) {
            return getColor(0xff, 0x90, 0x90, 0x90);
        } else {
            return getColor( 0xff, 0xff-63*val, 0x90 + 71*val, 0x00 + 43*val);
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

        if (invalidPath) {
            canvas.drawRect(0,0, width-1, height-1, redPaint);
        }
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

        if (status == statusT.MOTION && motionRect!=null) {
            drawCell(canvas, motionRect, motionPaint, motionText);
        }

        String scoreText = "score: " + Integer.toString(score) + "   (" + Integer.toString(highScore)+")";
        canvas.drawText(statusMessage, 20, canvasHeight-STATUS_TEXT_SIZE, statusPaint );
        canvas.drawText(scoreText, 20, canvasHeight-3*(STATUS_TEXT_SIZE-20), statusPaint );
        canvas.drawCircle(20,canvasHeight-20, 10, paintArray[startPositionX][startPositionY]);
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
        if (status == statusT.SELECT_START_POSITION) {
            statusMessage = "Select start position";
            // handeled by onTouchEvent
        }


        else if (status == statusT.SELECT_TARGET_POSITION) {
            statusMessage = "Select target position";
            // handeled by onTouchEvent
        }


        else if (status == statusT.MOTION) {
            statusMessage = "Motion";
            if (reachedPathPosition() || firstStatusMotionEntry) {
                if ( !firstStatusMotionEntry) {
                    gameBoardArray.set(xAfterMotion, yAfterMotion, gameBoardArrayValueAfterMotion);
                    paintArray[xAfterMotion][yAfterMotion].setColor(motionPaint.getColor());
                }
                firstStatusMotionEntry = false;
                if (!motionPath.isEmpty()) {
                    directionT dir = motionPath.get(motionPath.size() - 1);
                    motionPath.remove(motionPath.size() - 1);
                    if (dir == directionT.LEFT) {
                        moveLeft(startPositionX, startPositionY);
                        startPositionX--;
                    } else if (dir == directionT.DOWN) {
                        moveDown(startPositionX, startPositionY);
                        startPositionY++;
                    } else if (dir == directionT.RIGHT) {
                        moveRight(startPositionX, startPositionY);
                        startPositionX++;
                    } else if (dir == directionT.UP) {
                        moveUp(startPositionX, startPositionY);
                        startPositionY--;
                    }

                } else {
                    status = statusT.MERGE;
                    firstStatusMotionEntry = true;
                    dropInCount = DROP_INS_AFTER_MOTION;
                }
            } else {
                motionRect.left = motionRect.left + motionIncrementX;
                motionRect.right = motionRect.right + motionIncrementX;
                motionRect.bottom = motionRect.bottom + motionIncrementY;
                motionRect.top = motionRect.top + motionIncrementY;
            }
        }


        else if (status == statusT.MERGE) {
            statusMessage = "Merge";
            mergeGroup = merge(targetPositionX, targetPositionY);
            if (mergeGroup.size() >= 4) {
                Iterator<Coord> it = mergeGroup.iterator();
                while (it.hasNext()) {
                    Coord c = it.next();
                    gameBoardArray.set(c.x, c.y, -1);
                }
                gameBoardArray.set(targetPositionX, targetPositionY, gameBoardArrayValueAfterMotion + 2);
                score += (1<<(gameBoardArrayValueAfterMotion + 2)) * (mergeGroup.size()-3);
                if (score>highScore) {
                    highScore = score;
                }
            }
            int freeCells = gameBoardArray.getNumFreeCells();
            if (freeCells == 0) {
                status = statusT.GAME_OVER;
            } else {
                status = statusT.DROP_IN_NEW_CELLS;
            }
        }



        else if (status == statusT.DROP_IN_NEW_CELLS) {
            if (dropInCount>0) {
                statusMessage = "Drop in new cells";

                Coord c = gameBoardArray.randomlyAddCell(3);
                dropInCount--;
                targetPositionX = c.x;
                targetPositionY = c.y;
                status = statusT.MERGE;
            } else {
                status = statusT.SELECT_START_POSITION;
            }
        }

        else if (status == statusT.GAME_OVER) {
            statusMessage = "GAME OVER!";
        }

    }

    private Set<Coord> merge(int x, int y) {
        return gameBoardArray.findMergeGroup(x,y);
    }

    private boolean reachedPathPosition() {

        boolean res = false;
        if ((motionDir == directionT.LEFT && motionRect.left <= finalPosition.left) ||
                        (motionDir == directionT.RIGHT && motionRect.left >= finalPosition.left) ||
                        (motionDir == directionT.UP && motionRect.bottom <= finalPosition.bottom) ||
                        (motionDir == directionT.DOWN && motionRect.bottom >= finalPosition.bottom)
            ) {
            res = true;
        }
        return res;
    }


    private void moveDown(int x, int y) {
        if (y < height-1 && gameBoardArray.get(x,y+1) == -1 && gameBoardArray.get(x,y) != -1) {
            applyCommonMotionSettings(x,y, x, y+1);
            motionDir = directionT.DOWN;
        }
    }

    private void moveUp(int x, int y) {
        if (y > 0 && gameBoardArray.get(x,y-1) == -1 && gameBoardArray.get(x,y) != -1) {
            applyCommonMotionSettings(x,y, x, y-1);
            motionDir = directionT.UP;
        }
    }

    private void moveRight(int x, int y) {
        if (x<width-1 && gameBoardArray.get(x+1,y) == -1 && gameBoardArray.get(x,y) != -1) {
            applyCommonMotionSettings(x,y, x+1, y);
            motionDir = directionT.RIGHT;
        }
    }

    // move the content of cell x,y one position to the left
    public void moveLeft(int x, int y) {
        if (x > 0 && gameBoardArray.get(x-1,y) == -1 && gameBoardArray.get(x,y) != -1) {
            applyCommonMotionSettings(x,y, x-1, y);
            motionDir = directionT.LEFT;
        }
    }

    public void applyCommonMotionSettings(int x, int y, int newX, int newY) {
        motionRect = getRect(x,y);
        motionPaint.setColor(paintArray[x][y].getColor());
        motionText = gameBoardArray.getText(x,y);
        finalPosition = rectArray[newX][newY];
        gameBoardArrayValueAfterMotion = gameBoardArray.get(x,y);
        xAfterMotion = newX;
        yAfterMotion = newY;
        gameBoardArray.set(x,y,-1);
        paintArray[x][y].setColor(getColor(x,y));
        motionIncrementX = (finalPosition.left-motionRect.left) / MOTION_STEPS;
        motionIncrementY = (finalPosition.bottom-motionRect.bottom) / MOTION_STEPS;

    }

    public void onTouchEvent(int x, int y) {

        int indexX = (int)(x / cellWidth);
        int indexY = (int)(y / cellHeight);

        statusMessage = "--- " + Integer.toString(indexX) + " / " + Integer.toString(indexY);

        if ( status == statusT.SELECT_START_POSITION ) {
            if (indexY < height) {
                int cellValue = gameBoardArray.get(indexX, indexY);

                if (cellValue != -1) {
                    // start cell selection
                    startPositionX = indexX;
                    startPositionY = indexY;
                    highlightCell(startPositionX, startPositionY);
                    status = statusT.SELECT_TARGET_POSITION;
                }
            }
        } else if (status == statusT.SELECT_TARGET_POSITION) {
            if (indexY < height) {
                int cellValue = gameBoardArray.get(indexX, indexY);

                if (cellValue!=-1) {
                    //selected new start position
                    resetCell(startPositionX, startPositionY);
                    // start cell selection
                    startPositionX = indexX;
                    startPositionY = indexY;
                    highlightCell(startPositionX, startPositionY);
                    status = statusT.SELECT_TARGET_POSITION;
                } else {
                    targetPositionX = indexX;
                    targetPositionY = indexY;
                    // find path from start to target
                    motionPath = gameBoardArray.findPath(startPositionX, startPositionY, targetPositionX, targetPositionY);
                    if (!motionPath.isEmpty()) {
                        resetCell(startPositionX, startPositionY);
                        status = statusT.MOTION;
                    } else {
                        invalidPath = true;
                    }
                }
            }
        } else if (status == statusT.GAME_OVER) {
            if ( indexY >= height ) {
                gameInit();
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
