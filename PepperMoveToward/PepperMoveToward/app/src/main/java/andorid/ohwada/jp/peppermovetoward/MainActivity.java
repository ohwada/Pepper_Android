/**
 * Pepper controlled by Android
 * using Aldebaran Android SDK 
 * 2015-09-01 K.OHWADA
 */

package andorid.ohwada.jp.peppermovetoward;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aldebaran.qi.EmbeddedTools;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.proxies.*;

import java.io.File;

/**
 * MainActivity
 */
public class MainActivity extends Activity {
    // debug
    private static final String TAG = Constant.TAG;
    private static final boolean D = Constant.DEBUG;

    // char
    private static final String LF = "\n";

    // Pepper Connection
    private static final String IP_KEY = "ip_address";
    private static final String IP_DEFAULT = "192.168.1.1";
    private static final String IP_PORT = "9559";
    private static final String IP_PATTERN = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

    // command
    private static final int CMD_CONNECT = 1;
    private static final int CMD_MOVE = 2;

    // Velocity
    private static final int VELOCITY_MAX = 100;
    private static final int VELOCITY_PROGRESS = 10; 
    private static final int VELOCITY_SENSITIVITY = 10;

    // View
    private EditText mEditTextIp;
    private TextView mTextViewVelocity;
    private NineButtonsView mNineButtonsView;
    private SeekBar mSeekBarVelocity;

    private SharedPreferences mPreferences;

    // Pepper API
    private Session mQiSession;
    private ALMotion mALMotion;

    private String mAddr = "";
    private float mVelocity = (float)VELOCITY_PROGRESS /  (float)VELOCITY_MAX;
    private int mNumButton = 0;
    private int mAction = 0;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate( R.layout.activity_main, null );
        setContentView( view ); 
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // View
        mEditTextIp = (EditText) findViewById( R.id.EditText_ip );
        mEditTextIp.setText( mPreferences.getString(IP_KEY, IP_DEFAULT) );
        mTextViewVelocity = (TextView) findViewById( R.id.TextView_velocity );
        setTextViewVelocity();
        mNineButtonsView = new NineButtonsView( this, view );
        mNineButtonsView.setOnTouchListener( new NineButtonsView.OnButtonTouchListener() { 
            @Override 
            public void onTouch( View view, MotionEvent event, int button ) {
                procTouch( event.getAction(), button ); 
            } 
        });
        mSeekBarVelocity = (SeekBar) findViewById( R.id.SeekBar_velocity );
        mSeekBarVelocity.setMax( VELOCITY_MAX );
        mSeekBarVelocity.setProgress( VELOCITY_PROGRESS );
        mSeekBarVelocity.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch( SeekBar seek ) {
                // noting to do
            }
            @Override
            public void onProgressChanged( SeekBar seek, int progress, boolean touch ) {
                procProgressChanged( seek, progress, touch );
            }
            @Override
            public void onStopTrackingTouch( SeekBar seek ) {
                // noting to do
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
     * setTextViewVelocity
     */
    private void setTextViewVelocity() {
        mTextViewVelocity.setText( Float.toString(mVelocity) );
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
     * procProgressChanged
     * @param SeekBar seek
     * @param int progress
     * @param boolean touch
     */	
    private void procProgressChanged( SeekBar seek, int progress, boolean touch ) {
        // Progress adjustment by sensitivity
        if ( Math.abs( mVelocity - progress ) > VELOCITY_SENSITIVITY ) {
            float velocity = (float)progress / (float)VELOCITY_MAX;
            if ( velocity > 1.0f ) velocity = 1.0f;
            if ( velocity < -1.0f ) velocity = -1.0f;
            mVelocity = velocity;
            setTextViewVelocity();
        }
    }

    /**
     * procTouch
     * @param int button
     */
    private void procTouch( int action, int button ) {
        log_d( "sendMove " + action + " " + button );
        mAction = action;
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
                robotMove( mAction, mNumButton ); 
               break;
        }
    }

    /**
     * robot Connect
     */
    private boolean robotConnect( String addr ) {
        String ip = "tcp://" + addr + ":" + IP_PORT;
        log_d( "connectPepper " + ip );
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
            if (D) e.printStackTrace();
        }
        return true;
    }

    /**
     * robotMove
     * @param int action
     * @param int button
     */
    private void robotMove( int action, int button ) {
        log_d("robotMove " + action + " " + button);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                switch (button) {
                    case NineButtonsView.BUTTON_CENTER:
                        robotStopMove();
                        break;
                    default:
                        robotMoveToward( button );
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                robotStopMove();
                break;
        }
    }

    /**
     * robotMoveToward
     * @param int button
     */    
    private void robotMoveToward( int button ) {
        log_d("robotMoveToward "  + button );
        float x = 0.0f;
        float y = 0.0f;
        float theta = 0.0f;
        switch( button ){
            case NineButtonsView.BUTTON_FORWARD_LEFT:
                x = mVelocity;
                y = mVelocity;
                break;
            case NineButtonsView.BUTTON_FORWARD:
                x = mVelocity;
                break;
            case NineButtonsView.BUTTON_FORWARD_RIGHT:
                x = mVelocity;
                y = -mVelocity;
                break;
            case NineButtonsView.BUTTON_LEFT:
                y = mVelocity;
                break;
            case NineButtonsView.BUTTON_RIGHT:
                y = -mVelocity;
                break;
            case NineButtonsView.BUTTON_BACK_LEFT:
                theta = mVelocity;
                break;
            case NineButtonsView.BUTTON_BACK:
                x = -mVelocity;
                break;
            case NineButtonsView.BUTTON_BACK_RIGHT:
                theta = -mVelocity;
                break;
            default:
                break;
        }
        log_d( "moveToward "  + x + " " + y + " " + theta );
        // ALMotion#moveToward
        // x - The normalized velocity along x axis (between -1 and 1).
        // y - The normalized velocity along y axis (between -1 and 1).
        // theta - The normalized velocity around z axis (between -1 and 1).
        try {
            mALMotion.moveToward( x, y, theta );
        } catch (Exception e) {
            toastOnUiThread( e, R.string.toast_move_failed );
            e.printStackTrace();
        }
    }

    /**
     * robotStopMove
     * @param int button
     */
    private void robotStopMove() {
        log_d("robotStopMove");
        try {
            mALMotion.stopMove();
        } catch (Exception e) {
            toastOnUiThread( e, R.string.toast_move_failed );
            e.printStackTrace();
        }
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
