package com.yo.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.orion.android.common.util.ConnectivityHelper;
import com.orion.android.common.util.ToastFactory;
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

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by root on 19/7/16.
 */
public class UpdateProfileActivity extends BaseActivity {
    private static final String USER_NAME_REGX = "^[a-zA-Z0-9-_ ]*$";
    private EditText username;
    private TextView mobileNum;
    private TextView addPhoto;
    private ImageView profileImage;


    private Button nextBtn;
    @Inject
    ToastFactory toastFactory;
    @Inject
    ImagePickHelper cameraIntent;
    private String mobileNumberTxt;
    File imgFile;
    @Inject
    ConnectivityHelper mHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_screen);

        setupToolbar();
        initializeViews();
        cameraIntent.setActivity(this);

        mobileNumberTxt = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        mobileNum.setText(mobileNumberTxt);


        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UpdateProfileActivity.this != null) {
                    Util.hideKeyboard(UpdateProfileActivity.this, getCurrentFocus());
                }
                cameraIntent.showDialog();
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UpdateProfileActivity.this != null) {
                    Util.hideKeyboard(UpdateProfileActivity.this, getCurrentFocus());
                }
                cameraIntent.showDialog();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performActionNext();

            }
        });

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


    private void initializeViews() {
        username = (EditText) findViewById(R.id.user_name);
        mobileNum = (TextView) findViewById(R.id.mobile_number);
        addPhoto = (TextView) findViewById(R.id.add_photo);
        profileImage = (ImageView) findViewById(R.id.profile_pic);
        nextBtn = (Button) findViewById(R.id.next_btn);
    }

    private void setupToolbar() {
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        String title = getResources().getString(R.string.profile);
        getSupportActionBar().setTitle(title);
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
                        Glide.with(this).load(imgFile)
                                .dontAnimate()
                                .placeholder(R.drawable.dynamic_profile)
                                .error(R.drawable.dynamic_profile)
                                .fitCenter()
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
                            if(this != null) {
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
                    Util.saveUserDetails(response, preferenceEndPoint);
                    preferenceEndPoint.saveStringPreference(Constants.USER_ID, response.body().getId());
                    preferenceEndPoint.saveStringPreference(Constants.USER_AVATAR, response.body().getAvatar());
                    preferenceEndPoint.saveStringPreference(Constants.USER_STATUS, response.body().getDescription());
                    preferenceEndPoint.saveStringPreference(Constants.USER_NAME, response.body().getFirstName());
                    preferenceEndPoint.saveStringPreference(Constants.FIREBASE_USER_ID, response.body().getFirebaseUserId());
                    preferenceEndPoint.saveBooleanPreference(Constants.USER_TYPE, response.body().isRepresentative());
                    uploadFile(imgFile);
                } else {
                    toastFactory.showToast(getResources().getString(R.string.unable_to_fetch));
                }
            }

            @Override
            public void onFailure(Call<UserProfileInfo> call, Throwable t) {

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
                    //TODO:Disable flag for Profile
                    //TODO:Enable flag for Follow more
                    preferenceEndPoint.saveBooleanPreference(Constants.ENABLE_PROFILE_SCREEN, false);
                    preferenceEndPoint.saveBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN, true);
                    preferenceEndPoint.saveBooleanPreference(Constants.LOGED_IN, true);
                    preferenceEndPoint.saveBooleanPreference(Constants.LOGED_IN_AND_VERIFIED, true);
                    preferenceEndPoint.saveStringPreference(Constants.USER_NAME, response.body().getFirstName());
                    Util.saveUserDetails(response, preferenceEndPoint);
                    Intent intent = new Intent(UpdateProfileActivity.this, FollowMoreTopicsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("From", "UpdateProfileActivity");
                    startActivity(intent);
                    UpdateProfileActivity.this.finish();
                } else {
                    if (response.code() == 422) {
                        toastFactory.showToast(getResources().getString(R.string.invalid_username));
                    } else {
                        toastFactory.showToast(getResources().getString(R.string.profile_failed));
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfileInfo> call, Throwable t) {
                dismissProgressDialog();
                toastFactory.showToast(getResources().getString(R.string.profile_failed));
            }
        });
    }
}
