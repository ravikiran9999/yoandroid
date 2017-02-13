package com.yo.android.helpers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AlphabetAdapter;
import com.yo.android.crop.Bitmaps;
import com.yo.android.crop.MainImageCropActivity;
import com.yo.android.model.Contact;
import com.yo.android.model.FindPeople;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rajesh on 20/9/16.
 */
public class Helper {
    public static final String GALLERY_IMAGE_ITEM = "gallery_image_item";
    public static final String IS_FROM_CAMERA = "is_from_camera";
    public static final String IMAGE_PATH = "image_path";
    public static final int CROP_ACTIVITY = 1001;
    private static float density = 1;
    public static final int MATCH_PARENT = -1;
    public static final int WRAP_CONTENT = -2;
    public static final int MEDIA_DIR_IMAGE = 0;
    public static final int MEDIA_DIR_AUDIO = 1;
    public static final int MEDIA_DIR_VIDEO = 2;
    public static final int MEDIA_DIR_DOCUMENT = 3;
    public static final int MEDIA_DIR_CACHE = 4;
    private static HashMap<Integer, File> mediaDirs = null;
    public static Bitmap finalRotatedBitmap;
    public static boolean IS_FROM_CAMERA_BITMAP;

    public static void createNewContactWithPhoneNumber(Activity activity, String phoneNumber) {
        Intent i = new Intent(Intent.ACTION_INSERT);
        i.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
        i.setType(ContactsContract.Contacts.CONTENT_TYPE);
        if (Integer.valueOf(Build.VERSION.SDK) > 14) {
            i.putExtra("finishActivityOnSaveCompleted", true); // Fix for 4.0.3 +
        }
        activity.startActivity(i);

    }

    public static void addContactWithPhoneNumber(Activity activity, String phoneNumber) {

        Intent intentInsertEdit = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intentInsertEdit.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
        if (Integer.valueOf(Build.VERSION.SDK) > 14) {
            intentInsertEdit.putExtra("finishActivityOnSaveCompleted", true); // Fix for 4.0.3 +
        }
        intentInsertEdit.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        activity.startActivity(intentInsertEdit);
    }

    public static void displayIndex(Activity context, final ListView indexLayout, final List<Contact> contactList, final ListView listview) {
        final LinkedHashMap mapIndex = getIndexList(contactList);
        final List<String> indexList = new ArrayList<String>(mapIndex.keySet());
        AlphabetAdapter adapter = new AlphabetAdapter(context, indexList);
        indexLayout.setAdapter(adapter);

        indexLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    listview.setSelection(0);
                } else {
                    try {
                        listview.setSelection(((Integer) mapIndex.get(indexList.get(position).substring(0, 1))) + 1);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        listview.setSelection(((Integer) mapIndex.get(indexList.get(position).substring(0, 1))));

                    }
                }
            }
        });
    }

    public static void displayIndexTransferBalance(Activity context, final ListView indexLayout, final List<FindPeople> contactList, final ListView listview) {
        final LinkedHashMap mapIndex = getIndexListTransferBalance(contactList);
        final List<String> indexList = new ArrayList<String>(mapIndex.keySet());
        AlphabetAdapter adapter = new AlphabetAdapter(context, indexList);
        indexLayout.setAdapter(adapter);
        indexLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    listview.setSelection(0);
                } else {
                    try {
                        listview.setSelection(((Integer) mapIndex.get(indexList.get(position).substring(0, 1))) + 1);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        listview.setSelection(((Integer) mapIndex.get(indexList.get(position).substring(0, 1))));
                    }
                }
            }
        });
    }


    public static LinkedHashMap getIndexList(List<Contact> list) {
        LinkedHashMap mapIndex = new LinkedHashMap<String, Integer>();
        int i = -1;
        for (Contact contact : list) {
            String fruit = contact.getName();
            if (fruit != null && fruit.length() >= 1) {
                String index = fruit.substring(0, 1).toUpperCase();
                i = i + 1;

                if (mapIndex.get(index) == null) {
                    if (TextUtils.isDigitsOnly(index) || !index.matches(".*[a-zA-Z]+.*")) {
                        mapIndex.put("#", i);
                    } else {
                        mapIndex.put(index, i);
                    }
                }
            }
        }
        return mapIndex;
    }

    public static LinkedHashMap getIndexListTransferBalance(List<FindPeople> list) {
        LinkedHashMap mapIndex = new LinkedHashMap<String, Integer>();
        int i = -1;
        for (FindPeople contact : list) {
            String fruit = contact.getFirst_name();
            if (fruit != null && fruit.length() >= 1) {
                String index = fruit.substring(0, 1).toUpperCase();
                // Pattern p = Pattern.compile("^[a-zA-Z]");
                // Matcher m = p.matcher(index);
                // boolean b = m.matches();
                i = i + 1;
                // if (b) {
                if (mapIndex.get(index) == null) {
                    mapIndex.put(index, i);
                }
            }
            // }
        }
        return mapIndex;
    }


    public static void loadDirectly(final Context activity, final ImageView imageView, final File file) {
        File newFile = file;
        if (file != null && !file.exists()) {
            newFile = new File(Environment.getExternalStorageDirectory() + "/YO/YOImages/" + file.getName());
        }
        final File finalNewFile = newFile;
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(finalNewFile);
                    final Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    if (activity instanceof Activity) {
                        ((Activity) activity).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            //e.printStackTrace();
                        }
                    }
                }
            }
        }).start();

    }

    public static void setSelectedImage(Activity activity, String path, boolean isFromCam, Bitmap bitmap, boolean isFromCameraBitmap) {
        IS_FROM_CAMERA_BITMAP = isFromCameraBitmap;

        ExifInterface ei = null;
        try {
            ei = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = 0;
        if (ei != null) {
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
        }

        Bitmap rotatedBitmap = null;

        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
                rotatedBitmap = bitmap;
                break;

            default:
                rotatedBitmap = bitmap;
                break;
        }



        if(activity != null) {
            Intent intent = new Intent(activity, MainImageCropActivity.class);
            if(!isFromCameraBitmap) {
                intent.putExtra(GALLERY_IMAGE_ITEM, path);
                finalRotatedBitmap = null;
            }
            if(rotatedBitmap != null) {
                finalRotatedBitmap = rotatedBitmap;
            }
            if (isFromCam) {
                intent.putExtra(IS_FROM_CAMERA, IS_FROM_CAMERA);
            }
            activity.startActivityForResult(intent, CROP_ACTIVITY);
        } else {
            Log.d("Helper", "Activity is null");
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        if(source != null) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                    matrix, true);
        }
        return null;
    }

    public static void setSelectedImage(Fragment activity, String path, boolean isFromCam) {
        Intent intent = new Intent(activity.getActivity(), MainImageCropActivity.class);
        intent.putExtra(GALLERY_IMAGE_ITEM, path);
        if (isFromCam) {
            intent.putExtra(IS_FROM_CAMERA, IS_FROM_CAMERA);
        }
        activity.startActivityForResult(intent, CROP_ACTIVITY);
    }

    public static int dp(Context context, float value) {
        density = context.getResources().getDisplayMetrics().density;
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    private static int getSize(Context context, float size) {
        return (int) (size < 0 ? size : dp(context, size));
    }

    public static LinearLayout.LayoutParams createLinear(Context context, int width, int height, float weight, int gravity, int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getSize(context, width), getSize(context, height), weight);
        layoutParams.setMargins(dp(context, leftMargin), dp(context, topMargin), dp(context, rightMargin), dp(context, bottomMargin));
        layoutParams.gravity = gravity;
        return layoutParams;
    }

    public static LinearLayout.LayoutParams createLinear(Context context, int width, int height, float weight, int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getSize(context, width), getSize(context, height), weight);
        layoutParams.setMargins(dp(context, leftMargin), dp(context, topMargin), dp(context, rightMargin), dp(context, bottomMargin));
        return layoutParams;
    }

    public static LinearLayout.LayoutParams createLinear(Context context, int width, int height, int gravity, int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getSize(context, width), getSize(context, height));
        layoutParams.setMargins(dp(context, leftMargin), dp(context, topMargin), dp(context, rightMargin), dp(context, bottomMargin));
        layoutParams.gravity = gravity;
        return layoutParams;
    }

    public static RelativeLayout.LayoutParams createRelative(Context context, int width, int height, int gravity, int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(getSize(context, width), getSize(context, height));
        layoutParams.setMargins(dp(context, leftMargin), dp(context, topMargin), dp(context, rightMargin), dp(context, bottomMargin));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        return layoutParams;
    }

    public static LinearLayout.LayoutParams createLinear(Context context, int width, int height, float leftMargin, float topMargin, float rightMargin, float bottomMargin) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getSize(context, width), getSize(context, height));
        layoutParams.setMargins(dp(context, leftMargin), dp(context, topMargin), dp(context, rightMargin), dp(context, bottomMargin));
        return layoutParams;
    }

    public static LinearLayout.LayoutParams createLinear(Context context, int width, int height, float weight, int gravity) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getSize(context, width), getSize(context, height), weight);
        layoutParams.gravity = gravity;
        return layoutParams;
    }

    public static LinearLayout.LayoutParams createLinear(Context context, int width, int height, int gravity) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getSize(context, width), getSize(context, height));
        layoutParams.gravity = gravity;
        return layoutParams;
    }

    public static LinearLayout.LayoutParams createLinear(Context context, int width, int height, float weight) {
        return new LinearLayout.LayoutParams(getSize(context, width), getSize(context, height), weight);
    }

    public static LinearLayout.LayoutParams createLinear(Context context, int width, int height) {
        return new LinearLayout.LayoutParams(getSize(context, width), getSize(context, height));
    }

    public static File generatePicturePath(Context context) {
        try {
            File storageDir = getAlbumDir(context);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            if (storageDir != null) {
                return new File(storageDir, "IMG_" + timeStamp + ".jpg");
            }
        } catch (Exception e) {
            Log.w("tmessages", e);
        }
        return null;
    }

    public void setMediaDirs(HashMap<Integer, File> dirs) {
        mediaDirs = dirs;
    }

    public static File getDirectory(int type) {
        if (mediaDirs != null) {
            File dir = mediaDirs.get(type);
            if (dir == null && type != MEDIA_DIR_CACHE) {
                dir = mediaDirs.get(MEDIA_DIR_CACHE);
            }
            try {
                if (!dir.isDirectory()) {
                    dir.mkdirs();
                }
            } catch (Exception e) {
                //don't promt
            }
            return dir;
        }
        return null;

    }

    private static File getAlbumDir(Context context) {
        if (Build.VERSION.SDK_INT >= 23 && context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return getDirectory(MEDIA_DIR_CACHE);
        }
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YO");
            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    Log.d("Yo", "failed to create directory");
                    return null;
                }
            }
        } else {
            Log.d("Yo", "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    public void startPhotoSelectActivity(Activity activity) {
        try {
            Intent videoPickerIntent = new Intent();
            videoPickerIntent.setType("video/*");
            videoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
            videoPickerIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, (long) (1024 * 1024 * 1536));

            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            Intent chooserIntent = Intent.createChooser(photoPickerIntent, null);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{videoPickerIntent});

            activity.startActivityForResult(chooserIntent, 1);
        } catch (Exception e) {
            Log.w("tmessages", e);
        }
    }

    public static String getContactName(Context context, String phoneNumber) {
        String contactName = null;
        Cursor cursor = null;
        try {
            ContentResolver cr = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }


        } catch (RuntimeException e) {

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return contactName;
    }

    private static void scaleAndSaveImageInternal(Bitmap bitmap, int w, int h, float photoW, float photoH, float scaleFactor, int quality, boolean cache, boolean scaleAnyway) throws Exception {
        Bitmap scaledBitmap;
        String type;
        if (scaleFactor > 1 || scaleAnyway) {
            scaledBitmap = Bitmaps.createScaledBitmap(bitmap, w, h, true);
        } else {
            scaledBitmap = bitmap;
        }
        int width = scaledBitmap.getWidth();
        int height = scaledBitmap.getHeight();
        if (width <= 100 && height <= 100) {
            type = "s";
        } else if (width <= 320 && height <= 320) {
            type = "m";
        } else if (width <= 800 && height <= 800) {
            type = "x";
        } else if (width <= 1280 && height <= 1280) {
            type = "y";
        } else {
            type = "w";
        }
    }
}
