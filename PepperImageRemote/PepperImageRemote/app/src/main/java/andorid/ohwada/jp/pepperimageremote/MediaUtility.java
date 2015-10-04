/**
 * Pepper controlled by Android
 * 2015-10-01 K.OHWADA
 */

package andorid.ohwada.jp.pepperimageremote;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import java.io.File;

/**
 * MediaUtility
 */
public class MediaUtility {

    // debug
    private static final String TAG = Constant.TAG;
    private static final boolean D = Constant.DEBUG;

   private static final String[] MIME_TYPES = { "image/jpeg" };

    private Context mContext;

    /**
     * Constractor
     * @param Context context
     */
    public MediaUtility( Context context ) {
        mContext = context;
    }

    /**
     * register
     * @param File file
     */
    public void register( File file ) {
        log_d( "register " + file );
        register( file.getAbsolutePath() );
    }

    /**
     * register
     * @param String file
     */
    public void register( String file ) {
        log_d( "register " + file );
        String[] paths = { file };
        MediaScannerConnection.scanFile( mContext, paths, MIME_TYPES, mListener );
    }

    /**
     * OnScanCompletedListener
     */
    MediaScannerConnection.OnScanCompletedListener mListener = new MediaScannerConnection.OnScanCompletedListener() {
        @Override
        public void onScanCompleted( String path, Uri uri ) {
            log_d( "onScanCompleted " + path + " " + uri );
        }
    };

    /**
     * log_d
     */
    private void log_d( String str ) {
        if (D) Log.d(TAG, str);
    }

}
