/**
 * Pepper controlled by Android
 * using Aldebaran Android SDK 
 * 2015-07-01 K.OHWADA
 */

package andorid.ohwada.jp.peppersaysample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
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

    // View
    private EditText mEditTextIp;

    // Pepper API
    private Session mQiSession;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditTextIp = (EditText) findViewById( R.id.EditText_ip );
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
        log_d("onConnect");
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                connectPepper();
            }
        });
        thread.start();
    }

    /**
     * connect to robot
     */
    private void connectPepper() {
        mQiSession = new Session();
        String ip = "tcp://" + mEditTextIp.getText().toString() + ":9559";
        log_d( "connectPepper " + ip );
        try {
            mQiSession.connect(ip).get();
        } catch (Exception e) {
            toastOnUiThread( e, "Connect failed" ); 
            e.printStackTrace();
            mQiSession = null;
            return;
        }
        toastOnUiThread( "Connected" ); 
    }

    /**
     * --- onClick Say ---
     */
    public void onClickSay( View view ) {
        log_d("onClickSay");
        if ( mQiSession == null ) {
            toast_long( "Not connected" );
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
            tts.setLanguage( "Japanese" );
            tts.say( "こんにちは　ぼくはペッパーです" );
        } catch (Exception e) {
            toastOnUiThread( e, "Speech failed" );
            e.printStackTrace();
        }
    }

    /**
     * toast on UI thread
     */
    private void toastOnUiThread( Exception e, String str ) {
        toastOnUiThread( str+ "\n" + e.getMessage() );
    }
 
    /**
     * toast on UI thread
     */
    private void toastOnUiThread( String str ) {
        final String msg = str;
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                toast_long( msg );
            }
        });
    }

    /**
     * toast long
     */       
    private void toast_long( String str ) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    /**
     * log_d
     */ 
    private void log_d( String str ) {
        Log.d( "PepperSaySample", str );
    }

}
