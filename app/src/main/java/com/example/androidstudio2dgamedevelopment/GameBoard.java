package com.example.androidstudio2dgamedevelopment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
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
    private static final int STATUS_FIELD_HEIGHT = 620;
    private static final int STATUS_TEXT_SIZE = 52;
    private static final int STATUS_LABEL_TEXT_SIZE = 28;
    private static final int MOTION_STEPS = 10;
    private static final int GAME_OVER_STEPS = 10;
    private static final int DROP_INS_AFTER_MOTION = 3;
    private static final int ALERT_TIME = 30;
    private static final int MERGE_ANIMATION_TIME = 30;
    private static final int SELECTION_PULSE_TIME = 4;
    private static final int SELECTION_PULSE_WIDTH = 8;
    private static final int TEXT_COLOR_WHITE = 0xffffffff;
    private static final int BONUS_TEXT_SIZE = 34;
    private static final int HIGHLIGHT_COLOR = 0xff00aa33;
    private static final int GAME_OVER_RECT_COLOR = 0xc0000000;
    private static final int GAME_OVER_TEXT_SIZE = 100;
    private static final int LEVEL_ANIMATION_DURATION = 100;
    private static final int BONUS_ANIMATION_DURATION = 50;
    private static final int DISSOLVE_ANIMATION_DURATION = 14;
    private static final int LINE_DISSOLVE_ANIMATION_DURATION = 18;
    private static final int SHIFT_BONUS_ANIMATION_STEPS = 10;
    private static final int SHIFT_BLUR_TRAIL_COUNT = 3;
    private static final int SHIFT_BLUR_BASE_ALPHA = 92;
    private static final int SHIFT_EDGE_DISSOLVE_MIN_ALPHA = 28;
    private static final float SHIFT_EDGE_DISSOLVE_INSET_RATIO = 0.34f;
    private static final long SECRET_CODE_TAP_TIMEOUT_MS = 1800L;
    private static final int SECRET_CODE_SCORE = 0;
    private static final int SECRET_CODE_LEVEL = 1;
    private static final int SECRET_CODE_BEST = 2;
    // Hidden test code: SCORE -> LEVEL -> BEST -> SCORE
    private static final int[] SECRET_CODE_SEQUENCE = {
            SECRET_CODE_SCORE,
            SECRET_CODE_LEVEL,
            SECRET_CODE_BEST,
            SECRET_CODE_SCORE
    };
    private static final int HUD_CORNER_RADIUS = 26;
    private static final int HUD_PANEL_COLOR = 0xff16213a;
    private static final int HUD_PANEL_BORDER_COLOR = 0xff32518b;
    private static final int HUD_LABEL_TEXT_COLOR = 0xffcfd7ee;
    private static final int HUD_SCORE_ACCENT_COLOR = 0xfff5bf4f;
    private static final int HUD_LEVEL_ACCENT_COLOR = 0xff63d88e;
    private static final int HUD_BEST_ACCENT_COLOR = 0xff63b8ff;
    private static final int HUD_BEST_NEW_RECORD_ACCENT_COLOR = 0xffff8bd9;
    private static final int BONUS_ICON_BG_DEFAULT_COLOR = 0xff000000;
    private static final int BONUS_SLOT_CARD_COLOR = 0xff111a2d;
    private static final int BONUS_SLOT_BORDER_COLOR = 0xff2e466f;
    private static final String GAME_OVER_TEXT = "game over!";
    private static final String PREF_KEY_HIGHSCORE = "highscore";
    private static final String PREF_KEY_HIGHSCORE_RESET_VERSION = "highscore_reset_version";
    private static final String PREF_KEY_HIGHSCORE_LOCKED_BY_CHEAT = "highscore_locked_by_cheat";
    // Bump this value to trigger a one-time highscore reset on next launch
    private static final int HIGHSCORE_RESET_VERSION = 2;

    private static final int MIN_COMBO_SIZE = 4;
    private static final long BASE_COMBO_SCORE = 4L;
    private static final long BASE_LEVEL_SCORE = 100L;
    private static final long BASE_BONUS_STEP = 100L;
    private static final long BONUS_STEP_PER_LEVEL = 50L;
    private static final long BUY_COST_UNDO = 600L;
    private static final long BUY_COST_SWAP = 1200L;
    private static final long BUY_COST_JUMP = 1400L;
    private static final long BUY_COST_DISSOLVE = 1000L;
    private static final long BUY_COST_DEL_ROW = 1800L;
    private static final long BUY_COST_DEL_COL = 1800L;
    private static final long BUY_COST_SHIFT_ROW = 1300L;
    private static final long BUY_COST_SHIFT_COL = 1300L;
    private static final int BONUS_AWARD_AMOUNT = 2;
    // Bonus order must stay in sync with counters, icons, buy buttons and animation handling.
    private static final long[] BONUS_BUY_COSTS = {
            BUY_COST_UNDO,
            BUY_COST_SWAP,
            BUY_COST_JUMP,
            BUY_COST_DISSOLVE,
            BUY_COST_DEL_ROW,
            BUY_COST_DEL_COL,
            BUY_COST_SHIFT_ROW,
            BUY_COST_SHIFT_COL
    };
    private static final int[] BONUS_DRAWABLE_IDS = {
            R.drawable.undo_circle_icon,
            R.drawable.swap_icon,
            R.drawable.jump_icon,
            R.drawable.delete_block_icon,
            R.drawable.delete_row_icon,
            R.drawable.delete_col_icon,
            R.drawable.shift_row_icon,
            R.drawable.shift_col_icon
    };
    private static final int[] BONUS_ACCENT_COLORS = {
            0xff66d8ff,
            0xff91a8ff,
            0xff9be27c,
            0xffff9f6b,
            0xffff7f7f,
            0xffff6aa6,
            0xffbd8bff,
            0xfff7c65f
    };

    public enum directionT {
        UP, DOWN, LEFT, RIGHT
    }

    public enum statusT {
        SELECT_START_POSITION, SELECT_TARGET_POSITION,
        MOTION, MERGE, DROP_IN_NEW_CELLS, BONUS_SHIFT_ANIMATION, GAME_OVER
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
    private boolean highscoreLockedByCheat;

    private boolean highscoreExceeded;
    private long nextScoreForBonus;
    private long chainScoreProduct;
    private int chainLength;

    private final Paint textPaint;
    private final Paint scoreAndLevelPaint;
    private final Paint statusLabelPaint;
    private final Paint statusPanelPaint;
    private final Paint statusPanelBorderPaint;
    private final Paint statusPanelAccentPaint;
    private Paint bonusPaint;
    private Paint highlightPaint;
    private final RectF statusPanelRect;

    // dissolve bonus animation
    private boolean dissolveAnimationRunning;
    private int dissolveAnimationCounter;
    private int dissolveTargetX;
    private int dissolveTargetY;
    private Rect dissolveAnimationRect;
    private Paint dissolveAnimationPaint;

    // line dissolve bonus animation (delete row/column)
    private boolean lineDissolveAnimationRunning;
    private boolean lineDissolveIsRow;
    private int lineDissolveLineIndex;
    private int lineDissolveAnimationCounter;
    private Rect[] lineDissolveRects;
    private Paint[] lineDissolvePaints;
    private String[] lineDissolveTexts;
    private Rect lineDissolveDrawRect;

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

    // line shift bonus animation
    private boolean shiftBonusAnimationIsRow;
    private int shiftBonusLineIndex;
    private int shiftBonusAnimationCounter;
    private float shiftBonusOffsetX;
    private float shiftBonusOffsetY;
    private float shiftBonusStepX;
    private float shiftBonusStepY;
    private int[] shiftBonusValues;
    private Rect[] shiftBonusRects;
    private Paint[] shiftBonusPaints;
    private String[] shiftBonusTexts;
    private Rect shiftBonusDrawRect;
    private final Paint shiftEdgeDissolvePaint;
    private final Paint shiftEdgeDissolveStrokePaint;
    private final Paint shiftEdgeDissolveTextPaint;
    private Rect statusScoreRect;
    private Rect statusLevelRect;
    private Rect statusBestRect;
    private int secretCodeProgress;
    private long secretCodeLastTapMs;

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
    private int bonusSlotWidth;
    private int bonusSlotHeight;
    private int buyButtonHeight;

    private int animateBonusCounter;
    private int bonusWin;
    private Rect bonusWinRect;
    private Drawable bonusWinDrawable;

    private Rect undoRect;
    private Rect swapRect;
    private Rect jumpRect;
    private Rect dissolveRect;
    private Rect delRowRect;
    private Rect delColRect;
    private Rect shiftRowRect;
    private Rect shiftColRect;
    private Rect undoSlotRect;
    private Rect swapSlotRect;
    private Rect jumpSlotRect;
    private Rect dissolveSlotRect;
    private Rect delRowSlotRect;
    private Rect delColSlotRect;
    private Rect shiftRowSlotRect;
    private Rect shiftColSlotRect;
    private Rect undoBuyRect;
    private Rect swapBuyRect;
    private Rect jumpBuyRect;
    private Rect dissolveBuyRect;
    private Rect delRowBuyRect;
    private Rect delColBuyRect;
    private Rect shiftRowBuyRect;
    private Rect shiftColBuyRect;
    private Drawable undo;
    private Drawable jump;
    private Drawable swap;
    private Drawable dissolve;
    private Drawable delRow;
    private Drawable delCol;
    private Drawable shiftRow;
    private Drawable shiftCol;
    private int undoCounter;
    private int swapCounter;
    private int jumpCounter;
    private int dissolveCounter;
    private int delRowCounter;
    private int delColCounter;
    private int shiftRowCounter;
    private int shiftColCounter;

    private boolean undoSelected;
    private boolean swapSelected;
    private boolean jumpSelected;
    private boolean dissolveSelected;
    private boolean delRowSelected;
    private boolean delColSelected;
    private boolean shiftRowSelected;
    private boolean shiftColSelected;

    private String undoText;
    private String swapText;
    private String jumpText;
    private String dissolveText;
    private String delRowText;
    private String delColText;
    private String shiftRowText;
    private String shiftColText;
    private Paint buyPaint;
    private Paint buyBoxPaint;
    private Paint bonusSlotCardPaint;
    private Paint bonusSlotBorderPaint;
    private Paint bonusIconBgPaint;
    private Paint bonusIconSelectedBorderPaint;
    private final RectF bonusSlotRect;

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
        int savedResetVersion = prefs.getInt(PREF_KEY_HIGHSCORE_RESET_VERSION, 0);
        if (savedResetVersion < HIGHSCORE_RESET_VERSION) {
            SharedPreferences.Editor resetEditor = prefs.edit();
            resetEditor.putLong(PREF_KEY_HIGHSCORE, 0);
            resetEditor.putInt(PREF_KEY_HIGHSCORE_RESET_VERSION, HIGHSCORE_RESET_VERSION);
            resetEditor.apply();
        }

        this.highScore = prefs.getLong(PREF_KEY_HIGHSCORE, 0);
        this.highscoreLockedByCheat = prefs.getBoolean(PREF_KEY_HIGHSCORE_LOCKED_BY_CHEAT, false);


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
        scoreAndLevelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scoreAndLevelPaint.setColor(TEXT_COLOR_WHITE);
        scoreAndLevelPaint.setTextSize(STATUS_TEXT_SIZE);
        scoreAndLevelPaint.setFakeBoldText(true);

        statusLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        statusLabelPaint.setColor(HUD_LABEL_TEXT_COLOR);
        statusLabelPaint.setTextSize(STATUS_LABEL_TEXT_SIZE);

        statusPanelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        statusPanelPaint.setColor(HUD_PANEL_COLOR);
        statusPanelPaint.setStyle(Paint.Style.FILL);

        statusPanelBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        statusPanelBorderPaint.setColor(HUD_PANEL_BORDER_COLOR);
        statusPanelBorderPaint.setStyle(Paint.Style.STROKE);
        statusPanelBorderPaint.setStrokeWidth(4.0f);

        statusPanelAccentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        statusPanelAccentPaint.setStyle(Paint.Style.FILL);

        shiftEdgeDissolvePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shiftEdgeDissolvePaint.setStyle(Paint.Style.FILL);

        shiftEdgeDissolveStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shiftEdgeDissolveStrokePaint.setStyle(Paint.Style.STROKE);

        shiftEdgeDissolveTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shiftEdgeDissolveTextPaint.setColor(TEXT_COLOR_WHITE);

        statusPanelRect = new RectF();
        bonusSlotRect = new RectF();

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
        dissolveCounter = 0;
        delRowCounter = 0;
        delColCounter = 0;
        shiftRowCounter = 0;
        shiftColCounter = 0;

        deselectAllBonuses();
        secretCodeProgress = 0;
        secretCodeLastTapMs = 0L;
        shiftBonusValues = null;
        shiftBonusRects = null;
        shiftBonusPaints = null;
        shiftBonusTexts = null;
        lineDissolveAnimationRunning = false;
        lineDissolveRects = null;
        lineDissolvePaints = null;
        lineDissolveTexts = null;


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
        drawShiftBonusAnimation(canvas);

        if (status == statusT.GAME_OVER) {
            drawGameOverAnimation(canvas);
        }

        drawScoreAndLevelInfo(canvas);

        drawBonusInformation(canvas);

        if (dissolveAnimationRunning) {
            drawDissolveAnimation(canvas);
        }
        if (lineDissolveAnimationRunning) {
            drawLineDissolveAnimation(canvas);
        }

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

    private void drawShiftBonusAnimation(Canvas canvas) {
        if (status != statusT.BONUS_SHIFT_ANIMATION || shiftBonusRects == null) {
            return;
        }
        float progress = 1.0f - (float) shiftBonusAnimationCounter / (float) SHIFT_BONUS_ANIMATION_STEPS;
        int outgoingIndex = shiftBonusRects.length - 1;
        for (int i = 0; i < shiftBonusRects.length; i++) {
            if (shiftBonusRects[i] == null || shiftBonusPaints[i] == null) {
                continue;
            }
            int originalAlpha = shiftBonusPaints[i].getAlpha();

            // Draw a short trailing blur behind the moving block.
            for (int trail = SHIFT_BLUR_TRAIL_COUNT; trail >= 1; trail--) {
                float trailFactor = trail * 0.55f;
                shiftBonusDrawRect.left = (int) (shiftBonusRects[i].left + shiftBonusOffsetX - shiftBonusStepX * trailFactor);
                shiftBonusDrawRect.right = (int) (shiftBonusRects[i].right + shiftBonusOffsetX - shiftBonusStepX * trailFactor);
                shiftBonusDrawRect.top = (int) (shiftBonusRects[i].top + shiftBonusOffsetY - shiftBonusStepY * trailFactor);
                shiftBonusDrawRect.bottom = (int) (shiftBonusRects[i].bottom + shiftBonusOffsetY - shiftBonusStepY * trailFactor);
                int blurAlpha = Math.max(16, SHIFT_BLUR_BASE_ALPHA - trail * 22 - (int) (progress * 22.0f));
                shiftBonusPaints[i].setAlpha(blurAlpha);
                canvas.drawRect(shiftBonusDrawRect, shiftBonusPaints[i]);
            }

            shiftBonusPaints[i].setAlpha(originalAlpha);
            shiftBonusDrawRect.left = (int) (shiftBonusRects[i].left + shiftBonusOffsetX);
            shiftBonusDrawRect.right = (int) (shiftBonusRects[i].right + shiftBonusOffsetX);
            shiftBonusDrawRect.top = (int) (shiftBonusRects[i].top + shiftBonusOffsetY);
            shiftBonusDrawRect.bottom = (int) (shiftBonusRects[i].bottom + shiftBonusOffsetY);

            if (i == outgoingIndex) {
                drawShiftEdgeDissolve(canvas, shiftBonusDrawRect, shiftBonusPaints[i], shiftBonusTexts[i], progress);
                continue;
            }
            drawCell(canvas, shiftBonusDrawRect, shiftBonusPaints[i], shiftBonusTexts[i]);
        }
    }

    private void drawShiftEdgeDissolve(Canvas canvas, Rect movingRect, Paint basePaint, String text, float progress) {
        int alpha = Math.max(SHIFT_EDGE_DISSOLVE_MIN_ALPHA, 255 - (int) (progress * 240.0f));
        float inset = progress * Math.min(movingRect.width(), movingRect.height()) * SHIFT_EDGE_DISSOLVE_INSET_RATIO;

        float left = movingRect.left + inset;
        float right = movingRect.right - inset;
        float top = movingRect.top + inset;
        float bottom = movingRect.bottom - inset;

        shiftEdgeDissolvePaint.setColor(basePaint.getColor());
        shiftEdgeDissolvePaint.setAlpha(alpha);
        canvas.drawRoundRect(left, top, right, bottom, 10.0f, 10.0f, shiftEdgeDissolvePaint);

        shiftEdgeDissolveStrokePaint.setColor(TEXT_COLOR_WHITE);
        shiftEdgeDissolveStrokePaint.setStrokeWidth(Math.max(2.0f, (1.0f - progress) * 7.0f));
        shiftEdgeDissolveStrokePaint.setAlpha(Math.max(24, alpha - 60));
        canvas.drawLine(left, top, right, bottom, shiftEdgeDissolveStrokePaint);
        canvas.drawLine(left, bottom, right, top, shiftEdgeDissolveStrokePaint);

        if (text != null && !text.isEmpty()) {
            shiftEdgeDissolveTextPaint.setTextSize(textPaint.getTextSize());
            shiftEdgeDissolveTextPaint.setAlpha(Math.max(18, alpha - 90));
            String drawText = text;
            Rect asRect = new Rect((int) left, (int) top, (int) right, (int) bottom);
            shiftEdgeDissolveTextPaint.setTextSize(getTextSize(drawText));
            canvas.drawText(drawText, getTextPosX(asRect, drawText, shiftEdgeDissolveTextPaint), getTextPosY(asRect, shiftEdgeDissolveTextPaint), shiftEdgeDissolveTextPaint);
        }
    }

    private void drawDissolveAnimation(Canvas canvas) {
        float progress = 1.0f - (float) dissolveAnimationCounter / (float) DISSOLVE_ANIMATION_DURATION;
        float inset = progress * (Math.min(rectWidth, rectHeight) * 0.42f);
        float left = dissolveAnimationRect.left + inset;
        float top = dissolveAnimationRect.top + inset;
        float right = dissolveAnimationRect.right - inset;
        float bottom = dissolveAnimationRect.bottom - inset;

        dissolveAnimationPaint.setStyle(Paint.Style.FILL);
        dissolveAnimationPaint.setColor(0xff000000);
        dissolveAnimationPaint.setAlpha(Math.min(220, 80 + (int) (progress * 140.0f)));
        canvas.drawRoundRect(left, top, right, bottom, 10.0f, 10.0f, dissolveAnimationPaint);

        dissolveAnimationPaint.setStyle(Paint.Style.STROKE);
        dissolveAnimationPaint.setColor(TEXT_COLOR_WHITE);
        dissolveAnimationPaint.setStrokeWidth(Math.max(2.0f, 10.0f * (1.0f - progress)));
        dissolveAnimationPaint.setAlpha(Math.max(40, 220 - (int) (progress * 180.0f)));
        canvas.drawLine(left, top, right, bottom, dissolveAnimationPaint);
        canvas.drawLine(right, top, left, bottom, dissolveAnimationPaint);
    }

    private void drawLineDissolveAnimation(Canvas canvas) {
        if (!lineDissolveAnimationRunning || lineDissolveRects == null) {
            return;
        }

        float baseProgress = 1.0f - (float) lineDissolveAnimationCounter / (float) LINE_DISSOLVE_ANIMATION_DURATION;
        int length = lineDissolveRects.length;
        for (int i = 0; i < length; i++) {
            Rect sourceRect = lineDissolveRects[i];
            Paint sourcePaint = lineDissolvePaints[i];
            if (sourceRect == null || sourcePaint == null) {
                continue;
            }

            float stagger = (length <= 1) ? 0.0f : ((float) i / (float) (length - 1)) * 0.22f;
            float progress = Math.min(1.0f, Math.max(0.0f, (baseProgress - stagger) / (1.0f - stagger + 0.0001f)));
            int alpha = Math.max(18, 255 - (int) (progress * 235.0f));
            float inset = progress * Math.min(sourceRect.width(), sourceRect.height()) * 0.34f;

            float left = sourceRect.left + inset;
            float top = sourceRect.top + inset;
            float right = sourceRect.right - inset;
            float bottom = sourceRect.bottom - inset;

            shiftEdgeDissolvePaint.setColor(sourcePaint.getColor());
            shiftEdgeDissolvePaint.setAlpha(alpha);
            canvas.drawRoundRect(left, top, right, bottom, 10.0f, 10.0f, shiftEdgeDissolvePaint);

            shiftEdgeDissolveStrokePaint.setColor(TEXT_COLOR_WHITE);
            shiftEdgeDissolveStrokePaint.setStrokeWidth(Math.max(1.8f, (1.0f - progress) * 6.0f));
            shiftEdgeDissolveStrokePaint.setAlpha(Math.max(10, alpha - 70));
            canvas.drawLine(left, top, right, bottom, shiftEdgeDissolveStrokePaint);
            canvas.drawLine(left, bottom, right, top, shiftEdgeDissolveStrokePaint);

            String text = lineDissolveTexts[i];
            if (text != null && !text.isEmpty()) {
                lineDissolveDrawRect.left = (int) left;
                lineDissolveDrawRect.top = (int) top;
                lineDissolveDrawRect.right = (int) right;
                lineDissolveDrawRect.bottom = (int) bottom;
                shiftEdgeDissolveTextPaint.setTextSize(getTextSize(text));
                shiftEdgeDissolveTextPaint.setAlpha(Math.max(16, alpha - 90));
                canvas.drawText(text,
                        getTextPosX(lineDissolveDrawRect, text, shiftEdgeDissolveTextPaint),
                        getTextPosY(lineDissolveDrawRect, shiftEdgeDissolveTextPaint),
                        shiftEdgeDissolveTextPaint);
            }
        }
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
        int statusTop = (int) (height * cellHeight);
        int gridTop = statusTop + 3 * RECT_BORDER;
        int bonusGridBottom = gridTop + 2 * bonusSlotHeight;
        float cardLeft = RECT_BORDER;
        float cardGap = RECT_BORDER;
        float cardTop = bonusGridBottom + 2.0f * RECT_BORDER;
        float cardBottom = canvasHeight - 2.0f * RECT_BORDER;
        float cardWidth = (canvasWidth - 2.0f * cardLeft - 2.0f * cardGap) / 3.0f;
        float cornerRadius = Math.min(HUD_CORNER_RADIUS, (cardBottom - cardTop) / 3.0f);
        int bestAccentColor = highscoreExceeded ? HUD_BEST_NEW_RECORD_ACCENT_COLOR : HUD_BEST_ACCENT_COLOR;

        statusScoreRect = new Rect((int) cardLeft, (int) cardTop, (int) (cardLeft + cardWidth), (int) cardBottom);
        drawStatusPanel(canvas, cardLeft, cardTop, cardLeft + cardWidth, cardBottom,
                "SCORE", getScoreText(score), HUD_SCORE_ACCENT_COLOR, cornerRadius);
        cardLeft += cardWidth + cardGap;
        statusLevelRect = new Rect((int) cardLeft, (int) cardTop, (int) (cardLeft + cardWidth), (int) cardBottom);
        drawStatusPanel(canvas, cardLeft, cardTop, cardLeft + cardWidth, cardBottom,
                "LEVEL", Integer.toString(level), HUD_LEVEL_ACCENT_COLOR, cornerRadius);
        cardLeft += cardWidth + cardGap;
        statusBestRect = new Rect((int) cardLeft, (int) cardTop, (int) (cardLeft + cardWidth), (int) cardBottom);
        drawStatusPanel(canvas, cardLeft, cardTop, cardLeft + cardWidth, cardBottom,
                "BEST", getScoreText(highScore), bestAccentColor, cornerRadius);
    }

    private void drawStatusPanel(Canvas canvas, float left, float top, float right, float bottom,
                                 String label, String value, int accentColor, float cornerRadius) {
        statusPanelRect.set(left, top, right, bottom);
        canvas.drawRoundRect(statusPanelRect, cornerRadius, cornerRadius, statusPanelPaint);
        canvas.drawRoundRect(statusPanelRect, cornerRadius, cornerRadius, statusPanelBorderPaint);

        float inset = RECT_BORDER / 2.0f;
        float accentHeight = Math.max(10.0f, (bottom - top) * 0.12f);
        statusPanelAccentPaint.setColor(accentColor);
        statusPanelRect.set(left + inset, top + inset, right - inset, top + inset + accentHeight);
        canvas.drawRoundRect(statusPanelRect, cornerRadius * 0.7f, cornerRadius * 0.7f, statusPanelAccentPaint);

        float innerLeft = left + RECT_BORDER;
        float innerRight = right - RECT_BORDER;
        float horizontalGap = RECT_BORDER;
        float defaultLabelTextSize = statusLabelPaint.getTextSize();
        float defaultValueTextSize = scoreAndLevelPaint.getTextSize();
        float defaultLabelWidth = statusLabelPaint.measureText(label);
        float availableWidth = innerRight - innerLeft;

        float minLabelTextSize = Math.max(16.0f, defaultLabelTextSize * 0.70f);
        while (statusLabelPaint.getTextSize() > minLabelTextSize
                && (statusLabelPaint.measureText(label) + horizontalGap + scoreAndLevelPaint.measureText(value) > availableWidth)) {
            statusLabelPaint.setTextSize(statusLabelPaint.getTextSize() - 1.0f);
        }

        float labelWidth = statusLabelPaint.measureText(label);
        if (labelWidth > availableWidth * 0.48f) {
            labelWidth = availableWidth * 0.48f;
        }
        float valueAvailableWidth = Math.max(24.0f, availableWidth - labelWidth - horizontalGap);
        float minValueTextSize = Math.max(30.0f, defaultValueTextSize * 0.58f);
        float fittedTextSize = defaultValueTextSize;
        while (fittedTextSize > minValueTextSize && scoreAndLevelPaint.measureText(value) > valueAvailableWidth) {
            fittedTextSize -= 2.0f;
            scoreAndLevelPaint.setTextSize(fittedTextSize);
        }

        Paint.FontMetrics labelMetrics = statusLabelPaint.getFontMetrics();
        Paint.FontMetrics valueMetrics = scoreAndLevelPaint.getFontMetrics();
        float contentTop = top + accentHeight + 2.0f * RECT_BORDER;
        float contentBottom = bottom - RECT_BORDER;
        float labelHeight = labelMetrics.descent - labelMetrics.ascent;
        float valueHeight = valueMetrics.descent - valueMetrics.ascent;
        float contentCenterY = (contentTop + contentBottom) / 2.0f;
        float baselineForLabel = contentCenterY - (labelHeight / 2.0f) - labelMetrics.ascent;
        float baselineForValue = contentCenterY - (valueHeight / 2.0f) - valueMetrics.ascent;

        canvas.drawText(label, innerLeft, baselineForLabel, statusLabelPaint);
        float valueX = innerRight - scoreAndLevelPaint.measureText(value);
        canvas.drawText(value, valueX, baselineForValue, scoreAndLevelPaint);

        statusLabelPaint.setTextSize(defaultLabelTextSize);
        scoreAndLevelPaint.setTextSize(defaultValueTextSize);
    }

    private void drawBonusInformation(Canvas canvas) {
        drawBonusSlot(canvas, undo, undoRect, undoText, undoBuyRect, BUY_COST_UNDO, BONUS_ACCENT_COLORS[0], undoSelected);
        drawBonusSlot(canvas, swap, swapRect, swapText, swapBuyRect, BUY_COST_SWAP, BONUS_ACCENT_COLORS[1], swapSelected);
        drawBonusSlot(canvas, jump, jumpRect, jumpText, jumpBuyRect, BUY_COST_JUMP, BONUS_ACCENT_COLORS[2], jumpSelected);
        drawBonusSlot(canvas, dissolve, dissolveRect, dissolveText, dissolveBuyRect, BUY_COST_DISSOLVE, BONUS_ACCENT_COLORS[3], dissolveSelected);
        drawBonusSlot(canvas, delRow, delRowRect, delRowText, delRowBuyRect, BUY_COST_DEL_ROW, BONUS_ACCENT_COLORS[4], delRowSelected);
        drawBonusSlot(canvas, delCol, delColRect, delColText, delColBuyRect, BUY_COST_DEL_COL, BONUS_ACCENT_COLORS[5], delColSelected);
        drawBonusSlot(canvas, shiftRow, shiftRowRect, shiftRowText, shiftRowBuyRect, BUY_COST_SHIFT_ROW, BONUS_ACCENT_COLORS[6], shiftRowSelected);
        drawBonusSlot(canvas, shiftCol, shiftColRect, shiftColText, shiftColBuyRect, BUY_COST_SHIFT_COL, BONUS_ACCENT_COLORS[7], shiftColSelected);
    }

    private void drawBonusSlot(Canvas canvas, Drawable icon, Rect iconRect, String countText, Rect buyRect,
                               long buyCost, int accentColor, boolean isSelected) {
        float slotTop = iconRect.top - 2.0f * RECT_BORDER;
        float slotBottom = buyRect.bottom + RECT_BORDER;
        bonusSlotRect.set(buyRect.left - RECT_BORDER, slotTop, buyRect.right + RECT_BORDER, slotBottom);
        canvas.drawRoundRect(bonusSlotRect, 16.0f, 16.0f, bonusSlotCardPaint);
        if (isSelected) {
            int prevAlpha = bonusIconBgPaint.getAlpha();
            bonusIconBgPaint.setColor(accentColor);
            bonusIconBgPaint.setAlpha(88);
            canvas.drawRoundRect(bonusSlotRect, 16.0f, 16.0f, bonusIconBgPaint);
            bonusIconBgPaint.setAlpha(prevAlpha);
        }
        canvas.drawRoundRect(bonusSlotRect, 16.0f, 16.0f, bonusSlotBorderPaint);

        bonusIconBgPaint.setColor(isSelected ? accentColor : BONUS_ICON_BG_DEFAULT_COLOR);
        float iconFieldInset = RECT_BORDER * 0.45f;
        bonusSlotRect.set(
                iconRect.left - iconFieldInset,
                iconRect.top - iconFieldInset,
                iconRect.right + iconFieldInset,
                iconRect.bottom + iconFieldInset
        );
        canvas.drawRoundRect(bonusSlotRect, 14.0f, 14.0f, bonusIconBgPaint);
        if (isSelected) {
            bonusIconSelectedBorderPaint.setColor(accentColor);
            canvas.drawRoundRect(bonusSlotRect, 14.0f, 14.0f, bonusIconSelectedBorderPaint);
        }

        if (icon != null) {
            icon.draw(canvas);
        }
        int countOffset = getApproxXToCenterText(countText, bonusPaint, bonusSlotWidth);
        canvas.drawText(countText, iconRect.left + countOffset, iconRect.bottom + bonusPaint.getTextSize(), bonusPaint);

        canvas.drawRect(buyRect, buyBoxPaint);
        String buyText = "+1 " + getScoreText(buyCost);
        int buyOffset = getApproxXToCenterText(buyText, buyPaint, bonusSlotWidth);
        canvas.drawText(buyText, buyRect.left + buyOffset, buyRect.bottom - RECT_BORDER, buyPaint);
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
        int statusTop = (int) (height * cellHeight);
        int gridTop = statusTop + 3 * RECT_BORDER;
        int rowHeight = (STATUS_FIELD_HEIGHT - 16 * RECT_BORDER) / 2;
        bonusSlotWidth = canvasWidth / 4;
        bonusSlotHeight = rowHeight;
        buyButtonHeight = RECT_BORDER + BONUS_TEXT_SIZE + 4;
        int maxIconSize = Math.min(bonusSlotWidth - 6 * RECT_BORDER, bonusSlotHeight - buyButtonHeight - 6 * RECT_BORDER - BONUS_TEXT_SIZE);
        bonusIconWidth = (int) (Math.max(24, maxIconSize) * 0.96f);

        undoRect = createBonusIconRect(0, 0, gridTop);
        swapRect = createBonusIconRect(0, 1, gridTop);
        jumpRect = createBonusIconRect(0, 2, gridTop);
        dissolveRect = createBonusIconRect(0, 3, gridTop);
        delRowRect = createBonusIconRect(1, 0, gridTop);
        delColRect = createBonusIconRect(1, 1, gridTop);
        shiftRowRect = createBonusIconRect(1, 2, gridTop);
        shiftColRect = createBonusIconRect(1, 3, gridTop);

        undoSlotRect = createBonusSlotRect(0, 0, gridTop);
        swapSlotRect = createBonusSlotRect(0, 1, gridTop);
        jumpSlotRect = createBonusSlotRect(0, 2, gridTop);
        dissolveSlotRect = createBonusSlotRect(0, 3, gridTop);
        delRowSlotRect = createBonusSlotRect(1, 0, gridTop);
        delColSlotRect = createBonusSlotRect(1, 1, gridTop);
        shiftRowSlotRect = createBonusSlotRect(1, 2, gridTop);
        shiftColSlotRect = createBonusSlotRect(1, 3, gridTop);

        undoBuyRect = createBuyRectForSlot(0, 0, gridTop);
        swapBuyRect = createBuyRectForSlot(0, 1, gridTop);
        jumpBuyRect = createBuyRectForSlot(0, 2, gridTop);
        dissolveBuyRect = createBuyRectForSlot(0, 3, gridTop);
        delRowBuyRect = createBuyRectForSlot(1, 0, gridTop);
        delColBuyRect = createBuyRectForSlot(1, 1, gridTop);
        shiftRowBuyRect = createBuyRectForSlot(1, 2, gridTop);
        shiftColBuyRect = createBuyRectForSlot(1, 3, gridTop);

        undo = ContextCompat.getDrawable(context, R.drawable.undo_circle_icon);
        if (undo != null) undo.setBounds(undoRect);

        swap = ContextCompat.getDrawable(context, R.drawable.swap_icon);
        if (swap != null) swap.setBounds(swapRect);

        jump = ContextCompat.getDrawable(context, R.drawable.jump_icon);
        if (jump != null) jump.setBounds(jumpRect);

        dissolve = ContextCompat.getDrawable(context, R.drawable.delete_block_icon);
        if (dissolve != null) dissolve.setBounds(dissolveRect);

        delRow = ContextCompat.getDrawable(context, R.drawable.delete_row_icon);
        if (delRow != null) delRow.setBounds(delRowRect);

        delCol = ContextCompat.getDrawable(context, R.drawable.delete_col_icon);
        if (delCol != null) delCol.setBounds(delColRect);

        shiftRow = ContextCompat.getDrawable(context, R.drawable.shift_row_icon);
        if (shiftRow != null) shiftRow.setBounds(shiftRowRect);

        shiftCol = ContextCompat.getDrawable(context, R.drawable.shift_col_icon);
        if (shiftCol != null) shiftCol.setBounds(shiftColRect);

        bonusPaint = new Paint();
        bonusPaint.setColor(TEXT_COLOR_WHITE);
        bonusPaint.setTextSize(BONUS_TEXT_SIZE);

        float approxPanelWidth = (canvasWidth - 6.0f * RECT_BORDER) / 3.0f;
        statusLabelPaint.setTextSize(Math.max(20.0f, Math.min(STATUS_LABEL_TEXT_SIZE, approxPanelWidth * 0.16f)));
        scoreAndLevelPaint.setTextSize(Math.max(34.0f, Math.min(STATUS_TEXT_SIZE, approxPanelWidth * 0.23f)));

        buyPaint = new Paint();
        buyPaint.setColor(TEXT_COLOR_WHITE);
        buyPaint.setTextSize(BONUS_TEXT_SIZE - 8);

        buyBoxPaint = new Paint();
        buyBoxPaint.setColor(0xff2d2d2d);

        bonusSlotCardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bonusSlotCardPaint.setColor(BONUS_SLOT_CARD_COLOR);
        bonusSlotCardPaint.setStyle(Paint.Style.FILL);

        bonusSlotBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bonusSlotBorderPaint.setColor(BONUS_SLOT_BORDER_COLOR);
        bonusSlotBorderPaint.setStyle(Paint.Style.STROKE);
        bonusSlotBorderPaint.setStrokeWidth(3.0f);

        bonusIconBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bonusIconBgPaint.setStyle(Paint.Style.FILL);

        bonusIconSelectedBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bonusIconSelectedBorderPaint.setStyle(Paint.Style.STROKE);
        bonusIconSelectedBorderPaint.setStrokeWidth(5.0f);

        highlightPaint = new Paint();
        highlightPaint.setColor(HIGHLIGHT_COLOR);

        highlightRect = new Rect();
        shiftBonusDrawRect = new Rect();
        lineDissolveDrawRect = new Rect();
    }

    private Rect createBonusIconRect(int row, int col, int gridTop) {
        int slotLeft = col * bonusSlotWidth;
        int rowTop = gridTop + row * bonusSlotHeight;
        int iconLeft = slotLeft + (bonusSlotWidth - bonusIconWidth) / 2;
        int iconTop = rowTop + 2 * RECT_BORDER;
        return new Rect(iconLeft, iconTop, iconLeft + bonusIconWidth, iconTop + bonusIconWidth);
    }

    private Rect createBuyRectForSlot(int row, int col, int gridTop) {
        int slotLeft = col * bonusSlotWidth;
        int rowTop = gridTop + row * bonusSlotHeight;
        int buyTop = rowTop + bonusSlotHeight - buyButtonHeight - 2 * RECT_BORDER;
        return new Rect(slotLeft + RECT_BORDER, buyTop, slotLeft + bonusSlotWidth - RECT_BORDER, buyTop + buyButtonHeight);
    }

    private Rect createBonusSlotRect(int row, int col, int gridTop) {
        int slotLeft = col * bonusSlotWidth;
        int rowTop = gridTop + row * bonusSlotHeight;
        return new Rect(slotLeft, rowTop + RECT_BORDER, slotLeft + bonusSlotWidth, rowTop + bonusSlotHeight + RECT_BORDER);
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

        if (dissolveAnimationRunning) {
            animateDissolveBonus();
            return;
        }
        if (lineDissolveAnimationRunning) {
            animateLineDissolveBonus();
            return;
        }
        if (status == statusT.BONUS_SHIFT_ANIMATION) {
            animateShiftBonus();
            return;
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

            if (highscoreExceeded && !highscoreLockedByCheat) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(PREF_KEY_HIGHSCORE, highScore);
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

    private void startDissolveAnimation(int x, int y) {
        dissolveAnimationRunning = true;
        dissolveAnimationCounter = DISSOLVE_ANIMATION_DURATION;
        dissolveTargetX = x;
        dissolveTargetY = y;
        dissolveAnimationRect = getRect(x, y);
        if (dissolveAnimationPaint == null) {
            dissolveAnimationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
    }

    private void animateDissolveBonus() {
        dissolveAnimationCounter--;
        if (dissolveAnimationCounter <= 0) {
            finishDissolveAnimation();
        }
    }

    private void startLineDissolveAnimation(boolean isRow, int lineIndex) {
        lineDissolveAnimationRunning = true;
        lineDissolveIsRow = isRow;
        lineDissolveLineIndex = lineIndex;
        lineDissolveAnimationCounter = LINE_DISSOLVE_ANIMATION_DURATION;

        int length = isRow ? width : height;
        lineDissolveRects = new Rect[length];
        lineDissolvePaints = new Paint[length];
        lineDissolveTexts = new String[length];

        for (int i = 0; i < length; i++) {
            int x = isRow ? i : lineIndex;
            int y = isRow ? lineIndex : i;
            int value = gameBoardArray.get(x, y);
            if (value != -1) {
                lineDissolveRects[i] = new Rect(rectArray[x][y]);
                Paint p = new Paint();
                p.setColor(paintArray[x][y].getColor());
                lineDissolvePaints[i] = p;
                lineDissolveTexts[i] = getText(x, y);
            }
            gameBoardArray.set(x, y, -1);
            paintArray[x][y].setColor(getColor(x, y));
        }
    }

    private void animateLineDissolveBonus() {
        lineDissolveAnimationCounter--;
        if (lineDissolveAnimationCounter <= 0) {
            finishLineDissolveAnimation();
        }
    }

    private void finishLineDissolveAnimation() {
        lineDissolveAnimationRunning = false;
        lineDissolveRects = null;
        lineDissolvePaints = null;
        lineDissolveTexts = null;

        if (lineDissolveIsRow) {
            delRowCounter--;
            delRowSelected = false;
        } else {
            delColCounter--;
            delColSelected = false;
        }

        startDropInsIfBoardEmpty();
        updateBonusValues();
    }

    private void startShiftBonusAnimation(boolean isRow, int lineIndex) {
        shiftBonusAnimationIsRow = isRow;
        shiftBonusLineIndex = lineIndex;
        shiftBonusAnimationCounter = SHIFT_BONUS_ANIMATION_STEPS;
        shiftBonusOffsetX = 0.0f;
        shiftBonusOffsetY = 0.0f;
        shiftBonusStepX = isRow ? cellWidth / SHIFT_BONUS_ANIMATION_STEPS : 0.0f;
        shiftBonusStepY = isRow ? 0.0f : cellHeight / SHIFT_BONUS_ANIMATION_STEPS;

        int length = isRow ? width : height;
        shiftBonusValues = new int[length];
        shiftBonusRects = new Rect[length];
        shiftBonusPaints = new Paint[length];
        shiftBonusTexts = new String[length];

        for (int i = 0; i < length; i++) {
            int x = isRow ? i : lineIndex;
            int y = isRow ? lineIndex : i;
            int value = gameBoardArray.get(x, y);
            shiftBonusValues[i] = value;
            if (value != -1) {
                shiftBonusRects[i] = new Rect(rectArray[x][y]);
                Paint p = new Paint();
                p.setColor(paintArray[x][y].getColor());
                shiftBonusPaints[i] = p;
                shiftBonusTexts[i] = getText(x, y);
            }
            gameBoardArray.set(x, y, -1);
            paintArray[x][y].setColor(getColor(x, y));
        }

        status = statusT.BONUS_SHIFT_ANIMATION;
    }

    private void animateShiftBonus() {
        shiftBonusOffsetX += shiftBonusStepX;
        shiftBonusOffsetY += shiftBonusStepY;
        shiftBonusAnimationCounter--;
        if (shiftBonusAnimationCounter <= 0) {
            finishShiftBonusAnimation();
        }
    }

    private void finishShiftBonusAnimation() {
        int length = shiftBonusAnimationIsRow ? width : height;
        for (int i = length - 1; i >= 1; i--) {
            int shiftedValue = shiftBonusValues[i - 1];
            if (shiftedValue == -1) {
                continue;
            }
            int x = shiftBonusAnimationIsRow ? i : shiftBonusLineIndex;
            int y = shiftBonusAnimationIsRow ? shiftBonusLineIndex : i;
            gameBoardArray.set(x, y, shiftedValue);
            paintArray[x][y].setColor(getColor(x, y));
        }

        if (shiftBonusAnimationIsRow) {
            shiftRowCounter--;
            shiftRowSelected = false;
        } else {
            shiftColCounter--;
            shiftColSelected = false;
        }

        shiftBonusValues = null;
        shiftBonusRects = null;
        shiftBonusPaints = null;
        shiftBonusTexts = null;
        status = statusT.SELECT_START_POSITION;
        updateBonusValues();
    }

    private void finishDissolveAnimation() {
        dissolveAnimationRunning = false;
        gameBoardArray.set(dissolveTargetX, dissolveTargetY, -1);
        paintArray[dissolveTargetX][dissolveTargetY].setColor(getColor(dissolveTargetX, dissolveTargetY));
        dissolveCounter--;
        dissolveSelected = false;
        startDropInsIfBoardEmpty();
        updateBonusValues();
    }

    private boolean handleSecretBonusCodeTap(int x, int y) {
        int tappedCard = getStatusCardTapIndex(x, y);
        if (tappedCard == -1) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (now - secretCodeLastTapMs > SECRET_CODE_TAP_TIMEOUT_MS) {
            secretCodeProgress = 0;
        }
        secretCodeLastTapMs = now;

        if (SECRET_CODE_SEQUENCE[secretCodeProgress] == tappedCard) {
            secretCodeProgress++;
            if (secretCodeProgress == SECRET_CODE_SEQUENCE.length) {
                secretCodeProgress = 0;
                grantAllBonusesForTesting();
            }
        } else {
            secretCodeProgress = (SECRET_CODE_SEQUENCE[0] == tappedCard) ? 1 : 0;
        }
        return true;
    }

    private int getStatusCardTapIndex(int x, int y) {
        if (statusScoreRect == null || statusLevelRect == null || statusBestRect == null) {
            return -1;
        }
        if (statusScoreRect.contains(x, y)) {
            return SECRET_CODE_SCORE;
        }
        if (statusLevelRect.contains(x, y)) {
            return SECRET_CODE_LEVEL;
        }
        if (statusBestRect.contains(x, y)) {
            return SECRET_CODE_BEST;
        }
        return -1;
    }

    private void grantAllBonusesForTesting() {
        for (int i = 0; i < BONUS_BUY_COSTS.length; i++) {
            addBonusCount(i, 1);
        }
        if (!highscoreLockedByCheat) {
            highscoreLockedByCheat = true;
            highscoreExceeded = false;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(PREF_KEY_HIGHSCORE_LOCKED_BY_CHEAT, true);
            editor.apply();
        }
        // Subtle feedback that the hidden code was accepted.
        alertAnimationCounter = ALERT_TIME / 2;
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

        int drawableId = R.drawable.cross_icon;
        if (bonusWin >= 0 && bonusWin < BONUS_DRAWABLE_IDS.length) {
            drawableId = BONUS_DRAWABLE_IDS[bonusWin];
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
        dissolveText = Integer.toString(dissolveCounter);
        delRowText = Integer.toString(delRowCounter);
        delColText = Integer.toString(delColCounter);
        shiftRowText = Integer.toString(shiftRowCounter);
        shiftColText = Integer.toString(shiftColCounter);
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

    private int getWeightedRandomBonusIndex() {
        double totalWeight = 0.0d;
        for (long bonusCost : BONUS_BUY_COSTS) {
            totalWeight += 1.0d / (double) bonusCost;
        }

        double roll = rand.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0d;
        for (int i = 0; i < BONUS_BUY_COSTS.length; i++) {
            cumulativeWeight += 1.0d / (double) BONUS_BUY_COSTS[i];
            if (roll <= cumulativeWeight) {
                return i;
            }
        }
        return BONUS_BUY_COSTS.length - 1;
    }

    private void addBonusCount(int bonusIndex, int amount) {
        switch (bonusIndex) {
            case 0:
                undoCounter += amount;
                break;
            case 1:
                swapCounter += amount;
                break;
            case 2:
                jumpCounter += amount;
                break;
            case 3:
                dissolveCounter += amount;
                break;
            case 4:
                delRowCounter += amount;
                break;
            case 5:
                delColCounter += amount;
                break;
            case 6:
                shiftRowCounter += amount;
                break;
            case 7:
                shiftColCounter += amount;
                break;
            default:
                break;
        }
    }

    private void awardRandomBonus() {
        bonusWin = getWeightedRandomBonusIndex();
        addBonusCount(bonusWin, BONUS_AWARD_AMOUNT);
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

        if (!highscoreLockedByCheat && score > highScore) {
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

        if (dissolveAnimationRunning || lineDissolveAnimationRunning || status == statusT.BONUS_SHIFT_ANIMATION) {
            return;
        }

        if (handleSecretBonusCodeTap(x, y)) {
            updateBonusValues();
            return;
        }

        int indexX = (int) (x / cellWidth);
        int indexY = (int) (y / cellHeight);

        if (status == statusT.GAME_OVER) {
            gameOverAnimationPhase = 0;
            gameInit();
        }

        if (status == statusT.SELECT_START_POSITION) {
            if (indexX >= 0 && indexX < width && indexY >= 0 && indexY < height) {
                int cellValue = gameBoardArray.get(indexX, indexY);

                if (handleBoardBonusTap(indexX, indexY, cellValue)) {
                    updateBonusValues();
                } else if (cellValue != -1) {
                    // start cell selection
                    startPositionX = indexX;
                    startPositionY = indexY;
                    highlightCell(startPositionX, startPositionY);
                    status = statusT.SELECT_TARGET_POSITION;
                }
            }
        } else if (status == statusT.SELECT_TARGET_POSITION) {
            if (indexX >= 0 && indexX < width && indexY >= 0 && indexY < height) {
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

        if (status == statusT.SELECT_START_POSITION || status == statusT.SELECT_TARGET_POSITION) {
            if (handleBonusBarTap(x, y)) {
                updateBonusValues();
            }
        }
    }

    private boolean handleBoardBonusTap(int indexX, int indexY, int cellValue) {
        if (dissolveSelected) {
            if (cellValue != -1) {
                startDissolveAnimation(indexX, indexY);
            } else {
                alertAnimationCounter = ALERT_TIME;
            }
            return true;
        }

        if (delRowSelected) {
            startLineDissolveAnimation(true, indexY);
            return true;
        }

        if (delColSelected) {
            startLineDissolveAnimation(false, indexX);
            return true;
        }

        if (shiftRowSelected) {
            startShiftBonusAnimation(true, indexY);
            return true;
        }

        if (shiftColSelected) {
            startShiftBonusAnimation(false, indexX);
            return true;
        }

        return false;
    }

    private void startDropInsIfBoardEmpty() {
        if (gameBoardArray.getNumFreeCells() == width * height) {
            status = statusT.DROP_IN_NEW_CELLS;
            dropInCount = DROP_INS_AFTER_MOTION;
        } else {
            status = statusT.SELECT_START_POSITION;
        }
    }

    private boolean handleBonusBarTap(int x, int y) {
        if (undoRect == null) {
            return false;
        }

        if (undoBuyRect.contains(x, y)) {
            if (tryBuyBonus(undoBuyRect, x, y, BUY_COST_UNDO)) {
                undoCounter++;
            }
            return true;
        }
        if (swapBuyRect.contains(x, y)) {
            if (tryBuyBonus(swapBuyRect, x, y, BUY_COST_SWAP)) {
                swapCounter++;
            }
            return true;
        }
        if (jumpBuyRect.contains(x, y)) {
            if (tryBuyBonus(jumpBuyRect, x, y, BUY_COST_JUMP)) {
                jumpCounter++;
            }
            return true;
        }
        if (dissolveBuyRect.contains(x, y)) {
            if (tryBuyBonus(dissolveBuyRect, x, y, BUY_COST_DISSOLVE)) {
                dissolveCounter++;
            }
            return true;
        }
        if (delRowBuyRect.contains(x, y)) {
            if (tryBuyBonus(delRowBuyRect, x, y, BUY_COST_DEL_ROW)) {
                delRowCounter++;
            }
            return true;
        }
        if (delColBuyRect.contains(x, y)) {
            if (tryBuyBonus(delColBuyRect, x, y, BUY_COST_DEL_COL)) {
                delColCounter++;
            }
            return true;
        }
        if (shiftRowBuyRect.contains(x, y)) {
            if (tryBuyBonus(shiftRowBuyRect, x, y, BUY_COST_SHIFT_ROW)) {
                shiftRowCounter++;
            }
            return true;
        }
        if (shiftColBuyRect.contains(x, y)) {
            if (tryBuyBonus(shiftColBuyRect, x, y, BUY_COST_SHIFT_COL)) {
                shiftColCounter++;
            }
            return true;
        }

        if (undoSlotRect.contains(x, y) && undoCounter > 0) {
            deselectAllBonuses();
            gameBoardArray.unrollBackup();
            undoCounter--;
            return true;
        }
        if (swapSlotRect.contains(x, y) && swapCounter > 0) {
            boolean prev = swapSelected;
            deselectAllBonuses();
            swapSelected = !prev;
            return true;
        }
        if (jumpSlotRect.contains(x, y) && jumpCounter > 0) {
            boolean prev = jumpSelected;
            deselectAllBonuses();
            jumpSelected = !prev;
            return true;
        }
        if (dissolveSlotRect.contains(x, y) && dissolveCounter > 0) {
            leaveTargetSelectionMode();
            boolean prev = dissolveSelected;
            deselectAllBonuses();
            dissolveSelected = !prev;
            return true;
        }
        if (delRowSlotRect.contains(x, y) && delRowCounter > 0) {
            leaveTargetSelectionMode();
            boolean prev = delRowSelected;
            deselectAllBonuses();
            delRowSelected = !prev;
            return true;
        }
        if (delColSlotRect.contains(x, y) && delColCounter > 0) {
            leaveTargetSelectionMode();
            boolean prev = delColSelected;
            deselectAllBonuses();
            delColSelected = !prev;
            return true;
        }
        if (shiftRowSlotRect.contains(x, y) && shiftRowCounter > 0) {
            leaveTargetSelectionMode();
            boolean prev = shiftRowSelected;
            deselectAllBonuses();
            shiftRowSelected = !prev;
            return true;
        }
        if (shiftColSlotRect.contains(x, y) && shiftColCounter > 0) {
            leaveTargetSelectionMode();
            boolean prev = shiftColSelected;
            deselectAllBonuses();
            shiftColSelected = !prev;
            return true;
        }

        return false;
    }

    private boolean tryBuyBonus(Rect buyRect, int x, int y, long buyCost) {
        if (buyRect == null || !buyRect.contains(x, y)) {
            return false;
        }
        if (score < buyCost) {
            alertAnimationCounter = ALERT_TIME;
            return false;
        }
        score -= buyCost;
        return true;
    }

    private void leaveTargetSelectionMode() {
        if (status == statusT.SELECT_TARGET_POSITION) {
            resetCell(startPositionX, startPositionY);
            status = statusT.SELECT_START_POSITION;
        }
    }

    /** Deselects all bonus actions at once. */
    private void deselectAllBonuses() {
        undoSelected   = false;
        swapSelected   = false;
        jumpSelected   = false;
        dissolveSelected = false;
        delRowSelected = false;
        delColSelected = false;
        shiftRowSelected = false;
        shiftColSelected = false;
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
