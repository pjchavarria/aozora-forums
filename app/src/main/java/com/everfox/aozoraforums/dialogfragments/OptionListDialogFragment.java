package com.everfox.aozoraforums.dialogfragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.adapters.OptionListAdapter;
import com.everfox.aozoraforums.controls.AoLinearLayoutManager;
import com.everfox.aozoraforums.utils.AoConstants;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by daniel.soto on 1/16/2017.
 */

public class OptionListDialogFragment extends DialogFragment {

    RecyclerView rvLists;
    TextView tvDialogTitle;
    TextView tvDialogSubTitle1;
    TextView tvDialogSubTitle2;
    Context mContext;
    ArrayList<String> options;
    String title;
    String subtitle1;
    String subtitle2;
    Integer selectedList;
    LinearLayout llTitles;
    LinearLayout llTitleSingleLine;


    private OnListSelectedListener mListSelectedCallback;

    public interface OnListSelectedListener {
        public void onListSelected(Integer list,Integer selectedList);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public static OptionListDialogFragment newInstance(Context context, String title , String subtitle1, String subtitle2, Fragment callback, Integer selectedList, Activity callbackActivity) {
        OptionListDialogFragment frag = new OptionListDialogFragment();
        frag.selectedList = selectedList;
        frag.mContext = context;
        frag.title = title;
        frag.subtitle1 = subtitle1;
        frag.subtitle2 = subtitle2;
        Bundle args = new Bundle();
        List<String> _options = null;
        _options = AoUtils.getOptionListFromID(context,selectedList);

        args.putSerializable("options",new ArrayList<>(_options));
        frag.setArguments(args);
        if(callback != null)
            frag.mListSelectedCallback = (OnListSelectedListener) callback;
        else
            frag.mListSelectedCallback = (OnListSelectedListener) callbackActivity;

        return frag;
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_listoptions,null);

        Boolean isSingleLine = selectedList == AoConstants.SORT_OPTIONS_DIALOG;


        Bundle bundle = getArguments();
        options = (ArrayList<String>)bundle.getSerializable("options");
        rvLists = (RecyclerView)view.findViewById(R.id.rvList);
        rvLists.setLayoutManager(new AoLinearLayoutManager(mContext));
        rvLists.setAdapter(new OptionListAdapter(options, mContext));
        rvLists.addOnItemTouchListener(new RecyclerItemClickListener
                (mContext, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        dismiss();
                        mListSelectedCallback.onListSelected(position,selectedList);
                    }
                }));
        tvDialogTitle = (TextView) view.findViewById(R.id.tvDialogTitle);
        tvDialogSubTitle1 = (TextView) view.findViewById(R.id.tvDialogSubTitle1);
        tvDialogSubTitle2 = (TextView)view.findViewById(R.id.tvDialogSubTitle2);
        llTitles = (LinearLayout) view.findViewById(R.id.llTitles);
        llTitleSingleLine = (LinearLayout) view.findViewById(R.id.llTitleSingleLine);


        if(isSingleLine) {
            llTitles.setVisibility(View.GONE);
            llTitleSingleLine.setVisibility(View.VISIBLE);
            tvDialogTitle = (TextView) view.findViewById(R.id.tvDialogSingleLineTitle);
        }

        if(title == null && subtitle1 == null && subtitle2 == null) {
            llTitles.setVisibility(View.GONE);
        } else {
            if (title != null) {
                tvDialogTitle.setText(title);
            } else {
                tvDialogTitle.setVisibility(View.GONE);
            }
            if (subtitle1 != null) {
                tvDialogSubTitle1.setText(subtitle1);
            } else {
                tvDialogSubTitle1.setVisibility(View.GONE);
            }

            if (subtitle2 != null) {
                tvDialogSubTitle2.setText(subtitle2);
            } else {
                tvDialogSubTitle2.setVisibility(View.GONE);
            }
        }




        return view;
    }
}
