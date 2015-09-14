/**
 * Pepper controlled by Android
 * 2015-08-01 K.OHWADA
 */

package andorid.ohwada.jp.peppersaylist;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.*;

/**
 * helper for DB
 */
public class MsgHelper extends SQLiteOpenHelper {

    private static final int MAX_RECORD = Constant.MAX_RECORD;

    // database
    private static final String DB_TITLE = "msg.db";
    private static final int DB_VERSION = 1;

    // table
    private static final String TBL_TITLE = "card";

    // column	
    private static final String COL_ID = "_id";
    private static final String COL_TIME = "time";
    private static final String COL_TITLE = "title";
    private static final String COL_MSG = "msg";

    private static final String[] COLUMNS =
        new String[]
        { COL_ID, COL_TIME, COL_TITLE, COL_MSG } ;

    // query 
    private static final String ORDER_BY_ID =  "_id desc" ;
    private static final String ORDER_BY_TIME_ID =  "time desc, _id desc" ;

    private static final String CREATE_SQL =
    	"CREATE TABLE IF NOT EXISTS " 
                + TBL_TITLE 
                + " ( " 
                + COL_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
                + COL_TIME
                + " INTEGER, "
                + COL_TITLE
                + " TEXT, " 
                + COL_MSG
                + " TEXT )" ;

    private static final String DROP_SQL =
        "DROP TABLE IF EXISTS " + TBL_TITLE ;

    private Resources mResources;
    
    /**
     * === constractor ===
     * @param Context context   
     */		
    public MsgHelper( Context context ) {
        super( context, DB_TITLE, null, DB_VERSION );
        mResources = context.getResources();
    }

    /**
     * === onCreate ===
     */
    @Override
    public void onCreate( SQLiteDatabase db ) {
        createDb( db );
        insertDb( db );
    }

    /**
     * create DB
     * @param SQLiteDatabase db 
     */
    private void createDb( SQLiteDatabase db ) {
        db.execSQL( CREATE_SQL );
    }

    /**
     * insertDb
     * @param SQLiteDatabase db 
     */
    private void insertDb( SQLiteDatabase db ) {
        String[] list = mResources.getStringArray( R.array.messages );  
        ArrayList<String> arrayList = new ArrayList<String>(asList(list));
        MsgRecord record;
        for ( String str: arrayList ) {
            record = new MsgRecord( "",  str );
            insert( db, record );
        }
    }

    /**
     * === onUpgrade ===
     */
    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )  {
        db.execSQL( DROP_SQL );
        createDb( db );
    }

    /**
     * --- insert  ---
     * @param MsgRecord record
     * @return long : the row ID of the newly inserted row
     */
    public long insert( MsgRecord record ) {
        SQLiteDatabase db = getWritableDatabase();
        if ( db == null ) return 0;
        long ret = insert( db, record );
        db.close();
        return ret;
    }

    /**
     * insert
     * @param SQLiteDatabase db
     * @param MsgRecord record
     * @return long : the row ID of the newly inserted row
     */
    private long insert( SQLiteDatabase db, MsgRecord record ) {
        return db.insert( 
            TBL_TITLE, 
            null, 
            buildValues( record ) );
    }

    /**
     * --- update  ---
     * @param MsgRecord record
     * @return int : the number of rows affected
     */    	
    public int update( MsgRecord record  ) {
        SQLiteDatabase db = getWritableDatabase();
        if ( db == null ) return 0;
        int ret = db.update(
            TBL_TITLE, 
            buildValues( record ),
            buildWhereId( record ), 
            null );
        db.close();
        return ret;
    }

    /**
     * --- delete  ---
     * @param MsgRecord record
     * @return int : the number of rows affected
     */ 			
    public int delete( MsgRecord record ) {
        return delete( buildWhereId( record ) );
    }

    /**
     * --- delete  ---
     * @param int id
     * @return int : the number of rows affected
     */ 	
    public int delete( int id  ) {
        return delete( buildWhereId( id ) );
    }	

    /**
     * delete all
     * @return int : the number of rows affected
     */ 	
    public int deleteAll()  {
        String where = null;
        return delete( where ); 
    }
	
    /**
     * delete  for common
     * @param int id
     * @return int : the number of rows affected
     */ 	
    public int delete( String where )  {
        SQLiteDatabase db = getWritableDatabase();
        if ( db == null ) return 0;
        int ret = db.delete(
            TBL_TITLE, 
            where, 
            null );		
        db.close();
        return ret;	
    }
	
    /**
     * build ContentValues
     * @param MsgRecord r    
     * @return ContentValues
     */        
    private ContentValues buildValues( MsgRecord r ) {
        ContentValues v = new ContentValues();	
        v.put( COL_TIME, r.time );
        v.put( COL_TITLE, r.title );
        v.put( COL_MSG, r.msg );
        return v;
    }

    /**
     * build where
     * @param MsgRecord r
     * @return String : where
     */ 
    private String buildWhereId( MsgRecord r ) {
        return buildWhereId( r.id );
    }

    /**
     * build where
     * @param int id
     * @return String : where
     */ 
    private String buildWhereId( int id ) {
        String s = COL_ID + "=" + id ;
        return s;
    }
			
    /**
     * build limit
     * @param int limit
     * @param int offset
     * @return String : limit
     */ 
    private String buildLimit( int limit, int offset ) {
        String limit_str = Integer.toString( limit );
        String offset_str = Integer.toString( offset );
        String str = null;
        if (( limit > 0 )&&( offset > 0 )) {
            str = limit_str+ ", " + offset_str;
        } else if (( limit > 0 )&&( offset== 0 )) {
            str = limit_str;
        } else if (( limit == 0 )&&( offset > 0 )) {
            str = "0, " + offset_str;
        }
        return str;
    }	
		
    /**
     * --- get record   ---
     * @param int id
     * @return MsgRecord
     */ 			
    public MsgRecord getRecordById( int id ) {
        return getRecordCommon( buildWhereId( id ) );
    }

    /**
     * get record
     * @param String where
     * @return MsgRecord
     */ 			
    private MsgRecord getRecordCommon( String where ) {
        SQLiteDatabase db = getReadableDatabase();
        if ( db == null ) return null;
        Cursor c = getCursorCommon( db, where, ORDER_BY_ID, null );
        if (( c == null )||( c.getCount() == 0 )) {
            db.close();
            return null;
        }
        c.moveToFirst();   
        MsgRecord r = buildRecord( c );
        db.close();
        return r;
    }
		
    /**
     * --- get record list   ---
     * @param int limit
     * @return List<MsgRecord>
     */ 
    public List<MsgRecord> getListOrderId( int limit ) {
        return getList( ORDER_BY_ID, limit, 0 );
    }

    /**
     * --- get record list   ---
     * @param int limit
     * @return List<MsgRecord>
     */ 
    public List<MsgRecord> getListOrderTime( int limit ) {
        return getList( ORDER_BY_TIME_ID, limit, 0 );
    }

    /**
     * --- get record list   ---
     * @param int limit
     * @param int offset
     * @return List<MsgRecord>
     */ 
    public List<MsgRecord> getListOrderTime( int limit, int offset ) {
        return getList( ORDER_BY_TIME_ID, limit, offset );
    }

    /**
     * --- get record list   ---
     * @param String orderby
     * @param int limit 
     * @param int offset
     * @return List<MsgRecord>
     */ 
    public List<MsgRecord> getList( String orderby, int limit, int offset ) {
        SQLiteDatabase db = getReadableDatabase();
        if ( db == null ) return null;
        Cursor c = getCursorCommon( 
            db, null,  orderby, buildLimit( limit, offset ) );
        if (( c == null )||( c.getCount() == 0 )) {
            db.close();
            return null;
        }
        List<MsgRecord> list = buildRecordList( c );
        db.close();
        return list;
    }

    /**
     * get cursor for common
     * @param SQLiteDatabase db
     * @param String where
     * @param String limit  
     * @return Cursor
     */ 
    private Cursor getCursorCommon( SQLiteDatabase db, String where, String orderby, String limit ) {		
        String[] param = null;
        String groupby = null;
        String having = null;
        return db.query( 
            TBL_TITLE,
            COLUMNS,
            where , 
            param, 
            groupby , 
            having, 
            orderby , 
            limit );
        }
	
    /**
     * buile record
     * @param Cursor c 
     * @return MsgRecord
     */ 	
    private MsgRecord buildRecord( Cursor c ) {
        MsgRecord r = new MsgRecord(
            c.getInt(0),
            c.getLong(1),
            c.getString(2),
            c.getString(3) );
        return r;
    }

    /**
     * buile record list
     * @param Cursor c 
     * @return List<MsgRecord>
     */ 			
    private List<MsgRecord> buildRecordList( Cursor c ) {
        List<MsgRecord> list = new ArrayList<MsgRecord>();		        
        int count = c.getCount();
        if ( count == 0 ) return list;		
        c.moveToFirst();   
        for ( int i = 0; i < count; i++ ) {
            list.add( buildRecord( c ) );
            c.moveToNext();
        } 		
        c.close();		
        return list;
    }

    /**
     * --- getCount ---
     * @return long
     */ 
    public long getCount() {
        long count = 0;
        SQLiteDatabase db = getReadableDatabase();
        if ( db == null ) return count;
        String sql = "select count(*) from " + TBL_TITLE;
        Cursor c = db.rawQuery( sql, null );
        if ( c != null ) {
        	c.moveToLast();
        	count = c.getLong( 0 );
        }	
        c.close();
        return count;
    }

    /**
     * --- deleteIfOver ---
     * @return long
     */ 
    public long deleteIfOver() {
        long count = getCount();
        if ( count > MAX_RECORD ) {
            List<MsgRecord> list = getListOrderTime( MAX_RECORD, MAX_RECORD ); 
            for ( MsgRecord r: list ) {
                delete( r );    
            }
            return list.size();           
        }
        return 0; 
    }
}
