package com.yo.android.helpers;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;


import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by MYPC on 7/17/2016.
 */
public class FindPeopleViewHolder extends AbstractViewHolder {

    @BindView(R.id.imv_find_people_pic)
    CircleImageView imvFindPeoplePic;
    @BindView(R.id.tv_find_people_name)
    TextView tvFindPeopleName;
    @BindView(R.id.tv_find_people_desc)
    TextView tvFindPeopleDesc;
    @BindView(R.id.imv_find_people_follow)
    Button btnFindPeopleFollow;

    public FindPeopleViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public CircleImageView getImvFindPeoplePic() {
        return imvFindPeoplePic;
    }

    public void setImvFindPeoplePic(CircleImageView imvFindPeoplePic) {
        this.imvFindPeoplePic = imvFindPeoplePic;
    }

    public TextView getTvFindPeopleName() {
        return tvFindPeopleName;
    }

    public void setTvFindPeopleName(TextView tvFindPeopleName) {
        this.tvFindPeopleName = tvFindPeopleName;
    }

    public TextView getTvFindPeopleDesc() {
        return tvFindPeopleDesc;
    }

    public void setTvFindPeopleDesc(TextView tvFindPeopleDesc) {
        this.tvFindPeopleDesc = tvFindPeopleDesc;
    }

    public Button getBtnFindPeopleFollow() {
        return btnFindPeopleFollow;
    }

    public void setBtnFindPeopleFollow(Button btnFindPeopleFollow) {
        this.btnFindPeopleFollow = btnFindPeopleFollow;
    }
}
