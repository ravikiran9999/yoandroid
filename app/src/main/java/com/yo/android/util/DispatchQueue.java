package com.yo.android.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.orion.android.common.logger.Log;
import com.orion.android.common.logger.LogImpl;

import java.util.concurrent.CountDownLatch;

public class DispatchQueue extends Thread {
    private static final String TAG = "DispatchQueue";
    private volatile Handler handler = null;
    private CountDownLatch syncLatch = new CountDownLatch(1);
    private Log mLog = new LogImpl();

    public DispatchQueue(final String threadName) {
        setName(threadName);
        start();
    }

    private void sendMessage(Message msg, int delay) {
        try {
            syncLatch.await();
            if (delay <= 0) {
                handler.sendMessage(msg);
            } else {
                handler.sendMessageDelayed(msg, delay);
            }
        } catch (Exception e) {
            mLog.w(TAG, e);
        }
    }

    public void cancelRunnable(Runnable runnable) {
        try {
            syncLatch.await();
            handler.removeCallbacks(runnable);
        } catch (Exception e) {
            mLog.w(TAG, e);
        }
    }

    public void postRunnable(Runnable runnable) {
        postRunnable(runnable, 0);
    }

    public void postRunnable(Runnable runnable, long delay) {
        try {
            syncLatch.await();
            if (delay <= 0) {
                handler.post(runnable);
            } else {
                handler.postDelayed(runnable, delay);
            }
        } catch (Exception e) {
            mLog.w(TAG, e);
        }
    }

    public void cleanupQueue() {
        try {
            syncLatch.await();
            handler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            mLog.w(TAG, e);
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new Handler();
        syncLatch.countDown();
        Looper.loop();
    }
}
