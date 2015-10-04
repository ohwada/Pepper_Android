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

import com.aldebaran.qi.EmbeddedTools;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.proxies.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * RobotController
 * http://doc.aldebaran.com/2-1/naoqi/vision/alvideodevice.html
 * http://doc.aldebaran.com/2-1/family/robots/video_robot.html
 */
public class RobotController {

    // debug
    private static final String TAG = Constant.TAG;
    private static final boolean D = Constant.DEBUG;

    // resolution
    private static final int RESOLUTION_QQVGA = 0;  // 160 x 120
    private static final int RESOLUTION_QVGA = 1;   // 320 x 240
    private static final int RESOLUTION_VGA = 2;    // 640 x 480

    // color space
    // Buffer only contains the Y (luma component) equivalent to one unsigned char
    private static final int COLOR_SPACE_Y = 0;
    // YUV422 : Native format, 0xY’Y’VVYYUU equivalent to four unsigned char for two pixels. With Y luma for pixel n, Y’ luma for pixel n+1, and U and V are the average chrominance value of both pixels.
    private static final int COLOR_SPACE_YUV422 = 9;
    // YUV : Buffer contains triplet on the format 0xVVUUYY, equivalent to three unsigned char
    private static final int COLOR_SPACE_YUV = 10;
    // Buffer contains triplet on the format 0xBBGGRR, equivalent to three unsigned char
    private static final int COLOR_SPACE_RGB = 11;
    // Buffer contains triplet on the format 0xYYSSHH, equivalent to three unsigned cha
    private static final int COLOR_SPACE_HSY = 12;
    // Buffer contains triplet on the format 0xRRGGBB, equivalent to three unsigned char
    private static final int COLOR_SPACE_BGR = 13;
    private static final int COLOR_SPACE = COLOR_SPACE_YUV422;

    // frames per second
    private static final int FPS_5 = 5;
    private static final int FPS_10 = 10;
    private static final int FPS_15 = 15;
    private static final int FPS_30 = 30;

    // ImageFormat.NV21 or ImageFormat.YUY2
    // NV21 : YCrCb format used for images, which uses the NV21 encoding format.
    // YUY2 : YCbCr format used for images, which uses YUYV (YUY2) encoding format.
    private int YUV_FORMAT = ImageFormat.YUY2;

    private static final int IMAGE_WAIT = 10000;  // 10 sec

    // char
    private static final String LF = "\n";

    // SharedPreferences
    private static final String KEY_IP = "key_ip";

    // connection
    private static final String IP_PORT = "9559";
    private static final String IP_DEFAULT = "192.168.1.1";
    private static final String IP_PATTERN = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

    // app command
    private static final int CMD_CONNECT = 1;
    private static final int CMD_GET_IMAGE_REMOTE = 2;

    private static final int WHAT_TOAST = 1;
    private static final int WHAT_CONNECT_SUCCESS = 2;
    private static final int WHAT_CONNECT_FAIL = 3;
    private static final int WHAT_GET_IMAGE_REMOTE = 4;

    // image remote
    private static final String GVM_NAME = "android_client";

    // class object
    private Context mContext;
    private SharedPreferences mPreferences;
    private ImageUtility mImageUtility;

    // Pepper API
    private Session mQiSession;
    private ALVideoDevice mALVideoDevice;
    private ImageRemoteResult mResult;

    private String mAddr = "";
    private String mToast = "";
    private Bitmap mBitmap;
    private ByteBuffer mByteBuffer;

    // callback 
    private OnChangedListener mListener;  

    /*
     * callback interface
     */    
    public interface OnChangedListener {
        void onConnectChanged( boolean isSuccess );
        void onImageRemoteChanged( ByteBuffer buf, Bitmap bitmap );
    }

    /*
     * callback
     */ 
    public void setOnChangedListener( OnChangedListener listener ) {
        mListener = listener;
    }

    /*
     * Constructor
     */ 
    public RobotController( Context context ) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mImageUtility = new ImageUtility();
    }

    /*
     * getPrefAddr
     * @return String
     */ 
    public String getPrefAddr() {
        return mPreferences.getString( KEY_IP, IP_DEFAULT );
    }

    /*
     * serPrefAddr
     * @param String addr
     */ 
    private void serPrefAddr( String addr ) {
        mPreferences.edit().putString( KEY_IP, addr ).apply();
    }

    /*
     * execEmbeddedTool
     */ 
    public void execEmbeddedTool() {
        EmbeddedTools tools = new EmbeddedTools();
        File dir = mContext.getCacheDir();
        log_d( "Extracting libraries in " + dir.getAbsolutePath() );
        tools.overrideTempDirectory(dir);
        tools.loadEmbeddedLibraries();
    }

    /**
     * connect
     * @param String addr
     */
    public void connect( String addr ) {
        log_d( "connect " + addr );
        if ( "".equals(addr) ) {
            toast_short( R.string.toast_enter_ip );
            return;
        } else if ( !addr.matches(IP_PATTERN) ) {
            toast_short( R.string.toast_enter_correct );
            return;
        }
        mAddr = addr;
        startThread( CMD_CONNECT );
    }

    /**
     * isConnect
     * @return boolean
     */
    public boolean isConnect() {
        return robotIsConnected();
    }

    /*
     * getImageRemote
     */ 
    public void getImageRemote() {
        procThread( CMD_GET_IMAGE_REMOTE ); 
    }

    /**
     * procThread
     */
    private void procThread( int cmd ) {
        log_d("procThread " + cmd );
        if ( !robotIsConnected() ) {
            toast_short( R.string.toast_not_connected );
            return;
        }
        startThread( cmd );
    }

    /**
     * startThread
     */
    private void startThread( int _cmd ) {
        log_d("startThread " + _cmd );
        final int cmd = _cmd;
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                procRobot( cmd );
            }
        });
        thread.start();
    }

// --- robot ---
    /**
     * procRobot
     * @param int cmd
     */
    private void procRobot( int cmd ) {
        switch( cmd ) {
             case CMD_CONNECT:
                boolean ret1 = robotConnect( mAddr );
                if ( ret1 ) {
                    serPrefAddr( mAddr );	
                    sendMessage( WHAT_CONNECT_SUCCESS );
                } else {
                    sendMessage( WHAT_CONNECT_FAIL );
                }
                break;
            case CMD_GET_IMAGE_REMOTE:
                getImageRemoteBitmap();
                sendMessage( WHAT_GET_IMAGE_REMOTE );
                break; 
        }
    }

    /**
     * robotIsConnected
     */
    private boolean robotIsConnected() {
        if (( mQiSession != null )&&( mQiSession.isConnected() )) {
            return true;
        }
        return false;
    }

    /**
     * robot Connect
     * @param String addr
     */
    private boolean robotConnect( String addr ) {
        String ip = "tcp://" + addr + ":" + IP_PORT;
        log_d( "robotConnect " + ip );
        mQiSession = new Session();
        try {
            mQiSession.connect(ip).get();
        } catch (Exception e) {
            if (D) e.printStackTrace();
            return false;
        }
        try {
            mALVideoDevice = new ALVideoDevice( mQiSession );
        } catch (Exception e) {
            if (D) e.printStackTrace();
        }
        return true;
    }

    /**
     * getImageRemoteBitmap
     */
    private void getImageRemoteBitmap() {
        mByteBuffer = null;
        mBitmap = null;
        ImageRemoteResult result = null;
        String client = robotSubscribe( GVM_NAME, RESOLUTION_VGA, COLOR_SPACE, FPS_5 );
        if (( client != null )&&( client.length() > 0 )) {
            result = robotGetImageRemote( client );
            // WIN DEATH !
            // sleep( IMAGE_WAIT );
            robotUnsubscribe( client );
        }
        if (( result != null )&&( result.buf != null )&&( result.buf.limit() > 0 )) {
            mByteBuffer = result.buf;
            mBitmap = mImageUtility.getBitmapFromYuv( mByteBuffer, YUV_FORMAT, result.width, result.height );
            // mBitmap = mImageUtility.getBitmapFromRgb( mByteBuffer, result.width, result.height );
        }
    }

    /**
     * robotSubscribe
     * @param gvmName - Name of the subscribing G.V.M.
     * @param resolution - Resolution requested. { 0 = kQQVGA, 1 = kQVGA, 2 = kVGA }
     * @param rcolorSpace - Colorspace requested. { 0 = kYuv, 9 = kYUV422, 10 = kYUV, 11 = kRGB, 12 = kHSY, 13 = kBGR }
     * @param fps - Fps (frames per second) requested. { 5, 10, 15, 30 }
     * @return String
     */
    private String robotSubscribe( String gvmName, int resolution, int colorSpace, int fps ) {
        log_d("robotSubscribe " + gvmName + " " + resolution + " " + colorSpace + " " + fps);
        String str = "";
        try {
            str = mALVideoDevice.subscribe( gvmName, resolution, colorSpace, fps );
        } catch (Exception e) {
            if (D) e.printStackTrace();
            sendToast( e, R.string.toast_video_failed );
        }
        log_d( "subscribe " + str );
        return str;
    }

    /**
     * robotUnsubscribe
     * @param String name
     */
    private void  robotUnsubscribe( String name ) {
        log_d( "robotUnsubscribe " + name );
        try {			
            mALVideoDevice.unsubscribe(name);
        } catch (Exception e) {
            if (D) e.printStackTrace();
            sendToast( e, R.string.toast_video_failed );
        }
    }

    /**
     * robotGetImageRemote
     * @param String name
     * @return ImageRemoteResult
     */
    private ImageRemoteResult robotGetImageRemote( String name ) {
        log_d("robotGetImageRemote " + name);
        ImageRemoteResult result = null;
        List<Object> list = null;
        try {
            list = (List<Object>) mALVideoDevice.getImageRemote( name );
        } catch (Exception e) {
            if (D) e.printStackTrace();
            sendToast( e, R.string.toast_video_failed );
        }
        log_d("getImageRemote " + list);
        if (( list != null )&&( list.size() > 0 )) {
            result = new ImageRemoteResult( list );
        }
        return result;
    }

    /**
     * sendToast
     */
    private void sendToast( Exception e, int res_id) {
        String str = getString(res_id);
        sendToast( str+ LF + e.getMessage() );
    }

    /**
     * sendToast
     */
    private void sendToast( int res_id ) {
        String str = getString(res_id);
        sendToast( str );
    }
 
    /**
     * sendToast
     */
    private void sendToast( String str ) {
        mToast = str;
        sendMessage( WHAT_TOAST );
    }

    /**
     * sendMessage
     */
    private void sendMessage( int what ) {
        sendMessage( what, 0, 0 );
    }

    /*
     * sendMessage
     */ 
    private void sendMessage( int what, int arg1, int arg2 ) {
        mHandler.obtainMessage( what, arg1, arg2 ).sendToTarget();
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage( Message msg ) {
            switch (msg.what) {
                case WHAT_TOAST:
                    toast_short( mToast );
                    break; 
               case WHAT_CONNECT_SUCCESS:
                    toast_short( R.string.toast_connected );
                    notifyConnect( true );
                    break;
               case WHAT_CONNECT_FAIL:
                    toast_short( R.string.toast_connect_failed );
                    notifyConnect( false );
                    break;
               case WHAT_GET_IMAGE_REMOTE:
                    notifyImageRemote( mByteBuffer, mBitmap );
                    break;
            }
        }
    };

    /**
     * notifyConnect
     * @param boolean isSuccess
     */ 
    private void notifyConnect( boolean isSuccess ) {
        if ( mListener != null ) {
            mListener.onConnectChanged( isSuccess );
        }
    }

    /**
     * notifyImageRemote
     * @param boolean isSuccess
     * @param ByteBuffer buf
     * @param Bitmap bitmap
     */ 
    private void notifyImageRemote( ByteBuffer buf, Bitmap bitmap ) {
        if ( mListener != null ) {
            mListener.onImageRemoteChanged( buf, bitmap );
        }
    }

    /**
     * sleep
     * @param long time
     */ 
    private void sleep( long time ) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            if (D) e.printStackTrace();
        }
    }

    /**
     * toast short
     */       
    private void toast_short( String str ) {
        ToastMaster.makeText( mContext, str, Toast.LENGTH_SHORT ).show();
    }

    /**
     * toast short
     */       
    private void toast_short( int res_id ) {
        ToastMaster.makeText( mContext, res_id, Toast.LENGTH_SHORT ).show();
    }
 
    /**
     * log_d
     */ 
    private void log_d( int res_id ) {
        log_d( getString(res_id) );
    }

    /**
     * log_d
     */ 
    private void log_d( String str ) {
        if (D) Log.d( TAG, str );
    }

   /**
     * getString
     */  
    private String getString( int res_id ) {
        return mContext.getResources().getString( res_id );
    }

   /**
     * --- class ImageRemoteResult ---
     * [0] : width; [1] : height; [2] : number of layers; [3] : ColorSpace; [4] : time stamp (highest 32 bits); [5] : time stamp (lowest 32 bits); [6] : array of size height * width * nblayers containing image data; [7] : cameraID; [8] : left angle; [9] : top angle; [10] : right angle; [11] : bottom angle;
     */ 
    private class ImageRemoteResult {
        public int width = 0;
        public int height = 0;
        public int layers = 0;
        public int color = 0;
        public int time1 = 0;
        public int time2 = 0;
        public long time = 0;
        public int id = 0;     
        public float left = 0;
        public float top = 0;
        public float right = 0;
        public float bottom = 0;
        ByteBuffer buf = null;

        /*
         * Constructor
         */ 
        public ImageRemoteResult( List<Object> list ) {
            log_d("ImageRemoteResult " + list);
            width = (int) list.get(0);
            height = (int) list.get(1);
            layers = (int) list.get(2);
            color = (int) list.get(3);
            time1 = (int) list.get(4);
            time2 = (int) list.get(5);
            time = ((long)time1 << 32 ) + (long)time2;
            buf = (ByteBuffer) list.get(6);
            id = (int) list.get(7);     
            left = (float) list.get(8);
            top = (float) list.get(9);
            right = (float) list.get(10);
            bottom = (float) list.get(11);
            debug();
        }

        /*
         * debug
         */ 
        private void debug() {
            String msg = "";
            msg += " width=" + width;
            msg += " height=" + height;
            msg += " layers=" + layers;
            msg += " color=" + color;
            msg += " time=" + time;
            msg += " id=" + id;
            msg += " left=" + left;
            msg += " top=" + top;
            msg += " right=" + right;
            msg += " bottom=" + bottom;
            msg += " buffer=" + buf;
            log_d( msg );
        }
    }

}
