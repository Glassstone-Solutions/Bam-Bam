package ng.codehaven.bambam.services;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.models.Track;
import ng.codehaven.bambam.utils.FileHelper;
import ng.codehaven.bambam.utils.GetTunesHelper;
import ng.codehaven.cloudconvert.Process;

public class UploadTuneService extends Service {
    public static final String S3_KEY_DONE_ACTION = UploadArtService.class.getSimpleName() + ".donebroadcast";
    public static final String S3_KEY_PROGRESS_ACTION = UploadArtService.class.getSimpleName() + ".progressbroadcast";
    public static final int FILE_SIZE_LIMIT = 1024 * 1024 * 10; // 10MB

    private ParseObject mTrack;
    private boolean isTrackDone, isArtDone;

    private int artProgress = 0;
    private int tuneProgress = 0;
    private int notification_id = 1;

    private String ext;

    private NotificationCompat.Builder builder;
    private NotificationManager nm;

    public UploadTuneService() {
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle mBundle = new Bundle(intent.getBundleExtra("trackBundle"));
        Log.w("BUNDLE", "Service started with => " + mBundle.getString("trackPath"));
        File f = new File(mBundle.getString("trackPath"));
        Uri tUri = Uri.fromFile(f);

        if (tUri.getScheme().equals("content")) {
            // do it using the mime type
            String mimeType = this.getContentResolver().getType(tUri);
            int slashIndex = mimeType.indexOf("/");
            ext = mimeType.substring(slashIndex + 1);
        } else {
            ext = "mp3";
        }

        ParseACL acl = new ParseACL(ParseUser.getCurrentUser());
        acl.setPublicReadAccess(true);
        acl.setPublicWriteAccess(false);

        mTrack = new ParseObject("Tunes");
        mTrack.put("title", mBundle.getString("fTrackTitle"));
        mTrack.put("desc", mBundle.getString("fTrackDesc"));
        mTrack.put("forSale", false);
        mTrack.put("owner", ParseUser.getCurrentUser());

        mTrack.setACL(acl);

        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Uploading").setContentText("Uploading song");
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        final int tp = artProgress + tuneProgress;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (tp < 200) {
                    builder.setProgress(200, tp, false);
                    nm.notify(notification_id, builder.build());
                }
            }
        });


        tUri = Uri.fromFile(f);

        int fileSize = 0;
        final InputStream inputStream = null;

        fileSize = getFileSize(tUri, fileSize, inputStream);

        if (fileSize >= FILE_SIZE_LIMIT) {
            Toast.makeText(this, R.string.error_file_size_too_large, Toast.LENGTH_LONG).show();
            stopSelf();
        } else {

            Toast.makeText(this, R.string.upload_started, Toast.LENGTH_LONG).show();

            final byte[] fByte = FileHelper.getByteArrayFromFile(this.getApplicationContext(), tUri);
            Uri uri = Uri.parse(mBundle.getString("artURI"));
            final byte[] mArtFileBytes = FileHelper.getByteArrayFromFile(UploadTuneService.this, uri);
            SaveTrack(fByte, mArtFileBytes);


        }
        return START_REDELIVER_INTENT;
    }

    private void startConvertProcess(Process process, ParseObject mTrack) {
        builder.setProgress(0, 0, false);
        builder.setContentText("Upload complete");
        nm.notify(notification_id, builder.build());

        Realm realm = Realm.getInstance(UploadTuneService.this);
        Track t = GetTunesHelper.getTrack(ParseUser.getCurrentUser(), mTrack, false);
        realm.beginTransaction();
        Track trackRealm = realm.copyToRealm(t);
        realm.commitTransaction();

        Log.e("PROCESS", process.getUrl());

        sendBroadcast(new Intent(S3_KEY_DONE_ACTION));

        stopSelf();

    }

    private void SaveTrack(byte[] fByte, final byte[] mArtFileBytes) {
        final ParseFile tParseFile = new ParseFile(fByte);

        tParseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    mTrack.put("track", tParseFile);
                    SaveArt(mArtFileBytes);
                }
            }
        }, new ProgressCallback() {
            @Override
            public void done(Integer integer) {
                Log.e("TUNE-Progress", String.valueOf(integer));
                if (integer == 100) {
                    isTrackDone = true;
                    tuneProgress = integer;
                }
            }
        });
    }

    private void SaveArt(byte[] mArtFileBytes) {

        final ParseFile aParseFile = new ParseFile(mArtFileBytes);
        aParseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    mTrack.put("art", aParseFile);
                    mTrack.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.e("SAVED", "Object saved");

                                builder.setProgress(0, 0, false);
                                builder.setContentText("Upload complete");
                                nm.notify(notification_id, builder.build());

                                Realm realm = Realm.getInstance(UploadTuneService.this);
                                Track t = GetTunesHelper.getTrack(ParseUser.getCurrentUser(), mTrack, false);
                                realm.beginTransaction();
                                Track trackRealm = realm.copyToRealm(t);
                                realm.commitTransaction();

                                sendBroadcast(new Intent(S3_KEY_DONE_ACTION));
                            }
                            stopSelf();
                        }
                    });
                }
            }
        }, new ProgressCallback() {
            @Override
            public void done(Integer integer) {
                Log.e("ART-Progress", String.valueOf(integer));
                if (integer == 100) {
                    isArtDone = true;
                    artProgress = integer;

                    sendBroadcast(new Intent(S3_KEY_PROGRESS_ACTION));

                }
            }
        });
    }

    private int getFileSize(Uri tUri, int fileSize, InputStream inputStream) {
        try {
            inputStream = getContentResolver().openInputStream(tUri);
            fileSize = inputStream.available();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileSize;
    }

    /*
     * Gets the file path of the given Uri.
     */
    @SuppressLint("NewApi")
    private String getPath(Uri uri) {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor;
            try {
                cursor = getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    String cc = cursor.getString(column_index);
                    cursor.close();
                    return cc;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
