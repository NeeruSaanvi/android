package com.pine.emeter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.pine.emeter.R;
import com.pine.emeter.utils.Utility;

public class WelCome extends AppCompatActivity implements View.OnClickListener {
    Button login, register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_wel_come);
        Utility.doPermission(WelCome.this,WelCome.this);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        register.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == login) {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }
        if (v == register) {
            Intent intent = new Intent(this, Register.class);
            startActivity(intent);
        }
    }
}
