package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.model.OwnMagazine;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This activity is used to enter a magazine title and description and select the privacy setting of it
 */
public class NewMagazineActivity extends BaseActivity implements View.OnClickListener {

    private String magazinePrivacy;
    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private EditText etTitle;
    private EditText etDesc;
    private SwitchCompat togglePrivacy;
    private boolean isSaveClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_magazine);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "New Magazine";

        getSupportActionBar().setTitle(title);

        EventBus.getDefault().register(this);

        etTitle = (EditText) findViewById(R.id.magazine_title);
        etDesc = (EditText) findViewById(R.id.magazine_desc);
        togglePrivacy = (SwitchCompat) findViewById(R.id.privacy_toggle);
        TextView tvAddStory = (TextView) findViewById(R.id.add_story);

        magazinePrivacy = "";

        tvAddStory.setOnClickListener(this);

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

        switch (item.getItemId()) {
            case R.id.menu_save:
                final String magazineTitle = etTitle.getText().toString().trim();
                final String magazineDesc = etDesc.getText().toString().trim();

                if (togglePrivacy.isChecked()) {
                    magazinePrivacy = "Public";
                } else {
                    magazinePrivacy = "Private";
                }

                if (!TextUtils.isEmpty(magazineTitle.trim())) {
                    if (!isSaveClicked) {
                        isSaveClicked = true;
                        String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
                        yoService.createMagazinesAPI(accessToken, magazineTitle, magazineDesc, magazinePrivacy).enqueue(new Callback<OwnMagazine>() {
                            @Override
                            public void onResponse(Call<OwnMagazine> call, Response<OwnMagazine> response) {
                                try {
                                    if (response.body() != null) {
                                        Intent intent = new Intent();
                                        setResult(2, intent);
                                        //finishing activity
                                        finish();
                                    } else if (response.errorBody() != null) {
                                        isSaveClicked = false;
                                        mToastFactory.showToast("Magazine Title is already taken");
                                    }
                                } finally {
                                    if(response != null && response.body() != null) {
                                        try {
                                            response = null;
                                        }catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<OwnMagazine> call, Throwable t) {
                                isSaveClicked = false;
                            }
                        });
                    }
                } else {
                    Util.hideKeyboard(this, etTitle);
                    etTitle.requestFocus();
                    mToastFactory.showToast("Please enter the Magazine Title");
                }

                break;
            default:
                break;


        }
        return true;
    }

    @Override
    public void onClick(View v) {
        final String magazineTitle = etTitle.getText().toString().trim();
        final String magazineDesc = etDesc.getText().toString().trim();

        if (togglePrivacy.isChecked()) {
            magazinePrivacy = "Public";
        } else {
            magazinePrivacy = "Private";
        }

        if (!TextUtils.isEmpty(magazineTitle.trim())) {

            Intent intent = new Intent(NewMagazineActivity.this, LoadMagazineActivity.class);
            intent.putExtra("MagazineTitle", magazineTitle);
            intent.putExtra("MagazineDesc", magazineDesc);
            intent.putExtra("MagazinePrivacy", magazinePrivacy);
            startActivityForResult(intent, Constants.ADD_STORY_ACTION);
        } else {
            Util.hideKeyboard(this, v);
            etTitle.requestFocus();
            mToastFactory.showToast("Please enter the Magazine Title");
        }
    }

    public void onEventMainThread(String action) {
        if (Constants.DELETE_MAGAZINE_ACTION.equals(action)) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ADD_STORY_ACTION && resultCode == RESULT_OK) {
            finish();
        }
    }
}
