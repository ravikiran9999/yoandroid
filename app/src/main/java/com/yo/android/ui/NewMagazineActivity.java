package com.yo.android.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.model.OwnMagazine;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewMagazineActivity extends BaseActivity {

    private String magazinePrivacy;
    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private EditText etTitle;
    private EditText etDesc;
    private SwitchCompat togglePrivacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_magazine);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "New Magazine";

        getSupportActionBar().setTitle(title);

        etTitle = (EditText) findViewById(R.id.magazine_title);
        etDesc = (EditText) findViewById(R.id.magazine_desc);
        togglePrivacy = (SwitchCompat) findViewById(R.id.privacy_toggle);
        TextView tvAddStory = (TextView) findViewById(R.id.add_story);

        magazinePrivacy = "";

        tvAddStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_new_magazine, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch(item.getItemId()) {
            case R.id.menu_save:


                final String magazineTitle = etTitle.getText().toString();
                final String magazineDesc = etDesc.getText().toString();

                if (togglePrivacy.isChecked()) {
                    magazinePrivacy = "Public";
                } else {
                    magazinePrivacy = "Private";
                }

                if(!TextUtils.isEmpty(magazineTitle)) {

                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.createMagazinesAPI(accessToken, magazineTitle, magazineDesc, magazinePrivacy).enqueue(new Callback<OwnMagazine>() {
                        @Override
                        public void onResponse(Call<OwnMagazine> call, Response<OwnMagazine> response) {
                            Intent intent = new Intent();
                            setResult(2, intent);
                            finish();//finishing activity
                        }

                        @Override
                        public void onFailure(Call<OwnMagazine> call, Throwable t) {

                        }
                    });
                }
                else {
                    mToastFactory.showToast("Please enter the Magazine Title");
                }

                break;


        }
        return true;
    }
}
