package com.pine.emeter.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pine.emeter.R;
import com.pine.emeter.model.RegistrationModel;
import com.pine.emeter.scanMeter.OcrCaptureActivity;
import com.pine.emeter.utils.AppPreferences;
import com.pine.emeter.utils.Utility;
import com.pine.emeter.weService.LogoutSession;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private NavigationView navigationView;
    private DrawerLayout drawer;
    ImageView toggle;
    LinearLayout scan;
    // tags used to attach the fragments
    private Toolbar toolbar;
    Gson gson = new Gson();
    private RegistrationModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUpNavigationView();
        init();
        model = gson.fromJson(AppPreferences.getInstance(this).getRegistrationModel(), RegistrationModel.class);
        Log.d("name",model.getFirst_name()+" "+model.getMeter_no());
    }

    private void init() {
        toggle = findViewById(R.id.toggle);
        scan = findViewById(R.id.scan);
        scan.setOnClickListener(this);
        toggle.setOnClickListener(this);
    }

    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                switch (item.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.profile:
                        Intent intent0 = new Intent(HomeActivity.this, MyProfile.class);
                        startActivity(intent0);
                        break;
                    case R.id.history:
                        Intent intent1 = new Intent(HomeActivity.this, History.class);
                        startActivity(intent1);
                        break;
                    case R.id.scan:
                        Intent intent2 = new Intent(HomeActivity.this, HomeActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.logout:
                        new LogoutSession().doLogout(HomeActivity.this,model.getEmail_id());
                        break;
                    default:
                        Intent intent3 = new Intent(HomeActivity.this, HomeActivity.class);
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
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
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
        if (v == scan) {
            if (isPermissionGranted()) {
                Intent intent = new Intent(this, OcrCaptureActivity.class);
                startActivity(intent);
            }
        }
    }

    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED )
                    {
                Log.v("TAG", "Permission is granted");
                return true;
            } else {

                Log.v("TAG", "Permission is revoked");
                ActivityCompat.requestPermissions(this, Utility.PERMISSIONS, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG", "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Intent intent = new Intent(this, ScanReading.class);
                    startActivity(intent);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
