package com.yo.android.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.helpers.SpendDetailsViewHolder;
import com.yo.android.model.SpendData;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Ramesh on 25/7/16.
 */
public class SpendDetailsFragment extends BaseFragment {

    @Bind(R.id.txtEmpty)
    TextView txtEmpty;

    @Bind(R.id.progress)
    ProgressBar progress;

    @Bind(R.id.listView)
    ListView listView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_spend_details, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final SpentDetailsAdapter adapter = new SpentDetailsAdapter(getActivity());
        listView.setAdapter(adapter);
    }

    public static class SpentDetailsAdapter extends AbstractBaseAdapter<SpendData, SpendDetailsViewHolder> {

        public SpentDetailsAdapter(Context context) {
            super(context);
        }

        @Override
        public int getLayoutId() {
            return R.layout.frag_spent_list_row_item;
        }

        @Override
        public SpendDetailsViewHolder getViewHolder(View convertView) {
            return new SpendDetailsViewHolder(convertView);
        }

        @Override
        public void bindView(int position, SpendDetailsViewHolder holder, final SpendData item) {
            holder.getDate().setText(item.getDate());
            holder.getDuration().setText(item.getDuration());
            holder.getTxtPhone().setText(item.getMobileNumber());
            holder.getTxtPulse().setText(item.getPulse());
            holder.getTxtPrice().setText(item.getPrice());
            holder.getArrow().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.setArrowDown(!item.isArrowDown());
                    notifyDataSetChanged();
                }
            });
            if (item.isArrowDown()) {
                holder.getDurationContainer().setVisibility(View.VISIBLE);
            } else {
                holder.getDurationContainer().setVisibility(View.GONE);
            }
        }
    }

}
