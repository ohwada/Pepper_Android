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
import android.widget.SeekBar;
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
public class MainActivity extends ListActivity
    implements AdapterView.OnItemClickListener {

    // debug
    private static final String TAG = Constant.TAG;
    private static final boolean D = Constant.DEBUG;

    // char
    private static final String LF = "\n";

    // activity
    private static final int REQUEST_CODE_LIST = Constant.REQUEST_CODE_LIST;
    private static final int REQUEST_CODE_CREATE = Constant.REQUEST_CODE_CREATE;
    private static final String BUNDLE_EXTRA_MSG  = Constant.BUNDLE_EXTRA_MSG;

    // database
    private static final int LIMIT = Constant.MAX_RECORD;
    private static final int SQL_ERREOR = -1;

    // Volume
    private static final int VOL_MAX = RobotController.VOL_MAX;
    private static final int VOL_PROGRESS = RobotController.VOL_PROGRESS;
    private static final int VOL_ERROR = RobotController.VOL_ERROR;

    // class object 
    private RobotController mRobotController;

    // View
    private ListView mListView;
    private EditText mEditTextIp;
    private EditText mEditTextMsg;
    private TextView mTextViewVolume;
    private SeekBar mSeekBarVolume;

    // List
    private MsgHelper mHelper;
    private SayAdapter mAdapter;
    private List<MsgRecord> mList = new ArrayList<MsgRecord>();

    // Pepper API
    private String mLanguage;
    private int mVolume = VOL_PROGRESS;

    /**
     * === onCreate ===
     */
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        initRobotController();       
        // View
        mEditTextIp = (EditText) findViewById( R.id.EditText_ip );
        mEditTextIp.setText( mRobotController.getPrefAddr() );
        mEditTextMsg = (EditText) findViewById( R.id.EditText_msg );
        mTextViewVolume = (TextView) findViewById( R.id.TextView_volume );
        setTextViewVolume( mVolume );
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
        // List
        mHelper = new MsgHelper( this );
        mAdapter = new SayAdapter( this, 0, mList );
        // list view
        mListView = getListView();
        mListView.setAdapter( mAdapter );
        mListView.setOnItemClickListener( this );
        // pepper
        mLanguage = getString( R.string.language );
    }

    /**
     * initRobotController
     */
    private void initRobotController() {
        mRobotController = new RobotController( this );
        mRobotController.execEmbeddedTool();
        mRobotController.setOnChangedListener( 
            new RobotController.OnChangedListener() { 
            @Override
            public void onConnectChanged( boolean isSuccess ) {
                procConnectChanged( isSuccess );
            }
        });
    }

    /**
     * procConnectChanged
     */
    private void procConnectChanged( boolean isSuccess ) {
        log_d( "procConnectChanged " + isSuccess );
        if ( !isSuccess ) return;
        int vol = mRobotController.getVolume();
        if ( vol == VOL_ERROR ) return;
        mVolume = vol;
        setTextViewVolume( vol );
        mSeekBarVolume.setProgress( vol );
    }

    /**
     * procProgressChanged
     * @param int progress
     */	
    private void procProgressChanged( int progress ) {
        log_d( "procProgressChanged " + progress );
        mVolume = progress;
        setTextViewVolume( progress );
    }

    /**
     * procStopTrackingTouch
     */	
    private void procStopTrackingTouch() {
        log_d( "procStopTrackingTouch" );
        mRobotController.setVolume( mVolume );
    }

    /**
     * setTextViewVolume
     */
    private void setTextViewVolume( int vol ) {
        mTextViewVolume.setText( Integer.toString(vol) );
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
        mRobotController.connect( ip );
    }

    /**
     * --- onClick Say ---
     */
    public void onClickSay( View view ) {
        log_d("onClickSay");
        String msg = getEditTextMsg();
        if ( "".equals(msg) ) {
            toast_short( R.string.toast_please_enter );
            return;
        }
        mRobotController.say( mLanguage, msg );
    }

    /**
     * --- onClick Add ---
     */
    public void onClickAdd( View view ) {
        String msg = getEditTextMsg();
        if ( "".equals(msg)) {
            toast_short( R.string.toast_please_enter );
            return;
        }
        Intent intent = new Intent( this, CreateActivity.class );
        intent.putExtra( BUNDLE_EXTRA_MSG, msg );
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
     * toast short
     */       
    private void toast_short( String str ) {
        ToastMaster.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    /**
     * toast short
     */       
    private void toast_short( int res_id ) {
        ToastMaster.makeText(this, res_id, Toast.LENGTH_SHORT).show();
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
