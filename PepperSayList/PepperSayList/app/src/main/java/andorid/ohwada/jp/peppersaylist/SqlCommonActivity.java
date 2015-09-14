/**
 * Pepper controlled by Android
 * 2015-08-01 K.OHWADA
 */

package andorid.ohwada.jp.peppersaylist;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * SQLite Common Activity
 */
public class SqlCommonActivity extends Activity {
    // dubug
    protected static final boolean D = Constant.DEBUG; 

    // activity    
    protected static final String BUNDLE_EXTRA_ID  = Constant.BUNDLE_EXTRA_ID;
    protected static final String BUNDLE_EXTRA_MSG  = Constant.BUNDLE_EXTRA_MSG;
    
    // View 
    protected TextView mTextViewId;
    protected TextView mTextViewTime;
    protected EditText mEditTextTitle;
    protected EditText mEditTextMsg;
    protected Button mButtonCreate;
    protected Button mButtonUpdate;
    protected Button mButtonDelete;
    protected Button mButtonBack;

    // sql
    protected MsgHelper mHelper;

    /**
     * execCreate
     */    
    protected void execCreate() {
        setContentView( R.layout.activity_record );
        // sql
        mHelper = new MsgHelper( this );
        // view conponent
        mTextViewId = (TextView) findViewById( R.id.TextView_rec_id );
        mTextViewTime = (TextView) findViewById( R.id.TextView_rec_time );
        mEditTextTitle = (EditText) findViewById( R.id.EditText_rec_title );
        mEditTextMsg = (EditText) findViewById( R.id.EditText_rec_msg );
        mButtonCreate = (Button) findViewById( R.id.Button_rec_create );
        mButtonUpdate = (Button) findViewById( R.id.Button_rec_update );
        mButtonDelete = (Button) findViewById( R.id.Button_rec_delete );
        mButtonBack = (Button) findViewById( R.id.Button_rec_back );
     }

    /**
     * --- onClick Back ---
     */
    public void onClickRecBack( View v ) {
        finish();
    }

    /**
     * --- onClick Create ---
     */ 
    public void onClickRecCreate( View v ) {
    	// dummy
    }

    /**
     * --- onClick Update ---
     */ 
    public void onClickRecUpdate( View v ) {
    	// dummy
    }

    /**
     * --- onClick Delete ---
     */ 
    public void onClickRecDelete( View v ) {
    	// dummy
    }
    
    /**
     * get EditTextTitle
     * @return String 
     */                    
    protected String getEditTextTitle() {
        return mEditTextTitle.getText().toString().trim();
    }

    /**
     * get EditTextMsg
     * @return String 
     */
    protected String getEditTextMsg() {
        return mEditTextMsg.getText().toString().trim();
    }

    /**
     * get EditTextTime
     * @return long
     */        
    protected long getEditTextTime() {
        String str = mTextViewTime.getText().toString().trim();
        if ( "".equals(str) ) return 0;
        return Long.parseLong( str );
    }

    /**
     * clear Text   
     */                
    protected void clearText() {    
        mTextViewId.setText("0");
        mTextViewTime.setText("0");
        mEditTextTitle.setText("");
        mEditTextMsg.setText("");
    }

// --- debug ---         
    /**
     * toast short
     * @param int id
     */ 
    protected void toast_short( int id ) {
        ToastMaster.makeText( this, id, Toast.LENGTH_SHORT ).show();
    }
}
