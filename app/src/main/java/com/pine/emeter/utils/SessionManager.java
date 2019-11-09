package com.pine.emeter.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.pine.emeter.activity.HomeActivity;
import com.pine.emeter.activity.Login;
import com.pine.emeter.activity.WelCome;
import com.pine.emeter.model.RegistrationModel;


/**
 * Created by PinesucceedAndroid on 12/26/2017.
 */

public class SessionManager {
    SharedPreferences pref;
    SharedPreferences sharedPreferences;

    SharedPreferences.Editor editor;
    String type;


    Context _context;


    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "Reg";
    private static final String IS_LOGIN = "IsUserLoggedIn";
    private static final String IS_FIRST_RUN = "IsUserFirstRun";
    public static final String KEY_NAME = "Name";
    public static final String KEY_EMAIL = "Email";
    private boolean isVerified;
    private boolean lang;


    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public void createUserLoginSession() {

        editor.putBoolean(IS_LOGIN, true);
        editor.commit();
    }

    Gson gson = new Gson();

    public boolean checkLogin() {
        String strmodel = AppPreferences.getInstance(_context).getRegistrationModel();
        RegistrationModel model = gson.fromJson(strmodel,RegistrationModel.class);
            if (this.isLoggedIn()) {
                Intent intent = new Intent(_context, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                _context.startActivity(intent);

            } else {
                Intent i = new Intent(_context, WelCome.class);

                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                _context.startActivity(i);

            }

        return false;

    }


    public void logoutUser() {

        editor.clear();
        editor.commit();
        editor.putBoolean(IS_LOGIN, false);
        AppPreferences.getInstance(_context).clearPrefernce();
        Intent i = new Intent(_context, Login.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        _context.startActivity(i);
    }


    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    public boolean isFirstRun() {
        return pref.getBoolean(IS_FIRST_RUN, false);
    }
}
