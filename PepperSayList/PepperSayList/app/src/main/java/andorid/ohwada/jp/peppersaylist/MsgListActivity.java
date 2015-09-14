/**
 * Pepper controlled by Android
 * 2015-08-01 K.OHWADA
 */

package andorid.ohwada.jp.peppersaylist;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Create Read Update Delete
 */
public class MsgListActivity extends ListActivity
    implements OnItemClickListener {  
	
    // activity
    private static final String BUNDLE_EXTRA_ID  = Constant.BUNDLE_EXTRA_ID;
    private static final int REQUEST_CODE_CREATE = Constant.REQUEST_CODE_CREATE;
    private static final int REQUEST_CODE_UPDATE = Constant.REQUEST_CODE_UPDATE;

    // database
    private static final int LIMIT = Constant.MAX_RECORD;
   	
    // View
    private ListView mListView;

    // List
    private MsgHelper mHelper;
    private MsgAdapter mAdapter;
    private List<MsgRecord> mList = new ArrayList<MsgRecord>();

    /**
     * === onCreate === 
     */		
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_msg_list );			        	        
        // List
        mHelper = new MsgHelper( this );	
        mAdapter = new MsgAdapter( this, 0, mList );
        // list view
        View headerView = getLayoutInflater().inflate( R.layout.msg_header, null );
        mListView = getListView();
        mListView.addHeaderView( headerView );
        mListView.setAdapter( mAdapter );
        mListView.setOnItemClickListener( this );	
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
        List<MsgRecord> list = mHelper.getListOrderId( LIMIT );
        if ( list != null ) {
            mList.addAll( list );
        }
        mAdapter.notifyDataSetChanged();				    
    }

    /**
     * --- onClick ListCreate ---
     */
    public void onClickListCreate( View view ) {
        startCreateActivity();
    }

    /**
     * start create activity
     */
    private void startCreateActivity() {
        Intent intent = new Intent( this, CreateActivity.class );
        startActivityForResult( intent, REQUEST_CODE_CREATE );
    }

    /**
     * --- onClick ListBack ---
     */
    public void onClickListBack( View view ) {
        finish();
    }

    /** 
     * === onItemClick === 
     */
    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
        if ( position == 0 ) {
            // header : noting to do
        } else if (id == -1 ) {
            // footer : noting to do
        } else {
            // item
            clickItem( position );
        }
    }

    /**
     * when click Item
     * @param int position
     */	
    private void clickItem( int position ) {	
        // check position
        int n = position - 1;
        if (( n < 0 )||( n >= mList.size() )) return;
        // get record (Read)	
        MsgRecord record = mList.get( n );
        if ( record == null ) return;
        // startActivity
        startUpdateActivity( record.id );				
    }

    /**
     * start update activity with id
     * @param int id
     */
    private void startUpdateActivity( int id ) {
        Intent intent = new Intent( this, UpdateActivity.class );
        Bundle bandle = new Bundle();
        bandle.putInt( BUNDLE_EXTRA_ID, id );
        intent.putExtras( bandle );
        startActivityForResult( intent, REQUEST_CODE_UPDATE );
    }

    /**
     * === onActivityResult ===
     */	    	    
    @Override
    protected void onActivityResult( int request, int result, Intent data ) {
        super.onActivityResult( request, result, data );
        // noting to do, pass to onResume     	
    }
    
    /**
     * toast short
     * @param int id
     */ 
    private void toast_short( int id ) {
        ToastMaster.makeText( this, id, Toast.LENGTH_SHORT ).show();
    }
}
