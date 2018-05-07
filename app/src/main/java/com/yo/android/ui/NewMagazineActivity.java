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

import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.model.OwnMagazine;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This activity is used to enter a magazine title and description and select the privacy setting of it
 */
public class NewMagazineActivity extends BaseActivity {

    private static final String PUBLIC = "Public";
    private static final String PRIVATE = "Private";

    @Bind(R.id.magazine_title)
    EditText etTitle;
    @Bind(R.id.magazine_desc)
    EditText etDesc;
    @Bind(R.id.privacy_toggle)
    SwitchCompat togglePrivacy;

    private boolean isSaveClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_magazine);
        ButterKnife.bind(this);
        setTitleHideIcon(R.string.new_magazine);
        enableBack();

        EventBus.getDefault().register(this);

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
                save();
                break;
            default:
                break;
        }
        return true;
    }

    private String getTogglePrivacy() {
        return togglePrivacy.isChecked() ? PUBLIC : PRIVATE;
    }

    private void save() {
        final String magazineTitle = etTitle.getText().toString().trim();
        final String magazineDesc = etDesc.getText().toString().trim();

        if (!TextUtils.isEmpty(magazineTitle.trim())) {
            if (!isSaveClicked) {
                isSaveClicked = true;
                String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
                yoService.createMagazinesAPI(accessToken, magazineTitle, magazineDesc, getTogglePrivacy()).enqueue(new Callback<OwnMagazine>() {
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
                                mToastFactory.showToast(R.string.title_already_exists);
                            }
                        } finally {
                            if (response != null && response.body() != null) {
                                try {
                                    response = null;
                                } catch (Exception e) {
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
            mToastFactory.showToast(R.string.enter_magazine_total);
        }
    }

    @OnClick(R.id.add_story)
    public void onClick(View v) {
        final String magazineTitle = etTitle.getText().toString().trim();
        final String magazineDesc = etDesc.getText().toString().trim();

        if (!TextUtils.isEmpty(magazineTitle.trim())) {

            Intent intent = new Intent(NewMagazineActivity.this, LoadMagazineActivity.class);
            intent.putExtra("MagazineTitle", magazineTitle);
            intent.putExtra("MagazineDesc", magazineDesc);
            intent.putExtra("MagazinePrivacy", getTogglePrivacy());
            startActivityForResult(intent, Constants.ADD_STORY_ACTION);
        } else {
            Util.hideKeyboard(this, v);
            etTitle.requestFocus();
            mToastFactory.showToast(R.string.enter_magazine_total);
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
