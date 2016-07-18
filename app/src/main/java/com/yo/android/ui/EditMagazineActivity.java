package com.yo.android.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.yo.android.model.UpdateMagazine;
import com.yo.android.util.Constants;

import org.w3c.dom.Text;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditMagazineActivity extends BaseActivity {

    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private String magazineId;
    private String magazinePrivacy;
    private EditText etTitle;
    private EditText etDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_magazine);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etTitle = (EditText) findViewById(R.id.et_title);
        etDesc = (EditText) findViewById(R.id.et_desc);
        TextView tvDelete = (TextView) findViewById(R.id.tv_delete);

        Intent intent = getIntent();
        final String magazineTitle = intent.getStringExtra("MagazineTitle");
        magazineId = intent.getStringExtra("MagazineId");
        final String magazineDesc = intent.getStringExtra("MagazineDesc");
        magazinePrivacy = intent.getStringExtra("MagazinePrivacy");

        String title = "Edit Magazine";

        getSupportActionBar().setTitle(title);

        tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accessToken = preferenceEndPoint.getStringPreference("access_token");
                yoService.deleteMagazineAPI(magazineId, accessToken).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        mToastFactory.showToast("Magazine " + magazineTitle + "  deleted successfully");
                        EventBus.getDefault().post(Constants.DELETE_MAGAZINE_ACTION);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });

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

                final String title = etTitle.getText().toString();
                final String description = etDesc.getText().toString();

                if(TextUtils.isEmpty(title.trim()) && TextUtils.isEmpty(description.trim())) {
                    mToastFactory.showToast("Please enter the Magazine Title/Description");

                }
                else {
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.updateMagazinesAPI(magazineId, accessToken, title, description, magazinePrivacy).enqueue(new Callback<UpdateMagazine>() {
                        @Override
                        public void onResponse(Call<UpdateMagazine> call, Response<UpdateMagazine> response) {
                            Intent intent = new Intent();
                            intent.putExtra("EditedTitle", title);
                            intent.putExtra("EditedDesc", description);
                            setResult(RESULT_OK, intent);
                            finish();
                        }

                        @Override
                        public void onFailure(Call<UpdateMagazine> call, Throwable t) {

                        }
                    });
                }

                break;


        }
        return true;
    }
}
