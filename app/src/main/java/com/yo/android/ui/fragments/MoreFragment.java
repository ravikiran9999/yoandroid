package com.yo.android.ui.fragments;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.adapters.MoreListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.MoreData;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.util.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoreFragment extends BaseFragment implements AdapterView.OnItemClickListener {


    private MoreListAdapter menuAdapter;
    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;
    @Inject
    YoApi.YoService yoService;
    FrameLayout changePhoto;
    ImageView profilePic;

    private File mFileTemp;
    private static String TEMP_PHOTO_FILE_NAME;
    private static final int ADD_IMAGE_CAPTURE = 1;
    private static final int ADD_SELECT_PICTURE = 2;
    private Uri mImageCaptureUri = null;
    private String imagePath;
    AlertDialog dialog;
    private static final String[] items = {"Camera", "Gallery"};

    public MoreFragment() {
        // Required empty public constructor
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
        changePhoto = (FrameLayout) view.findViewById(R.id.change_layout);
        profilePic = (ImageView) view.findViewById(R.id.profile_pic);

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
                Toast.makeText(getActivity(), "You have selected change photo.", Toast.LENGTH_LONG).show();
            }
        });
        String avatar = preferenceEndPoint.getStringPreference(Constants.USER_AVATAR);
        if (!TextUtils.isEmpty(avatar)) {
            Picasso.with(getActivity())
                    .load(avatar)
                    .into(profilePic);
        }

    }

    //Tested and image update is working
    //Make a prompt for pick a image from gallery/camera
    private void uploadFile(File file) {
        showProgressDialog();
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        //TODO: Dynamic
        //File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ram_charan.jpg");
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("user[avatar]", file.getName(), requestFile);
        yoService.updateProfile(userId, body).enqueue(new Callback<UserProfileInfo>() {
            @Override
            public void onResponse(Call<UserProfileInfo> call, Response<UserProfileInfo> response) {
                dismissProgressDialog();
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
        String balance = preferenceEndPoint.getStringPreference(Constants.CURRENT_BALANCE, "2.0");

        List<MoreData> menuDataList = new ArrayList<>();
        menuDataList.add(new MoreData("John Doe", false));
        String phone = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        menuDataList.add(new MoreData(phone, false));
        menuDataList.add(new MoreData("Yo Credit " + "($" + balance + ")", true));
        menuDataList.add(new MoreData("Invite Friends", true));
        menuDataList.add(new MoreData("Notifications", true));
        menuDataList.add(new MoreData("Settings", true));
        menuDataList.add(new MoreData("Sign out", false));
        return menuDataList;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String name = ((MoreData) parent.getAdapter().getItem(position)).getName();

        if (name.equalsIgnoreCase("sign out")) {
            showLogoutDialog();
        } else if (name.equalsIgnoreCase("Invite Friends")) {

            startActivity(new Intent(getActivity(), InviteActivity.class));

        } else {
            Toast.makeText(getActivity(), "You have clicked on " + name, Toast.LENGTH_LONG).show();
        }
    }

    public void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Sign out");
        builder.setMessage("Are you sure you want to sign out ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                preferenceEndPoint.clearAll();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();

            }
        });
        builder.setNegativeButton("No", null);
        builder.create().show();
    }

    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        dialog.dismiss();
        try {

            String state = Environment.getExternalStorageState();
            TEMP_PHOTO_FILE_NAME = "" + System.currentTimeMillis() + ".jpg";
            if (Environment.MEDIA_MOUNTED.equals(state)) {

                mFileTemp = new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        + "/Camera", TEMP_PHOTO_FILE_NAME);
            } else {
                mFileTemp = new File(getActivity().getFilesDir(), TEMP_PHOTO_FILE_NAME);
            }
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mImageCaptureUri = Uri.fromFile(mFileTemp);
            } else {

                //mImageCaptureUri = InternalStorageContentProvider.CONTENT_URI;
            }
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, ADD_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
        }
    }

    // open gallery
    public void getImageFromGallery() {
        dialog.dismiss();
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, ADD_SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case ADD_IMAGE_CAPTURE:
                try {
                    imagePath = mFileTemp.getPath();
                    uploadFile(new File(imagePath));

                } catch (Exception e) {
                }
                break;

            case ADD_SELECT_PICTURE: {
                if (data != null) {
                    Uri targetUri = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    try {
                        Cursor cursor = getActivity().getContentResolver().query(targetUri,
                                filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndexOrThrow(filePathColumn[0]);
                        imagePath = cursor.getString(columnIndex);

                        uploadFile(new File(imagePath));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            default:
                break;
        }
    }

    private void showDialog() {

        dialog = new AlertDialog.Builder(getActivity())
                .setSingleChoiceItems(items, 2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (items[which].toString().equalsIgnoreCase("Camera")) {
                            takePicture();
                            dialog.dismiss();
                        } else if (items[which].toString().equalsIgnoreCase("Gallery")) {
                            getImageFromGallery();
                        }

                    }
                })
                .show();
    }
}
