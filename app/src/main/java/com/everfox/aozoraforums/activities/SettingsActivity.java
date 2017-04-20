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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.util.IabHelper;
import com.everfox.aozoraforums.util.IabResult;
import com.everfox.aozoraforums.util.Inventory;
import com.everfox.aozoraforums.util.Purchase;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PurchaseUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQuery;
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
    @BindView(R.id.tvGoPro)
    TextView tvGoPro;


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

        AdView mAdView = (AdView) findViewById(R.id.adView);
        if (!PurchaseUtils.purchasedProduct(this, PurchaseUtils.PRODUCT_NO_ADS)) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
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
                goToMarket(AOTRACKING_PACKAGE);
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

        tvGoPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, GoProActivity.class);
                startActivity(intent);
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

    EditText editText;
    private void changeUsernameClicked() {


        AlertDialog.Builder change = new AlertDialog.Builder(SettingsActivity.this);
        editText = new EditText(SettingsActivity.this);
        change.setTitle("Change Username");
        change.setMessage("Enter your new username");
        change.setView(editText);

        change.setPositiveButton("Update Username", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            CheckUserNameAvailability(editText.getText().toString());
            }
        });
        change.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        change.show();
    }

    private boolean CheckUserNameAvailability(final String newUsername) {

        final List additionalSkuList = new ArrayList();
        additionalSkuList.add(PurchaseUtils.PRODUCT_CHANGE_USERNAME);

        final EditText finalEditText = editText;
        final IabHelper.QueryInventoryFinishedListener
                mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, final Inventory inventory)
            {
                if (result.isFailure()) {
                    // handle error
                    return;
                }
                /*try {
                    mHelper.consumeAsync(inventory.getPurchase(PurchaseUtils.PRODUCT_CHANGE_USERNAME), null);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }*/
                String applePrice =
                        inventory.getSkuDetails(PurchaseUtils.PRODUCT_CHANGE_USERNAME).getPrice();

                // update the UI
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                //builder.setTitle("Buy No Ads");
                builder.setMessage("Change Username at " + applePrice);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
                                = new IabHelper.OnIabPurchaseFinishedListener() {
                            public void onIabPurchaseFinished(IabResult result, Purchase purchase)
                            {
                                if (result.isFailure()) {
                                    return;
                                }
                                else if (purchase.getSku().equals(PurchaseUtils.PRODUCT_CHANGE_USERNAME)) {
                                    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
                                            new IabHelper.OnConsumeFinishedListener() {
                                                public void onConsumeFinished(Purchase purchase, IabResult result) {
                                                    if (result.isSuccess()) {
                                                        // provision the in-app purchase to the user
                                                        // (for example, credit 50 gold coins to player's character)
                                                        ParseUser user = ParseUser.getCurrentUser();
                                                        user.put("aozoraUsername", finalEditText.getText().toString());
                                                        if (!ParseFacebookUtils.isLinked(user)) {
                                                            user.put("username", finalEditText.getText().toString().toLowerCase());
                                                        }
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
                                                    }
                                                    else {
                                                        // handle error
                                                    }
                                                }
                                            };
                                    try {
                                        mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                                    } catch (IabHelper.IabAsyncInProgressException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        };
                        try {
                            mHelper.launchPurchaseFlow(SettingsActivity.this, PurchaseUtils.PRODUCT_CHANGE_USERNAME, 10001, mPurchaseFinishedListener, "");
                        } catch (IabHelper.IabAsyncInProgressException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.show();
            }
        };

        ParseQuery<PUser> query = ParseQuery.getQuery("_User");
        Log.d("Settigs", "Change " + newUsername);
        query.whereEqualTo("username", newUsername.toLowerCase());
        query.findInBackground(new FindCallback<PUser>() {
            @Override
            public void done(List<PUser> objects, ParseException e) {
                if (e != null) {
                    Log.d("Exception", e.getMessage());
                }
                if (objects != null && objects.size() != 0) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this).setMessage("This username is not available.");
                    alert.create().show();
                } else {
                    ParseQuery<PUser> query2 = ParseQuery.getQuery("_User");
                    query2.whereMatches("aozoraUsername", "^\\(" + newUsername + ")$");
                    query2.findInBackground(new FindCallback<PUser>() {
                        @Override
                        public void done(List<PUser> objects, ParseException e) {
                            if (objects != null && objects.size() != 0) {
                                AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this).setMessage("This username is not available.");
                                alert.create().show();
                            } else {
                                try {
                                    mHelper.queryInventoryAsync(true, additionalSkuList, null,
                                            mQueryFinishedListener);
                                } catch (IabHelper.IabAsyncInProgressException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });
                }

            }
        });
        return true;
    }

    private void removeAdsClicked() {

        if(!PurchaseUtils.purchasedProduct(this,PurchaseUtils.PRODUCT_NO_ADS)) {

            List additionalSkuList = new ArrayList();
            additionalSkuList.add(PurchaseUtils.PRODUCT_NO_ADS);

            IabHelper.QueryInventoryFinishedListener
                    mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
                public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
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
                                public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                                    if (result.isFailure()) {
                                        return;
                                    } else if (purchase.getSku().equals(PurchaseUtils.PRODUCT_NO_ADS)) {
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
                                            Intent i = new Intent(SettingsActivity.this, MainActivity.class);
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(i);
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
        } else {

            showDialogWithText("You have already purchased the product. If there is any problem try Restore Purchases");
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
                    if (inventory.hasPurchase(PurchaseUtils.PRODUCT_NO_ADS))
                        PurchaseUtils.purchaseProduct(SettingsActivity.this, PurchaseUtils.PRODUCT_NO_ADS);
                    if (inventory.hasPurchase(PurchaseUtils.PRODUCT_PRO))
                        PurchaseUtils.purchaseProduct(SettingsActivity.this, PurchaseUtils.PRODUCT_PRO);
                    if (inventory.hasPurchase(PurchaseUtils.PRODUCT_PRO_PLUS))
                        PurchaseUtils.purchaseProduct(SettingsActivity.this, PurchaseUtils.PRODUCT_PRO_PLUS);

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

            if(PurchaseUtils.purchasedProduct(this,PurchaseUtils.PRODUCT_NO_ADS))
                if(!jsonArray.toString().contains(PurchaseUtils.PRODUCT_NO_ADS))
                    jsonArray.put(PurchaseUtils.PRODUCT_NO_ADS);
            if(PurchaseUtils.purchasedProduct(this,PurchaseUtils.PRODUCT_PRO))
                if(!jsonArray.toString().contains(PurchaseUtils.PRODUCT_PRO)) {
                    jsonArray.put(PurchaseUtils.PRODUCT_PRO);
                    //Badge
                    JSONArray jsonBadges = user.getJSONArray("badges");
                    if (jsonBadges == null) {
                        jsonBadges = new JSONArray();
                    }
                    if(!jsonBadges.toString().contains("\"PRO\"")) {
                        jsonBadges.put("PRO");
                    }
                    user.put("badges",jsonBadges);
                }
            if(PurchaseUtils.purchasedProduct(this,PurchaseUtils.PRODUCT_PRO_PLUS))
                if(!jsonArray.toString().contains(PurchaseUtils.PRODUCT_PRO_PLUS)) {
                    jsonArray.put(PurchaseUtils.PRODUCT_PRO_PLUS);
                    //Badge
                    JSONArray jsonBadges = user.getJSONArray("badges");
                    if (jsonBadges == null) {
                        jsonBadges = new JSONArray();
                    }
                    if(!jsonBadges.toString().contains("\"PRO+\"")) {
                        jsonBadges.put("PRO+");
                    }
                    user.put("badges",jsonBadges);

                }
            user.put("unlockContent",jsonArray.toString());
            user.saveInBackground();

            showDialogWithTextAndRestartApp("Purchases have been restored, you might have to restart the app to see changes");
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


    private void showDialogWithTextAndRestartApp(String string) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(string);
        builder1.setCancelable(true);
        builder1.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d("GoPro", "onActivityResult handled by IABUtil.");
        }
    }

}
