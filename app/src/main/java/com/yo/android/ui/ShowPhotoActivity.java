package com.yo.android.ui;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.helpers.Helper;
import com.yo.android.util.Constants;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * The Settings screen
 */
public class ShowPhotoActivity extends BaseActivity {

    @Bind(R.id.image_open)
    protected ImageView imageOpen;

    @Bind(R.id.image_progress)
    protected ProgressBar mProgress;

    private PhotoViewAttacher mAttacher;

    @Bind(R.id.loadingFailed)
    protected TextView loadingFailed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_layout);
        ButterKnife.bind(this);
        getSupportActionBar().setTitle("Photo Preview ");
        enableBack();
        mAttacher = new PhotoViewAttacher(imageOpen);
        imageOpen.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
        if (getIntent() != null && getIntent().hasExtra(Constants.IMAGE)) {
            String imagePath = getIntent().getStringExtra(Constants.IMAGE);
            prepareImage(imagePath, imageOpen);
        }


    }
    private void getImageHeightAndWidth(final File file, ImageView imageView) {
        int maxWidth = 800;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        int height = options.outHeight;
        int width = options.outWidth;
        float ratio = (float) width / maxWidth;
        width = maxWidth;
        height = (int) (height / ratio);
        Glide.with(this)
                .load(file)
                .override(width, height)
                .dontAnimate()
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }
    public void prepareImage(String imagePath, final ImageView imageOpen) {
        /*File file = new File(imagePath);
        File newFile = new File(Environment.getExternalStorageDirectory() + "/YO/YOImages/" + file.getName());
        if (newFile.exists()) {
            Glide.with(this)
                    .load(file)
                    .dontAnimate()
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageOpen);
            imageOpen.setVisibility(View.VISIBLE);
        } else {*/
            try {
                // Create a storage reference from our app
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl(BuildConfig.STORAGE_BUCKET);
                StorageReference imageRef = storageRef.child(imagePath);
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(ShowPhotoActivity.this)
                                .load(uri)
                                .into(imageOpen, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        imageOpen.setVisibility(View.VISIBLE);
                                        mAttacher.update();
                                    }

                                    @Override
                                    public void onError() {
                                        // Handle any errors
                                        if (mProgress != null) {
                                            mProgress.setVisibility(View.GONE);
                                        }
                                        if (loadingFailed != null) {
                                            loadingFailed.setVisibility(View.VISIBLE);
                                        }

                                    }
                                });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        //}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
