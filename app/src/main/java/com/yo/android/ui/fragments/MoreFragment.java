package com.yo.android.ui.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.adapters.MoreListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.chat.ui.NonScrollListView;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.helpers.Helper;
import com.yo.android.helpers.PopupHelper;
import com.yo.android.model.MoreData;
import com.yo.android.model.Popup;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.ui.AccountDetailsActivity;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.MoreSettingsActivity;
import com.yo.android.ui.NotificationsActivity;
import com.yo.android.ui.TabsHeaderActivity;
import com.yo.android.ui.uploadphoto.ImagePickHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.ContactSyncHelper;
import com.yo.android.util.FireBaseHelper;
import com.yo.android.util.PopupDialogListener;
import com.yo.android.util.Util;
import com.yo.android.voip.VoipConstants;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoreFragment extends BaseFragment implements AdapterView.OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener, PopupDialogListener {

    private MoreListAdapter menuAdapter;

    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;

    @Inject
    ContactSyncHelper mContactSyncHelper;

    @Inject
    YoApi.YoService yoService;

    FrameLayout changePhoto;

    CircleImageView profilePic;

    @Inject
    ImagePickHelper cameraIntent;

    @Inject
    ConnectivityHelper mHelper;

    @Bind(R.id.add_change_photo_text)
    TextView addOrChangePhotoText;

    @Inject
    FireBaseHelper fireBaseHelper;

    private boolean isAlreadyShown;

    private boolean isRemoved;

    private TextView profileStatus;

    public MoreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        changePhoto = (FrameLayout) view.findViewById(R.id.change_layout);
        profilePic = (CircleImageView) view.findViewById(R.id.profile_pic);
        profileStatus = (TextView) view.findViewById(R.id.profile_status);
        cameraIntent.setActivity(getActivity());
        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraIntent.showDialog();
            }
        });
        loadImage();
        callOtherInfoApi();
    }

    private void loadImage() {
        String avatar = preferenceEndPoint.getStringPreference(Constants.USER_AVATAR);
        String localImage = preferenceEndPoint.getStringPreference(Constants.IMAGE_PATH);
        if (!TextUtils.isEmpty(localImage)) {
            addOrChangePhotoText.setText(getActivity().getResources().getString(R.string.change_photo));
            Glide.with(getActivity()).load(new File(localImage))
                    .dontAnimate()
                    .placeholder(profilePic.getDrawable())
                    .error(profilePic.getDrawable())
                    .fitCenter()
                    .into(profilePic);
        } else if (!TextUtils.isEmpty(avatar)) {
            addOrChangePhotoText.setText(getActivity().getResources().getString(R.string.change_photo));
            Glide.with(getActivity()).load(avatar)
                    .dontAnimate()
                    .placeholder(profilePic.getDrawable())
                    .error(profilePic.getDrawable())
                    .fitCenter()
                    .into(profilePic);
        } else {
            addOrChangePhotoText.setText(getActivity().getResources().getString(R.string.add_photo));
        }

    }

    //Tested and image update is working
    //Make a prompt for pick a image from gallery/camera
    private void uploadFile(final File file) {
        if (!mHelper.isConnected()) {
            mToastFactory.showToast(getResources().getString(R.string.connectivity_network_settings));
            return;
        }

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
                if (response.body() != null) {
                    preferenceEndPoint.saveStringPreference(Constants.USER_AVATAR, response.body().getAvatar());
                }
                loadImage();
            }

            @Override
            public void onFailure(Call<UserProfileInfo> call, Throwable t) {
                dismissProgressDialog();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        profileStatus.setText(preferenceEndPoint.getStringPreference(Constants.DESCRIPTION, ""));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareSettingsList();

        /*if(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
            Type type = new TypeToken<List<Popup>>() {
            }.getType();
            List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
            PopupHelper.getPopup(PopupHelper.PopupsEnum.MORE, popup, getActivity(), preferenceEndPoint, this);
        }*/
    }

    /**
     * Prepares the Settings list
     */
    public void prepareSettingsList() {
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
        menuDataList.add(new MoreData(String.format("Yo Credit (%s %s)", currencySymbol, balance), true));
        menuDataList.add(new MoreData("Account Details", true));
        menuDataList.add(new MoreData("Invite Friends", true));
        menuDataList.add(new MoreData("Notifications", true));
        menuDataList.add(new MoreData("Settings", true));
        menuDataList.add(new MoreData("Sign Out", false));
        return menuDataList;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String name = ((MoreData) parent.getAdapter().getItem(position)).getName();

        if (name.equalsIgnoreCase("Sign Out")) {
            showLogoutDialog();
        } else if (name.equalsIgnoreCase("Invite Friends")) {
            startActivity(new Intent(getActivity(), InviteActivity.class));
        } else if (name.contains("Yo Credit")) {
            startActivity(new Intent(getActivity(), TabsHeaderActivity.class));
        } else if (name.contains("Notifications")) {
            startActivity(new Intent(getActivity(), NotificationsActivity.class));
        } else if ("Settings".equals(name)) {
            Intent intent = new Intent(getActivity(), MoreSettingsActivity.class);
            startActivityForResult(intent, Constants.GO_TO_SETTINGS);
        } else if (name.equalsIgnoreCase("Account Details")) {
            startActivity(new Intent(getActivity(), AccountDetailsActivity.class));
        }
    }


    public void showLogoutDialog() {

        if (getActivity() != null) {

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
                    if (new ConnectivityHelper(getActivity()).isConnected()) {

                        //Clean contact sync
                        mContactSyncHelper.clean();

                        if (getActivity() != null) {
                            Util.cancelAllNotification(getActivity());
                        }
                        Uri uri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI; // Get all entries
                        int deleteContacts = getActivity().getContentResolver().delete(uri, null, null);
                        mLog.i("MoreFragment", "Deleted contacts >>>>%d", deleteContacts);
                        if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER))) {
                            String accessToken = preferenceEndPoint.getStringPreference("access_token");
                            fireBaseHelper.unauth();
                            FirebaseAuth.getInstance().signOut();
                            yoService.updateDeviceTokenAPI(accessToken, null).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {

                                }
                            });
                        }
                        preferenceEndPoint.clearAll();
                        MagazineFlipArticlesFragment.lastReadArticle = 0;

                        //stop firebase service
                        //getActivity().stopService(new Intent(getActivity(), FirebaseService.class));

                        //Stop SIP service
                        Intent intent = new Intent(VoipConstants.ACCOUNT_LOGOUT, null, getActivity(), YoSipService.class);
                        getActivity().startService(intent);
                        //Start login activity
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        getActivity().finish();

                    } else {
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
                        preferenceEndPoint.saveStringPreference(Constants.IMAGE_PATH, imagePath.getPath());
                        uploadFile(new File(imagePath.getPath()));
                    }
                }
                break;
            case Constants.ADD_IMAGE_CAPTURE:
                try {
                    String imagePath = cameraIntent.mFileTemp.getPath();
                    if (imagePath != null) {
                        Helper.setSelectedImage(getActivity(), imagePath, true);
                    }
                } catch (Exception e) {
                    mLog.w("MoreFragment", e);
                }
                break;

            case Constants.ADD_SELECT_PICTURE: {
                if (data != null) {
                    try {
                        String imagePath = ImagePickHelper.getGalleryImagePath(getActivity(), data);
                        Helper.setSelectedImage(getActivity(), imagePath, true);
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Constants.CURRENT_BALANCE)) {
            String balance = mBalanceHelper.getCurrentBalance();
            String currencySymbol = mBalanceHelper.getCurrencySymbol();
            if (menuAdapter != null) {
                menuAdapter.getItem(0).setName(String.format("Yo Credit (%s%s)", currencySymbol, balance));
                menuAdapter.notifyDataSetChanged();
            }
        } else if (key.equals(Constants.USER_NAME)) {
            String username = preferenceEndPoint.getStringPreference(Constants.USER_NAME);
        }

        if (getActivity() instanceof BottomTabsActivity) {
            BottomTabsActivity activity = (BottomTabsActivity) getActivity();
            if (activity.getFragment() instanceof MoreFragment) {
                if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                    if (!isRemoved) {
                        Type type = new TypeToken<List<Popup>>() {
                        }.getType();
                        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                        if (popup != null && popup.size() > 0 && popup.get(0).getPopupsEnum() == PopupHelper.PopupsEnum.MORE) {
                            if (!isAlreadyShown) {
                                PopupHelper.getPopup(PopupHelper.PopupsEnum.MORE, popup, getActivity(), preferenceEndPoint, this, this);
                                isAlreadyShown = true;
                            }
                        }
                    } /*else {
                        isRemoved = false;
                    }*/
                }
            }
        }

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getActivity() instanceof BottomTabsActivity) {
                BottomTabsActivity activity = (BottomTabsActivity) getActivity();
                if (activity.getFragment() instanceof MoreFragment) {
                    if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                        Type type = new TypeToken<List<Popup>>() {
                        }.getType();
                        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                        if (popup != null && popup.size() > 0 && popup.get(0).getPopupsEnum() == PopupHelper.PopupsEnum.MORE) {
                            if (!isAlreadyShown) {
                                PopupHelper.getPopup(PopupHelper.PopupsEnum.MORE, popup, getActivity(), preferenceEndPoint, this, this);
                                isAlreadyShown = true;
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
        isAlreadyShown = false;
        isRemoved = true;
        //preferenceEndPoint.removePreference(Constants.POPUP_NOTIFICATION);
        Type type = new TypeToken<List<Popup>>() {
        }.getType();
        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
        popup.remove(0);
        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(popup));
    }

    private void callOtherInfoApi() {
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        String access = "Bearer " + preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        String userName = preferenceEndPoint.getStringPreference(Constants.USER_NAME);
        RequestBody firstName =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), userName);
        yoService.updateProfile(userId, access, null, firstName, null, null, null, null, null, null, null).enqueue(new Callback<UserProfileInfo>() {
            @Override
            public void onResponse(Call<UserProfileInfo> call, Response<UserProfileInfo> response) {
                dismissProgressDialog();
                if (response.body() != null) {
                    saveUserProfileValues(response.body());
                }
                loadImage();
                profileStatus.setText(preferenceEndPoint.getStringPreference(Constants.DESCRIPTION, ""));
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

}
