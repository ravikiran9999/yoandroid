package com.yo.android.di;

import android.content.Context;

import com.yo.android.adapters.ChatRoomListAdapter;
import com.yo.android.adapters.FindPeopleAdapter;
import com.yo.android.app.BaseApp;
import com.yo.android.chat.firebase.FirebaseService;
import com.yo.android.chat.firebase.RetrieveContactsManager;
import com.yo.android.chat.notification.MyInstanceIDListenerService;
import com.yo.android.chat.notification.PushNotificationService;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.chat.ui.CreateGroupActivity;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.chat.ui.SignupActivity;
import com.yo.android.chat.ui.fragments.AppContactsActivity;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.chat.ui.fragments.ChatFragment;
import com.yo.android.chat.ui.fragments.ContactsFragment;
import com.yo.android.chat.ui.GroupContactsActivity;
import com.yo.android.chat.ui.fragments.OTPFragment;
import com.yo.android.chat.ui.fragments.UserChatFragment;
import com.yo.android.chat.ui.fragments.YoContactsFragment;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.flip.MagazineTopicsSelectionFragment;
import com.yo.android.inapp.UnManageInAppPurchaseActivity;
import com.yo.android.sync.YoContactsSyncAdapter;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.CountryListActivity;
import com.yo.android.ui.CreateMagazineActivity;
import com.yo.android.ui.CreatedMagazineDetailActivity;
import com.yo.android.ui.DialerActivity;
import com.yo.android.ui.EditMagazineActivity;
import com.yo.android.ui.FindPeopleActivity;
import com.yo.android.ui.FollowMoreTopicsActivity;
import com.yo.android.ui.FollowersActivity;
import com.yo.android.ui.FollowingsActivity;
import com.yo.android.ui.LoadMagazineActivity;
import com.yo.android.ui.MainActivity;
import com.yo.android.ui.MoreSettingsActivity;
import com.yo.android.ui.MyCollectionDetails;
import com.yo.android.ui.MyCollections;
import com.yo.android.ui.NavigationDrawerActivity;
import com.yo.android.ui.NewMagazineActivity;
import com.yo.android.ui.NotificationsActivity;
import com.yo.android.ui.OtherProfilesFollowers;
import com.yo.android.ui.OtherProfilesLinedArticles;
import com.yo.android.ui.OthersMagazinesDetailActivity;
import com.yo.android.ui.OthersProfileActivity;
import com.yo.android.ui.OthersProfileMagazines;
import com.yo.android.ui.UpdateProfileActivity;
import com.yo.android.ui.SettingsActivity;
import com.yo.android.ui.ShowPhotoActivity;
import com.yo.android.ui.SplashScreenActivity;
import com.yo.android.ui.TabsHeaderActivity;
import com.yo.android.ui.UserCreatedMagazineActivity;
import com.yo.android.ui.UserProfileActivity;
import com.yo.android.ui.WishListActivity;
import com.yo.android.ui.fragments.CreditAccountFragment;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.ui.fragments.InviteActivity;
import com.yo.android.ui.fragments.InviteFriendsFragment;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.ui.fragments.MoreFragment;
import com.yo.android.ui.fragments.RechargeDetailsFragment;
import com.yo.android.ui.fragments.SpendDetailsFragment;
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
                AppContactsActivity.class,
                NewMagazineActivity.class,
                FollowMoreTopicsActivity.class,
                CreateGroupActivity.class,
                CountryListActivity.class,
                CreateMagazineActivity.class,
                MyCollections.class,
                MyCollectionDetails.class,
                UserCreatedMagazineActivity.class,
                LoadMagazineActivity.class,
                ShowPhotoActivity.class,
                OthersProfileActivity.class,
                UpdateProfileActivity.class,
                CreatedMagazineDetailActivity.class,
                EditMagazineActivity.class,
                FindPeopleActivity.class,
                FollowersActivity.class,
                WishListActivity.class,
                FollowingsActivity.class,
                TabsHeaderActivity.class,
                MoreSettingsActivity.class,
                UserProfileActivity.class,

                InviteActivity.class,
                OthersProfileMagazines.class,
                OtherProfilesFollowers.class,
                OtherProfilesLinedArticles.class,
                OthersMagazinesDetailActivity.class,
                UnManageInAppPurchaseActivity.class,
                NotificationsActivity.class,

                //Fragments
                ContactsFragment.class,
                BaseFragment.class,
                OTPFragment.class,
                UserChatFragment.class,
                DialerFragment.class,
                ChatFragment.class,
                YoContactsFragment.class,
                MagazineTopicsSelectionFragment.class,
                MagazineFlipArticlesFragment.class,
                MoreFragment.class,
                MagazinesFragment.class,
                GroupContactsActivity.class,
                InviteFriendsFragment.class,
                CreditAccountFragment.class,
                SpendDetailsFragment.class,
                RechargeDetailsFragment.class,

                //Managers
                RetrieveContactsManager.class,

                //Adapters
                ChatRoomListAdapter.class,
                FindPeopleAdapter.class,

                FirebaseService.class,
                YoContactsSyncAdapter.class
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
