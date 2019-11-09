package com.pine.emeter.model;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.mvc.imagepicker.ImagePicker;
import com.pine.emeter.R;
import com.pine.emeter.activity.Blank;
import com.pine.emeter.activity.Login;
import com.pine.emeter.utils.AppPreferences;
import com.pine.emeter.utils.Constant;
import com.pine.emeter.weService.ApiService;
import com.pine.emeter.weService.ImageWebService;
import com.pine.emeter.weService.ResponseCallBack;
import com.pine.emeter.weService.ServerUtility;
import com.pine.emeter.weService.VolleyMultipartRequest;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ScanMeter extends AppCompatActivity implements ResponseCallBack {
    Gson gson = new Gson();
    private RegistrationModel model;
    private String text;

    public void ScanReading(final Context context, String reading) {
        model = gson.fromJson(AppPreferences.getInstance(context).getRegistrationModel(), RegistrationModel.class);
        LayoutInflater inflater = LayoutInflater.from(context);
        View subView = inflater.inflate(R.layout.custom_edit, null);
        final EditText subEditText = (EditText) subView.findViewById(R.id.edit);
        subEditText.setText(reading);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Is " + reading + " your meter reading?. You can edit this reading below.");
        builder.setView(subView);
        final AlertDialog alertDialog = builder.create();
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                text = subEditText.getText().toString();
                Intent intent = new Intent(context,Blank.class);
                intent.putExtra("reading",text);
                context.startActivity(intent);
               // ImagePicker.pickImage((Activity) context, "Click your meter picture",1,false);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        builder.show();
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

        new ImageWebService(this, ServerUtility.url.ScanMeter, params,paramsImg,  ScanMeter.this,  ServerUtility.webServiceName.ScanMeter);
    }

    @Override
    public void onResult(String response, Context context, String labelFor) {

    }
}
