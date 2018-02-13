package com.yo.android.ui.uploadphoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;


public class ImageLoader extends AsyncTask<Void, Void, Bitmap> {

    private final File imageFile;
    private final WeakReference<ImageView> imageViewReference;
    private Context mContext;


    public ImageLoader(ImageView imageView, File file, Context context) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<>(imageView);
        imageFile = file;
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        if (imageFile.exists()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile), null, null);
                return bitmap;


            } catch (FileNotFoundException e) {
                Log.i("File not found", "FNFE" + e);
            } catch (OutOfMemoryError e) {
                Log.i("Out of Memory", "OOME" + e);
            }
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (imageViewReference != null && result != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                ExifInterface ei = null;
                try {
                    ei = new ExifInterface(imageFile.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                switch (orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:
                        imageView.setImageBitmap(rotateImage(result, 90));
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        imageView.setImageBitmap(rotateImage(result, 180));
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        imageView.setImageBitmap(rotateImage(result, 270));
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:

                    default:
                        break;
                }
            }
        }
    }


    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
}