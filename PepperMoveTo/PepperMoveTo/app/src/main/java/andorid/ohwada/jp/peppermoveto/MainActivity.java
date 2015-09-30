/**
 * Pepper controlled by Android
 * using Aldebaran Android SDK 
 * 2015-09-01 K.OHWADA
 */

package andorid.ohwada.jp.peppermoveto;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.aldebaran.qi.EmbeddedTools;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.proxies.*;

import java.io.File;

import static java.lang.Float.*;

/**
 * MainActivity
 */
public class MainActivity extends Activity {
    // debug
    private static final String TAG = Constant.TAG;
    private static final boolean D = Constant.DEBUG;

    // char
    private static final String LF = "\n";

    // coefficient
    private static float DEG_TO_RAD = (float)( Math.PI / 180 );

    // Pepper Connection
    private static final String IP_KEY = "ip_address";
    private static final String IP_DEFAULT = "192.168.1.1";
    private static final String IP_PORT = "9559";
    private static final String IP_PATTERN = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

    // command
    private static final int CMD_CONNECT = 1;
    private static final int CMD_MOVE = 2;

    // motion
    private static final  float DEFAULT_X = 0.5f;   // 0.5 m
    private static final float DEFAULT_Y = 0.5f;    // 0.5 m
    private static final float DEFAULT_THETA = 90;     // 90 deg
    private static final float MAX_THETA = 360;     // 360 deg

    // View
    private EditText mEditTextIp;
    private EditText mEditTextX;
    private EditText mEditTextY;
    private EditText mEditTextTheta;
    private NineButtonsView mNineButtonsView;

    private SharedPreferences mPreferences;

    // Pepper API
    private Session mQiSession;
    private ALMotion mALMotion;

    private String mAddr = "";
    private int mNumButton = 0;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView( view ); 
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // View
        mEditTextIp = (EditText) findViewById( R.id.EditText_ip );
        mEditTextIp.setText( mPreferences.getString(IP_KEY, IP_DEFAULT) );
        mEditTextX = (EditText) findViewById( R.id.EditText_x );
        mEditTextX.setText( Float.toString( DEFAULT_X ) );
        mEditTextY = (EditText) findViewById( R.id.EditText_y );
        mEditTextY.setText( Float.toString( DEFAULT_Y ) );
        mEditTextTheta = (EditText) findViewById( R.id.EditText_theta );
        mEditTextTheta.setText( Float.toString( DEFAULT_THETA ) );
        mNineButtonsView = new NineButtonsView( this, view );
        mNineButtonsView.setOnTouchListener( new NineButtonsView.OnButtonTouchListener() { 
            @Override 
            public void onTouch( View view, MotionEvent event, int button ) {
                procTouch( event.getAction(), button ); 
            } 
        });
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
        startThread( CMD_CONNECT );
    }

    /**
     * procTouch
     * @param int action
     * @param int button
     */
    private void procTouch( int action, int button ) {
        log_d( "procTouch " + action + " " + button );
        if ( action != MotionEvent.ACTION_DOWN ) return;
        mNumButton = button;
        procThread( CMD_MOVE );
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
                boolean ret = robotConnect( mAddr );
                if ( ret ) {
                    mPreferences.edit().putString(IP_KEY, mAddr ).apply();
                    toastOnUiThread(R.string.toast_connected);
                } else {
                    toastOnUiThread( R.string.toast_connect_failed );
                }
                break;      
            case CMD_MOVE:
                robotMove( mNumButton ); 
               break;
        }
    }

    /**
     * robot Connect
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
            mALMotion = new ALMotion( mQiSession );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * robot Move
     * @param int button
     */
    private void robotMove( int button ) {
        log_d( "robotMove " + button );
        float val_x = getEditTextX();
        float val_y = getEditTextY();
        float val_theta = getEditTextTheta();
        if ( button == NineButtonsView.BUTTON_CENTER ) {
            robotStopMove();
            return;
        }
        float x = 0f;
        float y = 0f;
        float theta = 0f;
        switch( button ){
            case NineButtonsView.BUTTON_FORWARD_LEFT:
                x = val_x;
                y = val_y;
                break;
            case NineButtonsView.BUTTON_FORWARD:
                x = val_x;
                break;
            case NineButtonsView.BUTTON_FORWARD_RIGHT:
                x = val_x;
                y = - val_y;
                break;
            case NineButtonsView.BUTTON_LEFT:
                y = val_y;
                break;
            case NineButtonsView.BUTTON_RIGHT:
                y = - val_y;
                break;
            case NineButtonsView.BUTTON_BACK_LEFT:
                theta = val_theta;
                break;
            case NineButtonsView.BUTTON_BACK:
                x = - val_y;
                break;
            case NineButtonsView.BUTTON_BACK_RIGHT:
                theta = - val_theta;
                break;
            default:
                break;
        }
        // ALMotion#moveTo
        // x - The position along x axis [m].
        // y - The position along y axis [m].
        // theta - The position around z axis [rd].
        log_d( "moveTo " + Float.toString(x) + " " + Float.toString(y) + " " + Float.toString(theta) );
        try {
            mALMotion.moveTo( x, y, theta );
        } catch (Exception e) {
            toastOnUiThread( e, R.string.toast_move_failed );
            e.printStackTrace();
        }
    }

    /**
     * robotStopMove
     */
    private void robotStopMove() {
        log_d( "robotStopMove" );
        try {
            mALMotion.stopMove();
        } catch (Exception e) {
            toastOnUiThread( e, R.string.toast_move_failed );
            e.printStackTrace();
        }
    }

    /**
     * getEditTextX
     * @return float 
     */
    private float getEditTextX() {
        float ret = toFloat( mEditTextX );
        if ( ret < 0 ) ret = 0;
        return ret;
    }

    /**
     * getEditTextY
     * @return float 
     */
    private float getEditTextY() {
        float ret = toFloat( mEditTextY );
        if ( ret < 0 ) ret = 0;
        return ret;
    }

    /**
     * getEditTextTheta
     * @return float 
     */
    private float getEditTextTheta() {
        float ret = toFloat( mEditTextTheta );
        if ( ret < 0 ) ret = 0;
        if ( ret > MAX_THETA ) ret = MAX_THETA;
        float f = DEG_TO_RAD * ret;
        return f;
    }

    /**
     * toFloat
     * @param EditText text
     * @return float 
     */
    private float toFloat( EditText text ) {
        return toFloat( text.getText().toString().trim() );
    }

    /**
     * toFloat
     * @paramString str
     * @return float 
     */
    private float toFloat( String str ) {
        float ret = 0f;
        try {
            ret = Float.parseFloat( str ) ;
        } catch (Exception e) {
            if (D) e.printStackTrace();
        }
        return ret;
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
        final String msg = str;
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                toast_short( msg );
            }
        });
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
