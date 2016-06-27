package com.yo.android.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yo.android.R;
import com.yo.android.adapters.MenuListAdapter;
import com.yo.android.model.MenuData;

import java.util.ArrayList;
import java.util.List;

/**
 * The Settings screen
 */
public class SettingsActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    protected MenuListAdapter menuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prepareSettingsList();

    }

    /**
     * Prepares the Settings list
     */
    public void prepareSettingsList() {
        menuAdapter = new MenuListAdapter(this) {
            @Override
            public int getLayoutId() {
                return R.layout.settings_row;
            }
        };
        ListView menuListView = (ListView) findViewById(R.id.lv_settings);
        menuAdapter.addItems(getMenuList());

        menuListView.setAdapter(menuAdapter);
        menuListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        menuListView.setSelection(0);
        menuListView.setOnItemClickListener(this);
    }

    /**
     * Creates the Settings list
     *
     * @return
     */
    public List<MenuData> getMenuList() {
        List<MenuData> menuDataList = new ArrayList<>();
        menuDataList.add(new MenuData("Account", R.drawable.ic_settings_account));
        menuDataList.add(new MenuData("Privacy", R.drawable.ic_settings_privacy));
        menuDataList.add(new MenuData("Notifications", R.drawable.ic_settings_notifications));
        menuDataList.add(new MenuData("Chats", R.drawable.ic_settings_chats));
        menuDataList.add(new MenuData("Contacts", R.drawable.ic_settings_contacts));
        menuDataList.add(new MenuData("About and help", R.drawable.ic_settings_help));
        return menuDataList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
