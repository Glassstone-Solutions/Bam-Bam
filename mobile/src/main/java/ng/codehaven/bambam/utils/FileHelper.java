package ng.codehaven.bambam.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.util.Pair;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ng.codehaven.bambam.Constants;

public class FileHelper {
    public static final String TAG = FileHelper.class.getSimpleName();

    public static final int SHORT_SIDE_TARGET = 1280;

    public static InputStream mInputStream = null;

    public static byte[] getByteArrayFromFile(Context context, Uri uri) {

        byte[] fileBytes = null;
        InputStream inStream = null;
        ByteArrayOutputStream outStream = null;

        if (uri.getScheme().equals("content")) {
            try {
                inStream = context.getContentResolver().openInputStream(uri);
                outStream = new ByteArrayOutputStream();

                byte[] bytesFromFile = new byte[1024 * 1024]; // buffer size (1 MB)
                int bytesRead = inStream.read(bytesFromFile);
                while (bytesRead != -1) {
                    outStream.write(bytesFromFile, 0, bytesRead);
                    bytesRead = inStream.read(bytesFromFile);
                }

                fileBytes = outStream.toByteArray();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                try {
                    if (inStream != null) {
                        inStream.close();
                        assert outStream != null;
                        outStream.close();
                    }
                } catch (IOException e) { /*( Intentionally blank */ }
            }
        } else {
            try {
                File file = new File(uri.getPath());
                FileInputStream fileInput = new FileInputStream(file);
                fileBytes = IOUtils.toByteArray(fileInput);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        return fileBytes;
    }

    public static String getFileName(Context context, Uri uri, String fileType) {
        String fileName = "uploaded_file.";

        switch (fileType) {
            case Constants.TYPE_IMAGE:
                fileName += "png";
                break;
            case Constants.TYPE_MP3:
                fileName += "mp3";
                break;
            default:
                // For Audio, we want to get the actual file extension
                if (uri.getScheme().equals("content")) {
                    // do it using the mime type
                    String mimeType = context.getContentResolver().getType(uri);
                    int slashIndex = mimeType.indexOf("/");
                    String fileExtension = mimeType.substring(slashIndex + 1);
                    fileName += fileExtension;
                } else {
                    fileName = uri.getLastPathSegment();
                }
                break;
        }

        return fileName;
    }

    public static String getFacebookPicture(String userID) {
        return String.format(
                "https://graph.facebook.com/%s/picture?type=large",
                userID);
    }

    /*
	 * Call this static method to resize an image to a specified width and height.
	 *
	 * @param targetWidth  The width to resize to.
	 * @param targetHeight The height to resize to.
	 * @returns 		   The resized image as a Bitmap.
	 */
    public static Bitmap resizeImage(byte[] imageData, int targetWidth, int targetHeight) {
        // Use BitmapFactory to decode the image
        BitmapFactory.Options options = new BitmapFactory.Options();

        // inSampleSize is used to sample smaller versions of the image
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);

        // Decode bitmap with inSampleSize and target dimensions set
        options.inJustDecodeBounds = false;

        Bitmap reducedBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);

        return Bitmap.createScaledBitmap(reducedBitmap, targetWidth, targetHeight, false);
    }

    public static Bitmap resizeImageMaintainAspectRatio(byte[] imageData, int shorterSideTarget) {
        Pair<Integer, Integer> dimensions = getDimensions(imageData);

        // Determine the aspect ratio (width/height) of the image
        int imageWidth = dimensions.first;
        int imageHeight = dimensions.second;
        float ratio = (float) dimensions.first / dimensions.second;

        int targetWidth;
        int targetHeight;

        // Determine portrait or landscape
        if (imageWidth > imageHeight) {
            // Landscape image. ratio (width/height) is > 1
            targetHeight = shorterSideTarget;
            targetWidth = Math.round(shorterSideTarget * ratio);
        }
        else {
            // Portrait image. ratio (width/height) is < 1
            targetWidth = shorterSideTarget;
            targetHeight = Math.round(shorterSideTarget / ratio);
        }

        return resizeImage(imageData, targetWidth, targetHeight);
    }

    public static Pair<Integer, Integer> getDimensions(byte[] imageData) {
        // Use BitmapFactory to decode the image
        BitmapFactory.Options options = new BitmapFactory.Options();

        // Only decode the bounds of the image, not the whole image, to get the dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);

        return new Pair<Integer, Integer>(options.outWidth, options.outHeight);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();

        return state.equals(Environment.MEDIA_MOUNTED);
    }



    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }



}
