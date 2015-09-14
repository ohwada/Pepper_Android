/**
 * Pepper controlled by Android
 * 2015-08-01 K.OHWADA
 */

package andorid.ohwada.jp.peppersaylist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * adapter for ListView
 */
public class SayAdapter extends ArrayAdapter<MsgRecord> {
    // Layout Inflater
    private LayoutInflater mInflater = null;
			
    /**
     * === constractor ===
     * @param Context context
     * @param int resource
     * @param List<TextView> objects     
     * @return void	 
     */
    public SayAdapter( Context context, int resource, List<MsgRecord> objects ) {
        super( context, resource, objects );
        mInflater = (LayoutInflater) context.getSystemService( 
            Context.LAYOUT_INFLATER_SERVICE ) ;
    }

    /**
     * === get view ===
     * @param int position 
     * @param View convertView    
     * @param  ViewGroup parent      
     * @return View	 
     */
    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        View view = convertView;
        SayHolder h = null;              
        // once at first
        if ( view == null ) {
            // get view form xml
            view = mInflater.inflate( R.layout.say_item, null );
            // save 
            h = new SayHolder(); 
            h.tv_title = (TextView) view.findViewById( R.id.TextView_say_title );
            h.tv_msg = (TextView) view.findViewById( R.id.TextView_say_msg );
            view.setTag( h ); 
        } else {
            // load  
            h = (SayHolder) view.getTag();  
        }       
        // get item form Adapter
        MsgRecord item = (MsgRecord) getItem( position );
        // set value
        h.tv_title.setText( item.getTitleDisp() ) ;
        h.tv_msg.setText( item.getMsgDisp() ) ;		
        return view;
    }

    /**
     * holder class
     */	
    static class SayHolder { 
        public TextView tv_title;
        public TextView tv_msg;
    }
}
