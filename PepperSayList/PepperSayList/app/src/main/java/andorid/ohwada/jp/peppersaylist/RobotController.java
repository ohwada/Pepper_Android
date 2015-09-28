/**
 * Pepper controlled by Android
 * 2015-08-01 K.OHWADA
 */

package andorid.ohwada.jp.peppersaylist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.aldebaran.qi.EmbeddedTools;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.proxies.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity
 */
public class RobotController {

    // debug
    private static final String TAG = Constant.TAG;
    private static final boolean D = Constant.DEBUG;

    // Volume
    public static final int VOL_MAX = 100;
    public static final int VOL_PROGRESS = 70;
    public static final int VOL_ERROR = -1;

    private static final String LF = "\n";

    // SharedPreferences
    private static final String KEY_IP = "key_ip";

    // connection
    private static final String IP_PORT = "9559";
    private static final String IP_DEFAULT = "192.168.1.1";
    private static final String IP_PATTERN = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

    // app command
    private static final int CMD_CONNECT = 1;
    private static final int CMD_SAY = 2;
    private static final int CMD_SET_VOLUME = 3;

    private static final int WHAT_TOAST = 1;
    private static final int WHAT_CONNECT_SUCCESS = 2;
    private static final int WHAT_CONNECT_FAIL = 3;

    // class object
    private Context mContext;
    private SharedPreferences mPreferences;

    // Pepper API
    private Session mQiSession;
    private ALTextToSpeech mALTextToSpeech;
    private ALAudioDevice mALAudioDevice;

    private String mAddr = "";
    private String mLanguage = "";
    private String mMsg = "";
    private int mSetVolume = VOL_PROGRESS;
    private int mGetVolume = 0;
    private String mToast = "";

    // callback 
    private OnChangedListener mListener;  

    /*
     * callback interface
     */    
    public interface OnChangedListener {
        void onConnectChanged( boolean isSuccess );
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
     * say
     * @param String language
     * @param String msg
     */ 
    public void say( String language, String msg ) {
        mLanguage = language;
        mMsg = msg;
        procThread( CMD_SAY ); 
    }

   /*
     * setVolume
     * @param int vol
     */ 
    public void setVolume( int vol ) {
        mSetVolume = vol;
        procThread( CMD_SET_VOLUME ); 
    }

   /*
     * getVolume
     */ 
    public int getVolume() {
        return mGetVolume;
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
                boolean ret = robotConnect( mAddr );
                if ( ret ) {
                    serPrefAddr( mAddr );
                    mGetVolume = robotGetVolume();
                    log_d( "Volume " + mGetVolume );	
                    sendMessage( WHAT_CONNECT_SUCCESS );
                } else {
                    sendMessage( WHAT_CONNECT_FAIL );
                }
                break;
            case CMD_SAY:
                robotSay( mLanguage, mMsg );
                break;
             case CMD_SET_VOLUME:
                robotSetVolume( mSetVolume );
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
            mALTextToSpeech = new ALTextToSpeech( mQiSession );
            mALAudioDevice = new ALAudioDevice( mQiSession );
        } catch (Exception e) {
            if (D) e.printStackTrace();
        }
        return true;
    }

    /**
     * robot say
     * @param String language
     * @param String msg
     */
    private void robotSay( String language, String msg ) {
        log_d( "robotSay " + language + LF + msg );
        try {			
            mALTextToSpeech.setLanguage( language );
            mALTextToSpeech.say( msg );
        } catch (Exception e) {
            if (D) e.printStackTrace();
            sendToast( e, R.string.toast_speech_failed );
        }
    }

   /**
     * robotSetVolume
     * @param int vol
     */
    private void robotSetVolume( int vol ) {
        log_d( "robotSetVolume " + vol );
        try {		
            mALAudioDevice.setOutputVolume( vol );
        } catch (Exception e) {
            if (D) e.printStackTrace(); 
           sendToast( e, R.string.toast_volume_failed );
        }
    }

   /**
     * robotGetVolume
     * @return int
     */
    private int robotGetVolume() {
        log_d( "robotGetVolume " );
        int vol = VOL_ERROR;
        try {		
            vol = mALAudioDevice.getOutputVolume();
        } catch (Exception e) {
            if (D) e.printStackTrace(); 
           sendToast( e, R.string.toast_volume_failed );
        }
        return vol;
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
            }
        }
    };

    /**
     * notifyConnect
     */ 
    private void notifyConnect( boolean isSuccess ) {
        if ( mListener != null ) {
            mListener.onConnectChanged( isSuccess );
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

}
