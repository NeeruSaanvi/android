package com.pine.emeter.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by PinesucceedAndroid on 6/29/2018.
 */

public class AppPreferences {
    private static AppPreferences singletonPreference = null;
    private SharedPreferences sharedPreferences;

    public AppPreferences(Context context) {
        if (context == null)
            return;
        sharedPreferences = context.getSharedPreferences(Constant.PREFERENCE, 0);
    }

    public static AppPreferences getInstance(Context context) {
        if (singletonPreference == null)
            singletonPreference = new AppPreferences(context);
        return singletonPreference;
    }

    public void clearPrefernce() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void setRegistrationModel(String regModel) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constant.RegistrationModel, regModel);
        editor.apply();
    }

    public String getRegistrationModel() {
        return sharedPreferences.getString(Constant.RegistrationModel, "");
    }

}
