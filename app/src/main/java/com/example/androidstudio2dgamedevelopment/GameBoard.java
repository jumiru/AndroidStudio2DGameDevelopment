package com.example.androidstudio2dgamedevelopment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

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
    private static final int LEVEL_PRUNE_ANIMATION_DURATION = 22;
    private static final int SHIFT_BONUS_ANIMATION_STEPS = 10;
    private static final int SHIFT_SELECTION_PULSE_CYCLE = 30;
    private static final float SHIFT_SELECTION_SWIPE_THRESHOLD_CELLS = 0.38f;
    private static final int SHIFT_BLUR_TRAIL_COUNT = 3;
    private static final int SHIFT_BLUR_BASE_ALPHA = 92;
    private static final int SHIFT_EDGE_DISSOLVE_MIN_ALPHA = 28;
    private static final float SHIFT_EDGE_DISSOLVE_INSET_RATIO = 0.34f;
    private static final long SECRET_CODE_TAP_TIMEOUT_MS = 1800L;
    private static final int SECRET_CODE_SCORE = 0;
    private static final int SECRET_CODE_LEVEL = 1;
    private static final int SECRET_CODE_BEST = 2;
    private static final long SECRET_CODE_SCORE_BOOST_AMOUNT = 300L;
    // Hidden test code 1: SCORE -> LEVEL -> BEST -> SCORE
    private static final int[] SECRET_CODE_SEQUENCE_BONUS = {
            SECRET_CODE_SCORE,
            SECRET_CODE_LEVEL,
            SECRET_CODE_BEST,
            SECRET_CODE_SCORE
    };
    // Hidden test code 2: BEST -> LEVEL -> SCORE -> BEST
    private static final int[] SECRET_CODE_SEQUENCE_SCORE_BOOST = {
            SECRET_CODE_BEST,
            SECRET_CODE_LEVEL,
            SECRET_CODE_SCORE,
            SECRET_CODE_BEST
    };
    // Hidden code 3: BEST x4 -> reset highscore
    private static final int[] SECRET_CODE_SEQUENCE_RESET_HIGHSCORE = {
            SECRET_CODE_BEST,
            SECRET_CODE_BEST,
            SECRET_CODE_BEST,
            SECRET_CODE_BEST
    };
    // Hidden code 4: SCORE x4 -> toggle self-play / watch mode
    private static final int[] SECRET_CODE_SEQUENCE_SELF_PLAY = {
            SECRET_CODE_SCORE,
            SECRET_CODE_SCORE,
            SECRET_CODE_SCORE,
            SECRET_CODE_SCORE
    };
    private static final int[][] SECRET_CODE_SEQUENCES = {
            SECRET_CODE_SEQUENCE_BONUS,
            SECRET_CODE_SEQUENCE_SCORE_BOOST,
            SECRET_CODE_SEQUENCE_RESET_HIGHSCORE,
            SECRET_CODE_SEQUENCE_SELF_PLAY
    };
    private static final int SELF_PLAY_MOVE_DELAY   = 50;   // frames between moves
    private static final int SELF_PLAY_TAP_GAP      = 14;   // frames between start and target tap
    private static final int SELF_PLAY_TAP_ANIM_DUR = 22;   // frames for tap-ripple animation
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
    private static final String PREF_KEY_AUTO_HIGHSCORE = "auto_highscore";
    private static final String PREF_KEY_HIGHSCORE_RESET_VERSION = "highscore_reset_version";
    // Bump this value to trigger a one-time highscore reset on next launch
    private static final int HIGHSCORE_RESET_VERSION = 2;

    private static final int MIN_COMBO_SIZE = 4;
    private static final long BASE_COMBO_SCORE = 4L;
    private static final long BASE_LEVEL_SCORE = 25L;
    private static final long BASE_BONUS_STEP = 75L;
    private static final long BONUS_STEP_PER_LEVEL = 40L;
    private static final long BUY_COST_UNDO = 200L;
    private static final long BUY_COST_SWAP = 400L;
    private static final long BUY_COST_JUMP = 630L;
    private static final long BUY_COST_DISSOLVE = 300L;
    private static final long BUY_COST_DEL_LINE = 800L;
    private static final long BUY_COST_SHIFT_LINE = 565L;
    private static final long BUY_COST_COLOR_CLEAR = 1000L;
    private static final long BUY_COST_BOMB = 1500L;
    private static final int BOMB_ANIMATION_STEPS = 35;
    private static final int BONUS_AWARD_AMOUNT = 1;
    private static final double BONUS_COUNT_DAMPING = 0.5d;
    private static final int   HUD_FLIP_DURATION    = 18;
    private static final int   HUD_SLOT_SCORE       = 0;
    private static final int   HUD_SLOT_LEVEL       = 1;
    private static final int   HUD_SLOT_BEST        = 2;
    private static final float HUD_CARD_ASPECT      = 0.72f;  // card width / content height
    private static final float HUD_CARD_GAP_RATIO   = 0.10f;  // gap as fraction of card width
    private static final float HUD_CARD_CORNER      = 5f;
    private static final int   HUD_CARD_BG_COLOR    = 0xff1a2a42;
    private static final int   HUD_CARD_BORDER_COLOR = 0xff3a5a8a;
    // Bonus order must stay in sync with counters, icons, buy buttons and animation handling.
    private static final long[] BONUS_BUY_COSTS = {
            BUY_COST_UNDO,
            BUY_COST_DISSOLVE,
            BUY_COST_SWAP,
            BUY_COST_BOMB,
            BUY_COST_SHIFT_LINE,
            BUY_COST_JUMP,
            BUY_COST_DEL_LINE,
            BUY_COST_COLOR_CLEAR
    };
    private static final int[] BONUS_DRAWABLE_IDS = {
            R.drawable.undo_circle_icon,
            R.drawable.delete_block_icon,
            R.drawable.swap_icon,
            R.drawable.bomb_icon,
            R.drawable.shift_line_icon,
            R.drawable.jump_icon,
            R.drawable.delete_line_icon,
            R.drawable.color_clear_icon
    };
    private static final int[] BONUS_ACCENT_COLORS = {
            0xff66d8ff, // undo
            0xffff9f6b, // dissolve
            0xff91a8ff, // swap
            0xffff6b35, // bomb
            0xffbd8bff, // shift_line
            0xff9be27c, // jump
            0xffff7f7f, // del_line
            0xffffb0df  // color_clear
    };
    static final String[] BONUS_NAMES = {
            "Rückgängig", "Auflösen", "Tauschen", "Bombe",
            "Reihe verschieben", "Sprung", "Reihe löschen", "Farbe löschen"
    };
    static final String[] BONUS_DESCRIPTIONS = {
            "Macht den letzten Zug rückgängig.",
            "Entfernt einen einzelnen Block vom Spielfeld.",
            "Tauscht zwei Blöcke an beliebigen Positionen.",
            "Zerstört alle Blöcke in einem 3×3-Bereich um den angetippten Block.",
            "Verschiebt eine Reihe oder Spalte um ein Feld. Nach dem Antippen in die gewünschte Richtung wischen.",
            "Bewegt einen Block direkt an eine beliebige Zielposition.",
            "Löscht eine vollständige Reihe oder Spalte. Nach dem Antippen in die gewünschte Richtung wischen.",
            "Entfernt alle Blöcke mit derselben Zahl vom Spielfeld."
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
    private long autoHighScore;
    private boolean autoHighscoreExceeded;
    private long nextScoreForBonus;
    private long chainScoreProduct;
    private int chainLength;
    private boolean boardWideMergeScanActive;

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

    // level-up prune animation for removed low-value blocks
    private boolean levelPruneAnimationRunning;
    private int levelPruneAnimationCounter;
    private Coord[] levelPruneCoords;
    private Rect[] levelPruneRects;
    private Paint[] levelPrunePaints;
    private String[] levelPruneTexts;
    private Rect levelPruneDrawRect;

    private int alertAnimationCounter;

    private String levelText;
    private String levelHintText;
    private int levelAnimationCounter;
    private final Paint levelAnimationPaint;
    private final Paint levelHintPaint;

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
    private int shiftBonusDirectionSign;
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
    // combined line selection state (used by DELETE_LINE and SHIFT_LINE)
    private boolean lineSelectionActive;
    private int lineSelectionRow;
    private int lineSelectionCol;
    private int shiftSelectionPulseTick;
    // color clear bonus animation
    private boolean colorClearAnimationRunning;
    private int colorClearAnimationCounter;
    private Rect[] colorClearRects;
    private Paint[] colorClearPaints;
    private String[] colorClearTexts;
    private Rect colorClearDrawRect;
    // bomb bonus animation
    private boolean bombAnimationRunning;
    private int bombAnimationCounter;
    private Rect[] bombBlastRects;
    private Paint[] bombBlastPaints;
    private String[] bombBlastTexts;
    private int bombCenterPixelX;
    private int bombCenterPixelY;
    private Rect bombAnimDrawRect;
    private final Paint bombShockwavePaint;
    private final Paint shiftSelectionPulsePaint;
    private int touchDownX;
    private int touchDownY;
    private final Paint shiftEdgeDissolvePaint;
    private final Paint shiftEdgeDissolveStrokePaint;
    private final Paint shiftEdgeDissolveTextPaint;
    private Rect statusScoreRect;
    private Rect statusLevelRect;
    private Rect statusBestRect;
    private int[] secretCodeProgress;
    private long secretCodeLastTapMs;

    // self-play / watch mode
    private boolean selfPlayActive;
    private int     selfPlayDelayCounter;
    private int[][] selfPlayQueue;        // {pixelX, pixelY, motionAction, delayFrames}
    private int     selfPlayQueueIdx;
    private int selfPlayTapAnimX;
    private int selfPlayTapAnimY;
    private int selfPlayTapAnimCounter;
    private Paint selfPlayTapPaint;
    private Paint selfPlayIndicatorPaint;

    // split-flap HUD animation (score=0, level=1, best=2)
    private static final int MAX_HUD_DIGITS = 8;
    private final char[][] hudDigitCurrent = new char[3][MAX_HUD_DIGITS];
    private final char[][] hudDigitNext    = new char[3][MAX_HUD_DIGITS];
    private final char[][] hudDigitTarget  = new char[3][MAX_HUD_DIGITS];
    private final int[][]  hudDigitCounter = new int[3][MAX_HUD_DIGITS];
    private final int[]    hudDigitCount   = new int[3];
    private final Camera   hudFlipCamera   = new Camera();
    private final Matrix   hudFlipMatrix   = new Matrix();
    private Paint hudDividerPaint;
    private Paint hudCardBgPaint;
    private Paint hudCardBorderPaint;

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
    private int bonusIconWidthLarge;
    private int bonusSlotWidth;
    private int bonusSlotHeight;
    private int buyButtonHeight;

    private int animateBonusCounter;
    private int bonusWin;
    private int bonusWin2 = -1;
    private int bonusWin3 = -1;
    private Rect bonusWinRect;
    private Drawable bonusWinDrawable;

    private Rect undoRect;
    private Rect swapRect;
    private Rect jumpRect;
    private Rect dissolveRect;
    private Rect delLineRect;
    private Rect shiftLineRect;
    private Rect colorClearRect;
    private Rect bombRect;
    private Rect undoSlotRect;
    private Rect swapSlotRect;
    private Rect jumpSlotRect;
    private Rect dissolveSlotRect;
    private Rect delLineSlotRect;
    private Rect shiftLineSlotRect;
    private Rect colorClearSlotRect;
    private Rect bombSlotRect;
    private Rect undoBuyRect;
    private Rect swapBuyRect;
    private Rect jumpBuyRect;
    private Rect dissolveBuyRect;
    private Rect delLineBuyRect;
    private Rect shiftLineBuyRect;
    private Rect colorClearBuyRect;
    private Rect bombBuyRect;
    private Drawable undo;
    private Drawable jump;
    private Drawable swap;
    private Drawable dissolve;
    private Drawable delLine;
    private Drawable shiftLine;
    private Drawable colorClearDrawable;
    private Drawable bombIcon;
    private int undoCounter;
    private int swapCounter;
    private int jumpCounter;
    private int dissolveCounter;
    private int delLineCounter;
    private int shiftLineCounter;
    private int colorClearCounter;
    private int bombCounter;

    private boolean undoSelected;
    private boolean swapSelected;
    private boolean jumpSelected;
    private boolean dissolveSelected;
    private boolean delLineSelected;
    private boolean shiftLineSelected;
    private boolean colorClearSelected;
    private boolean bombSelected;

    private String undoText;
    private String swapText;
    private String jumpText;
    private String dissolveText;
    private String delLineText;
    private String shiftLineText;
    private String colorClearText;
    private String bombText;
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
        this.autoHighScore = prefs.getLong(PREF_KEY_AUTO_HIGHSCORE, 0);
        this.highscoreLockedByCheat = false;


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

        shiftSelectionPulsePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shiftSelectionPulsePaint.setStyle(Paint.Style.STROKE);
        shiftSelectionPulsePaint.setColor(0xfff7d774);

        bombShockwavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bombShockwavePaint.setStyle(Paint.Style.STROKE);

        statusPanelRect = new RectF();
        bonusSlotRect = new RectF();

        levelAnimationPaint = new Paint();
        levelAnimationPaint.setColor(TEXT_COLOR_WHITE);
        levelHintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        levelHintPaint.setColor(TEXT_COLOR_WHITE);

        rand = new Random();
        motionPath = new ArrayList<>();
        inverseMotionPath = new ArrayList<>();
        redPaint = new Paint();
        redPaint.setColor(0xffff0000);

        hudDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hudDividerPaint.setColor(0xff000000);
        hudDividerPaint.setStrokeWidth(2.0f);

        hudCardBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hudCardBgPaint.setStyle(Paint.Style.FILL);
        hudCardBgPaint.setColor(HUD_CARD_BG_COLOR);

        hudCardBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hudCardBorderPaint.setStyle(Paint.Style.STROKE);
        hudCardBorderPaint.setStrokeWidth(1.5f);
        hudCardBorderPaint.setColor(HUD_CARD_BORDER_COLOR);

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

        selfPlayTapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selfPlayTapPaint.setStyle(Paint.Style.STROKE);
        selfPlayTapPaint.setColor(0xFFFFFFFF);

        selfPlayIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selfPlayIndicatorPaint.setColor(0xFFFFFFFF);
        selfPlayIndicatorPaint.setFakeBoldText(true);

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
        autoHighscoreExceeded = false;

        undoCounter = 0;
        swapCounter = 0;
        jumpCounter = 0;
        dissolveCounter = 0;
        delLineCounter = 0;
        shiftLineCounter = 0;
        colorClearCounter = 0;
        bombCounter = 0;
        addBonusCount(getWeightedRandomBonusIndex(), 1);
        addBonusCount(getWeightedRandomBonusIndex(), 1);
        addBonusCount(getWeightedRandomBonusIndex(), 1);

        deselectAllBonuses();
        secretCodeProgress = new int[SECRET_CODE_SEQUENCES.length];
        secretCodeLastTapMs = 0L;
        shiftBonusValues = null;
        shiftBonusRects = null;
        shiftBonusPaints = null;
        shiftBonusTexts = null;
        lineSelectionActive = false;
        lineSelectionRow = -1;
        lineSelectionCol = -1;
        shiftSelectionPulseTick = 0;
        touchDownX = 0;
        touchDownY = 0;
        lineDissolveAnimationRunning = false;
        lineDissolveRects = null;
        lineDissolvePaints = null;
        lineDissolveTexts = null;
        colorClearAnimationRunning = false;
        colorClearRects = null;
        colorClearPaints = null;
        colorClearTexts = null;
        bombAnimationRunning = false;
        bombBlastRects = null;
        bombBlastPaints = null;
        bombBlastTexts = null;
        levelPruneAnimationRunning = false;
        levelPruneCoords = null;
        levelPruneRects = null;
        levelPrunePaints = null;
        levelPruneTexts = null;
        bonusWin2 = -1;
        bonusWin3 = -1;

        level = 1;
        levelText = "Level: " + level;
        levelHintText = getNextLevelHintText();
        updateBonusValues();
        resetHudDisplayedValues();

        // Reset self-play cycle but keep active flag
        selfPlayQueue = null;
        selfPlayDelayCounter = SELF_PLAY_MOVE_DELAY;
        selfPlayTapAnimCounter = 0;
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
        drawLineSelectionPulse(canvas);
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
        if (colorClearAnimationRunning) {
            drawColorClearAnimation(canvas);
        }
        if (bombAnimationRunning) {
            drawBombAnimation(canvas);
        }
        if (levelPruneAnimationRunning) {
            drawLevelPruneAnimation(canvas);
        }

        if (levelAnimationCounter > 0) {
            drawLevelAnimation(canvas);
        } else if (animateBonusCounter > 0) {
            drawBonusAnimation(canvas);
        }

        if (dropInAnimationRunning) {
            drawDropInAnimation(canvas);
        }

        if (selfPlayActive) {
            drawSelfPlayOverlay(canvas);
        }
    }

    private void drawSelfPlayOverlay(Canvas canvas) {
        // Tap-ripple animation
        if (selfPlayTapAnimCounter > 0) {
            float t = 1f - (float) selfPlayTapAnimCounter / SELF_PLAY_TAP_ANIM_DUR;
            float r = cellWidth * 0.52f * t;
            int   a = (int) (200 * (1f - t));
            selfPlayTapPaint.setAlpha(a);
            selfPlayTapPaint.setStrokeWidth(6f - 4f * t);
            canvas.drawCircle(selfPlayTapAnimX, selfPlayTapAnimY, r, selfPlayTapPaint);
        }

        // "AUTO" badge in top-left corner
        if (canvasWidth > 0) {
            float badgeFontSize = cellHeight * 0.18f;
            selfPlayIndicatorPaint.setTextSize(badgeFontSize);
            float badgeX = RECT_BORDER * 2f;
            float badgeY = badgeFontSize + RECT_BORDER;
            // semi-transparent background pill
            Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(0xAA000000);
            float padH = badgeFontSize * 0.4f, padV = badgeFontSize * 0.25f;
            float textW = selfPlayIndicatorPaint.measureText("AUTO");
            RectF pill = new RectF(badgeX - padH, badgeY - badgeFontSize - padV,
                                   badgeX + textW + padH, badgeY + padV);
            canvas.drawRoundRect(pill, padH, padH, bgPaint);
            selfPlayIndicatorPaint.setAlpha(255);
            canvas.drawText("AUTO", badgeX, badgeY, selfPlayIndicatorPaint);
        }
    }

    private void drawDropInAnimation(Canvas canvas) {
        canvas.drawRect(dropInRect, dropInAnimationPaint);
    }

    private void drawLineSelectionPulse(Canvas canvas) {
        if (!lineSelectionActive || status == statusT.BONUS_SHIFT_ANIMATION) {
            return;
        }

        float pulse = (float) (Math.sin((2.0f * Math.PI * shiftSelectionPulseTick) / SHIFT_SELECTION_PULSE_CYCLE) * 0.5f + 0.5f);
        shiftSelectionPulsePaint.setStrokeWidth(4.0f + 8.0f * pulse);
        shiftSelectionPulsePaint.setAlpha(96 + (int) (140.0f * pulse));

        // Draw the selected row
        for (int i = 0; i < width; i++) {
            Rect cellRect = rectArray[i][lineSelectionRow];
            if (cellRect != null) canvas.drawRect(cellRect, shiftSelectionPulsePaint);
        }
        // Draw the selected column
        for (int i = 0; i < height; i++) {
            Rect cellRect = rectArray[lineSelectionCol][i];
            if (cellRect != null) canvas.drawRect(cellRect, shiftSelectionPulsePaint);
        }
    }

    private void drawShiftBonusAnimation(Canvas canvas) {
        if (status != statusT.BONUS_SHIFT_ANIMATION || shiftBonusRects == null) {
            return;
        }
        float progress = 1.0f - (float) shiftBonusAnimationCounter / (float) SHIFT_BONUS_ANIMATION_STEPS;
        int outgoingIndex = shiftBonusDirectionSign >= 0 ? shiftBonusRects.length - 1 : 0;
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

    private void drawLevelPruneAnimation(Canvas canvas) {
        if (!levelPruneAnimationRunning || levelPruneRects == null) {
            return;
        }

        float baseProgress = 1.0f - (float) levelPruneAnimationCounter / (float) LEVEL_PRUNE_ANIMATION_DURATION;
        int length = levelPruneRects.length;
        for (int i = 0; i < length; i++) {
            Rect sourceRect = levelPruneRects[i];
            Paint sourcePaint = levelPrunePaints[i];
            if (sourceRect == null || sourcePaint == null) {
                continue;
            }

            float stagger = (length <= 1) ? 0.0f : ((float) i / (float) (length - 1)) * 0.18f;
            float progress = Math.min(1.0f, Math.max(0.0f, (baseProgress - stagger) / (1.0f - stagger + 0.0001f)));
            float pulse = (float) Math.sin(progress * Math.PI);
            float minDim = Math.min(sourceRect.width(), sourceRect.height());
            float inset = progress * minDim * 0.34f;
            float glowExpand = pulse * minDim * 0.12f;
            int alpha = Math.max(16, 255 - (int) (progress * 236.0f));

            float left = sourceRect.left + inset;
            float top = sourceRect.top + inset;
            float right = sourceRect.right - inset;
            float bottom = sourceRect.bottom - inset;

            shiftEdgeDissolvePaint.setColor(sourcePaint.getColor());
            shiftEdgeDissolvePaint.setAlpha(Math.max(22, alpha - 18));
            canvas.drawRoundRect(left, top, right, bottom, 12.0f, 12.0f, shiftEdgeDissolvePaint);

            shiftEdgeDissolvePaint.setColor(0xfffff0a8);
            shiftEdgeDissolvePaint.setAlpha(Math.max(0, 36 + (int) (pulse * 96.0f) - (int) (progress * 42.0f)));
            canvas.drawRoundRect(left - glowExpand, top - glowExpand, right + glowExpand, bottom + glowExpand,
                    14.0f, 14.0f, shiftEdgeDissolvePaint);

            shiftEdgeDissolveStrokePaint.setColor(TEXT_COLOR_WHITE);
            shiftEdgeDissolveStrokePaint.setStrokeWidth(Math.max(2.0f, (1.0f - progress) * 6.5f));
            shiftEdgeDissolveStrokePaint.setAlpha(Math.max(18, alpha - 56));
            canvas.drawLine(left, top, right, bottom, shiftEdgeDissolveStrokePaint);
            canvas.drawLine(left, bottom, right, top, shiftEdgeDissolveStrokePaint);

            String text = levelPruneTexts[i];
            if (text != null && !text.isEmpty()) {
                levelPruneDrawRect.left = (int) left;
                levelPruneDrawRect.top = (int) top;
                levelPruneDrawRect.right = (int) right;
                levelPruneDrawRect.bottom = (int) bottom;
                shiftEdgeDissolveTextPaint.setTextSize(getTextSize(text));
                shiftEdgeDissolveTextPaint.setAlpha(Math.max(12, alpha - 92));
                canvas.drawText(text,
                        getTextPosX(levelPruneDrawRect, text, shiftEdgeDissolveTextPaint),
                        getTextPosY(levelPruneDrawRect, shiftEdgeDissolveTextPaint),
                        shiftEdgeDissolveTextPaint);
            }
        }
    }

    private void drawBonusAnimation(Canvas canvas) {
        bonusWinDrawable.setBounds(bonusWinRect);
        bonusWinDrawable.draw(canvas);
    }

    private void drawLevelAnimation(Canvas canvas) {
        float mainTextSize = 3.0f * levelAnimationCounter;
        levelAnimationPaint.setTextSize(mainTextSize);
        float mainY = canvasHeight / 2.0f;
        canvas.drawText(levelText, getApproxXToCenterText(levelText, levelAnimationPaint, canvasWidth), mainY, levelAnimationPaint);

        if (levelHintText != null && !levelHintText.isEmpty()) {
            float hintTextSize = Math.max(26.0f, Math.min(56.0f, mainTextSize * 0.24f));
            levelHintPaint.setTextSize(hintTextSize);
            int hintAlpha = Math.max(110, Math.min(255, 90 + levelAnimationCounter));
            levelHintPaint.setAlpha(hintAlpha);
            float hintY = mainY + Math.max(58.0f, mainTextSize * 0.56f);
            canvas.drawText(levelHintText,
                    getApproxXToCenterText(levelHintText, levelHintPaint, canvasWidth),
                    hintY,
                    levelHintPaint);
        }
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
        statusScoreRect = new Rect((int) cardLeft, (int) cardTop, (int) (cardLeft + cardWidth), (int) cardBottom);
        drawStatusPanel(canvas, cardLeft, cardTop, cardLeft + cardWidth, cardBottom,
                "SCORE", getScoreText(score), HUD_SCORE_ACCENT_COLOR, cornerRadius, HUD_SLOT_SCORE);
        cardLeft += cardWidth + cardGap;
        statusLevelRect = new Rect((int) cardLeft, (int) cardTop, (int) (cardLeft + cardWidth), (int) cardBottom);
        drawStatusPanel(canvas, cardLeft, cardTop, cardLeft + cardWidth, cardBottom,
                "LEVEL", Integer.toString(level), HUD_LEVEL_ACCENT_COLOR, cornerRadius, HUD_SLOT_LEVEL);
        cardLeft += cardWidth + cardGap;
        statusBestRect = new Rect((int) cardLeft, (int) cardTop, (int) (cardLeft + cardWidth), (int) cardBottom);
        if (selfPlayActive) {
            int autoColor = autoHighscoreExceeded ? HUD_BEST_NEW_RECORD_ACCENT_COLOR : 0xFF66D8FF;
            drawStatusPanel(canvas, cardLeft, cardTop, cardLeft + cardWidth, cardBottom,
                    "AUTO", getScoreText(autoHighScore), autoColor, cornerRadius, HUD_SLOT_BEST);
        } else {
            int bestColor = highscoreExceeded ? HUD_BEST_NEW_RECORD_ACCENT_COLOR : HUD_BEST_ACCENT_COLOR;
            drawStatusPanel(canvas, cardLeft, cardTop, cardLeft + cardWidth, cardBottom,
                    "BEST", getScoreText(highScore), bestColor, cornerRadius, HUD_SLOT_BEST);
        }
    }

    private void drawStatusPanel(Canvas canvas, float left, float top, float right, float bottom,
                                 String label, String value, int accentColor, float cornerRadius, int hudSlot) {
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
        if (anyDigitAnimating(hudSlot)) {
            drawHudFlipValue(canvas, innerLeft, innerRight, contentTop, contentBottom,
                    hudSlot, scoreAndLevelPaint, baselineForValue);
        } else {
            drawStaticHudCards(canvas, getHudSlotDisplayString(hudSlot), innerLeft, innerRight,
                    contentTop, contentBottom, scoreAndLevelPaint, baselineForValue);
        }

        statusLabelPaint.setTextSize(defaultLabelTextSize);
        scoreAndLevelPaint.setTextSize(defaultValueTextSize);
    }

    private void resetHudDisplayedValues() {
        initHudSlot(HUD_SLOT_SCORE, getScoreText(score));
        initHudSlot(HUD_SLOT_LEVEL, Integer.toString(level));
        initHudSlot(HUD_SLOT_BEST,  getScoreText(selfPlayActive ? autoHighScore : highScore));
    }

    private void initHudSlot(int slot, String text) {
        int n = Math.min(text.length(), MAX_HUD_DIGITS);
        hudDigitCount[slot] = n;
        for (int d = 0; d < n; d++) {
            char ch = text.charAt(d);
            hudDigitCurrent[slot][d] = ch;
            hudDigitNext[slot][d]    = ch;
            hudDigitTarget[slot][d]  = ch;
            hudDigitCounter[slot][d] = 0;
        }
    }

    private void updateHudFlips() {
        updateSlotTargets(HUD_SLOT_SCORE, getScoreText(score));
        updateSlotTargets(HUD_SLOT_LEVEL, Integer.toString(level));
        updateSlotTargets(HUD_SLOT_BEST,  getScoreText(selfPlayActive ? autoHighScore : highScore));

        for (int slot = 0; slot < 3; slot++) {
            for (int d = 0; d < hudDigitCount[slot]; d++) {
                if (hudDigitCounter[slot][d] > 0) {
                    hudDigitCounter[slot][d]--;
                    if (hudDigitCounter[slot][d] == 0) {
                        hudDigitCurrent[slot][d] = hudDigitNext[slot][d];
                    }
                }
                if (hudDigitCounter[slot][d] == 0 && hudDigitCurrent[slot][d] != hudDigitTarget[slot][d]) {
                    hudDigitNext[slot][d]    = nextFlipChar(hudDigitCurrent[slot][d], hudDigitTarget[slot][d]);
                    hudDigitCounter[slot][d] = HUD_FLIP_DURATION;
                }
            }
        }
    }

    private void updateSlotTargets(int slot, String newText) {
        int newLen = Math.min(newText.length(), MAX_HUD_DIGITS);
        int nCells = Math.max(hudDigitCount[slot], newLen);

        if (nCells > hudDigitCount[slot]) {
            int shift = nCells - hudDigitCount[slot];
            for (int d = nCells - 1; d >= 0; d--) {
                int src = d - shift;
                if (src >= 0) {
                    hudDigitCurrent[slot][d] = hudDigitCurrent[slot][src];
                    hudDigitNext[slot][d]    = hudDigitNext[slot][src];
                    hudDigitTarget[slot][d]  = hudDigitTarget[slot][src];
                    hudDigitCounter[slot][d] = hudDigitCounter[slot][src];
                } else {
                    hudDigitCurrent[slot][d] = ' ';
                    hudDigitNext[slot][d]    = ' ';
                    hudDigitTarget[slot][d]  = ' ';
                    hudDigitCounter[slot][d] = 0;
                }
            }
            hudDigitCount[slot] = nCells;
        }

        for (int d = 0; d < nCells; d++) {
            int charIdx = d - (nCells - newLen);
            char targetCh = (charIdx >= 0 && charIdx < newLen) ? newText.charAt(charIdx) : ' ';
            hudDigitTarget[slot][d] = targetCh;
        }
    }

    private char nextFlipChar(char current, char target) {
        if (current >= '0' && current <= '9' && target >= '0' && target <= '9') {
            return (char) ((current - '0' + 1) % 10 + '0');
        }
        return target;
    }

    private boolean anyDigitAnimating(int slot) {
        for (int d = 0; d < hudDigitCount[slot]; d++) {
            if (hudDigitCounter[slot][d] > 0) return true;
        }
        return false;
    }

    private String getHudSlotDisplayString(int slot) {
        int n = hudDigitCount[slot];
        char[] chars = new char[n];
        for (int d = 0; d < n; d++) chars[d] = hudDigitCurrent[slot][d];
        return new String(chars);
    }

    // Returns [cardWidth, cardGap, startX] for a right-aligned card layout.
    // Cards shrink automatically if they don't fit in the available width.
    private float[] computeCardLayout(int nCells, float innerLeft, float innerRight,
                                       float contentTop, float contentBottom) {
        float contentHeight = contentBottom - contentTop;
        float cardWidth = contentHeight * HUD_CARD_ASPECT;
        float cardGap   = cardWidth * HUD_CARD_GAP_RATIO;
        float totalWidth = nCells * cardWidth + Math.max(0, nCells - 1) * cardGap;
        float avail = innerRight - innerLeft;
        if (totalWidth > avail && nCells > 0) {
            // Solve: nCells*w + (nCells-1)*w*GAP_RATIO = avail
            cardWidth = avail / (nCells + Math.max(0, nCells - 1) * HUD_CARD_GAP_RATIO);
            cardGap   = cardWidth * HUD_CARD_GAP_RATIO;
            totalWidth = nCells * cardWidth + Math.max(0, nCells - 1) * cardGap;
        }
        float startX = innerRight - totalWidth;
        return new float[]{cardWidth, cardGap, startX};
    }

    private void drawCardBackground(Canvas canvas, float left, float right,
                                     float top, float bottom, float cy) {
        canvas.drawRoundRect(left, top, right, bottom,
                HUD_CARD_CORNER, HUD_CARD_CORNER, hudCardBgPaint);
        canvas.drawRoundRect(left, top, right, bottom,
                HUD_CARD_CORNER, HUD_CARD_CORNER, hudCardBorderPaint);
        canvas.drawLine(left + 3, cy, right - 3, cy, hudDividerPaint);
    }

    private void drawStaticHudCards(Canvas canvas, String text,
                                     float innerLeft, float innerRight,
                                     float contentTop, float contentBottom,
                                     Paint valuePaint, float baseline) {
        if (text == null || text.isEmpty()) return;
        float cy = (contentTop + contentBottom) / 2.0f;
        float[] layout = computeCardLayout(text.length(), innerLeft, innerRight, contentTop, contentBottom);
        float cardWidth = layout[0];
        float cardGap   = layout[1];
        float startX    = layout[2];
        for (int i = 0; i < text.length(); i++) {
            float cLeft  = startX + i * (cardWidth + cardGap);
            float cRight = cLeft + cardWidth;
            drawCardBackground(canvas, cLeft, cRight, contentTop, contentBottom, cy);
            drawCharCentered(canvas, text.charAt(i), cLeft, cardWidth, baseline, valuePaint);
        }
    }

    // Returns the character for the i-th cell, right-aligning text within totalCells.
    private char getCharForCell(String text, int cellIndex, int totalCells) {
        int charIndex = cellIndex - (totalCells - text.length());
        if (charIndex < 0 || charIndex >= text.length()) return ' ';
        return text.charAt(charIndex);
    }

    private void drawCharCentered(Canvas canvas, char ch, float cardLeft, float cardWidth,
                                   float baseline, Paint paint) {
        if (ch == ' ') return;
        String s = String.valueOf(ch);
        float tx = cardLeft + (cardWidth - paint.measureText(s)) / 2f;
        canvas.drawText(s, tx, baseline, paint);
    }

    // Per-card split-flap animation. Each digit flips independently by one increment per flip.
    // Phase 1 (t=0..0.5): old top half folds away, new top appears underneath.
    // Phase 2 (t=0.5..1): new bottom half folds in, covering old bottom.
    private void drawHudFlipValue(Canvas canvas, float innerLeft, float innerRight,
                                   float contentTop, float contentBottom,
                                   int slot, Paint valuePaint, float baseline) {
        float cy = (contentTop + contentBottom) / 2.0f;
        int nCells = hudDigitCount[slot];

        float[] layout = computeCardLayout(nCells, innerLeft, innerRight, contentTop, contentBottom);
        float cardWidth = layout[0];
        float cardGap   = layout[1];
        float startX    = layout[2];

        for (int i = 0; i < nCells; i++) {
            float cLeft  = startX + i * (cardWidth + cardGap);
            float cRight = cLeft + cardWidth;
            float cCx    = (cLeft + cRight) / 2.0f;
            char fromCh  = hudDigitCurrent[slot][i];
            char toCh    = hudDigitNext[slot][i];
            int counter  = hudDigitCounter[slot][i];

            drawCardBackground(canvas, cLeft, cRight, contentTop, contentBottom, cy);

            if (counter <= 0) {
                drawCharCentered(canvas, fromCh, cLeft, cardWidth, baseline, valuePaint);
                continue;
            }

            float t = 1.0f - (float) counter / (float) HUD_FLIP_DURATION;

            if (t < 0.5f) {
                float angle = t * 2.0f * 90.0f;

                canvas.save();
                canvas.clipRect(cLeft, contentTop, cRight, cy);
                drawCharCentered(canvas, toCh, cLeft, cardWidth, baseline, valuePaint);
                canvas.restore();

                canvas.save();
                canvas.clipRect(cLeft, cy, cRight, contentBottom);
                drawCharCentered(canvas, fromCh, cLeft, cardWidth, baseline, valuePaint);
                canvas.restore();

                canvas.save();
                canvas.clipRect(cLeft, contentTop, cRight, cy);
                hudFlipMatrix.reset();
                hudFlipCamera.save();
                hudFlipCamera.rotateX(angle);
                hudFlipCamera.getMatrix(hudFlipMatrix);
                hudFlipCamera.restore();
                hudFlipMatrix.preTranslate(-cCx, -cy);
                hudFlipMatrix.postTranslate(cCx, cy);
                canvas.concat(hudFlipMatrix);
                drawCharCentered(canvas, fromCh, cLeft, cardWidth, baseline, valuePaint);
                canvas.restore();

            } else {
                float angle = (1.0f - t) * 2.0f * 90.0f;

                canvas.save();
                canvas.clipRect(cLeft, contentTop, cRight, cy);
                drawCharCentered(canvas, toCh, cLeft, cardWidth, baseline, valuePaint);
                canvas.restore();

                canvas.save();
                canvas.clipRect(cLeft, cy, cRight, contentBottom);
                drawCharCentered(canvas, fromCh, cLeft, cardWidth, baseline, valuePaint);
                canvas.restore();

                canvas.save();
                canvas.clipRect(cLeft, cy, cRight, contentBottom);
                hudFlipMatrix.reset();
                hudFlipCamera.save();
                hudFlipCamera.rotateX(angle);
                hudFlipCamera.getMatrix(hudFlipMatrix);
                hudFlipCamera.restore();
                hudFlipMatrix.preTranslate(-cCx, -cy);
                hudFlipMatrix.postTranslate(cCx, cy);
                canvas.concat(hudFlipMatrix);
                drawCharCentered(canvas, toCh, cLeft, cardWidth, baseline, valuePaint);
                canvas.restore();
            }
        }
    }

    private void drawBonusInformation(Canvas canvas) {
        drawBonusSlot(canvas, undo, undoRect, undoText, undoBuyRect, BUY_COST_UNDO, BONUS_ACCENT_COLORS[0], undoSelected, bonusSlotWidth);
        drawBonusSlot(canvas, dissolve, dissolveRect, dissolveText, dissolveBuyRect, BUY_COST_DISSOLVE, BONUS_ACCENT_COLORS[1], dissolveSelected, bonusSlotWidth);
        drawBonusSlot(canvas, swap, swapRect, swapText, swapBuyRect, BUY_COST_SWAP, BONUS_ACCENT_COLORS[2], swapSelected, bonusSlotWidth);
        drawBonusSlot(canvas, bombIcon, bombRect, bombText, bombBuyRect, BUY_COST_BOMB, BONUS_ACCENT_COLORS[3], bombSelected, bonusSlotWidth);
        drawBonusSlot(canvas, shiftLine, shiftLineRect, shiftLineText, shiftLineBuyRect, BUY_COST_SHIFT_LINE, BONUS_ACCENT_COLORS[4], shiftLineSelected, bonusSlotWidth);
        drawBonusSlot(canvas, jump, jumpRect, jumpText, jumpBuyRect, BUY_COST_JUMP, BONUS_ACCENT_COLORS[5], jumpSelected, bonusSlotWidth);
        drawBonusSlot(canvas, delLine, delLineRect, delLineText, delLineBuyRect, BUY_COST_DEL_LINE, BONUS_ACCENT_COLORS[6], delLineSelected, bonusSlotWidth);
        drawBonusSlot(canvas, colorClearDrawable, colorClearRect, colorClearText, colorClearBuyRect, BUY_COST_COLOR_CLEAR, BONUS_ACCENT_COLORS[7], colorClearSelected, bonusSlotWidth);
    }

    private void drawBonusSlot(Canvas canvas, Drawable icon, Rect iconRect, String countText, Rect buyRect,
                               long buyCost, int accentColor, boolean isSelected, int slotWidth) {
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
        int countOffset = getApproxXToCenterText(countText, bonusPaint, slotWidth);
        canvas.drawText(countText, iconRect.left + countOffset, iconRect.bottom + bonusPaint.getTextSize(), bonusPaint);

        canvas.drawRect(buyRect, buyBoxPaint);
        String buyText = "+1 " + getScoreText(buyCost);
        int buyOffset = getApproxXToCenterText(buyText, buyPaint, slotWidth);
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
        int maxIconSizeLarge = bonusSlotHeight - buyButtonHeight - 4 * RECT_BORDER - BONUS_TEXT_SIZE;
        bonusIconWidthLarge = Math.max(bonusIconWidth, maxIconSizeLarge);

        undoRect      = createBonusIconRect(0, 0, gridTop, bonusSlotWidth, bonusIconWidth);
        swapRect      = createBonusIconRect(0, 1, gridTop, bonusSlotWidth, bonusIconWidth);
        jumpRect      = createBonusIconRect(0, 2, gridTop, bonusSlotWidth, bonusIconWidth);
        dissolveRect  = createBonusIconRect(0, 3, gridTop, bonusSlotWidth, bonusIconWidthLarge);
        delLineRect   = createBonusIconRect(1, 0, gridTop, bonusSlotWidth, bonusIconWidthLarge);
        shiftLineRect = createBonusIconRect(1, 1, gridTop, bonusSlotWidth, bonusIconWidthLarge);
        colorClearRect= createBonusIconRect(1, 2, gridTop, bonusSlotWidth, bonusIconWidthLarge);
        bombRect      = createBonusIconRect(1, 3, gridTop, bonusSlotWidth, bonusIconWidthLarge);

        undoSlotRect = createBonusSlotRect(0, 0, gridTop, bonusSlotWidth);
        swapSlotRect = createBonusSlotRect(0, 1, gridTop, bonusSlotWidth);
        jumpSlotRect = createBonusSlotRect(0, 2, gridTop, bonusSlotWidth);
        dissolveSlotRect = createBonusSlotRect(0, 3, gridTop, bonusSlotWidth);
        delLineSlotRect = createBonusSlotRect(1, 0, gridTop, bonusSlotWidth);
        shiftLineSlotRect = createBonusSlotRect(1, 1, gridTop, bonusSlotWidth);
        colorClearSlotRect = createBonusSlotRect(1, 2, gridTop, bonusSlotWidth);
        bombSlotRect    = createBonusSlotRect(1, 3, gridTop, bonusSlotWidth);

        undoBuyRect = createBuyRectForSlot(0, 0, gridTop, bonusSlotWidth);
        swapBuyRect = createBuyRectForSlot(0, 1, gridTop, bonusSlotWidth);
        jumpBuyRect = createBuyRectForSlot(0, 2, gridTop, bonusSlotWidth);
        dissolveBuyRect = createBuyRectForSlot(0, 3, gridTop, bonusSlotWidth);
        delLineBuyRect = createBuyRectForSlot(1, 0, gridTop, bonusSlotWidth);
        shiftLineBuyRect = createBuyRectForSlot(1, 1, gridTop, bonusSlotWidth);
        colorClearBuyRect = createBuyRectForSlot(1, 2, gridTop, bonusSlotWidth);
        bombBuyRect    = createBuyRectForSlot(1, 3, gridTop, bonusSlotWidth);

        undo = ContextCompat.getDrawable(context, R.drawable.undo_circle_icon);
        if (undo != null) undo.setBounds(undoRect);

        swap = ContextCompat.getDrawable(context, R.drawable.swap_icon);
        if (swap != null) swap.setBounds(swapRect);

        jump = ContextCompat.getDrawable(context, R.drawable.jump_icon);
        if (jump != null) jump.setBounds(jumpRect);

        dissolve = ContextCompat.getDrawable(context, R.drawable.delete_block_icon);
        if (dissolve != null) dissolve.setBounds(dissolveRect);

        delLine = ContextCompat.getDrawable(context, R.drawable.delete_line_icon);
        if (delLine != null) delLine.setBounds(delLineRect);

        shiftLine = ContextCompat.getDrawable(context, R.drawable.shift_line_icon);
        if (shiftLine != null) shiftLine.setBounds(shiftLineRect);

        colorClearDrawable = ContextCompat.getDrawable(context, R.drawable.color_clear_icon);
        if (colorClearDrawable != null) colorClearDrawable.setBounds(colorClearRect);

        bombIcon = ContextCompat.getDrawable(context, R.drawable.bomb_icon);
        if (bombIcon != null) bombIcon.setBounds(bombRect);

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
        levelPruneDrawRect = new Rect();
        colorClearDrawRect = new Rect();
        bombAnimDrawRect = new Rect();
    }

    private Rect createBonusIconRect(int row, int col, int gridTop, int slotWidth, int iconWidth) {
        int slotLeft = col * slotWidth;
        int rowTop = gridTop + row * bonusSlotHeight;
        int iconLeft = slotLeft + (slotWidth - iconWidth) / 2;
        int iconTop = rowTop + RECT_BORDER;
        return new Rect(iconLeft, iconTop, iconLeft + iconWidth, iconTop + iconWidth);
    }

    private Rect createBuyRectForSlot(int row, int col, int gridTop, int slotWidth) {
        int slotLeft = col * slotWidth;
        int rowTop = gridTop + row * bonusSlotHeight;
        int buyTop = rowTop + bonusSlotHeight - buyButtonHeight - 2 * RECT_BORDER;
        return new Rect(slotLeft + RECT_BORDER, buyTop, slotLeft + slotWidth - RECT_BORDER, buyTop + buyButtonHeight);
    }

    private Rect createBonusSlotRect(int row, int col, int gridTop, int slotWidth) {
        int slotLeft = col * slotWidth;
        int rowTop = gridTop + row * bonusSlotHeight;
        return new Rect(slotLeft, rowTop + RECT_BORDER, slotLeft + slotWidth, rowTop + bonusSlotHeight + RECT_BORDER);
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
        updateHudFlips();
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
        if (colorClearAnimationRunning) {
            animateColorClearBonus();
            return;
        }
        if (bombAnimationRunning) {
            animateBombBonus();
            return;
        }
        if (levelPruneAnimationRunning) {
            animateLevelPruneAnimation();
            return;
        }
        if (status == statusT.BONUS_SHIFT_ANIMATION) {
            animateShiftBonus();
            return;
        }

        if (lineSelectionActive) {
            shiftSelectionPulseTick = (shiftSelectionPulseTick + 1) % SHIFT_SELECTION_PULSE_CYCLE;
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
        }

        if (selfPlayActive) tickSelfPlay();
        if (selfPlayTapAnimCounter > 0) selfPlayTapAnimCounter--;

    }

    // -------------------------------------------------------------------------
    // Self-play / watch mode
    // -------------------------------------------------------------------------

    public void toggleSelfPlay() {
        selfPlayActive = !selfPlayActive;
        if (selfPlayActive) {
            selfPlayQueue = null;
            selfPlayDelayCounter = SELF_PLAY_MOVE_DELAY;
            selfPlayTapAnimCounter = 0;
            if (status == statusT.SELECT_TARGET_POSITION) {
                resetCell(startPositionX, startPositionY);
                status = statusT.SELECT_START_POSITION;
            }
        }
        // Resync BEST slot to show the right high-score counter
        initHudSlot(HUD_SLOT_BEST, getScoreText(selfPlayActive ? autoHighScore : highScore));
        alertAnimationCounter = ALERT_TIME / 2;
    }

    public boolean isSelfPlayActive() { return selfPlayActive; }

    private void tickSelfPlay() {
        // Auto-restart after game over
        if (status == statusT.GAME_OVER) {
            if (--selfPlayDelayCounter <= 0) {
                onTouchEvent(MotionEvent.ACTION_DOWN, canvasWidth / 2, canvasHeight / 4);
                // gameInit() already resets selfPlayDelayCounter to SELF_PLAY_MOVE_DELAY
            }
            return;
        }

        // Execute pending action queue
        if (selfPlayQueue != null) {
            if (--selfPlayDelayCounter <= 0) {
                int[] step = selfPlayQueue[selfPlayQueueIdx];
                int px = step[0], py = step[1], action = step[2], nextDelay = step[3];
                triggerSelfPlayTapAnim(px, py);
                onTouchEvent(action, px, py);
                selfPlayQueueIdx++;
                if (selfPlayQueueIdx >= selfPlayQueue.length) {
                    selfPlayQueue = null;
                    selfPlayDelayCounter = SELF_PLAY_MOVE_DELAY;
                } else {
                    selfPlayDelayCounter = nextDelay;
                }
            }
            return;
        }

        // Compute next decision when idle
        if (status == statusT.SELECT_START_POSITION) {
            if (--selfPlayDelayCounter <= 0) {
                int[] bonusCounts = getBonusCounts();
                int freeCells = gameBoardArray.getNumFreeCells();
                SelfPlayBot.BotDecision dec = SelfPlayBot.computeDecision(
                        getBoardSnapshot(), width, height, bonusCounts, freeCells);
                if (dec != null) {
                    selfPlayQueue = buildActionQueue(dec);
                    selfPlayQueueIdx = 0;
                    selfPlayDelayCounter = 0;
                } else {
                    selfPlayDelayCounter = SELF_PLAY_MOVE_DELAY;
                }
            }
        }
    }

    /** Converts a BotDecision into a sequence of {pixelX, pixelY, action, delayFrames}. */
    private int[][] buildActionQueue(SelfPlayBot.BotDecision dec) {
        int gap = SELF_PLAY_TAP_GAP;
        int dn  = MotionEvent.ACTION_DOWN;
        int up  = MotionEvent.ACTION_UP;
        switch (dec.type) {
            case NORMAL:
                return new int[][]{
                    {gridToPixelX(dec.p1x), gridToPixelY(dec.p1y), dn, gap},
                    {gridToPixelX(dec.p2x), gridToPixelY(dec.p2y), dn, 0}};
            case JUMP: {
                Rect slot = getSlotRectByIndex(SelfPlayBot.IDX_JUMP);
                return new int[][]{
                    {slot.centerX(), slot.centerY(), dn, gap},
                    {gridToPixelX(dec.p1x), gridToPixelY(dec.p1y), dn, gap},
                    {gridToPixelX(dec.p2x), gridToPixelY(dec.p2y), dn, 0}};
            }
            case DISSOLVE: {
                Rect slot = getSlotRectByIndex(SelfPlayBot.IDX_DISSOLVE);
                return new int[][]{
                    {slot.centerX(), slot.centerY(), dn, gap},
                    {gridToPixelX(dec.p1x), gridToPixelY(dec.p1y), dn, 0}};
            }
            case BOMB: {
                Rect slot = getSlotRectByIndex(SelfPlayBot.IDX_BOMB);
                return new int[][]{
                    {slot.centerX(), slot.centerY(), dn, gap},
                    {gridToPixelX(dec.p1x), gridToPixelY(dec.p1y), dn, 0}};
            }
            case COLOR_CLEAR: {
                Rect slot = getSlotRectByIndex(SelfPlayBot.IDX_COLOR_CLEAR);
                return new int[][]{
                    {slot.centerX(), slot.centerY(), dn, gap},
                    {gridToPixelX(dec.p1x), gridToPixelY(dec.p1y), dn, 0}};
            }
            case DEL_LINE: {
                Rect slot = getSlotRectByIndex(SelfPlayBot.IDX_DEL_LINE);
                // dec.p1x = lineIdx, dec.flag = isRow
                // Tap any cell in the row/col, then swipe to trigger
                int tapGx = dec.flag ? 0 : dec.p1x;
                int tapGy = dec.flag ? dec.p1x : 0;
                int tapPx = gridToPixelX(tapGx);
                int tapPy = gridToPixelY(tapGy);
                int swipe = (int) (dec.flag ? cellWidth * 0.65f : cellHeight * 0.65f);
                int upPx  = dec.flag ? tapPx + swipe : tapPx;
                int upPy  = dec.flag ? tapPy : tapPy + swipe;
                return new int[][]{
                    {slot.centerX(), slot.centerY(), dn, gap},
                    {tapPx, tapPy, dn, gap},
                    {upPx,  upPy,  up, 0}};
            }
        }
        return null;
    }

    private int[] getBonusCounts() {
        return new int[]{
            undoCounter, dissolveCounter, swapCounter, bombCounter,
            shiftLineCounter, jumpCounter, delLineCounter, colorClearCounter
        };
    }

    private Rect getSlotRectByIndex(int idx) {
        switch (idx) {
            case SelfPlayBot.IDX_UNDO:        return undoSlotRect;
            case SelfPlayBot.IDX_DISSOLVE:    return dissolveSlotRect;
            case SelfPlayBot.IDX_SWAP:        return swapSlotRect;
            case SelfPlayBot.IDX_BOMB:        return bombSlotRect;
            case SelfPlayBot.IDX_SHIFT_LINE:  return shiftLineSlotRect;
            case SelfPlayBot.IDX_JUMP:        return jumpSlotRect;
            case SelfPlayBot.IDX_DEL_LINE:    return delLineSlotRect;
            case SelfPlayBot.IDX_COLOR_CLEAR: return colorClearSlotRect;
        }
        return null;
    }

    private void triggerSelfPlayTapAnim(int px, int py) {
        selfPlayTapAnimX = px;
        selfPlayTapAnimY = py;
        selfPlayTapAnimCounter = SELF_PLAY_TAP_ANIM_DUR;
    }

    private int gridToPixelX(int gx) { return (int) (gx * cellWidth  + cellWidth  / 2f); }
    private int gridToPixelY(int gy) { return (int) (gy * cellHeight + cellHeight / 2f); }

    private int[][] getBoardSnapshot() {
        int[][] snap = new int[width][height];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                snap[x][y] = gameBoardArray.get(x, y);
        return snap;
    }

    private void handleNewCellDropIns() {
        if (dropInCount > 0 && !dropInAnimationRunning) {

            Coord c = gameBoardArray.randomlyAddCell(getCurrentMinSpawnIndex(), getCurrentMaxSpawnIndex());

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
                    if (boardWideMergeScanActive && startBoardWideMergeIfAvailable()) {
                        return;
                    }
                    boardWideMergeScanActive = false;
                    finalizeChainScoreIfNeeded();
                    status = statusT.DROP_IN_NEW_CELLS;
                }
            }
            int freeCells = gameBoardArray.getNumFreeCells();
            if (freeCells == 0 && !(boardWideMergeScanActive && status == statusT.MERGE)) {
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

    private void startLevelPruneAnimation(List<Coord> cellsToRemove) {
        levelPruneAnimationRunning = true;
        levelPruneAnimationCounter = LEVEL_PRUNE_ANIMATION_DURATION;

        int length = cellsToRemove.size();
        levelPruneCoords = new Coord[length];
        levelPruneRects = new Rect[length];
        levelPrunePaints = new Paint[length];
        levelPruneTexts = new String[length];

        for (int i = 0; i < length; i++) {
            Coord coord = cellsToRemove.get(i);
            levelPruneCoords[i] = new Coord(coord);
            levelPruneRects[i] = new Rect(rectArray[coord.x][coord.y]);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setColor(paintArray[coord.x][coord.y].getColor());
            levelPrunePaints[i] = p;
            levelPruneTexts[i] = getText(coord.x, coord.y);
        }
    }

    private void animateLevelPruneAnimation() {
        levelPruneAnimationCounter--;
        if (levelPruneAnimationCounter <= 0) {
            finishLevelPruneAnimation();
        }
    }

    private void finishLevelPruneAnimation() {
        if (levelPruneCoords != null) {
            for (Coord coord : levelPruneCoords) {
                if (coord == null) {
                    continue;
                }
                gameBoardArray.set(coord.x, coord.y, -1);
                paintArray[coord.x][coord.y].setColor(getColor(coord.x, coord.y));
            }
        }

        levelPruneAnimationRunning = false;
        levelPruneCoords = null;
        levelPruneRects = null;
        levelPrunePaints = null;
        levelPruneTexts = null;

        if (status == statusT.SELECT_TARGET_POSITION
                && gameBoardArray.get(startPositionX, startPositionY) == -1) {
            resetCell(startPositionX, startPositionY);
            status = statusT.SELECT_START_POSITION;
        }

        ensureBoardCanRefillIfEmpty();
    }

    private void finishLineDissolveAnimation() {
        lineDissolveAnimationRunning = false;
        lineDissolveRects = null;
        lineDissolvePaints = null;
        lineDissolveTexts = null;

        delLineCounter--;
        delLineSelected = false;
        clearLineSelection();

        continueAfterBonusBoardMutation();
        updateBonusValues();
    }

    private void startColorClearAnimation(int targetValue) {
        java.util.ArrayList<Rect> rects = new java.util.ArrayList<>();
        java.util.ArrayList<Paint> paints = new java.util.ArrayList<>();
        java.util.ArrayList<String> texts = new java.util.ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (gameBoardArray.get(x, y) == targetValue) {
                    rects.add(new Rect(rectArray[x][y]));
                    Paint p = new Paint();
                    p.setColor(paintArray[x][y].getColor());
                    paints.add(p);
                    texts.add(getText(x, y));
                    gameBoardArray.set(x, y, -1);
                    paintArray[x][y].setColor(getColor(x, y));
                }
            }
        }

        colorClearRects = rects.toArray(new Rect[0]);
        colorClearPaints = paints.toArray(new Paint[0]);
        colorClearTexts = texts.toArray(new String[0]);
        colorClearAnimationRunning = true;
        colorClearAnimationCounter = LINE_DISSOLVE_ANIMATION_DURATION;
    }

    private void animateColorClearBonus() {
        colorClearAnimationCounter--;
        if (colorClearAnimationCounter <= 0) {
            finishColorClearAnimation();
        }
    }

    private void finishColorClearAnimation() {
        colorClearAnimationRunning = false;
        colorClearRects = null;
        colorClearPaints = null;
        colorClearTexts = null;
        colorClearCounter--;
        colorClearSelected = false;
        continueAfterBonusBoardMutation();
        updateBonusValues();
    }

    private void drawColorClearAnimation(Canvas canvas) {
        if (!colorClearAnimationRunning || colorClearRects == null) {
            return;
        }
        float baseProgress = 1.0f - (float) colorClearAnimationCounter / (float) LINE_DISSOLVE_ANIMATION_DURATION;
        int length = colorClearRects.length;
        for (int i = 0; i < length; i++) {
            Rect sourceRect = colorClearRects[i];
            Paint sourcePaint = colorClearPaints[i];
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

            String text = colorClearTexts[i];
            if (text != null && !text.isEmpty()) {
                colorClearDrawRect.set((int) left, (int) top, (int) right, (int) bottom);
                shiftEdgeDissolveTextPaint.setTextSize(getTextSize(text));
                shiftEdgeDissolveTextPaint.setAlpha(Math.max(16, alpha - 90));
                canvas.drawText(text,
                        getTextPosX(colorClearDrawRect, text, shiftEdgeDissolveTextPaint),
                        getTextPosY(colorClearDrawRect, shiftEdgeDissolveTextPaint),
                        shiftEdgeDissolveTextPaint);
            }
        }
    }

    private void startBombAnimation(int cx, int cy) {
        java.util.ArrayList<int[]> cells = new java.util.ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = cx + dx, ny = cy + dy;
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    int val = gameBoardArray.get(nx, ny);
                    if (val != -1) {
                        cells.add(new int[]{nx, ny, paintArray[nx][ny].getColor()});
                        gameBoardArray.set(nx, ny, -1);
                        paintArray[nx][ny].setColor(getColor(nx, ny));
                    }
                }
            }
        }

        int n = cells.size();
        bombBlastRects  = new Rect[n];
        bombBlastPaints = new Paint[n];
        bombBlastTexts  = new String[n];
        for (int i = 0; i < n; i++) {
            int bx = cells.get(i)[0], by = cells.get(i)[1];
            bombBlastRects[i] = new Rect(rectArray[bx][by]);
            Paint p = new Paint();
            p.setColor(cells.get(i)[2]);
            bombBlastPaints[i] = p;
            bombBlastTexts[i]  = getText(bx, by);
        }

        Rect centerRect = rectArray[cx][cy];
        if (centerRect != null) {
            bombCenterPixelX = centerRect.centerX();
            bombCenterPixelY = centerRect.centerY();
        } else {
            bombCenterPixelX = (int) ((cx + 0.5f) * cellWidth);
            bombCenterPixelY = (int) ((cy + 0.5f) * cellHeight);
        }

        bombAnimationRunning  = true;
        bombAnimationCounter  = BOMB_ANIMATION_STEPS;
        bombCounter--;
        bombSelected = false;
    }

    private void animateBombBonus() {
        bombAnimationCounter--;
        if (bombAnimationCounter <= 0) {
            finishBombAnimation();
        }
    }

    private void finishBombAnimation() {
        bombAnimationRunning = false;
        bombBlastRects  = null;
        bombBlastPaints = null;
        bombBlastTexts  = null;
        continueAfterBonusBoardMutation();
        updateBonusValues();
    }

    private void drawBombAnimation(Canvas canvas) {
        if (!bombAnimationRunning) return;
        float t = 1.0f - (float) bombAnimationCounter / (float) BOMB_ANIMATION_STEPS;

        // Blast cells: flash orange then shrink away
        if (bombBlastRects != null) {
            for (int i = 0; i < bombBlastRects.length; i++) {
                Rect r = bombBlastRects[i];
                // Flash peak at t=0.15: triangle shape 0→1→0 over t=0..0.30
                float flashRaw = Math.max(0.0f, 1.0f - Math.abs(t - 0.15f) / 0.15f);
                // Shrink from t=0.20 to t=0.85
                float shrinkT = Math.max(0.0f, Math.min(1.0f, (t - 0.20f) / 0.65f));
                shrinkT = shrinkT * shrinkT; // ease-in
                int inset = (int) (shrinkT * r.width() * 0.5f);
                int left = r.left + inset, top = r.top + inset;
                int right = r.right - inset, bottom = r.bottom - inset;
                if (right <= left || bottom <= top) continue;

                int cellAlpha = (int) ((1.0f - shrinkT) * 255);
                int origColor = bombBlastPaints[i].getColor();
                int blendedColor = blendColors(origColor, 0xFFFFDD44, flashRaw * 0.88f);
                bombAnimDrawRect.set(left, top, right, bottom);
                bombBlastPaints[i].setColor(blendedColor);
                bombBlastPaints[i].setAlpha(cellAlpha);
                canvas.drawRect(bombAnimDrawRect, bombBlastPaints[i]);

                if (shrinkT < 0.45f && bombBlastTexts[i] != null && !bombBlastTexts[i].isEmpty()) {
                    shiftEdgeDissolveTextPaint.setTextSize(getTextSize(bombBlastTexts[i]));
                    shiftEdgeDissolveTextPaint.setAlpha(Math.max(0, cellAlpha - 60));
                    canvas.drawText(bombBlastTexts[i],
                            getTextPosX(bombAnimDrawRect, bombBlastTexts[i], shiftEdgeDissolveTextPaint),
                            getTextPosY(bombAnimDrawRect, shiftEdgeDissolveTextPaint),
                            shiftEdgeDissolveTextPaint);
                }

                bombBlastPaints[i].setColor(origColor);
                bombBlastPaints[i].setAlpha(255);
            }
        }

        // Shockwave ring 1: orange, fast
        if (t > 0.05f) {
            float r1t = Math.min((t - 0.05f) / 0.55f, 1.0f);
            float r1radius = r1t * (float) Math.sqrt(cellWidth * cellWidth + cellHeight * cellHeight) * 2.4f;
            bombShockwavePaint.setColor(0xFFFF7B35);
            bombShockwavePaint.setAlpha((int) ((1.0f - r1t) * 230));
            bombShockwavePaint.setStrokeWidth(Math.max(2.0f, 14.0f * (1.0f - r1t)));
            canvas.drawCircle(bombCenterPixelX, bombCenterPixelY, r1radius, bombShockwavePaint);
        }

        // Shockwave ring 2: yellow, slower
        if (t > 0.18f) {
            float r2t = Math.min((t - 0.18f) / 0.72f, 1.0f);
            float r2radius = r2t * (float) Math.sqrt(cellWidth * cellWidth + cellHeight * cellHeight) * 2.8f;
            bombShockwavePaint.setColor(0xFFFFEE00);
            bombShockwavePaint.setAlpha((int) ((1.0f - r2t) * 160));
            bombShockwavePaint.setStrokeWidth(Math.max(1.5f, 8.0f * (1.0f - r2t)));
            canvas.drawCircle(bombCenterPixelX, bombCenterPixelY, r2radius, bombShockwavePaint);
        }
    }

    private static int blendColors(int c1, int c2, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int r = (int) (((c1 >> 16) & 0xFF) * (1.0f - t) + ((c2 >> 16) & 0xFF) * t);
        int g = (int) (((c1 >>  8) & 0xFF) * (1.0f - t) + ((c2 >>  8) & 0xFF) * t);
        int b = (int) (( c1        & 0xFF) * (1.0f - t) + ( c2        & 0xFF) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private void startShiftBonusAnimation(boolean isRow, int lineIndex, int directionSign) {
        shiftBonusAnimationIsRow = isRow;
        shiftBonusLineIndex = lineIndex;
        shiftBonusDirectionSign = directionSign >= 0 ? 1 : -1;
        shiftBonusAnimationCounter = SHIFT_BONUS_ANIMATION_STEPS;
        shiftBonusOffsetX = 0.0f;
        shiftBonusOffsetY = 0.0f;
        shiftBonusStepX = isRow ? shiftBonusDirectionSign * cellWidth / SHIFT_BONUS_ANIMATION_STEPS : 0.0f;
        shiftBonusStepY = isRow ? 0.0f : shiftBonusDirectionSign * cellHeight / SHIFT_BONUS_ANIMATION_STEPS;

        clearLineSelection();

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
        if (shiftBonusDirectionSign >= 0) {
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
        } else {
            for (int i = 0; i < length - 1; i++) {
                int shiftedValue = shiftBonusValues[i + 1];
                if (shiftedValue == -1) {
                    continue;
                }
                int x = shiftBonusAnimationIsRow ? i : shiftBonusLineIndex;
                int y = shiftBonusAnimationIsRow ? shiftBonusLineIndex : i;
                gameBoardArray.set(x, y, shiftedValue);
                paintArray[x][y].setColor(getColor(x, y));
            }
        }

        shiftLineCounter--;
        shiftLineSelected = false;

        shiftBonusValues = null;
        shiftBonusRects = null;
        shiftBonusPaints = null;
        shiftBonusTexts = null;
        shiftBonusDirectionSign = 1;
        continueAfterBonusBoardMutation();
        updateBonusValues();
    }

    private void finishDissolveAnimation() {
        dissolveAnimationRunning = false;
        gameBoardArray.set(dissolveTargetX, dissolveTargetY, -1);
        paintArray[dissolveTargetX][dissolveTargetY].setColor(getColor(dissolveTargetX, dissolveTargetY));
        dissolveCounter--;
        dissolveSelected = false;
        continueAfterBonusBoardMutation();
        updateBonusValues();
    }

    private boolean handleSecretBonusCodeTap(int x, int y) {
        int tappedCard = getStatusCardTapIndex(x, y);
        if (tappedCard == -1) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (now - secretCodeLastTapMs > SECRET_CODE_TAP_TIMEOUT_MS) {
            resetSecretCodeProgress();
        }
        secretCodeLastTapMs = now;

        for (int i = 0; i < SECRET_CODE_SEQUENCES.length; i++) {
            int[] sequence = SECRET_CODE_SEQUENCES[i];
            secretCodeProgress[i] = advanceSecretCodeProgress(secretCodeProgress[i], sequence, tappedCard);
            if (secretCodeProgress[i] == sequence.length) {
                resetSecretCodeProgress();
                if (i == 0) {
                    grantAllBonusesForTesting();
                } else if (i == 1) {
                    grantLevelUpTestPoints();
                } else if (i == 2) {
                    resetHighscore();
                } else {
                    toggleSelfPlay();
                }
                return true;
            }
        }
        return true;
    }

    private int advanceSecretCodeProgress(int currentProgress, int[] sequence, int tappedCard) {
        if (sequence[currentProgress] == tappedCard) {
            return currentProgress + 1;
        }
        return (sequence[0] == tappedCard) ? 1 : 0;
    }

    private void resetSecretCodeProgress() {
        if (secretCodeProgress == null || secretCodeProgress.length != SECRET_CODE_SEQUENCES.length) {
            secretCodeProgress = new int[SECRET_CODE_SEQUENCES.length];
            return;
        }
        for (int i = 0; i < secretCodeProgress.length; i++) {
            secretCodeProgress[i] = 0;
        }
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
        lockHighscoreByCheatIfNeeded();
        // Subtle feedback that the hidden code was accepted.
        alertAnimationCounter = ALERT_TIME / 2;
    }

    private void grantLevelUpTestPoints() {
        score = safeAdd(score, SECRET_CODE_SCORE_BOOST_AMOUNT);
        lockHighscoreByCheatIfNeeded();
        applyProgressionAfterScoreChange();
        // Reuse alert pulse as subtle confirmation for hidden test command.
        alertAnimationCounter = ALERT_TIME / 2;
    }

    private void resetHighscore() {
        highScore = 0;
        highscoreExceeded = false;
        prefs.edit().putLong(PREF_KEY_HIGHSCORE, 0).apply();
        alertAnimationCounter = ALERT_TIME / 2;
    }

    private void lockHighscoreByCheatIfNeeded() {
        if (!highscoreLockedByCheat) {
            highscoreLockedByCheat = true;
            highscoreExceeded = false;
        }
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
        if (mergePaint == null) mergePaint = new Paint();
        mergePaint.setColor(getColor(targetPositionX, targetPositionY));
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
         if (gameBoardArrayValueAfterMotion > getCurrentMaxSpawnIndex()) {
             comboScore = safeMultiply(comboScore, 4L);
         }
         if (chainLength == 0) {
             chainScoreProduct = comboScore;
         } else {
             chainScoreProduct = safeMultiply(chainScoreProduct, comboScore);
         }
         chainLength++;

         long liveScore = safeAdd(score, chainScoreProduct);
         if (!highscoreLockedByCheat && liveScore > highScore) {
             highScore = liveScore;
             highscoreExceeded = true;
             prefs.edit().putLong(PREF_KEY_HIGHSCORE, highScore).apply();
         }
         if (selfPlayActive && liveScore > autoHighScore) {
             autoHighScore = liveScore;
             autoHighscoreExceeded = true;
             prefs.edit().putLong(PREF_KEY_AUTO_HIGHSCORE, autoHighScore).apply();
         }
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
        if (bonusWin2 >= 0) {
            bonusWin = bonusWin2;
            bonusWin2 = -1;
            startBonusAnimation();
        } else if (bonusWin3 >= 0) {
            bonusWin = bonusWin3;
            bonusWin3 = -1;
            startBonusAnimation();
        }
    }

    private void startLevelAnimation() {
        levelAnimationCounter = LEVEL_ANIMATION_DURATION;
        levelText = "Level: " + level;
        levelHintText = getNextLevelHintText();
    }

    private String getNextLevelHintText() {
        int nextLevel = level + 1;
        long targetScore = getLevelThreshold(nextLevel);
        long remainingScore = Math.max(0L, targetScore - score);
        return "Next L" + nextLevel + " at " + getScoreText(targetScore) + " (" + getScoreText(remainingScore) + " left)";
    }

    private void animateLevel() {
        levelAnimationCounter--;
        if (levelAnimationCounter == 0) {
            finishLevelAnimation();
        }
    }

    private void finishLevelAnimation() {
        updateBonusValues();
        ensureBoardCanRefillIfEmpty();
    }

    private int getCurrentMinSpawnIndex() {
        return LevelProgression.getMinSpawnIndex(level);
    }

    private int getCurrentMaxSpawnIndex() {
        return LevelProgression.getMaxSpawnIndex(level);
    }

    private void applyCurrentLevelProgression() {
        int minSpawnIndex = getCurrentMinSpawnIndex();
        List<Coord> cellsToRemove = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = gameBoardArray.get(x, y);
                if (value != -1 && value < minSpawnIndex) {
                    cellsToRemove.add(new Coord(x, y));
                }
            }
        }

        if (cellsToRemove.isEmpty()) {
            ensureBoardCanRefillIfEmpty();
            return;
        }

        startLevelPruneAnimation(cellsToRemove);
    }

    private void ensureBoardCanRefillIfEmpty() {
        if (gameBoardArray.getNumFreeCells() != width * height) {
            return;
        }

        dropInCount = Math.max(dropInCount, DROP_INS_AFTER_MOTION);
        if (status == statusT.SELECT_TARGET_POSITION) {
            resetCell(startPositionX, startPositionY);
            status = statusT.SELECT_START_POSITION;
        }
        if (status == statusT.SELECT_START_POSITION) {
            status = statusT.DROP_IN_NEW_CELLS;
        }
    }


    private void updateBonusValues() {
        undoText   = Integer.toString(undoCounter);
        swapText   = Integer.toString(swapCounter);
        jumpText   = Integer.toString(jumpCounter);
        dissolveText = Integer.toString(dissolveCounter);
        delLineText  = Integer.toString(delLineCounter);
        shiftLineText = Integer.toString(shiftLineCounter);
        colorClearText = Integer.toString(colorClearCounter);
        bombText     = Integer.toString(bombCounter);
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
        for (int i = 0; i < BONUS_BUY_COSTS.length; i++) {
            totalWeight += getBonusWeight(i);
        }

        double roll = rand.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0d;
        for (int i = 0; i < BONUS_BUY_COSTS.length; i++) {
            cumulativeWeight += getBonusWeight(i);
            if (roll <= cumulativeWeight) {
                return i;
            }
        }
        return BONUS_BUY_COSTS.length - 1;
    }

    private double getBonusWeight(int index) {
        int count = getBonusCount(index);
        return 1.0d / ((double) BONUS_BUY_COSTS[index] * (1.0d + BONUS_COUNT_DAMPING * count));
    }

    private int getBonusCount(int index) {
        switch (index) {
            case 0: return undoCounter;
            case 1: return dissolveCounter;
            case 2: return swapCounter;
            case 3: return bombCounter;
            case 4: return shiftLineCounter;
            case 5: return jumpCounter;
            case 6: return delLineCounter;
            case 7: return colorClearCounter;
            default: return 0;
        }
    }

    private void addBonusCount(int bonusIndex, int amount) {
        switch (bonusIndex) {
            case 0:
                undoCounter += amount;
                break;
            case 1:
                dissolveCounter += amount;
                break;
            case 2:
                swapCounter += amount;
                break;
            case 3:
                bombCounter += amount;
                break;
            case 4:
                shiftLineCounter += amount;
                break;
            case 5:
                jumpCounter += amount;
                break;
            case 6:
                delLineCounter += amount;
                break;
            case 7:
                colorClearCounter += amount;
                break;
            default:
                break;
        }
    }

    private void awardRandomBonus() {
        bonusWin = getWeightedRandomBonusIndex();
        addBonusCount(bonusWin, BONUS_AWARD_AMOUNT);
        // Draw second bonus after updating the count so the damping
        // naturally reduces the chance of receiving the same one again.
        bonusWin2 = getWeightedRandomBonusIndex();
        addBonusCount(bonusWin2, BONUS_AWARD_AMOUNT);
        bonusWin3 = getWeightedRandomBonusIndex();
        addBonusCount(bonusWin3, BONUS_AWARD_AMOUNT);
        startBonusAnimation();
    }

    private void applyProgressionAfterScoreChange() {
        boolean leveledUp = false;
        while (score >= getLevelThreshold(level + 1)) {
            level++;
            leveledUp = true;
        }
        if (leveledUp) {
            applyCurrentLevelProgression();
            startLevelAnimation();
        }

        if (!highscoreLockedByCheat && score > highScore) {
            highScore = score;
            highscoreExceeded = true;
            prefs.edit().putLong(PREF_KEY_HIGHSCORE, highScore).apply();
        }
        if (selfPlayActive && score > autoHighScore) {
            autoHighScore = score;
            autoHighscoreExceeded = true;
            prefs.edit().putLong(PREF_KEY_AUTO_HIGHSCORE, autoHighScore).apply();
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
        onTouchEvent(MotionEvent.ACTION_DOWN, x, y);
    }

    public void onTouchEvent(int action, int x, int y) {

        if (action == MotionEvent.ACTION_DOWN) {
            touchDownX = x;
            touchDownY = y;
        }

        if (action == MotionEvent.ACTION_UP) {
            if (tryHandleShiftSwipeGesture(x, y)) {
                updateBonusValues();
                return;
            }
            return;
        }

        if (action != MotionEvent.ACTION_DOWN) {
            return;
        }

        if (dissolveAnimationRunning || lineDissolveAnimationRunning || colorClearAnimationRunning
                || bombAnimationRunning || levelPruneAnimationRunning
                || status == statusT.BONUS_SHIFT_ANIMATION) {
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

    private boolean tryHandleShiftSwipeGesture(int x, int y) {
        if (!lineSelectionActive || status != statusT.SELECT_START_POSITION) {
            return false;
        }

        int deltaX = x - touchDownX;
        int deltaY = y - touchDownY;
        float minSwipeX = Math.max(28.0f, cellWidth * SHIFT_SELECTION_SWIPE_THRESHOLD_CELLS);
        float minSwipeY = Math.max(28.0f, cellHeight * SHIFT_SELECTION_SWIPE_THRESHOLD_CELLS);

        boolean horizDominant = Math.abs(deltaX) >= Math.abs(deltaY) && Math.abs(deltaX) >= minSwipeX;
        boolean vertDominant  = Math.abs(deltaY) >  Math.abs(deltaX) && Math.abs(deltaY) >= minSwipeY;

        if (!horizDominant && !vertDominant) {
            return false;
        }

        if (delLineSelected) {
            if (horizDominant) {
                startLineDissolveAnimation(true, lineSelectionRow);
            } else {
                startLineDissolveAnimation(false, lineSelectionCol);
            }
            return true;
        }

        if (shiftLineSelected) {
            if (horizDominant) {
                int directionSign = deltaX > 0 ? 1 : -1;
                startShiftBonusAnimation(true, lineSelectionRow, directionSign);
            } else {
                int directionSign = deltaY > 0 ? 1 : -1;
                startShiftBonusAnimation(false, lineSelectionCol, directionSign);
            }
            return true;
        }

        return false;
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

        if (delLineSelected) {
            selectLineForBonus(indexX, indexY);
            return true;
        }

        if (shiftLineSelected) {
            selectLineForBonus(indexX, indexY);
            return true;
        }

        if (colorClearSelected) {
            if (cellValue != -1) {
                startColorClearAnimation(cellValue);
            } else {
                alertAnimationCounter = ALERT_TIME;
            }
            return true;
        }

        if (bombSelected) {
            startBombAnimation(indexX, indexY);
            return true;
        }

        return false;
    }

    private void selectLineForBonus(int indexX, int indexY) {
        lineSelectionActive = true;
        lineSelectionRow = indexY;
        lineSelectionCol = indexX;
        shiftSelectionPulseTick = 0;
    }

    private void clearLineSelection() {
        lineSelectionActive = false;
        lineSelectionRow = -1;
        lineSelectionCol = -1;
        shiftSelectionPulseTick = 0;
    }

    private void startDropInsIfBoardEmpty() {
        if (gameBoardArray.getNumFreeCells() == width * height) {
            status = statusT.DROP_IN_NEW_CELLS;
            dropInCount = DROP_INS_AFTER_MOTION;
        } else {
            status = statusT.SELECT_START_POSITION;
        }
    }

    private void continueAfterBonusBoardMutation() {
        boardWideMergeScanActive = true;
        if (!startBoardWideMergeIfAvailable()) {
            boardWideMergeScanActive = false;
            startDropInsIfBoardEmpty();
        }
    }

    private boolean startBoardWideMergeIfAvailable() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (gameBoardArray.get(x, y) == -1) {
                    continue;
                }
                Set<Coord> group = merge(x, y);
                if (group.size() >= MIN_COMBO_SIZE) {
                    targetPositionX = x;
                    targetPositionY = y;
                    status = statusT.MERGE;
                    return true;
                }
            }
        }
        return false;
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
        if (delLineBuyRect.contains(x, y)) {
            if (tryBuyBonus(delLineBuyRect, x, y, BUY_COST_DEL_LINE)) {
                delLineCounter++;
            }
            return true;
        }
        if (shiftLineBuyRect.contains(x, y)) {
            if (tryBuyBonus(shiftLineBuyRect, x, y, BUY_COST_SHIFT_LINE)) {
                shiftLineCounter++;
            }
            return true;
        }
        if (colorClearBuyRect.contains(x, y)) {
            if (tryBuyBonus(colorClearBuyRect, x, y, BUY_COST_COLOR_CLEAR)) {
                colorClearCounter++;
            }
            return true;
        }
        if (bombBuyRect.contains(x, y)) {
            if (tryBuyBonus(bombBuyRect, x, y, BUY_COST_BOMB)) {
                bombCounter++;
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
        if (delLineSlotRect.contains(x, y) && delLineCounter > 0) {
            leaveTargetSelectionMode();
            boolean prev = delLineSelected;
            deselectAllBonuses();
            delLineSelected = !prev;
            return true;
        }
        if (shiftLineSlotRect.contains(x, y) && shiftLineCounter > 0) {
            leaveTargetSelectionMode();
            boolean prev = shiftLineSelected;
            deselectAllBonuses();
            shiftLineSelected = !prev;
            return true;
        }
        if (colorClearSlotRect.contains(x, y) && colorClearCounter > 0) {
            leaveTargetSelectionMode();
            boolean prev = colorClearSelected;
            deselectAllBonuses();
            colorClearSelected = !prev;
            return true;
        }
        if (bombSlotRect.contains(x, y) && bombCounter > 0) {
            leaveTargetSelectionMode();
            boolean prev = bombSelected;
            deselectAllBonuses();
            bombSelected = !prev;
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
    public void deselectAllBonuses() {
        undoSelected   = false;
        swapSelected   = false;
        jumpSelected   = false;
        dissolveSelected = false;
        delLineSelected = false;
        shiftLineSelected = false;
        colorClearSelected = false;
        bombSelected = false;
        clearLineSelection();
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

    public int getBuyButtonIndexAt(int x, int y) {
        if (undoBuyRect == null) return -1;
        Rect[] buys = {
            undoBuyRect, dissolveBuyRect, swapBuyRect, bombBuyRect,
            shiftLineBuyRect, jumpBuyRect, delLineBuyRect, colorClearBuyRect
        };
        for (int i = 0; i < buys.length; i++) {
            if (buys[i] != null && buys[i].contains(x, y)) return i;
        }
        return -1;
    }

    public boolean performBuyForIndex(int index) {
        long cost = getBonusCost(index);
        if (score < cost) {
            alertAnimationCounter = ALERT_TIME;
            return false;
        }
        score -= cost;
        addBonusCount(index, 1);
        updateBonusValues();
        return true;
    }

    public int getBonusSlotIndexAt(int x, int y) {
        if (undoSlotRect == null) return -1;
        Rect[] slots = {
            undoSlotRect, dissolveSlotRect, swapSlotRect, bombSlotRect,
            shiftLineSlotRect, jumpSlotRect, delLineSlotRect, colorClearSlotRect
        };
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null && slots[i].contains(x, y)) return i;
        }
        return -1;
    }

    public static String getBonusName(int index) {
        return (index >= 0 && index < BONUS_NAMES.length) ? BONUS_NAMES[index] : "";
    }

    public static String getBonusDescription(int index) {
        return (index >= 0 && index < BONUS_DESCRIPTIONS.length) ? BONUS_DESCRIPTIONS[index] : "";
    }

    public static int getBonusAccentColor(int index) {
        return (index >= 0 && index < BONUS_ACCENT_COLORS.length) ? BONUS_ACCENT_COLORS[index] : 0xffffffff;
    }

    public static int getBonusDrawableId(int index) {
        return (index >= 0 && index < BONUS_DRAWABLE_IDS.length) ? BONUS_DRAWABLE_IDS[index] : -1;
    }

    public static long getBonusCost(int index) {
        return (index >= 0 && index < BONUS_BUY_COSTS.length) ? BONUS_BUY_COSTS[index] : 0;
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
