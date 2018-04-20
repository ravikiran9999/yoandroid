package com.yo.android.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.orion.android.common.util.ConnectivityHelper;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.helpers.Helper;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.ui.uploadphoto.ImagePickHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by root on 19/7/16.
 */
public class UpdateProfileActivity extends BaseActivity {
    private static final String USER_NAME_REGX = "^[a-zA-Z0-9-_ ]*$";
    private static final String TAG = UpdateProfileActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 101;

    @Bind(R.id.profile_layout)
    View mLayout;
    @Bind(R.id.user_name)
    EditText username;
    @Bind(R.id.mobile_number)
    TextView mobileNum;
    @Bind(R.id.add_photo)
    TextView addPhoto;
    @Bind(R.id.profile_pic)
    ImageView profileImage;
    @Bind(R.id.next_btn)
    Button nextBtn;

    @Inject
    ToastFactory toastFactory;
    @Inject
    ImagePickHelper cameraIntent;
    @Inject
    ConnectivityHelper mHelper;

    File imgFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_screen);
        ButterKnife.bind(this);

        setupToolbar();
        updateDeviceToken();
        cameraIntent.setActivity(this);

        String mobileNumberTxt = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        mobileNum.setText(mobileNumberTxt);

        username.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //do here
                    performActionNext();
                    return true;
                }
                return false;
            }
        });
    }

    @OnClick(R.id.add_photo)
    public void addPhoto() {
        Util.hideKeyboard(UpdateProfileActivity.this, getCurrentFocus());
        checkForPermissions();
    }

    @OnClick(R.id.profile_pic)
    public void addProfilePic() {
        Util.hideKeyboard(UpdateProfileActivity.this, getCurrentFocus());
        checkForPermissions();
    }

    @OnClick(R.id.next_btn)
    public void next() {
        performActionNext();
    }

    private void performActionNext() {
        if (!TextUtils.isEmpty(username.getText().toString().trim())) {
            if (Pattern.matches(USER_NAME_REGX, username.getText().toString())) {
                loadUserProfileInfo();
            } else {
                toastFactory.showToast(getResources().getString(R.string.invalid_username));
            }
        } else {
            Util.hideKeyboard(this, getCurrentFocus());
            toastFactory.showToast(getResources().getString(R.string.enter_username));
        }
    }


    private void setupToolbar() {
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setTitleHideIcon(R.string.profile);
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
                        imgFile = new File(imagePath.getPath());
                        RequestOptions requestOptions = new RequestOptions()
                                .dontAnimate()
                                .placeholder(R.drawable.dynamic_profile)
                                .error(R.drawable.dynamic_profile)
                                .fitCenter();
                        Glide.with(this).load(imgFile)
                                .apply(requestOptions)
                                .into(profileImage);
                        addPhoto.setText(getResources().getString(R.string.change_picture));
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
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        if (imagePath != null) {
                            if (this != null) {
                                Helper.setSelectedImage(this, imagePath, true, bitmap, true);
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
                        String imagePath = ImagePickHelper.getGalleryImagePath(this, data);
                        Helper.setSelectedImage(this, imagePath, true, null, false);
                    } catch (Exception e) {
                        mLog.w("MoreFragment", e);
                    }
                }
            }

            default:
                break;
        }
    }

    private void loadUserProfileInfo() {
        if (!mHelper.isConnected()) {
            mToastFactory.showToast(getResources().getString(R.string.connectivity_network_settings));
            return;
        }
        showProgressDialog();
        String access = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        yoService.getUserInfo(access).enqueue(new Callback<UserProfileInfo>() {
            @Override
            public void onResponse(Call<UserProfileInfo> call, Response<UserProfileInfo> response) {
                if (response.body() != null) {
                    try {
                        Util.saveUserDetails(response, preferenceEndPoint);
                        preferenceEndPoint.saveStringPreference(Constants.USER_ID, response.body().getId());
                        preferenceEndPoint.saveStringPreference(Constants.USER_AVATAR, response.body().getAvatar());
                        preferenceEndPoint.saveStringPreference(Constants.USER_STATUS, response.body().getDescription());
                        preferenceEndPoint.saveStringPreference(Constants.USER_NAME, response.body().getFirstName());
                        preferenceEndPoint.saveStringPreference(Constants.FIREBASE_USER_ID, response.body().getFirebaseUserId());
                        preferenceEndPoint.saveBooleanPreference(Constants.USER_TYPE, response.body().isRepresentative());
                        uploadFile(imgFile);
                    } finally {
                        if (response != null && response.body() != null) {
                            try {
                                response = null;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    dismissProgressDialog();
                    toastFactory.showToast(getResources().getString(R.string.unable_to_fetch));
                }
            }

            @Override
            public void onFailure(Call<UserProfileInfo> call, Throwable t) {
                dismissProgressDialog();
            }
        });
    }

    private void uploadFile(File file) {
        if (!mHelper.isConnected()) {
            mToastFactory.showToast(getResources().getString(R.string.connectivity_network_settings));
            return;
        }
        String descriptionString = username.getText().toString();
        if (TextUtils.isEmpty(descriptionString)) {
            return;
        }
        showProgressDialog();
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        MultipartBody.Part body;
        if (file == null) {
            body = null;
        } else { // create RequestBody instance from file
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), file);
            // MultipartBody.Part is used to send also the actual file name
            body = MultipartBody.Part.createFormData("user[avatar]", file.getName(), requestFile);
        }


        RequestBody description =
                RequestBody.create(
                        MediaType.parse("user[first_name]"), descriptionString);
        String access = "Bearer " + preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        yoService.updateProfile(userId, access, null, description, null, null, null, null, null, null, body).enqueue(new Callback<UserProfileInfo>() {
            @Override
            public void onResponse(Call<UserProfileInfo> call, Response<UserProfileInfo> response) {
                dismissProgressDialog();
                if (response.isSuccessful()) {
                    try {
                        //TODO:Disable flag for Profile
                        //TODO:Enable flag for Follow more
                        preferenceEndPoint.saveBooleanPreference(Constants.ENABLE_PROFILE_SCREEN, false);
                        preferenceEndPoint.saveBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN, true);
                        preferenceEndPoint.saveBooleanPreference(Constants.LOGED_IN, true);
                        preferenceEndPoint.saveBooleanPreference(Constants.LOGED_IN_AND_VERIFIED, true);
                        preferenceEndPoint.saveStringPreference(Constants.USER_NAME, response.body().getFirstName());
                        Util.saveUserDetails(response, preferenceEndPoint);
                    } finally {
                        if (response != null && response.body() != null) {
                            try {
                                response = null;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Intent intent;
                    intent = new Intent(UpdateProfileActivity.this, NewFollowMoreTopicsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("From", "UpdateProfileActivity");
                    startActivity(intent);
                    UpdateProfileActivity.this.finish();
                } else {
                    try {
                        if (response.code() == 422) {
                            toastFactory.showToast(getResources().getString(R.string.invalid_username));
                        } else if (response.code() == 500) {
                            toastFactory.showToast(getResources().getString(R.string.internal_server_error));
                        } else {
                            toastFactory.showToast(getResources().getString(R.string.profile_failed));
                        }
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
            }

            @Override
            public void onFailure(Call<UserProfileInfo> call, Throwable t) {
                dismissProgressDialog();
                toastFactory.showToast(R.string.profile_failed);
            }
        });
    }

    private void updateDeviceToken() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (!TextUtils.isEmpty(refreshedToken)) {
            preferenceEndPoint.saveStringPreference(Constants.FCM_REFRESH_TOKEN, refreshedToken);
        } else {
            refreshedToken = preferenceEndPoint.getStringPreference(Constants.FCM_REFRESH_TOKEN);
        }
        if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER))) {
            String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
            yoService.updateDeviceTokenAPI(accessToken, refreshedToken).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        Log.i(TAG, "FCM token updated successfully");
                    } finally {
                        if (response != null && response.body() != null) {
                            response.body().close();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i(TAG, "FCM token failure : " + t.getMessage());
                }
            });
        }
    }

    private void checkForPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            requestPermission();
        } else {
            // Permission has already been granted
            cameraIntent.showDialog();
        }
    }

    private void requestPermission() {
        // No explanation needed; request the permission
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_CAMERA);

        //}
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // camera-related task you need to do.
                    cameraIntent.showDialog();

                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showMessageDialog();

                }
                return;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        }
    }

}

