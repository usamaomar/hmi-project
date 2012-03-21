package visualgtd.nl;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;

public class VisualgtdActivity extends Activity {
	/**
	 * Test commit!
	 */
	
    private TasksDbAdapter mDbHelper;
	private Cursor mTaskCursor;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDbHelper = new TasksDbAdapter(this);
        mDbHelper.open();
        fillData();
        
    }

	private void fillData() {
		mTaskCursor = mDbHelper.fetchAllTasks();
		startManagingCursor(mTaskCursor);
	}
}