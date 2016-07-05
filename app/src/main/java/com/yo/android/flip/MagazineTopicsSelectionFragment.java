package com.yo.android.flip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.yo.android.R;
import com.yo.android.adapters.TopicsSpinnerAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.Articles;
import com.yo.android.model.Topics;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by creatives on 6/30/2016.
 */
public class MagazineTopicsSelectionFragment extends BaseFragment implements AdapterView.OnItemSelectedListener {

    private Spinner topicsSpinner;
    private String selectedTopic;
    @Inject
    YoApi.YoService yoService;
    private List<Topics> topicsList;

    public MagazineTopicsSelectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.magazine_landing, container, false);

        topicsSpinner = (Spinner) view.findViewById(R.id.sp_topics);
        topicsList = new ArrayList<Topics>();

        topicsSpinner.setOnItemSelectedListener(this);

        /*ArrayAdapter<Topics> topicsArrayAdapter = new ArrayAdapter<Topics>(getActivity(), android.R.layout.simple_spinner_item, topicsList);
        topicsSpinner.setAdapter(topicsArrayAdapter);

        topicsSpinner.setOnItemSelectedListener(this);
        //selectedTopic = "Top Stories";
        if(topicsList.size() >0) {
            selectedTopic = topicsList.get(0).getName();
        }*/

        String accessToken=preferenceEndPoint.getStringPreference("access_token");
        if(accessToken==null){
            accessToken="1eb510a50d86f49784741ba6abda3a888cda9fe592cea6a21dbefd1b64c3878e";
        }
        yoService.tagsAPI(accessToken).enqueue(new Callback<List<Topics>>() {
            @Override
            public void onResponse(Call<List<Topics>> call, Response<List<Topics>> response) {
                for(int i=0; i<response.body().size(); i ++) {
                    topicsList.add(response.body().get(i));
                }

                //ArrayAdapter<Topics> topicsArrayAdapter = new ArrayAdapter<Topics>(getActivity(), android.R.layout.simple_spinner_item, topicsList);
                TopicsSpinnerAdapter topicsArrayAdapter = new TopicsSpinnerAdapter (getActivity(), android.R.layout.simple_spinner_item, topicsList);
                topicsSpinner.setAdapter(topicsArrayAdapter);

                //selectedTopic = "Top Stories";
                if(topicsList.size() >0) {
                    selectedTopic = topicsList.get(0).getName();
                }
            }

            @Override
            public void onFailure(Call<List<Topics>> call, Throwable t) {

            }
        });

       /* yoService.getArticlesAPI("1eb510a50d86f49784741ba6abda3a888cda9fe592cea6a21dbefd1b64c3878e", "577a48b92434bc000356501a").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });*/

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        /*Toast.makeText(parent.getContext(),
                "Selected topic : " + parent.getItemAtPosition(position).toString(),
                Toast.LENGTH_SHORT).show();*/
        //selectedTopic = parent.getItemAtPosition(position).toString();
        selectedTopic = topicsList.get(position).getName();
        Intent intent = new Intent();
        intent.setAction("com.yo.magazine.SendBroadcast");
        intent.putExtra("SelectedTopic", selectedTopic);
        String topicId = topicsList.get(position).getId();
        intent.putExtra("TopicId", topicId);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public String getSelectedTopic() {
        if (selectedTopic == null) {
            selectedTopic = "";
        }
        return selectedTopic;
    }

    public void setSelectedTopic(String selectedTopic) {
        this.selectedTopic = selectedTopic;
    }
}
