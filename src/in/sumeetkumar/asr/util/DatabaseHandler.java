package in.sumeetkumar.asr.util;

import java.util.ArrayList;
import java.util.List;

import edu.mit.media.funf.util.LogUtil;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
 
public class DatabaseHandler extends SQLiteOpenHelper {
 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 5;
 
    // Database Name
    private static final String DATABASE_NAME = "SoundFeatures";
 
    // Features table name
    private static final String TABLE_FEATURES = "Features";

    // Features Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_LABEL = "label";
    private static final String KEY_L1NORM = "l1Norm";
    private static final String KEY_L2NORM = "l2Norm";
    private static final String KEY_LINFNORM = "linfNorm";
    private static final String KEY_MFCC = "mfccs";
    private static final String KEY_PSDACROSSFREQUENCYBANDS = "psdAcrossFrequencyBands";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_DIFFSECS = "diffSecs";
    
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_FEATURES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        		+ KEY_LABEL + " TEXT,"
                + KEY_L1NORM + " TEXT,"
                + KEY_L2NORM + " TEXT,"
                + KEY_LINFNORM + " TEXT,"
                + KEY_TIMESTAMP + " TEXT,"
                + KEY_DIFFSECS + " TEXT,"
                + KEY_MFCC + " TEXT,"
                + KEY_PSDACROSSFREQUENCYBANDS + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
        
        Log.d(LogUtil.TAG, "Features Table Created");
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEATURES);
 
        // Create tables again
        onCreate(db);
    }
 
    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
 
    // Adding new feature
    public void addFeature(Feature feature) {
    	
    	try {
    		 SQLiteDatabase db = this.getWritableDatabase();
    	        
    	        ContentValues values = new ContentValues();
    	        values.put(KEY_LABEL, feature.getName()); // Feature Name
    	        values.put(KEY_MFCC, feature.getMfccsAsString()); // Feature mfcc
    	        values.put(KEY_L1NORM, feature.getL1Norm());
    	        values.put(KEY_L2NORM, feature.getL2Norm());
    	        values.put(KEY_LINFNORM, feature.getLinfNorm());
    	        values.put(KEY_TIMESTAMP, feature.getTimestamp());
    	        values.put(KEY_DIFFSECS, feature.getDiffSecs());
    	        values.put(KEY_PSDACROSSFREQUENCYBANDS, feature.getPsdAcrossFrequencyBandsAsString());
    	        
    	        // Inserting Row
    	        db.insert(TABLE_FEATURES, null, values);
    	        Log.d(LogUtil.TAG, " New feature added");	
		} catch (Exception e) {
			Log.d(LogUtil.TAG, " Exception while adding new feature.");	
		}
        //db.close(); // Closing database connection, closing outside, need to fix
    }
 
    // Getting single feature
//    Feature getFeature(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
// 
//        Cursor cursor = db.query(TABLE_FEATURES, new String[] { KEY_ID,
//                KEY_LABEL, KEY_MFCC }, KEY_ID + "=?",
//                new String[] { String.valueOf(id) }, null, null, null, null);
//        if (cursor != null)
//            cursor.moveToFirst();
// 
//        Feature feature = new Feature(Integer.parseInt(cursor.getString(0)),
//                cursor.getString(1), cursor.getString(2));
//        
//        db.close(); // Closing database connection
//        // return feature
//        return feature;
//    }
     
    // Getting All Features
    public List<Feature> getAllFeatures() {
        List<Feature> featureList = new ArrayList<Feature>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_FEATURES;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Feature feature = new Feature();
                feature.setID(Integer.parseInt(cursor.getString(0)));
                feature.setName(cursor.getString(1));
                feature.setL1Norm(Double.parseDouble(cursor.getString(2)));
                feature.setL2Norm(Double.parseDouble(cursor.getString(3)));
                feature.setLinfNorm(Double.parseDouble(cursor.getString(4)));
                feature.setTimestamp(Double.parseDouble(cursor.getString(5)));
                feature.setDiffSecs(Double.parseDouble(cursor.getString(6)));
                feature.setMfccsAsString(cursor.getString(7));
                feature.setPsdAcrossFrequencyBandsAsString(cursor.getString(8));
                // Adding feature to list
                featureList.add(feature);
            } while (cursor.moveToNext());
        }
 
        db.close(); // Closing database connection
        // return feature list
        return featureList;
    }
 
//    // Updating single feature
//    public int updateFeature(Feature feature) {
//        SQLiteDatabase db = this.getWritableDatabase();
// 
//        ContentValues values = new ContentValues();
//        values.put(KEY_NAME, feature.getName());
//        values.put(KEY_M, feature.getPhoneNumber());
// 
//        // updating row
//        return db.update(TABLE_FEATURES, values, KEY_ID + " = ?",
//                new String[] { String.valueOf(feature.getID()) });
//    }
 
    // Deleting single feature
    public void deleteFeature(Feature feature) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FEATURES, KEY_ID + " = ?",
                new String[] { String.valueOf(feature.getID()) });
        db.close();
    }
 
 
    // Getting features Count
    public int getFeaturesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_FEATURES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close(); // Closing database connection
        // return count
        return count;
    }
 
}
