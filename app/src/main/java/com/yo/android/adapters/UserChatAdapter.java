package com.yo.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
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

import com.yo.android.R;
import com.yo.android.chat.ImageLoader;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.helpers.Helper;
import com.yo.android.helpers.UserChatViewHolder;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.Contact;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.util.Constants;
import com.yo.android.util.DateUtil;
import com.yo.android.util.Util;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import github.ankushsachdeva.emojicon.EmojiconTextView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import static com.yo.android.util.Util.ServerTimeStamp;


/**
 * Created by rdoddapaneni on 6/30/2016.
 */

public class UserChatAdapter extends AbstractBaseAdapter<ChatMessage, UserChatViewHolder> implements StickyListHeadersAdapter {

    private String userId;
    private String roomType;
    private Context context;
    private SparseBooleanArray mSelectedItemsIds;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private ContactsSyncManager mContactsSyncManager;
    private LayoutInflater inflater;

    /*public UserChatAdapter(Activity context) {
        super(context);
        this.context = context.getBaseContext();
    }*/

    public UserChatAdapter(Activity context, String userId, String type,
                           ContactsSyncManager mContactsSyncManager) {
        super(context);
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.userId = userId;
        this.mSelectedItemsIds = new SparseBooleanArray();
        this.roomType = type;
        this.mContactsSyncManager = mContactsSyncManager;

    }


    public boolean toggleSelection(int position) {
        selectedView(position, !mSelectedItemsIds.get(position));
        return !mSelectedItemsIds.get(position);
    }

    // Remove selection after unchecked
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    // Item checked on selection
    public void selectedView(int position, boolean value) {
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
                holder.getLinearLayout().setPadding(0, 0, 10, 0);
                if (item.getType().equals(Constants.TEXT)) {
                    newTextAddView(item, holder);
                } else if (item.getType().equals(Constants.IMAGE)) {
                    loadingFromXml(item, holder);
                }
            } else {
                holder.getLinearLayout().setGravity(Gravity.START);
                holder.getLinearLayout().setPadding(10, 0, 0, 0);
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

    private void loadingFromXml(final ChatMessage item, final UserChatViewHolder holder) {
        holder.getLl().removeAllViews();
        holder.getLl().setTag(holder);
        boolean isRTL = userId.equalsIgnoreCase(item.getSenderID());
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.chatitem, null);
        TextView senderNameOrNumber = (TextView) view.findViewById(R.id.sender_id);
        RelativeLayout profileNameLayout = (RelativeLayout) view.findViewById(R.id.chat_profilename_layout);
        RelativeLayout gravityLayout = view;
        TextView profileName = (TextView) view.findViewById(R.id.profile_name);
        ImageView loadImage = (ImageView) view.findViewById(R.id.chat_image);
        TextView extraText = (TextView) view.findViewById(R.id.extra_chat_message);

        TextView time = (TextView) view.findViewById(R.id.date_view);
        TextView sent = (TextView) view.findViewById(R.id.sent_txt);
        TextView seen = (TextView) view.findViewById(R.id.seen_txt);
        TextView timer = (TextView) view.findViewById(R.id.timer);
        timer.setVisibility(View.VISIBLE);
        sent.setVisibility(View.GONE);
        seen.setVisibility(View.GONE);

        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress);
        extraText.setVisibility(View.GONE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (!isRTL) {
            timer.setVisibility(View.GONE);
            sent.setVisibility(View.GONE);
            seen.setVisibility(View.GONE);

            lp.setMargins(30, 0, 0, 0);
            profileNameLayout.setVisibility(View.VISIBLE);
            gravityLayout.setGravity(Gravity.LEFT);
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
                String seenTxt = DateUtil.getTimeFormatForChat(context, item.getDeliveredTime());
                time.setText(seenTxt);
            }
            senderNameOrNumber.setTextColor(mColorGenerator.getRandomColor());
        } else {
            lp.setMargins(0, 0, 0, 0);
            gravityLayout.setGravity(Gravity.RIGHT);
            profileNameLayout.setVisibility(View.GONE);
            gravityLayout.setBackgroundResource(R.drawable.msg_out_photo);
            Drawable img = context.getResources().getDrawable(R.drawable.time_loader);
            img.setBounds(10, 0, 30, 50);
            img.setBounds(Helper.dp(context, 1), Helper.dp(context, 2), Helper.dp(context, 60), Helper.dp(context, 610));
            //time.setCompoundDrawables(null, null, img, null);
           // time.setCompoundDrawablePadding(10);
            String sentText = DateUtil.getTimeFormatForChat(context, item.getTime());
            time.setText(sentText);

            if (item.getSent() == 1) {
                sent.setVisibility(View.VISIBLE);
                seen.setVisibility(View.GONE);
                timer.setVisibility(View.GONE);

            }
            if (item.getDeliveredTime() != 0) {
                sent.setVisibility(View.GONE);
                seen.setVisibility(View.VISIBLE);
                timer.setVisibility(View.GONE);
            }
        }
        ImageLoader.updateImage(context, item, Constants.YOIMAGES, loadImage, progressBar);
        holder.getLl().addView(view, lp);
    }


    private LinearLayout newTextAddView(@NonNull final ChatMessage item, @NonNull final UserChatViewHolder holder) {
        holder.getLl().removeAllViews();
        holder.getLl().setTag(holder);
        LinearLayout secretChatPlaceholder = (LinearLayout) inflater.inflate(R.layout.balloon, null);
        secretChatPlaceholder.setOrientation(LinearLayout.HORIZONTAL);
        boolean isRTL = userId.equalsIgnoreCase(item.getSenderID());

        LinearLayout linearLayout1 = new LinearLayout(context);
        linearLayout1.setOrientation(LinearLayout.VERTICAL);
        linearLayout1.setPadding(Helper.dp(context, 2), Helper.dp(context, 8), Helper.dp(context, 2), Helper.dp(context, 8));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 0, 20, 0);

        TextView emojiTextView = (TextView) secretChatPlaceholder.findViewById(R.id.chat_msg);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        emojiTextView.setText(item.getMessage());
        emojiTextView.setGravity(Gravity.BOTTOM);
        LinearLayout mainLayout = (LinearLayout) secretChatPlaceholder.findViewById(R.id.lytStatusContainer);
        TextView time = (TextView) mainLayout.findViewById(R.id.date_view);

        TextView sent = (TextView) mainLayout.findViewById(R.id.sent_txt);
        TextView seen = (TextView) mainLayout.findViewById(R.id.seen_txt);
        TextView timer = (TextView) mainLayout.findViewById(R.id.timer);
        timer.setVisibility(View.VISIBLE);
        sent.setVisibility(View.GONE);
        seen.setVisibility(View.GONE);
        if (!isRTL) {
            timer.setVisibility(View.GONE);
            sent.setVisibility(View.GONE);
            seen.setVisibility(View.GONE);
            rlp.setMargins(30, 0, 0, 0);
            TextView senderId = new TextView(context);
            secretChatPlaceholder.setBackgroundResource(R.drawable.msg_in);
            senderId.setLayoutParams(lp);
            if (roomType != null) {
                Contact contact = mContactsSyncManager.getContactByVoxUserName(item.getVoxUserName());
                if (contact != null && !TextUtils.isEmpty(contact.getName())) {
                    senderId.setText(contact.getName());
                    int existingColor = mColorGenerator.getColor(contact.getName());
                    if (existingColor == 0) {
                        senderId.setTextColor(mColorGenerator.getRandomColor());
                    } else {
                        senderId.setTextColor(existingColor);
                    }
                } else {
                    senderId.setText(item.getSenderID());
                    int existingColor = mColorGenerator.getColor(item.getSenderID());
                    if (existingColor == 0) {
                        senderId.setTextColor(mColorGenerator.getRandomColor());
                    } else {
                        senderId.setTextColor(existingColor);
                    }
                }

                linearLayout1.addView(senderId);
            }
            //   seenLayout.setVisibility(View.GONE);
            if (item.getDeliveredTime() != 0) {
                String seenText = DateUtil.getTimeFormatForChat(context, item.getDeliveredTime());
                time.setText(seenText);
                time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            }
        } else {
            rlp.setMargins(0, 0, 30, 0);
            secretChatPlaceholder.setBackgroundResource(R.drawable.msg_out);
            String sentText = DateUtil.getTimeFormatForChat(context, item.getTime());
            time.setText(sentText);
            if (item.getSent() == 1) {
                sent.setVisibility(View.VISIBLE);
                seen.setVisibility(View.GONE);
                timer.setVisibility(View.GONE);

            }
            if (item.getDeliveredTime() != 0) {
                sent.setVisibility(View.GONE);
                seen.setVisibility(View.VISIBLE);
                timer.setVisibility(View.GONE);
            }
        }

        secretChatPlaceholder.addView(linearLayout1, Helper.createLinear(context, Helper.WRAP_CONTENT, Helper.WRAP_CONTENT, isRTL ? Gravity.RIGHT : Gravity.LEFT, isRTL ? 0 : 7, 0, 1, 0));
        holder.getLl().addView(secretChatPlaceholder, rlp);
        return secretChatPlaceholder;
    }

    private boolean isValidMobile(String phone) {
        try {
            return android.util.Patterns.PHONE.matcher(phone).matches();
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null || (convertView != null && convertView.getTag() == null)) {
            convertView = inflater.inflate(R.layout.stickey_timestamp_header, parent, false);
            holder = new HeaderViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        //set header text as first char in name
        Map map = getItem(position).getTimeStampMap();
        try {
            if (map != null && map.get(ServerTimeStamp) instanceof Long) {
                String headerTimeStamp = ChatMessage.getStickeyHeader((long) map.get(ServerTimeStamp));
                if (headerTimeStamp != null && !TextUtils.isEmpty(headerTimeStamp) && holder != null) {
                    String headerText = "" + headerTimeStamp;
                    holder.text.setText(headerText.toUpperCase());
                    return convertView;
                }
            }
        } catch (Exception e) {
            // catch Exception
        }
        return new View(parent.getContext());
    }

    @Override
    public long getHeaderId(int position) {
        Map map = getItem(position).getTimeStampMap();
        try {
            if (map != null && map.get(ServerTimeStamp) instanceof Long) {
                String timeStamp = ChatMessage.getStickeyHeader((long) map.get(ServerTimeStamp));
                if (timeStamp != null) {
                    return timeStamp.subSequence(0, timeStamp.length()).hashCode();
                }
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public void UpdateItem(ChatMessage message) {
        getAllItems().add(message);
        notifyDataSetChanged();
    }

    public class HeaderViewHolder {
        @Bind(R.id.time_stamp_text)
        TextView text;

        HeaderViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }
}
