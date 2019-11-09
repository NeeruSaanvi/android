package com.pine.emeter.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.pine.emeter.R;
import com.pine.emeter.model.RegistrationModel;
import com.pine.emeter.utils.AppPreferences;
import com.pine.emeter.utils.Constant;
import com.pine.emeter.utils.Utility;
import com.pine.emeter.validation.ValidationUtils;
import com.pine.emeter.weService.ApiService;
import com.pine.emeter.weService.LogoutSession;
import com.pine.emeter.weService.ResponseCallBack;
import com.pine.emeter.weService.ServerUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyProfile extends AppCompatActivity implements View.OnClickListener, ResponseCallBack {
    private NavigationView navigationView;
    private DrawerLayout drawer;
    ImageView toggle;
    // tags used to attach the fragments
    private Toolbar toolbar;
    Gson gson = new Gson();
    private RegistrationModel model;
    Button register;
    EditText firstname, lastname, email, contactNumber, address, customerZone, meterNumber, metertype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        setTitle(getString(R.string.my_profile));
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        model = gson.fromJson(AppPreferences.getInstance(this).getRegistrationModel(), RegistrationModel.class);
        setSupportActionBar(toolbar);
        setUpNavigationView();
        init();
    }

    private void init() {
        toggle = findViewById(R.id.toggle);
        toggle.setOnClickListener(this);
        firstname = findViewById(R.id.first);
        lastname = findViewById(R.id.last);
        email = findViewById(R.id.email);
        email.setFocusableInTouchMode(false);
        contactNumber = findViewById(R.id.number);
        address = findViewById(R.id.address);
        customerZone = findViewById(R.id.cuszone);
        meterNumber = findViewById(R.id.meternumber);
        meterNumber.setFocusableInTouchMode(false);
        metertype = findViewById(R.id.metertype);
        metertype.setFocusableInTouchMode(false);
        register = findViewById(R.id.submit);
        register.setOnClickListener(this);
        setvalue();
    }

    private void setvalue() {
        firstname.setText(model.getFirst_name());
        lastname.setText(model.getLast_name());
        email.setText(model.getEmail_id());
        contactNumber.setText(model.getContact_number());
        address.setText(model.getAddress());
        customerZone.setText(model.getCustomer_zone());
        meterNumber.setText(model.getMeter_no());
        metertype.setText(model.getMeter_type());
        firstname.setText(model.getFirst_name());
    }

    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.profile:
                        Intent intent0 = new Intent(MyProfile.this, MyProfile.class);
                        intent0.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent0);
                        break;
                    case R.id.history:
                        Intent intent1 = new Intent(MyProfile.this, History.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent1);
                        break;
                    case R.id.scan:
                        Intent intent2 = new Intent(MyProfile.this, HomeActivity.class);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent2);
                        break;
                    case R.id.logout:
                        new LogoutSession().doLogout(MyProfile.this, model.getEmail_id());
                        break;
                    default:
                        Intent intent3 = new Intent(MyProfile.this, HomeActivity.class);
                        intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent3);
                }
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                item.setChecked(true);

                return true;
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if (v == toggle) {
            if (drawer.isDrawerOpen(Gravity.RIGHT)) {
                drawer.closeDrawer(Gravity.RIGHT);
            } else {
                drawer.openDrawer(Gravity.RIGHT);
            }
        }
        if (v == register) {
            doValidate();
        }
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
        new ApiService(this, ServerUtility.url.UpdateUser, params, MyProfile.this, Constant.POST_REQUEST, ServerUtility.webServiceName.UpdateUser);

    }

    @Override
    public void onResult(String response, Context context, String labelFor) {
        if (labelFor.equals(ServerUtility.webServiceName.UpdateUser)) {
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
                        Utility.ShowToast(context,message);
                        Intent intent = new Intent(context,MyProfile.class);
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
    }
}
