package andorid.ohwada.jp.pepperposture;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
public class MainActivity extends Activity {
    // debug
    private static final String TAG = "PepperPosture";
    private static final boolean D = true;

    private static final String LF = "\n";

    // Pepper Connection
    private static final String IP_KEY = "ip_address";
    private static final String IP_DEFAULT = "192.168.1.1";
    private static final String IP_PORT = "9559";
    private static final String IP_PATTERN = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

    // command
    private static final int CMD_CONNECT = 1;
    private static final int CMD_POSTURE = 2;

    private static final int RES_TOAST = 1;
    private static final int RES_POSTURE_INFO = 2;

    // Posture
    private static final String POSTURE_STAND = "Stand";
    private static final String POSTURE_STAND_INIT = "StandInit";
    private static final String POSTURE_STAND_ZERO = "StandZero";
    private static final String POSTURE_CROUCH = "Crouch";

    private static final float POSTURE_SPEED = 1.0f;

    // View
    private EditText mEditTextIp;
    private TextView mTextViewInfo;

    private SharedPreferences mPreferences;

    // Pepper API
    private Session mQiSession;
    private ALRobotPosture mALRobotPosture;

    private String mAddr = "";
    private String mPosture = "";
    private List<String> mPostureList = new ArrayList<String>();
    private String mToast = "";
    private String mPostureInfo = "";

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // View
        mEditTextIp = (EditText) findViewById( R.id.EditText_ip );
        mEditTextIp.setText( mPreferences.getString(IP_KEY, IP_DEFAULT) );
        mTextViewInfo = (TextView) findViewById( R.id.TextView_info );
        // Embedded Tools
        EmbeddedTools tools = new EmbeddedTools();
        File dir = getApplicationContext().getCacheDir();
        log_d( "Extracting libraries in " + dir.getAbsolutePath() );
        tools.overrideTempDirectory(dir);
        tools.loadEmbeddedLibraries();
    }

    /**
     * --- onClick Connect ---
     */
    public void onClickConnect( View view ) {
        log_d("onClickConnect");
        String ip = mEditTextIp.getText().toString().trim();
        if ( "".equals(ip) ) {
            toast_short( R.string.toast_enter_ip );
            return;
        } else if ( !ip.matches(IP_PATTERN) ) {
            toast_short( R.string.toast_enter_correct );
            return;
        }
        mAddr = ip;
        startThread(CMD_CONNECT);
    }

    /**
     * --- onClick Stand ---
     */
    public void onClickStand( View view ) {
        log_d("onClickStand");
        procPosture( POSTURE_STAND );
    }

    /**
     * --- onClick StandInit ---
     */
    public void onClickStandInit( View view ) {
        log_d("onClickStandInit");
        procPosture( POSTURE_STAND_INIT );
    }

    /**
     * --- onClick StandZero ---
     */
    public void onClickStandZero( View view ) {
        log_d("onClickStandZero");
        procPosture(POSTURE_STAND_ZERO);
    }

    /**
     * --- onClick Crouch ---
     */
    public void onClickCrouch( View view ) {
        log_d("onClickCrouch");
        procPosture(POSTURE_CROUCH);
    }

    /**
     * procPosture
     * @param String posture
     */
    private void procPosture( String posture ) {
        mPosture = posture;
        procThread( CMD_POSTURE );
    }

    /**
     * procThread
     */
    private void procThread( int cmd ) {
        log_d("procThread " + cmd );
        if ( mQiSession == null ) {
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
                robotConnect();
                break;      
            case CMD_POSTURE:
                robotGoToPosture( mPosture, POSTURE_SPEED );
                break; 
        }
    }

    /**
     * robotConnect
     */
    private void robotConnect() {
        boolean ret = robotConnect( mAddr );
        if ( ret ) {
            mPreferences.edit().putString( IP_KEY, mAddr ).apply();
            toastOnUiThread( R.string.toast_connected );
            List<String> list = robotGetPostureList();
            mPostureInfo = debugList( "Posture", list );
            procOnUiThread( RES_POSTURE_INFO );
        } else {
            toastOnUiThread( R.string.toast_connect_failed ); 
        }
    }

    /**
     * robotConnect
     * @param String ip
     * @return boolean
     */
    private boolean robotConnect( String addr ) {
        String ip = "tcp://" + addr + ":" + IP_PORT;
        log_d( "robotConnect " + ip );
        mQiSession = new Session();
        try {
            mQiSession.connect(ip).get();
        } catch (Exception e) {
            if (D) e.printStackTrace();
            mQiSession = null;
           return false;
        }
        try {
            mALRobotPosture = new ALRobotPosture( mQiSession );
        } catch (Exception e) {
            if (D) e.printStackTrace();
        }
        return true;
    }

    /**
     * robotGoToPosture
     * @param String posture
     * @param float speed
     */
    private void robotGoToPosture( String posture, float speed ) {
        log_d( "robotGoToPosture " + posture + " " + speed );
        try {	
            mALRobotPosture.goToPosture( posture, speed );
        } catch (Exception e) {
            if (D) e.printStackTrace(); 
           toastOnUiThread( e, R.string.toast_posture_failed );
        }
    }

    /**
     * robot GetPostureList
     * @return List<String>
     */
    private List<String> robotGetPostureList() {
        log_d( "robotGetPostureList" );
        List<String> list = new ArrayList<String>();
        try {			
            list = mALRobotPosture.getPostureList();
        } catch (Exception e) {
            if (D) e.printStackTrace();
            toastOnUiThread( e, R.string.toast_posture_failed );
        }
        return list;
    }


    /**
     * debugList
     * @param String name
     * @param List<String> list
     * @return String
     */
    private String debugList( String name, List<String> list ) {
        String msg = name + ": ";
        if (( list == null )||( list.size() == 0 )) {
            log_d( "not get  " + name );
            return msg;
        }
        for ( String str: list ) {
            msg += str + ", ";
        }
        log_d( msg );
        return msg;
    }

    /**
     * toast on UI thread
     */
    private void toastOnUiThread( Exception e, int res_id) {
        String str = getString(res_id);
        toastOnUiThread( str+ LF + e.getMessage() );
    }

    /**
     * toast on UI thread
     */
    private void toastOnUiThread( int res_id ) {
        String str = getString(res_id);
        toastOnUiThread( str );
    }
 
    /**
     * toast on UI thread
     */
    private void toastOnUiThread( String str ) {
        mToast = str;
        procOnUiThread( RES_TOAST );
    }

    /**
     * proc on UI thread
     * @param int _res
     */
    private void procOnUiThread( int _res ) {
        log_d( "procOnUiThread " +  _res );
        final int res = _res;
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                procUi( res );
            }
        });
    }

// --- UI ---
    /**
     * procUi
     * @param int res
     */
    private void procUi( int res ) {
        switch( res ) {
             case RES_TOAST:
                toast_short( mToast );
                break;      
             case RES_POSTURE_INFO:
                mTextViewInfo.setText( mPostureInfo );
                break; 
        }
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
