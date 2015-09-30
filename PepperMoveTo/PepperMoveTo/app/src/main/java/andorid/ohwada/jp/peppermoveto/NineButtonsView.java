/**
 * Pepper controlled by Android
 * 2015-09-01 K.OHWADA
 */

package andorid.ohwada.jp.peppermoveto;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * NineButtons View
 */
public class NineButtonsView {

    // Button
    public static final int BUTTON_FORWARD_LEFT = 1;
    public static final int BUTTON_FORWARD = 2;
    public static final int BUTTON_FORWARD_RIGHT = 3;
    public static final int BUTTON_LEFT = 4;
    public static final int BUTTON_CENTER = 5;
    public static final int BUTTON_RIGHT = 6;
    public static final int BUTTON_BACK_LEFT = 7;
    public static final int BUTTON_BACK = 8;
    public static final int BUTTON_BACK_RIGHT = 9;
						
    // alpha
    private static final int ALPHA_OFF = 50;
    private static final int ALPHA_ON = 255;

    private static final long TOUCH_SENSITIVITY = 1000;  // 1sec

    // callback
    private OnButtonTouchListener mOnTouchListener;

    private Context mContext;
    private DisplayMetrics mDisplayMetrics;
    	     	
    // view			
    private ImageView mImageViewForwardLeft ;   
    private ImageView mImageViewForward ;
    private ImageView mImageViewForwardRight ;
    private ImageView mImageViewLeft ;
    private ImageView mImageViewCenter ;
    private ImageView mImageViewRight ;
    private ImageView mImageViewBackLeft ;   
    private ImageView mImageViewBack ;
    private ImageView mImageViewBackRight ;

    // timing adjustment
    private int mPrevAction = 0;
    private int mPrevButton = 0;
    private long mPrevTime = 0;

    /**
     * interface OnTouchListener
     */
    public interface OnButtonTouchListener {
        void onTouch( View view, MotionEvent event, int buton );
    }
	
    /**
     * === Constractor ===
     * @param Context context
     * @param View view
     */	
    public NineButtonsView( Context context, View view ) {     
        mContext = context;
        mDisplayMetrics = context.getResources().getDisplayMetrics();       	
        mImageViewForwardLeft = (ImageView) view.findViewById( R.id.ImageView_nine_forward_left );   
        mImageViewForward = (ImageView) view.findViewById( R.id.ImageView_nine_forward );
        mImageViewForwardRight = (ImageView) view.findViewById( R.id.ImageView_nine_forward_right );
        mImageViewLeft = (ImageView) view.findViewById( R.id.ImageView_nine_left );
        mImageViewCenter = (ImageView) view.findViewById( R.id.ImageView_nine_center );
        mImageViewRight = (ImageView) view.findViewById( R.id.ImageView_nine_right );
        mImageViewBackLeft = (ImageView) view.findViewById( R.id.ImageView_nine_back_left );   
        mImageViewBack = (ImageView) view.findViewById( R.id.ImageView_nine_back );
        mImageViewBackRight = (ImageView) view.findViewById( R.id.ImageView_nine_back_right );
        setSizeByOrientation();
    }

    /**
     * setOnTouchListener
     * @param OnTouchListener listener
     */
    public void setOnTouchListener( OnButtonTouchListener listener ) {
        mOnTouchListener = listener;
        mImageViewForwardLeft.setOnTouchListener( new OnTouchListener() { 
            @Override 
            public boolean onTouch( View view, MotionEvent event ) {
                procTouch( view, event, BUTTON_FORWARD_LEFT ); 
                return true; 
            } 
        });
        mImageViewForward.setOnTouchListener( new OnTouchListener() { 
            @Override 
            public boolean onTouch( View view, MotionEvent event ) {
                procTouch( view, event, BUTTON_FORWARD ); 
                return true; 
            } 
        });
        mImageViewForwardRight.setOnTouchListener( new OnTouchListener() { 
            @Override 
            public boolean onTouch( View view, MotionEvent event ) {
                procTouch( view, event, BUTTON_FORWARD_RIGHT ); 
                return true; 
            } 
        });
        mImageViewLeft.setOnTouchListener( new OnTouchListener() { 
            @Override 
            public boolean onTouch( View view, MotionEvent event ) {
                procTouch( view, event, BUTTON_LEFT ); 
                return true; 
            } 
        });
        mImageViewCenter.setOnTouchListener( new OnTouchListener() {
            @Override
            public boolean onTouch( View view, MotionEvent event ) {
                // exception for Center button
                // No timing adjustment by sensitivity
                mOnTouchListener.onTouch( view, event, BUTTON_CENTER ); 
                return true;
            }
        });
        mImageViewRight.setOnTouchListener( new OnTouchListener() { 
            @Override 
            public boolean onTouch( View view, MotionEvent event ) {
                procTouch( view, event, BUTTON_RIGHT ); 
                return true; 
            } 
        });
        mImageViewBackLeft.setOnTouchListener( new OnTouchListener() { 
            @Override 
            public boolean onTouch( View view, MotionEvent event ) {
                procTouch( view, event, BUTTON_BACK_LEFT ); 
                return true; 
            } 
        });
        mImageViewBack.setOnTouchListener( new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                procTouch(view, event, BUTTON_BACK);
                return true;
            }
        });
        mImageViewBackRight.setOnTouchListener( new View.OnTouchListener() { 
            @Override 
            public boolean onTouch( View view, MotionEvent event ) {
                procTouch( view, event, BUTTON_BACK_RIGHT ); 
                return true; 
            } 
        }); 
    }

    /**
     * procTouch
     */
    private void procTouch( View view, MotionEvent event, int button ) {
        int action = event.getAction();
        long time = System.currentTimeMillis();
        // timing adjustment, when same action and same button
        if (( action != mPrevAction )||( button != mPrevButton )||(( time - mPrevTime ) > TOUCH_SENSITIVITY )) {
            mOnTouchListener.onTouch( view, event, button ); 
            mPrevAction = action;
            mPrevButton = button;
            mPrevTime = time;
        }
    }

    /**
     * setSize By Orientation
     */
    public void setSizeByOrientation() {
        if( getOrientation() == Configuration.ORIENTATION_LANDSCAPE ) {
            setSizeForLandscape();
        } else {
            setSizeForPortrait();
        }
    }	

    /**
     * getOrientation
     */
    public int getOrientation() {
        Configuration config = mContext.getResources().getConfiguration();
        return config.orientation;
    }	
	
    /**
     * setSize For Portrait
     */
    public void setSizeForPortrait() {
    	Point point = getDisplaySize();
    	int size = adjustSize( point.x / 4 );
    	setSize( size );
    }

    /**
     * setSize For Landscape
     */
    public void setSizeForLandscape() {
    	Point point = getDisplaySize();
    	int size = adjustSize( point.y / 4.5f );
    	setSize( size );
    }

    /**
     * setSize
     * @param float size
     */
    private void setSize( int size ) {
        TableRow.LayoutParams params = new TableRow.LayoutParams( size, size );
        mImageViewForwardLeft.setLayoutParams( params );  
        mImageViewForward.setLayoutParams( params );
        mImageViewForwardRight.setLayoutParams( params );
        mImageViewLeft.setLayoutParams( params );
        mImageViewCenter.setLayoutParams( params );
        mImageViewRight.setLayoutParams( params );
        mImageViewBackLeft.setLayoutParams( params ); 
        mImageViewBack.setLayoutParams( params );
        mImageViewBackRight.setLayoutParams( params );		
    }

    /**
     * getDisplaySize
     * @return Point
     */   
    private Point getDisplaySize() {
        WindowManager wm = (WindowManager) mContext.getSystemService( Context.WINDOW_SERVICE );
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize( point );
        return point;
    }

    /**
     * adjustSize
     * @param float size
     * @return int
     */ 
    private int adjustSize( float size ) {
        float max = getPixcelSize( TypedValue.COMPLEX_UNIT_DIP, 100 );
        float min = getPixcelSize( TypedValue.COMPLEX_UNIT_DIP,  50 );
        if ( size > max ) {
            size = max;
        }
        if ( size < min ) {
            size = min;
        }
        return (int)size;
    }

    /**
     * Get the pixcel size from a given unit and value.  
     * @param unit The desired dimension unit.
     * @param size The desired size in the given units.
     */
    private float getPixcelSize( int unit, float size ) {    
        return TypedValue.applyDimension( unit, size, mDisplayMetrics );
    }
 	       
    /**
     * setCenterStop
     */
    public void setCenterStop() {
        setOnEightButons();
        mImageViewCenter.setImageResource( R.drawable.robot_stop )	;
    }

    /**
     * setCenterRun
     */
    public void setCenterRun() {        
        setOffEightButons();
        mImageViewCenter.setImageResource( R.drawable.robot_center )	;	
    }

    /**
     * setOnEightButons
     */				
    public void setOnEightButons() {
        setAlphaEightButons( ALPHA_ON );
    }

    /**
     * setOffEightButons
     */	
    private void setOffEightButons() {
        setAlphaEightButons( ALPHA_OFF );	
    }

    /**
     * setAlphaEightButons
     * @param int alpha
     */	
    private void setAlphaEightButons( int alpha ) {
        mImageViewForwardLeft.setImageAlpha( alpha );					
        mImageViewForward.setImageAlpha( alpha );	
        mImageViewForwardRight.setImageAlpha( alpha );	
        mImageViewLeft.setImageAlpha( alpha );	
        mImageViewRight.setImageAlpha( alpha );	
        mImageViewBackLeft.setImageAlpha( alpha );	
        mImageViewBack.setImageAlpha( alpha );	
        mImageViewBackRight.setImageAlpha( alpha );	
    }
	
}
