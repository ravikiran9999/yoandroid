package com.yo.android.util;

import android.app.Activity;
import android.content.Context;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ConnectivityHelper;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.model.dialer.OpponentDetails;
import com.yo.android.pjsip.StatusCodes;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.vox.BalanceHelper;

import de.greenrobot.event.EventBus;

/**
 * Created by rdoddapaneni on 5/15/2017.
 */

public class ErrorCode {

    public static void showErrorMessages(final EventBus bus, final OpponentDetails details, final Context context, final ToastFactory mToastFactory, final BalanceHelper mBalanceHelper, final PreferenceEndPoint mPreferenceEndPoint, final ConnectivityHelper mHelper) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int statusCode = details.getStatusCode();
                    switch (statusCode) {
                        case StatusCodes.TWO_THOUSAND_ONE:
                            break;
                        case 408:
                            if (!details.isSelfReject()) {
                                mToastFactory.showToast(R.string.no_answer);
                            }
                            break;
                        case 603:
                        case 486:
                            if (!details.isSelfReject()) {
                                mToastFactory.showToast(R.string.busy);
                            }
                            break;
                        case 404:
                            mToastFactory.showToast(R.string.calls_no_network);
                            break;
                        case 503:
                            if (!mHelper.isConnected()) {
                                mToastFactory.showToast(R.string.calls_no_network);
                                ((Activity) context).finish();
                            } else {
                                if (details != null && details.getVoxUserName() != null && details.getVoxUserName().contains(BuildConfig.RELEASE_USER_TYPE)) {
                                    YODialogs.redirectToPSTN(bus, (Activity) context, details, mPreferenceEndPoint, mBalanceHelper, mToastFactory);
                                }
                            }
                            break;
                        case 487:
                            //Missed call
                            break;
                        case 181:
                            mToastFactory.showToast(R.string.call_forwarded);
                            break;
                        case 182:
                        case 480:
                            mToastFactory.showToast(R.string.no_answer);
                            break;
                        case 180:
                            mToastFactory.showToast(R.string.ringing);
                            break;
                        case 600:
                            mToastFactory.showToast(R.string.all_busy);
                            break;
                        case 403:
                            mToastFactory.showToast(R.string.unknown_error);
                            break;

                    }
                    if (statusCode != 503) {
                        bus.post(DialerFragment.REFRESH_CALL_LOGS);
                        ((Activity) context).finish();
                    }
                }
            });
        }
    }
}
