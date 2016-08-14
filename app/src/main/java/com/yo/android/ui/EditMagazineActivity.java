package com.yo.android.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
        etTitle.requestFocus();
        Intent intent = getIntent();
        final String magazineTitle = intent.getStringExtra("MagazineTitle");
        magazineId = intent.getStringExtra("MagazineId");
        final String magazineDesc = intent.getStringExtra("MagazineDesc");
        magazinePrivacy = intent.getStringExtra("MagazinePrivacy");

        String title = "Edit Magazine";

        getSupportActionBar().setTitle(title);

        etTitle.setText(magazineTitle);
        etDesc.setText(magazineDesc);

        tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAlert(magazineTitle);
            }
        });


    }

    private void deleteMagazine(final String magazineTitle) {
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
               // do nothing
            }
        });
    }

    private void showDeleteAlert(final String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.delete_topic_message));
        builder.setCancelable(false);

        builder.setPositiveButton(
                getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        dialog.dismiss();
                        deleteMagazine(title);
                    }
                });

        builder.setNegativeButton(
                getResources().getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

                final String title = etTitle.getText().toString();
                final String description = etDesc.getText().toString();

                if (TextUtils.isEmpty(title.trim())) {
                    mToastFactory.showToast("Please enter the Magazine Title");

                } else {
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.updateMagazinesAPI(magazineId, accessToken, title, description, magazinePrivacy).enqueue(new Callback<UpdateMagazine>() {
                        @Override
                        public void onResponse(Call<UpdateMagazine> call, Response<UpdateMagazine> response) {
                            Intent intent = new Intent();
                            intent.putExtra("EditedTitle", title);
                            intent.putExtra("EditedDesc", description);
                            setResult(RESULT_OK, intent);
                            EventBus.getDefault().post(Constants.EDIT_MAGAZINE_ACTION);
                            finish();
                        }

                        @Override
                        public void onFailure(Call<UpdateMagazine> call, Throwable t) {
                           // do nothing
                        }
                    });
                }

                break;
            default:
                break;

        }
        return true;
    }
}
