package com.pine.emeter.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;
import com.pine.emeter.R;
import com.pine.emeter.adapter.MeterTypeAdapter;
import com.pine.emeter.controlList.ListControl;
import com.pine.emeter.model.MeterTypeModel;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity implements View.OnClickListener, ResponseCallBack {

    EditText firstname, lastname, email, contactNumber, address, customerZone, meterNumber, metertype;
    Button register;
    ArrayList<String> list = new ArrayList<>();
    Gson gson= new Gson();
    private String meterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);
        init();
        ListControl.meterList.clear();
        getMeterType();
    }

    private void getMeterType() {
        Map params = new HashMap<String, String>();
        new ApiService(this, ServerUtility.url.GetMeterType, params, Register.this, Constant.GET_REQUEST, ServerUtility.webServiceName.GetMeterType);

    }

    private void init() {
        firstname = findViewById(R.id.first);
        lastname = findViewById(R.id.last);
        email = findViewById(R.id.email);
        contactNumber = findViewById(R.id.number);
        address = findViewById(R.id.address);
        customerZone = findViewById(R.id.cuszone);
        meterNumber = findViewById(R.id.meternumber);
        metertype = findViewById(R.id.metertype);
        metertype.setOnClickListener(this);
        register = findViewById(R.id.submit);
        register.setOnClickListener(this);
        list.add("Domestic");
        list.add("Commercial");
        list.add("Other");
    }

    @Override
    public void onClick(View v) {
        if (v == register) {
            doValidate();
        }
        if (v == metertype) {
            selectType();
        }
    }

    private void selectType() {
        final MeterTypeAdapter adapter = new MeterTypeAdapter(Register.this, R.layout.specific_item, ListControl.meterList);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView lw = ((AlertDialog) dialog).getListView();
                        String text = ListControl.meterList.get(which).getType();
                        meterId = ListControl.meterList.get(which).getId();
                        Log.d("meterid",meterId);
                        metertype.setText(text);
                    }
                });
        builder.create();
        builder.show();
    }

    private void doValidate() {
        if (ValidationUtils.isEmpty(firstname)) {
            Utility.showSimpleDialogBox(this, getString(R.string.enter_first_name));
        } else if (ValidationUtils.isEmpty(lastname)) {
            Utility.showSimpleDialogBox(this, "Please enter last name");
        } else if (ValidationUtils.isValidEmail(email)) {
            Utility.showSimpleDialogBox(this, "Please enter valid email");
        } else if (ValidationUtils.isEmpty(contactNumber)) {
            Utility.showSimpleDialogBox(this, "Please enter Contact number");
        } else if (ValidationUtils.isEmpty(address)) {
            Utility.showSimpleDialogBox(this, "Please enter address");
        } else if (ValidationUtils.isEmpty(customerZone)) {
            Utility.showSimpleDialogBox(this, "Please enter customer zone");
        } else if (ValidationUtils.isEmpty(metertype)) {
            Utility.showSimpleDialogBox(this, "Please enter meter type");
        } else if (ValidationUtils.isEmpty(meterNumber)) {
            Utility.showSimpleDialogBox(this, "Please enter meter number");
        } else {
            doRegister();
        }
    }

    private void doRegister() {
        Map params = new HashMap<String, String>();
        params.put("fname", firstname.getText().toString());
        params.put("lname", lastname.getText().toString());
        params.put("email", email.getText().toString());
        params.put("contact", contactNumber.getText().toString());
        params.put("address", address.getText().toString());
        params.put("customer_zone", customerZone.getText().toString());
        params.put("fcm_id", "asdf");
        params.put("meter_no", meterNumber.getText().toString());
        params.put("meter_type", meterId);
        new ApiService(this, ServerUtility.url.RegisterUrl, params, Register.this, Constant.POST_REQUEST, ServerUtility.webServiceName.Register);
    }

    @Override
    public void onResult(String response, Context context, String labelFor) {
        if (labelFor.equals(ServerUtility.webServiceName.Register)) {
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
                        //new SessionManager(context).createUserLoginSession();
                        Utility.ShowToast(context,message);
                        Intent intent = new Intent(context,Login.class);
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
        if (labelFor.equals(ServerUtility.webServiceName.GetMeterType)){
            try {
                JSONObject object = new JSONObject(response);
                boolean success = object.getBoolean("result");
                String message = object.getString("message");
                if (success) {
                    JSONArray jsonArray = object.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        MeterTypeModel model = gson.fromJson(jsonObject.toString(), MeterTypeModel.class);
                        ListControl.meterList.add(model);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
