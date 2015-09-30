/**
 * Pepper controlled by Android
 * using Aldebaran Android SDK 
 * 2015-09-01 K.OHWADA
 */

package andorid.ohwada.jp.pepperjoint;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aldebaran.qi.EmbeddedTools;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.proxies.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity
 */
public class MainActivity extends Activity {
    // debug
    private static final String TAG = "PepperJoint";
    private static final boolean D = true;

    // char
    private static final String LF = "\n";
    private static final String COMMA_SPACE = ", ";

    // Coefficient
    private static final float DEG_TO_RAD = (float)Math.PI / 180f ;
    private static final float RAD_TO_DEG = 180f / (float)Math.PI ;

    // Pepper Connection
    private static final String IP_KEY = "ip_address";
    private static final String IP_DEFAULT = "192.168.1.1";
    private static final String IP_PORT = "9559";
    private static final String IP_PATTERN = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

    // command
    private static final int CMD_CONNECT = 1;
    private static final int CMD_ANGLE = 2;

    private static final int RES_TOAST = 1;
    private static final int RES_JOINT_INFO = 2;

    // joint
    private static final int NUM_JOINT = 15;
    private static final int JOINT_HEAD_YAW = 0;
    private static final int JOINT_HEAD_PITCH = 1;
    private static final int JOINT_L_SHOULDER_PITCH = 2;
    private static final int JOINT_L_SHOULDER_ROLL = 3;
    private static final int JOINT_L_ELBOW_YAW = 4;
    private static final int JOINT_L_ELBOW_ROLL = 5;
    private static final int JOINT_L_WRIST_YAW = 6;
    private static final int JOINT_R_SHOULDER_PITCH = 7;
    private static final int JOINT_R_SHOULDER_ROLL = 8;
    private static final int JOINT_R_ELBOW_YAW = 9;
    private static final int JOINT_R_ELBOW_ROLL = 10;
    private static final int JOINT_R_WRIST_YAW = 11;
    private static final int JOINT_HIP_ROLL = 12;
    private static final int JOINT_HIP_PITCH = 13;
    private static final int JOINT_KNEE_PITCH = 14;

    private static final int NUM_VIEW = 15; 

    private static final String[] JOINT_NAMES = 
        { "HeadYaw", "HeadPitch", 
        "LShoulderPitch", "LShoulderRoll", "LElbowYaw", "LElbowRoll", "LWristYaw", 
        "RShoulderPitch", "RShoulderRoll", "RElbowYaw", "RElbowRoll", "RWristYaw", 
        "HipRoll", "HipPitch", "KneePitch",
        "LHand", "RHand" };

    private static final float[][] JOINT_VALEES = 
        {
        // HeadYaw
            {-2.0856686f, 2.0856686f, 0f}, 
        // HeadPitch
            {-0.70685834f, 0.6370452f, 0f}, 
        // LShoulderPitch
            {-2.0856686f, 2.0856686f, 0f},
        // LShoulderRoll 8deg
            {0.008726646f, 1.5620697f, 0.14f},
        // LElbowYaw 
           {-2.0856686f, 2.0856686f, 0f},
        // LElbowRoll 30deg
            {-1.5620697f, -0.008726646f, -0.523f},
        // LWristYaw
            {-1.8238691f, 1.8238691f, 0f},
        // RShoulderPitch
            {-2.0856686f, 2.0856686f, 0f},
        // RShoulderRoll -8deg
            {-1.5620697f, -0.008726646f, -0.14f},
        // RElbowYaw
            {-2.0856686f, 2.0856686f, 0f},
        // RElbowRoll 30deg
            {0.008726646f, 1.5620697f, 0.523f},
        // RWristYaw
            {-1.8238691f, 1.8238691f, 0f},
        // HipRoll
            {-0.51487213f, 0.51487213f, 0f},
        // HipPitch
            {-1.0384709f, 1.0384709f, 0f},
        // KneePitch
            {-0.51487213f, 0.51487213f, 0f}
        }; 

    private static final String BODY_NAME_JOINT = "Body";
    private static final boolean USE_SENSORS = true;
    private static final float STIFFNESSE = 1.0f; 
    private static final float SPEED = 0.2f; 
    private static final float ANGLE_ERROR = 10f;

    // View
    private EditText mEditTextIp;
    private TextView mTextViewInfo;
    private TextView[] mTextViewJoints = new TextView[NUM_VIEW];
    private SeekBar[] mSeekBarJoints = new SeekBar[NUM_VIEW];

    // object
    private SharedPreferences mPreferences;
    private Resources mResources;

    // Pepper API
    private Session mQiSession;
    private ALMotion mALMotion;

    private String mAddr = "";
    private int mJointNum = 0;
    private boolean[] isJointStiffnesses = new boolean[NUM_JOINT];
    private String mToast = "";
    private String mJointInfo = "";

    // radian
    private float[] mJointAngles = new float[NUM_JOINT];

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mResources = getResources();
        // View
        mEditTextIp = (EditText) findViewById( R.id.EditText_ip );
        mEditTextIp.setText( mPreferences.getString(IP_KEY, IP_DEFAULT) );
        mTextViewInfo = (TextView) findViewById( R.id.TextView_info );
        initViewJoints();
        // Embedded Tools
        EmbeddedTools tools = new EmbeddedTools();
        File dir = getApplicationContext().getCacheDir();
        log_d( "Extracting libraries in " + dir.getAbsolutePath() );
        tools.overrideTempDirectory(dir);
        tools.loadEmbeddedLibraries();
    }

    /**
     * initViewJoints
     */
    private void initViewJoints() {
        String pkg = getPackageName();
        for ( int i = 0; i < NUM_VIEW; i++ ) {
            initViewJoint( i, pkg );
        }
    }

    /**
     * initViewJoint
     * @param int n
     * @param String pkg  
     */
    private void initViewJoint( int n, String pkg ) {
        float joint_min = JOINT_VALEES[n][0];
        float joint_max = JOINT_VALEES[n][1];
        float joint_default = JOINT_VALEES[n][2];
        int max_deg = (int)( RAD_TO_DEG * ( joint_max - joint_min ));
        int progress_deg = (int)( RAD_TO_DEG * ( joint_default - joint_min ));
        float angle_rad = joint_default;
        log_d( "initViewJoint " + n + " " +max_deg + " " + progress_deg + " " + angle_rad );
        mJointAngles[n] = angle_rad;
        isJointStiffnesses[n] = false;
        // TextView
        String name_text = "TextView_joint_" + n ;
        int id_text = mResources.getIdentifier( name_text, "id", pkg );
        mTextViewJoints[n] = (TextView) findViewById( id_text );
        setTextViewJoint( n, angle_rad );
        // SeekBar
        String name_seek = "SeekBar_joint_" + n ;
        int id_seek = mResources.getIdentifier( name_seek, "id", pkg );
        mSeekBarJoints[n] = (SeekBar) findViewById( id_seek );
        mSeekBarJoints[n].setMax( max_deg );
        mSeekBarJoints[n].setProgress( progress_deg );
        mSeekBarJoints[n].setTag( n );
        mSeekBarJoints[n].setOnSeekBarChangeListener( 
            new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch( SeekBar seek ) {
                // noting to do
            }
            @Override
            public void onProgressChanged( SeekBar seek, int progress, boolean touch ) {
                procProgressChanged( seek, progress );
            }
            @Override
            public void onStopTrackingTouch( SeekBar seek ) {
                procStopTrackingTouch( seek );
            }
        });
    }

    /**
     * procProgressChanged
     * @param int progress
     */	
    private void procProgressChanged( SeekBar seek, int progress ) {
        int tag = (int)seek.getTag();
        float min_seek = JOINT_VALEES[tag][0];
        // margin 1 degree
        float min = min_seek + DEG_TO_RAD;
        float max = JOINT_VALEES[tag][1] - DEG_TO_RAD;
        float angle_rad = DEG_TO_RAD * (float)progress + min_seek;
        if ( angle_rad < min ) {
            angle_rad = min;
        }
        if ( angle_rad > max ) {
            angle_rad = max;
        }
        mJointAngles[tag] = angle_rad;
        setTextViewJoint( tag, angle_rad );
    }

    /**
     * procStopTrackingTouch
     */	
    private void procStopTrackingTouch( SeekBar seek ) {
        int tag = (int)seek.getTag();
        mJointNum = tag;
        procThread( CMD_ANGLE );
    }

    /**
     * setTextViewJoint
     * @param int n
     * @param float angle_rad
     */
    private void setTextViewJoint( int n, float angle_rad ) {
        int angle_deg = (int)( RAD_TO_DEG * angle_rad );
        log_d( "setTextViewJoint " + n + " " + angle_rad + " " + angle_deg );
        mTextViewJoints[n].setText( Integer.toString(angle_deg) );
    }

    /**
     * --- onClick Connect ---
     */
    public void onClickConnect( View view ) {
        log_d("onClickConnect");
        String ip = mEditTextIp.getText().toString().trim();
        if ( "".equals(ip) ) {
            toast_short( R.string.toast_enter_ip );
            return;
        } else if ( !ip.matches(IP_PATTERN) ) {
            toast_short( R.string.toast_enter_correct );
            return;
        }
        mAddr = ip;
        startThread(CMD_CONNECT);
    }

    /**
     * showInfo
     */
    private void showInfo() {
        mTextViewInfo.setText( mJointInfo );
        int angle_deg = 0;
        for ( int i = 0; i < NUM_VIEW; i++ ) { 
            angle_deg = (int)( RAD_TO_DEG * mJointAngles[i] );
            mTextViewJoints[i].setText( Integer.toString(angle_deg) );
        }
    }

    /**
     * procThread
     */
    private void procThread( int cmd ) {
        log_d("procThread " + cmd );
        if ( mQiSession == null ) {
            toast_short( R.string.toast_not_connected );
            return;
        }
        startThread(cmd);
    }

    /**
     * startThread
     */
    private void startThread( int _cmd ) {
        log_d("startThread " + _cmd );
        final int cmd = _cmd;
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                procRobot( cmd );
            }
        });
        thread.start();
    }

// --- robot ---
    /**
     * procRobot
     * @param int cmd
     */
    private void procRobot( int cmd ) {
        switch( cmd ) {
             case CMD_CONNECT:
                robotConnect();
                break;      
            case CMD_ANGLE:
                robotSetAngle();
                break; 
        }
    }

    /**
     * robotConnect
     */
    private void robotConnect() {
        boolean ret = robotConnect( mAddr );
        if ( ret ) {
            log_d( "robotConnect ok" );
            mPreferences.edit().putString( IP_KEY, mAddr ).apply();
            toastOnUiThread( R.string.toast_connected );
            List<String> list_body = robotGetBodyNames( BODY_NAME_JOINT );
            mJointInfo = debugBodyNames( BODY_NAME_JOINT, list_body ) + LF;
            mJointInfo += robotGetLimitsAngles();
            procOnUiThread( RES_JOINT_INFO );
        } else {
            toastOnUiThread( R.string.toast_connect_failed );
        }
    }

    /**
     * robotConnect
     * @param String ip
     * @return boolean
     */
    private boolean robotConnect( String addr ) {
        String ip = "tcp://" + addr + ":" + IP_PORT;
        log_d( "robotConnect " + ip );
        mQiSession = new Session();
        try {
            mQiSession.connect(ip).get();
        } catch (Exception e) {
            if (D) e.printStackTrace();
            mQiSession = null;
            return false;
        }
        try {
            mALMotion = new ALMotion( mQiSession );
        } catch (Exception e) {
            if (D) e.printStackTrace();
        }
        return true;
    }

    /**
     * robotGetAngles
     * @return String
     */
    private String robotGetLimitsAngles() {
        List<List<Float>> list_list_limits = null;
        List<Float> list_angle = null;
        String name = "";
        float angle_rad = 0;
        String msg = "";
        for ( int i = 0; i < NUM_JOINT; i++ ) { 
            name = JOINT_NAMES[i];
            list_list_limits = robotGetLimits( name );
            list_angle = robotGetAngles( name, USE_SENSORS );
            angle_rad = convertAngle( list_angle );
            setJointAngle( i, angle_rad );
            msg += debugLimits( name, list_list_limits ) + LF;
            msg += debugAngle( name, angle_rad ) + LF; 
        }
        return msg;
    }

    /**
     * robotSetAngle
     */
    private void robotSetAngle() {
        String name = JOINT_NAMES[ mJointNum ];
        float angle_rad = mJointAngles[ mJointNum ];
        if ( !isJointStiffnesses[ mJointNum ] ) {
            isJointStiffnesses[ mJointNum ] = true;
            robotSetStiffness( name, STIFFNESSE );
        }
        robotSetAngleInterpolationWithSpeed( name, angle_rad, SPEED );
    }

    /**
     * robotSetStiffness
     * @param String name
     * @param float stiffnesse
    */
    private void robotSetStiffness( String name, float stiffnesse ) {
        log_d( "robotSetStiffness " + name + " " + stiffnesse  );
        try {
            mALMotion.setStiffnesses( name, stiffnesse );
        } catch (Exception e) {
            if (D) e.printStackTrace(); 
           toastOnUiThread( e, R.string.toast_motion_failed );
        }
    }

    /**
     * robotSetAngleInterpolationWithSpeed
     * @param String name
     * @param float angle (radian)
     * @param float speed
     */
    private void robotSetAngleInterpolationWithSpeed( String name, float angle, float speed ) {
        log_d("robotSetAngleInterpolationWithSpeed " + name + " " + angle + " " + speed);
        try {
            mALMotion.angleInterpolationWithSpeed( name, angle, speed );
        } catch (Exception e) {
            if (D) e.printStackTrace(); 
           toastOnUiThread( e, R.string.toast_motion_failed );
        }
    }

    /**
     * robotGetBodyNames
     * @param String name
     * @return List<String>
     */
    private List<String> robotGetBodyNames( String name ) {
        log_d( "robotGetBodyNames " + name );
        List<String> list = new ArrayList<String>();
        try {
            list = mALMotion.getBodyNames( name );
        } catch (Exception e) {
            if (D) e.printStackTrace(); 
           toastOnUiThread( e, R.string.toast_motion_failed );
        }
        log_d( "getBodyNames " + list );
        return list;
    }
    
    /**
     * robotGetLimits
     * @param String name
     * @return List<List<Float>>
     * the minAngle, maxAngle, maxVelocity, maxTorque
     */
    private List<List<Float>> robotGetLimits( String name ) {
        log_d( "robotGetLimits " + name );
        List<List<Float>> list_list = new ArrayList<List<Float>>();
        try {
            list_list = (List<List<Float>>) mALMotion.getLimits( name );
        } catch (Exception e) {
            if (D) e.printStackTrace(); 
           toastOnUiThread( e, R.string.toast_motion_failed );
        }
        log_d( "getLimits " + list_list );
        return list_list;
    }

    /**
     * robotGetAngle
     * @param List<String> nameList
     * @param boolean useSensors
     * @return List<List<Float>> (radians)
     */
    private List<Float> robotGetAngles( List<String> nameList, boolean useSensors ) {
        log_d( "robotGetAngles "  + debugListString( nameList ) + " " + useSensors );
        List<Float> list = new ArrayList<Float>();
        try {
            list = mALMotion.getAngles( nameList, useSensors );
        } catch (Exception e) {
            if (D) e.printStackTrace(); 
           toastOnUiThread( e, R.string.toast_motion_failed );
        }
        log_d( "getAngles " + list );
        return list;
    }

    /**
     * robotGetAngle
     * @param String name
     * @param boolean useSensors
     * @return List<Float> (radians)
     */
    private List<Float> robotGetAngles( String name, boolean useSensors ) {
        log_d( "robotGetAngles " + name );
        List<Float> list = new ArrayList<Float>();
        try {
            list = mALMotion.getAngles( name, useSensors );
        } catch (Exception e) {
            if (D) e.printStackTrace(); 
           toastOnUiThread( e, R.string.toast_motion_failed );
        }
        log_d( "getAngles " + list );
        return list;
    }

    /**
     * convertAngles
     * @param List<List<Float>> list_list
     * @return fList<Float> (radian)
     */
    private List<Float> convertAngles( List<List<Float>> list_list ) {
        if (( list_list == null )||( list_list.size() == 0 )) {
            log_d( "not getAngles" );
            return null;
        }
        List<Float> list_ret = new ArrayList<Float>(); 
        for ( List<Float> list: list_list ) {
            list_ret.add( convertAngle( list ) );
        }
        return list_ret;
    }

    /**
     * convertAngle
     * @param List<Float> list
     * @return float (radian)
     */
    private float convertAngle( List<Float> list ) {
        if (( list == null )||( list.size() == 0 )) {
            log_d( "not get robotGetAngles" );
            return ( ANGLE_ERROR + 1 );
        }
        return list.get(0);
    }

    /**
     * setJointAngle
     * @param int n
     * @param float angle
     */
    private void setJointAngle( int n, float angle_rad ) {
        if ( angle_rad < ANGLE_ERROR ) {
            mJointAngles[n] = angle_rad;
        }
    }

    /**
     * debugBodyNames
     * @param String name
     * @param List<String> list
     * @return String 
     */
    private String debugBodyNames( String name, List<String> list ) {
        String msg = "BodyNames " + name + ": ";
        if (( list == null )||( list.size() == 0 )) {
            log_d( "not getBodyNames" );
            return msg;
        }
        for ( String str: list ) {
            msg += str+ COMMA_SPACE;
        }
        log_d(msg);
        return msg;
    }

    /**
     * debugLimits
     * @param String name
     * @param List<List<Float>> list_list
     * @return String
     */
    private String debugLimits( String name, List<List<Float>> list_list ) {
        String msg = name + " limits: ";
        if (( list_list == null )||( list_list.size() == 0 )) {
            log_d( "not get robotGetLimits" );
            return msg;
        }
        msg += debugListFloat( list_list.get(0) );
        log_d( msg );
        return msg;
    }

    /**
     * debugAngle
     * @param String name
     * @param float angle
     * @return String
     */
    private String debugAngle( String name, float angle_rad ) {
        String msg = name + " angle: ";
       if ( angle_rad < ANGLE_ERROR ) {
            msg += angle_rad;
        }
        log_d( msg );
        return msg;
    }

    /**
     * debugListString
     * @param List<String> list
     * @return String
     */
    private String debugListString( List<String> list ) {
        if (( list == null )||( list.size() == 0 )) {
            return "";
        }
        String msg = "";
        for ( String str: list ) {
            msg += str + COMMA_SPACE;
        }
        return msg;
    }

    /**
     * debugListFloat
     * @param List<Float> list
     * @return String
     */
    private String debugListFloat( List<Float> list ) {
        if (( list == null )||( list.size() == 0 )) {
            return "";
        }
        String msg = "";
        for ( Float f: list ) {
            msg += f + COMMA_SPACE;
        }
        return msg;
    }

    /**
     * toast on UI thread
     */
    private void toastOnUiThread( Exception e, int res_id) {
        String str = getString(res_id);
        toastOnUiThread( str+ LF + e.getMessage() );
    }

    /**
     * toast on UI thread
     */
    private void toastOnUiThread( int res_id ) {
        String str = getString(res_id);
        toastOnUiThread( str );
    }
 
    /**
     * toast on UI thread
     */
    private void toastOnUiThread( String str ) {
        mToast = str;
        procOnUiThread( RES_TOAST );
    }

    /**
     * proc on UI thread
     * @param int _res
     */
    private void procOnUiThread( int _res ) {
        log_d( "procOnUiThread " +  _res );
        final int res = _res;
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                procUi( res );
            }
        });
    }

// --- UI ---
    /**
     * procUi
     * @param int res
     */
    private void procUi( int res ) {
        switch( res ) {
             case RES_TOAST:
                toast_short( mToast );
                break;      
             case RES_JOINT_INFO:
                showInfo();
                break; 
        }
    }

    /**
     * toast short
     */       
    private void toast_short( String str ) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    /**
     * toast short
     */       
    private void toast_short( int res_id ) {
        Toast.makeText(this, res_id, Toast.LENGTH_SHORT).show();
    }

    /**
     * log_d
     */ 
    private void log_d( String str ) {
        if (D) Log.d( TAG, str );
    }

}
