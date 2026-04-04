package com.example.androidstudio2dgamedevelopment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

/**
 * MainActivity is the main entry point to the game.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Game game;

    @SuppressLint("SourceCompatibility") // portrait lock is intentional for this 2D game
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Full-screen using AndroidX WindowCompat – works on all supported API levels
        Window window = getWindow();
        View decorView = window.getDecorView();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, decorView);
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        game = new Game(this, prefs);
        setContentView(game);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        game.pause();
        super.onPause();
    }

}