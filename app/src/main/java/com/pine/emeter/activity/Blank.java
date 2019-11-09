package com.pine.emeter.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.Gson;
import com.mvc.imagepicker.ImagePicker;
import com.pine.emeter.R;
import com.pine.emeter.controlList.ListControl;
import com.pine.emeter.model.HistoryModel;
import com.pine.emeter.model.RegistrationModel;
import com.pine.emeter.model.ScanMeter;
import com.pine.emeter.utils.AppPreferences;
import com.pine.emeter.utils.SessionManager;
import com.pine.emeter.utils.Utility;
import com.pine.emeter.weService.ImageWebService;
import com.pine.emeter.weService.ResponseCallBack;
import com.pine.emeter.weService.ServerUtility;
import com.pine.emeter.weService.VolleyMultipartRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Blank extends AppCompatActivity  implements ResponseCallBack {
    private RegistrationModel model;
    private String text;
    Gson gson= new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        model = gson.fromJson(AppPreferences.getInstance(this).getRegistrationModel(), RegistrationModel.class);

        text = getIntent().getStringExtra("reading");
        text = text.replaceAll("[^0-9]+", "");
        ImagePicker.pickImage(this, "Click your meter picture",1,false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                if (bitmap != null) {
                    //image.setImageBitmap(bitmap);
                    UploadMeterReading(bitmap);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void UploadMeterReading(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        Map params = new HashMap<String, String>();
        params.put("meter_id",model.getMeter_id());
        params.put("user_id",model.getId());
        params.put("meter_reading",text);

        Map paramsImg = new HashMap<String, VolleyMultipartRequest.DataPart>();
        paramsImg.put("meter_image", new VolleyMultipartRequest.DataPart("file_avatar.jpg", byteArrayOutputStream.toByteArray(), "image/jpeg"));

        new ImageWebService(this, ServerUtility.url.ScanMeter, params,paramsImg,  Blank.this,  ServerUtility.webServiceName.ScanMeter);
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
                    HistoryModel model = gson.fromJson(jsonObject.toString(), HistoryModel.class);
                    finish();
                    Intent intent = new Intent(context,BillDetail.class);
                    intent.putExtra("model",gson.toJson(model));
                    startActivity(intent);
                }
            } else {
                onBackPressed();
                Utility.ShowToast(context,message);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
