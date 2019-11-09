package com.pine.emeter.weService;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pine.emeter.model.ScanMeter;
import com.pine.emeter.utils.Constant;
import com.pine.emeter.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ImageWebService {
    Context context;
    String url;
    int reqType;
    String labelFor;
    Map param;
    Map paramsImg;
    ResponseCallBack responseCallBack;

    public ImageWebService(Context context, String url, Map param, Map paramsImg, ResponseCallBack responseCallBack, String labelFor) {
        this.context = context;
        this.url = url;
        this.param = param;
        this.paramsImg = paramsImg;
        this.reqType = reqType;
        this.responseCallBack = responseCallBack;
        this.labelFor = labelFor;
        if (Utility.isInternetAvailable(context)) {
            callRequest();
        } else {
            Utility.showSimpleDialogBox(context, "Internet Not Available");

        }

    }

    private void callRequest() {
        Utility.showDialog(context);
        VolleyMultipartRequest objectRequest = new VolleyMultipartRequest(
                Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {

                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(NetworkResponse response) {
                        // TODO Auto-generated method stub
                        try {
                            Utility.hideDialog();
                            String resRemovedNull = String.valueOf(Utility.convertNull(new JSONObject(new String(response.data))));
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

            @Override
            protected Map<String, DataPart> getByteData() {
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                //params.put("profile", new DataPart("file_cover.jpg", AppHelper.getFileDataFromDrawable(getBaseContext(), mCoverImage.getDrawable()), "image/jpeg"));

                return paramsImg;
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
}
