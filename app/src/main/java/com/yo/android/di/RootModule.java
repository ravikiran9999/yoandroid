package com.yo.android.di;

import android.content.Context;

import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.adapters.NewSuggestionsAdapter;
import com.yo.android.chat.ChatRefreshBackground;
import com.yo.android.ui.NewFollowMoreTopicsActivity;
import com.yo.android.usecase.AddTopicsUsecase;
import com.yo.android.usecase.AppLogglyUsecase;
import com.yo.android.usecase.ChatNotificationUsecase;
import com.yo.android.usecase.DenominationsUsecase;
import com.yo.android.usecase.PackageDenominationsUsecase;
import com.yo.android.usecase.RandomTopicsUsecase;
import com.yo.android.usecase.WebserviceUsecase;
import com.yo.android.adapters.ChatRoomListAdapter;
import com.yo.android.adapters.CountryCodeListAdapter;
import com.yo.android.adapters.FindPeopleAdapter;
import com.yo.android.adapters.SuggestionsAdapter;
import com.yo.android.adapters.UserChatAdapter;
import com.yo.android.app.BaseApp;
import com.yo.android.chat.firebase.FireBaseAuthToken;
import com.yo.android.chat.firebase.FirebaseService;
import com.yo.android.chat.firebase.RetrieveContactsManager;
import com.yo.android.chat.notification.MyInstanceIDListenerService;
import com.yo.android.chat.notification.PushNotificationService;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.chat.ui.CountryCodeActivity;
import com.yo.android.chat.ui.CreateGroupActivity;
import com.yo.android.chat.ui.GroupContactsActivity;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.chat.ui.SignupActivity;
import com.yo.android.chat.ui.fragments.AppContactsActivity;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.chat.ui.fragments.ChatFragment;
import com.yo.android.chat.ui.fragments.ContactsFragment;
import com.yo.android.chat.ui.fragments.OTPFragment;
import com.yo.android.chat.ui.fragments.UserChatFragment;
import com.yo.android.chat.ui.fragments.YoContactsFragment;
import com.yo.android.crop.MainImageCropActivity;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.flip.MagazineTopicsSelectionFragment;
import com.yo.android.inapp.UnManageInAppPurchaseActivity;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.sync.YoContactsSyncAdapter;
import com.yo.android.ui.AccountDetailsActivity;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.CallLogDetailsActivity;
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
import com.yo.android.ui.NewDailerActivity;
import com.yo.android.ui.NewMagazineActivity;
import com.yo.android.ui.NewOTPActivity;
import com.yo.android.ui.NotificationsActivity;
import com.yo.android.ui.OtherProfilesFollowers;
import com.yo.android.ui.OtherProfilesLikedArticles;
import com.yo.android.ui.OthersMagazinesDetailActivity;
import com.yo.android.ui.OthersProfileActivity;
import com.yo.android.ui.OthersProfileMagazines;
import com.yo.android.ui.PhoneBookActivity;
import com.yo.android.ui.PhoneChatActivity;
import com.yo.android.ui.PlainActivity;
import com.yo.android.ui.SettingsActivity;
import com.yo.android.ui.ShowPhotoActivity;
import com.yo.android.ui.SplashScreenActivity;
import com.yo.android.ui.TabsHeaderActivity;
import com.yo.android.ui.TopicsDetailActivity;
import com.yo.android.ui.TransferBalanceActivity;
import com.yo.android.ui.TransferBalanceSelectContactActivity;
import com.yo.android.ui.UpdateProfileActivity;
import com.yo.android.ui.UserCreatedMagazineActivity;
import com.yo.android.ui.UserProfileActivity;
import com.yo.android.ui.WishListActivity;
import com.yo.android.ui.fragments.AccountDetailsEditFragment;
import com.yo.android.ui.fragments.AccountDetailsFragment;
import com.yo.android.ui.fragments.CreditAccountFragment;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.ui.fragments.GeneralWebViewFragment;
import com.yo.android.ui.fragments.InviteActivity;
import com.yo.android.ui.fragments.InviteFriendsFragment;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.ui.fragments.MoreFragment;
import com.yo.android.ui.fragments.NewContactsFragment;
import com.yo.android.ui.fragments.NewCreditAccountFragment;
import com.yo.android.ui.fragments.NewOTPFragment;
import com.yo.android.ui.fragments.RechargeDetailsFragment;
import com.yo.android.ui.fragments.SpendDetailsFragment;
import com.yo.android.ui.fragments.WebViewFragment;
import com.yo.android.ui.fragments.findpeople.FindPeopleFragment;
import com.yo.android.ui.fragments.findpeople.FollowersFragment;
import com.yo.android.ui.fragments.findpeople.FollowingsFragment;
import com.yo.android.util.FetchNewArticlesService;
import com.yo.android.util.ReCreateService;
import com.yo.android.util.Util;
import com.yo.android.video.InAppVideoActivity;
import com.yo.android.video.YouTubeFailureRecoveryActivity;
import com.yo.android.voip.DialPadView;
import com.yo.android.voip.InComingCallActivity;
import com.yo.android.voip.OutGoingCallActivity;
import com.yo.android.voip.Receiver;
import com.yo.android.voip.SipService;
import com.yo.dialer.DialerHelper;
import com.yo.dialer.NewDialerFragment;
import com.yo.dialer.ui.IncomingCallActivity;
import com.yo.dialer.ui.OutgoingCallActivity;
import com.yo.dialer.yopj.YoSipServiceHandler;
import com.yo.services.BackgroundServices;

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
                CountryCodeActivity.class,
                NewFollowMoreTopicsActivity.class,


                InviteActivity.class,
                AccountDetailsActivity.class,
                OthersProfileMagazines.class,
                OtherProfilesFollowers.class,
                OtherProfilesLikedArticles.class,
                OthersMagazinesDetailActivity.class,
                UnManageInAppPurchaseActivity.class,
                NotificationsActivity.class,
                TopicsDetailActivity.class,
                TransferBalanceSelectContactActivity.class,
                TransferBalanceActivity.class,
                PlainActivity.class,
                NewOTPActivity.class,
                IncomingCallActivity.class,
                OutgoingCallActivity.class,
                InAppVideoActivity.class,
                YouTubeFailureRecoveryActivity.class,
                PhoneBookActivity.class,
                PhoneChatActivity.class,
                CallLogDetailsActivity.class,

                //Fragments
                ContactsFragment.class,
                BaseFragment.class,
                OTPFragment.class,
                UserChatFragment.class,
                DialerFragment.class,
                ChatFragment.class,
                YoContactsFragment.class,
                AccountDetailsFragment.class,
                AccountDetailsEditFragment.class,
                MagazineTopicsSelectionFragment.class,
                MagazineFlipArticlesFragment.class,
                MoreFragment.class,
                MagazinesFragment.class,
                GroupContactsActivity.class,
                InviteFriendsFragment.class,
                CreditAccountFragment.class,
                NewCreditAccountFragment.class,
                SpendDetailsFragment.class,
                RechargeDetailsFragment.class,
                FindPeopleFragment.class,
                FollowersFragment.class,
                FollowingsFragment.class,
                GeneralWebViewFragment.class,
                WebViewFragment.class,
                NewOTPFragment.class,
                NewDialerFragment.class,
                NewContactsFragment.class,

                //Managers
                RetrieveContactsManager.class,

                //Adapters
                ChatRoomListAdapter.class,
                FindPeopleAdapter.class,
                SuggestionsAdapter.class,
                UserChatAdapter.class,
                YoContactsSyncAdapter.class,
                NewSuggestionsAdapter.class,


                FirebaseService.class,
                YoSipService.class,
                ReCreateService.class,
                DialPadView.class,

                FireBaseAuthToken.class,
                NewDailerActivity.class,
                NewContactsFragment.class,
                MainImageCropActivity.class,

                Util.class,
                FetchNewArticlesService.class,
                CountryCodeListAdapter.class,
                BackgroundServices.class,

                com.yo.dialer.YoSipService.class,
                YoSipServiceHandler.class,
                DialerHelper.class,
                ChatRefreshBackground.class,

                //usecases
                WebserviceUsecase.class,
                DenominationsUsecase.class,
                PackageDenominationsUsecase.class,
                ChatNotificationUsecase.class,
                AddTopicsUsecase.class,
                RandomTopicsUsecase.class,
                AppLogglyUsecase.class
        },
        includes = {
                AppModule.class,
                SharedPreferencesModule.class,
                NetWorkModule.class,
                AwsModule.class,
                JobsModule.class
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
