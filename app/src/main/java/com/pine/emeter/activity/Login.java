package com.pine.emeter.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.pine.emeter.R;
import com.pine.emeter.model.RegistrationModel;
import com.pine.emeter.utils.AppPreferences;
import com.pine.emeter.utils.Constant;
import com.pine.emeter.utils.SessionManager;
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

public class Login extends AppCompatActivity implements View.OnClickListener, ResponseCallBack {
    EditText email;
    EditText password;
    Button login;
    TextView forgot;
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        init();
    }


    private void init() {
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        forgot = findViewById(R.id.forgot);
        login = findViewById(R.id.buttonlogin);
        login.setOnClickListener(this);
        forgot.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        if (v == login) {
            doValidate();
        }
        if (v == forgot) {
            Intent intent = new Intent(this,ForgotPassword.class);
            startActivity(intent);
        }
    }

    private void doValidate() {
        if (ValidationUtils.isValidEmail(email)) {
            Utility.showSimpleDialogBox(this, "Please enter valid email");
        } else if (ValidationUtils.isEmpty(password)) {
            Utility.showSimpleDialogBox(this, "Please enter password");
        } else if (ValidationUtils.isValidPassword(password)) {
            Utility.showSimpleDialogBox(this, " Password length should be large than 6 character");
        } else {
            doRegister();
        }
    }

    private void doRegister() {
        Map params = new HashMap<String, String>();
        params.put("email", email.getText().toString());
        params.put("password", password.getText().toString());
        params.put("device_name", "android");
        params.put("fcm_id", "");
        new ApiService(this, ServerUtility.url.LoginUrl, params, Login.this, Constant.POST_REQUEST, ServerUtility.webServiceName.Login);
    }

    @Override
    public void onResult(String response, Context context, String labelFor) {
        try {
            JSONObject object = new JSONObject(response);
            boolean success = object.getBoolean("result");
            String message = object.getString("message");
            if (success) {
                JSONArray jsonArray = object.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    RegistrationModel model = gson.fromJson(jsonObject.toString(), RegistrationModel.class);
                    String regModel = gson.toJson(model);
                    AppPreferences.getInstance(context).setRegistrationModel(regModel);
                    new SessionManager(context).createUserLoginSession();
                    Intent intent = new Intent(context,HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            } else {
                Utility.showSimpleDialogBox(context, message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
