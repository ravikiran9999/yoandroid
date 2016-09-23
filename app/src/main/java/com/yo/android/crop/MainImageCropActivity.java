package com.yo.android.crop;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import com.yo.android.R;
import com.yo.android.helpers.Helper;
import com.yo.android.ui.BaseActivity;


public class MainImageCropActivity extends BaseActivity {
    private static final String TAG = MainImageCropActivity.class.getSimpleName();
    private String selectedImageItem;

    // Lifecycle Method ////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_image_crop);
        getSupportActionBar().setTitle(getResources().getString(R.string.crop_image));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Helper.GALLERY_IMAGE_ITEM)) {
            selectedImageItem = intent.getStringExtra(Helper.GALLERY_IMAGE_ITEM);
        }

        if (savedInstanceState == null) {
            MainFragment instance = MainFragment.getInstance();
            Bundle bundle = new Bundle();
            bundle.putString(Helper.GALLERY_IMAGE_ITEM, selectedImageItem);
            instance.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().add(R.id.container, instance).commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void startResultActivity(Uri uri) {
        if (isFinishing()) return;

        Intent intent = new Intent();
        intent.putExtra(Helper.IMAGE_PATH, uri.getPath());
        setResult(RESULT_OK, intent);
        finish();
        // Start ResultActivity
        //startActivity(ResultActivity.createIntent(this, uri));
    }

    /*
 * Retrieves the path to the selected image from the Gallery app.
 */
    public static String getGalleryImagePath(Intent data, Activity activity) {
        Uri imgUri = data.getData();
        String filePath = "";
        if (data.getType() == null) {
            // For getting images from gallery.
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = activity.getContentResolver().query(imgUri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return filePath;
    }
}
