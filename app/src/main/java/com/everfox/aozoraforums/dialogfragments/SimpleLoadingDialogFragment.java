package com.everfox.aozoraforums.dialogfragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by Daniel on 30/06/2016.
 */
public class SimpleLoadingDialogFragment extends DialogFragment {

    public SimpleLoadingDialogFragment() {
        // use empty constructors. If something is needed use onCreate's
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {


        ProgressDialog dialog = new ProgressDialog(getActivity(),getTheme());
        this.setStyle(STYLE_NO_TITLE, getTheme()); // You can use styles or inflate a view
        dialog.setMessage("Please wait...");
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        return dialog;
    }
}

