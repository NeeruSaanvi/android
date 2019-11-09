package com.pine.emeter.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.pine.emeter.R;
import com.pine.emeter.model.HistoryModel;
import com.pine.emeter.model.RegistrationModel;
import com.pine.emeter.utils.AppPreferences;
import com.pine.emeter.utils.Utility;
import com.pine.emeter.weService.ServerUtility;
import com.squareup.picasso.Picasso;

public class BillDetail extends AppCompatActivity {

    TextView no, date, name, phone, address, zone, number , cread , pread , cost;
    ImageView image;
    Gson gson = new Gson();
    private HistoryModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        model = gson.fromJson(getIntent().getStringExtra("model"), HistoryModel.class);
        init();
    }

    private void init() {
        no = findViewById(R.id.no);
        date = findViewById(R.id.date);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        address = findViewById(R.id.address);
        zone = findViewById(R.id.zone);
        number = findViewById(R.id.number);
        image = findViewById(R.id.image);
        cread = findViewById(R.id.cread);
        pread = findViewById(R.id.pread);
        cost = findViewById(R.id.cost);
        setValue();
    }

    private void setValue() {
        no.setText(model.getReceipt_no());
        date.setText(Utility.getLocalDate(model.getReceipt_date_time())+ " "+ Utility.getLocalTime(model.getReceipt_date_time()));
        name.setText(model.getCustomer_name());
        phone.setText(model.getCustomer_phone());
        address.setText(model.getCustomer_address());
        zone.setText(model.getCustomer_zone());
        number.setText(model.getMeter_no());
        cread.setText(model.getCurrent_Reading());
        pread.setText(model.getPrevious_Reading());
        cost.setText("$"+model.getReading_Cost());
        Picasso.get().load(ServerUtility.url.BASE_URL + model.getMeter_image())
                .placeholder(R.drawable.placeholder)
                .into(image);
    }
}
