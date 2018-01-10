package com.yo.android.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yo.android.BuildConfig;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.ChatMessageReceived;
import com.yo.android.util.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by rajesh on 1/10/16.
 */
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
                    //listener.onDownlaoded(null);
                    //Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });
        }
    }


    /*static CustomTransformation transformation = new CustomTransformation() {
        private String fileName;
        private String folderName;

        @Override
        public void setFileName(String file) {
            this.fileName = file;
        }

        @Override
        public void setFolderName(String folderName) {
            this.folderName = folderName;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            int targetWidth = 800;
            double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
            int targetHeight = (int) (targetWidth * aspectRatio);
            Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);

            String filepath = CompressImage.getFilename(new File(fileName).getName(), folderName);
            try {
                FileOutputStream out = new FileOutputStream(filepath);
                result.compress(Bitmap.CompressFormat.JPEG, 80, out);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (result != source) {
                source.recycle();
            }

            return result;
        }

        @Override
        public String key() {
            return "transformation" + " desiredWidth";
        }
    };*/

    private static void getImageHeightAndWidth(Context context, final File file, ImageView imageView, final ProgressBar progressBar) {
        //NewImageCompress.getImage(context, file, imageView, progressBar);
        oldImageProcess(context, file, imageView, progressBar);

    }

    private static void oldImageProcess(Context context, File file, ImageView imageView, final ProgressBar progressBar) {
        float ratio = 1;
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

        Glide.with(context)
                .load(file)
                .listener(new RequestListener<File, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .priority(Priority.HIGH)
                .dontAnimate()
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);
    }
}
