package com.example.androidstudio2dgamedevelopment;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class GameLoop extends Thread {

    private static final String TAG = "GameLoop";
    private static final double MAX_UPS    = 60.0;
    private static final double UPS_PERIOD = 1E3 / MAX_UPS;

    private final Game          game;
    private final SurfaceHolder surfaceHolder;

    /** Must be volatile – read/written from different threads. */
    private volatile boolean isRunning;

    private double averageUPS;
    private double averageFPS;

    public GameLoop(Game game, SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        this.game          = game;
    }

    public double getAverageUPS() { return averageUPS; }
    public double getAverageFPS() { return averageFPS; }

    public void startLoop() {
        Log.d(TAG, "startLoop()");
        isRunning = true;
        start();
        Log.d(TAG, "startLoop(): " + getState());
    }

    @Override
    @SuppressWarnings("BusyWait") // intentional: game loop uses sleep to cap UPS
    public void run() {
        Log.d(TAG, "run()");

        int  updateCount = 0;
        int  frameCount  = 0;
        long startTime   = System.currentTimeMillis();

        while (isRunning) {

            // Reset canvas so a failed lockCanvas never re-uses a stale reference
            Canvas canvas = null;

            // Try to update and render game
            try {
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    synchronized (surfaceHolder) {
                        game.update();
                        updateCount++;
                        game.draw(canvas);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "game loop error", e);
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        frameCount++;
                    } catch (Exception e) {
                        Log.e(TAG, "unlockCanvasAndPost error", e);
                    }
                }
            }

            // Pause game loop to not exceed target UPS
            long elapsedTime = System.currentTimeMillis() - startTime;
            long sleepTime   = (long) (updateCount * UPS_PERIOD - elapsedTime);

            if (sleepTime > 0) {
                try {
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // restore interrupt status
                    Log.w(TAG, "sleep interrupted", e);
                }
            }

            // Skip frames to keep up with target UPS
            while (sleepTime < 0 && updateCount < MAX_UPS - 1) {
                game.update();
                updateCount++;
                elapsedTime = System.currentTimeMillis() - startTime;
                sleepTime   = (long) (updateCount * UPS_PERIOD - elapsedTime);
            }

            // Calculate average FPS and UPS every second
            elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > 1000) {
                averageUPS   = updateCount / (1E-3 * elapsedTime);
                averageFPS   = frameCount  / (1E-3 * elapsedTime);
                updateCount  = 0;
                frameCount   = 0;
                startTime    = System.currentTimeMillis();
            }
        }
    }

    public void stopLoop() {
        Log.d(TAG, "stopLoop()");
        isRunning = false;
        try {
            join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore interrupt status
            Log.w(TAG, "join interrupted", e);
        }
        Log.d(TAG, "stopLoop(): " + getState());
    }
}
