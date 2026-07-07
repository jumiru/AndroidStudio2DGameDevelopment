package com.example.androidstudio2dgamedevelopment;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.List;

/**
 * Step-by-step spotlight tutorial drawn on top of the running {@link GameBoard}.
 * Highlighted regions are supplied lazily via {@link RectProvider} because the
 * board's HUD layout (bonus slots, status cards) is only known once the board
 * has performed its first-time layout pass in {@code GameBoard.draw()}.
 */
class TutorialOverlay {

    interface RectProvider {
        Rect get();
    }

    static class Step {
        final String title;
        final String body;
        final RectProvider highlight; // null = no spotlight, card is centered

        Step(String title, String body, RectProvider highlight) {
            this.title = title;
            this.body = body;
            this.highlight = highlight;
        }
    }

    private static final int FADE_FRAMES = 14;
    private static final int SCRIM_COLOR = 0xCC0B1020;
    private static final int HIGHLIGHT_BORDER_COLOR = 0xFFF5BF4F;
    private static final int CARD_BG_COLOR = 0xFF16213A;
    private static final int CARD_BORDER_COLOR = 0xFF32518B;
    private static final int TITLE_COLOR = 0xFFF5BF4F;
    private static final int BODY_COLOR = 0xFFCFD7EE;
    private static final int PROGRESS_COLOR = 0xFF7C88A8;
    private static final int NEXT_COLOR = 0xFF63B8FF;
    private static final int SKIP_COLOR = 0xFF7C88A8;
    private static final int HELP_BG_COLOR = 0xAA16213A;
    private static final int HELP_BORDER_COLOR = 0xFFF5BF4F;

    private final Step[] steps;
    private final Runnable onFinish;

    private boolean active;
    private int stepIndex;
    private int fadeCounter;

    private final Paint scrimPaint;
    private final Paint highlightBorderPaint;
    private final Paint cardBgPaint;
    private final Paint cardBorderPaint;
    private final Paint titlePaint;
    private final Paint bodyPaint;
    private final Paint progressPaint;
    private final Paint nextPaint;
    private final Paint skipPaint;
    private final Paint helpBgPaint;
    private final Paint helpBorderPaint;
    private final Paint menuLinePaint;

    private final RectF cardRectF = new RectF();
    private final RectF highlightRectF = new RectF();
    private final Rect skipHitRect = new Rect();

    TutorialOverlay(Step[] steps, Runnable onFinish) {
        this.steps = steps;
        this.onFinish = onFinish;

        scrimPaint = new Paint();
        scrimPaint.setColor(SCRIM_COLOR);

        highlightBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightBorderPaint.setStyle(Paint.Style.STROKE);
        highlightBorderPaint.setColor(HIGHLIGHT_BORDER_COLOR);
        highlightBorderPaint.setStrokeWidth(6f);

        cardBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBgPaint.setColor(CARD_BG_COLOR);
        cardBgPaint.setStyle(Paint.Style.FILL);

        cardBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBorderPaint.setColor(CARD_BORDER_COLOR);
        cardBorderPaint.setStyle(Paint.Style.STROKE);
        cardBorderPaint.setStrokeWidth(3f);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(TITLE_COLOR);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);

        bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyPaint.setColor(BODY_COLOR);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(PROGRESS_COLOR);

        nextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nextPaint.setColor(NEXT_COLOR);
        nextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        nextPaint.setTextAlign(Paint.Align.RIGHT);

        skipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        skipPaint.setColor(SKIP_COLOR);
        skipPaint.setTextAlign(Paint.Align.RIGHT);

        helpBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        helpBgPaint.setColor(HELP_BG_COLOR);

        helpBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        helpBorderPaint.setStyle(Paint.Style.STROKE);
        helpBorderPaint.setStrokeWidth(3f);
        helpBorderPaint.setColor(HELP_BORDER_COLOR);

        menuLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        menuLinePaint.setColor(HELP_BORDER_COLOR);
        menuLinePaint.setStyle(Paint.Style.STROKE);
        menuLinePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    boolean isActive() {
        return active;
    }

    void start() {
        active = true;
        stepIndex = 0;
        fadeCounter = FADE_FRAMES;
    }

    void update() {
        if (active && fadeCounter > 0) {
            fadeCounter--;
        }
    }

    /** Call on ACTION_DOWN while {@link #isActive()}; advances or ends the tutorial. */
    void handleTouch(int x, int y) {
        if (!active) {
            return;
        }
        if (!skipHitRect.isEmpty() && skipHitRect.contains(x, y)) {
            finish();
            return;
        }
        stepIndex++;
        if (stepIndex >= steps.length) {
            finish();
        } else {
            fadeCounter = FADE_FRAMES;
        }
    }

    private void finish() {
        active = false;
        if (onFinish != null) {
            onFinish.run();
        }
    }

    void draw(Canvas canvas, int canvasWidth, int canvasHeight, float density) {
        if (!active || stepIndex >= steps.length) {
            return;
        }
        Step step = steps[stepIndex];
        Rect highlight = step.highlight != null ? step.highlight.get() : null;

        float alpha = 1f - (float) fadeCounter / FADE_FRAMES;
        int fadeAlpha = (int) (255 * alpha);

        drawScrimWithHole(canvas, canvasWidth, canvasHeight, highlight);

        if (highlight != null) {
            highlightBorderPaint.setAlpha(fadeAlpha);
            highlightRectF.set(highlight);
            canvas.drawRoundRect(highlightRectF, 16f * density, 16f * density, highlightBorderPaint);
        }

        float margin = 28f * density;
        float padding = 20f * density;
        float cardWidth = canvasWidth - 2f * margin;

        titlePaint.setTextSize(21f * density);
        bodyPaint.setTextSize(15f * density);
        progressPaint.setTextSize(13f * density);
        nextPaint.setTextSize(15f * density);
        skipPaint.setTextSize(13f * density);

        List<String> lines = wrapText(step.body, bodyPaint, cardWidth - 2f * padding);
        float lineHeight = bodyPaint.getFontMetrics().descent - bodyPaint.getFontMetrics().ascent + 4f * density;
        float titleBlock = 30f * density;
        float footerBlock = 40f * density;
        float cardHeight = padding * 2f + titleBlock + lines.size() * lineHeight + footerBlock;

        float cardTop = pickCardTop(highlight, canvasHeight, cardHeight, margin);
        cardRectF.set(margin, cardTop, margin + cardWidth, cardTop + cardHeight);

        cardBgPaint.setAlpha(fadeAlpha);
        cardBorderPaint.setAlpha(fadeAlpha);
        titlePaint.setAlpha(fadeAlpha);
        bodyPaint.setAlpha(fadeAlpha);
        progressPaint.setAlpha(fadeAlpha);
        nextPaint.setAlpha(fadeAlpha);
        skipPaint.setAlpha(fadeAlpha);

        float cardRadius = 18f * density;
        canvas.drawRoundRect(cardRectF, cardRadius, cardRadius, cardBgPaint);
        canvas.drawRoundRect(cardRectF, cardRadius, cardRadius, cardBorderPaint);

        // Skip link, top-right of the card
        String skipLabel = "Überspringen";
        float skipY = cardRectF.top + padding * 0.6f + 10f * density;
        canvas.drawText(skipLabel, cardRectF.right - padding, skipY, skipPaint);
        float skipWidth = skipPaint.measureText(skipLabel);
        int skipTouchPad = (int) (10f * density);
        skipHitRect.set(
                (int) (cardRectF.right - padding - skipWidth) - skipTouchPad,
                (int) (cardRectF.top) ,
                (int) (cardRectF.right),
                (int) (skipY + skipTouchPad));

        float textX = cardRectF.left + padding;
        float y = cardRectF.top + padding + 18f * density;
        canvas.drawText(step.title, textX, y, titlePaint);
        y += titleBlock;
        for (String line : lines) {
            canvas.drawText(line, textX, y, bodyPaint);
            y += lineHeight;
        }

        String progress = (stepIndex + 1) + " / " + steps.length;
        float footerY = cardRectF.bottom - padding * 0.5f;
        canvas.drawText(progress, textX, footerY, progressPaint);

        String nextLabel = (stepIndex == steps.length - 1) ? "Los geht's!" : "Weiter";
        canvas.drawText(nextLabel, cardRectF.right - padding, footerY, nextPaint);
    }

    private float pickCardTop(Rect highlight, int canvasHeight, float cardHeight, float margin) {
        if (highlight == null) {
            return (canvasHeight - cardHeight) / 2f;
        }
        float spaceBelow = canvasHeight - highlight.bottom;
        float spaceAbove = highlight.top;
        if (spaceBelow >= cardHeight + margin) {
            return highlight.bottom + margin * 0.6f;
        }
        if (spaceAbove >= cardHeight + margin) {
            return highlight.top - cardHeight - margin * 0.6f;
        }
        // Not enough room on either side: center it over the highlighted area.
        return Math.max(margin, Math.min(canvasHeight - cardHeight - margin,
                highlight.top + (highlight.height() - cardHeight) / 2f));
    }

    private void drawScrimWithHole(Canvas canvas, int cw, int ch, Rect hole) {
        if (hole == null) {
            canvas.drawRect(0, 0, cw, ch, scrimPaint);
            return;
        }
        canvas.drawRect(0, 0, cw, hole.top, scrimPaint);
        canvas.drawRect(0, hole.bottom, cw, ch, scrimPaint);
        canvas.drawRect(0, hole.top, hole.left, hole.bottom, scrimPaint);
        canvas.drawRect(hole.right, hole.top, cw, hole.bottom, scrimPaint);
    }

    private List<String> wrapText(String text, Paint paint, float maxWidth) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n")) {
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (paint.measureText(candidate) > maxWidth && line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(candidate);
                }
            }
            if (line.length() > 0) {
                lines.add(line.toString());
            }
        }
        return lines;
    }

    void drawMenuButton(Canvas canvas, Rect rect) {
        if (rect == null || active) {
            return;
        }
        float radius = rect.width() / 2f;
        canvas.drawCircle(rect.centerX(), rect.centerY(), radius, helpBgPaint);
        canvas.drawCircle(rect.centerX(), rect.centerY(), radius, helpBorderPaint);

        float lineHalfWidth = rect.width() * 0.28f;
        float left = rect.centerX() - lineHalfWidth;
        float right = rect.centerX() + lineHalfWidth;
        float spacing = rect.height() * 0.22f;
        menuLinePaint.setStrokeWidth(rect.height() * 0.09f);
        for (int i = -1; i <= 1; i++) {
            float lineY = rect.centerY() + i * spacing;
            canvas.drawLine(left, lineY, right, lineY, menuLinePaint);
        }
    }
}
