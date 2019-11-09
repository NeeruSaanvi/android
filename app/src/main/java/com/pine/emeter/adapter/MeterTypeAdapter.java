package com.pine.emeter.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pine.emeter.R;
import com.pine.emeter.model.MeterTypeModel;

import java.util.ArrayList;

public class MeterTypeAdapter extends ArrayAdapter<MeterTypeModel> {
    ArrayList<MeterTypeModel> list= new ArrayList<>();

    public MeterTypeAdapter(Context context, int textViewResourceId, ArrayList<MeterTypeModel> objects) {
        super(context, textViewResourceId, objects);
        list = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.specific_item, null);
        TextView textView = (TextView) v.findViewById(R.id.text);
        textView.setText(list.get(position).getType());
        return v;
    }

}
