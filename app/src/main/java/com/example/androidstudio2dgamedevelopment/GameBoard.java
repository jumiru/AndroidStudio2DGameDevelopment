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

    private static final int RECT_BORDER = 12;
    private static final float TEXT_HEIGHT = 100.0f;
    private static final int STATUS_FIELD_HEIGHT = 500;
    private static final int STATUS_TEXT_SIZE = 80;
    private static final int MOTION_STEPS = 10;
    private static final int GAME_OVER_STEPS = 10;
    private static final int DROP_INS_AFTER_MOTION = 3;
    private static final int ALERT_TIME = 30;
    private static final int MERGE_ANIMATION_TIME = 30;
    private static final int SELECTION_PULSE_TIME = 4;
    private static final int SELECTION_PULSE_WIDTH = 8;
    private static final int TEXT_COLOR_WHITE = 0xffffffff;
    private static final int BONUS_TEXT_SIZE = 50;
    private static final int HIGHLIGHT_COLOR = 0xff00aa33;
    private static final int GAME_OVER_RECT_COLOR = 0xc0000000;
    private static final int GAME_OVER_TEXT_SIZE = 100;
    private static final int LEVEL_ANIMATION_DURATION = 100;
    private static final int BONUS_ANIMATION_DURATION = 50;
    private static final String GAME_OVER_TEXT = "game over!";
    // Bump this value to trigger a one-time highscore reset on next launch
    private static final int HIGHSCORE_RESET_VERSION = 2;

    private static final int MIN_COMBO_SIZE = 4;
    private static final long BASE_COMBO_SCORE = 4L;
    private static final long BASE_LEVEL_SCORE = 100L;
    private static final long BASE_BONUS_STEP = 100L;
    private static final long BONUS_STEP_PER_LEVEL = 50L;

    public enum directionT {
        UP, DOWN, LEFT, RIGHT
    }

    public enum statusT {
        SELECT_START_POSITION, SELECT_TARGET_POSITION,
        MOTION, MERGE, DROP_IN_NEW_CELLS, GAME_OVER
    }

    private statusT status;

    private final Context context;
    private final int height;
    private final int width;
    private final Paint[][] paintArray;
    private final Rect[][] rectArray;
    private final GameBoardArray gameBoardArray;

    // general dimension information
    private int canvasWidth;
    private int canvasHeight;
    private float cellWidth;
    private float cellHeight;
    private float rectWidth;
    private float rectHeight;


    private final SharedPreferences prefs;
    private final Random rand;
    private final int[] colorArray;

    // scoring
    private long score;
    private int level;
    private long highScore;

    private boolean highscoreExceeded;
    private long nextScoreForBonus;
    private long chainScoreProduct;
    private int chainLength;

    private final Paint textPaint;
    private final Paint scoreAndLevelPaint;
    private Paint bonusPaint;
    private Paint highlightPaint;

    private int alertAnimationCounter;

    private String levelText;
    private int levelAnimationCounter;
    private final Paint levelAnimationPaint;

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
    private final Paint motionPaint;
    private String motionText;
    private Rect finalPosition;
    private final Paint redPaint;
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
    private Rect[] mergeRects;
    private float[] mergeIncrementsX;
    private float[] mergeIncrementsY;
    private int numMergeRects;
    private Paint mergePaint;
    private String mergeText;

    // bonus actions
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

    // swap bonus
    private Rect swapMotionRect;
    private final Paint swapMotionPaint;
    private String swapMotionText;
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
    private final List<directionT> inverseMotionPath;
    private int swap2ndMergePositionX;
    private int swap2ndMergePositionY;

    // game over
    private int gameOverAnimationCounter;
    private int gameOverAnimationPhase;
    private Rect gameOverRect;
    private Paint gameOverPaint;
    private Paint gameOverTextPaint;

    // reusable highlight rect to avoid per-frame allocation
    private Rect highlightRect;

    public GameBoard(Context context, SharedPreferences p) {

        this.prefs = p;

        this.width = prefs.getInt("width", 5);
        this.height = prefs.getInt("height", 7);

        // One-time highscore reset: bump HIGHSCORE_RESET_VERSION to reset again in the future
        int savedResetVersion = prefs.getInt("highscore_reset_version", 0);
        if (savedResetVersion < HIGHSCORE_RESET_VERSION) {
            SharedPreferences.Editor resetEditor = prefs.edit();
            resetEditor.putLong("highscore", 0);
            resetEditor.putInt("highscore_reset_version", HIGHSCORE_RESET_VERSION);
            resetEditor.apply();
        }

        this.highScore = prefs.getLong("highscore", 0);


        updateBonusValues();

        this.context = context;

        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR_WHITE);
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
        levelAnimationPaint.setColor(TEXT_COLOR_WHITE);

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
            colorArray[i] = getColor(r, g, b);
        }
        colorArray[5] = getColor(0x80, 0x10, 0x80);
        gameInit();

    }


    private void gameInit() {
        gameBoardArray.clear();
        gameBoardArray.initCells(4);
        status = statusT.SELECT_START_POSITION;
        startMotion = true;
        score = 0;
        chainScoreProduct = 0L;
        chainLength = 0;

        nextScoreForBonus = getBonusStepForLevel(1);

        highscoreExceeded = false;


        undoCounter = 0;
        swapCounter = 0;
        jumpCounter = 1;
        delRowCounter = 0;
        delColCounter = 0;


        level = 1;
        levelText = "Level: " + level;
        updateBonusValues();
    }

    private int getColor(int x, int y) {
        int val = gameBoardArray.get(x, y);
        if (val == -1) {
            return getColor(0x90, 0x90, 0x90);
        }
        if (val < 40) {
            return colorArray[val];
        } else {
            return getColor(0xff - 63 * val, 0x90 + 71 * val, 43 * val);
        }
    }

    private static int getColor(int R, int G, int B) {
        return 0xff000000 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
    }


    public void draw(Canvas canvas) {

        // first time initializations
        if (canvasWidth == 0) {
            canvasWidth = canvas.getWidth();
            canvasHeight = canvas.getHeight();
            cellWidth  = (float) canvasWidth / width;
            cellHeight = (float) (canvasHeight - STATUS_FIELD_HEIGHT) / height;
            rectWidth  = cellWidth  - 2.0f * RECT_BORDER;
            rectHeight = cellHeight - 2.0f * RECT_BORDER;
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
        canvas.drawText(levelText, getApproxXToCenterText(levelText, levelAnimationPaint, canvasWidth), canvasHeight / 2.0f, levelAnimationPaint);
    }

    private void drawScoreAndLevelInfo(Canvas canvas) {
        String scoreText     = "score: " + getScoreText(score);
        String highScoreText = "best:  " + getScoreText(highScore);
        canvas.drawText(levelText,     20, canvasHeight - STATUS_TEXT_SIZE,             scoreAndLevelPaint);
        canvas.drawText(highScoreText, 20, canvasHeight - 3 * (STATUS_TEXT_SIZE - 20),  scoreAndLevelPaint);
        canvas.drawText(scoreText,     20, canvasHeight - 5 * (STATUS_TEXT_SIZE - 20),  scoreAndLevelPaint);
    }

    private void drawBonusInformation(Canvas canvas) {
        // draw bonus icons
        undo.draw(canvas);
        jump.draw(canvas);
        swap.draw(canvas);
        delRow.draw(canvas);
        delCol.draw(canvas);

        int offset = getApproxXToCenterText(undoText, bonusPaint, (int) cellWidth);
        canvas.drawText(undoText, offset, height * cellHeight + bonusIconWidth + RECT_BORDER + bonusPaint.getTextSize(), bonusPaint);

        offset = getApproxXToCenterText(swapText, bonusPaint, (int) cellWidth);
        canvas.drawText(swapText, cellWidth + offset, height * cellHeight + bonusIconWidth + RECT_BORDER + bonusPaint.getTextSize(), bonusPaint);

        offset = getApproxXToCenterText(jumpText, bonusPaint, (int) cellWidth);
        canvas.drawText(jumpText, 2 * cellWidth + offset, height * cellHeight + bonusIconWidth + RECT_BORDER + bonusPaint.getTextSize(), bonusPaint);

        offset = getApproxXToCenterText(delRowText, bonusPaint, (int) cellWidth);
        canvas.drawText(delRowText, 3 * cellWidth + offset, height * cellHeight + bonusIconWidth + RECT_BORDER + bonusPaint.getTextSize(), bonusPaint);

        offset = getApproxXToCenterText(delColText, bonusPaint, (int) cellWidth);
        canvas.drawText(delColText, 4 * cellWidth + offset, height * cellHeight + bonusIconWidth + RECT_BORDER + bonusPaint.getTextSize(), bonusPaint);
    }

    private void highlightBonusSelection(Rect bonusRect, Canvas canvas) {
        // Reuse cached rect to avoid per-frame allocation
        highlightRect.left   = bonusRect.left   - RECT_BORDER;
        highlightRect.top    = bonusRect.top     - RECT_BORDER;
        highlightRect.right  = bonusRect.right   + RECT_BORDER;
        highlightRect.bottom = bonusRect.bottom  + RECT_BORDER;
        canvas.drawRect(highlightRect, highlightPaint);
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
                    // color already set above, no need to set again
                }
            } else {
                gameOverAnimationCounter += GAME_OVER_STEPS;
                if (gameOverAnimationCounter > canvasWidth) {
                    gameOverAnimationCounter = -((int) gameOverTextPaint.measureText(GAME_OVER_TEXT));
                }
            }
            canvas.drawRect(gameOverRect, gameOverPaint);
            canvas.drawText(GAME_OVER_TEXT, gameOverAnimationCounter, getTextPosY(gameOverRect, gameOverTextPaint), gameOverTextPaint);
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
        int red;
        if (alertAnimationCounter > ALERT_TIME / 2) {
            red = ((ALERT_TIME - alertAnimationCounter) + 1) * (150) / (ALERT_TIME / 2);
        } else {
            red = alertAnimationCounter * (150) / (ALERT_TIME / 2);
        }
        redPaint.setColor(getColor(red, 0, 0));

        canvas.drawRect(0, 0, canvasWidth - 1, canvasHeight - STATUS_FIELD_HEIGHT, redPaint);
    }

    private void initBonusIcons() {
        int bonusIconBorder = 6 * RECT_BORDER;
        bonusIconWidth = (int) (cellWidth - 2 * bonusIconBorder);
        int top = (int) (height * cellHeight + RECT_BORDER);

        int left = bonusIconBorder;
        undoRect = new Rect(left, top, left + bonusIconWidth, top + bonusIconWidth);

        left = (int) (cellWidth + bonusIconBorder);
        swapRect = new Rect(left, top, left + bonusIconWidth, top + bonusIconWidth);

        left = (int) (2 * cellWidth + bonusIconBorder);
        jumpRect = new Rect(left, top, left + bonusIconWidth, top + bonusIconWidth);

        left = (int) (3 * cellWidth + bonusIconBorder);
        delRowRect = new Rect(left, top + 2 * RECT_BORDER, left + bonusIconWidth, top + bonusIconWidth - 2 * RECT_BORDER);

        left = (int) (4 * cellWidth + bonusIconBorder);
        delColRect = new Rect(left + 2 * RECT_BORDER, top, left + bonusIconWidth - 2 * RECT_BORDER, top + bonusIconWidth);

        undo = ContextCompat.getDrawable(context, R.drawable.undo_circle_icon);
        if (undo != null) undo.setBounds(undoRect);

        swap = ContextCompat.getDrawable(context, R.drawable.swap_icon);
        if (swap != null) swap.setBounds(swapRect);

        jump = ContextCompat.getDrawable(context, R.drawable.jump_icon);
        if (jump != null) jump.setBounds(jumpRect);

        delRow = ContextCompat.getDrawable(context, R.drawable.cross_icon);
        if (delRow != null) delRow.setBounds(delRowRect);

        delCol = ContextCompat.getDrawable(context, R.drawable.cross_icon);
        if (delCol != null) delCol.setBounds(delColRect);

        bonusPaint = new Paint();
        bonusPaint.setColor(TEXT_COLOR_WHITE);
        bonusPaint.setTextSize(BONUS_TEXT_SIZE);

        highlightPaint = new Paint();
        highlightPaint.setColor(HIGHLIGHT_COLOR);

        highlightRect = new Rect();
    }

    private void drawCell(Canvas canvas, Rect rect, Paint paint, String text) {
        canvas.drawRect(rect, paint);
        textPaint.setTextSize(getTextSize(text));
        canvas.drawText(text, getTextPosX(rect, text, textPaint), getTextPosY(rect, textPaint), textPaint);
    }

    private float getTextSize(String text) {
        return TEXT_HEIGHT * (100.0f - (text.length() - 1) * 10.0f) / 100.0f;
    }


    private float getTextPosY(Rect rect, Paint p) {
        float posY = rect.bottom;
        return posY - ((rect.bottom - rect.top) / 2.0f - p.getTextSize() / 2) - RECT_BORDER;
    }

    private float getTextPosX(Rect rect, String text, Paint paint) {
        float posX = rect.left;
        float thisCellWidth = rect.right - rect.left;
        int offset = getApproxXToCenterText(text, paint, (int) thisCellWidth);
        return posX + offset;
    }

    public static int getApproxXToCenterText(String text, Paint p, int widthToFitStringInto) {
        float textWidth = p.measureText(text);
        return (int) ((widthToFitStringInto - textWidth) / 2f);
    }

    private Rect getRect(int x, int y) {
        return new Rect(
                x * (int) cellWidth + RECT_BORDER,
                y * (int) cellHeight + RECT_BORDER,
                (x + 1) * (int) cellWidth - RECT_BORDER,
                (y + 1) * (int) cellHeight - RECT_BORDER
        );
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
            // handled by onTouchEvent
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
            if (mergeGroup.size() >= MIN_COMBO_SIZE) {
                startMergeAnimation();
            } else {
                if (swapSelected) {
                    swapSelected = false;
                    targetPositionX = swap2ndMergePositionX;
                    targetPositionY = swap2ndMergePositionY;
                    status = statusT.MERGE; // do merge for the swap target position too
                } else {
                    finalizeChainScoreIfNeeded();
                    status = statusT.DROP_IN_NEW_CELLS;
                }
            }
            int freeCells = gameBoardArray.getNumFreeCells();
            if (freeCells == 0) {
                finalizeChainScoreIfNeeded();
                status = statusT.GAME_OVER;
            }
        } else {
            mergeAnimationStep--;
            for (int i = 0; i < numMergeRects; i++) {
                mergeRects[i].left   = (int) (((float) mergeRects[i].left)   + mergeIncrementsX[i]);
                mergeRects[i].right  = (int) (((float) mergeRects[i].right)  + mergeIncrementsX[i]);
                mergeRects[i].top    = (int) (((float) mergeRects[i].top)    + mergeIncrementsY[i]);
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
                    dropInCount = DROP_INS_AFTER_MOTION;
                }
                status = statusT.MERGE;
                startMotion = true;
            }
        } else {
            offsetX = offsetX + motionIncrementX;
            offsetY = offsetY + motionIncrementY;
            motionRect.left   = (int) (startFloatPosX + offsetX);
            motionRect.right  = (int) (startFloatPosX + offsetX + rectWidth);
            motionRect.top    = (int) (startFloatPosY + offsetY);
            motionRect.bottom = (int) (startFloatPosY + offsetY + rectHeight);
            if (swapSelected) {
                swapOffsetX = swapOffsetX + swapMotionIncrementX;
                swapOffsetY = swapOffsetY + swapMotionIncrementY;
                swapMotionRect.left   = (int) (swapStartFloatPosX + swapOffsetX);
                swapMotionRect.right  = (int) (swapStartFloatPosX + swapOffsetX + rectWidth);
                swapMotionRect.top    = (int) (swapStartFloatPosY + swapOffsetY);
                swapMotionRect.bottom = (int) (swapStartFloatPosY + swapOffsetY + rectHeight);
            }
        }
    }

    private void handleNextSwapMotionStep(directionT ignoredDir) {
        directionT dir = inverseMotionPath.get(inverseMotionPath.size() - 1);
        inverseMotionPath.remove(inverseMotionPath.size() - 1);
        if (dir == directionT.LEFT) {
            applyCommonSwapMotionSettings(--startSwapPositionX, startSwapPositionY);
        } else if (dir == directionT.DOWN) {
            applyCommonSwapMotionSettings(startSwapPositionX, ++startSwapPositionY);
        } else if (dir == directionT.RIGHT) {
            applyCommonSwapMotionSettings(++startSwapPositionX, startSwapPositionY);
        } else if (dir == directionT.UP) {
            applyCommonSwapMotionSettings(startSwapPositionX, --startSwapPositionY);
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
            motionRect.left   = (int) (startFloatPosX + offsetX);
            motionRect.right  = (int) (startFloatPosX + offsetX + rectWidth);
            motionRect.top    = (int) (startFloatPosY + offsetY);
            motionRect.bottom = (int) (startFloatPosY + offsetY + rectHeight);

        }
    }

    private void animateSelectionPulse() {
        selectionPulseCounter--;
        if (selectionPulseCounter == 0) {
            rectArray[startPositionX][startPositionY].left   += selectionPulseDirection;
            rectArray[startPositionX][startPositionY].right  -= selectionPulseDirection;
            rectArray[startPositionX][startPositionY].top    += selectionPulseDirection;
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

        dropInAdderX = cellWidth / 2.0f - RECT_BORDER;
        dropInAdderY = cellHeight / 2.0f - RECT_BORDER;

        dropInRect = getRect(targetPositionX, targetPositionY);
        dropInRect.left   = rectArray[targetPositionX][targetPositionY].left   + (int) dropInAdderX;
        dropInRect.right  = rectArray[targetPositionX][targetPositionY].right  - (int) dropInAdderX; // fixed: was dropInAdderY
        dropInRect.top    = rectArray[targetPositionX][targetPositionY].top    + (int) dropInAdderY;
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
            dropInRect.left   = rectArray[targetPositionX][targetPositionY].left   + adderX;
            dropInRect.right  = rectArray[targetPositionX][targetPositionY].right  - adderX;
            dropInRect.top    = rectArray[targetPositionX][targetPositionY].top    + adderY;
            dropInRect.bottom = rectArray[targetPositionX][targetPositionY].bottom - adderY;


        } else {
            gameBoardArray.set(targetPositionX, targetPositionY, dropInValue);
            dropInAnimationRunning = false;
            status = statusT.MERGE;
        }
    }

    private boolean reachedJumpTargetPosition() {
        float leftBorderLow  = finalPosition.left - Math.abs(motionIncrementX);
        float leftBorderHigh = finalPosition.left + Math.abs(motionIncrementX);
        float topBorderLow   = finalPosition.top  - Math.abs(motionIncrementY);
        float topBorderHigh  = finalPosition.top  + Math.abs(motionIncrementY);

        return (leftBorderLow <= motionRect.left) && (motionRect.left <= leftBorderHigh) &&
               (topBorderLow  <= motionRect.top)  && (motionRect.top  <= topBorderHigh);
    }

    private void startGameOverAnimation() {
        gameOverAnimationPhase = 1;
        gameOverAnimationCounter = 0;
        gameOverRect = new Rect();
        gameOverPaint = new Paint();
        gameOverPaint.setColor(GAME_OVER_RECT_COLOR);
        gameOverTextPaint = new Paint();
        gameOverTextPaint.setTextSize(GAME_OVER_TEXT_SIZE);
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
            mergeIncrementsY[i] = ((float) (targetY - mergeRects[i].top))  / ((float) MERGE_ANIMATION_TIME);
            i++;
        }

        mergeAnimationStep = MERGE_ANIMATION_TIME;
        gameBoardArray.set(targetPositionX, targetPositionY, gameBoardArrayValueAfterMotion + 2);
    }

     private void finishMergeAnimation() {
         if (dropInCount == DROP_INS_AFTER_MOTION) {
             dropInCount = 0;
         }

         long comboScore = calculateComboScore(mergeGroup.size());
         if (chainLength == 0) {
             chainScoreProduct = comboScore;
         } else {
             // Multiply the scores for chain combos
             chainScoreProduct = safeMultiply(chainScoreProduct, comboScore);
         }
         chainLength++;
     }

    private void startBonusAnimation() {
        animateBonusCounter = BONUS_ANIMATION_DURATION;

        bonusWinRect = new Rect(canvasWidth / 4, canvasWidth / 4, canvasWidth * 3 / 4, canvasWidth * 3 / 4);

        int drawableId;
        switch (bonusWin) {
            case 0:  drawableId = R.drawable.undo_circle_icon; break;
            case 1:  drawableId = R.drawable.swap_icon;        break;
            case 2:  drawableId = R.drawable.jump_icon;        break;
            case 3:
            case 4:
            default: drawableId = R.drawable.cross_icon;       break;
        }
        bonusWinDrawable = ContextCompat.getDrawable(context, drawableId);
        if (bonusWinDrawable != null) {
            bonusWinDrawable.setBounds(bonusWinRect);
        }
    }

    private void animateBonusWin() {
        animateBonusCounter--;
        bonusWinRect.left   -= 2;
        bonusWinRect.right  += 2;
        bonusWinRect.top    -= 2;
        bonusWinRect.bottom += 2;

        if (animateBonusCounter == 0) {
            finishBonusAnimation();
        }
    }

    private void finishBonusAnimation() {
        updateBonusValues();
    }

    private void startLevelAnimation() {
        levelAnimationCounter = LEVEL_ANIMATION_DURATION;
        levelText = "Level: " + level;
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
        undoText   = Integer.toString(undoCounter);
        swapText   = Integer.toString(swapCounter);
        jumpText   = Integer.toString(jumpCounter);
        delRowText = Integer.toString(delRowCounter);
        delColText = Integer.toString(delColCounter);
    }

    private long calculateComboScore(int comboSize) {
        if (comboSize < MIN_COMBO_SIZE) {
            return 0L;
        }
        int shift = comboSize - MIN_COMBO_SIZE;
        if (shift >= 60) {
            return Long.MAX_VALUE;
        }
        return BASE_COMBO_SCORE << shift;
    }

    private long safeAdd(long a, long b) {
        if (a > Long.MAX_VALUE - b) {
            return Long.MAX_VALUE;
        }
        return a + b;
    }

    private long safeMultiply(long a, long b) {
        if (a == 0L || b == 0L) {
            return 0L;
        }
        if (a > Long.MAX_VALUE / b) {
            return Long.MAX_VALUE;
        }
        return a * b;
    }

    private long getLevelThreshold(int nextLevel) {
        return BASE_LEVEL_SCORE * nextLevel * (long) nextLevel;
    }

    private long getBonusStepForLevel(int currentLevel) {
        return BASE_BONUS_STEP + BONUS_STEP_PER_LEVEL * Math.max(0, currentLevel - 1);
    }

    private void awardRandomBonus() {
        bonusWin = rand.nextInt(5);
        switch (bonusWin) {
            case 0:
                undoCounter += 2;
                break;
            case 1:
                swapCounter += 2;
                break;
            case 2:
                jumpCounter += 2;
                break;
            case 3:
                delRowCounter += 2;
                break;
            case 4:
                delColCounter += 2;
                break;
            default:
                break;
        }
        startBonusAnimation();
    }

    private void applyProgressionAfterScoreChange() {
        boolean leveledUp = false;
        while (score >= getLevelThreshold(level + 1)) {
            level++;
            leveledUp = true;
        }
        if (leveledUp) {
            startLevelAnimation();
        }

        if (score > highScore) {
            highScore = score;
            highscoreExceeded = true;
        }

        while (score >= nextScoreForBonus) {
            awardRandomBonus();
            nextScoreForBonus = safeAdd(nextScoreForBonus, getBonusStepForLevel(level));
        }
    }

    private void finalizeChainScoreIfNeeded() {
        if (chainLength == 0) {
            return;
        }

        score = safeAdd(score, chainScoreProduct);
        chainScoreProduct = 0L;
        chainLength = 0;
        applyProgressionAfterScoreChange();
    }

    private Set<Coord> merge(int x, int y) {
        return gameBoardArray.findMergeGroup(x, y);
    }

    private boolean reachedPathPosition() {
        return (motionDir == directionT.LEFT  && motionRect.left   <= finalPosition.left)   ||
               (motionDir == directionT.RIGHT && motionRect.left   >= finalPosition.left)   ||
               (motionDir == directionT.UP    && motionRect.bottom <= finalPosition.bottom) ||
               (motionDir == directionT.DOWN  && motionRect.bottom >= finalPosition.bottom);
    }


    private void moveDown(int x, int y) {
        applyCommonMotionSettings(x, y, x, y + 1);
        motionDir = directionT.DOWN;
    }

    private void moveUp(int x, int y) {
        applyCommonMotionSettings(x, y, x, y - 1);
        motionDir = directionT.UP;
    }

    private void moveRight(int x, int y) {
        applyCommonMotionSettings(x, y, x + 1, y);
        motionDir = directionT.RIGHT;
    }

    private void moveLeft(int x, int y) {
        applyCommonMotionSettings(x, y, x - 1, y);
        motionDir = directionT.LEFT;
    }

    private void applyCommonMotionSettings(int x, int y, int newX, int newY) {
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
        motionIncrementY = (float) (finalPosition.top  - motionRect.top)  / (float) MOTION_STEPS;
        offsetX = 0;
        offsetY = 0;
        startFloatPosX = motionRect.left;
        startFloatPosY = motionRect.top;
    }

    private void applyCommonSwapMotionSettings(int newX, int newY) {
        Rect swapFinalPosition = rectArray[newX][newY];
        xAfterSwapMotion = newX;
        yAfterSwapMotion = newY;
        swapMotionIncrementX = (float) (swapFinalPosition.left - swapMotionRect.left) / (float) MOTION_STEPS;
        swapMotionIncrementY = (float) (swapFinalPosition.top  - swapMotionRect.top)  / (float) MOTION_STEPS;
        swapOffsetX = 0;
        swapOffsetY = 0;
        swapStartFloatPosX = swapMotionRect.left;
        swapStartFloatPosY = swapMotionRect.top;
    }

    public void onTouchEvent(int x, int y) {

        int indexX = (int) (x / cellWidth);
        int indexY = (int) (y / cellHeight);

        // Bounds checking to prevent array index out of bounds
        if (indexX < 0 || indexX >= width) {
            return;
        }

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
                        for (int col = 0; col < width; col++) {
                            gameBoardArray.set(col, indexY, -1);
                        }
                        delRowSelected = false;
                        delRowCounter--;
                        updateBonusValues();
                        if (gameBoardArray.getNumFreeCells() == width * height) {
                            status = statusT.DROP_IN_NEW_CELLS;
                            dropInCount = DROP_INS_AFTER_MOTION;
                        } else {
                            status = statusT.SELECT_START_POSITION;
                        }
                    } else if (delColSelected) {
                        resetCell(startPositionX, startPositionY);
                        for (int row = 0; row < height; row++) {
                            gameBoardArray.set(indexX, row, -1);
                        }
                        delColSelected = false;
                        delColCounter--;
                        updateBonusValues();
                        if (gameBoardArray.getNumFreeCells() == width * height) {
                            status = statusT.DROP_IN_NEW_CELLS;
                            dropInCount = DROP_INS_AFTER_MOTION;
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
                deselectAllBonuses();
                gameBoardArray.unrollBackup();
                undoCounter--;
            }
            if (indexX == 1 && swapCounter > 0) {
                boolean prev = swapSelected;
                deselectAllBonuses();
                swapSelected = !prev;
            }
            if (indexX == 2 && jumpCounter > 0) {
                boolean prev = jumpSelected;
                deselectAllBonuses();
                jumpSelected = !prev;
            }
            if (indexX == 3 && delRowCounter > 0) {
                if (status == statusT.SELECT_TARGET_POSITION) {
                    resetCell(startPositionX, startPositionY);
                }
                status = statusT.SELECT_START_POSITION;
                boolean prev = delRowSelected;
                deselectAllBonuses();
                delRowSelected = !prev;
            }
            if (indexX == 4 && delColCounter > 0) {
                if (status == statusT.SELECT_TARGET_POSITION) {
                    resetCell(startPositionX, startPositionY);
                }
                status = statusT.SELECT_START_POSITION;
                boolean prev = delColSelected;
                deselectAllBonuses();
                delColSelected = !prev;
            }
            updateBonusValues();
        }
    }

    /** Deselects all bonus actions at once. */
    private void deselectAllBonuses() {
        undoSelected   = false;
        swapSelected   = false;
        jumpSelected   = false;
        delRowSelected = false;
        delColSelected = false;
    }

    private void invertMotionPath() {
        inverseMotionPath.clear(); // prevent stale entries from aborted swaps
        for (directionT dir : motionPath) {
            inverseMotionPath.add(0, invertDir(dir));
        }
    }

    private directionT invertDir(directionT dir) {
        switch (dir) {
            case LEFT:  return directionT.RIGHT;
            case RIGHT: return directionT.LEFT;
            case UP:    return directionT.DOWN;
            case DOWN:  return directionT.UP;
            default:    throw new IllegalArgumentException("Unknown direction: " + dir);
        }
    }

    private void resetCell(int x, int y) {
        rectArray[x][y] = originalSelectedRect;
        selectionPulseDirection = 0;
    }

    private void highlightCell(int x, int y) {
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
            return (1 << (val - 10)) + "k";
        } else if (val < 30) {
            return (1 << (val - 20)) + "M";
        } else if (val < 40) {
            return (1 << (val - 30)) + "G";
        } else if (val < 50) {
            return (1 << (val - 40)) + "T";
        } else if (val < 60) {
            return (1 << (val - 50)) + "E";
        }
        return "";
    }

    public String getScoreText(long val) {
        if (val < 0) {
            return "";
        } else if (val < (1L << 20)) {
            return Long.toString(val);
        } else if (val < (1L << 30)) {
            return (val >> 10) + "k";
        } else if (val < (1L << 40)) {
            return (val >> 20) + "M";
        } else if (val < (1L << 50)) {
            return (val >> 30) + "G";
        } else if (val < (1L << 60)) {
            return (val >> 40) + "T";
        }
        return "";

    }

}
