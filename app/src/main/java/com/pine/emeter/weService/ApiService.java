package com.pine.emeter.weService;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pine.emeter.utils.Constant;
import com.pine.emeter.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by PinesucceedAndroid on 6/29/2018.
 */

public class ApiService {
    Context context;
    String url;
    int reqType;
    String labelFor;
    Map param;
    ResponseCallBack responseCallBack;

    public ApiService(Context context, String url, Map param, ResponseCallBack responseCallBack, int reqType, String labelFor) {
        this.context = context;
        this.url = url;
        this.param = param;
        this.reqType = reqType;
        this.responseCallBack = responseCallBack;
        this.labelFor = labelFor;
        if (Utility.isInternetAvailable(context)) {
            if (reqType == Constant.POST_REQUEST) {
                callPostRequest();
            } else if (reqType == Constant.GET_REQUEST) {
                callGetRequest();
            }
        } else {
            Utility.showSimpleDialogBox(context, "Internet Not Available");

        }

    }

    private void callPostRequest() {
        Utility.showDialog(context);
        StringRequest objectRequest = new StringRequest(
                Request.Method.POST, url,
                new Response.Listener<String>() {

                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        // TODO Auto-generated method stub
                        try {
                            Utility.hideDialog();
                            String resRemovedNull = String.valueOf(Utility.convertNull(new JSONObject(response)));
                            Log.d("after remove null " + url, "REsponse:::remov null "
                                    + resRemovedNull);
                            responseCallBack.onResult(resRemovedNull.toString(),context,labelFor);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Utility.hideDialog();
                Log.e("Error", "Error:::" + error.getMessage());

            }
        }) {

            @Override
            protected Map<String, String> getParams()
                    throws AuthFailureError {
                // TODO Auto-generated method stub
                Log.d("Param", "Param " + param);
                return param;
            }

        };
        int socketTimeout = 60000;// 30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        objectRequest.setRetryPolicy(policy);
        // Add the request to the RequestQueue.
        AppSingleton.getInstance(context).getRequestQueue().add(objectRequest);
    }

    private void callGetRequest() {
        Utility.showDialog(context);
        StringRequest objectRequest = new StringRequest(
                Request.Method.GET, url,
                new Response.Listener<String>() {

                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        // TODO Auto-generated method stub
                        try {
                            Utility.hideDialog();
                            String resRemovedNull = String.valueOf(Utility.convertNull(new JSONObject(response)));
                            Log.d("after remove null " + url, "REsponse:::remov null "
                                    + resRemovedNull);
                            responseCallBack.onResult(resRemovedNull.toString(),context,labelFor);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Utility.hideDialog();
                Log.e("Error", "Error:::" + error.getMessage());

            }
        });
        int socketTimeout = 60000;// 30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        objectRequest.setRetryPolicy(policy);
        // Add the request to the RequestQueue.
        AppSingleton.getInstance(context).getRequestQueue().add(objectRequest);
    }
}
