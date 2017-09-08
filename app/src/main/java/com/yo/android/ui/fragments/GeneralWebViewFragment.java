package com.yo.android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yo.android.R;


public class GeneralWebViewFragment extends WebViewFragment {

    public final static String KEY_URL = "key_url";

    public static GeneralWebViewFragment newInstance(Bundle args) {
        GeneralWebViewFragment fragment = new GeneralWebViewFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public GeneralWebViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        initUrlToLoad();

        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        return rootView;
    }

    private void initUrlToLoad() {
        Bundle args = getArguments();
        if (args != null) {
            urlToLoad = args.getString(KEY_URL);
            domainUrl = "Domain URL";
        }
    }

    @Override
    protected void initToolbar() {
        setHasOptionsMenu(true);
    }

    @Override
    protected void onWebViewInflated() {
    }

    @Override
    protected void onPageStartedCallback() {
    }

    @Override
    protected void onPageFinishedCallback() {
    }

    @Override
    protected boolean isCacheSettingEnabled() {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_single_cancel,menu);
    }

}