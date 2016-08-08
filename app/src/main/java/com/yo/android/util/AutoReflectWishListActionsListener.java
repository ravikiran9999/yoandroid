package com.yo.android.util;

import com.yo.android.model.Articles;

/**
 * Created by kalyani on 8/8/16.
 */
public interface AutoReflectWishListActionsListener {
    void updateFollowOrLikesStatus(Articles data, String follow);
}
