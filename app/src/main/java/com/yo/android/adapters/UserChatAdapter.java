package com.yo.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.fragments.UserChatFragment;
import com.yo.android.helpers.Helper;
import com.yo.android.helpers.UserChatViewHolder;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.Contact;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import github.ankushsachdeva.emojicon.EmojiconTextView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;


/**
 * Created by rdoddapaneni on 6/30/2016.
 */

public class UserChatAdapter extends AbstractBaseAdapter<ChatMessage, UserChatViewHolder> implements StickyListHeadersAdapter {

    private LayoutInflater inflater;
    private String userId;
    private String roomType;
    private Context context;
    private SparseBooleanArray mSelectedItemsIds;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    ContactsSyncManager mContactsSyncManager;

    public UserChatAdapter(Activity context) {
        super(context);
        this.context = context.getBaseContext();

    }

    public UserChatAdapter(Activity context, String userId, String type,
                           ContactsSyncManager mContactsSyncManager) {
        super(context);
        inflater = LayoutInflater.from(context);
        this.context = context.getBaseContext();
        this.userId = userId;
        this.mSelectedItemsIds = new SparseBooleanArray();
        this.roomType = type;
        this.mContactsSyncManager = mContactsSyncManager;
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

            if (userId.equals(item.getSenderID())) {
                if (item.getSent() != 0) {
                    holder.getChatTimeStamp().setText(Constants.SENT + " " + Util.getTimeFormat(mContext, item.getTime()));
                } else {
                    holder.getChatTimeStamp().setText("");
                }
                if (item.getDeliveredTime() != 0) {
                    holder.getSeenTimeStamp().setText(Constants.SEEN + " " + Util.getTimeFormat(mContext, item.getDeliveredTime()));
                } else {
                    holder.getSeenTimeStamp().setText("");
                }
                holder.getLinearLayout().setGravity(Gravity.END);
                if (item.getType().equals(Constants.TEXT)) {
                    newTextAddView(item, holder);
                } else if (item.getType().equals(Constants.IMAGE)) {
                    newAddView(item, holder);
                }
            } else {
                if (item.getDeliveredTime() != 0) {
                    holder.getChatTimeStamp().setText("");
                    holder.getSeenTimeStamp().setText(Constants.RECEIVED + " " + Util.getTimeFormat(mContext, item.getDeliveredTime()));
                } else {
                    holder.getSeenTimeStamp().setText("");
                }
                holder.getLinearLayout().setGravity(Gravity.START);
                if (item.getType().equals(Constants.TEXT)) {
                    newTextAddView(item, holder);
                } else if (item.getType().equals(Constants.IMAGE)) {
                    newAddView(item, holder);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void newAddView(final ChatMessage item, final UserChatViewHolder holder) {
        holder.getLl().removeAllViews();
        holder.getLl().setTag(holder);
        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        final LinearLayout secretChatPlaceholder = new LinearLayout(context);
        boolean isRTL = userId.equalsIgnoreCase(item.getSenderID());
        secretChatPlaceholder.setBackgroundResource(R.drawable.system);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(30, 0, 30, 5);
        secretChatPlaceholder.setLayoutParams(lp);
        secretChatPlaceholder.getBackground().setColorFilter(colorFilter);
        secretChatPlaceholder.setPadding(Helper.dp(context, 2), Helper.dp(context, 2), Helper.dp(context, 2), Helper.dp(context, 2));
        secretChatPlaceholder.setOrientation(LinearLayout.VERTICAL);
        //Adding person name or phone number
        LinearLayout linearLayout1 = new LinearLayout(context);
        linearLayout1.setOrientation(LinearLayout.VERTICAL);
        if (!isRTL) {

            TextView senderId = new TextView(context);
            senderId.setLayoutParams(lp);
            Contact contact = mContactsSyncManager.getContactByVoxUserName(item.getVoxUserName());
            if (contact != null && contact.getName() != null) {
                senderId.setText(contact.getName());
            } else {
                //  boolean isValidMobile = isValidMobile(item.getChatProfileUserName());
                // String profileName = isValidMobile ? " ~" + item.getChatProfileUserName() : "";
                senderId.setText(item.getSenderID() + "");
            }
            senderId.setTextColor(mColorGenerator.getRandomColor());
            linearLayout1.addView(senderId);
        }
        // Add image
        final ImageView imageView1 = new ImageView(context);
        linearLayout1.addView(imageView1);
        secretChatPlaceholder.addView(linearLayout1, Helper.createLinear(context, Helper.WRAP_CONTENT, Helper.WRAP_CONTENT, isRTL ? Gravity.RIGHT : Gravity.LEFT, 0, 8, 0, 0));

        if (item.getImagePath() != null) {
            updateImage(item, holder, secretChatPlaceholder, imageView1);
        }
    }

    private LinearLayout updateImage(ChatMessage item, UserChatViewHolder holder, LinearLayout secretChatPlaceholder, final ImageView imageView1) {
        File file = new File(item.getImagePath());
        if (file != null && !file.exists()) {
            file = new File(Environment.getExternalStorageDirectory() + "/YO/YOImages/" + file.getName());
        }
        if (file.exists()) {
            getImageHeightAndWidth(file, imageView1);
        } else {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(BuildConfig.STORAGE_BUCKET);
            StorageReference imageRef = storageRef.child(item.getImagePath());
            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(final Uri uri) {
                    Picasso.with(context).load(uri).transform(transformation).into(imageView1);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            });
        }
        holder.getLl().addView(secretChatPlaceholder);
        return secretChatPlaceholder;
    }

    Transformation transformation = new Transformation() {

        @Override
        public Bitmap transform(Bitmap source) {
            //storeImage(source, "");
            int targetWidth = 800;
            double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
            int targetHeight = (int) (targetWidth * aspectRatio);
            Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override
        public String key() {
            return "transformation" + " desiredWidth";
        }
    };

    private void storeImage(Bitmap bmp, String filename) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private LinearLayout newTextAddView(final ChatMessage item, final UserChatViewHolder holder) {
        holder.getLl().removeAllViews();
        holder.getLl().setTag(holder);
        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        LinearLayout secretChatPlaceholder = new LinearLayout(context);
        secretChatPlaceholder.setBackgroundResource(R.drawable.system);
        secretChatPlaceholder.getBackground().setColorFilter(colorFilter);
        secretChatPlaceholder.setPadding(Helper.dp(context, 2), Helper.dp(context, 2), Helper.dp(context, 2), Helper.dp(context, 2));
        secretChatPlaceholder.setOrientation(LinearLayout.VERTICAL);
        boolean isRTL = userId.equalsIgnoreCase(item.getSenderID());

        LinearLayout linearLayout1 = new LinearLayout(context);
        linearLayout1.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(30, 0, 30, 5);
        if (!isRTL) {
            TextView senderId = new TextView(context);
            senderId.setLayoutParams(lp);
            Contact contact = mContactsSyncManager.getContactByVoxUserName(item.getVoxUserName());
            if (contact != null && contact.getName() != null) {
                senderId.setText(contact.getName());
            } else {
                // boolean isValidMobile = isValidMobile(item.getChatProfileUserName());
                // String profileName = isValidMobile ? " ~" + item.getChatProfileUserName() : "";
                senderId.setText(item.getSenderID() + "");
            }
            senderId.setTextColor(mColorGenerator.getRandomColor());
            linearLayout1.addView(senderId);
        }

        EmojiconTextView textView = new EmojiconTextView(context);
        textView.setEmojiconSize(28);
        textView.setLayoutParams(lp);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setGravity(Gravity.LEFT);
        textView.setMaxWidth(Helper.dp(context, 260));
        textView.setTextColor(Color.BLACK);
        textView.setText(item.getMessage());
        linearLayout1.addView(textView);

        // linearLayout.setBackgroundResource(R.drawable.profile_background);
        //linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        secretChatPlaceholder.addView(linearLayout1, Helper.createLinear(context, Helper.WRAP_CONTENT, Helper.WRAP_CONTENT, isRTL ? Gravity.RIGHT : Gravity.LEFT, 0, 8, 0, 0));


        holder.getLl().addView(secretChatPlaceholder);
        return secretChatPlaceholder;
    }

    private void getImageHeightAndWidth(final File file, ImageView imageView) {
        int maxWidth = 800;
        int maxHeight = 500;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        int height = options.outHeight;
        int width = options.outWidth;
        float ratio = (float) width / maxWidth;
        width = maxWidth;
        height = (int) (height / ratio);
        Glide.with(context)
                .load(file)
                .override(width, height)
                .dontAnimate()
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    private boolean isValidMobile(String phone) {
        try {
            return android.util.Patterns.PHONE.matcher(phone).matches();
        } catch (NullPointerException e) {
            return false;
        }
    }

    private void addView(final RelativeLayout relativeLayout, final ChatMessage item, final UserChatViewHolder holder) {

        holder.getLl().removeAllViews();
        holder.getLl().setTag(holder);
        LinearLayout linearLayout1 = new LinearLayout(context);
        linearLayout1.setOrientation(LinearLayout.VERTICAL);

        if (item.getType().equals(Constants.TEXT)) {

            if (roomType != null) {
                TextView senderId = new TextView(context);
                senderId.setText(item.getSenderID());
                senderId.setTextColor(Color.BLACK);
                linearLayout1.addView(senderId);
            }

            TextView textView = new TextView(context);
            textView.setTextColor(Color.BLACK);
            textView.setText(item.getMessage());
            linearLayout1.addView(textView);
            relativeLayout.addView(linearLayout1);

            holder.getLl().setTag(holder);
            holder.getLl().addView(relativeLayout);


        } else if (item.getType().equals(Constants.IMAGE)) {
            try {

                final ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(1050, 900);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                relativeLayout.addView(progressBar, params);


                if (roomType != null) {
                    TextView senderId = new TextView(context);
                    senderId.setText(item.getSenderID());
                    senderId.setTextColor(Color.BLACK);

                    linearLayout1.addView(senderId);
                }

                final ImageView imageView = new ImageView(context);
                RelativeLayout.LayoutParams imageparams = new RelativeLayout.LayoutParams(1050, 900);
                imageparams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                imageView.setLayoutParams(imageparams);
                imageView.setTag(holder);
                imageView.setAdjustViewBounds(true);

                imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                // Create a storage reference from our app
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl(BuildConfig.STORAGE_BUCKET);
                StorageReference imageRef = storageRef.child(item.getImagePath());
                linearLayout1.addView(imageView);
                relativeLayout.addView(linearLayout1);
                if (item.getImagePath() != null) {
                    Helper.loadDirectly(mContext, imageView, new File(item.getImagePath()));
                } else if (item.getImageUrl() != null) {
                    Picasso.with(context).load(Uri.parse(item.getImageUrl()))
                            .config(Bitmap.Config.RGB_565)
                            .into(imageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    progressBar.setVisibility(View.INVISIBLE);
                                }

                                @Override
                                public void onError() {

                                }
                            });

                } else {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            Picasso.with(context).load(uri).into(imageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    progressBar.setVisibility(View.INVISIBLE);
                                }

                                @Override
                                public void onError() {

                                }
                            });


                            item.setImageUrl(uri.toString());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

            } catch (OutOfMemoryError outOfMemoryError) {
                outOfMemoryError.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.getLl().addView(relativeLayout);
        } else {
            holder.getLl().addView(null);
        }

    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {

            convertView = inflater.inflate(R.layout.stickey_timestamp_header, parent, false);
            holder = new HeaderViewHolder(convertView);
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

    public void UpdateItem(ChatMessage message) {
        getAllItems().add(message);
        notifyDataSetChanged();
    }

    private class HeaderViewHolder {
        TextView text;

        HeaderViewHolder(View v) {
            text = (TextView) v.findViewById(R.id.time_stamp_text);
        }
    }


}
