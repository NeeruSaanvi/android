package com.pine.emeter.weService;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.pine.emeter.R;
import com.pine.emeter.activity.Register;
import com.pine.emeter.utils.Constant;
import com.pine.emeter.utils.SessionManager;
import com.pine.emeter.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LogoutSession implements ResponseCallBack {

    private static SessionManager sessionManager;

    public void doLogout(final Context context, final String email) {
        sessionManager = new SessionManager(context);
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.logout)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Map params = new HashMap<String, String>();
                        params.put("email", email);
                        new ApiService(context, ServerUtility.url.Logout, params, LogoutSession.this, Constant.POST_REQUEST, ServerUtility.webServiceName.Logout);


                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }

    @Override
    public void onResult(String response, Context context, String labelFor) {
        try {
            JSONObject object = new JSONObject(response);
            boolean success = object.getBoolean("result");
            String message = object.getString("message");
            if (success) {
                sessionManager.logoutUser();
            } else {
                Utility.showSimpleDialogBox(context,message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
