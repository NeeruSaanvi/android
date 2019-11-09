package com.pine.emeter.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pine.emeter.R;
import com.pine.emeter.model.HistoryModel;
import com.pine.emeter.utils.Constant;
import com.pine.emeter.utils.Utility;
import com.pine.emeter.validation.ValidationUtils;
import com.pine.emeter.weService.ApiService;
import com.pine.emeter.weService.ResponseCallBack;
import com.pine.emeter.weService.ServerUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPassword extends AppCompatActivity implements View.OnClickListener, ResponseCallBack {

    EditText email;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        email = findViewById(R.id.email);
        submit = findViewById(R.id.submit);
        submit.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        doValidate();
    }

    private void doValidate() {
        if (ValidationUtils.isValidEmail(email)) {
            Utility.showSimpleDialogBox(this, "Please enter valid email");
        } else {
            Map<String, String> param = new HashMap<String, String>();
            param.put("email", email.getText().toString());
            new ApiService(this, ServerUtility.url.ForgotPassword, param, ForgotPassword.this, Constant.POST_REQUEST, ServerUtility.webServiceName.ForgotPassword);
        }
    }

    @Override
    public void onResult(String response, Context context, String labelFor) {
        try {
            JSONObject object = new JSONObject(response);
            boolean success = object.getBoolean("result");
            String message = object.getString("message");
            if (success) {
                Utility.showSimpleDialogBox(context, message);
                email.setText("");
            } else {
                Utility.showSimpleDialogBox(context, message);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

