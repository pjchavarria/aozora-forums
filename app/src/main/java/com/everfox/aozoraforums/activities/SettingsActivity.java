package com.everfox.aozoraforums.activities;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.util.IabHelper;
import com.everfox.aozoraforums.util.IabResult;
import com.everfox.aozoraforums.util.Inventory;
import com.everfox.aozoraforums.util.Purchase;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PurchaseUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AozoraActivity {

    String AOTRACKING_PACKAGE ="com.everfox.animetrackerandroid";
    String AODISCOVER_PACKAGE ="";
    String AOTALK_PACKAGE ="";
    SharedPreferences sharedPreferences;
    public static String FACEBOOK_URL = "https://m.facebook.com/AozoraApp";
    public static String FACEBOOK_PAGE_ID = "713541968752502";

    IabHelper mHelper;

    @BindView(R.id.tvVersion)
    TextView tvVersion;
    @BindView(R.id.tvLogout)
    TextView tvLogout;
    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.tvRemoveAds)
    TextView tvRemoveAds;
    @BindView(R.id.tvChangeUsername)
    TextView tvChangeUsername;
    @BindView(R.id.tvRate)
    TextView tvRate;
    @BindView(R.id.tvAotracking)
    TextView tvAotracking;
    @BindView(R.id.tvAoTrackingWhat)
    TextView tvAoTrackingWhat;
    @BindView(R.id.tvAoDiscover)
    TextView tvAoDiscover;
    @BindView(R.id.tvAoDiscoverWhat)
    TextView tvAoDiscoverWhat;
    @BindView(R.id.tvAoTalk)
    TextView tvAoTalk;
    @BindView(R.id.tvAoTalkWhat)
    TextView tvAoTalkWhat;
    @BindView(R.id.tvLikeFacebook)
    TextView tvLikeFacebook;
    @BindView(R.id.tvRestorePurchases)
    TextView tvRestorePurchases;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mHelper = new IabHelper(this, getString(R.string.bepk));
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                }
            }
        });

        if (!PurchaseUtils.purchasedProduct(this, PurchaseUtils.PRODUCT_NO_ADS)) {
            AdView mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        ButterKnife.bind(this);
        setTitle("Settings");
        Typeface typeface = AozoraForumsApp.getAwesomeTypeface();
        tvAoDiscoverWhat.setTypeface(typeface);
        tvAoTalkWhat.setTypeface(typeface);
        tvAoTrackingWhat.setTypeface(typeface);

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AoUtils.logout(SettingsActivity.this);
            }
        });

        try {
            tvVersion.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        
        tvRemoveAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAdsClicked();
            }
        });

        tvChangeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeUsernameClicked();
            }
        });

        tvRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMarket(getPackageName());
            }
        });

        tvAotracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appRowClicked(AOTRACKING_PACKAGE);
            }
        });

        tvAoTrackingWhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogWithText(R.string.aotracking_desc);
            }
        });

        tvAoDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appRowClicked(AODISCOVER_PACKAGE);
            }
        });

        tvAoDiscoverWhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogWithText(R.string.aodiscover_desc);
            }
        });

        tvAoTalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appRowClicked(AOTALK_PACKAGE);
            }
        });

        tvAoTalkWhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogWithText(R.string.app_inconstruction);
            }
        });


        tvLikeFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                facebookIntent.setData(Uri.parse(FACEBOOK_URL));
                startActivity(facebookIntent);
            }
        });

        tvRestorePurchases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restorePurchases();
                showDialogWithText("#KvnWillDoIt");
            }
        });
    }

    private void appRowClicked(String packageName) {

        if(appInstalledOrNot(packageName)) {
            runApp(packageName);
        } else {
            goToMarket(packageName);
        }
    }


    private void changeUsernameClicked() {

        //#kvn93willdoit
    }

    private void removeAdsClicked() {

        //#kvn93willdoit
        List additionalSkuList = new ArrayList();
        additionalSkuList.add(PurchaseUtils.PRODUCT_NO_ADS);

        IabHelper.QueryInventoryFinishedListener
                mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory)
            {
                if (result.isFailure()) {
                    // handle error
                    return;
                }

                String applePrice =
                        inventory.getSkuDetails(PurchaseUtils.PRODUCT_NO_ADS).getPrice();

                // update the UI
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Buy No Ads");
                builder.setMessage("Remove ads at " + applePrice);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("Buy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
                                = new IabHelper.OnIabPurchaseFinishedListener() {
                            public void onIabPurchaseFinished(IabResult result, Purchase purchase)
                            {
                                if (result.isFailure()) {
                                    return;
                                }
                                else if (purchase.getSku().equals(PurchaseUtils.PRODUCT_NO_ADS)) {
                                    PurchaseUtils.purchaseProduct(SettingsActivity.this, PurchaseUtils.PRODUCT_NO_ADS);
                                    try {
                                        final ParseUser user = ParseUser.getCurrentUser();
                                        JSONArray jsonArray;
                                        String unlockContent = user.getString("unlockContent");
                                        if (unlockContent != null) {
                                            jsonArray = new JSONArray(unlockContent);
                                        } else {
                                            jsonArray = new JSONArray();
                                        }
                                        jsonArray.put(PurchaseUtils.PRODUCT_NO_ADS);
                                        user.put("unlockContent", jsonArray.toString());
                                        user.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e != null) {
                                                    //Yey
                                                } else {
                                                    //Safe in local
                                                }
                                            }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        };
                        try {
                            mHelper.launchPurchaseFlow(SettingsActivity.this, PurchaseUtils.PRODUCT_NO_ADS, 10001, mPurchaseFinishedListener, "");
                        } catch (IabHelper.IabAsyncInProgressException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.show();
            }
        };

        try {
            mHelper.queryInventoryAsync(true, additionalSkuList, null,
                    mQueryFinishedListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    private void restorePurchases() {
        IabHelper.QueryInventoryFinishedListener mGotInventoryListener
                = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result,
                                                 Inventory inventory) {

                if (result.isFailure()) {
                    // handle error here
                }
                else {
                    // does the user have the premium upgrade?
                    //mIsPremium = inventory.hasPurchase(SKU_PREMIUM);
                    // update UI accordingly
                    if (inventory.hasPurchase(PurchaseUtils.PRODUCT_NO_ADS)) {
                        PurchaseUtils.purchaseProduct(SettingsActivity.this, PurchaseUtils.PRODUCT_NO_ADS);
                    }
                }
            }
        };
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
            ParseUser user = ParseUser.getCurrentUser();
            JSONArray jsonArray;
            String unlockContent = user.getString("unlockContent");
            if (unlockContent != null) {
                jsonArray = new JSONArray(unlockContent);
            } else {
                jsonArray = new JSONArray();
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                PurchaseUtils.purchaseProduct(this, jsonArray.getString(i));
            }
            showDialogWithText("Purchases have been restored");
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showDialogWithText(String string) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(string);
        builder1.setCancelable(true);
        builder1.create().show();
    }

    private void showDialogWithText(int stringID) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(getString(stringID));
        builder1.setCancelable(true);
        builder1.create().show();
    }

    private boolean appInstalledOrNot(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    private void goToMarket(String packageName) {

        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(SettingsActivity.this,"Please try again later", Toast.LENGTH_SHORT).show();
        }
    }

    private void runApp(String packageName) {

        Intent LaunchIntent = getPackageManager()
                .getLaunchIntentForPackage(packageName);
        startActivity(LaunchIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) try {
            mHelper.dispose();
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
        mHelper = null;
    }

}
