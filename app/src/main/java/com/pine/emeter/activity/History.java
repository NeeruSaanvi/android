package com.pine.emeter.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.pine.emeter.R;
import com.pine.emeter.adapter.HistoryAdapter;
import com.pine.emeter.controlList.ListControl;
import com.pine.emeter.model.HistoryModel;
import com.pine.emeter.model.RegistrationModel;
import com.pine.emeter.utils.AppPreferences;
import com.pine.emeter.utils.Constant;
import com.pine.emeter.utils.SessionManager;
import com.pine.emeter.utils.Utility;
import com.pine.emeter.weService.ApiService;
import com.pine.emeter.weService.LogoutSession;
import com.pine.emeter.weService.ResponseCallBack;
import com.pine.emeter.weService.ServerUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class History extends AppCompatActivity implements View.OnClickListener, ResponseCallBack {
    private NavigationView navigationView;
    private DrawerLayout drawer;
    // tags used to attach the fragments
    private Toolbar toolbar;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter recyclerAdapter;
    ImageView toggle;
    RegistrationModel model;
    Gson gson = new Gson();
    ArrayList<HistoryModel> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setTitle("Billing History");
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
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setHasFixedSize(true);
        ListControl.historyList.clear();
        getBillHistory();
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        recyclerAdapter = new HistoryAdapter(this, ListControl.historyList, new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(HistoryModel item) {
                String model = gson.toJson(item);
                Intent intent = new Intent(History.this,BillDetail.class);
                intent.putExtra("model",model);
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    private void getBillHistory() {
        Map params = new HashMap<String, String>();
        params.put("meter_id", model.getMeter_id());
        params.put("user_id", model.getId());
        new ApiService(this, ServerUtility.url.History, params, History.this, Constant.POST_REQUEST, ServerUtility.webServiceName.History);

    }

    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.profile:
                        Intent intent0 = new Intent(History.this, MyProfile.class);
                        startActivity(intent0);
                        break;
                    case R.id.history:
                        Intent intent1 = new Intent(History.this, History.class);
                        startActivity(intent1);
                        break;
                    case R.id.scan:
                        Intent intent2 = new Intent(History.this, HomeActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.logout:
                        new LogoutSession().doLogout(History.this, model.getEmail_id());
                        break;
                    default:
                        Intent intent3 = new Intent(History.this, HomeActivity.class);
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
            drawer.closeDrawer(GravityCompat.START);
        } else if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
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
                    ListControl.historyList.add(model);
                }
            } else {
                Utility.showSimpleDialogBox(context, message);
            }
            recyclerAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
