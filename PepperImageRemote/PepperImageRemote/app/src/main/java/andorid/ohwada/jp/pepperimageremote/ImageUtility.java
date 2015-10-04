/**
 * Pepper controlled by Android
 * 2015-10-01 K.OHWADA
 */

package andorid.ohwada.jp.pepperimageremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * ImageUtility
 */
public class ImageUtility {

    // debug
    private static final String TAG = Constant.TAG;
    private static final boolean D = Constant.DEBUG;

    // YUV
    private static final int[] YUV_STRIDES = null;
    private static final int JPEG_RECT_LEFT = 0;
    private static final int JPEG_RECT_TOP = 0;
    private static final int JPEG_QUALITY = 80;
    private static final int JPEG_BYTE_OFFSET = 0;

    // RGB
    private static final byte RGB_ALPHA = (byte)0xff;

    /*
     * Constructor
     */ 
    public ImageUtility() {
        // dummy
    }

    /**
     * getBitmapFromYuv
     * @param ByteBuffer buf
     * @param int format
     * @param int width
     * @param int height
     * @return Bitmap
     */
    public Bitmap getBitmapFromYuv( ByteBuffer buf, int format, int width, int height ) {
        byte[] bytes_jpeg = yuvToJpeg( buf.array(), format, width, height );
        Bitmap bitmap = jpegToBitmap( bytes_jpeg );
        return bitmap;
    }

    /**
     * yuvToBitmap
     * @param byte[] bytes
     * @param int format : ImageFormat.NV21 or ImageFormat.YUY2
     * @param int width
     * @param int height
     * @return byte[]
     */
    private byte[] yuvToJpeg( byte[] bytes, int format, int width, int height ) {
        YuvImage image = new YuvImage( bytes, format, width, height, YUV_STRIDES );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Rect rect = new Rect( JPEG_RECT_LEFT, JPEG_RECT_TOP, width, height );
        image.compressToJpeg( rect, JPEG_QUALITY, baos );
        byte[] data = baos.toByteArray();
        return data;
    }

    /**
     * jpegToBitmap
     * @param byte[] bytes
     * @return Bitmap
     */
    private Bitmap jpegToBitmap( byte[] bytes ) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeByteArray( bytes, JPEG_BYTE_OFFSET, bytes.length, options );
        return bitmap;
    }

    /**
     * getBitmapFromRgb
     * @param ByteBuffer buf_rgb
     * @param int width
     * @param int height
     * @return Bitmap
     */
    public Bitmap getBitmapFromRgb( ByteBuffer buf_rgb, int width, int height ) {
        ByteBuffer buf_argb = rbgToArgb( buf_rgb, width * height );
        Bitmap bitmap = argbToBitmap( buf_argb, width, height );
        return bitmap;
    }

    /**
     * rgbToBitmap
     * @param ByteBuffer buf
     * @param int width
     * @param int height
     * @return Bitmap
     */
    private Bitmap argbToBitmap( ByteBuffer buf, int width, int height ) {
        buf.position(0);
        Bitmap bitmap = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
        bitmap.copyPixelsFromBuffer( buf );
        return bitmap;
    }

    /**
     * rbgToArgb
     * @param ByteBuffer buf
     * @param int size
     * @return ByteBuffer
     */
    private ByteBuffer rbgToArgb( ByteBuffer buf, int size ) {
        ByteBuffer buf_new = ByteBuffer.allocate( 4*size );
        for ( int i=0; i<size; i++ ) {
            buf_new.put( RGB_ALPHA );  // A
            buf_new.put( buf.get( 3*i + 0 ) );  // R
            buf_new.put( buf.get( 3*i + 1 ) );  // G
            buf_new.put( buf.get( 3*i + 2 ) );  // B
        }
        return buf_new;
    }

    /**
     * log_d
     */ 
    private void log_d( String str ) {
        if (D) Log.d( TAG, str );
    }

}
