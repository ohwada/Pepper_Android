/**
 * Pepper controlled by Android
 * using Aldebaran Android SDK 
 * 2015-10-01 K.OHWADA
 */

package andorid.ohwada.jp.pepperimageremote;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.proxies.ALVideoDevice;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * MainActivity
 */
public class MainActivity extends Activity {

    // debug
    private static final String TAG = Constant.TAG;
    private static final boolean D = Constant.DEBUG;

    // photo
    private static final String PHOTO_DIR = "Pepper";

    // class object
    private RobotController mRobotController;
    private FileUtility mFileUtility;
    private MediaUtility mMediaUtility ;

    // View
    private EditText mEditTextIp;
    private ImageView mImageViewPhoto;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMediaUtility = new MediaUtility( this );
        mFileUtility = new FileUtility( PHOTO_DIR );
        initRobotController();
        // View
        mEditTextIp = (EditText) findViewById( R.id.EditText_ip );
        mEditTextIp.setText( mRobotController.getPrefAddr() );
        mImageViewPhoto = (ImageView) findViewById( R.id.ImageView_photo );
    }

   /**
     * initRobotController
     */
    private void initRobotController() {
        mRobotController = new RobotController( this );
        mRobotController.execEmbeddedTool();
        mRobotController.setOnChangedListener( 
            new RobotController.OnChangedListener() {
            @Override
            public void onConnectChanged( boolean isSuccess ) {
                // dummy
            }
            @Override
            public void onImageRemoteChanged( ByteBuffer buf, Bitmap bitmap ) {
                log_d( "onImageRemoteChanged " );
                procImageRemoteChanged( buf, bitmap );
            }
        });
    }

   /**
     * procImageRemoteChanged
     */
    private void procImageRemoteChanged( ByteBuffer buf, Bitmap bitmap ) {
        log_d( "procImageRemoteChanged " + buf + " " + bitmap );
        String name = mFileUtility.getName();
        if ( buf != null ) {
            mFileUtility.writeData( name, buf.array() );
        }
        if ( bitmap != null ) {
            mImageViewPhoto.setImageBitmap( bitmap );
            File file = mFileUtility.getJpegFile( name );
            mFileUtility.writeJpeg( file, bitmap );
            mMediaUtility.register( file );
            toast_short( R.string.toast_photo_saved );
        } else {
            toast_short( R.string.toast_get_image_remote_failed );
        }
    }

    /**
     * --- onClick Connect ---
     */
    public void onClickConnect( View view ) {
        log_d("onClickConnect");
        String ip = mEditTextIp.getText().toString().trim();
        mRobotController.connect( ip );
    }

    /**
     * --- onClickGetImageRemote ---
     */
    public void onClickGetImageRemote( View view ) {
        log_d("onClickGetImageRemote");
        mRobotController.getImageRemote();
    }

    /**
     * toast short
     */       
    private void toast_short( String str ) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    /**
     * toast short
     */       
    private void toast_short( int res_id ) {
        Toast.makeText(this, res_id, Toast.LENGTH_SHORT).show();
    }

    /**
     * log_d
     */ 
    private void log_d( String str ) {
        if (D) Log.d( TAG, str );
    }

}
