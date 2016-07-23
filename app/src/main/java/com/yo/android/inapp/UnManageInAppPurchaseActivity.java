package com.yo.android.inapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.yo.android.inapp.inapputil.IabHelper;
import com.yo.android.inapp.inapputil.IabResult;
import com.yo.android.inapp.inapputil.Inventory;
import com.yo.android.inapp.inapputil.Purchase;

/**
 * Created by Ramesh on 23/7/16.
 */
public class UnManageInAppPurchaseActivity extends Activity {
    // Is debug logging enabled?
    boolean mDebugLog = true;
    String mDebugTag = "YoApp";

    public String BASE_64_KEY_FOR_IN_APP_PURCHASE = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgLnkldaAzY3MceHToADNqqCQ1BHJj2wjqtWfq6YAOof7w2MtNUvznvW+/q+iQ387JUxbinZZ7ELXsO0xBD4e5eckGGrwZC8FIMOHqAS0zzaxm27AWvWdX2atKhaAsyyf/IpI8KNmgEBfKdhGZ5mIVe/40JNGmHhKq2lrN2Wv8pL7viGfy8/cbPLWYTEOK+K4wUtmtCrKJz3J+p3zn+cyLvyHMBjF8v77XSctnSNDOK3Lnrb7wKn9NWR/GnnTOIZs4ts/UCp22F/xNFsVEP9iRDe2QkfcGbsH8rj0iZbgHt8vGCsk0NZr1Nxu7uQrWjAAfNfeHJIX65EYtlyHOWsrwwIDAQAB";
    private IabHelper mHelper;
    public static String ITEM_SKU = "com.yo.test.hundred";
    //        public static String ITEM_SKU = "android.test.purchased";
    private static final int REQUEST_CODE = 585;
    //Can be userid
    private String emailAddress = "ramesh.akula@mtuity.com";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        BASE_64_KEY_FOR_IN_APP_PURCHASE = getResources().getString(R.string.BASE_64_KEY_FOR_IN_APP_PURCHASE);
//        ITEM_SKU = getIntent().getStringExtra("sku");
        //developer payload
//        emailAddress ="user_id"
        mHelper = new IabHelper(this, BASE_64_KEY_FOR_IN_APP_PURCHASE);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {

                if (mHelper == null) {
                    return;
                }

                if (!result.isSuccess()) {
                    logError("onStart:In App Purchase Setup Failed: " + result.getMessage());
                } else {
                    logError("onStart: queryInventoryAsync called");
                    mHelper.queryInventoryAsync(mReceivedInventoryListener);
                }
            }
        });
    }

    private IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (mHelper == null) {
                return;
            }
            logError("mReceivedInventoryListener: onQueryInventoryFinished called");
            logError("mReceivedInventoryListener: onQueryInventoryFinished called; result- " + result.isFailure() + "; inventory - " + inventory);
            if (result.isFailure() || inventory == null || inventory.getPurchase(ITEM_SKU) == null) {
                logError("mReceivedInventoryListener: launchPurchaseFlow called");
                mHelper.launchPurchaseFlow(UnManageInAppPurchaseActivity.this, ITEM_SKU, REQUEST_CODE, mPurchaseFinishedListener, emailAddress);
            } else {
                logError("mReceivedInventoryListener: Calling Consuming");
                final Purchase purchase = inventory.getPurchase(ITEM_SKU);
                if (verifyPayLoad(purchase) && false) {
                    logError("mReceivedInventoryListener: verifyPayLoad true");
                } else {
                    Toast.makeText(UnManageInAppPurchaseActivity.this, "Calling Consuming..", Toast.LENGTH_SHORT).show();
                    //Calling the consume function will "free" your item and make it "available" again. (Your user will be able to purchase it as many time as he wants)
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                }
            }
        }
    };
    private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (mHelper == null) {
                        return;
                    }
                    logError("mConsumeFinishedListener: onConsumeFinished called");
                    if (result.isSuccess()) {
                        logError("mConsumeFinishedListener: launchPurchaseFlow called");
                        mHelper.launchPurchaseFlow(UnManageInAppPurchaseActivity.this, ITEM_SKU, REQUEST_CODE,
                                mPurchaseFinishedListener, emailAddress);
                    } else {
                        logError("mConsumeFinishedListener: Failed to get consume your product");
                        Toast.makeText(UnManageInAppPurchaseActivity.this, "Failed to get consume your product! Please try again later", Toast.LENGTH_SHORT).show();
                        //showMessage(result.getMessage(), "Failed to get consume your product! Please try again later");
                    }
                }
            };

    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, final Purchase info) {
            Toast.makeText(UnManageInAppPurchaseActivity.this, "Purchase finished called!", Toast.LENGTH_SHORT).show();
            logError("mPurchaseFinishedListener: Purchase finished called");
            if (mHelper == null) {
                return;
            }

            if (result.isFailure() || info == null || info.getSku() == null || !info.getSku().equals(ITEM_SKU)) {
                logError("mPurchaseFinishedListener: Transaction Failed! reason - " + result.getMessage());
                finish();
                return;
            } else {
                //
                logError("mPurchaseFinishedListener: Product purchased Successfully");
                Toast.makeText(UnManageInAppPurchaseActivity.this, "Product purchased Successfully!", Toast.LENGTH_SHORT).show();
                //Calling the consume function will "free" your item and make it "available" again. (Your user will be able to purchase it as many time as he wants)
                mHelper.consumeAsync(info, new IabHelper.OnConsumeFinishedListener() {
                    @Override
                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                        Toast.makeText(UnManageInAppPurchaseActivity.this, "Consume fininshed after purchase.", Toast.LENGTH_SHORT).show();
                    }
                });


                CommonUtils.showAlert(UnManageInAppPurchaseActivity.this, "Yo - Product purchased Successfully!", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent data = new Intent();
                        data.putExtra("sku", ITEM_SKU);
                        data.putExtra("details", info.toString());
                        setResult(RESULT_OK, data);
                        finish();
                    }
                });
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        logError("onActivityResult: called");
        if (mHelper != null && !mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }


    void logDebug(String msg) {
        if (mDebugLog) Log.d(mDebugTag, msg);
    }

    void logError(String msg) {
        Log.e(mDebugTag, "In-app billing error: " + msg);
    }

    void logWarn(String msg) {
        Log.w(mDebugTag, "In-app billing warning: " + msg);
    }

    private boolean verifyPayLoad(Purchase purchase) {
        return !TextUtils.isEmpty(purchase.getDeveloperPayload()) && purchase.getDeveloperPayload().equalsIgnoreCase(emailAddress);
    }

}
