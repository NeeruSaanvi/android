package com.pine.emeter.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.pine.emeter.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by PinesucceedAndroid on 6/21/2018.
 */

public class Utility {
    public static ProgressDialog progressDialog;
    public  static int PERMISSION_ALL = 1;
    public  static String[] PERMISSIONS = {Manifest.permission.CAMERA};

    public static void showSimpleDialogBox(Context context, String message) {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(context);
        popDialog.setCancelable(true);
        popDialog.setMessage(message);
        popDialog.setPositiveButton(context.getString(R.string.ok),

                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        popDialog.create();
        popDialog.show();
    }

    public static void ShowToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }



    public static void doPermission(Activity activity, Context context) {
        if (!hasPermissions(context, PERMISSIONS)) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    public static boolean isInternetAvailable(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // test for connection
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            // Log.v("APP_ROOT", "Internet Connection Not Present");
            return false;
        }
    }

    public static void showDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.please_wait));
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    public static void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public static String getLocalDate(String review) {
        String localDate= "";
        DateFormat targetFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH);
        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            targetFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date local = utcFormat.parse(review);
            targetFormat.setTimeZone(TimeZone.getDefault());
            localDate =targetFormat.format(local);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return localDate;
    }

    public static String getLocalTime(String review) {
        String localDate= "";
        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat targetFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        try {
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date local = utcFormat.parse(review);
            targetFormat.setTimeZone(TimeZone.getDefault());
            localDate =targetFormat.format(local);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return localDate;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static Object convertNull(Object obj) {

        if (obj instanceof JSONArray) {

            JSONArray jsonArray = (JSONArray) obj;
            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                try {
                    Object a = jsonArray.get(i);
                    if (a == null) {
                        jsonArray.remove(i);
                    } else {
                        jsonArray.put(i, convertNull(a));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return jsonArray;
        } else if (obj instanceof JSONObject) {
            JSONObject b = (JSONObject) obj;
            Iterator<?> keys = b.keys();
            //  Log.i("hello","nuul[[[[[lllllllll"+b.toString());
            while (keys.hasNext()) {
                try {
                    String key = (String) keys.next();
                    Object value = b.get(key);
                    if (value == null || value.equals(null)) {
                        b.put(key, "");
                    } else {
                        b.put(key, convertNull(value));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return b;
        } else {
            return obj;
        }
    }

}
