package com.everfox.aozoraforums.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.everfox.aozoraforums.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel.soto on 1/16/2017.
 */

public class OptionListAdapter extends RecyclerView.Adapter<OptionListAdapter.ListViewHolder> {

    List<String> lstOptions;


    Context mContext;


    public OptionListAdapter(ArrayList<String> contents, Context context) {
        this.lstOptions = contents;
        this.mContext = context;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = null;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dialogfragment_listoptions_item, parent, false);
        return new OptionListAdapter.ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        String listName = lstOptions.get(position);
        holder.tvSeasonTitle.setText(listName);
    }

    @Override
    public int getItemCount() {
        return lstOptions.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        public TextView tvSeasonTitle;
        public ListViewHolder(View v) {
            super(v);
            tvSeasonTitle = (TextView) v.findViewById(R.id.tvListTitle);
        }
    }
}