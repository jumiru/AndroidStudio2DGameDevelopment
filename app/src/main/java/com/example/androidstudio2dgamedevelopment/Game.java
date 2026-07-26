package com.example.androidstudio2dgamedevelopment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;

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
    private final LeaderboardManager leaderboardManager = new LeaderboardManager();
    private GameBoard.statusT lastStatus = null;

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
                if (gameBoard.isTutorialActive()) return;
                int bx = (int) e.getX(), by = (int) e.getY();
                int idx = gameBoard.getBonusSlotIndexAt(bx, by);
                if (idx >= 0) {
                    gameBoard.cancelPendingBonusTap();
                    gameBoard.deselectAllBonuses();
                    showBonusInfoPopup(idx);
                } else if (gameBoard.isBestHudAt(bx, by)) {
                    showLeaderboardDialog();
                } else if (gameBoard.isLevelHudAt(bx, by)) {
                    showLevelHint();
                } else if (gameBoard.isScoreHudAt(bx, by)) {
                    showSaveLoadDialog();
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
        if (action == MotionEvent.ACTION_DOWN && !gameBoard.isTutorialActive()) {
            if (gameBoard.isMenuButtonAt((int) event.getX(), (int) event.getY())) {
                showMenuDialog();
                performClick();
                return true;
            }
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
        GameBoard.statusT status = gameBoard.getStatus();
        if (status == GameBoard.statusT.GAME_OVER && lastStatus != GameBoard.statusT.GAME_OVER
                && gameBoard.isHighscoreExceeded() && !gameBoard.isCheatActive()) {
            post(this::showNewHighscoreSubmitDialog);
        }
        lastStatus = status;
    }

    public void pause() {
        gameBoard.saveGame();
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

    private void showSaveLoadDialog() {
        Context ctx = getContext();
        if (!(ctx instanceof Activity)) return;

        // Read slot summaries before switching to UI thread (avoids holding game-thread lock)
        String[] summaries = new String[GameBoard.NUM_SAVE_SLOTS];
        for (int i = 0; i < GameBoard.NUM_SAVE_SLOTS; i++) {
            summaries[i] = gameBoard.getSlotSummary(i);
        }
        boolean cheatActive = gameBoard.isCheatActive();

        ((Activity) ctx).runOnUiThread(() -> {
            LinearLayout root = new LinearLayout(ctx);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(dp(16), dp(16), dp(16), dp(8));

            if (cheatActive) {
                TextView cheatNotice = new TextView(ctx);
                cheatNotice.setText("Debug-Code aktiv: Speichern ist deaktiviert, Laden weiterhin möglich.");
                cheatNotice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                cheatNotice.setTextColor(0xFFFF7F7F);
                cheatNotice.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams noticeLP = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                noticeLP.bottomMargin = dp(10);
                cheatNotice.setLayoutParams(noticeLP);
                root.addView(cheatNotice);
            }

            AlertDialog[] holder = {null};

            // Per-slot views that may be updated in-place after a save
            TextView[] summaryViews = new TextView[GameBoard.NUM_SAVE_SLOTS];
            Button[]   loadBtns    = new Button[GameBoard.NUM_SAVE_SLOTS];

            for (int i = 0; i < GameBoard.NUM_SAVE_SLOTS; i++) {
                final int slot = i;
                boolean hasData = summaries[i] != null;

                // Slot card
                LinearLayout card = new LinearLayout(ctx);
                card.setOrientation(LinearLayout.VERTICAL);
                GradientDrawable cardBg = new GradientDrawable();
                cardBg.setColor(0xFF1A2A42);
                cardBg.setCornerRadius(dp(10));
                cardBg.setStroke(dp(1), 0xFF3A5A8A);
                card.setBackground(cardBg);
                card.setPadding(dp(12), dp(10), dp(12), dp(6));
                LinearLayout.LayoutParams cardLP = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                cardLP.bottomMargin = dp(8);
                card.setLayoutParams(cardLP);

                // Slot label
                TextView slotLabel = new TextView(ctx);
                slotLabel.setText("Slot " + (i + 1));
                slotLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
                slotLabel.setTypeface(Typeface.DEFAULT_BOLD);
                slotLabel.setTextColor(0xFFF5BF4F);
                card.addView(slotLabel);

                // Summary text
                TextView summaryView = new TextView(ctx);
                summaryView.setText(hasData ? summaries[i] : "(leer)");
                summaryView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
                summaryView.setTextColor(hasData ? 0xFFCFD7EE : 0xFF606880);
                LinearLayout.LayoutParams sumLP = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                sumLP.bottomMargin = dp(6);
                summaryView.setLayoutParams(sumLP);
                card.addView(summaryView);
                summaryViews[i] = summaryView;

                // Button row
                LinearLayout btnRow = new LinearLayout(ctx);
                btnRow.setOrientation(LinearLayout.HORIZONTAL);

                Button saveBtn = new Button(ctx);
                saveBtn.setText("Speichern");
                saveBtn.setTextColor(cheatActive ? 0xFF404858 : 0xFF63D88E);
                saveBtn.setEnabled(!cheatActive);
                saveBtn.setBackgroundColor(Color.TRANSPARENT);
                saveBtn.setOnClickListener(v -> {
                    gameBoard.saveToSlot(slot);
                    String updated = gameBoard.getSlotSummary(slot);
                    summaryViews[slot].setText(updated != null ? updated : "(leer)");
                    summaryViews[slot].setTextColor(0xFFCFD7EE);
                    loadBtns[slot].setEnabled(true);
                    loadBtns[slot].setTextColor(0xFF63B8FF);
                });
                btnRow.addView(saveBtn);

                Button loadBtn = new Button(ctx);
                loadBtn.setText("Laden");
                loadBtn.setTextColor(hasData ? 0xFF63B8FF : 0xFF404858);
                loadBtn.setEnabled(hasData);
                loadBtn.setBackgroundColor(Color.TRANSPARENT);
                loadBtn.setOnClickListener(v -> {
                    gameBoard.loadFromSlot(slot);
                    if (holder[0] != null) holder[0].dismiss();
                });
                btnRow.addView(loadBtn);
                loadBtns[i] = loadBtn;

                card.addView(btnRow);
                root.addView(card);
            }

            AlertDialog dialog = new AlertDialog.Builder(ctx)
                    .setTitle("Spielstände")
                    .setView(root)
                    .setNegativeButton("Schließen", null)
                    .create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0xFF16213A));
            }
            holder[0] = dialog;
            dialog.show();
            Button closeBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (closeBtn != null) closeBtn.setTextColor(0xFFCFD7EE);
        });
    }

    private void showLevelHint() {
        Context ctx = getContext();
        if (!(ctx instanceof Activity)) return;
        String hint = gameBoard.getLevelHintText();
        if (hint == null || hint.isEmpty()) return;
        ((Activity) ctx).runOnUiThread(() ->
                Toast.makeText(ctx, hint, Toast.LENGTH_LONG).show());
    }

    /** Shows the burger menu opened from the top-right menu button. */
    private void showMenuDialog() {
        Context ctx = getContext();
        if (!(ctx instanceof Activity)) return;
        ((Activity) ctx).runOnUiThread(() -> {
            LinearLayout root = new LinearLayout(ctx);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(dp(16), dp(16), dp(16), dp(8));

            AlertDialog dialog = new AlertDialog.Builder(ctx)
                    .setView(root)
                    .setNegativeButton("Schließen", null)
                    .create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0xFF16213A));
            }

            addMenuItem(root, ctx, "Spielanleitung", 0xFF63B8FF, () -> {
                dialog.dismiss();
                gameBoard.startTutorial();
            });
            addMenuItem(root, ctx, "Spiel neu starten", 0xFFF5BF4F, () -> {
                dialog.dismiss();
                showRestartConfirmDialog();
            });
            addMenuItem(root, ctx, "Highscore zurücksetzen", 0xFFFF7F7F, () -> {
                dialog.dismiss();
                showResetHighscoreConfirmDialog();
            });
            addMenuItem(root, ctx, "Debug-Status teilen", 0xFF63D88E, () -> {
                dialog.dismiss();
                shareDebugSnapshot();
            });
            addMenuItem(root, ctx, "Debug-Status einlesen", 0xFF63D88E, () -> {
                dialog.dismiss();
                showImportDebugSnapshotDialog();
            });

            dialog.show();
            Button closeBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (closeBtn != null) closeBtn.setTextColor(0xFFCFD7EE);
        });
    }

    private void addMenuItem(LinearLayout root, Context ctx, String label, int accent, Runnable action) {
        TextView item = new TextView(ctx);
        item.setText(label);
        item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        item.setTextColor(accent);
        item.setTypeface(Typeface.DEFAULT_BOLD);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(16), dp(16), dp(16), dp(16));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF1A2A42);
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(1), 0xFF3A5A8A);
        item.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        item.setLayoutParams(lp);
        item.setOnClickListener(v -> action.run());
        root.addView(item);
    }

    private void showRestartConfirmDialog() {
        showConfirmDialog("Spiel neu starten?", "Der aktuelle Spielstand geht verloren.",
                "Neu starten", gameBoard::restartGame);
    }

    private void showResetHighscoreConfirmDialog() {
        showConfirmDialog("Highscore zurücksetzen?", "Dein gespeicherter Highscore wird auf 0 gesetzt.",
                "Zurücksetzen", gameBoard::resetAllHighscores);
    }

    /** Shares board, undo stack and bonus counters as plain text (e.g. via Messenger/Mail) for a bug report. */
    private void shareDebugSnapshot() {
        Context ctx = getContext();
        if (!(ctx instanceof Activity)) return;
        String snapshot = gameBoard.exportDebugSnapshot();
        if (snapshot == null) {
            Toast.makeText(ctx, "Debug-Status konnte nicht erstellt werden.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, snapshot);
        ctx.startActivity(Intent.createChooser(sendIntent, "Debug-Status teilen"));
    }

    /** Lets the player paste a debug status shared via {@link #shareDebugSnapshot} to reproduce a bug 1:1. */
    private void showImportDebugSnapshotDialog() {
        Context ctx = getContext();
        if (!(ctx instanceof Activity)) return;
        ((Activity) ctx).runOnUiThread(() -> {
            LinearLayout root = new LinearLayout(ctx);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(dp(24), dp(16), dp(24), dp(8));
            root.addView(buildDialogTitle(ctx, "Debug-Status einlesen"));

            TextView hint = new TextView(ctx);
            hint.setText("Fügt einen geteilten Debug-Status ein. Board, Undo-Verlauf und Boni werden übernommen " +
                    "- der aktuelle Spielstand geht dabei verloren.");
            hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
            hint.setTextColor(0xFFCFD7EE);
            LinearLayout.LayoutParams hintLP = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            hintLP.bottomMargin = dp(10);
            hint.setLayoutParams(hintLP);
            root.addView(hint);

            EditText input = new EditText(ctx);
            input.setHint("Hier einfügen...");
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            input.setMinLines(4);
            input.setMaxLines(8);
            input.setTextColor(0xFFCFD7EE);
            input.setHintTextColor(0xFF606880);

            ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()
                    && clipboard.getPrimaryClipDescription() != null
                    && clipboard.getPrimaryClipDescription().hasMimeType("text/plain")
                    && clipboard.getPrimaryClip().getItemCount() > 0) {
                CharSequence clip = clipboard.getPrimaryClip().getItemAt(0).getText();
                if (clip != null && clip.toString().trim().startsWith("{")) input.setText(clip);
            }
            root.addView(input);

            AlertDialog dialog = new AlertDialog.Builder(ctx)
                    .setView(root)
                    .setPositiveButton("Übernehmen", null)
                    .setNegativeButton("Abbrechen", null)
                    .create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0xFF16213A));
            }
            dialog.show();
            Button negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (negBtn != null) negBtn.setTextColor(0xFFCFD7EE);
            Button posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (posBtn != null) {
                posBtn.setTextColor(0xFF63D88E);
                posBtn.setOnClickListener(v -> {
                    if (gameBoard.importDebugSnapshot(input.getText().toString())) {
                        dialog.dismiss();
                        Toast.makeText(ctx, "Debug-Status geladen.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ctx, "Ungültiger oder nicht passender Debug-Status.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /** Generic destructive-action confirmation, styled to match the dark dialog theme
     *  (plain {@code setTitle()}/{@code setMessage()} render in barely-visible dark text here). */
    private void showConfirmDialog(String title, String message, String confirmLabel, Runnable onConfirm) {
        Context ctx = getContext();
        if (!(ctx instanceof Activity)) return;
        ((Activity) ctx).runOnUiThread(() -> {
            LinearLayout root = new LinearLayout(ctx);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(dp(24), dp(16), dp(24), dp(8));
            root.addView(buildDialogTitle(ctx, title));

            TextView messageView = new TextView(ctx);
            messageView.setText(message);
            messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            messageView.setTextColor(0xFFCFD7EE);
            messageView.setGravity(Gravity.CENTER);
            root.addView(messageView);

            AlertDialog dialog = new AlertDialog.Builder(ctx)
                    .setView(root)
                    .setPositiveButton(confirmLabel, (d, w) -> onConfirm.run())
                    .setNegativeButton("Abbrechen", null)
                    .create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0xFF16213A));
            }
            dialog.show();
            Button posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (posBtn != null) posBtn.setTextColor(0xFFFF7F7F);
            if (negBtn != null) negBtn.setTextColor(0xFFCFD7EE);
        });
    }

    /** Builds a title TextView styled to match the dialogs' dark background (setTitle() renders black text). */
    private TextView buildDialogTitle(Context ctx, String text) {
        TextView titleView = new TextView(ctx);
        titleView.setText(text);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setTextColor(0xFFF5BF4F);
        titleView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        titleLP.bottomMargin = dp(12);
        titleView.setLayoutParams(titleLP);
        return titleView;
    }

    /** Shows a full, scrollable/selectable error message so long texts (e.g. Firestore error codes) aren't cut off like a Toast would be. */
    private void showErrorDialog(Context ctx, String title, String message) {
        TextView messageView = new TextView(ctx);
        messageView.setText(message);
        messageView.setTextIsSelectable(true);
        messageView.setTextColor(0xFFCFD7EE);
        messageView.setPadding(dp(24), dp(16), dp(24), dp(8));

        ScrollView scrollView = new ScrollView(ctx);
        scrollView.addView(messageView);

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setView(scrollView)
                .setPositiveButton("OK", null)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0xFF16213A));
        }
        dialog.show();
        Button okBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (okBtn != null) okBtn.setTextColor(0xFF63D88E);
    }

    /** Prompt shown once when the player beats their local highscore, offering to submit it online. */
    private void showNewHighscoreSubmitDialog() {
        Context ctx = getContext();
        if (!(ctx instanceof Activity)) return;
        long score = gameBoard.getHighScore();
        String savedName = gameBoard.getLeaderboardName();
        ((Activity) ctx).runOnUiThread(() -> {
            LinearLayout root = new LinearLayout(ctx);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(dp(24), dp(16), dp(24), dp(8));
            root.addView(buildDialogTitle(ctx, "Neuer Highscore!"));

            TextView scoreView = new TextView(ctx);
            scoreView.setText(gameBoard.getScoreText(score) + " Punkte");
            scoreView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            scoreView.setTypeface(Typeface.DEFAULT_BOLD);
            scoreView.setTextColor(0xFFF5BF4F);
            scoreView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams scoreLP = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            scoreLP.bottomMargin = dp(12);
            scoreView.setLayoutParams(scoreLP);
            root.addView(scoreView);

            EditText nameInput = new EditText(ctx);
            nameInput.setHint("Dein Name");
            nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
            nameInput.setSingleLine(true);
            nameInput.setText(savedName);
            nameInput.setSelection(savedName != null ? savedName.length() : 0);
            nameInput.setTextColor(0xFFCFD7EE);
            nameInput.setHintTextColor(0xFF606880);
            root.addView(nameInput);

            AlertDialog dialog = new AlertDialog.Builder(ctx)
                    .setView(root)
                    .setPositiveButton("Einreichen", null)
                    .setNegativeButton("Überspringen", null)
                    .setCancelable(false)
                    .create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0xFF16213A));
            }
            dialog.show();
            Button posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (posBtn != null) {
                posBtn.setTextColor(0xFF63D88E);
                posBtn.setOnClickListener(v -> {
                    String name = nameInput.getText().toString().trim();
                    gameBoard.setLeaderboardName(name);
                    leaderboardManager.submitScore(name, score,
                            () -> ((Activity) ctx).runOnUiThread(() ->
                                    Toast.makeText(ctx, "Score eingereicht!", Toast.LENGTH_SHORT).show()),
                            e -> ((Activity) ctx).runOnUiThread(() ->
                                    showErrorDialog(ctx, "Einreichen fehlgeschlagen", String.valueOf(e.getMessage()))));
                    dialog.dismiss();
                });
            }
            if (negBtn != null) negBtn.setTextColor(0xFFCFD7EE);
        });
    }

    /** Shows the online top-10 leaderboard, following the same card-list style as the save/load dialog. */
    private void showLeaderboardDialog() {
        Context ctx = getContext();
        if (!(ctx instanceof Activity)) return;
        ((Activity) ctx).runOnUiThread(() -> {
            LinearLayout root = new LinearLayout(ctx);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(dp(16), dp(16), dp(16), dp(8));
            root.setGravity(Gravity.CENTER_HORIZONTAL);
            root.addView(buildDialogTitle(ctx, "Bestenliste"));

            ProgressBar progress = new ProgressBar(ctx);
            root.addView(progress);

            AlertDialog dialog = new AlertDialog.Builder(ctx)
                    .setView(root)
                    .setNegativeButton("Schließen", null)
                    .create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0xFF16213A));
            }
            dialog.show();
            Button closeBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (closeBtn != null) closeBtn.setTextColor(0xFFCFD7EE);

            leaderboardManager.fetchTop(10,
                    entries -> ((Activity) ctx).runOnUiThread(() -> populateLeaderboard(root, progress, entries)),
                    e -> ((Activity) ctx).runOnUiThread(() -> {
                        root.removeView(progress);
                        TextView errorView = new TextView(ctx);
                        errorView.setText("Bestenliste konnte nicht geladen werden.");
                        errorView.setTextColor(0xFFFF7F7F);
                        errorView.setGravity(Gravity.CENTER);
                        root.addView(errorView);
                    }));
        });
    }

    private void populateLeaderboard(LinearLayout root, ProgressBar progress, List<LeaderboardManager.LeaderboardEntry> entries) {
        Context ctx = getContext();
        root.removeView(progress);
        if (entries.isEmpty()) {
            TextView emptyView = new TextView(ctx);
            emptyView.setText("Noch keine Einträge.");
            emptyView.setTextColor(0xFFCFD7EE);
            emptyView.setGravity(Gravity.CENTER);
            root.addView(emptyView);
            return;
        }
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardManager.LeaderboardEntry entry = entries.get(i);

            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(LinearLayout.HORIZONTAL);
            GradientDrawable rowBg = new GradientDrawable();
            rowBg.setColor(0xFF1A2A42);
            rowBg.setCornerRadius(dp(10));
            rowBg.setStroke(dp(1), 0xFF3A5A8A);
            row.setBackground(rowBg);
            row.setPadding(dp(12), dp(8), dp(12), dp(8));
            LinearLayout.LayoutParams rowLP = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLP.bottomMargin = dp(6);
            row.setLayoutParams(rowLP);

            TextView rankView = new TextView(ctx);
            rankView.setText("#" + (i + 1));
            rankView.setTypeface(Typeface.DEFAULT_BOLD);
            rankView.setTextColor(0xFFF5BF4F);
            LinearLayout.LayoutParams rankLP = new LinearLayout.LayoutParams(dp(36), LinearLayout.LayoutParams.WRAP_CONTENT);
            rankView.setLayoutParams(rankLP);
            row.addView(rankView);

            TextView nameView = new TextView(ctx);
            nameView.setText(entry.name);
            nameView.setTextColor(0xFFCFD7EE);
            LinearLayout.LayoutParams nameLP = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            nameView.setLayoutParams(nameLP);
            row.addView(nameView);

            TextView scoreView = new TextView(ctx);
            scoreView.setText(gameBoard.getScoreText(entry.score));
            scoreView.setTypeface(Typeface.DEFAULT_BOLD);
            scoreView.setTextColor(0xFF63B8FF);
            row.addView(scoreView);

            root.addView(row);
        }
    }

    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
