package com.yo.android.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yo.android.R;
import com.yo.android.util.Constants;

import java.io.File;


import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * The Settings screen
 */
public class ShowPhotoActivity extends BaseActivity {

    private static final String TAG = ShowPhotoActivity.class.getSimpleName();

    @BindView(R.id.image_open)
    protected ImageView imageOpen;
    @BindView(R.id.loadingFailed)
    protected TextView loadingFailed;
    @BindView(R.id.image_progress)
    protected ProgressBar progressBar;

    private PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_layout);
        ButterKnife.bind(this);
        setTitleHideIcon(R.string.photo_preview);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        enableBack();
        mAttacher = new PhotoViewAttacher(imageOpen);
        progressBar.setVisibility(View.VISIBLE);
        if (getIntent() != null && getIntent().hasExtra(Constants.IMAGE)) {
            String imagePath = getIntent().getStringExtra(Constants.IMAGE);
            getImageHeightAndWidth(imagePath, imageOpen);
        }
        imageOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleHideyBar();
            }
        });
    }

    private void getImageHeightAndWidth(String imageUrl, final ImageView imageView) {
        SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {

                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                float density = displayMetrics.density;
                int width = displayMetrics.widthPixels;
                int height = displayMetrics.heightPixels;
                imageView.getLayoutParams().width = width > Math.round(bitmap.getWidth() * density) ? Math.round(bitmap.getWidth() * density) : width;
                imageView.getLayoutParams().height = height > Math.round(bitmap.getHeight() * density) ? Math.round(bitmap.getHeight() * density) : height;
                imageView.setImageBitmap(bitmap);
            }
        };
        RequestOptions requestOptions = new RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        if (imageUrl.contains("storage/")) {
            Glide.with(this)
                    .asBitmap()
                    .load(new File(imageUrl))
                    .apply(requestOptions)
                    .into(target);
        } else {
            Glide.with(this)
                    .asBitmap()
                    .load((imageUrl))
                    .apply(requestOptions)
                    .into(target);
        }
        imageOpen.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Detects and toggles immersive mode.
     */
    public void toggleHideyBar() {
        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i(TAG, "Turning immersive mode mode off. ");
        } else {
            Log.i(TAG, "Turning immersive mode mode on.");
        }

        // Immersive mode: Backward compatible to KitKat (API 19).
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // This sample uses the "sticky" form of immersive mode, which will let the user swipe
        // the bars back in again, but will automatically make them disappear a few seconds later.
        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }

}
