package andorid.ohwada.jp.pepperspeaker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.aldebaran.qi.EmbeddedTools;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.proxies.ALTextToSpeech;

import java.io.File;
import java.util.ArrayList;

/**
 * MainActivity
 */
public class MainActivity extends Activity {
    // debug
    private static final String TAG = "PepperSpeaker";
    private static final boolean D = true;

    private static final int REQUEST_SPEECH = 1;
    private static final String PORT = "9559";
    private static final String IP_KEY = "ip_address";
    private static final String IP_DEFAULT = "192.168.1.1";

    // View
    private EditText mEditTextIP;
    private EditText mEditTextMessage;
    private LinearLayout mLinearLayoutMain;

    private SharedPreferences mPreferences;
    private boolean isRepeat = false;

    // Pepper API
    private ALTextToSpeech mALTextToSpeech;
    private Session mQiSession;

    /**
     * === onCreate ===
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mLinearLayoutMain = (LinearLayout) findViewById(R.id.LinearLayout_main);
        mEditTextIP = (EditText) findViewById(R.id.EditText_ip);
        mEditTextIP.setText( mPreferences.getString(IP_KEY, IP_DEFAULT) );
        mEditTextMessage = (EditText) findViewById(R.id.EditText_message);

        // EmbeddedLibraries
        EmbeddedTools ebt = new EmbeddedTools();
        File cacheDir = getApplicationContext().getCacheDir();
        log_d("Extracting libraries in " + cacheDir.getAbsolutePath());
        ebt.overrideTempDirectory(cacheDir);
        ebt.loadEmbeddedLibraries();
    }

    /**
     * === onActivityResult ===
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SPEECH && resultCode == RESULT_OK) {
            procActivityResultRecognizer(resultCode, data);
        }
    }

    /**
     * procActivityResultRecognizer
     */
    private void procActivityResultRecognizer( int resultCode, Intent data ) {
        boolean is_say = false;
        ArrayList<String> list =  
            data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if ( list != null ) {
            String text = list.get(0);
            if ( text.length() > 0 ) {
                sayPepper( text );
                is_say = true;
            }
        }
        if ( !is_say ) {
            toast_short(R.string.toast_not_recognize);
        }
        if ( isRepeat ) {
            startRecognizer();
        }
    }

    /**
     * sayPepper
     */
    private void sayPepper( String text ) {
        log_d("text : " + text);
        try {
            mALTextToSpeech.say(text);
        } catch (Exception e) {
            if (D) e.printStackTrace();
        }
    }

    /**
     * --- onClick Connect ---
     */
    public void onClickConnect(View view) {
        log_d("onClickConnect");
        Thread thread = new Thread( new Runnable() {        
            @Override
            public void run() {
                Looper.prepare();
                procConnect();
            }
        });
        thread.start();
    }

    /**
     * procConnect
     */
    private void procConnect() {
        log_d("procConnect");
        String ip = mEditTextIP.getText().toString();
        if ( ip.length() > 0 ) {
            mPreferences.edit().putString(IP_KEY, ip).apply();
            connectPepper(ip);
        } else {
            toast_short(R.string.toast_enter_ip);
        }
    }

    /**
     * connectPepper
     */
    private void connectPepper( String ip ) {
        log_d("connectPepper");
         try {
            mQiSession = new Session();
            String url = "tcp://" + ip + ":" + PORT;
            log_d("url "+ url);
            mQiSession.connect(url).get();
            mALTextToSpeech = new ALTextToSpeech(mQiSession);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLinearLayoutMain.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            if (D) e.printStackTrace();
        }
    }

    /**
     * --- onClick Send ---
     */
    public void onClickSend(View view) {
        String text = mEditTextMessage.getText().toString();
        if ( text.length() > 0 ) {
            sayPepper(text);
        } else {
            toast_short(R.string.toast_enter_message);
        }
    }

    /**
     * --- onClick Speak ---
     */
    public void onClickSpeak(View view) {
        startRecognizer();
    }

    /**
     * startRecognizer
     */
    private void startRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        try {
            startActivityForResult(intent, REQUEST_SPEECH);
        } catch (Exception e) {
            if (D) e.printStackTrace();
            toast_long(R.string.toast_error);
        }
    }

    /**
     * --- onClick Repeat ---
     */
    public void onClickRepeat(View view) {
        CheckBox checkBox = (CheckBox) view;
        isRepeat = checkBox.isChecked();
    }

    /**
     * toast_long
     */
    private void toast_long( int id ) {
        Toast.makeText(this, id, Toast.LENGTH_LONG).show();
    }

    /**
     * toast_short
     */
    private void toast_short( int id ) {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
    }

    /**
     * log_d
     */
    private void log_d( String str ) {
        if (D) Log.d(TAG, str);
    }

}
