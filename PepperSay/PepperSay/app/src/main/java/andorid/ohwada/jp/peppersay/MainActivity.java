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

    // Pepper Connection
    private static final String IP_KEY = "ip_address";
    private static final String IP_DEFAULT = "192.168.1.1";
    private static final String PORT = "9559";
    private static final String IP_PATTERN = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

    // View
    private EditText mEditTextIp;

    private SharedPreferences mPreferences;

    // Pepper API
    private Session mQiSession;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditTextIp = (EditText) findViewById( R.id.EditText_ip );
        mEditTextIp.setText( mPreferences.getString(IP_KEY, IP_DEFAULT) );

        // Embedded Tools
        EmbeddedTools tools = new EmbeddedTools();
        File dir = getApplicationContext().getCacheDir();
        log_d( "Extracting libraries in " + dir.getAbsolutePath() );
        tools.overrideTempDirectory(dir);
        tools.loadEmbeddedLibraries();
    }

    /**
     * onClick of connect buton
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
        final String addr = ip;
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                connectPepper(addr);
            }
        });
        thread.start();
    }

    /**
     * connect to Pepper
     */
    private void connectPepper( String ip ) {
        log_d( "connectPepper " + ip );
        mQiSession = new Session();
        try {
            mQiSession.connect(ip).get();
        } catch (Exception e) {
            toastOnUiThread( e, R.string.toast_connect_failed ); 
            if (D) e.printStackTrace();
            mQiSession = null;
            return;
        }
        mPreferences.edit().putString(IP_KEY, ip).apply();
        toastOnUiThread( R.string.toast_connected ); 
    }

    /**
     * onClick of say buton
     */
    public void onClickSay( View view ) {
        log_d("onClickSay");
        if ( mQiSession == null ) {
            toast_short( R.string.toast_not_connected );
            return;
        }
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                robotSay();
            }
        });
        thread.start();
    }

    /**
     * robot say
     */
    private void robotSay() {
        log_d("robotSay");
        try {
            ALTextToSpeech tts = new ALTextToSpeech( mQiSession );			
            tts.setLanguage( getString(R.string.language) );
            tts.say( getString(R.string.say_hello) );
        } catch (Exception e) {
            toastOnUiThread( e, R.string.toast_speech_failed );
            if (D) e.printStackTrace();
        }
    }

    /**
     * toast on UI thread
     */
    private void toastOnUiThread( Exception e, int res_id) {
        String str = getString(res_id);
        toastOnUiThread( str+ "\n" + e.getMessage() );
    }

    /**
     * toast on UI thread
     */
    private void toastOnUiThread( int res_id ) {
        final int id = res_id;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast_short( id );
            }
        });
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
