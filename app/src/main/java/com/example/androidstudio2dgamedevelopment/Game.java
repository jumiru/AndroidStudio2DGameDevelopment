package com.example.androidstudio2dgamedevelopment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            gameBoard.onTouchEvent((int) event.getX(), (int) event.getY());
            performClick(); // accessibility requirement
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
    }

    public void pause() {
        gameLoop.stopLoop();
    }
}
