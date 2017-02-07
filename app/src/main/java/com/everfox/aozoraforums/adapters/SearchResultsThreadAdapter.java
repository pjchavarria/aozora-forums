package com.everfox.aozoraforums.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.ThreadActivity;
import com.everfox.aozoraforums.models.AoThread;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/7/2017.
 */

public class SearchResultsThreadAdapter extends RecyclerView.Adapter  {


    private List<AoThread> lstThreads;
    private Context context;

    public SearchResultsThreadAdapter (Context context,List<AoThread> lst) {
        this.context = context;
        this.lstThreads = lst;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(context).inflate(R.layout.layout_searchthread_item,parent,false);
        vh = new ThreadViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ThreadViewHolder viewHolder = (ThreadViewHolder)holder;
        final AoThread aoThread = lstThreads.get(position);
        viewHolder.tvThreadTitle.setText(aoThread.getString(AoThread.TITLE));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aoThread.setHasMenu(false);
                AozoraForumsApp.setThreadToPass(aoThread);
                Intent i = new Intent(context, ThreadActivity.class);
                context.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return lstThreads.size();
    }

    public static class ThreadViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tvThreadTitle)
        TextView tvThreadTitle;
        public ThreadViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
