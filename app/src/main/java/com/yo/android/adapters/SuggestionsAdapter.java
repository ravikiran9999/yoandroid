package com.yo.android.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.di.Injector;
import com.yo.android.helpers.SuggestionsViewHolder;
import com.yo.android.model.Topics;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by creatives on 9/6/2016.
 */
public class SuggestionsAdapter extends AbstractBaseAdapter<Topics, SuggestionsViewHolder> {

    private Context context;
    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    public SuggestionsAdapter(Context context) {
        super(context);
        this.context = context;
        Injector.obtain(context.getApplicationContext()).inject(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.suggestions_list_item;
    }

    @Override
    public SuggestionsViewHolder getViewHolder(View convertView) {
        return new SuggestionsViewHolder(convertView);
    }

    @Override
    public void bindView(final int position, final SuggestionsViewHolder holder, final Topics item) {

        holder.getBtnTopics().setText(item.getName());
        holder.getBtnFollow().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accessToken = preferenceEndPoint.getStringPreference("access_token");
                final List<String> followedTopicsIdsList = new ArrayList<String>();
                if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("magazine_tags"))) {
                    String[] prefTags = TextUtils.split(preferenceEndPoint.getStringPreference("magazine_tags"), ",");
                    for(int i=0; i<prefTags.length; i++) {
                        followedTopicsIdsList.add(prefTags[i]);
                    }
                }
                followedTopicsIdsList.add(String.valueOf(item.getId()));
                yoService.addTopicsAPI(accessToken, followedTopicsIdsList).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        holder.getBtnFollow().setText("Following");
                        holder.getBtnFollow().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                        item.setSelected(true);

                        preferenceEndPoint.saveStringPreference("magazine_tags", TextUtils.join(",", followedTopicsIdsList));
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(context, "Error while adding topics", Toast.LENGTH_LONG).show();
                        holder.getBtnFollow().setText("Follow");
                        holder.getBtnFollow().setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        item.setSelected(false);
                    }
                });
            }
        });

    }

}
