package com.dailyvery.apps.imhome

import android.content.Intent
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class LoaderSplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loader_splash_screen)

        val handler = Handler()
        handler.postDelayed({
            val intent = Intent(this@LoaderSplashScreen, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 600)


    }
}
