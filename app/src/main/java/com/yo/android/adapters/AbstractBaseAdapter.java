package com.yo.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.model.Contact;
import com.yo.android.model.Members;
import com.yo.android.model.Room;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.List;

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
        if (mOriginalList.isEmpty()) {
            mOriginalList = new ArrayList<>(list);
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

    public void performSearch(@NonNull String key) {
        key = key.trim();
        if (key.isEmpty()) {
            addItems(mOriginalList);
        } else {
            List<T> temp = new ArrayList<>();
            for (T event : mOriginalList) {
                if (hasData(event, key)) {
                    temp.add(event);
                }
            }
            addItems(temp);
        }
    }

    public void performContactsSearch(@NonNull String key) {
        if (key.isEmpty()) {
            addItems(mOriginalList);
        } else {

            List<T> temp = new ArrayList<>();
            try {
                if (loginPrefs != null) {
                    myNumber = loginPrefs.getStringPreference(Constants.PHONE_NUMBER);
                }
            } catch (Exception e) {
            }

            for (T event : mOriginalList) {
                List<Members> memberses = ((Room) event).getMembers();

                for (int j = 0; j < memberses.size(); j++) {
                    String number = memberses.get(j).getMobileNumber();
                    if (number != null && !number.contentEquals(myNumber)) {
                        String mKey = memberses.get(j).getMobileNumber();
                        if (mKey.contains(key)) {
                            temp.add(event);
                        } else if (mKey.contains(key)) {
                            temp.clear();
                            temp.add(event);
                        }
                    }
                }
            }
            addItems(temp);
        }
    }

    public void performYoContactsSearch(@NonNull String key) {
        if (key.isEmpty()) {
            addItems(mOriginalList);
        } else {

            List<T> temp = new ArrayList<>();
            for (T event : mOriginalList) {
                if (((Contact) event).getName() != null && ((Contact) event).getName().toLowerCase().contains(key.toLowerCase())) {
                    temp.add(event);
                }
                if (((Contact) event).getPhoneNo() != null && ((Contact) event).getPhoneNo().contains(key)) {
                    temp.add(event);
                }
            }
            addItems(temp);
        }
    }

    protected boolean hasData(T event, String key) {
        return false;
    }

    public void removeItem(T object) {
        this.mList.remove(object);
        notifyDataSetChanged();
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
