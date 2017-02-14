package com.everfox.aozoraforums.activities;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.utils.AoUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Calendar;

/**
 * Created by daniel.soto on 2/14/2017.
 */

public class AozoraActivity extends AppCompatActivity {

    private static int sessionDepth = 0;
    Boolean comesFromBackground= true;

    @Override
    protected void onStop() {
        super.onStop();
        if (sessionDepth > 0)
            sessionDepth--;

        if (sessionDepth == 0) {
            comesFromBackground = true;
            ParseUser user = ParseUser.getCurrentUser();
            if(user != null) {
                user.put(ParseUserColumns.ACTIVE, false);
                user.put(ParseUserColumns.ACTIVE_END, Calendar.getInstance().getTime());
                user.saveInBackground();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        sessionDepth++;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(comesFromBackground) {
            comesFromBackground = false;
            ParseUser user = ParseUser.getCurrentUser();
            if(user != null) {
                user.put(ParseUserColumns.ACTIVE, true);
                user.put(ParseUserColumns.ACTIVE_START, Calendar.getInstance().getTime());
                user.saveInBackground();
            }
        }
    }

}
