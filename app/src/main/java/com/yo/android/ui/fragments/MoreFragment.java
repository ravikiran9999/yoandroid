package com.yo.android.ui.fragments;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.flurry.android.FlurryAgent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.adapters.BalanceAdapter;
import com.yo.android.adapters.MoreListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.calllogs.CallLog;
import com.yo.android.chat.firebase.FirebaseService;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.chat.ui.NonScrollListView;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.database.ChatMessageDao;
import com.yo.android.database.RoomDao;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.helpers.Helper;
import com.yo.android.helpers.PopupHelper;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.MoreData;
import com.yo.android.model.Popup;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.networkmanager.StartServiceAtBootReceiver;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.ui.AccountDetailsActivity;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.MoreSettingsActivity;
import com.yo.android.ui.NotificationsActivity;
import com.yo.android.ui.TabsHeaderActivity;
import com.yo.android.ui.uploadphoto.ImagePickHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.ContactSyncHelper;
import com.yo.android.util.FetchNewArticlesService;
import com.yo.android.util.FireBaseHelper;
import com.yo.android.util.PopupDialogListener;
import com.yo.android.util.Util;
import com.yo.android.voip.VoipConstants;
import com.yo.dialer.CallExtras;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.yo.dialer.googlesheet.UploadCallDetails.PREF_ACCOUNT_NAME;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoreFragment extends BaseFragment implements AdapterView.OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener, PopupDialogListener, BalanceAdapter.MoreItemListener {

    private MoreListAdapter menuAdapter;
    private BalanceAdapter balanceAdapter;

    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;

    @Inject
    ContactSyncHelper mContactSyncHelper;

    @Inject
    YoApi.YoService yoService;

    @Inject
    ImagePickHelper cameraIntent;

    @Inject
    ConnectivityHelper mHelper;

    @Inject
    FireBaseHelper fireBaseHelper;

    @Inject
    RoomDao roomDao;
    @Inject
    ChatMessageDao chatMessageDao;

    @Bind(R.id.add_change_photo_text)
    TextView addOrChangePhotoText;

    @Bind(R.id.profile_pic)
    CircleImageView profilePic;

    FrameLayout changePhoto;
    private boolean isAlreadyShown;
    private TextView profileStatus;
    private boolean isSharedPreferenceShown;

    private boolean isEventLogged;
    private Activity activity;

    public MoreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        if (!isEventLogged) {
            if (getActivity() instanceof BottomTabsActivity) {
                BottomTabsActivity activity = (BottomTabsActivity) getActivity();
                if (activity.getFragment() instanceof MoreFragment) {
                    // Capture user id
                    Map<String, String> profileParams = new HashMap<String, String>();
                    String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                    //param keys and values have to be of String type
                    profileParams.put("UserId", userId);

                    FlurryAgent.logEvent("Profile", profileParams, true);
                }

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.more_fragment, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        changePhoto = (FrameLayout) view.findViewById(R.id.change_layout);
        profileStatus = (TextView) view.findViewById(R.id.profile_status);
        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraIntent.showDialog();
            }
        });

        initCircularView();
        loadImage();
        callOtherInfoApi();
    }

    private void initCircularView() {
        profilePic.setBorderColor(getResources().getColor(R.color.white));
        profilePic.setBorderWidth(5);
    }

    public void loadImage() {
        if (preferenceEndPoint != null && addOrChangePhotoText != null && getActivity() != null) {
            String avatar = preferenceEndPoint.getStringPreference(Constants.USER_AVATAR);
            String localImage = preferenceEndPoint.getStringPreference(Constants.IMAGE_PATH);
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.default_avatar_40)
                    .error(R.drawable.default_avatar_40)
                    .dontAnimate()
                    .fitCenter();
            if (!TextUtils.isEmpty(localImage)) {
                addOrChangePhotoText.setText(getActivity().getResources().getString(R.string.change_photo));
                Glide.with(getActivity()).load(new File(localImage))
                        .apply(requestOptions)
                        .into(profilePic);
            } else if (!TextUtils.isEmpty(avatar)) {
                addOrChangePhotoText.setText(getActivity().getResources().getString(R.string.change_photo));
                Glide.with(getActivity()).clear(profilePic);
                Glide.with(getActivity()).load(avatar)
                        .apply(requestOptions)
                        .into(profilePic);
            } else {
                profilePic.setImageResource(R.drawable.default_avatar_40);
                addOrChangePhotoText.setText(getActivity().getResources().getString(R.string.add_photo));
            }
        }
    }

    //Tested and image update is working
    //Make a prompt for pick a image from gallery/camera
    private void uploadFile(final File file) {
        if (mHelper != null && !mHelper.isConnected()) {
            mToastFactory.showToast(getResources().getString(R.string.connectivity_network_settings));
            return;
        }
        if (preferenceEndPoint != null && yoService != null) {
            showProgressDialog();
            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            // create RequestBody instance from file
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), file);

            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("user[avatar]", file.getName(), requestFile);
            String access = "Bearer " + preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
            yoService.updateProfile(userId, access, null, null, null, null, null, null, null, null, body).enqueue(new Callback<UserProfileInfo>() {
                @Override
                public void onResponse(Call<UserProfileInfo> call, Response<UserProfileInfo> response) {
                    dismissProgressDialog();
                    try {
                        if (response.body() != null) {
                            preferenceEndPoint.saveStringPreference(Constants.USER_AVATAR, response.body().getAvatar());
                        }
                        loadImage();
                    } finally {
                        if (response != null && response.body() != null) {
                            try {
                                response = null;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<UserProfileInfo> call, Throwable t) {
                    dismissProgressDialog();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraIntent.setActivity(getActivity());
        String status = preferenceEndPoint.getStringPreference(Constants.DESCRIPTION, "Available");
        if (status.equalsIgnoreCase("")) {
            status = "Available";
        }
        profileStatus.setText(status);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            activity = (Activity) context;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareSettingsList();
    }

    /**
     * Prepares the Settings list
     */
    /*public void prepareSettingsList() {
        menuAdapter = new MoreListAdapter(getActivity()) {
            @Override
            public int getLayoutId() {
                return R.layout.item_with_options;
            }
        };
        ListView menuListView = (ListView) getView().findViewById(R.id.lv_settings);
        menuAdapter.addItems(getMenuList());

        menuListView.setAdapter(menuAdapter);
        menuListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        menuListView.setSelection(0);
        menuListView.setOnItemClickListener(this);
    }*/
    public void prepareSettingsList() {
        /*menuAdapter = new MoreListAdapter(getActivity()) {
            @Override
            public int getLayoutId() {
                return R.layout.item_with_options;
            }
        };*/
        ArrayList<Object> data = new ArrayList<>();
        data.addAll(getMenuList());
        balanceAdapter = new BalanceAdapter(getActivity(), data, null, MoreFragment.this);
        balanceAdapter.setMoreItemListener(this);
        RecyclerView menuListView = (RecyclerView) getView().findViewById(R.id.lv_settings);
        menuListView.setAdapter(balanceAdapter);
        menuListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        balanceAdapter.notifyDataSetChanged();

    }

    /**
     * Creates the Settings list
     *
     * @return
     */
    public List<MoreData> getMenuList() {

        List<MoreData> menuDataList = new ArrayList<>();
        // menuDataList.add(new MoreData(preferenceEndPoint.getStringPreference(Constants.USER_NAME), false));
        String phone = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        // menuDataList.add(new MoreData(phone, false));
        String balance = mBalanceHelper.getCurrentBalance();
        String currencySymbol = mBalanceHelper.getCurrencySymbol();
        //menuDataList.add(new MoreData(String.format(getString(R.string.yocredit), currencySymbolDollarNoSpace, balance), true));
        menuDataList.add(new MoreData(String.format(getString(R.string.your_yo_balance_without_line_break), mBalanceHelper.currencySymbolLookup(balance)), true, null));
        menuDataList.add(new MoreData(getString(R.string.accountdetails), true, null));
        menuDataList.add(new MoreData(getString(R.string.invitefriends), true, null));
        menuDataList.add(new MoreData(getString(R.string.morenotifications), true, null));
        menuDataList.add(new MoreData(getString(R.string.settings), true, null));
        menuDataList.add(new MoreData(getString(R.string.signout), false, null));
        return menuDataList;
    }

    //Todo remove this as we are not using
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String name = ((MoreData) parent.getAdapter().getItem(position)).getName();

        if (name.equalsIgnoreCase(getString(R.string.signout))) {
            showLogoutDialog();
        } else if (name.equalsIgnoreCase(getString(R.string.invitefriends))) {
            startActivity(new Intent(getActivity(), InviteActivity.class));
        } else if (name.contains("Yo Credit")) {
            startActivity(new Intent(getActivity(), TabsHeaderActivity.class));
        } else if (name.contains(getString(R.string.morenotifications))) {
            startActivity(new Intent(getActivity(), NotificationsActivity.class));
        } else if (name.equals(getString(R.string.settings))) {
            Intent intent = new Intent(getActivity(), MoreSettingsActivity.class);
            startActivityForResult(intent, Constants.GO_TO_SETTINGS);
        } else if (name.equalsIgnoreCase(getString(R.string.accountdetails))) {
            startActivity(new Intent(getActivity(), AccountDetailsActivity.class));
        }
    }


    public void showLogoutDialog() {
        if (activity == null) {
            activity = getActivity();
        }
        if (activity != null) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            final View view = layoutInflater.inflate(R.layout.custom_signout, null);
            builder.setView(view);

            Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
            Button noBtn = (Button) view.findViewById(R.id.no_btn);


            final AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();

            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    if (isAdded()) {
                        showProgressDialog();
                    }
                    if (new ConnectivityHelper(getActivity()).isConnected()) {

                        //Clean contact sync
                        mContactSyncHelper.clean();

                        if (activity != null) {
                            Util.cancelAllNotification(getActivity());
                            //  23	Data is missing in dialer screen once user logouts & login again  - Fixed
                            CallLog.Calls.clearCallHistory(getActivity());
                        }

                        //Delete user from PJSIP
                        Intent service = new Intent(getActivity(), com.yo.dialer.YoSipService.class);
                        service.setAction(CallExtras.UN_REGISTER);
                        getActivity().startService(service);


                        Uri uri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI; // Get all entries
                        int deleteContacts = getActivity().getContentResolver().delete(uri, null, null);
                        mLog.i("MoreFragment", "Deleted contacts >>>>%d", deleteContacts);
                        if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER))) {
                            String accessToken = preferenceEndPoint.getStringPreference("access_token");
                            yoService.updateDeviceTokenAPI(accessToken, null).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    try {
                                        if (activity instanceof BaseActivity) {
                                            ((BaseActivity) activity).stopTimer();
                                        }

                                        //stop service on logout
                                        activity.stopService(new Intent(activity, FirebaseService.class));

                                        fireBaseHelper.unauth();

                                        //FirebaseAuth.getInstance().signOut();

                                        clearPreferences();
                                        preferenceEndPoint.clearAll();
                                        MagazineFlipArticlesFragment.lastReadArticle = 0;
                                        if (getActivity() != null) {
                                            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                                            try {
                                                if (BottomTabsActivity.pintent != null) {
                                                    alarmManager.cancel(BottomTabsActivity.pintent);
                                                }
                                                if (FetchNewArticlesService.pintent != null) {
                                                    alarmManager.cancel(FetchNewArticlesService.pintent);
                                                }
                                                if (StartServiceAtBootReceiver.pintent != null) {
                                                    alarmManager.cancel(StartServiceAtBootReceiver.pintent);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        //stop firebase service
                                        //getActivity().stopService(new Intent(getActivity(), FirebaseService.class));

                                        //Stop SIP service
                                        Intent intent = new Intent(VoipConstants.ACCOUNT_LOGOUT, null, getActivity(), YoSipService.class);
                                        getActivity().startService(intent);
                                        //Start login activity
                                        startActivity(new Intent(getActivity(), LoginActivity.class));
                                        getActivity().finish();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (isAdded()) {
                                                    dismissProgressDialog();
                                                }
                                            }
                                        }, 1000);
                                    } finally {
                                        if (response != null && response.body() != null) {
                                            response.body().close();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    mToastFactory.showToast(R.string.connectivity_network_settings);
                                    if (isAdded()) {
                                        dismissProgressDialog();
                                    }
                                }
                            });
                        }


                    } else {
                        dismissProgressDialog();
                        mToastFactory.showToast(R.string.connectivity_network_settings);
                    }
                }
            });

            noBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        switch (requestCode) {
            case Helper.CROP_ACTIVITY:
                if (data != null && data.hasExtra(Helper.IMAGE_PATH)) {
                    Uri imagePath = Uri.parse(data.getStringExtra(Helper.IMAGE_PATH));
                    if (imagePath != null) {
                        uploadFile(new File(imagePath.getPath()));
                    }
                }
                break;
            case Constants.ADD_IMAGE_CAPTURE:
                try {
                    String imagePath = ImagePickHelper.mFileTemp.getPath();
                    File file = new File(imagePath);
                    Uri uri = Uri.fromFile(file);
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
                        if (imagePath != null) {
                            if (activity != null) {
                                Helper.setSelectedImage(activity, imagePath, true, bitmap, true);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    // mLog.w("MoreFragment", e);
                }
                break;

            case Constants.ADD_SELECT_PICTURE: {
                if (data != null) {
                    try {
                        String imagePath = ImagePickHelper.getGalleryImagePath(activity, data);
                        Helper.setSelectedImage(activity, imagePath, true, null, false);
                    } catch (Exception e) {
                        mLog.w("MoreFragment", e);
                    }
                }
            }
            case Constants.GO_TO_SETTINGS:
                if (menuAdapter != null) {
                    menuAdapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Constants.CURRENT_BALANCE)) {
            String balance = mBalanceHelper.getCurrentBalance();
            //String currencySymbol = mBalanceHelper.getCurrencySymbol();
            if (balanceAdapter != null) {
                balanceAdapter.getItem(0).setName(String.format(getString(R.string.your_yo_balance_without_line_break), mBalanceHelper.currencySymbolLookup(balance)));
                balanceAdapter.notifyDataSetChanged();
            }
        } else if (key.equals(Constants.USER_NAME)) {
            String username = preferenceEndPoint.getStringPreference(Constants.USER_NAME);
        }

        if (getActivity() instanceof BottomTabsActivity) {
            BottomTabsActivity activity = (BottomTabsActivity) getActivity();
            if (activity.getFragment() instanceof MoreFragment) {
                if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                    Type type = new TypeToken<List<Popup>>() {
                    }.getType();
                    List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                    if (popup != null) {
                        for (Popup p : popup) {
                            if (p.getPopupsEnum() == PopupHelper.PopupsEnum.MORE) {
                                if (!isAlreadyShown) {
                                    PopupHelper.getSinglePopup(PopupHelper.PopupsEnum.MORE, p, getActivity(), preferenceEndPoint, this, this, popup);
                                    isAlreadyShown = true;
                                    isSharedPreferenceShown = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

            if (preferenceEndPoint != null) {
                // Capture user id
                Map<String, String> profileParams = new HashMap<String, String>();
                String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                //param keys and values have to be of String type
                profileParams.put("UserId", userId);

                FlurryAgent.logEvent("Profile", profileParams, true);
                isEventLogged = true;
            }

            if (getActivity() instanceof BottomTabsActivity) {
                BottomTabsActivity activity = (BottomTabsActivity) getActivity();
                if (activity.getFragment() instanceof MoreFragment) {
                    if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                        Type type = new TypeToken<List<Popup>>() {
                        }.getType();
                        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                        if (popup != null) {
                            Collections.reverse(popup);
                            isAlreadyShown = false;
                            for (Popup p : popup) {
                                if (p.getPopupsEnum() == PopupHelper.PopupsEnum.MORE) {
                                    if (!isAlreadyShown) {
                                        PopupHelper.getSinglePopup(PopupHelper.PopupsEnum.MORE, p, getActivity(), preferenceEndPoint, this, this, popup);
                                        isAlreadyShown = true;
                                        isSharedPreferenceShown = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } else {
        }
    }

    @Override
    public void closePopup() {
        Type type = new TypeToken<List<Popup>>() {
        }.getType();
        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
        if (popup != null) {
            if (!isSharedPreferenceShown) {
                Collections.reverse(popup);
            }
            List<Popup> tempPopup = new ArrayList<>(popup);
            for (Popup p : popup) {
                if (p.getPopupsEnum() == PopupHelper.PopupsEnum.MORE) {
                    tempPopup.remove(p);
                    break;
                }
            }
            popup = tempPopup;
        }
        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(popup));
    }

    private void callOtherInfoApi() {
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        String access = "Bearer " + preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        String userName = preferenceEndPoint.getStringPreference(Constants.USER_NAME);
        RequestBody firstName =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), userName);
        //showProgressDialog();
        yoService.updateProfile(userId, access, null, firstName, null, null, null, null, null, null, null).enqueue(new Callback<UserProfileInfo>() {
            @Override
            public void onResponse(Call<UserProfileInfo> call, Response<UserProfileInfo> response) {
                dismissProgressDialog();
                try {
                    if (response.body() != null) {
                        saveUserProfileValues(response.body());
                    }
                    loadImage();
                    String status = preferenceEndPoint.getStringPreference(Constants.DESCRIPTION, "Available");
                    if (status.equalsIgnoreCase("")) {
                        status = "Available";
                    }
                    profileStatus.setText(status);
                } finally {
                    if (response != null && response.body() != null) {
                        try {
                            response = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfileInfo> call, Throwable t) {
                dismissProgressDialog();
            }
        });
    }

    private void saveUserProfileValues(final UserProfileInfo response) {
        String avatar = response.getAvatar();
        String email = response.getEmail();
        String description = response.getDescription();
        String dob = response.getDob();
        String gender = response.getGender();
        String firstName = response.getFirstName();
        String phoneNo = response.getPhoneNumber();
        preferenceEndPoint.saveStringPreference(Constants.USER_AVATAR, avatar);
        preferenceEndPoint.saveStringPreference(Constants.EMAIL, email);
        preferenceEndPoint.saveStringPreference(Constants.DESCRIPTION, description);
        preferenceEndPoint.saveStringPreference(Constants.DOB, dob);
        preferenceEndPoint.saveStringPreference(Constants.GENDER, gender);
        preferenceEndPoint.saveStringPreference(Constants.FIRST_NAME, firstName);
        preferenceEndPoint.saveStringPreference(Constants.PHONE_NO, phoneNo);
        preferenceEndPoint.saveStringPreference(Constants.AVATAR_TEMP, avatar);
        preferenceEndPoint.saveStringPreference(Constants.EMAIL_TEMP, email);
        preferenceEndPoint.saveStringPreference(Constants.DESCRIPTION_TEMP, description);
        preferenceEndPoint.saveStringPreference(Constants.DOB_TEMP, dob);
        preferenceEndPoint.saveStringPreference(Constants.GENDER_TEMP, gender);
        preferenceEndPoint.saveStringPreference(Constants.FIRST_NAME_TEMP, firstName);
        preferenceEndPoint.saveStringPreference(Constants.PHONE_NO_TEMP, phoneNo);
    }

    @Override
    public void onRowSelected(int keyIndex) {
        switch (keyIndex) {
            case 0:
                startActivity(new Intent(getActivity(), TabsHeaderActivity.class));
                break;
            case 1:
                startActivity(new Intent(getActivity(), AccountDetailsActivity.class));
                break;
            case 2:
                startActivity(new Intent(getActivity(), InviteActivity.class));
                break;
            case 3:
                startActivity(new Intent(getActivity(), NotificationsActivity.class));
                break;
            case 4:
                Intent intent = new Intent(getActivity(), MoreSettingsActivity.class);
                startActivityForResult(intent, Constants.GO_TO_SETTINGS);
                break;
            case 5:
                showLogoutDialog();
                break;

        }
    }

    private void clearPreferences() {
        // clear balance
        preferenceEndPoint.removePreference(Constants.CURRENT_BALANCE);
        preferenceEndPoint.removePreference(Constants.SWITCH_BALANCE);
        preferenceEndPoint.removePreference(Constants.WALLET_BALANCE);
        preferenceEndPoint.removePreference(Constants.CURRENCY_SYMBOL);

        // clear firebase cached rooms
        preferenceEndPoint.removePreference(Constants.FIRE_BASE_ROOMS);
        preferenceEndPoint.removePreference(Constants.FIRE_BASE_ROOMS);

        // delete realm database on signout
        try {
            roomDao.clearDatabase();
            chatMessageDao.clearDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // clear firebase userId
        preferenceEndPoint.removePreference(Constants.FIREBASE_USER_ID);
        // clear firebase authToken
        preferenceEndPoint.removePreference(Constants.FIREBASE_TOKEN);

        //clear google sheet upload account name
        preferenceEndPoint.removePreference(PREF_ACCOUNT_NAME);

    }
}
