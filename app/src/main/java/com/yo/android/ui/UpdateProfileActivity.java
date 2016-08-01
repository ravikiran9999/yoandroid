package com.yo.android.ui;

import android.content.Intent;
import android.database.Cursor;
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

import com.orion.android.common.util.ConnectivityHelper;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.ui.uploadphoto.ImageLoader;
import com.yo.android.ui.uploadphoto.ImagePickHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.io.File;

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
            loadUserProfileInfo();
        } else {
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

        switch (requestCode) {

            case Constants.ADD_IMAGE_CAPTURE:
                try {
                    String imagePath = cameraIntent.mFileTemp.getPath();
                    imgFile = new File(imagePath);
                    new ImageLoader(profileImage, imgFile, this).execute();
                    addPhoto.setText(getResources().getString(R.string.change_picture));

                } catch (Exception e) {
                }
                break;

            case Constants.ADD_SELECT_PICTURE: {
                if (data != null) {
                    try {
                        String imagePath = ImagePickHelper.getGalleryImagePath(this,data);
                        imgFile = new File(imagePath);
                        new ImageLoader(profileImage, imgFile, this).execute();
                        addPhoto.setText(getResources().getString(R.string.change_picture));
                    } catch (Exception e) {
                        e.printStackTrace();
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
                    preferenceEndPoint.saveStringPreference(Constants.USER_ID, response.body().getId());
                    preferenceEndPoint.saveStringPreference(Constants.USER_AVATAR, response.body().getAvatar());
                    preferenceEndPoint.saveStringPreference(Constants.USER_STATUS, response.body().getDescription());
                    preferenceEndPoint.saveStringPreference(Constants.USER_NAME, response.body().getFirstName());
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

        String descriptionString = username.getText().toString();
        RequestBody description =
                RequestBody.create(
                        MediaType.parse("user[first_name]"), descriptionString);
        yoService.updateProfile(userId, null, description, body).enqueue(new Callback<UserProfileInfo>() {
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
                    Intent intent = new Intent(UpdateProfileActivity.this, FollowMoreTopicsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("From", "UpdateProfileActivity");
                    startActivity(intent);
                    UpdateProfileActivity.this.finish();
                } else {
                    toastFactory.showToast("Unable to update the profile");
                }
            }

            @Override
            public void onFailure(Call<UserProfileInfo> call, Throwable t) {
                dismissProgressDialog();
                toastFactory.showToast(getResources().getString(R.string.unable_to_fetch));
            }
        });

    }

}
