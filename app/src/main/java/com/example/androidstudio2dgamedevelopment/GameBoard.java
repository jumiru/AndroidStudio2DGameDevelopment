package com.example.androidstudio2dgamedevelopment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameBoard {

    private static final int RECT_BOARDER = 12;
    private static final float TEXT_HEIGHT = 100.0f;
    private static final int STATUS_FIELD_HEIGHT = 500;
    private static final int STATUS_TEXT_SIZE = 80;
    private static final int MOTION_STEPS = 10;
    private static final int GAME_OVER_STEPS = 10;
    private static final int DROP_INS_AFTER_MOTION = 3;
    private static int ALERT_TIME = 30;
    private static int MERGE_ANIMATION_TIME = 30;
    private static int SELECTION_PULSE_TIME = 4;
    private static int SELECTION_PULSE_WIDTH = 8;



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
    private Rect rectArray[][];
    private GameBoardArray gameBoardArray;

    // general dimension information
    private int canvasWidth;
    private int canvasHeight;
    private float cellWidth;
    private float cellHeight;
    private float rectWidth;
    private float rectHeight;
    
    
    SharedPreferences prefs;
    private Random rand;
    private int colorArray[];
    
    // scoring
    private long score;
    private int level;
    private long highScore;
    private boolean highscoreExceeded;
    private int nextScoreForBonus;
    private Paint textPaint; //numbers on the cells
    private Paint scoreAndLevelPaint; // score and level text
    
    private int alertAnimationCounter; //activated if user selects invalid path
    
    private String levelText;
    private int levelAnimationCounter;
    private Paint levelAnimationPaint;


    // counter for dropping in new cells after motion
    private int dropInCount;
    
    // drop in animation
    private boolean dropInAnimationRunning;
    private int dropInAnimationCounter;
    private int dropInValue;
    private Rect dropInRect;
    private float dropInAdderX;
    private float dropInAdderY;
    private Paint dropInAnimationPaint;

    // cell pulsing while selected
    private int selectionPulseCounter;
    private int selectionWidth;
    private int selectionPulseDirection;
    private Rect originalSelectedRect;
    
    
    // store information while one cell is in motion
    private List<directionT> motionPath;
    private directionT motionDir;
    private Rect motionRect;
    private Paint motionPaint;
    private String motionText;
    private Rect finalPosition;
    private Paint redPaint;
    private int xAfterMotion;
    private int yAfterMotion;
    private int gameBoardArrayValueAfterMotion;
    private float motionIncrementX;
    private float motionIncrementY;
    private float offsetX;
    private float offsetY;
    private int startPositionX;
    private int startPositionY;
    private float startFloatPosX;
    private float startFloatPosY;
    private int targetPositionX;
    private int targetPositionY;
    private boolean startMotion;
    
    
    // fields used for merging
    private Set<Coord> mergeGroup;
    private int mergeAnimationStep;
    private Rect mergeRects[];
    private float mergeIncrementsX[];
    private float mergeIncrementsY[];
    private int numMergeRects;
    private Paint mergePaint;
    private String mergeText;

    // bonus actions
    private int bonusIconBoarder;
    private int bonusIconWidth;
    
    private int animateBonusCounter;
    private int bonusWin;
    private Rect bonusWinRect;
    private Drawable bonusWinDrawable;
    
    private Rect undoRect;
    private Rect swapRect;
    private Rect jumpRect;
    private Rect delRowRect;
    private Rect delColRect;
    private Drawable undo;
    private Drawable jump;
    private Drawable swap;
    private Drawable delRow;
    private Drawable delCol;
    private int undoCounter;
    private int swapCounter;
    private int jumpCounter;
    private int delRowCounter;
    private int delColCounter;

    private boolean undoSelected;
    private boolean swapSelected;
    private boolean jumpSelected;
    private boolean delRowSelected;
    private boolean delColSelected;

    private String undoText;
    private String swapText;
    private String jumpText;
    private String delRowText;
    private String delColText;

    private Paint bonusPaint;
    
    // swap bonus
    private Rect swapMotionRect;
    private Paint swapMotionPaint;
    private String swapMotionText;
    private Rect swapFinalPosition;
    private int swapPartnerValueAfterMotion;
    private int xAfterSwapMotion;
    private int yAfterSwapMotion;
    private float swapMotionIncrementX;
    private float swapMotionIncrementY;
    private float swapOffsetX;
    private float swapOffsetY;
    private float swapStartFloatPosX;
    private float swapStartFloatPosY;
    private int startSwapPositionX;
    private int startSwapPositionY;
    private List<directionT> inverseMotionPath;
    private int swap2ndMergePositionX;
    private int swap2ndMergePositionY;
    
    // game over
    private int gameOverAnimationCounter;
    private int gameOverAnimationPhase;
    private Rect gameOverRect;
    private Paint gameOverPaint;
    private Paint gameOverTextPaint;
    private String gameOverText = "game over!";

    
    
    
    
    public GameBoard(Context context, SharedPreferences p) {

        this.prefs = p;

        this.width = prefs.getInt("width", 5);
        this.height = prefs.getInt("height", 7);
        this.highScore = prefs.getLong("highscore", 0);

        updateBonusValues();
        
        this.context = context;
        
        textPaint = new Paint();
        textPaint.setColor(0xffffffff);
        textPaint.setTextSize(TEXT_HEIGHT);

        gameBoardArray = new GameBoardArray(width, height);

        rectArray = new Rect[width][height];
        paintArray = new Paint[width][height];

        motionPaint = new Paint();
        swapMotionPaint = new Paint();
        scoreAndLevelPaint = new Paint();
        scoreAndLevelPaint.setColor(ContextCompat.getColor(context, R.color.status));
        scoreAndLevelPaint.setTextSize(STATUS_TEXT_SIZE);

        levelAnimationPaint = new Paint();
        levelAnimationPaint.setColor(0xffffffff);

        rand = new Random();
        motionPath = new ArrayList<>();
        inverseMotionPath = new ArrayList<>();
        redPaint = new Paint();
        redPaint.setColor(0xffff0000);

        // the colors are generated by a pseudo random generator with fixed seed
        Random crand = new Random(44);
        colorArray = new int[40];
        for (int i = 0; i < 40; i++) {
            int r = crand.nextInt(0xe0) + 0x10;
            int g = crand.nextInt(0xe0) + 0x10;
            int b = crand.nextInt(0xe0) + 0x10;
            colorArray[i] = getColor(0xff, r, g, b);
        }
        colorArray[5] = getColor(0xff, 0x80, 0x10, 0x80);
        gameInit();

    }


    private void gameInit() {
        gameBoardArray.clear();
        gameBoardArray.initCells(4);
        status = statusT.SELECT_START_POSITION;
        startMotion = true;
        score = 0;
        nextScoreForBonus = 256;
        highscoreExceeded = false;


        undoCounter = 0;
        swapCounter = 0;
        jumpCounter = 0;
        delRowCounter = 0;
        delColCounter = 0;


        level = 1;
        levelText = "Level: " + Integer.toString(level);
        updateBonusValues();
    }

    private int getColor(int x, int y) {
        int val = gameBoardArray.get(x, y);
        if (val == -1) {
            return getColor(0xff, 0x90, 0x90, 0x90);
        }
        if (val < 40) {
            return colorArray[val];
        } else {
            return getColor(0xff, 0xff - 63 * val, 0x90 + 71 * val, 0x00 + 43 * val);
        }
    }

    private int getColor(int A, int R, int G, int B) {
        return (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
    }


    public void draw(Canvas canvas) {

        // first time initializations
        if (canvasWidth == 0) {
            canvasWidth = canvas.getWidth();
            canvasHeight = canvas.getHeight();
            cellWidth = canvasWidth / width;
            cellHeight = (canvasHeight - STATUS_FIELD_HEIGHT) / height;
            rectWidth = cellWidth - 2.0f * RECT_BOARDER;
            rectHeight = cellHeight - 2.0f * RECT_BOARDER;
            initBonusIcons();
        }

        if (alertAnimationCounter > 0) {
            animateAlert(canvas);
        }

        drawGameboard(canvas);
        drawMotionRect(canvas);
        drawMergeRects(canvas);

        if (status == statusT.GAME_OVER) {
            drawGameOverAnimation(canvas);
        }

        drawScoreAndLevelInfo(canvas);

        if (undoSelected) {
            highlightBonusSelection(undoRect, canvas);
        } else if (swapSelected) {
            highlightBonusSelection(swapRect, canvas);
        } else if (jumpSelected) {
            highlightBonusSelection(jumpRect, canvas);
        } else if (delRowSelected) {
            highlightBonusSelection(delRowRect, canvas);
        } else if (delColSelected) {
            highlightBonusSelection(delColRect, canvas);
        }

        drawBonusInformation(canvas);

        if (levelAnimationCounter > 0) {
            drawLevelAnimation(canvas);
        } else if (animateBonusCounter > 0) {
            drawBonusAnimation(canvas);
        }

        if (dropInAnimationRunning) {
            drawDropInAnimation(canvas);
        }
    }

    private void drawDropInAnimation(Canvas canvas) {
        canvas.drawRect(dropInRect, dropInAnimationPaint);
    }

    private void drawBonusAnimation(Canvas canvas) {
        bonusWinDrawable.setBounds(bonusWinRect);
        bonusWinDrawable.draw(canvas);
    }

    private void drawLevelAnimation(Canvas canvas) {
        levelAnimationPaint.setTextSize(3 * levelAnimationCounter);
        canvas.drawText(levelText, getApproxXToCenterText(levelText, levelAnimationPaint, canvasWidth), canvasWidth / 2, levelAnimationPaint);
    }

    private void drawScoreAndLevelInfo(Canvas canvas) {
        String scoreText = "score: " + getScoreText(score) + "   (" + getScoreText(highScore) + ")";
        canvas.drawText(levelText, 20, canvasHeight - STATUS_TEXT_SIZE, scoreAndLevelPaint);
        canvas.drawText(scoreText, 20, canvasHeight - 3 * (STATUS_TEXT_SIZE - 20), scoreAndLevelPaint);
    }

    private void drawBonusInformation(Canvas canvas) {
        // draw bonus icons
        undo.draw(canvas);
        jump.draw(canvas);
        swap.draw(canvas);
        delRow.draw(canvas);
        delCol.draw(canvas);

        int offset = getApproxXToCenterText(undoText, bonusPaint, (int) cellWidth);
        canvas.drawText(undoText, 0 * cellWidth + offset, height*cellHeight + bonusIconWidth + RECT_BOARDER + bonusPaint.getTextSize(), bonusPaint);

        offset = getApproxXToCenterText(swapText, bonusPaint, (int) cellWidth);
        canvas.drawText(swapText, 1 * cellWidth + offset, height*cellHeight + bonusIconWidth + RECT_BOARDER + bonusPaint.getTextSize(), bonusPaint);

        offset = getApproxXToCenterText(jumpText, bonusPaint, (int) cellWidth);
        canvas.drawText(jumpText, 2 * cellWidth + offset, height*cellHeight + bonusIconWidth + RECT_BOARDER + bonusPaint.getTextSize(), bonusPaint);

        offset = getApproxXToCenterText(delRowText, bonusPaint, (int) cellWidth);
        canvas.drawText(delRowText, 3 * cellWidth + offset, height*cellHeight + bonusIconWidth + RECT_BOARDER + bonusPaint.getTextSize(), bonusPaint);

        offset = getApproxXToCenterText(delColText, bonusPaint, (int) cellWidth);
        canvas.drawText(delColText, 4 * cellWidth + offset, height*cellHeight + bonusIconWidth + RECT_BOARDER + bonusPaint.getTextSize(), bonusPaint);
    }

    private void highlightBonusSelection(Rect bonusRect, Canvas canvas) {
        Rect r = new Rect(bonusRect.left - RECT_BOARDER,
                bonusRect.top - RECT_BOARDER,
                bonusRect.right + RECT_BOARDER,
                bonusRect.bottom + RECT_BOARDER
        );
        Paint p = new Paint();
        p.setColor(0xff00aa33);
        canvas.drawRect(r, p);
    }

    private void drawGameOverAnimation(Canvas canvas) {
        if (gameOverAnimationPhase >= 1) {

            if (gameOverAnimationPhase == 1) {
                gameOverTextPaint.setColor(0xffffffff);

                if (gameOverAnimationCounter < (canvasHeight - STATUS_FIELD_HEIGHT) / 2) {
                    gameOverAnimationCounter += GAME_OVER_STEPS;
                    gameOverRect.top = (canvasHeight - STATUS_FIELD_HEIGHT) / 2 - gameOverAnimationCounter;
                    gameOverRect.bottom = (canvasHeight - STATUS_FIELD_HEIGHT) / 2 + gameOverAnimationCounter;
                } else {
                    gameOverAnimationPhase = 2;
                    gameOverTextPaint.setColor(0xffffffff);
                }
            } else {
                gameOverAnimationCounter += GAME_OVER_STEPS;
                if (gameOverAnimationCounter > canvasWidth) {
                    gameOverAnimationCounter = -((int) gameOverTextPaint.measureText(gameOverText));
                }
            }
            canvas.drawRect(gameOverRect, gameOverPaint);
            canvas.drawText(gameOverText, gameOverAnimationCounter, getTextPosY(gameOverRect, gameOverTextPaint), gameOverTextPaint);
        }
    }

    private void drawMergeRects(Canvas canvas) {
        if (status == statusT.MERGE && mergeAnimationStep != 0) {
            for (int i = 0; i < numMergeRects; i++) {
                drawCell(canvas, mergeRects[i], mergePaint, mergeText);
            }
        }
    }

    private void drawMotionRect(Canvas canvas) {
        if (status == statusT.MOTION && motionRect != null) {
            drawCell(canvas, motionRect, motionPaint, motionText);
            if (swapSelected) {
                drawCell(canvas, swapMotionRect, swapMotionPaint, swapMotionText);
            }
        }
    }

    private void drawGameboard(Canvas canvas) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (rectArray[x][y] == null) {
                    rectArray[x][y] = getRect(x, y);
                }
                if (paintArray[x][y] == null) {
                    paintArray[x][y] = new Paint();
                }
                paintArray[x][y].setColor(getColor(x, y));
                drawCell(canvas, rectArray[x][y], paintArray[x][y], getText(x, y));
            }
        }
    }

    private void animateAlert(Canvas canvas) {
        alertAnimationCounter--;
        int red = 0;
        if (alertAnimationCounter > ALERT_TIME / 2) {
            red = ((ALERT_TIME - alertAnimationCounter) + 1) * (150) / (ALERT_TIME / 2);
        } else {
            red = alertAnimationCounter * (150) / (ALERT_TIME / 2);
        }
        redPaint.setColor(getColor(0xFF, red, 0, 0));

        canvas.drawRect(0, 0, canvasWidth - 1, canvasHeight - STATUS_FIELD_HEIGHT, redPaint);
    }

    private void initBonusIcons() {
        bonusIconBoarder = 6 * RECT_BOARDER;
        bonusIconWidth = (int) (cellWidth - 2 * bonusIconBoarder);
        int top = (int) (height * cellHeight + RECT_BOARDER);

        int left = (int) (0 * cellWidth + bonusIconBoarder);
        undoRect = new Rect(left, top, left + bonusIconWidth, top + bonusIconWidth);

        left = (int) (1 * cellWidth + bonusIconBoarder);
        swapRect = new Rect(left, top, left + bonusIconWidth, top + bonusIconWidth);

        left = (int) (2 * cellWidth + bonusIconBoarder);
        jumpRect = new Rect(left, top, left + bonusIconWidth, top + bonusIconWidth);

        left = (int) (3 * cellWidth + bonusIconBoarder);
        delRowRect = new Rect(left, top + 2 * RECT_BOARDER, left + bonusIconWidth, top + bonusIconWidth - 2 * RECT_BOARDER);

        left = (int) (4 * cellWidth + bonusIconBoarder);
        delColRect = new Rect(left + 2 * RECT_BOARDER, top, left + bonusIconWidth - 2 * RECT_BOARDER, top + bonusIconWidth);

        undo = context.getResources().getDrawable(R.drawable.undo_circle_icon, null);
        undo.setBounds(undoRect);

        swap = context.getResources().getDrawable(R.drawable.swap_icon, null);
        swap.setBounds(swapRect);

        jump = context.getResources().getDrawable(R.drawable.jump_icon, null);
        jump.setBounds(jumpRect);

        delRow = context.getResources().getDrawable(R.drawable.cross_icon, null);
        delRow.setBounds(delRowRect);

        delCol = context.getResources().getDrawable(R.drawable.cross_icon, null);
        delCol.setBounds(delColRect);

        bonusPaint = new Paint();
        bonusPaint.setColor(0xffffffff);
        bonusPaint.setTextSize(50);
    }

    private void drawCell(Canvas canvas, Rect rect, Paint paint, String text) {
        canvas.drawRect(rect, paint);
        textPaint.setTextSize(getTextSize(text));
        canvas.drawText(text, getTextPosX(rect, text, textPaint), getTextPosY(rect, textPaint), textPaint);
    }

    private float getTextSize(String text) {
        float res = TEXT_HEIGHT * (100.0f - (text.length() - 1) * 10.0f) / 100.0f;
        return res;
    }


    private float getTextPosY(Rect rect, Paint p) {
        float posY = rect.bottom;
        return posY - ((rect.bottom - rect.top) / 2 - (p.getTextSize()) / 2) - RECT_BOARDER;
    }

    private float getTextPosX(Rect rect, String text, Paint paint) {
        float posX = rect.left;
        float thisCellWidth = rect.right - rect.left;
        int offset = getApproxXToCenterText(text, paint, (int) thisCellWidth);
        return posX + offset;
    }

    public static int getApproxXToCenterText(String text, Paint p, int widthToFitStringInto) {
        float textWidth = p.measureText(text);
        int xOffset = (int) ((widthToFitStringInto - textWidth) / 2f);
        return xOffset;
    }

    private Rect getRect(int x, int y) {
        Rect rect = new Rect(
                x * (int) cellWidth + RECT_BOARDER,
                y * (int) cellHeight + RECT_BOARDER,
                (x + 1) * (int) cellWidth - RECT_BOARDER,
                (y + 1) * (int) cellHeight - RECT_BOARDER
        );
        return rect;
    }

    public void update() {
        //-----------------------------------------------------
        // general animation
        if (levelAnimationCounter > 0) {
            animateLevel();
        } else if (animateBonusCounter > 0) {
            animateBonusWin();
        }


        //-----------------------------------------------------
        if (status == statusT.SELECT_START_POSITION) {
            // handeled by onTouchEvent
            gameBoardArray.backupGameBoardWithNextModification(); // used for undo bonus
            
        }


        // -----------------------------------------------------
        else if (status == statusT.SELECT_TARGET_POSITION) {
            animateSelectionPulse();
        }


        // -----------------------------------------------------
        else if (status == statusT.MOTION) {
            if (jumpSelected) {
                animateJump();
            } else {
                // normal motion along the motion path
                normalMotionAnimation();
            }
        }


        // -----------------------------------------------------
        else if (status == statusT.MERGE) {
            handleCellMerging();
        }


        // -----------------------------------------------------
        else if (status == statusT.DROP_IN_NEW_CELLS) {
            handleNewCellDropIns();
        }


        // -----------------------------------------------------
        else if (status == statusT.GAME_OVER) {
            if (gameOverAnimationPhase == 0) {
                startGameOverAnimation();
            }
            
            if (highscoreExceeded) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("highscore", highScore);
                editor.apply();
            }
        }

    }

    private void handleNewCellDropIns() {
        if (dropInCount > 0 && !dropInAnimationRunning) {

            Coord c = gameBoardArray.randomlyAddCell(level - 1, 2 + level);

            dropInCount--;
            targetPositionX = c.x;
            targetPositionY = c.y;

            startDropInAnimation();

        } else {
            if (dropInAnimationRunning) {
                animateDropIn();
            } else {
                status = statusT.SELECT_START_POSITION;
            }
        }
    }

    private void handleCellMerging() {
        if (mergeAnimationStep == 0) {
            gameBoardArrayValueAfterMotion = gameBoardArray.get(targetPositionX, targetPositionY);
            mergeGroup = merge(targetPositionX, targetPositionY);
            if (mergeGroup.size() >= 4) {
                startMergeAnimation();
            } else {
                if (swapSelected) {
                    swapSelected = false;
                    targetPositionX = swap2ndMergePositionX;
                    targetPositionY = swap2ndMergePositionY;
                    status = statusT.MERGE; // do merge for the swap target position too
                } else {
                    status = statusT.DROP_IN_NEW_CELLS;
                }
            }
            int freeCells = gameBoardArray.getNumFreeCells();
            if (freeCells == 0) {
                status = statusT.GAME_OVER;
            }
        } else {
            mergeAnimationStep--;
            for (int i = 0; i < numMergeRects; i++) {
                mergeRects[i].left = (int) (((float) mergeRects[i].left) + mergeIncrementsX[i]);
                mergeRects[i].right = (int) (((float) mergeRects[i].right) + mergeIncrementsX[i]);
                mergeRects[i].top = (int) (((float) mergeRects[i].top) + mergeIncrementsY[i]);
                mergeRects[i].bottom = (int) (((float) mergeRects[i].bottom) + mergeIncrementsY[i]);
            }
            if (mergeAnimationStep == 0) {
                finishMergeAnimation();
            }
        }
    }

    private void normalMotionAnimation() {
        if (reachedPathPosition() || startMotion) {
            if (!startMotion) {
                gameBoardArray.set(xAfterMotion, yAfterMotion, gameBoardArrayValueAfterMotion);
                paintArray[xAfterMotion][yAfterMotion].setColor(motionPaint.getColor());
            }
            startMotion = false;
            if (!motionPath.isEmpty()) {
                directionT dir = motionPath.get(motionPath.size() - 1);
                handleNextMotionStep(dir);

                if (swapSelected) {
                    handleNextSwapMotionStep(dir);
                }

            } else {
                if (swapSelected) {
                    swapCounter--;
                    updateBonusValues();
                    gameBoardArray.set(xAfterSwapMotion, yAfterSwapMotion, swapPartnerValueAfterMotion);
                    paintArray[xAfterSwapMotion][yAfterSwapMotion].setColor(swapMotionPaint.getColor());
                    dropInCount = 0;
                } else {
                    dropInCount = DROP_INS_AFTER_MOTION+level/10;
                }
                status = statusT.MERGE;
                startMotion = true;
            }
        } else {
            offsetX = offsetX + motionIncrementX;
            offsetY = offsetY + motionIncrementY;
            motionRect.left = (int) (startFloatPosX + offsetX);
            motionRect.right = (int) (startFloatPosX + offsetX + rectWidth);
            motionRect.top = (int) (startFloatPosY + offsetY);
            motionRect.bottom = (int) (startFloatPosY + offsetY + rectHeight);
            if (swapSelected) {
                swapOffsetX = swapOffsetX + swapMotionIncrementX;
                swapOffsetY = swapOffsetY + swapMotionIncrementY;
                swapMotionRect.left = (int) (swapStartFloatPosX + swapOffsetX);
                swapMotionRect.right = (int) (swapStartFloatPosX + swapOffsetX + rectWidth);
                swapMotionRect.top = (int) (swapStartFloatPosY + swapOffsetY);
                swapMotionRect.bottom = (int) (swapStartFloatPosY + swapOffsetY + rectHeight);
            }
        }
    }

    private void handleNextSwapMotionStep(directionT dir) {
        dir = inverseMotionPath.get(inverseMotionPath.size() - 1);
        inverseMotionPath.remove(inverseMotionPath.size() - 1);
        if (dir == directionT.LEFT) {
            applyCommonSwapMotionSettings(startSwapPositionX, startSwapPositionY, --startSwapPositionX, startSwapPositionY);
        } else if (dir == directionT.DOWN) {
            applyCommonSwapMotionSettings(startSwapPositionX, startSwapPositionY, startSwapPositionX, ++startSwapPositionY);
        } else if (dir == directionT.RIGHT) {
            applyCommonSwapMotionSettings(startSwapPositionX, startSwapPositionY, ++startSwapPositionX, startSwapPositionY);
        } else if (dir == directionT.UP) {
            applyCommonSwapMotionSettings(startSwapPositionX, startSwapPositionY, startSwapPositionX, --startSwapPositionY);
        }
    }

    private void handleNextMotionStep(directionT dir) {
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
    }

    private void animateJump() {
        if (reachedJumpTargetPosition()) {
            status = statusT.MERGE;
            startMotion = true;
            dropInCount = 0;
            jumpSelected = false;
            jumpCounter--;
            gameBoardArray.set(xAfterMotion, yAfterMotion, gameBoardArrayValueAfterMotion);
            paintArray[xAfterMotion][yAfterMotion].setColor(motionPaint.getColor());
            updateBonusValues();
        } else {
            offsetX = offsetX + motionIncrementX / 5.0f;
            offsetY = offsetY + motionIncrementY / 5.0f;
            motionRect.left = (int) (startFloatPosX + offsetX);
            motionRect.right = (int) (startFloatPosX + offsetX + rectWidth);
            motionRect.top = (int) (startFloatPosY + offsetY);
            motionRect.bottom = (int) (startFloatPosY + offsetY + rectHeight);

        }
    }

    private void animateSelectionPulse() {
        selectionPulseCounter--;
        if (selectionPulseCounter == 0) {
            rectArray[startPositionX][startPositionY].left += selectionPulseDirection;
            rectArray[startPositionX][startPositionY].right -= selectionPulseDirection;
            rectArray[startPositionX][startPositionY].top += selectionPulseDirection;
            rectArray[startPositionX][startPositionY].bottom -= selectionPulseDirection;
            selectionPulseCounter = SELECTION_PULSE_TIME;

            selectionWidth++;
            if (selectionWidth == SELECTION_PULSE_WIDTH) {
                selectionWidth = 0;
                selectionPulseDirection *= -1;
            }
        }
    }

    private void startDropInAnimation() {
        dropInAnimationCounter = 100;
        dropInAnimationRunning = true;
        dropInValue = gameBoardArray.get(targetPositionX, targetPositionY);

        dropInAdderX = cellWidth / 2.0f - RECT_BOARDER;
        dropInAdderY = cellHeight / 2.0f - RECT_BOARDER;

        dropInRect = getRect(targetPositionX, targetPositionY);
        dropInRect.left = rectArray[targetPositionX][targetPositionY].left + (int) dropInAdderX;
        dropInRect.right = rectArray[targetPositionX][targetPositionY].right - (int) dropInAdderY;
        dropInRect.top = rectArray[targetPositionX][targetPositionY].top + (int) dropInAdderY;
        dropInRect.bottom = rectArray[targetPositionX][targetPositionY].bottom - (int) dropInAdderY;

        dropInAnimationPaint = new Paint();
        dropInAnimationPaint.setColor(getColor(targetPositionX, targetPositionY));

        gameBoardArray.set(targetPositionX, targetPositionY, -1);
    }

    private void animateDropIn() {
        if (dropInAnimationCounter > 0) {
            dropInAnimationCounter -= 10;
            int adderX = (int) (dropInAdderX * dropInAnimationCounter / 100.0f);
            int adderY = (int) (dropInAdderY * dropInAnimationCounter / 100.0f);
            dropInRect.left = rectArray[targetPositionX][targetPositionY].left + (int) adderX;
            dropInRect.right = rectArray[targetPositionX][targetPositionY].right - (int) adderX;
            dropInRect.top = rectArray[targetPositionX][targetPositionY].top + (int) adderY;
            dropInRect.bottom = rectArray[targetPositionX][targetPositionY].bottom - (int) adderY;


        } else {
            gameBoardArray.set(targetPositionX, targetPositionY, dropInValue);
            dropInAnimationRunning = false;
            status = statusT.MERGE;
        }
    }

    private boolean reachedJumpTargetPosition() {
        float leftLeftBoarder = (float) finalPosition.left - Math.abs(motionIncrementX);
        float leftRightBoarder = (float) finalPosition.left + Math.abs(motionIncrementX);

        float topTopBoarder = (float) finalPosition.top - Math.abs(motionIncrementY);
        float topBottomBoarder = (float) finalPosition.top + Math.abs(motionIncrementY);

        float motionRectLeft = (float) motionRect.left;
        float motionRectTop = (float) motionRect.top;

        if ((leftLeftBoarder <= motionRectLeft) && (motionRectLeft <= leftRightBoarder) &&
                (topTopBoarder <= motionRectTop) && (motionRectTop <= topBottomBoarder)) {
            return true;
        }
        return false;
    }

    private void startGameOverAnimation() {
        gameOverAnimationPhase = 1;
        gameOverAnimationCounter = 0;
        gameOverRect = new Rect();
        gameOverPaint = new Paint();
        gameOverPaint.setColor(0xc0000000);
        gameOverTextPaint = new Paint();
        gameOverTextPaint.setTextSize(100.0f);
        gameOverRect.left = 0;
        gameOverRect.right = canvasWidth;
    }

    private void startMergeAnimation() {
        int targetX = rectArray[targetPositionX][targetPositionY].left;
        int targetY = rectArray[targetPositionX][targetPositionY].top;
        numMergeRects = mergeGroup.size();
        Iterator<Coord> it = mergeGroup.iterator();
        mergeRects = new Rect[numMergeRects];
        mergeIncrementsX = new float[numMergeRects];
        mergeIncrementsY = new float[numMergeRects];
        mergeText = getText(targetPositionX, targetPositionY);
        mergePaint = new Paint(paintArray[targetPositionX][targetPositionY]);
        int i = 0;
        while (it.hasNext()) {
            Coord c = it.next();
            gameBoardArray.set(c.x, c.y, -1);
            mergeRects[i] = new Rect(rectArray[c.x][c.y]);
            mergeIncrementsX[i] = ((float) (targetX - mergeRects[i].left)) / ((float) MERGE_ANIMATION_TIME);
            mergeIncrementsY[i] = ((float) (targetY - mergeRects[i].top)) / ((float) MERGE_ANIMATION_TIME);
            i++;
        }

        mergeAnimationStep = MERGE_ANIMATION_TIME;
        gameBoardArray.set(targetPositionX, targetPositionY, gameBoardArrayValueAfterMotion + 2);
    }

    private void finishMergeAnimation() {
        if (dropInCount == DROP_INS_AFTER_MOTION+level/10) {
            dropInCount = 0;
        }

        score += (1 << (gameBoardArrayValueAfterMotion + 2)) * (mergeGroup.size() - 3);

        int newLevel = 1;
        long test = score;
        test = test >> 10;
        while (test > 0) {
            newLevel++;
            test = test >> 1;
        }

        if (newLevel > level) {
            level = newLevel;
            startLevelAnimation();
        }

        if (score > highScore) {
            highScore = score;
            highscoreExceeded = true;
        }
        if (score >= nextScoreForBonus) {
            nextScoreForBonus *= 2;
            bonusWin = rand.nextInt(5);
            switch (bonusWin) {
                case 0:
                    undoCounter++;
                    break;
                case 1:
                    swapCounter++;
                    break;
                case 2:
                    jumpCounter++;
                    break;
                case 3:
                    delRowCounter++;
                    break;
                case 4:
                    delColCounter++;
                    break;
            }

            startBonusAnimation();
        }

    }

    private void startBonusAnimation() {
        animateBonusCounter = 50;

        bonusWinRect = new Rect(canvasWidth / 4, canvasWidth / 4, canvasWidth * 3 / 4, canvasWidth * 3 / 4);

        switch (bonusWin) {
            case 0:
                bonusWinDrawable = context.getResources().getDrawable(R.drawable.undo_circle_icon, null);
                break;
            case 1:
                bonusWinDrawable = context.getResources().getDrawable(R.drawable.swap_icon, null);
                break;
            case 2:
                bonusWinDrawable = context.getResources().getDrawable(R.drawable.jump_icon, null);
                break;
            case 3:
                bonusWinDrawable = context.getResources().getDrawable(R.drawable.cross_icon, null);
                break;
            case 4:
                bonusWinDrawable = context.getResources().getDrawable(R.drawable.cross_icon, null);
                break;
        }

        bonusWinDrawable.setBounds(bonusWinRect);
    }

    private void animateBonusWin() {
        animateBonusCounter--;
        bonusWinRect.left = bonusWinRect.left - 2;
        bonusWinRect.right = bonusWinRect.right + 2;
        bonusWinRect.top = bonusWinRect.top - 2;
        bonusWinRect.bottom = bonusWinRect.bottom + 2;

        if (animateBonusCounter == 0) {
            finishBonusAnimation();
        }
    }

    private void finishBonusAnimation() {
        updateBonusValues();
    }

    private void startLevelAnimation() {
        levelAnimationCounter = 100;
        levelText = "Level: " + Integer.toString(level);
    }

    private void animateLevel() {
        levelAnimationCounter--;
        if (levelAnimationCounter == 0) {
            finishLevelAnimation();
        }
    }

    private void finishLevelAnimation() {
        updateBonusValues();
        gameBoardArray.removeAllCellsSmallerThan(level);
    }


    private void updateBonusValues() {
        undoText = Integer.toString(undoCounter);
        swapText = Integer.toString(swapCounter);
        jumpText = Integer.toString(jumpCounter);
        delRowText = Integer.toString(delRowCounter);
        delColText = Integer.toString(delColCounter);
    }

    private Set<Coord> merge(int x, int y) {
        return gameBoardArray.findMergeGroup(x, y);
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
        //if (y < height-1 && gameBoardArray.get(x,y+1) == -1 && gameBoardArray.get(x,y) != -1) {
        applyCommonMotionSettings(x, y, x, y + 1);
        motionDir = directionT.DOWN;
        //}
    }

    private void moveUp(int x, int y) {
        //if (y > 0 && gameBoardArray.get(x,y-1) == -1 && gameBoardArray.get(x,y) != -1) {
        applyCommonMotionSettings(x, y, x, y - 1);
        motionDir = directionT.UP;
        //}
    }

    private void moveRight(int x, int y) {
        //if (x<width-1 && gameBoardArray.get(x+1,y) == -1 && gameBoardArray.get(x,y) != -1) {
        applyCommonMotionSettings(x, y, x + 1, y);
        motionDir = directionT.RIGHT;
        //}
    }

    // move the content of cell x,y one position to the left
    public void moveLeft(int x, int y) {
        //if (x > 0 && gameBoardArray.get(x-1,y) == -1 && gameBoardArray.get(x,y) != -1) {
        applyCommonMotionSettings(x, y, x - 1, y);
        motionDir = directionT.LEFT;
        //}
    }

    public void applyCommonMotionSettings(int x, int y, int newX, int newY) {
        motionRect = getRect(x, y);
        motionPaint.setColor(paintArray[x][y].getColor());
        motionText = getText(x, y);
        finalPosition = rectArray[newX][newY];
        gameBoardArrayValueAfterMotion = gameBoardArray.get(x, y);
        xAfterMotion = newX;
        yAfterMotion = newY;
        gameBoardArray.set(x, y, -1);
        paintArray[x][y].setColor(getColor(x, y));
        motionIncrementX = (float) (finalPosition.left - motionRect.left) / (float) MOTION_STEPS;
        motionIncrementY = (float) (finalPosition.top - motionRect.top) / (float) MOTION_STEPS;
        offsetX = 0;
        offsetY = 0;
        startFloatPosX = motionRect.left;
        startFloatPosY = motionRect.top;
    }

    public void applyCommonSwapMotionSettings(int x, int y, int newX, int newY) {
        swapFinalPosition = rectArray[newX][newY];
        xAfterSwapMotion = newX;
        yAfterSwapMotion = newY;
        swapMotionIncrementX = (float) (swapFinalPosition.left - swapMotionRect.left) / (float) MOTION_STEPS;
        swapMotionIncrementY = (float) (swapFinalPosition.top - swapMotionRect.top) / (float) MOTION_STEPS;
        swapOffsetX = 0;
        swapOffsetY = 0;
        swapStartFloatPosX = swapMotionRect.left;
        swapStartFloatPosY = swapMotionRect.top;
    }

    public void performJump(int fromX, int fromY, int toX, int toY) {
        applyCommonMotionSettings(fromX, fromY, toX, toY);
    }

    public void onTouchEvent(int x, int y) {

        int indexX = (int) (x / cellWidth);
        int indexY = (int) (y / cellHeight);

        if (status == statusT.GAME_OVER) {
            gameOverAnimationPhase = 0;
            gameInit();
        }

        if (status == statusT.SELECT_START_POSITION) {
            if (indexY < height) {
                int cellValue = gameBoardArray.get(indexX, indexY);

                if (cellValue != -1) {
                    // start cell selection
                    startPositionX = indexX;
                    startPositionY = indexY;
                    highlightCell(startPositionX, startPositionY);
                    status = statusT.SELECT_TARGET_POSITION;


                    if (delRowSelected) {
                        resetCell(startPositionX, startPositionY);
                        for (x = 0; x < width; x++) {
                            gameBoardArray.set(x, indexY, -1);
                        }
                        delRowSelected = false;
                        delRowCounter--;
                        updateBonusValues();
                        if (gameBoardArray.getNumFreeCells() == width * height) {
                            status = statusT.DROP_IN_NEW_CELLS;
                            dropInCount = DROP_INS_AFTER_MOTION+level/10;
                        } else {
                            status = statusT.SELECT_START_POSITION;
                        }
                    } else if (delColSelected) {
                        resetCell(startPositionX, startPositionY);
                        for (y = 0; y < height; y++) {
                            gameBoardArray.set(indexX, y, -1);
                        }
                        delColSelected = false;
                        delColCounter--;
                        updateBonusValues();
                        if (gameBoardArray.getNumFreeCells() == width * height) {
                            status = statusT.DROP_IN_NEW_CELLS;
                            dropInCount = DROP_INS_AFTER_MOTION+level/10;
                        } else {
                            status = statusT.SELECT_START_POSITION;
                        }
                    }
                }
            }
        } else if (status == statusT.SELECT_TARGET_POSITION) {
            if (indexY < height) {
                int cellValue = gameBoardArray.get(indexX, indexY);

                if (cellValue != -1 && !swapSelected) {
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
                    if (jumpSelected) {
                        resetCell(startPositionX, startPositionY);
                        applyCommonMotionSettings(startPositionX, startPositionY, targetPositionX, targetPositionY);
                        status = statusT.MOTION;
                    } else {
                        // normal motion, i.e. find path from start to target
                        motionPath = gameBoardArray.findPath(startPositionX, startPositionY, targetPositionX, targetPositionY, swapSelected);
                        if (swapSelected) {
                            if (cellValue != -1) {
                                invertMotionPath();
                                swapPartnerValueAfterMotion = gameBoardArray.get(targetPositionX, targetPositionY);
                                startSwapPositionX = indexX;
                                startSwapPositionY = indexY;
                                swapMotionPaint.setColor(paintArray[indexX][indexY].getColor());
                                swapMotionText = getText(indexX, indexY);
                                swapMotionRect = getRect(indexX, indexY);
                                gameBoardArray.set(targetPositionX, targetPositionY, -1);
                                swap2ndMergePositionX = startPositionX;
                                swap2ndMergePositionY = startPositionY;
                            } else {
                                motionPath = new ArrayList<>();
                                alertAnimationCounter = ALERT_TIME;
                            }
                        }
                        if (!motionPath.isEmpty()) {
                            resetCell(startPositionX, startPositionY);
                            status = statusT.MOTION;
                        } else {
                            alertAnimationCounter = ALERT_TIME;
                            motionPath = new ArrayList<>();
                            if (swapSelected) {
                                resetCell(startPositionX, startPositionY);
                                status = statusT.SELECT_START_POSITION;
                                gameBoardArray.set(targetPositionX, targetPositionY, swapPartnerValueAfterMotion);
                            }
                        }
                    }
                }
            }
        }

        if ((status == statusT.SELECT_START_POSITION || status == statusT.SELECT_TARGET_POSITION) && (indexY == height)) {
            // action button pressed
            if (indexX == 0 && undoCounter > 0) {
                undoSelected = false;
                swapSelected = false;
                jumpSelected = false;
                delRowSelected = false;
                delColSelected = false;
                gameBoardArray.unrollBackup();
                undoCounter--;
            }
            if (indexX == 1 && swapCounter > 0) {
                undoSelected = false;
                swapSelected = !swapSelected;
                jumpSelected = false;
                delRowSelected = false;
                delColSelected = false;
            }
            if (indexX == 2 && jumpCounter > 0) {
                undoSelected = false;
                swapSelected = false;
                jumpSelected = !jumpSelected;
                delRowSelected = false;
                delColSelected = false;
            }
            if (indexX == 3 && delRowCounter > 0) {
                if (status == statusT.SELECT_TARGET_POSITION) {
                    resetCell(startPositionX, startPositionY);
                }
                status = statusT.SELECT_START_POSITION;
                undoSelected = false;
                swapSelected = false;
                jumpSelected = false;
                delRowSelected = !delRowSelected;
                delColSelected = false;
            }
            if (indexX == 4 && delColCounter > 0) {
                if (status == statusT.SELECT_TARGET_POSITION) {
                    resetCell(startPositionX, startPositionY);
                }
                status = statusT.SELECT_START_POSITION;
                undoSelected = false;
                swapSelected = false;
                jumpSelected = false;
                delRowSelected = false;
                delColSelected = !delColSelected;
            }
            updateBonusValues();
        }
    }

    private void invertMotionPath() {
        for (Iterator<directionT> iter = motionPath.iterator(); iter.hasNext(); ) {
            directionT dir = iter.next();
            inverseMotionPath.add(0, invertDir(dir));
        }
    }

    private directionT invertDir(directionT dir) {
        if (dir == directionT.LEFT) return directionT.RIGHT;
        if (dir == directionT.RIGHT) return directionT.LEFT;
        if (dir == directionT.UP) return directionT.DOWN;
        else return directionT.UP;
    }

    public void resetCell(int x, int y) {
        rectArray[x][y] = originalSelectedRect;
        selectionPulseDirection = 0;
    }

    public void highlightCell(int x, int y) {
        originalSelectedRect = new Rect(rectArray[x][y]);

        selectionPulseDirection = -1;
        selectionPulseCounter = SELECTION_PULSE_TIME;
        selectionWidth = 0;
    }


    public String getText(int x, int y) {
        int val = gameBoardArray.get(x, y);
        if (val == -1) {
            return "";
        } else if (val < 10) {
            return Integer.toString(1 << val);
        } else if (val < 20) {
            return Integer.toString(1 << (val - 10)) + "k";
        } else if (val < 30) {
            return Integer.toString(1 << (val - 20)) + "M";
        } else if (val < 40) {
            return Integer.toString(1 << (val - 30)) + "G";
        } else if (val < 50) {
            return Integer.toString(1 << (val - 40)) + "T";
        } else if (val < 60) {
            return Integer.toString(1 << (val - 50)) + "E";
        }
        return "";
    }

    public String getScoreText(long val) {
        if (val == -1) {
            return "";
        } else if (val < (1l<<20)) {
            return Long.toString(val);
        } else if (val < (1l<<30)) {
            return Long.toString(val >> 10) + "k";
        } else if (val < (1l<<40)) {
            return Long.toString(val >> 20) + "M";
        } else if (val < (1l<<50)) {
            return Long.toString(val >> 30) + "G";
        } else if (val < (1l<<60)) {
            return Long.toString(val >> 40) + "T";
        }
        return "";

    }

}