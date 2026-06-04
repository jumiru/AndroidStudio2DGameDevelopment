package com.example.androidstudio2dgamedevelopment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * Game manages all objects in the game and is responsible for updating all states
 * and renders all objects to the screen.
 */
public class Game extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "Game";

    private final GameBoard gameBoard;
    private GameLoop gameLoop;
    private final GestureDetector gestureDetector;
    private boolean lastSelfPlayActive = false;

    /** Cached paints for debug overlays – allocated once, not every frame. */
    private final Paint debugPaint;

    public Game(Context context, SharedPreferences prefs) {
        super(context);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        gameBoard  = new GameBoard(getContext(), prefs);
        debugPaint = new Paint();
        debugPaint.setColor(ContextCompat.getColor(context, R.color.orange));
        debugPaint.setTextSize(50);

        gestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                int bx = (int) e.getX(), by = (int) e.getY();
                int idx = gameBoard.getBonusSlotIndexAt(bx, by);
                if (idx >= 0) {
                    gameBoard.deselectAllBonuses();
                    showBonusInfoPopup(idx);
                }
            }
        });

        setFocusable(true);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated()");
        gameLoop = new GameLoop(this, holder);
        gameLoop.startLoop();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged()");
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed()");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            int buyIdx = gameBoard.getBuyButtonIndexAt((int) event.getX(), (int) event.getY());
            if (buyIdx >= 0) {
                showBuyConfirmDialog(buyIdx);
                performClick();
                return true;
            }
        }
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP) {
            gameBoard.onTouchEvent(action, (int) event.getX(), (int) event.getY());
            if (action == MotionEvent.ACTION_DOWN) {
                performClick(); // accessibility requirement
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        gameBoard.draw(canvas);
        // Uncomment for performance debugging:
        // drawUPS(canvas);
        // drawFPS(canvas);
    }

    /** Draws the current updates-per-second onto the canvas (debug use). */
    private void drawUPS(Canvas canvas) {
        canvas.drawText("UPS: " + gameLoop.getAverageUPS(), 100, 80, debugPaint);
    }

    /** Draws the current frames-per-second onto the canvas (debug use). */
    private void drawFPS(Canvas canvas) {
        canvas.drawText("FPS: " + gameLoop.getAverageFPS(), 100, 140, debugPaint);
    }

    public void update() {
        gameBoard.update();
        boolean selfPlay = gameBoard.isSelfPlayActive();
        if (selfPlay != lastSelfPlayActive) {
            lastSelfPlayActive = selfPlay;
            post(() -> setKeepScreenOn(selfPlay));
        }
    }

    public void pause() {
        gameLoop.stopLoop();
    }

    private void showBuyConfirmDialog(final int index) {
        Context ctx = getContext();
        if (!(ctx instanceof Activity)) return;
        ((Activity) ctx).runOnUiThread(() -> {
            int accent     = GameBoard.getBonusAccentColor(index);
            String name    = GameBoard.getBonusName(index);
            long cost      = GameBoard.getBonusCost(index);
            int drawableId = GameBoard.getBonusDrawableId(index);

            // Custom layout
            LinearLayout root = new LinearLayout(ctx);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setGravity(Gravity.CENTER_HORIZONTAL);
            int padH = dp(24), padV = dp(20);
            root.setPadding(padH, padV, padH, dp(8));

            // Icon circle
            int circleSize = dp(64);
            FrameLayout iconFrame = new FrameLayout(ctx);
            LinearLayout.LayoutParams frameLP =
                    new LinearLayout.LayoutParams(circleSize, circleSize);
            frameLP.bottomMargin = dp(14);
            frameLP.gravity = Gravity.CENTER_HORIZONTAL;
            iconFrame.setLayoutParams(frameLP);
            GradientDrawable circleBg = new GradientDrawable();
            circleBg.setShape(GradientDrawable.OVAL);
            circleBg.setColor(Color.argb(48,
                    Color.red(accent), Color.green(accent), Color.blue(accent)));
            circleBg.setStroke(dp(2), accent);
            iconFrame.setBackground(circleBg);
            ImageView iconView = new ImageView(ctx);
            int iconSize = (int) (circleSize * 0.62f);
            FrameLayout.LayoutParams iconLP =
                    new FrameLayout.LayoutParams(iconSize, iconSize);
            iconLP.gravity = Gravity.CENTER;
            iconView.setLayoutParams(iconLP);
            if (drawableId > 0) iconView.setImageResource(drawableId);
            iconFrame.addView(iconView);
            root.addView(iconFrame);

            // Title
            TextView titleView = new TextView(ctx);
            titleView.setText(name + " kaufen?");
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            titleView.setTypeface(Typeface.DEFAULT_BOLD);
            titleView.setTextColor(accent);
            titleView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams titleLP = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            titleLP.bottomMargin = dp(8);
            titleView.setLayoutParams(titleLP);
            root.addView(titleView);

            // Cost message
            TextView msgView = new TextView(ctx);
            msgView.setText("Kaufpreis: " + cost + " Punkte");
            msgView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            msgView.setTextColor(0xFFCFD7EE);
            msgView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams msgLP = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            msgLP.bottomMargin = dp(4);
            msgView.setLayoutParams(msgLP);
            root.addView(msgView);

            AlertDialog dialog = new AlertDialog.Builder(ctx)
                    .setView(root)
                    .setPositiveButton("Ja", (d, w) -> gameBoard.performBuyForIndex(index))
                    .setNegativeButton("Nein", null)
                    .create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0xFF16213A));
            }
            dialog.show();
            Button yesBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button noBtn  = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (yesBtn != null) yesBtn.setTextColor(accent);
            if (noBtn  != null) noBtn.setTextColor(0xFFCFD7EE);
        });
    }

    private void showBonusInfoPopup(final int index) {
        Context ctx = getContext();
        if (!(ctx instanceof Activity)) return;

        ((Activity) ctx).runOnUiThread(() -> {
            int accent     = GameBoard.getBonusAccentColor(index);
            String name    = GameBoard.getBonusName(index);
            String desc    = GameBoard.getBonusDescription(index);
            int drawableId = GameBoard.getBonusDrawableId(index);
            long cost      = GameBoard.getBonusCost(index);

            // Root layout
            LinearLayout root = new LinearLayout(ctx);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setGravity(Gravity.CENTER_HORIZONTAL);
            int padH = dp(24), padV = dp(20);
            root.setPadding(padH, padV, padH, 0);

            // Icon circle
            int circleSize = dp(72);
            FrameLayout iconFrame = new FrameLayout(ctx);
            LinearLayout.LayoutParams frameLP =
                    new LinearLayout.LayoutParams(circleSize, circleSize);
            frameLP.bottomMargin = dp(16);
            frameLP.gravity = Gravity.CENTER_HORIZONTAL;
            iconFrame.setLayoutParams(frameLP);

            GradientDrawable circleBg = new GradientDrawable();
            circleBg.setShape(GradientDrawable.OVAL);
            circleBg.setColor(Color.argb(48,
                    Color.red(accent), Color.green(accent), Color.blue(accent)));
            circleBg.setStroke(dp(2), accent);
            iconFrame.setBackground(circleBg);

            ImageView iconView = new ImageView(ctx);
            int iconSize = (int) (circleSize * 0.64f);
            FrameLayout.LayoutParams iconLP =
                    new FrameLayout.LayoutParams(iconSize, iconSize);
            iconLP.gravity = Gravity.CENTER;
            iconView.setLayoutParams(iconLP);
            if (drawableId > 0) iconView.setImageResource(drawableId);
            iconFrame.addView(iconView);
            root.addView(iconFrame);

            // Bonus name
            TextView nameView = new TextView(ctx);
            nameView.setText(name);
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f);
            nameView.setTypeface(Typeface.DEFAULT_BOLD);
            nameView.setTextColor(accent);
            nameView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams nameLP = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            nameLP.bottomMargin = dp(10);
            nameView.setLayoutParams(nameLP);
            root.addView(nameView);

            // Description
            TextView descView = new TextView(ctx);
            descView.setText(desc);
            descView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            descView.setTextColor(0xFFCFD7EE);
            descView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams descLP = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            descLP.bottomMargin = dp(10);
            descView.setLayoutParams(descLP);
            root.addView(descView);

            // Cost line
            TextView costView = new TextView(ctx);
            costView.setText("Kaufpreis: " + cost + " Punkte");
            costView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            costView.setTextColor(Color.argb(160,
                    Color.red(accent), Color.green(accent), Color.blue(accent)));
            costView.setGravity(Gravity.CENTER);
            root.addView(costView);

            AlertDialog dialog = new AlertDialog.Builder(ctx)
                    .setView(root)
                    .setPositiveButton("OK", null)
                    .create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(0xFF16213A));
            }
            dialog.show();
            Button okBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (okBtn != null) okBtn.setTextColor(accent);
        });
    }

    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
