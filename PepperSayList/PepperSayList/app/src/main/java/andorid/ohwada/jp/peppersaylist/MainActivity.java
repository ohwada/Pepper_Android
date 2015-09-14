/**
 * Pepper controlled by Android
 * 2015-08-01 K.OHWADA
 */

package andorid.ohwada.jp.peppersaylist;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
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
public class MainActivity extends ListActivity
    implements AdapterView.OnItemClickListener {

    // debug
    private static final String TAG = Constant.TAG;
    private static final boolean D = Constant.DEBUG;

    // Pepper Connection
    private static final String IP_KEY = "ip_address";
    private static final String IP_DEFAULT = "192.168.1.1";
    private static final String PORT = "9559";
    private static final String IP_PATTERN = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

    // activity
    private static final int REQUEST_CODE_LIST = Constant.REQUEST_CODE_LIST;
    private static final int REQUEST_CODE_CREATE = Constant.REQUEST_CODE_CREATE;
    private static final String BUNDLE_EXTRA_MSG  = Constant.BUNDLE_EXTRA_MSG;

    // database
    private static final int LIMIT = Constant.MAX_RECORD;
    private static final int SQL_ERREOR = -1;

    // View
    private ListView mListView;
    private EditText mEditTextIp;
    private EditText mEditTextMsg;

    // List
    private MsgHelper mHelper;
    private SayAdapter mAdapter;
    private List<MsgRecord> mList = new ArrayList<MsgRecord>();

    private SharedPreferences mPreferences;

    // Pepper API
    private Session mQiSession;
    private String mLanguage;

    /**
     * === onCreate ===
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);        
        // View
        mEditTextIp = (EditText) findViewById( R.id.EditText_ip );
        mEditTextIp.setText( mPreferences.getString(IP_KEY, IP_DEFAULT) );
        mEditTextMsg = (EditText) findViewById( R.id.EditText_msg );
        // List
        mHelper = new MsgHelper( this );
        mAdapter = new SayAdapter( this, 0, mList );
        // list view
        mListView = getListView();
        mListView.setAdapter( mAdapter );
        mListView.setOnItemClickListener( this );
        // pepper
        mLanguage = getString( R.string.language );
        // Embedded Tools
        EmbeddedTools tools = new EmbeddedTools();
        File dir = getApplicationContext().getCacheDir();
        log_d( "Extracting libraries in " + dir.getAbsolutePath() );
        tools.overrideTempDirectory(dir);
        tools.loadEmbeddedLibraries();
    }

    /**
     * === onResume ===
     */
    @Override
    public void onResume() {
        super.onResume();
        showList();	    
    }

    /**
     * get records & showList 
     */
    private void showList() {
        mList.clear();
        List<MsgRecord> list = mHelper.getListOrderTime( LIMIT );
        if ( list != null ) {
            mList.addAll( list );
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * --- onClick Connect ---
     */
    public void onClickConnect( View view ) {
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
     * --- onClick Say ---
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
        try {
            ALTextToSpeech tts = new ALTextToSpeech( mQiSession );			
            tts.setLanguage( mLanguage );
            tts.say( getEditTextMsg() );
        } catch (Exception e) {
            toastOnUiThread( e, R.string.toast_speech_failed );
            if (D) e.printStackTrace();
        }
    }

    /**
     * --- onClick Add ---
     */
    public void onClickAdd( View view ) {
        Intent intent = new Intent( this, CreateActivity.class );
        intent.putExtra( BUNDLE_EXTRA_MSG, getEditTextMsg() );
        startActivityForResult(intent, REQUEST_CODE_CREATE);
    }

    /**
     * getEditTextMsg
     */
    private String getEditTextMsg() {
        return mEditTextMsg.getText().toString().trim();
    }

    /** 
     * === onItemClick ===	 
     */
    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
        if (id == -1 ) {
            // footer : noting to do
        } else {
            // item
            clickItem( position );
        }
    }

    /**
     * when click Item 
     * @param int n	 
     */	
    private void clickItem( int n ) {	
        // check position
        if (( n < 0 )||( n >= mList.size() )) return;
        // get msg record	
        MsgRecord record = mList.get( n );
        if ( record == null ) return;
        // show message
        mEditTextMsg.setText(record.msg);
        // update timestamp
        record.setTimeNow();
        int ret = mHelper.update( record );         
        // message
        if ( ret == SQL_ERREOR ) {    	   
            log_d( R.string.toast_update_failed );
        }
        showList();
    }

    /**
     * === onCreateOptionsMenu ===
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * === onOptionsItemSelected ===
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int id = item.getItemId();
        if ( id == R.id.action_list ) {  
            startMsgListActivity(); 
        } else if ( id == R.id.action_language ) {
            selectLanguage(); 
        }
        return true;
    }

    /**
     * startMsgListActivity
     */ 
    private void startMsgListActivity() {
       Intent intent = new Intent( this, MsgListActivity.class );
        startActivityForResult(intent, REQUEST_CODE_LIST);
    }

    /**
     * selectLanguage
     */ 
    private void selectLanguage() {
        LanguageDialog dialog = new LanguageDialog(this);
        dialog.create( mLanguage );
        dialog.setOnCheckedChangedListener( new LanguageDialog.OnCheckedChangedListener() {
            @Override
            public void onCheckedChanged( LanguageDialog dialog, String language ) {
                mLanguage = language;
            }
        });
        dialog.show();
    }

    /**
     * === onActivityResult ===	 
     */	    	    
    @Override
    protected void onActivityResult( int request, int result, Intent data ) {
        super.onActivityResult(request, result, data);
        // noting to do, pass to onResume 
    }

    /**
     * toast on UI thread
     */
    private void toastOnUiThread( Exception e, int res_id) {
        String str = getString(res_id);
        toastOnUiThread(str + "\n" + e.getMessage());
    }

    /**
     * toast on UI thread
     */
    private void toastOnUiThread( int res_id ) {
        final int id = res_id;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast_short(id);
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
    private void log_d( int res_id ) {
        log_d( getString(res_id) );
    }

    /**
     * log_d
     */ 
    private void log_d( String str ) {
        if (D) Log.d( TAG, str );
    }

}
