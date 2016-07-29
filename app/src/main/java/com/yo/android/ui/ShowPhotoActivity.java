package com.yo.android.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.util.Constants;
import com.yo.android.util.FirebaseConfig;

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
        getSupportActionBar().setTitle("Image");
        enableBack();
        mAttacher = new PhotoViewAttacher(imageOpen);
        imageOpen.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
        if (getIntent() != null && getIntent().hasExtra(Constants.IMAGE)) {
            String imagePath = getIntent().getStringExtra(Constants.IMAGE);
            prepareImage(imagePath, imageOpen);
        }


    }

    public void prepareImage(String imagePath, final ImageView imageOpen) {
        try {
            // Create a storage reference from our app
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(FirebaseConfig.STORAGE_BUCKET);
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


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
