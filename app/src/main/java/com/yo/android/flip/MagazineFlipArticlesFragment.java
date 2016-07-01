package com.yo.android.flip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aphidmobile.flip.FlipViewController;
import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.IO;
import com.aphidmobile.utils.UI;
import com.yo.android.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by creatives on 6/30/2016.
 */
public class MagazineFlipArticlesFragment extends Fragment {

    private FlipViewController flipView;
    private static MagazineTopicsSelectionFragment magazineTopicsSelectionFragment;
    private static List<Travels.Data> articlesList = new ArrayList<Travels.Data>();
    private MyReceiver myReceiver;

    public MagazineFlipArticlesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_magazines, container, false);
        IntentFilter filter = new IntentFilter("com.yo.magazine.SendBroadcast");
        myReceiver = new MyReceiver();
        getActivity().getApplicationContext().registerReceiver(myReceiver, filter);

        magazineTopicsSelectionFragment = (MagazineTopicsSelectionFragment)getFragmentManager().findFragmentById(R.id.topics_selection_fragment);
        /*for(int i=0; i<Travels.getImgDescriptions().size(); i++) {
            if (magazineTopicsSelectionFragment.getSelectedTopic().equals(Travels.getImgDescriptions().get(i).getTopicName())) {
                //articlesList = new ArrayList<Travels.Data>();
                articlesList.add(Travels.getImgDescriptions().get(i));
            }
        }
        flipView = new FlipViewController(getActivity());
        flipView.setAdapter(new MyBaseAdapter(getActivity(), flipView));*/
        loadArticles(magazineTopicsSelectionFragment.getSelectedTopic());
        flipView = new FlipViewController(getActivity());
        flipView.setAdapter(new MyBaseAdapter(getActivity(), flipView));

        return flipView;
    }

    public void loadArticles(String selectedTopic) {
        for(int i=0; i<Travels.getImgDescriptions().size(); i++) {
            //if (magazineTopicsSelectionFragment.getSelectedTopic().equals(Travels.getImgDescriptions().get(i).getTopicName())) {
            if (selectedTopic.equals(Travels.getImgDescriptions().get(i).getTopicName())) {
                //articlesList = new ArrayList<Travels.Data>();
                articlesList.add(Travels.getImgDescriptions().get(i));
            }
        }
        MyBaseAdapter adapter = new MyBaseAdapter(getActivity(),flipView);
        adapter.notifyDataSetChanged();
        /*flipView = new FlipViewController(getActivity());
        flipView.setAdapter(new MyBaseAdapter(getActivity(), flipView));*/
    }

    public void onDestroyView() {
        super.onDestroyView();
        getActivity().getApplicationContext().unregisterReceiver(myReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        flipView.onResume();
    }

    public void onPause() {
        super.onPause();
        flipView.onPause();
    }

    private static class MyBaseAdapter extends BaseAdapter {

        private FlipViewController controller;

        private Context context;

        private LayoutInflater inflater;

        private Bitmap placeholderBitmap;

        private MyBaseAdapter(Context context, FlipViewController controller) {
            inflater = LayoutInflater.from(context);
            this.context = context;
            this.controller = controller;

            //Use a system resource as the placeholder
            placeholderBitmap =
                    BitmapFactory.decodeResource(context.getResources(), android.R.drawable.dark_header);
        }

        @Override
        public int getCount() {
            return articlesList.size();
            //return Travels.getImgDescriptions().size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View layout = convertView;
            if (convertView == null) {
                layout = inflater.inflate(R.layout.magazine_flip_layout, null);
            }

            final Travels.Data data = Travels.getImgDescriptions().get(position);
            if(magazineTopicsSelectionFragment.getSelectedTopic().equals(data.getTopicName())) {
                //articlesList = new ArrayList<Travels.Data>();
                articlesList.add(data);

                UI
                        .<TextView>findViewById(layout, R.id.tv_category_name)
                        .setText(AphidLog.format("%s", data.getTopicName()));

                UI
                        .<TextView>findViewById(layout, R.id.tv_article_title)
                        .setText(AphidLog.format("%s", data.getTitle()));

                UI
                        .<TextView>findViewById(layout, R.id.tv_article_short_desc)
                        .setText(Html.fromHtml(data.getDescription()));

           /* UI
                    .<Button>findViewById(layout, R.id.wikipedia)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(data.getLink())
                            );
                            inflater.getContext().startActivity(intent);
                        }
                    });
*/
                ImageView photoView = UI.findViewById(layout, R.id.photo);
                //Use an async task to load the bitmap
                boolean needReload = true;
                AsyncImageTask previousTask = AsyncDrawable.getTask(photoView);
                if (previousTask != null) {
                    //check if the convertView happens to be previously used
                    if (previousTask.getPageIndex() == position && previousTask.getImageName()
                            .equals(data.getImageFilename())) {
                        needReload = false;
                    } else {
                        previousTask.cancel(true);
                    }
                }

                if (needReload) {
                    AsyncImageTask
                            task =
                            new AsyncImageTask(layout.getContext().getAssets(), photoView, controller, position,
                                    data.getImageFilename());
                    photoView
                            .setImageDrawable(new AsyncDrawable(context.getResources(), placeholderBitmap, task));

                    task.execute();
                }
            }


            return layout;
        }
    }

    /**
     * Borrowed from the official BitmapFun tutorial: http://developer.android.com/training/displaying-bitmaps/index.html
     */
    private static final class AsyncDrawable extends BitmapDrawable {

        private final WeakReference<AsyncImageTask> taskRef;

        public AsyncDrawable(Resources res, Bitmap bitmap, AsyncImageTask task) {
            super(res, bitmap);
            this.taskRef = new WeakReference<AsyncImageTask>(task);
        }

        public static AsyncImageTask getTask(ImageView imageView) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                return ((AsyncDrawable) drawable).taskRef.get();
            }

            return null;
        }
    }

    private static final class AsyncImageTask extends AsyncTask<Void, Void, Bitmap> {

        private static final Random RANDOM = new Random();

        private final AssetManager assetManager;

        private final WeakReference<ImageView> imageViewRef;
        private final WeakReference<FlipViewController> controllerRef;
        private final int pageIndex;
        private final String imageName;

        public AsyncImageTask(AssetManager assetManager, ImageView imageView,
                              FlipViewController controller, int pageIndex, String imageName) {
            this.assetManager = assetManager;
            imageViewRef = new WeakReference<ImageView>(imageView);
            controllerRef = new WeakReference<FlipViewController>(controller);
            this.pageIndex = pageIndex;
            this.imageName = imageName;
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public String getImageName() {
            return imageName;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                //wait for a random time
                Thread.sleep(500 + RANDOM.nextInt(2000));
            } catch (InterruptedException e) {
                Log.e("TAG", "doInBackground", e);
            }

            return IO.readBitmap(assetManager, imageName);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                return;
            }

            ImageView imageView = imageViewRef.get();
            //the imageView can be reused for another page, so it's necessary to check its consistence
            if (imageView != null && AsyncDrawable.getTask(imageView) == this) {
                imageView.setImageBitmap(bitmap);
                FlipViewController controller = controllerRef.get();
                if (controller != null) {
                    controller.refreshPage(pageIndex);
                }
            }
        }
    }

    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Toast.makeText(context, "Broadcast Intent Detected." + intent.getStringExtra("SelectedTopic"),
                    Toast.LENGTH_LONG).show();
            String selectedTopic = intent.getStringExtra("SelectedTopic");
            loadArticles(selectedTopic);
        }
    }


    }
