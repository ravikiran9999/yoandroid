package com.yo.android.flip;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.yo.android.R;
import com.yo.android.ui.BaseActivity;

import java.io.IOException;
import java.io.InputStream;

public class MagazineArticleDetailsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magazine_article_details);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String title = intent.getStringExtra("Title");
        String detailedDesc = intent.getStringExtra("DetailedDesc");
        String image = intent.getStringExtra("Image");

        getSupportActionBar().setTitle(title);
        UI
                .<TextView>findViewById(this, R.id.tv_article_long_desc)
                .setText(detailedDesc);
        ImageView photoView = UI.findViewById(this, R.id.photo);
        // load image
        try {
            // get input stream
            InputStream ims = getAssets().open(image);
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            photoView.setImageDrawable(d);
        } catch (IOException ex) {
            return;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
