package com.yo.android.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.di.Injector;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.helpers.SuggestionsViewHolder;
import com.yo.android.model.Topics;
import com.yo.android.ui.BaseActivity;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by creatives on 9/6/2016.
 */

/**
 * This adapter is used to display the Suggestions page in the Landing screen
 */
public class SuggestionsAdapter extends AbstractBaseAdapter<Topics, SuggestionsViewHolder> {

    private Context context;
    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private MagazineFlipArticlesFragment magazineFlipArticlesFragment;

    public SuggestionsAdapter(Context context, MagazineFlipArticlesFragment magazineFlipArticlesFragment) {
        super(context);
        this.context = context;
        this.magazineFlipArticlesFragment = magazineFlipArticlesFragment;
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
                if(magazineFlipArticlesFragment.mProgress != null) {
                    magazineFlipArticlesFragment.mProgress.setVisibility(View.VISIBLE);
                }
                String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
                final List<String> followedTopicsIdsList = new ArrayList<String>();
                if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference(Constants.MAGAZINE_TAGS))) {
                    String[] prefTags = TextUtils.split(preferenceEndPoint.getStringPreference(Constants.MAGAZINE_TAGS), ",");
                    for(int i=0; i<prefTags.length; i++) {
                        followedTopicsIdsList.add(prefTags[i]);
                    }
                }
                followedTopicsIdsList.add(String.valueOf(item.getId()));
                yoService.addTopicsAPI(accessToken, followedTopicsIdsList,"").enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        holder.getBtnFollow().setText("Following");
                        holder.getBtnFollow().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                        item.setSelected(true);

                        EventBus.getDefault().post(item.getId());

                        preferenceEndPoint.saveStringPreference(Constants.MAGAZINE_TAGS, TextUtils.join(",", followedTopicsIdsList));
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if(magazineFlipArticlesFragment.mProgress != null) {
                            magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                        }
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
