package ng.codehaven.bambam.ui.activities;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;

import ng.codehaven.bambam.R;
import ng.codehaven.bambam.ui.BaseActivity;

public class ChooseMp3Activity extends BaseActivity {

    @Override
    public int getActivityResourceId() {
        return R.layout.activity_choose_mp3;
    }

    @Override
    public boolean hasToolBar() {
        return false;
    }

    @Override
    public Toolbar getToolBar() {
        return null;
    }

    @Override
    public int getMenuResourceId() {
        return R.menu.menu_choose_mp3;
    }

    @Override
    public boolean enableBack() {
        return true;
    }

    Cursor cursor;
    HashMap<String, String> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver contentResolver = getContentResolver();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        cursor = contentResolver.query(uri, null, null, null, null);

        map = new HashMap<>();

        if (cursor == null) {
            // query failed, handle error.
        } else if (!cursor.moveToFirst()) {
            // no media on the device
        } else {
            int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            do {
                long thisId = cursor.getLong(idColumn);
                String thisTitle = cursor.getString(titleColumn);
                String thisArtist = cursor.getString(artistColumn);
                // ...process entry...
                map.put(thisTitle, thisArtist);
            } while (cursor.moveToNext());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursor != null && !cursor.isClosed()){
            cursor.close();
        }
    }
}
