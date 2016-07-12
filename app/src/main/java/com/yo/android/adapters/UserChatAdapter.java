package com.yo.android.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yo.android.R;
import com.yo.android.helpers.UserChatViewHolder;
import com.yo.android.model.ChatMessage;
import com.yo.android.util.Constants;


/**
 * Created by rdoddapaneni on 6/30/2016.
 */

public class UserChatAdapter extends AbstractBaseAdapter<ChatMessage, UserChatViewHolder> {

    private String userId;
    private Context context;
    private SparseBooleanArray mSelectedItemsIds;
    private LinearLayout.LayoutParams layoutParams;

    public UserChatAdapter(Context context) {
        super(context);
        this.context = context;

    }

    public UserChatAdapter(Context context, String userId) {
        super(context);
        this.context = context;
        this.userId = userId;
        this.mSelectedItemsIds = new SparseBooleanArray();
    }


    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    // Remove selection after unchecked
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    // Item checked on selection
    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    // Get number of selected item
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    @Override
    public int getLayoutId() {
        return R.layout.user_chat_list_item;
    }

    @Override
    public UserChatViewHolder getViewHolder(View convertView) {
        return new UserChatViewHolder(convertView);
    }

    @Override
    public void bindView(int position, UserChatViewHolder holder, ChatMessage item) {
        try {
            String timeStamp = DateUtils.getRelativeTimeSpanString(item.getTime(), System.currentTimeMillis(), DateUtils.WEEK_IN_MILLIS).toString();

            LinearLayout layout = new LinearLayout(context);
            holder.getChatTimeStamp().setText(timeStamp);

            if (userId.equals(item.getSenderID())) {

                holder.getLinearLayout().setGravity(Gravity.RIGHT);
                if (item.getType().equals(Constants.TEXT)) {
                    layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                } else if (item.getType().equals(Constants.IMAGE)) {
                    layoutParams = new LinearLayout.LayoutParams(300, 300);
                }

                layout.setLayoutParams(layoutParams);
                layoutParams.setMargins(60, 0, 0, 0);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                    holder.getLinearLayoutText().setBackground(mContext.getResources().getDrawable(R.drawable.bg_sms_yellow));
                } else {
                    holder.getLinearLayoutText().setBackgroundResource(R.drawable.bg_sms_yellow);
                }
                holder.getLinearLayoutText().setLayoutParams(layoutParams);
                addView(holder.getLinearLayoutText(), item, holder);

            } else {
                holder.getLinearLayout().setGravity(Gravity.LEFT);

                if (item.getType().equals(Constants.TEXT)) {
                    layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                } else if (item.getType().equals(Constants.IMAGE)) {
                    layoutParams = new LinearLayout.LayoutParams(300, 300);
                }

                layout.setLayoutParams(layoutParams);
                layoutParams.setMargins(0, 0, 60, 0);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    holder.getLinearLayoutText().setBackground(mContext.getResources().getDrawable(R.drawable.bg_sms_grey));
                } else {
                    holder.getLinearLayoutText().setBackgroundResource(R.drawable.bg_sms_grey);
                }

                holder.getLinearLayoutText().setLayoutParams(layoutParams);

                addView(holder.getLinearLayoutText(), item, holder);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addView(final LinearLayout linearLayout, ChatMessage item, final UserChatViewHolder holder) {

        if (item.getType().equals(Constants.TEXT)) {
            TextView textView = new TextView(context);
            textView.setTextColor(Color.BLACK);
            textView.setText(item.getMessage());
            if (linearLayout.getTag() == null) {
                linearLayout.setTag(holder);
                linearLayout.addView(textView);
            }

        } else if (item.getType().equals(Constants.IMAGE)) {
            try {
                final ImageView imageView = new ImageView(context);
                // Create a storage reference from our app
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://samplefcm-ce2c6.appspot.com");
                StorageReference imageRef = storageRef.child(item.getImagePath());
                final long oneMegaByte = 1024 * 1024;
                imageRef.getBytes(oneMegaByte).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap);
                        if (linearLayout.getTag() == null) {
                            linearLayout.setTag(holder);
                            linearLayout.addView(imageView);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        exception.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
