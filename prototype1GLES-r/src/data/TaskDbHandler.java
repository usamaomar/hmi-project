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
    private static final int DATABASE_VERSION = 1;
 
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
    
    public void addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
        values.put(KEY_DESCRIPTION, task.getDescription()); // 
        values.put(KEY_POSX, task.getX()); // 
        values.put(KEY_POSY, task.getY());
        values.put(KEY_STATUS, task.getStatus());
        values.put(KEY_URGENCY, task.getUrgency());
        // Inserting Row
        Long id = db.insert(TABLE_TASKS, null, values);
        task.setID(id);
        db.close(); // Closing database connection
    }
    
    // Getting single task
	public Task getTask(Long id) {
	    SQLiteDatabase db = this.getReadableDatabase();
	 
	    Cursor cursor = db.query(TABLE_TASKS, new String[] { KEY_ID,
	    		KEY_DESCRIPTION, KEY_POSX, KEY_POSY, KEY_STATUS, KEY_URGENCY }, KEY_ID + "=?",
	            new String[] { String.valueOf(id) }, null, null, null, null);
	    if (cursor != null)
	        cursor.moveToFirst();
	 
	  
	    return cursorToTask(cursor);
	}
	
    // Getting All Tasks
	 public List<Task> getAllTasks() {
	    List<Task> taskList = new ArrayList<Task>();
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_TASKS;
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            Task task = cursorToTask(cursor);
	            // Adding task to list
	            taskList.add(task);
	        } while (cursor.moveToNext());
	    }
	    cursor.close();
	    // return task list
	    return taskList;
	}
	 
	// Updating single contact
	 public int updateTask(Task task) {
	     SQLiteDatabase db = this.getWritableDatabase();
	  
	    ContentValues values = new ContentValues();
	    values.put(KEY_DESCRIPTION, task.getDescription()); // 
        values.put(KEY_POSX, task.getX()); // 
        values.put(KEY_POSY, task.getY());
        values.put(KEY_STATUS, task.getStatus());
        values.put(KEY_URGENCY, task.getUrgency());
	  
	     // updating row
	     return db.update(TABLE_TASKS, values, KEY_ID + " = ?",
	             new String[] { String.valueOf(task.getID()) });
	 }
	 
	 // Updating all tasks
	 public void updateAllTasks(List<Task> tasks){
		 for(Task task : tasks){
			 this.updateTask(task);
		 }
	 }
	 
	 
	// Deleting single contact
	 public void deleteTask(Task task) {
	     SQLiteDatabase db = this.getWritableDatabase();
	     db.delete(TABLE_TASKS, KEY_ID + " = ?",
	             new String[] { String.valueOf(task.getID()) });
	     db.close();
	 }
	
	private Task cursorToTask(Cursor cursor){
		Task task = new Task(
	    		cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
	    		cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)),
	    		cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_POSX)),
	    		cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_POSY)),
	    		cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_URGENCY)),
	    		cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STATUS))
	    		);
	            
	    return task;
	}

}
