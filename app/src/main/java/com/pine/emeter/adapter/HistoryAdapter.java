package com.pine.emeter.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pine.emeter.R;
import com.pine.emeter.model.HistoryModel;
import com.pine.emeter.utils.Utility;
import com.pine.emeter.weService.ServerUtility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by PinesucceedAndroid on 6/21/2018.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public ArrayList<HistoryModel> list = new ArrayList<HistoryModel>();
    Context context;
    private final OnItemClickListener listener;
    int frame;

    public interface OnItemClickListener {
        void onItemClick(HistoryModel item);
    }

    public HistoryAdapter(Context context, ArrayList<HistoryModel> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
        this.frame = frame;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_history, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.bind(list.get(position), listener);
        HistoryModel model = list.get(position);
        String localDate = Utility.getLocalDate(model.getReceipt_date_time());
        holder.date.setText(localDate);
        String localTime = Utility.getLocalTime(model.getReceipt_date_time());
        holder.time.setText(localTime);
        holder.meter.setText(model.getCurrent_Reading());
        Picasso.get().load(ServerUtility.url.BASE_URL + model.getMeter_image())
                .placeholder(R.drawable.placeholder)
                .into(holder.image);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView date, time, meter;
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
            meter = itemView.findViewById(R.id.meter);
            image = itemView.findViewById(R.id.image);
        }


        public void bind(final HistoryModel sitterList, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(sitterList);
                }
            });
        }
    }

}
