package com.everfox.aozoraforums.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.models.Anime;
import com.everfox.aozoraforums.models.AoThreadTag;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoUtils;
import com.parse.ParseObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 3/13/2017.
 */

public class SelectTagAdapter extends RecyclerView.Adapter<SelectTagAdapter.ViewHolderTag> {

    public static final int THREAD_TAG = 0;
    public static final int ANIME = 1;

    private List<Anime> lstAnime;
    private List<AoThreadTag> lstTags;
    private Context context;
    private int type;

    private OnItemTappedListener mOnItemTappedListener;
    public interface OnItemTappedListener {
        public void mOnItemTappedListener(ParseObject itemTapped);
    }

    public SelectTagAdapter (Context context, List<AoThreadTag> lstTags, List<Anime> lstAnime, Activity callback) {
        this.context = context;
        if(lstTags == null) {
            type = ANIME;
        } else {
            type = THREAD_TAG;
        }
        this.lstTags = lstTags;
        this.lstAnime = lstAnime;
        mOnItemTappedListener = (OnItemTappedListener) callback;
    }


    @Override
    public ViewHolderTag onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v1 = inflater.inflate(R.layout.layout_selectag_item, parent, false);
        ViewHolderTag viewHolder = new ViewHolderTag(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolderTag holder, int position) {
        switch (type) {
            case THREAD_TAG:
                final AoThreadTag aoThreadTag = lstTags.get(position);
                holder.tvTitle.setText("#"+aoThreadTag.getString(AoThreadTag.NAME));
                holder.tvDesc.setText(aoThreadTag.getString(AoThreadTag.DETAIL));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemTappedListener.mOnItemTappedListener(aoThreadTag);
                    }
                });
                break;
            case ANIME:

                final Anime anime = lstAnime.get(position);
                holder.tvTitle.setText("#"+anime.getString(Anime.TITLE));
                String type = anime.getString(Anime.TYPE);
                String episodes = AoUtils.numberToStringOrZero(anime.getNumber(Anime.EPISODES));
                String duration = AoUtils.numberToStringOrInterrogation(anime.getNumber(Anime.DURATION));
                String animeDesc = type + " - " + episodes + " eps - " + duration + "min";
                holder.tvDesc.setText(animeDesc);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemTappedListener.mOnItemTappedListener(anime);
                    }
                });
                break;
        }

    }

    @Override
    public int getItemCount() {
        if(type == ANIME)
            return lstAnime.size();
        else
            return lstTags.size();
    }

    public static class ViewHolderTag extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle) TextView tvTitle;
        @BindView(R.id.tvDesc)
        TextView tvDesc;

        public ViewHolderTag(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
