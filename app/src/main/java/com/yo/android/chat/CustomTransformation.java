package com.yo.android.chat;

import com.squareup.picasso.Transformation;

/**
 * Created by rajesh on 28/9/16.
 */
public interface CustomTransformation extends Transformation {
    void setFileName(String file);
    void setFolderName(String folderName);
}
