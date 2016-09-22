package com.yo.android.ui.fragments;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.adapters.MoreListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.MoreData;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.MoreSettingsActivity;
import com.yo.android.ui.NotificationsActivity;
import com.yo.android.ui.TabsHeaderActivity;
import com.yo.android.ui.uploadphoto.ImagePickHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.ContactSyncHelper;
import com.yo.android.util.FireBaseHelper;
import com.yo.android.util.Util;
import com.yo.android.voip.VoipConstants;

import java.io.File;
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
public class MoreFragment extends BaseFragment implements AdapterView.OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {


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

    public MoreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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
        if (getActivity() instanceof BottomTabsActivity) {
            BottomTabsActivity activity = (BottomTabsActivity) getActivity();
            activity.getSupportActionBar().hide();
        }

        return inflater.inflate(R.layout.more_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        changePhoto = (FrameLayout) view.findViewById(R.id.change_layout);
        profilePic = (CircleImageView) view.findViewById(R.id.profile_pic);
        cameraIntent.setActivity(getActivity());

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraIntent.showDialog();
            }
        });
        loadImage();


    }

    private void loadImage() {
        String avatar = preferenceEndPoint.getStringPreference(Constants.USER_AVATAR);
        if (!TextUtils.isEmpty(avatar)) {
            addOrChangePhotoText.setText(getActivity().getResources().getString(R.string.change_photo));
            // setup Glide request without the into() method
            DrawableRequestBuilder<String> thumbnailRequest = Glide
                    .with(getActivity())
                    .load(avatar);


            Glide.with(getActivity())
                    .load(avatar)
                    .placeholder(R.drawable.dynamic_profile)
                    .error(R.drawable.dynamic_profile)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
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

        Glide.with(getActivity())
                .load(file)
                .fitCenter()
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(profilePic);


        showProgressDialog();
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("user[avatar]", file.getName(), requestFile);
        String access = "Bearer " + preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        yoService.updateProfile(userId, access, null, null, null, null, body).enqueue(new Callback<UserProfileInfo>() {
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareSettingsList();
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
        menuDataList.add(new MoreData(preferenceEndPoint.getStringPreference(Constants.USER_NAME), false));
        String phone = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        menuDataList.add(new MoreData(phone, false));
        String balance = mBalanceHelper.getCurrentBalance();
        String currencySymbol = mBalanceHelper.getCurrencySymbol();
        menuDataList.add(new MoreData(String.format("Yo Credit (%s%s)", currencySymbol, balance), true));
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

        switch (requestCode) {

            case Constants.ADD_IMAGE_CAPTURE:
                try {
                    String imagePath = cameraIntent.mFileTemp.getPath();
                    uploadFile(new File(imagePath));

                } catch (Exception e) {
                    mLog.w("MoreFragment", e);
                }
                break;

            case Constants.ADD_SELECT_PICTURE: {
                if (data != null) {
                    try {
                        String imagePath = ImagePickHelper.getGalleryImagePath(getActivity(), data);
                        uploadFile(new File(imagePath));
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
            menuAdapter.getItem(2).setName(String.format("Yo Credit (%s%s)", currencySymbol, balance));
            menuAdapter.notifyDataSetChanged();
        } else if (key.equals(Constants.USER_NAME)) {
            String name = preferenceEndPoint.getStringPreference(Constants.USER_NAME);
            menuAdapter.getItem(0).setName(name);
            menuAdapter.notifyDataSetChanged();
        }
    }
}
