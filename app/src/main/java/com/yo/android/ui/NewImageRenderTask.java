package com.yo.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yo.android.R;
import com.yo.android.model.Articles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by root on 1/11/17.
 */

public  class NewImageRenderTask extends AsyncTask<Void, Void, Bitmap> {
    private String imageLink;
    private ImageView articleImageView;
    private Context mContext;

    public NewImageRenderTask(Context context, String url, ImageView photoView) {
        this.imageLink = url;
        this.articleImageView = photoView;
        this.mContext = context;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        Bitmap urlBitmap = getBitmapFromURL(imageLink);
        return urlBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap urlBitmap) {
        super.onPostExecute(urlBitmap);
        int screenWidth = DeviceDimensionsHelper.getDisplayWidth(mContext);
        int screenHeight = DeviceDimensionsHelper.getDisplayHeight(mContext);
        if (urlBitmap != null) {
            Bitmap bmp = BitmapScaler.scaleToFitWidth(urlBitmap, screenWidth);
            //BitmapCache.getInstance(mContext).addBitmapToMemoryCache(imageLink,bmp);
            //articleImageView.setImageBitmap(bmp);
            //byte[] byteArray = bitmapToByte(bmp);
            Drawable drawable = new BitmapDrawable(mContext.getResources(), bmp);
            if (!((BaseActivity) mContext).hasDestroyed()) {
                /*Glide.with(mContext)
                        .load(byteArray)
                        .override(screenWidth, screenHeight)
                        .placeholder(R.drawable.img_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(articleImageView);*/
/*                Glide.with(mContext)
                        .load("")
                        .placeholder(drawable)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(articleImageView);*/
                Glide.with(mContext)
                        .load(imageLink)
                        .override(bmp.getWidth(), bmp.getHeight())
                        .placeholder(R.drawable.img_placeholder)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .dontAnimate()
                        .into(articleImageView);
            }

        } else {
            articleImageView.setImageResource(R.drawable.img_placeholder);
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
}
