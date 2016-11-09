package com.yo.android.ui.uploadphoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.amazonaws.util.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
                imageView.setImageBitmap(result);
            }
        }
    }
}