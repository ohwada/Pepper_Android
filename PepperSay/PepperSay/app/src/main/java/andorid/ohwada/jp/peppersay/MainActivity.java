/**
 * Pepper controlled by Android
 * using Aldebaran Android SDK 
 * 2015-07-01 K.OHWADA
 */

package andorid.ohwada.jp.peppersay;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
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
    private static final String TAG = "PepperSay";
    private static final boolean D = true;

    private static final String LF = "\n";

    // Pepper Connection
    private static final String IP_KEY = "ip_address";
    private static final String IP_DEFAULT = "192.168.1.1";
    private static final String IP_PORT = "9559";
    private static final String IP_PATTERN = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

    // command
    private static final int CMD_CONNECT = 1;
    private static final int CMD_SAY = 2;
    private static final int CMD_VOLUME = 3;

    private static final int RES_TOAST = 1;
    private static final int RES_VOLUME = 2;

    // Volume
    private static final int VOL_MAX = 100;
    private static final int VOL_PROGRESS = 70; 

    // View
    private EditText mEditTextIp;
    private TextView mTextViewVolume;
    private SeekBar mSeekBarVolume;

    private SharedPreferences mPreferences;

    // Pepper API
    private Session mQiSession;
    private ALTextToSpeech mALTextToSpeech;
    private ALAudioDevice mALAudioDevice;

    private String mAddr = "";
    private String mSay = "";
    private int mVolume = VOL_PROGRESS;
    private String mToast = "";

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
        mTextViewVolume = (TextView) findViewById( R.id.TextView_volume );
        setTextViewVolume();
        // SeekBar
        mSeekBarVolume = (SeekBar) findViewById( R.id.SeekBar_volume );
        mSeekBarVolume.setMax( VOL_MAX );
        mSeekBarVolume.setProgress( VOL_PROGRESS );
        mSeekBarVolume.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch( SeekBar seek ) {
                // noting to do
            }
            @Override
            public void onProgressChanged( SeekBar seek, int progress, boolean touch ) {
                procProgressChanged( progress );
            }
            @Override
            public void onStopTrackingTouch( SeekBar seek ) {
                procStopTrackingTouch();
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
     * setTextViewVolume
     */
    private void setTextViewVolume() {
        mTextViewVolume.setText(Integer.toString(mVolume));
    }

    /**
     * procProgressChanged
     * @param int progress
     */	
    private void procProgressChanged( int progress ) {
        mVolume = progress;
        setTextViewVolume();
    }

    /**
     * procStopTrackingTouch
     */	
    private void procStopTrackingTouch() {
        procThread(CMD_VOLUME);
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
     * --- onClick Hello ---
     */
    public void onClickHello( View view ) {
        log_d("onClickHello");
        procSay( R.string.say_hello );
    }

    /**
     * --- onClick Goodbye ---
     */
    public void onClickGoodbye( View view ) {
        log_d("onClickGoodbye");
        procSay( R.string.say_goodbye );
    }

    /**
     * --- onClick Thankyou ---
     */
    public void onClickThankyou( View view ) {
        log_d("onClickThankyou");
        procSay(R.string.say_thankyou);
    }

    /**
     * procSay
     * @param int res_id
     */
    private void procSay( int res_id ) {
        mSay = getString( res_id );
        procThread(CMD_SAY);
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
        startThread(cmd);
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
                    mPreferences.edit().putString( IP_KEY, mAddr ).apply();
                    toastOnUiThread( R.string.toast_connected );
                    mVolume = robotGetVolume();
                    log_d( "Volume " + mVolume );
                    procOnUiThread( RES_VOLUME );  	
                } else {
                    toastOnUiThread( R.string.toast_connect_failed );
                }
                break;      
            case CMD_SAY:
                String language = getString(R.string.language);
                robotSay( language, mSay );
                break; 
             case CMD_VOLUME:
                robotSetVolume( mVolume );
                break; 
        }
    }

    /**
     * robotConnect
     * @param String ip
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
            mALTextToSpeech.say(msg);
        } catch (Exception e) {
            if (D) e.printStackTrace();
            toastOnUiThread(e, R.string.toast_speech_failed);
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
                toastOnUiThread(e, R.string.toast_volume_failed );
            }
        }

        /**
         * robotGetVolume
         * @return int
         */
        private int robotGetVolume() {
            log_d( "robotGetVolume " );
            int vol = 0;
            try {
                vol = mALAudioDevice.getOutputVolume();
            } catch (Exception e) {
                if (D) e.printStackTrace();
                toastOnUiThread(e, R.string.toast_volume_failed );
            }
            return vol;
        }

    /**
     * toast on UI thread
     * @param Exception e
     * @param int res_id
     */
    private void toastOnUiThread( Exception e, int res_id) {
        String str = getString(res_id);
        toastOnUiThread( str+ LF + e.getMessage() );
    }

    /**
     * toast on UI thread
     * @param int res_id
     */
    private void toastOnUiThread( int res_id ) {
        String str = getString(res_id);
        toastOnUiThread( str );
    }
 
    /**
     * toast on UI thread
     * @param String str
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
             case RES_VOLUME:
                setTextViewVolume();
                mSeekBarVolume.setProgress( mVolume );
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
