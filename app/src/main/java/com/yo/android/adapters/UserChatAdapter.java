package com.yo.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.helpers.UserChatViewHolder;
import com.yo.android.model.ChatMessage;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import static com.yo.android.R.color.colorPrimaryDark;


/**
 * Created by rdoddapaneni on 6/30/2016.
 */

public class UserChatAdapter extends AbstractBaseAdapter<ChatMessage, UserChatViewHolder> implements StickyListHeadersAdapter {

    private LayoutInflater inflater;
    private String userId;
    private String roomType;
    private Context context;
    private SparseBooleanArray mSelectedItemsIds;
    private LinearLayout.LayoutParams layoutParams;

    public UserChatAdapter(Activity context) {
        super(context);
        this.context = context.getBaseContext();

    }

    public UserChatAdapter(Activity context, String userId, String type) {
        super(context);
        inflater = LayoutInflater.from(context);
        this.context = context.getBaseContext();
        this.userId = userId;
        this.mSelectedItemsIds = new SparseBooleanArray();
        this.roomType = type;
    }


    public boolean toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
        return !mSelectedItemsIds.get(position);
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
            LinearLayout layout = new LinearLayout(context);
            holder.getChatTimeStamp().setText(Util.getChatListTimeFormat(item.getTime()) + " " + Util.getTimeFormat(mContext, item.getTime()));

            if (userId.equals(item.getSenderID())) {

                holder.getLinearLayout().setGravity(Gravity.END);
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
                holder.getLinearLayout().setGravity(Gravity.START);

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
        linearLayout.removeAllViews();
        linearLayout.setTag(holder);

        if (item.getType().equals(Constants.TEXT)) {
            if(roomType != null) {
                TextView tvName = new TextView(context);
                tvName.setTextColor(context.getResources().getColor(colorPrimaryDark));
                tvName.setText(item.getSenderID());
                linearLayout.addView(tvName);
            }

            TextView textView = new TextView(context);
            textView.setTextColor(Color.BLACK);
            textView.setText(item.getMessage());
//            if (linearLayout.getTag() == null) {
            linearLayout.setTag(holder);

            linearLayout.addView(textView);
//            }

        } else if (item.getType().equals(Constants.IMAGE)) {
            try {
                if(roomType != null) {
                    TextView tvName = new TextView(context);
                    tvName.setTextColor(context.getResources().getColor(colorPrimaryDark));
                    tvName.setText(item.getSenderID());
                    linearLayout.addView(tvName);
                }

                final ImageView imageView = new ImageView(context);
                imageView.setTag(holder);
                // Create a storage reference from our app
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://yoandroid-a0b48.appspot.com");
                StorageReference imageRef = storageRef.child(item.getImagePath());
                linearLayout.addView(imageView);

                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(context).load(uri).into(imageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (OutOfMemoryError outOfMemoryError) {
                outOfMemoryError.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.stickey_timestamp_header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.time_stamp_text);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        //set header text as first char in name
        String headerTimeStamp = getItem(position).getStickeyHeader();
        String headerText = "" + headerTimeStamp;
        holder.text.setText(headerText);
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        String timeStamp = getItem(position).getStickeyHeader();
        return timeStamp.subSequence(0, timeStamp.length()).hashCode();
    }

    private class HeaderViewHolder {
        TextView text;
    }
}
