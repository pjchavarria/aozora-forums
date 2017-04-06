package com.everfox.aozoraforums.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by daniel.soto on 1/10/2017.
 */

public class PurchaseUtils {

    public static final String PRODUCT_NO_ADS = "no_ads_forum";
    public static final String PRODUCT_PRO = "unlock_all";
    public static final String PRODUCT_CHANGE_USERNAME = "change_username";

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

    public static boolean purchasedProduct(Context context, String productID) {
        SharedPreferences settings = context.getSharedPreferences(PreferencesUtils.PREFS_INAPP, 0);
        Boolean pro = settings.getBoolean(PRODUCT_PRO, false);
        Boolean product =  settings.getBoolean(productID, false);
        return (pro || product);
    }

}
