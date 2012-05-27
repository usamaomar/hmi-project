package data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDbHandler extends SQLiteOpenHelper {

	 // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2;
 
    // Database Name
    private static final String DATABASE_NAME = "visualtasks";
 
    // Tasks table name
    private static final String TABLE_TASKS = "tasks";
 
    // Tasks Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_URGENCY = "urgency";
    public static final String KEY_POSX = "posx";
    public static final String KEY_POSY = "posy";
    public static final String KEY_STATUS = "status";
    
 
    public TaskDbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_DESCRIPTION + " TEXT,"
                + KEY_URGENCY + " FLOAT," 
                + KEY_POSX + " LONG," + KEY_POSY + " LONG,"
                + KEY_STATUS + " INT" + ")";
        db.execSQL(CREATE_TASKS_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
 
        // Create tables again
        onCreate(db);
    }
    
    public long addTask(String description, float x, float y, int status, float urgency) {
        SQLiteDatabase db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
        values.put(KEY_DESCRIPTION, description); // 
        values.put(KEY_POSX, x); // 
        values.put(KEY_POSY, y);
        values.put(KEY_STATUS, status);
        values.put(KEY_URGENCY, urgency);
        // Inserting Row
        Long id = db.insert(TABLE_TASKS, null, values);
       
        db.close(); // Closing database connection
        return id;
    }
    
    // Getting single task
	public Cursor getTask(Long id) {
	    SQLiteDatabase db = this.getReadableDatabase();
	 
	    Cursor cursor = db.query(TABLE_TASKS, new String[] { KEY_ID,
	    		KEY_DESCRIPTION, KEY_POSX, KEY_POSY, KEY_STATUS, KEY_URGENCY }, KEY_ID + "=?",
	            new String[] { String.valueOf(id) }, null, null, null, null);
	    if (cursor != null)
	        cursor.moveToFirst();
	 
	  
	    return cursor;
	}
	
    // Getting All Tasks
	 public Cursor getAllTasks() {
	 
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_TASKS;
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    return cursor;
	}
	 
	// Updating single contact
	 public int updateTask(Long id, String description, float x, float y, int status, float urgency) {
	     SQLiteDatabase db = this.getWritableDatabase();
	  
	    ContentValues values = new ContentValues();
	    values.put(KEY_DESCRIPTION, description); // 
        values.put(KEY_POSX, x); // 
        values.put(KEY_POSY, y);
        values.put(KEY_STATUS, status);
        values.put(KEY_URGENCY, urgency);
	  
	     // updating row
	     return db.update(TABLE_TASKS, values, KEY_ID + " = ?",
	             new String[] { String.valueOf(id) });
	 }
	 
//	 // Updating all tasks
//	 public void updateAllTasks(List<Task> tasks){
//		 for(Task task : tasks){
//			 this.updateTask(task);
//		 }
//	 }
	 
	 
	// Deleting single contact
	 public void deleteTask(Long id) {
	     SQLiteDatabase db = this.getWritableDatabase();
	     db.delete(TABLE_TASKS, KEY_ID + " = ?",
	             new String[] { String.valueOf(id) });
	     db.close();
	 }
	
//	private Task cursorToTask(Cursor cursor){
//		Task task = new Task(
//	    		cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
//	    		cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)),
//	    		cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_POSX)),
//	    		cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_POSY)),
//	    		cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_URGENCY)),
//	    		cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STATUS))
//	    		);
//	            
//	    return task;
//	}

}
