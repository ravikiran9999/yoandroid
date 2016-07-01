package com.yo.android.flip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.yo.android.R;

/**
 * Created by creatives on 6/30/2016.
 */
public class MagazineTopicsSelectionFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private Spinner topicsSpinner;
    private String selectedTopic;

    public MagazineTopicsSelectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.magazine_landing, container, false);

        topicsSpinner = (Spinner) view.findViewById(R.id.sp_topics);

        topicsSpinner.setOnItemSelectedListener(this);
        selectedTopic = "Top Stories";

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(parent.getContext(),
                "Selected topic : " + parent.getItemAtPosition(position).toString(),
                Toast.LENGTH_SHORT).show();
        selectedTopic = parent.getItemAtPosition(position).toString();
        Intent intent = new Intent();
        intent.setAction("com.yo.magazine.SendBroadcast");
        intent.putExtra("SelectedTopic", selectedTopic);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public String getSelectedTopic() {
        return selectedTopic;
    }

    public void setSelectedTopic(String selectedTopic) {
        this.selectedTopic = selectedTopic;
    }
}
