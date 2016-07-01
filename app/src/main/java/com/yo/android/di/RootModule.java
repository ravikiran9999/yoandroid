package com.yo.android.di;

import android.content.Context;

import com.yo.android.app.BaseApp;
import com.yo.android.chat.notification.MyInstanceIDListenerService;
import com.yo.android.chat.notification.PushNotificationService;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.chat.ui.SignupActivity;
import com.yo.android.ui.DialerActivity;
import com.yo.android.ui.MainActivity;
import com.yo.android.ui.NavigationDrawerActivity;
import com.yo.android.ui.SettingsActivity;
import com.yo.android.ui.SplashScreenActivity;
import com.yo.android.voip.InComingCallActivity;
import com.yo.android.voip.OutGoingCallActivity;
import com.yo.android.voip.Receiver;
import com.yo.android.voip.SipService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Ramesh on 17/06/16.
 */
@Module(
        injects = {
                BaseApp.class,
                MyInstanceIDListenerService.class,
                PushNotificationService.class,

                //Activities
                MainActivity.class,
                NavigationDrawerActivity.class,
                SettingsActivity.class,
                LoginActivity.class,
                SettingsActivity.class,
                DialerActivity.class,
                OutGoingCallActivity.class,
                SipService.class,
                Receiver.class,
                InComingCallActivity.class,
                SplashScreenActivity.class,
                SignupActivity.class

        },
        includes = {
                AppModule.class,
                SharedPreferencesModule.class
        }
)

public class RootModule {

    private BaseApp app;

    /**
     * Constructor
     *
     * @param app
     */
    public RootModule(BaseApp app) {
        this.app = app;
    }

    /**
     * The provide application context
     *
     * @return
     */
    @Provides
    @Singleton
    public Context provideApplicationContext() {
        return app;
    }

}
