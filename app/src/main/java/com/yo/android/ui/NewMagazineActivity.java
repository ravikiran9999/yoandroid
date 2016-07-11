package com.yo.android.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_magazine);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "New Magazine";

        getSupportActionBar().setTitle(title);

        final EditText etTitle = (EditText) findViewById(R.id.magazine_title);
        final EditText etDesc = (EditText) findViewById(R.id.magazine_desc);
        SwitchCompat togglePrivacy = (SwitchCompat) findViewById(R.id.privacy_toggle);
        TextView tvAddStory = (TextView) findViewById(R.id.add_story);

        magazinePrivacy = "";
        if (togglePrivacy.isChecked()) {
            magazinePrivacy = "Public";
        } else {
            magazinePrivacy = "Private";
        }
        tvAddStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String magazineTitle = etTitle.getText().toString();
                final String magazineDesc = etDesc.getText().toString();

                String accessToken = preferenceEndPoint.getStringPreference("access_token");
                yoService.createMagazinesAPI(accessToken, magazineTitle, magazineDesc, magazinePrivacy).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });


                /*Intent intent = new Intent(NewMagazineActivity.this, LoadMagazineActivity.class);
                intent.putExtra("magazineTitle", magazineTitle);
                intent.putExtra("magazineDesc", magazineDesc);
                intent.putExtra("magazinePrivacy", magazinePrivacy);
                startActivity(intent);*/
            }
        });

    }
}
