/**
 * Pepper controlled by Android
 * 2015-08-01 K.OHWADA
 */

package andorid.ohwada.jp.peppersaylist;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;

/**
 * Language Dialog
 */
public class LanguageDialog extends Dialog {

    // constant
    private static final float WIDTH_RATIO_FULL = 0.95f;

    private static final String LANGUAGE_ENGLISH = "English";
    private static final String LANGUAGE_JAPANESE = "Japanese";

    // interface
    private OnCheckedChangedListener mCheckedChangedListener;

    private String mLanguage;

    /**
     * interface
     */ 		    
    public interface OnCheckedChangedListener {
        void onCheckedChanged( LanguageDialog dialog, String language );
    }

    /**
     * setOnCheckedChangedListener
     * @param OnCheckedChangedListener listener
     */  
    public void setOnCheckedChangedListener( OnCheckedChangedListener listener ) {
        mCheckedChangedListener = listener;
    }
	
    /**
    * === Constructor ===
    * @param Context context
    */ 	
    public LanguageDialog( Context context ) {
        super( context );
    }

    /**
     * === Constructor ===
     * @param Context context
     * @param int theme
     */ 
    public LanguageDialog( Context context, int theme ) {
        super(context, theme);
    }
				
    /**
     * create
     * @param String Language
     */ 	
    public void create( String language ) {
        setContentView( R.layout.dialog_language );
        setTitle(R.string.menu_language );
        Button btn = (Button) findViewById( R.id.Button_language_back );
        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                dismiss();
            }
        });
        RadioGroup rb = (RadioGroup) findViewById( R.id.RadioGroup_language );
        if ( LANGUAGE_ENGLISH.equals(language) ) {
            rb.check( R.id.RadioButton_language_english );
        } else if ( LANGUAGE_JAPANESE.equals(language) ) {
            rb.check( R.id.RadioButton_language_japanese );
        }
        rb.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                procCheckedChanged(group, checkedId);
            }
        });
    }

    /**
     * setLayout
    */ 
    private void setLayoutFull() {
        int width = (int)( getWindowWidth() * WIDTH_RATIO_FULL );
        getWindow().setLayout( width, ViewGroup.LayoutParams.WRAP_CONTENT );
    }

    /**
     * getWindowWidth
     * @return int 
     */ 
    private int getWindowWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService( Context.WINDOW_SERVICE );
        Display display = wm.getDefaultDisplay();
        // Display#getWidth() This method was deprecated in API level 13
        return display.getWidth();
    }

    /**
     * setGravity
     */ 
    private void setGravityBottom() {
        // show on the lower of screen. 
        getWindow().getAttributes().gravity = Gravity.BOTTOM;
    }

    /**
     * procCheckedChanged
     */ 
    private void procCheckedChanged( RadioGroup group, int checkedId ) {
        if ( checkedId == R.id.RadioButton_language_english ){
            notifyCheckedChanged( LANGUAGE_ENGLISH );
        } else if ( checkedId == R.id.RadioButton_language_japanese ){
            notifyCheckedChanged( LANGUAGE_JAPANESE );
        }
    }

    /**
     * notifyCheckedChanged
     */
    private void notifyCheckedChanged( String language ) {
        if ( mCheckedChangedListener != null ) {
            mCheckedChangedListener.onCheckedChanged( this, language );
        }
    }
}
