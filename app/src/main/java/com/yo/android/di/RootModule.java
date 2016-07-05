package com.yo.android.di;

import android.content.Context;

import com.yo.android.app.BaseApp;
import com.yo.android.chat.notification.MyInstanceIDListenerService;
import com.yo.android.chat.notification.PushNotificationService;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.chat.ui.fragments.ChatFragment;
import com.yo.android.chat.ui.fragments.ContactsFragment;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.chat.ui.fragments.OTPFragment;
import com.yo.android.chat.ui.SignupActivity;
import com.yo.android.chat.ui.fragments.UserChatFragment;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.flip.MagazineTopicsSelectionFragment;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.ui.DialerActivity;
import com.yo.android.ui.MainActivity;
import com.yo.android.ui.NavigationDrawerActivity;
import com.yo.android.ui.SettingsActivity;
import com.yo.android.ui.SplashScreenActivity;
import com.yo.android.ui.fragments.DialerFragment;
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
                BottomTabsActivity.class,
                SettingsActivity.class,
                LoginActivity.class,
                SettingsActivity.class,
                DialerActivity.class,
                OutGoingCallActivity.class,
                SipService.class,
                Receiver.class,
                InComingCallActivity.class,
                SplashScreenActivity.class,
                SignupActivity.class,
                MagazineArticleDetailsActivity.class,
                ChatActivity.class,

                //Fragments
                ContactsFragment.class,
                BaseFragment.class,
                OTPFragment.class,
                UserChatFragment.class,
                DialerFragment.class,
                ChatFragment.class,
                ChatFragment.class,
                MagazineTopicsSelectionFragment.class,
                MagazineFlipArticlesFragment.class,


        },
        includes = {
                AppModule.class,
                SharedPreferencesModule.class,
                NetWorkModule.class,
                AwsModule.class
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
