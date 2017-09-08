package com.yo.android.inapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.yo.android.di.Injector;
import com.yo.android.inapp.inapputil.IabHelper;
import com.yo.android.inapp.inapputil.IabResult;
import com.yo.android.inapp.inapputil.Inventory;
import com.yo.android.inapp.inapputil.Purchase;
import com.yo.android.ui.BaseActivity;
import com.yo.android.util.Constants;
import com.yo.android.vox.BalanceHelper;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ramesh on 23/7/16.
 */
public class UnManageInAppPurchaseActivity extends BaseActivity {
    // Is debug logging enabled?
    boolean mDebugLog = true;
    String mDebugTag = "YoApp";

    public String BASE_64_KEY_FOR_IN_APP_PURCHASE = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvN0nwJTxnfciBYiwTrZ2pzevIKGAB09Kulx0akR8Lzrd8bju2kCpe/PGeLQQ9BQWpvz3p81vl3N9sT7k0pfcJp10MMrtzyfYAR1t9U5R7manRzzRM2j1BNHnPneOaJ9WrQDUWpXqvDaeDgiS0rrSfWCHtTqDaoQV8RCbMLtanTJlBQgvYmObvhzLwtSSsD558UPUb7bEZtoFKgzSCqIS4pGFhFqVESxdRt95LpKbagVZSGEo4Nd2UoqDJ6gkG5cRLwHcl3ob2Nr+GRK3ybvNotCuGz3/cdVnqZjoWH73PP2qkG4iOopxhLW7ifZVtYVAW0hJFlM1Mf1TwPZ1AfICxQIDAQAB";
    //public String BASE_64_KEY_FOR_IN_APP_PURCHASE = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnDW78g7q5sjBENMt+qS4ioPqwtu1rdMMWFAhrsdUHRVXyftD2IPg50EuN/phvsANdpWbSqJHLjaw/1mNPVNcaBQ5VeS4IxvmGsQbJdLcVcKMj2gJ2eAWB31HtVCaOvv4zYNAzR250AK6ZT+GGgPjXoQiaB68DbHNiHGqska8lwswC08NRSmR5voMCpfodpt9z4hK7dSjRjiDfxH8+McbiFjCliWE0ygg4Je63aZW7erjJyw3MWc1fIOGlNwxmwNkM6uJnZTMWSCs3aTXKvujEvReea2//gjwfZgKB5sZG+AN7NmHKvy2+Jo/tB+FoBkr+tiq/845Xi1SffjOm4Cp8wIDAQAB";
    private IabHelper mHelper;
    private static String ITEM_SKU;
    private static float ITEM_PRICE;
    private static final int REQUEST_CODE = 585;
    //Can be userid
    //Todo remove emailAddress
    private String emailAddress = "rajesh.polamarasetti@quantela" +
            ".com";

    @Inject
    BalanceHelper mBalanceHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.obtain(getApplication()).inject(this);
        ITEM_SKU = getIntent().getStringExtra("sku");
        ITEM_PRICE = getIntent().getFloatExtra("price", 0f);
        ITEM_SKU = "android.test.purchased";
        ITEM_SKU = ITEM_SKU.toLowerCase();
        //developer payload
        emailAddress = getIntent().getStringExtra(Constants.USER_ID);
        mHelper = new IabHelper(this, BASE_64_KEY_FOR_IN_APP_PURCHASE);
        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

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
                    logDebug("Calling Consuming..");
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
                    }
                }
            };

    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, final Purchase info) {
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
                logDebug("mPurchaseFinishedListener: Product purchased Successfully");
//                Toast.makeText(UnManageInAppPurchaseActivity.this, "Product purchased Successfully!", Toast.LENGTH_SHORT).show();
                //Calling the consume function will "free" your item and make it "available" again. (Your user will be able to purchase it as many time as he wants)
                consumePurchase(info);
            }
        }
    };

    private void consumePurchase(final Purchase info) {
        showProgressDialog();
        mHelper.consumeAsync(info, new IabHelper.OnConsumeFinishedListener() {
            @Override
            public void onConsumeFinished(Purchase purchase, IabResult result) {
                mBalanceHelper.addBalance(String.valueOf(ITEM_PRICE), new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dismissProgressDialog();
                        Intent data = new Intent();
                        data.putExtra("sku", ITEM_SKU);
                        data.putExtra("details", info.toString());
                        if (response.isSuccessful()) {
                            setResult(RESULT_OK, data);
                            //for multiple purchase.
                            //mHelper.consumeAsync(info, mConsumeFinishedListener);
                        } else {
                            setResult(RESULT_CANCELED, data);
                        }
                        finish();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        dismissProgressDialog();
                        Intent data = new Intent();
                        data.putExtra("sku", ITEM_SKU);
                        data.putExtra("details", info.toString());
                        setResult(RESULT_CANCELED, data);
                        finish();
                    }
                });
            }
        });
    }

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
        //return !TextUtils.isEmpty(purchase.getDeveloperPayload()) && purchase.getDeveloperPayload().equalsIgnoreCase(emailAddress);
        return !TextUtils.isEmpty(purchase.getDeveloperPayload());
    }

}
