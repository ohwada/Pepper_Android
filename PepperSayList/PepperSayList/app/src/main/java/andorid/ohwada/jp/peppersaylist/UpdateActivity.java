/**
 * Pepper controlled by Android
 * 2015-08-01 K.OHWADA
 */

package andorid.ohwada.jp.peppersaylist;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

/**
 * Update & Delete record
 */
public class UpdateActivity extends SqlCommonActivity {
	
    // id of item
    private int mId = 0; 

    /**
     * === onCreate === 
     */	    	
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        execCreate();		
        // hide create button		
        mButtonCreate.setVisibility( View.GONE );
        // show
        showRocord();
    }
    
    /**
     * show rocord 
     */
    private void showRocord() {	
        // get id from bundle
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();        
        // check bundle
        if ( bundle == null ) return;
        mId = bundle.getInt( BUNDLE_EXTRA_ID );
        // check id
        if ( mId == 0 ) return;
        // get record (Read)
        MsgRecord r = mHelper.getRecordById( mId ) ; 
        // set Text
        mTextViewId.setText( Integer.toString(mId) );
        mTextViewTime.setText( r.getTimeDisp() );
        mEditTextTitle.setText( r.title );
        mEditTextMsg.setText( r.msg );
    }

    /**
     * -- onClick Update ---
     */
    @Override		
    public void onClickRecUpdate( View v ) {
    	updateRecord();
    }

    /**
     * update record 
     */
    private void updateRecord() {  
        String msg = getEditTextMsg();
        if ( "".equals(msg) ) {
            toast_short( R.string.toast_please_enter );
            return;
        } 	
        // save to DB
        MsgRecord r = new MsgRecord( 
            mId,
            getEditTextTitle(), 
            msg );
        int ret = mHelper.update( r );         
        // message
        if ( ret > 0 ) {
            toast_short( R.string.toast_update_success );       		
        } else {	    	   
            toast_short( R.string.toast_update_failed );
        }
    }

    /**
     * --- onClick Delete ---
     */
    @Override		
    public void onClickRecDelete( View v ) {
        deleteRecord();
        disableButton();
        clearText();
    }

    /**
     * delete record 
     */
    private void deleteRecord() {   
        // delete from DB
        int ret = mHelper.delete( mId ); 
        // message        
        if ( ret > 0 ) {
            toast_short( R.string.toast_delete_success );       		
        } else {	    	   
            toast_short( R.string.toast_delete_failed );
        }
        finish();      	   
    }

    /**
     * disable button	 
     */
    private void disableButton() {
        // disable click 	
        mButtonUpdate.setClickable( false );
        mButtonDelete.setClickable( false );
        // set gray color
        mButtonUpdate.setTextColor( Color.GRAY );
        mButtonDelete.setTextColor( Color.GRAY );
    }					
}
