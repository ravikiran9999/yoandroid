package com.yo.android;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;

import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipManager;
import com.csipsimple.api.SipProfile;
import com.csipsimple.utils.AccountListUtils;
import com.csipsimple.utils.Log;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private String THIS_FILE = "MainActivity";

    private AccountStatusContentObserver statusObserver;
    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        findViewById(R.id.btnRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        findViewById(R.id.btnCall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeCall();
            }
        });
        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        // Optional, but here we bundle so just ensure we are using csipsimple package
        serviceIntent.setPackage(getPackageName());
        bindService(serviceIntent, connection,
                Context.BIND_AUTO_CREATE);

        //
        if (statusObserver == null) {
            statusObserver = new AccountStatusContentObserver(mHandler);
            getContentResolver().registerContentObserver(SipProfile.ACCOUNT_STATUS_URI,
                    true, statusObserver);
        }
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindService(connection);
        } catch (Exception e) {
            // Just ignore that
            Log.w("Ramesh", "Unable to un bind", e);
        }
        //
        if (statusObserver != null) {
            getContentResolver().unregisterContentObserver(statusObserver);
            statusObserver = null;
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, SipProfile.ACCOUNT_URI, new String[]{
                SipProfile.FIELD_ID + " AS " + BaseColumns._ID,
                SipProfile.FIELD_ID,
                SipProfile.FIELD_DISPLAY_NAME,
                SipProfile.FIELD_WIZARD,
                SipProfile.FIELD_ACTIVE
        }, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //updateCheckedItem();
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    do {
                        final SipProfile acc = new SipProfile(cursor);
                        Log.e(THIS_FILE, "Profile Name: " + acc.getSipUserName());
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                Log.e(THIS_FILE, "Error on looping over sip profiles", e);
            } finally {
                cursor.close();
            }
        }
    }

//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        super.onLoadFinished(loader, data);
//        // Select correct item if any
//        updateCheckedItem();
//    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private ISipService service;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ISipService.Stub.asInterface(arg1);
            /*
             * timings.addSplit("Service connected"); if(configurationService !=
             * null) { timings.dumpToLog(); }
             */
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
        }
    };

    private void makeCall() {
        if (service != null) {
            // It is a SIP account, try to call service for that
            try {
                service.makeCallWithOptions("8341569102", 1, null);
            } catch (RemoteException e) {
                //Log.e(THIS_FILE, "Service can't be called to make the call");
            }

        }
    }

    private void register() {
        ContentResolver cr = getContentResolver();
        // Clear old existing accounts
        cr.delete(SipProfile.ACCOUNT_URI, "1", null);
        cr.delete(SipManager.FILTER_URI, "1", null);

        YoSipUtil yoSipUtil = new YoSipUtil();
        yoSipUtil.saveAccount(this, "BASIC");
//        AccountListUtils.AccountStatusDisplay activeAccount = yoSipUtil.getActiveAccount(this);
        Log.e("register", "Service can't be called to make the call");
    }

    /**
     * Observer for changes of account registration status
     */
    class AccountStatusContentObserver extends ContentObserver {
        public AccountStatusContentObserver(Handler h) {
            super(h);
        }

        public void onChange(boolean selfChange) {
            Log.d(THIS_FILE, "Accounts status.onChange( " + selfChange + ")");
            updateRegistration(MainActivity.this);
        }
    }

    private static final String[] ACC_PROJECTION = new String[]{
            SipProfile.FIELD_ID,
            SipProfile.FIELD_ACC_ID, // Needed for default domain
            SipProfile.FIELD_REG_URI, // Needed for default domain
            SipProfile.FIELD_PROXY, // Needed for default domain
            SipProfile.FIELD_DEFAULT_URI_SCHEME, // Needed for default scheme
            SipProfile.FIELD_DISPLAY_NAME,
            SipProfile.FIELD_WIZARD
    };

    /**
     * Update user interface when registration of account has changed
     * This include change selected account if we are in canChangeIfValid mode
     */
    private void updateRegistration(Context context) {
        Cursor c = getContentResolver().query(SipProfile.ACCOUNT_URI, ACC_PROJECTION, SipProfile.FIELD_ACTIVE + "=?", new String[]{
                "1"
        }, null);

        SipProfile toSelectAcc = null;
        SipProfile firstAvail = null;

        if (c != null) {
            try {
                if (c.getCount() > 0 && c.moveToFirst()) {
                    do {
                        final SipProfile acc = new SipProfile(c);

                        AccountListUtils.AccountStatusDisplay accountStatusDisplay = AccountListUtils
                                .getAccountDisplay(context, acc.id);
                        if (accountStatusDisplay.availableForCalls) {
                            if (firstAvail == null) {
                                firstAvail = acc;
                            }
                        }
                    } while (c.moveToNext());
                }
            } catch (Exception e) {
                Log.e(THIS_FILE, "Error on looping over sip profiles", e);
            } finally {
                c.close();
            }
        }

        if (toSelectAcc == null) {
            // Nothing to force select, fallback to first avail
            toSelectAcc = firstAvail;
        }
    }

}
