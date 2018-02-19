package com.yo.android.chat;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yo.android.BuildConfig;
import com.yo.android.model.ChatMessage;
import com.yo.android.util.Constants;

import java.io.File;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ImageLoader {

    public interface ImageDownloadListener {
        void onDownlaoded(File file);
    }

    public static void updateImage(final Context context, final ChatMessage item, String folderName, final ImageView imageView1, final ProgressBar progressBar) {
        updateImage(context, item, folderName, new ImageDownloadListener() {

            @Override
            public void onDownlaoded(File file) {
                if (file != null && item != null) {
                    item.setImagePath(file.getAbsolutePath());
                    getImageHeightAndWidth(context, file, imageView1, progressBar);
                }
            }
        });
    }

    public static void updateImage(final Context mContext, final ChatMessage item, String folderName, final ImageDownloadListener listener) {
        File file = new File(item.getImagePath());
        String firebaseImagePath;
        if (file != null && !file.exists()) {
            file = new File(Environment.getExternalStorageDirectory() + "/YO/" + folderName + "/" + file.getName());
        }
        if (file.exists()) {
            listener.onDownlaoded(file);
        } else {
            if (item.getImagePath().contains("/YO/" + folderName + "/") && folderName.equalsIgnoreCase(Constants.YOIMAGES)) {
                firebaseImagePath = "images/" + file.getName();
            } else {
                firebaseImagePath = item.getImagePath();
            }
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(BuildConfig.STORAGE_BUCKET);
            final StorageReference imageRef = storageRef.child(firebaseImagePath);
            final File finalFile = file;
            imageRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    listener.onDownlaoded(finalFile);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }


    private static void getImageHeightAndWidth(Context context, final File file, ImageView imageView, final ProgressBar progressBar) {
        oldImageProcess(context, file, imageView, progressBar);

    }

    private static void oldImageProcess(Context context, File file, ImageView imageView, final ProgressBar progressBar) {
        try {
            float ratio = 1;
            progressBar.setVisibility(View.VISIBLE);
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            int maxWidth = display.getWidth() * 2 / 3;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            int height = options.outHeight;
            int width = options.outWidth;
            ratio = (float) width / maxWidth;

            if (height > width) {
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                height = display.getWidth() * 2 / 3;
            } else {
                height = (int) (height / ratio);
            }

            width = maxWidth;

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
            layoutParams.setMargins(5, 5, 5, 5);
            imageView.setLayoutParams(layoutParams);
            RequestOptions requestOptions = new RequestOptions()
                    .priority(Priority.HIGH)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.NONE);
            Glide.with(context)
                    .load(file)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .apply(requestOptions)
                    .transition(withCrossFade())
                    .into(imageView);
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
