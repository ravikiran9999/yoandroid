package com.yo.android.util;

import com.yo.android.model.Articles;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by creatives on 12/7/2016.
 */
public class ArticlesComparator implements Comparator<Articles> {
    @Override
    public int compare(Articles lhs, Articles rhs) {
        String modifiedTime1 = lhs.getUpdated().substring(0, lhs.getUpdated().lastIndexOf("."));
        Date date1 = DateUtil.convertUtcToGmt(modifiedTime1);
        String modifiedTime2 = rhs.getUpdated().substring(0, rhs.getUpdated().lastIndexOf("."));
        Date date2 = DateUtil.convertUtcToGmt(modifiedTime2);
        return date1.compareTo(date2);
    }
}
