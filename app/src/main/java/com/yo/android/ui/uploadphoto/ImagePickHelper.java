package com.yo.android.ui.uploadphoto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.yo.android.util.Constants;

import java.io.File;

public class ImagePickHelper {

    private static Activity cameraActivity = null;
    AlertDialog dialog;
    public File mFileTemp;
    private static String TEMP_PHOTO_FILE_NAME;
    private Uri mImageCaptureUri = null;
    private static final String[] items = {"Camera", "Gallery"};


    public ImagePickHelper() {

    }

    public static ImagePickHelper setActivity(Activity activity) {
        if (activity == null) {
            throw new NullPointerException("Activity context should not be null");
        } else {
            cameraActivity = activity;
            return new ImagePickHelper();
        }
    }

    public void showDialog() {

        dialog = new AlertDialog.Builder(cameraActivity)
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
                mFileTemp = new File(cameraActivity.getFilesDir(), TEMP_PHOTO_FILE_NAME);
            }
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mImageCaptureUri = Uri.fromFile(mFileTemp);
            } else {

                //mImageCaptureUri = InternalStorageContentProvider.CONTENT_URI;
            }
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            cameraActivity.startActivityForResult(intent, Constants.ADD_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
        }
    }

    // open gallery
    public void getImageFromGallery() {
        dialog.dismiss();
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        cameraActivity.startActivityForResult(intent, Constants.ADD_SELECT_PICTURE);
    }


}
