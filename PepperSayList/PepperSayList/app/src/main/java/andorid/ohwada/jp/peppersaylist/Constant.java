/**
 * Pepper controlled by Android
 * 2015-08-01 K.OHWADA
 */

package andorid.ohwada.jp.peppersaylist;

/**
 * Constant
 */
public class Constant {

// debug
    public static final String TAG = "PepperSayList";
    public static final boolean DEBUG = true; 

// REQUEST CODE	
    public static final int REQUEST_CODE_LIST = 1;	    
    public static final int REQUEST_CODE_CREATE = 2;
    public static final int REQUEST_CODE_UPDATE = 3;	

// BUNDLE EXTRA
    public static final String BUNDLE_EXTRA_ID  = "id";
    public static final String BUNDLE_EXTRA_MSG  = "msg";

// database
    public static final int MAX_RECORD = 50;
}
