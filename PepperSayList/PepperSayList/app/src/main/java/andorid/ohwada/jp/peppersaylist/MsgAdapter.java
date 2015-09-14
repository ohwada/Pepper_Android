/**
 * Pepper controlled by Android
 * 2015-08-01 K.OHWADA
 */

package andorid.ohwada.jp.peppersaylist;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * adapter for ListView
 */
public class MsgAdapter extends ArrayAdapter<MsgRecord> {
    // Layout Inflater
    private LayoutInflater mInflater;
			
    /**
     * === constractor ===
     * @param Context context
     * @param int resource
     * @param List<MsgRecord> objects     
     */
    public MsgAdapter( Context context, int resource, List<MsgRecord> objects ) {
        super( context, resource, objects );
        mInflater = (LayoutInflater) context.getSystemService( 
            Context.LAYOUT_INFLATER_SERVICE ) ;
    }

    /**
     * === get view === 
     */
    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        View view = convertView;
        MsgHolder h = null;              
        // once at first
        if ( view == null ) {
            // get view form xml
            view = mInflater.inflate( R.layout.msg_item, null );
            // save 
            h = new MsgHolder(); 
            h.tv_id = (TextView) view.findViewById( R.id.TextView_msg_id ); 
            h.tv_time = (TextView) view.findViewById( R.id.TextView_msg_time );
            h.tv_title = (TextView) view.findViewById( R.id.TextView_msg_title );
            h.tv_msg = (TextView) view.findViewById( R.id.TextView_msg_msg );
            view.setTag( h ); 
        } else {
            // load  
            h = (MsgHolder) view.getTag();  
        }       
        // get item form Adapter
        MsgRecord item = (MsgRecord) getItem( position );
        // set value
        h.tv_id.setText( item.getIdString() ) ;
        h.tv_title.setText( item.getTitleDisp() ) ;
        h.tv_msg.setText( item.getMsgDisp() ) ;
        h.tv_time.setText( item.getTimeDisp() );		
        return view;
    }

    /**
     * holder class
     */	
    static class MsgHolder { 
        public TextView tv_id;
        public TextView tv_time;
        public TextView tv_title;
        public TextView tv_msg;
    } 
}
