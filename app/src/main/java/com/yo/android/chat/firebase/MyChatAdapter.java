package com.yo.android.chat.firebase;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yo.android.R;
import com.yo.android.helpers.UserChatViewHolder;
import com.yo.android.model.ChatMessage;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

public class MyChatAdapter extends FireBaseListAdapter<ChatMessage> {

    private final Activity activity;
    private String userId;

    public MyChatAdapter(Activity activity, Class<ChatMessage> modelClass, String userId, int modelLayout, DatabaseReference ref) {
        super(activity, modelClass, modelLayout, ref);
        this.activity = activity;
        this.userId = userId;
    }

    @Override
    protected void populateView(View v, ChatMessage item, int position) {
        UserChatViewHolder holder = new UserChatViewHolder(v);
        LinearLayout layout = new LinearLayout(activity);
        holder.getChatTimeStamp().setText(Util.getChatListTimeFormat(item.getTime()) + " " + Util.getTimeFormat(activity, item.getTime()));
        LinearLayout.LayoutParams layoutParams;
        if (userId.equals(item.getSenderID())) {
            holder.getLinearLayout().setGravity(Gravity.END);
            if (item.getType().equals(Constants.TEXT)) {
                layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            } else {
                layoutParams = new LinearLayout.LayoutParams(300, 300);
            }
            layout.setLayoutParams(layoutParams);
            layoutParams.setMargins(60, 0, 0, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                holder.getLinearLayoutText().setBackground(activity.getResources().getDrawable(R.drawable.bg_sms_yellow));
            } else {
                holder.getLinearLayoutText().setBackgroundResource(R.drawable.bg_sms_yellow);
            }
            holder.getLinearLayoutText().setLayoutParams(layoutParams);
            addView(holder.getLinearLayoutText(), item, holder);

        } else {
            holder.getLinearLayout().setGravity(Gravity.START);
            if (item.getType().equals(Constants.TEXT)) {
                layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            } else {
                layoutParams = new LinearLayout.LayoutParams(300, 300);
            }
            layout.setLayoutParams(layoutParams);
            layoutParams.setMargins(0, 0, 60, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.getLinearLayoutText().setBackground(activity.getResources().getDrawable(R.drawable.bg_sms_grey));
            } else {
                holder.getLinearLayoutText().setBackgroundResource(R.drawable.bg_sms_grey);
            }

            holder.getLinearLayoutText().setLayoutParams(layoutParams);
            addView(holder.getLinearLayoutText(), item, holder);

        }
    }

    private void addView(final LinearLayout linearLayout, ChatMessage item, final UserChatViewHolder holder) {
        linearLayout.removeAllViews();
        linearLayout.setTag(holder);
        if (item.getType().equals(Constants.TEXT)) {
            TextView textView = new TextView(activity);
            textView.setTextColor(Color.BLACK);
            textView.setText(item.getMessage());
            linearLayout.setTag(holder);
            linearLayout.addView(textView);
        } else if (item.getType().equals(Constants.IMAGE)) {
            final ImageView imageView = new ImageView(activity);
            imageView.setTag(holder);
            // Create a storage reference from our app
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://samplefcm-ce2c6.appspot.com");
            StorageReference imageRef = storageRef.child(item.getImagePath());
            final long oneMegaByte = 1024 * 1024;
            linearLayout.addView(imageView);
            imageRef.getBytes(oneMegaByte).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageView.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    exception.printStackTrace();
                }
            });
        }
    }

}
