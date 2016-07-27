package com.yo.android;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.csipsimple.api.SipProfile;
import com.csipsimple.api.SipUri;
import com.csipsimple.utils.AccountListUtils;

import java.util.UUID;

/**
 * Created by Ramesh on 27/7/16.
 */
public class YoSipUtil {
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
     * Save the account with given wizard id
     *
     * @param wizardId the wizard to use for account entry
     */
    public void saveAccount(Context context, String wizardId) {
        boolean needRestart = false;

        SipProfile account = buildAccount(new SipProfile());
        account.wizard = wizardId;
        if (account.id == SipProfile.INVALID_ID) {
            // This account does not exists yet
            applyNewAccountDefault(account);
            Uri uri = context.getContentResolver().insert(SipProfile.ACCOUNT_URI, account.getDbContentValues());

            // After insert, add filters for this wizard
            account.id = ContentUris.parseId(uri);

        } else {
            context.getContentResolver().update(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, account.id), account.getDbContentValues(), null, null);
        }
    }

    public SipProfile buildAccount(SipProfile account) {
//        Log.d(THIS_FILE, "begin of save ....");
        account.display_name = "Ramesh";
        String userName = "789";
        String domain = "209.239.120.239";
        String password = "123456";
        account.acc_id = "<sip:" + SipUri.encodeUser(userName) + "@" + domain + ">";

        String regUri = "sip:" + domain;
        account.reg_uri = regUri;
        account.proxies = new String[]{regUri};


        account.realm = "*";
        account.username = userName;
        account.data = password;
        account.scheme = SipProfile.CRED_SCHEME_DIGEST;
        account.datatype = SipProfile.CRED_DATA_PLAIN_PASSWD;
        //By default auto transport
        account.transport = SipProfile.TRANSPORT_UDP;
        return account;
    }

    /**
     * Apply default settings for a new account to check very basic coherence of settings and auto-modify settings missing
     *
     * @param account
     */
    private void applyNewAccountDefault(SipProfile account) {
        if (account.use_rfc5626) {
            if (TextUtils.isEmpty(account.rfc5626_instance_id)) {
                String autoInstanceId = (UUID.randomUUID()).toString();
                account.rfc5626_instance_id = "<urn:uuid:" + autoInstanceId + ">";
            }
        }
    }

    public AccountListUtils.AccountStatusDisplay getActiveAccount(Context context) {
        Cursor c = context.getContentResolver().query(SipProfile.ACCOUNT_URI, ACC_PROJECTION, SipProfile.FIELD_ACTIVE + "=?", new String[]{
                "1"
        }, null);
        AccountListUtils.AccountStatusDisplay accountStatusDisplay = null;
        boolean hasSomeSip = false;
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    do {
                        final SipProfile account = new SipProfile(c);
                        accountStatusDisplay = AccountListUtils
                                .getAccountDisplay(context, account.id);
                        if (accountStatusDisplay.availableForCalls) {
                            hasSomeSip = true;
                        }
                    } while (c.moveToNext());
                }
            } catch (Exception e) {
                //  Log.e(THIS_FILE, "Error on looping over sip profiles", e);
            } finally {
                c.close();
            }
        }
        if (!hasSomeSip) {
            //No account is available for call
        }
        return accountStatusDisplay;
    }

}
