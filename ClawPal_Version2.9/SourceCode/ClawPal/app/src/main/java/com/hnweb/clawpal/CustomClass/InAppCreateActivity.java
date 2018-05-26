package com.hnweb.clawpal.CustomClass;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.hnweb.clawpal.login.LoginActivity;
import com.hnweb.clawpal.R;
import com.hnweb.clawpal.WebUrl.WebURL;
import com.hnweb.clawpal.util.AppConstant;
import com.hnweb.clawpal.util.IabBroadcastReceiver;
import com.hnweb.clawpal.util.IabHelper;
import com.hnweb.clawpal.util.IabResult;
import com.hnweb.clawpal.util.Inventory;
import com.hnweb.clawpal.util.Purchase;
import com.hnweb.clawpal.util.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shree on 28-Nov-17.
 */

public class InAppCreateActivity extends AppCompatActivity   implements IabBroadcastReceiver.IabBroadcastListener{
    static final String SKU_PLAN1 = "plan1";
    static final String SKU_PLAN2 = "plan2";
    static final int RC_REQUEST = 10001;
    Context context;
    SharedPreferences sharedPreferences;
    String from;
    String user_Id;
    String isPaymentDone;
    boolean mIsPremium = false;
    boolean mAutoRenewEnabled = false;
    // Tracks the currently owned infinite gas SKU, and the options in the Manage dialog
    String mInfiniteGasSku = "";
    String mFirstChoiceSku = "";
    String mSecondChoiceSku = "";
    // Used to select between purchasing gas on a monthly or yearly basis
    String mSelectedSubscriptionPeriod = "";
    // Current amount of gas in tank, in units
    int mTank;
    // The helper object
    IabHelper mHelper;
    String paymentId;
    // Debug tag, for logging
    //  static final String TAG = "TrivialDrive";
    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver;

    // Does the user have an active subscription to the infinite gas plan?
//    boolean mSubscribedToInfiniteGas = false;
    private int chooserType;
    private DatePickerDialog.OnDateSetListener datePickerListener;
    private int day;
    private String filePath;
    private int hour;
    private ArrayList<String> mSelectPath;
    private int minute;

    // SKU for our subscription (infinite gas)
    //  static final String SKU_INFINITE_GAS_MONTHLY = "infinite_gas_monthly";
    //   static final String SKU_INFINITE_GAS_YEARLY = "infinite_gas_yearly";
    private int month;

    // Graphics for the gas gauge
    private TimePickerDialog.OnTimeSetListener timePickerListener;
    private int year;
    private ArrayList<String> mSelecthandPath;
    private int mYear, mMonth, mDay, mHour, mMinute;


    ////punnyfuzzle
    private String TAG = LoginActivity.class.getSimpleName();
    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
//                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(SKU_PLAN1);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

          /*  // First find out which subscription is auto renewing
            Purchase gasMonthly = inventory.getPurchase(SKU_INFINITE_GAS_MONTHLY);
            Purchase gasYearly = inventory.getPurchase(SKU_INFINITE_GAS_YEARLY);
            if (gasMonthly != null && gasMonthly.isAutoRenewing()) {
                mInfiniteGasSku = SKU_INFINITE_GAS_MONTHLY;
                mAutoRenewEnabled = true;
            } else if (gasYearly != null && gasYearly.isAutoRenewing()) {
                mInfiniteGasSku = SKU_INFINITE_GAS_YEARLY;
                mAutoRenewEnabled = true;
            } else {
                mInfiniteGasSku = "";
                mAutoRenewEnabled = false;
            }
*/
            // The user is subscribed if either subscription exists, even if neither is auto
            // renewing
         /*   mSubscribedToInfiniteGas = (gasMonthly != null && verifyDeveloperPayload(gasMonthly))
                    || (gasYearly != null && verifyDeveloperPayload(gasYearly));
            Log.d(TAG, "User " + (mSubscribedToInfiniteGas ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");
            if (mSubscribedToInfiniteGas) mTank = TANK_MAX;
*/
            // Check for gas delivery -- if we own gas, we should fill up the tank immediately
            Purchase gasPurchase = inventory.getPurchase(SKU_PLAN2);
            Purchase prePurchase = inventory.getPurchase(SKU_PLAN1);
            if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
                Log.d(TAG, "We have gas. Consuming it.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_PLAN2), new IabHelper.OnConsumeFinishedListener() {
                        @Override
                        public void onConsumeFinished(Purchase purchase, IabResult result) {
                            Log.d("InApp Purchase", "onConsumeFinished= " + result.getMessage());
                        }
                    });
                } catch (IabHelper.IabAsyncInProgressException e) {
//                    complain("Error consuming gas. Another async operation in progress.");
                    Log.d("InApp Purchase", "IabAsyncInProgressException");
                }
                return;
            }
            if (prePurchase != null && verifyDeveloperPayload(prePurchase)) {
                Log.d(TAG, "We have gas. Consuming it.");
                try {
                    //     Purchase purchase = new Purchase("inapp", THAT_JSON_STRING, "");
                    mHelper.consumeAsync(inventory.getPurchase(SKU_PLAN1), new IabHelper.OnConsumeFinishedListener() {
                        @Override
                        public void onConsumeFinished(Purchase purchase, IabResult result) {
                            Log.d("InApp Purchase", "onConsumeFinished= " + result.getMessage());
                        }
                    });
                } catch (IabHelper.IabAsyncInProgressException e) {
//                    complain("Error consuming gas. Another async operation in progress.");
                    Log.d("InApp Purchase", "IabAsyncInProgressException");
                }
                return;
            }
            //   updateUi();
            //   setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };
    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
              /*  Log.d(TAG, "Consumption successful. Provisioning.");
                mTank = mTank == TANK_MAX ? TANK_MAX : mTank + 1;
                saveData();
                alert("You filled 1/4 tank. Your tank is now " + String.valueOf(mTank) + "/4 full!");*/

                Utilities.showAlertDailog(InAppCreateActivity.this, "PunnyFuzzles", "PayPal Payment Successfully Done.", "Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        }, false);
            } else {
//                complain("Error while consuming: " + result);
            }
            // updateUi();
            // setWaitScreen(false);
            Log.d(TAG, "End consumption flow.");
        }
    };
    private Activity activity;
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
//                complain("Error purchasing: " + result);
                //   setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
//                complain("Error purchasing. Authenticity verification failed.");
                //  setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_PLAN2)) {

                ///punny fuzzle
//.makeText(getApplicationContext(),"40 puzzles done",Toast.LENGTH_LONG).show();

                Utilities.showAlertDailog(activity, "PunnyFuzzles", "Payment Successfully Done.", "Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        }, false);

                ///
                // bought 1/4 tank of gas. So consume it.
               /* Log.d(TAG, "Purchase is gas. Starting gas consumption.");
                try {
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                } catch (IabAsyncInProgressException e) {
                    complain("Error consuming gas. Another async operation in progress.");
                 //   setWaitScreen(false);
                    return;
                }*/
            } else if (purchase.getSku().equals(SKU_PLAN1)) {
                // bought the premium upgrade!
            /*    Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                alert("Thank you for upgrading to premium!");
                mIsPremium = true;*/


                Utilities.showAlertDailog(InAppCreateActivity.this, "PunnyFuzzles", "PayPal Payment Successfully Done.", "Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        }, false);
                //  updateUi();
                //  setWaitScreen(false);
            }
           /* else if (purchase.getSku().equals(SKU_INFINITE_GAS_MONTHLY)
                    || purchase.getSku().equals(SKU_INFINITE_GAS_YEARLY)) {
                // bought the infinite gas subscription
                Log.d(TAG, "Infinite gas subscription purchased.");
                alert("Thank you for subscribing to infinite gas!");
                mSubscribedToInfiniteGas = true;
                mAutoRenewEnabled = purchase.isAutoRenewing();
                mInfiniteGasSku = purchase.getSku();
                mTank = TANK_MAX;
              //  updateUi();
               // setWaitScreen(false);
            }*/
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public void inAPpOnCreate() {

        loadData();

        /* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
         * (that you got from the Google Play developer console). This is not your
         * developer public key, it's the *app-specific* public key.
         *
         * Instead of just storing the entire literal string here embedded in the
         * program,  construct the key at runtime from pieces or
         * use bit manipulation (for example, XOR with some other string) to hide
         * the actual key.  The key itself is not secret information, but we don't
         * want to make it easy for an attacker to replace the public key with one
         * of their own and then fake messages from the server.
         */
        String base64EncodedPublicKey = AppConstant.Base64EncodedPublicKey;

        // Some sanity checks to see if the developer (that's you!) really followed the
        // instructions to run this sample (don't put these checks on your app!)
        if (base64EncodedPublicKey.contains("CONSTRUCT_YOUR")) {
            throw new RuntimeException("Please put your app's public key in MainActivity.java. See README.");
        }
        if (getPackageName().startsWith("com.example")) {
            throw new RuntimeException("Please change the sample's package name! See README.");
        }

        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                try {


                    try {
                        String payload = "";
                        mHelper.launchPurchaseFlow(InAppCreateActivity.this, SKU_PLAN1, RC_REQUEST,
                                mPurchaseFinishedListener, payload);
                    } catch (IabHelper.IabAsyncInProgressException e) {
//                        complain("Service Not Available , Please Try Again Later..");
                        //setWaitScreen(false);
                    }

                } catch (Exception ex) {
                    Log.e(AppConstant.TAG, "Error(btn100_onClick):" + ex.toString());
                }

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
//                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(InAppCreateActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
//                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });
        consumeProducts();
    }
    private void consumeProducts() {
    }

    void loadData() {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        mTank = sp.getInt("tank", 2);
        Log.d(TAG, "Loaded data: tank = " + String.valueOf(mTank));
    }

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
//            complain("Error querying inventory. Another async operation in progress.");
        }
    }


    private void addPaymentInfo(final String paymentId) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebURL.UPDATE_PAYMENT_URL,
                new Response.Listener<String>() {

                    public void onResponse(String response) {
                        System.out.println("res= " + response);
                        //    System.out.println("reg"+GET_PLANS__URL.toString()+response.toString());
                        try {
                            JSONObject j = new JSONObject(response);
//                            flag=j.getInt("message_code");
//                            System.out.println("arshflag"+ flag);
//                            if(flag)
                            //    int flag=j.getInt("message_code");
                            // System.out.println("flag"+ response.toString());
                            System.out.println("resArsh" + response.toString() + response.toString());
                            String res = j.getString("message");
                            //      System.out.println("res" + res);

                            // Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
                            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(InAppCreateActivity.this, R.style.MyAlertDialogStyle);

                            //  android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity_save_data.this);
                            builder.setMessage(res)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent intent;


                                            try {


                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    });
                            android.support.v7.app.AlertDialog alert = builder.create();
                            alert.show();

                        } catch (JSONException e) {
                            System.out.println("jsonexeption1111" + e.toString());
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                        System.out.println("jsonexeption" + error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                try {

                    params.put(AppConstant.USER_ID, user_Id);
                    params.put(AppConstant.KEY_PAYMENT_ID, paymentId);

                } catch (Exception e) {
                    System.out.println("arsherror" + e.toString());
                }


                System.out.println("myparams" + params);
                return params;
            }

        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println("rescode" + resultCode);
        if (requestCode == RC_REQUEST) {

            if (resultCode == RESULT_OK) {
                try {
                    int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
                    String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
                    String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
                    Log.d("InAPPPrchase", "dataSignature= " + dataSignature);
                    Log.d("InAPPPrchase", "purchaseData" + purchaseData);
                    Log.d("InAPPPrchase", "responseCode" + String.valueOf(responseCode));

                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    paymentId = jo.getString("orderId");

                    //      Toast.makeText(getApplicationContext(),"sku"+sku,Toast.LENGTH_LONG).show();
                    System.out.println("op" + sku);

                    if (sku.equals("plan1")) {
                        //   Toast.makeText(getApplicationContext(),"40 Puzzles called",Toast.LENGTH_LONG).show();
                        addPaymentInfo(paymentId);

                    }

                } catch (JSONException e) {
                    alert("Failed to parse purchase data.");
                    e.printStackTrace();
                }
            }
        }

    }

    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        if (mBroadcastReceiver != null) {
            try {
                unregisterReceiver(mBroadcastReceiver);
            } catch (Exception e) {

            }
        }

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    void alert(String message) {
        android.support.v7.app.AlertDialog.Builder bld = new android.support.v7.app.AlertDialog.Builder(this, R.style.MyAlertDialogStyle);

        bld.setMessage(message);
        bld.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                        finish();
                    }
                });
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }

}
