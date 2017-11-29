package com.yo.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.helpers.NewSuggestionsViewHolder;
import com.yo.android.model.Categories;
import com.yo.android.model.Topics;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

public class NewSuggestionsAdapter extends RecyclerView.Adapter<YoViewHolder> {

    private Context context;
    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    private MagazineFlipArticlesFragment magazineFlipArticlesFragment;
    private TopicSelectionListener topicSelectionListener;
    private LayoutInflater mInflater;
    private ArrayList<Categories> mData;

    public interface TopicSelectionListener {
        void onItemSelected(Topics topics);
    }

    public NewSuggestionsAdapter(Context context, MagazineFlipArticlesFragment magazineFlipArticlesFragment, ArrayList<Categories> data) {
        this.context = context;
        this.magazineFlipArticlesFragment = magazineFlipArticlesFragment;
        mInflater = LayoutInflater.from(context);
        mData = data;
    }

    @Override
    public YoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NewSuggestionsViewHolder(context, mInflater.inflate(R.layout.new_suggestions_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(YoViewHolder holder, int position) {
        final NewSuggestionsViewHolder subCategoryItemViewHolder = (NewSuggestionsViewHolder) holder;
        subCategoryItemViewHolder.bindData(mData.get(position));
        subCategoryItemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = subCategoryItemViewHolder.getAdapterPosition();
                if (position < 0) {
                    return;
                }
                if (topicSelectionListener != null) {
                    Topics item = mData.get(position).getTags().get(0);
                    topicSelectionListener.onItemSelected(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public ArrayList<Categories> getmData() {
        return mData;
    }

    public void setmData(ArrayList<Categories> mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }

    public void setTopicsItemListener(TopicSelectionListener listener) {
        topicSelectionListener = listener;
    }
}
