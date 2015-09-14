/**
 * Pepper controlled by Android
 * 2015-08-01 K.OHWADA
 */

package andorid.ohwada.jp.peppersaylist;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * record 
 */
public class MsgRecord {

    // limit
    private static final int MAX_TITLE = 10; 
    private static final int MAX_MSG = 100; 

    // char
    private static final String LF = "\n";
    private static final String SPACE = " ";
    private static final String LEADER = "...";

    // DateFormat
    private SimpleDateFormat mFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss", Locale.ENGLISH );
	
    // column
    public int id;
    public long time;
    public String title;
    public String msg;

    /**
     * === constractor for helper ===
     * @param int _id 
     * @param long _time
     * @param String _title 
     * @param String _msg 
     */
    public MsgRecord( int _id, long _time, String _title, String _msg ) {
        setRecord( _id, _time, _title, _msg );
    }

    /**
     * === constractor for update ===
     * @param int _id 
     * @param String _title 
     * @param String _msg 
     */
    public MsgRecord( int _id, String _title, String _msg ) {
        setRecord( _id, getCurrentTime(), _title, _msg );
    }

    /**
     * === constractor for create ===
     * @param String _title 
     * @param String _msg 
     */
    public MsgRecord( String _title, String _msg ) {
        setRecord( 0, getCurrentTime(), _title, _msg );
    }

    /**
     * === constractor ===
     * @param int _id 
     * @param long _time
     * @param String _title
     * @param String _msg  
    */	
    public void setRecord( int _id, long _time, String _title, String _msg ) {
        id = _id;
        time = _time;
        title = _title;
        msg = _msg;
    }
	
    /**
     * ID
     * @return String
     */		
    public String getIdString() {
        return Integer.toString( id );
    }

    /**
     * Title
     * @return String
     */	
    public String getTitleDisp() {
        if ( title.length() > 0 ) {
            return formatDisp( title, MAX_TITLE );
        }
        return formatDisp( msg, MAX_TITLE );
    } 

    /**
     * Message
     * @return String
     */
    public String getMsgDisp() {
        return formatDisp( msg, MAX_MSG );
    } 

    /**
     * formatDisp
     * @param String str 
     * @param int max 
     * @return String
     */
    private String formatDisp( String str, int max ) {
        String text = str.replace( LF, SPACE );
        if ( text.length() > max ) {
            String ret = text.substring( 0, max ) + LEADER;
            return ret;
        }
        return text;
    }

    /**
     * Time
     * @return String
     */
    public String getTimeDisp() {
        return mFormat.format( time );
    }

    /**
     * Time
     */
    public void setTimeNow() {
        time = getCurrentTime();
    }

    /**
     * getCurrentTime
     * @param long
     */
    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

}
