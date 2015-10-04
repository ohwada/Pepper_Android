/**
 * Pepper controlled by Android
 * 2015-10-01 K.OHWADA
 */

package andorid.ohwada.jp.pepperimageremote;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FileUtility
 */
public class FileUtility {

    // debug
    private static final String TAG = Constant.TAG;
    private static final boolean D = Constant.DEBUG;

    private static final String CHAR_DOT = ".";
    private static final String EXT_JPEG = "jpg";
    private static final String EXT_DATA = "dat";
    private static final int JPEG_QUALITY = 100;

    private SimpleDateFormat mFormat = new SimpleDateFormat( "yyyyMMddHHmmss" );

    private File mDir;

    /**
     * Constractor
     */
    public FileUtility( String sub ) {
        String path = Environment.getExternalStorageDirectory() + "/" + sub;
        File dir = new File( path );
        if ( !dir.exists() ) {
            dir.mkdirs();
        }
        mDir = dir;
    }

    /**
     * writeJpeg
     * @param String name
     * @param Bitmap bitmap
     */
    public void writeJpeg( String name, Bitmap bitmap ) {
        File file = getFile( name, EXT_JPEG );
        writeJpeg( file, bitmap );
    }

    /**
     * writeData
     * @param String name
     * @param byte[] bytes
     */
    public void writeData( String name, byte[] bytes ) {
        File file = getFile( name, EXT_DATA );
        writeData( file, bytes );
    }

    /**
     * writeJpeg
     * @param File file
     * @param Bitmap bitmap
     */
    public void writeJpeg( File file, Bitmap bitmap ) {
        log_d( "writeJpeg " + file );
        try {
            FileOutputStream fos = new FileOutputStream( file );
            bitmap.compress( Bitmap.CompressFormat.JPEG,  JPEG_QUALITY, fos );
            fos.close();
        } catch (Exception e) {
            if (D) e.printStackTrace();
        }
    }

    /**
     * writeData
     * @param File file
     * @param Bitmap bitmap
     */
    public void writeData( File file, byte[] bytes ) {
        log_d( "writeData " + file );
        try {
            FileOutputStream fos = new FileOutputStream( file );
            BufferedOutputStream bos = new BufferedOutputStream( fos );
            bos.write( bytes );
            bos.flush();
            bos.close();
        } catch(Exception e) {
            if (D) e.printStackTrace();
        }
    }

    /**
     * getJpegFile
     * @param String name
     * @return File
     */
    public File getJpegFile( String name ) {
        return getFile( name, EXT_JPEG );
    }

    /**
     * getDataFile
     * @param String name
     * @return File
     */
    public File getDataFile( String name ) {
        return getFile( name, EXT_DATA );
    }

    /**
     * getFile
     * @param String name
     * @param String ext
     * @return File
     */
    public File getFile( String name, String ext ) {
        String filename = name + CHAR_DOT + ext;
        File file = new File( mDir, filename );
        return file;
    }

    /**
     * getName
     * @return String
     * current datetime : yyyyMMddHHmmss
     */
    public String getName() {
        String name = mFormat.format( new Date() );
        return name;
    }

    /**
     * log_d
     */
    private void log_d( String str ) {
        if (D) Log.d(TAG, str);
    }

}
