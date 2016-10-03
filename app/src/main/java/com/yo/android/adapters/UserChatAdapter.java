package com.yo.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
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
import com.bumptech.glide.Priority;
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
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.chat.CompressImage;
import com.yo.android.chat.CustomTransformation;
import com.yo.android.chat.ImageLoader;
import com.yo.android.chat.MaxWidthLinearLayout;
import com.yo.android.chat.SquareImageView;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;

import github.ankushsachdeva.emojicon.EmojiconTextView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;


/**
 * Created by rdoddapaneni on 6/30/2016.
 */

public class UserChatAdapter extends AbstractBaseAdapter<ChatMessage, UserChatViewHolder> implements StickyListHeadersAdapter {

    private String userId;
    private String roomType;
    private Context context;
    private SparseBooleanArray mSelectedItemsIds;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    ContactsSyncManager mContactsSyncManager;
    private LayoutInflater inflater;

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
        this.inflater = LayoutInflater.from(this.context);
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
                holder.getLinearLayout().setGravity(Gravity.END);
                if (item.getType().equals(Constants.TEXT)) {
                    newTextAddView(item, holder);
                } else if (item.getType().equals(Constants.IMAGE)) {
                    loadingFromXml(item, holder);
                }
            } else {
                holder.getLinearLayout().setGravity(Gravity.START);
                if (item.getType().equals(Constants.TEXT)) {
                    newTextAddView(item, holder);
                } else if (item.getType().equals(Constants.IMAGE)) {
                    loadingFromXml(item, holder);
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
        linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
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
            //updateImage(item, holder, secretChatPlaceholder, imageView1);
        }

    }

    private void loadingFromXml(final ChatMessage item, final UserChatViewHolder holder) {
        holder.getLl().removeAllViews();
        holder.getLl().setTag(holder);
        boolean isRTL = userId.equalsIgnoreCase(item.getSenderID());
        View view = inflater.inflate(R.layout.chatitem, null);
        TextView senderNameOrNumber = (TextView) view.findViewById(R.id.sender_id);
        RelativeLayout profileNameLayout = (RelativeLayout) view.findViewById(R.id.chat_profilename_layout);
        LinearLayout gravityLayout = (LinearLayout) view.findViewById(R.id.chat_gravity_decide_layout);
        TextView profileName = (TextView) view.findViewById(R.id.profile_name);
        SquareImageView loadImage = (SquareImageView) view.findViewById(R.id.chat_image);
        TextView extraText = (TextView) view.findViewById(R.id.extra_chat_message);
        RelativeLayout seenLayout = (RelativeLayout) view.findViewById(R.id.seen_layout);
        TextView sentTxt = (TextView) view.findViewById(R.id.sent_txt);
        TextView seenTxt = (TextView) view.findViewById(R.id.seen_txt);
        TextView time = (TextView) view.findViewById(R.id.time);
        extraText.setVisibility(View.GONE);
        if (!isRTL) {
            profileNameLayout.setVisibility(View.VISIBLE);
            gravityLayout.setGravity(Gravity.LEFT);
            seenLayout.setVisibility(View.GONE); //No seen for left side panel
            gravityLayout.setBackgroundResource(R.drawable.msg_in_photo);
            if (roomType != null) {
                profileNameLayout.setVisibility(View.VISIBLE);
                Contact contact = mContactsSyncManager.getContactByVoxUserName(item.getVoxUserName());
                if (contact != null && contact.getName() != null) {
                    senderNameOrNumber.setText(contact.getName());
                } else {
                    String profileNameValue = item.getChatProfileUserName() == null ? "" : " ~" + item.getChatProfileUserName();
                    senderNameOrNumber.setText(item.getSenderID());
                    profileName.setText(profileNameValue);
                }
            } else {
                profileNameLayout.setVisibility(View.GONE);
            }
            if (item.getDeliveredTime() != 0) {
                String seen = Util.getTimeFormatForChat(mContext, item.getDeliveredTime());
                time.setText(seen);
            }
            senderNameOrNumber.setTextColor(mColorGenerator.getRandomColor());
        } else {
            gravityLayout.setGravity(Gravity.RIGHT);
            profileNameLayout.setVisibility(View.GONE);
            gravityLayout.setBackgroundResource(R.drawable.msg_out_photo);
            sentTxt.setVisibility(View.VISIBLE);
            seenLayout.setVisibility(View.VISIBLE);
            if (item.getSent() != 0) {
                String sent = Util.getTimeFormatForChat(mContext, item.getTime());
                time.setText(sent);
                seenTxt.setVisibility(View.GONE);
            }
            if (item.getDeliveredTime() != 0) {
                String seen = Util.getTimeFormatForChat(mContext, item.getDeliveredTime());
                time.setText(seen);
                seenTxt.setVisibility(View.VISIBLE);
            }
        }
        ImageLoader.updateImage(context,item, loadImage);
        holder.getLl().addView(view);
    }




    private LinearLayout newTextAddView(final ChatMessage item, final UserChatViewHolder holder) {
        holder.getLl().removeAllViews();
        holder.getLl().setTag(holder);
        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        LinearLayout secretChatPlaceholder = new LinearLayout(context);
        secretChatPlaceholder.setPadding(Helper.dp(context, 2), Helper.dp(context, 2), Helper.dp(context, 2), Helper.dp(context, 2));
        secretChatPlaceholder.setOrientation(LinearLayout.VERTICAL);
        boolean isRTL = userId.equalsIgnoreCase(item.getSenderID());

        LinearLayout linearLayout1 = new LinearLayout(context);
        linearLayout1.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(30, 0, 50, 0);
        EmojiconTextView textView = new EmojiconTextView(context);
        textView.setEmojiconSize(28);
        textView.setLayoutParams(lp);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        textView.setGravity(Gravity.LEFT);
        textView.setMaxWidth(Helper.dp(context, 260));
        textView.setTextColor(Color.BLACK);
        textView.setText(item.getMessage());

        RelativeLayout mainLayout = (RelativeLayout) inflater.inflate(R.layout.chat_message, null);
       /* MaxWidthLinearLayout seenDetailsRelativeLayout = new MaxWidthLinearLayout(context, Helper.dp(context, 230));
        LinearLayout.LayoutParams lps = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.setMargins(30, 0, 50, 5);*/
        //RelativeLayout mainLayout = new RelativeLayout(context);
        // MaxWidthLinearLayout seenDetailsLayout = new MaxWidthLinearLayout(context, Helper.dp(context, 230));

        //seenDetailsLayout.setOrientation(LinearLayout.HORIZONTAL);
        //TextView time = new TextView(context);
        TextView time = (TextView) mainLayout.findViewById(R.id.time);
        time.setGravity(Gravity.BOTTOM);
        time.setTextColor(context.getResources().getColor(R.color.black));
        //seenDetailsLayout.addView(time);
        RelativeLayout seenLayout = (RelativeLayout)mainLayout.findViewById(R.id.seen_layout);
        //TextView seen = new TextView(context);
        TextView seen = (TextView) mainLayout.findViewById(R.id.seen_txt);

        //seen.setGravity(Gravity.BOTTOM);
        //seen.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.msg_check), null, null, null);
        // TextView sent = new TextView(context);
        TextView sent = (TextView) mainLayout.findViewById(R.id.sent_txt);

        // sent.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.msg_halfcheck), null, null, null);
        // sent.setGravity(Gravity.BOTTOM);
        // seenLayout.addView(seen);

       /* LinearLayout.LayoutParams head_params = (LinearLayout.LayoutParams) holder.getLl().getLayoutParams();
        head_params.setMargins(getPx(50), 0, 0, 0);
        sent.setLayoutParams(head_params);*/
        /// seenLayout.addView(sent);
        // seenLayout.setGravity(Gravity.RIGHT);
        // seenDetailsLayout.addView(seenLayout);
        //head_params.setMargins(getPx(50), 0, 0, 0);
        // seenDetailsLayout.setLayoutParams(head_params);
        // seenDetailsLayout.setGravity(Gravity.RIGHT);
        //mainLayout.addView(seenDetailsLayout);


        if (!isRTL) {
            TextView senderId = new TextView(context);
            secretChatPlaceholder.setBackgroundResource(R.drawable.msg_in);
            senderId.setLayoutParams(lp);
            if (roomType != null) {
                Contact contact = mContactsSyncManager.getContactByVoxUserName(item.getVoxUserName());
                if (contact != null && !TextUtils.isEmpty(contact.getName())) {
                    senderId.setText(contact.getName());
                } else {
                    senderId.setText(item.getSenderID());
                }
                senderId.setTextColor(mColorGenerator.getRandomColor());
                linearLayout1.addView(senderId);
            }
            seenLayout.setVisibility(View.GONE);
            if (item.getDeliveredTime() != 0) {
                String seenText = Util.getTimeFormatForChat(mContext, item.getDeliveredTime());
                time.setText(seenText);
                time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                time.setGravity(Gravity.RIGHT);
            }
        } else {
            sent.setVisibility(View.VISIBLE);
            time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            time.setGravity(Gravity.RIGHT);
            sent.setVisibility(View.VISIBLE);
            secretChatPlaceholder.setBackgroundResource(R.drawable.msg_out);
            if (item.getSent() != 0) {
                String sentText = Util.getTimeFormatForChat(mContext, item.getTime());
                time.setText(sentText);
                sent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                sent.setGravity(Gravity.RIGHT);
            }
            if (item.getDeliveredTime() != 0) {
                String seenText = Util.getTimeFormatForChat(mContext, item.getDeliveredTime());
                time.setText(seenText);
                seen.setVisibility(View.VISIBLE);
                seen.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                seen.setGravity(Gravity.RIGHT);
            }else{
                seen.setVisibility(View.GONE);
            }
        }
        linearLayout1.addView(textView);
        secretChatPlaceholder.addView(linearLayout1, Helper.createLinear(context, Helper.WRAP_CONTENT, Helper.WRAP_CONTENT, isRTL ? Gravity.RIGHT : Gravity.LEFT, 0, 8, 0, 0));
        secretChatPlaceholder.addView(mainLayout, Helper.createLinear(context, Helper.dp(context, 20), Helper.WRAP_CONTENT, isRTL ? Gravity.RIGHT : Gravity.RIGHT, 0, 8, 0, 0));

        holder.getLl().addView(secretChatPlaceholder);
        return secretChatPlaceholder;
    }

    private int getPx(int dp) {
        Resources r = mContext.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
        return px;
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
