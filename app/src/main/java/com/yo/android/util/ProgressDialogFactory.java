package com.yo.android.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.orion.android.common.logger.Log;
import com.yo.android.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Ramesh on 15/3/16.
 */
@Singleton
public class ProgressDialogFactory {

    final Context mContext;
    final Log mLog;

    @Inject
    public ProgressDialogFactory(Context context, Log log) {
        mContext = context;
        mLog = log;
    }

    /**
     * The Class HideProgressNumbersDialog.
     */
    public static class HideProgressNumbersDialog extends ProgressDialog {
        final Log mLog;

        /**
         * Instantiates a new hide progress numbers dialog.
         *
         * @param context the context
         */
        public HideProgressNumbersDialog(final Context context, final Log log) {
            super(context);
            mLog = log;
        }

        /**
         * On create.
         *
         * @param savedInstanceState the saved instance state
         */
        @Override
        protected void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            hideProgressNumber();
        }

        /**
         * Hide progress number.
         */
        private void hideProgressNumber() {
            try {
                final Method method = TextView.class.getMethod("setVisibility", Integer.TYPE);
                final Field[] fields = this.getClass().getSuperclass().getDeclaredFields();

                if (method != null && fields != null) {
                    for (final Field field : fields) {
                        if ("mProgressNumber".equalsIgnoreCase(field.getName())) {
                            field.setAccessible(true);
                            final TextView textView = (TextView) field.get(this);
                            if (textView != null) {
                                method.invoke(textView, View.GONE);
                            }
                            break;
                        }
                    }
                }
            } catch (final Exception e) {
                mLog.w("ProgressDialogFactory", e);
            }
        }

    }

    public Dialog createNormalProgressDialog(Activity activity) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        final boolean hasNewApis = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
        final ProgressDialog dialog = (hasNewApis) ? new ProgressDialog(activity)
                : new HideProgressNumbersDialog(activity, mLog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOwnerActivity(activity);
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setOnCancelListener(null);
        return progressDialog;
    }

    public Dialog createTransparentDialog(Activity activity) {
        Dialog dialogTransparent = new Dialog(activity, android.R.style.Theme_Black);
        View view = LayoutInflater.from(activity).inflate(
                R.layout.remove_border, null);
        dialogTransparent.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogTransparent.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent);
        dialogTransparent.setContentView(view);
        return dialogTransparent;
    }
}
