package com.everfox.aozoraforums.dialogfragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.parse.ParseUser;

import java.util.Calendar;

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
                Integer days = isNumeric(editText.getText().toString());
                if(days == null)
                    Toast.makeText(context,"Your mute duration is too long or you have entered characters.",Toast.LENGTH_SHORT).show();
                else {
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.DAY_OF_YEAR,days);
                    userToMute.getParseObject("details").put("mutedUntil",c.getTime());
                    userToMute.saveInBackground();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle("Muted user").setMessage("You have muted " + userToMute.getString(ParseUserColumns.AOZORA_USERNAME));
                    builder.create().show();
                    dismiss();
                }
            }
        });

        return view;
    }

    public static Integer isNumeric(String str)
    {
        Integer integer = null;
        try {
            integer = Integer.parseInt(str);
        }
        catch(NumberFormatException nfe) {
            return null;
        }
        return integer;
    }
}
