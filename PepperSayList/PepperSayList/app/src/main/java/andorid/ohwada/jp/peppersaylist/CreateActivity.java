/**
 * Pepper controlled by Android
 * 2015-08-01 K.OHWADA
 */

package andorid.ohwada.jp.peppersaylist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Create record
 */
public class CreateActivity extends SqlCommonActivity {

    // flag from MainActivity
    private boolean isAdd = false;

    /**
     * === onCreate ===
     * @param Bundle savedInstanceState 
     */	    	
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        execCreate();
        // hide update & delete button
        mButtonUpdate.setVisibility( View.GONE );
        mButtonDelete.setVisibility( View.GONE );
        // show
        showRocord();
    }

    /**
     * show rocord 
     */
    private void showRocord() {
        isAdd = false;
        clearText();	
        // get id from bundle
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();        
        // check bundle
        if ( bundle == null ) return;
        String msg = bundle.getString( BUNDLE_EXTRA_MSG );           
        // check id
        if ( "".equals( msg ) ) return;
        // set EditText
        mEditTextMsg.setText( msg );
        isAdd = true;
    }
	
    /**=
     * --- onClick Create ---
     */
    @Override	
    public void onClickRecCreate( View v ) {
        createRecord();
        clearText();
    }

    /**
     * create record 
     */
    private void createRecord() {   
        // get value
        String msg = getEditTextMsg();
        if ( "".equals(msg) ) {
            toast_short( R.string.toast_please_enter );
            return;
        }
        // save to DB
        MsgRecord r = new MsgRecord( 
            getEditTextTitle(), 
            msg ) ;
        long ret = mHelper.insert( r );        
        // message
        if ( ret > 0 ) {
            toast_short( R.string.toast_create_success );       		
        } else {	    	   
            toast_short( R.string.toast_create_failed );
        }
        // delete if Over
        mHelper.deleteIfOver();    
        // finish and back if from MainActivity
        if ( isAdd ) finish();
    }
	
}
