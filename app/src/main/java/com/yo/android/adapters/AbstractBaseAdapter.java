package com.yo.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.Contact;
import com.yo.android.model.Members;
import com.yo.android.model.Room;
import com.yo.android.model.dialer.CallLogsResult;
import com.yo.android.model.dialer.CallRateDetail;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Ramesh on 14/1/16.
 */
public abstract class AbstractBaseAdapter<T, V extends AbstractViewHolder> extends BaseAdapter {

    protected List<T> mList;
    protected final Context mContext;
    private List<T> mOriginalList = new ArrayList<>();
    private SparseBooleanArray mSelectedItemsIds;
    private String myNumber;
    public List<T> temp;
    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    public AbstractBaseAdapter(Context context) {
        mContext = context;
        mList = new ArrayList<>();
        mSelectedItemsIds = new SparseBooleanArray();
    }


    public void addItems(List<T> list) {
        mList = new ArrayList<>(list);
        if (mOriginalList.size() == 0) {
            mOriginalList = new ArrayList<>(list);
        }
        notifyDataSetChanged();
    }

    public void addChatRoomItems(List<T> list) {
        mList = new ArrayList<>(list);
        if (mOriginalList.size() == 0) {
            mOriginalList = new ArrayList<>(list);
        } else if (list.size() > mOriginalList.size()) {
            mOriginalList.clear();
            mOriginalList.addAll(list);
        }
        notifyDataSetChanged();
    }

    /*public boolean toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
        return false;
    }*/

    // Remove selection after unchecked
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    // Item checked on selection
    public void selectView(int position, boolean value) {
        if (value) {
            mSelectedItemsIds.put(position, value);
        } else {
            mSelectedItemsIds.delete(position);
        }
        notifyDataSetChanged();
    }

    // Get number of selected item
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }


    public void addItemsAll(List<T> list) {
        this.mList.addAll(list);
        this.mOriginalList.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * Use this method for search to clear data when calling addItems() multiple times.
     */
    public void clearAll() {
        mOriginalList.clear();
        mList.clear();
        notifyDataSetChanged();
    }

    public List<T> performSearch(final @NonNull String key) {
        String searchKey = key.trim();
        if (searchKey.isEmpty()) {
            addItems(mOriginalList);
            return mOriginalList;
        } else {
            List<T> temp = new ArrayList<>();
            for (T event : mOriginalList) {
                if (hasData(event, searchKey)) {
                    temp.add(event);
                }
            }
            addItems(temp);
            return temp;
        }
    }

    public void performTransferBalanceContactsSearch(final @NonNull String key) {
        String searchKey = key.trim();
        if (searchKey.isEmpty()) {
            addItems(mOriginalList);
        } else if (searchKey.length() > 2) {
            List<T> temp = new ArrayList<>();
            for (T event : mOriginalList) {
                if (hasData(event, searchKey)) {
                    temp.add(event);
                }
            }
            addItems(temp);
        }
    }

    public void performCountryCodeSearch(final @NonNull String key) {
        String searchKey = key.trim();
        if (searchKey.isEmpty()) {
            addItems(mOriginalList);
        } else {
            List<T> temp = new ArrayList<>();
            for (T event : mOriginalList) {

                if (((CallRateDetail) event).getDestination() != null && ((CallRateDetail) event).getDestination().toLowerCase().contains(searchKey.toLowerCase())) {
                    temp.add(event);
                } else if (((CallRateDetail) event).getPrefix() != null && ((CallRateDetail) event).getPrefix().contains(searchKey)) {
                    temp.add(event);
                }
            }
            addItems(temp);
        }
    }

    public void performCallLogsSearch(final @NonNull String key, TextView noSearchResult, boolean isFromClose) {
        String searchKey = key.trim();
        if (searchKey.isEmpty()) {
            addItems(mOriginalList);
            if (mOriginalList.size() == 0 && !isFromClose) {
                noSearchResult.setVisibility(View.VISIBLE);
            } else {
                noSearchResult.setVisibility(View.GONE);
            }
        } else {
            List<T> temp = new ArrayList<>();
            for (T event : mOriginalList) {
                int callSize = ((ArrayList) ((Map.Entry) event).getValue()).size();
                for (int i = 0; i < callSize; i++) {
                    CallLogsResult callLogsResult = (CallLogsResult) ((ArrayList) ((Map.Entry) event).getValue()).get(i);
                    if (callLogsResult.getDialnumber() != null && callLogsResult.getDialnumber().toLowerCase().contains(searchKey)) {
                        temp.add(event);
                        break;
                    } else if (callLogsResult.getDestination_name() != null && callLogsResult.getDestination_name().toLowerCase().contains(searchKey.toLowerCase())) {
                        temp.add(event);
                        break;
                    }
                }

            }
            if (temp.size() == 0) {
                noSearchResult.setVisibility(View.VISIBLE);
            } else {
                noSearchResult.setVisibility(View.GONE);
            }
            addItems(temp);
        }
    }

    public void performContactsSearch(final @NonNull String key, TextView noSearchResult, boolean isFromClose) {
        String searchKey = key.trim();
        if (searchKey.isEmpty()) {
            addItems(mOriginalList);
            if (mOriginalList.size() == 0 && !isFromClose) {
                noSearchResult.setVisibility(View.VISIBLE);
            } else {
                noSearchResult.setVisibility(View.GONE);
            }
        } else {

            List<T> temp = new ArrayList<>();
            try {
                if (loginPrefs != null) {
                    myNumber = loginPrefs.getStringPreference(Constants.PHONE_NUMBER);
                }
            } catch (Exception e) {
            }

            for (T event : mOriginalList) {

                if (((Room) event).getMobileNumber() != null && ((Room) event).getMobileNumber().contains(searchKey)) {
                    temp.add(event);
                }

                if (((Room) event).getFullName() != null && ((Room) event).getFullName().toLowerCase().contains(searchKey.toLowerCase())
                        || ((Room) event).getGroupName() != null && ((Room) event).getGroupName().toLowerCase().contains(searchKey.toLowerCase())) {
                    temp.add(event);
                }
            }
            if (temp.size() == 0) {
                noSearchResult.setVisibility(View.VISIBLE);
            } else {
                noSearchResult.setVisibility(View.GONE);
            }
            addItems(temp);
        }
    }

    public void performYoContactsSearch(final @NonNull String key, final @NonNull String contactType, TextView noSearchResult, boolean isFromClose) {
        String searchKey = key.trim();
        if (searchKey.isEmpty()) {
            addItems(mOriginalList);
            if (mOriginalList.size() == 0 && !isFromClose) {
                noSearchResult.setVisibility(View.VISIBLE);
            } else {
                noSearchResult.setVisibility(View.GONE);
            }
        } else {
            temp = new ArrayList<>();
            if (contactType.equalsIgnoreCase(Constants.Yo_CONT_FRAG)) {
                if (mOriginalList != null && mOriginalList.size() > 0) {
                    temp.add(0, mOriginalList.get(0));
                }
            }
            for (T event : mOriginalList) {
                if (((Contact) event).getName() != null && ((Contact) event).getName().toLowerCase().contains(searchKey.toLowerCase())) {
                    temp.add(event);
                } else if (((Contact) event).getPhoneNo() != null && ((Contact) event).getPhoneNo().contains(searchKey)) {
                    temp.add(event);
                }

            }

            if (temp.size() == 0) {
                noSearchResult.setVisibility(View.VISIBLE);
            } else {
                noSearchResult.setVisibility(View.GONE);
            }
            addItems(temp);
        }
    }

    protected boolean hasData(T event, String key) {
        return false;
    }

    public void removeItem(T object) {
        this.mList.remove(object);
        this.mOriginalList.remove(object);
        notifyDataSetChanged();
    }

    public int getOriginalListCount() {
        return mOriginalList.size();
    }

    public List<T> getAllItems() {
        return mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public T getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        V holder;
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(getLayoutId(), null);
            holder = getViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (V) view.getTag();
        }
        bindView(position, holder, getItem(position));
        return view;
    }

    public abstract int getLayoutId();

    public abstract V getViewHolder(View convertView);

    public abstract void bindView(int position, V holder, T item);


}
