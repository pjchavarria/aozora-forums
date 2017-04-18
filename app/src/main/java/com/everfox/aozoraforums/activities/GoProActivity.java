package com.everfox.aozoraforums.activities;

import android.content.Intent;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.fragments.ProFragment;
import com.everfox.aozoraforums.util.IabHelper;
import com.everfox.aozoraforums.util.IabResult;
import com.everfox.aozoraforums.util.Inventory;
import com.everfox.aozoraforums.util.Purchase;
import com.everfox.aozoraforums.utils.PurchaseUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GoProActivity extends AppCompatActivity {

    IabHelper mHelper;

    @BindView(R.id.tv_pro_price)
    TextView tvProPrice;
    @BindView(R.id.tv_pro_plus_price)
    TextView tvProPlusPrice;
    @BindView(R.id.gopro_view)
    View goProView;
    @BindView(R.id.gopro_plus_view)
    View goProPlusView;
    @BindView(R.id.vpPro)
    ViewPager vpPro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_pro);

        mHelper = new IabHelper(this, getString(R.string.bepk));
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    return;
                }
                GetPrices();
            }
        });

        ButterKnife.bind(this);

        ProductPurchasePagerAdapter mSectionsPagerAdapter = new ProductPurchasePagerAdapter(getSupportFragmentManager());
        vpPro.setAdapter(mSectionsPagerAdapter);

        goProView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoPro();
            }
        });

        goProPlusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoProPlus();
            }
        });

    }

    public void GetPrices() {
        List additionalSkuList = new ArrayList();
        additionalSkuList.add(PurchaseUtils.PRODUCT_PRO);
        additionalSkuList.add(PurchaseUtils.PRODUCT_PRO_PLUS);

        IabHelper.QueryInventoryFinishedListener
                mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory)
            {
                if (result.isFailure()) {
                    return;
                }
                //ForTesting
                /*try {
                    mHelper.consumeAsync(inventory.getPurchase(PurchaseUtils.PRODUCT_PRO), null);
                    //mHelper.consumeAsync(inventory.getPurchase(PurchaseUtils.PRODUCT_PRO_PLUS), null);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }*/
                //ForTesting
                String proPrice = inventory.getSkuDetails(PurchaseUtils.PRODUCT_PRO).getPrice();
                String proPlusPrice = inventory.getSkuDetails(PurchaseUtils.PRODUCT_PRO_PLUS).getPrice();
                // update the UI
                tvProPlusPrice.setText(proPlusPrice);
                tvProPrice.setText(proPrice);
            }
        };

        try {
            mHelper.queryInventoryAsync(true, additionalSkuList, null,
                    mQueryFinishedListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }

    }

    public void GoPro() {
        IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
                = new IabHelper.OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result, Purchase purchase)
            {
                if (result.isFailure()) {
                    return;
                }
                else if (purchase.getSku().equals(PurchaseUtils.PRODUCT_PRO)) {
                    PurchaseUtils.purchaseProduct(GoProActivity.this, PurchaseUtils.PRODUCT_PRO);
                    try {
                        final ParseUser user = ParseUser.getCurrentUser();
                        JSONArray jsonArray;
                        String unlockContent = user.getString("unlockContent");
                        if (unlockContent != null && !unlockContent.equals("")) {
                            jsonArray = new JSONArray(unlockContent);
                        } else {
                            jsonArray = new JSONArray();
                        }
                        jsonArray.put(PurchaseUtils.PRODUCT_PRO);
                        JSONArray jsonBadges = user.getJSONArray("badges");
                        if (jsonBadges == null) {
                            jsonBadges = new JSONArray();
                        }
                        jsonBadges.put("PRO");

                        user.put("unlockContent", jsonArray.toString());
                        user.put("badges", jsonBadges);

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
                        Intent i = new Intent(GoProActivity.this, MainActivity.class);
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
            mHelper.launchPurchaseFlow(GoProActivity.this, PurchaseUtils.PRODUCT_PRO, 10001, mPurchaseFinishedListener, "");
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    public void GoProPlus() {
        IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
                = new IabHelper.OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result, Purchase purchase)
            {
                if (result.isFailure()) {
                    return;
                }
                else if (purchase.getSku().equals(PurchaseUtils.PRODUCT_PRO_PLUS)) {
                    PurchaseUtils.purchaseProduct(GoProActivity.this, PurchaseUtils.PRODUCT_PRO_PLUS);
                    try {
                        final ParseUser user = ParseUser.getCurrentUser();
                        JSONArray jsonArray;
                        String unlockContent = user.getString("unlockContent");
                        if (unlockContent != null && !unlockContent.equals("")) {
                            jsonArray = new JSONArray(unlockContent);
                        } else {
                            jsonArray = new JSONArray();
                        }
                        jsonArray.put(PurchaseUtils.PRODUCT_PRO_PLUS);
                        JSONArray jsonBadges = user.getJSONArray("badges");
                        if (jsonBadges == null) {
                            jsonBadges = new JSONArray();
                        }
                        jsonBadges.put("PRO+");
                        user.put("unlockContent", jsonArray.toString());
                        user.put("badges", jsonBadges);
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
                        Intent i = new Intent(GoProActivity.this, MainActivity.class);
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
            mHelper.launchPurchaseFlow(GoProActivity.this, PurchaseUtils.PRODUCT_PRO_PLUS, 10001, mPurchaseFinishedListener, "");
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    public class ProductPurchasePagerAdapter extends FragmentPagerAdapter {

        public ProductPurchasePagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return ProFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 2;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("GoPro", "onActivityResult(" + requestCode + "," + resultCode + "," + data);
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
