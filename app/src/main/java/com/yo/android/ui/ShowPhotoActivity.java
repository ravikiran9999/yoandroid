package com.yo.android.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
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
        final Callback loadedCallback = new Callback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                progressBar.setVisibility(View.GONE);
            }
        };
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                exception.printStackTrace();
            }
        });
        if (imageUrl.contains("storage")) {
            builder.build()
                    .load(new File(imageUrl))
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                            float density = displayMetrics.density;
                            int width = displayMetrics.widthPixels;
                            int height = displayMetrics.heightPixels;
                            imageView.getLayoutParams().width = width > Math.round(bitmap.getWidth() * density) ? Math.round(bitmap.getWidth() * density) : width;
                            imageView.getLayoutParams().height = height > Math.round(bitmap.getHeight() * density) ? Math.round(bitmap.getHeight() * density) : height;
                            imageView.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
        } else {
            builder.build()
                    .load(imageUrl)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                            float density = displayMetrics.density;
                            int width = displayMetrics.widthPixels;
                            int height = displayMetrics.heightPixels;
                            imageView.getLayoutParams().width = width > Math.round(bitmap.getWidth() * density) ? Math.round(bitmap.getWidth() * density) : width;
                            imageView.getLayoutParams().height = height > Math.round(bitmap.getHeight() * density) ? Math.round(bitmap.getHeight() * density) : height;
                            imageView.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
        }
        imageOpen.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
