package com.yo.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.yo.android.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class EditGroupSubjectActivity extends Activity {

    public static final String GROUP_SUBJECT = "group_subject";

    @Bind(R.id.edit_profile)
    EditText groupSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group_subject);
        ButterKnife.bind(this);

        groupSubject.setText(getIntent().getStringExtra(GROUP_SUBJECT));
    }

    @OnClick(R.id.cancel_edit)
    public void cancel() {
        finish();
    }

    @OnClick(R.id.ok_edit)
    public void okay() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(GROUP_SUBJECT,groupSubject.getText().toString());
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
}
