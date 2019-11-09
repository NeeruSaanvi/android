package com.pine.emeter.validation;

import android.widget.EditText;

/**
 * Created by PinesucceedAndroid on 6/21/2018.
 */

public class ValidationUtils {
    public static boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0)
            return false;

        return true;
    }


    public static boolean isValidPassword(EditText editText) {
        if (editText.getText().toString().trim().length() > 5)
            return false;

        return true;
    }

    public static boolean isValidEmail(EditText email) {
        return !android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches();
    }

    public static boolean isConfirmPassword(EditText pass, EditText confirm) {
        if (pass.getText().toString().trim().equals(confirm.getText().toString().trim()))
            return false;

        return true;
    }

}
