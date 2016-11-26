package com.yo.android.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yo.android.R;
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

    private PhotoViewAttacher mAttacher;

    @Bind(R.id.loadingFailed)
    protected TextView loadingFailed;

    @Bind(R.id.image_progress)
    protected ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_layout);
        ButterKnife.bind(this);
        getSupportActionBar().setTitle("Photo Preview ");
        enableBack();
        mAttacher = new PhotoViewAttacher(imageOpen);
        progressBar.setVisibility(View.VISIBLE);
        if (getIntent() != null && getIntent().hasExtra(Constants.IMAGE)) {
            String imagePath = getIntent().getStringExtra(Constants.IMAGE);
            getImageHeightAndWidth(imagePath, imageOpen);
        }
    }

    private void getImageHeightAndWidth(String imageUrl, final ImageView imageView) {
        SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                float density = displayMetrics.density;
                int width = displayMetrics.widthPixels;
                int height = displayMetrics.heightPixels;
                imageView.getLayoutParams().width = width > Math.round(bitmap.getWidth() * density) ? Math.round(bitmap.getWidth() * density) : width;
                imageView.getLayoutParams().height = height > Math.round(bitmap.getHeight() * density) ? Math.round(bitmap.getHeight() * density) : height;
                imageView.setImageBitmap(bitmap);
            }
        };
        if (imageUrl.contains("storage/")) {
            Glide.with(getApplicationContext())
                    .load(new File(imageUrl))
                    .asBitmap()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(target);
        } else {
            Glide.with(getApplicationContext())
                    .load((imageUrl))
                    .asBitmap()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(target);
        }
        imageOpen.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
