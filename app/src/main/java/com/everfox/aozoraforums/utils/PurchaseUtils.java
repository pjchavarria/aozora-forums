package com.everfox.aozoraforums.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by daniel.soto on 1/10/2017.
 */

public class PurchaseUtils {


    public static void purchaseProduct(Context context, String productID) {
        SharedPreferences settings = context.getSharedPreferences(PreferencesUtils.PREFS_INAPP, 0);
        SharedPreferences.Editor edit= settings.edit();
        edit.putBoolean(productID, true);
        edit.commit();
    }

    public static void deletePurchases(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PreferencesUtils.PREFS_INAPP, 0);
        settings.edit().clear().commit();
    }
}
