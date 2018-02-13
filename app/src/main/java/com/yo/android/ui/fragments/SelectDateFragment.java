package com.yo.android.ui.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.orion.android.common.preferences.PreferenceEndPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Sindhura on 11/24/2016.
 */
public class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public static final String FLAG = "flag";
    protected View view;

    protected boolean isDateOfBirth;

    public static final String stringDate = "dd-mm-yyyy";

    public SelectDateFragment() {
    }

    public void setDateFragmentView(View view) {
        this.view = view;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        this.isDateOfBirth = getArguments().getBoolean(FLAG);

        if (view instanceof EditText) {
            String dateValue = ((EditText) view).getText().toString().trim();
            if (dateValue.equalsIgnoreCase(stringDate)) {
                return getDialog(calendar, "");
            }
            return getDialog(calendar, dateValue);
        } else if (view instanceof TextView) {
            String dateValue = ((TextView) view).getText().toString().trim();
            if (dateValue.equalsIgnoreCase(stringDate)) {
                return getDialog(calendar, "");
            }
            return getDialog(calendar, dateValue);
        }
        return null;

    }

    @NonNull
    private Dialog getDialog(Calendar calendar, String dateValue) {
        if (!TextUtils.isEmpty(dateValue)) {
            try {
                String[] parts = dateValue.split("-");
                int yy = Integer.parseInt(parts[2]);
                int mm = Integer.parseInt(parts[1]);
                int dd = Integer.parseInt(parts[0]);
                return new DatePickerDialog(getActivity(), this, yy, mm - 1, dd);
            } catch (Exception e) {
                e.printStackTrace();
                int yy = calendar.get(Calendar.YEAR);
                int mm = calendar.get(Calendar.MONTH);
                int dd = calendar.get(Calendar.DAY_OF_MONTH);
                return new DatePickerDialog(getActivity(), this, yy, mm - 1, dd);
            }
        } else {
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, yy, mm, dd);
        }
    }

    public void onDateSet(DatePicker view, int yy, int mm, int dd) {
        if (this.view instanceof EditText) {
            Date strDate = null;
            if (isDateOfBirth) {
                checkIsFromDateOfBirth(yy, mm + 1, dd, strDate);
            } else {
                ((EditText) this.view).setText(dd + "-" + (mm + 1) + "-" + yy);
            }
        }

    }

    private void checkIsFromDateOfBirth(int yy, int mm, int dd, Date strDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            strDate = sdf.parse(dd + "/" + mm + "/" + yy);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (strDate != null) {
            if (new Date().compareTo(strDate) > 0) {
                ((EditText) this.view).setText(dd + "-" + mm + "-" + yy);
            } else {
                Toast.makeText(getActivity(), "Date should not be more than current date", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
