package com.aliaygor.trainpop

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import android.os.Bundle
import android.view.WindowManager

class MainActivity : ComponentActivity() {
    private lateinit var gameView: TrainView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        gameView = TrainView(this); setContentView(gameView)
        onBackPressedDispatcher.addCallback(this,object:OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { gameView.back() }
        })
    }
    override fun onPause() { gameView.pause(); super.onPause() }
    override fun onResume() { super.onResume(); if (::gameView.isInitialized) gameView.resume() }
    override fun onDestroy() { gameView.release(); super.onDestroy() }
}
