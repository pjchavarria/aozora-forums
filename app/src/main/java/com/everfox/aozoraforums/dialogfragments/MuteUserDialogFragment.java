package com.everfox.aozoraforums.dialogfragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.everfox.aozoraforums.R;
import com.parse.ParseUser;

/**
 * Created by daniel.soto on 2/10/2017.
 */

public class MuteUserDialogFragment extends DialogFragment {


    Context context;
    ParseUser userToMute;
    EditText editText;

    public static MuteUserDialogFragment newInstance(Context context, ParseUser userToMute) {
        MuteUserDialogFragment frag = new MuteUserDialogFragment();
        frag.userToMute = userToMute;
        frag.context = context;
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_muteuser,null);
        editText = (EditText) view.findViewById(R.id.etMuteDays);
        view.findViewById(R.id.tvCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        view.findViewById(R.id.tvSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int days = Integer.valueOf(editText.getText().toString());
                Toast.makeText(context,String.valueOf(days) + "Muted",Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
