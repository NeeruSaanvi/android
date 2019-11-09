package com.pine.emeter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.pine.emeter.activity.WelCome;
import com.pine.emeter.utils.SessionManager;
import io.fabric.sdk.android.Fabric;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        Thread splashThread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    finish();
                    new SessionManager(SplashScreen.this).checkLogin();
//                     Intent i = new Intent(getApplicationContext(), WelCome.class);
//                     startActivity(i);

                }
            }

        };
        splashThread.start();
    }
}
