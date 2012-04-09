package visualtasks.com;

import java.io.Serializable;

public class Task implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int STATUS_ACTIVE = 0;
	public static final int STATUS_COMPLETED = STATUS_ACTIVE + 1;
	public static final int STATUS_DELETED = STATUS_COMPLETED + 1;
	
	private int mID;
	private String mDescription;
	
	private float mX, mY;

	private int mStatus;
	
	public Task(int pID, String pDescription, float pX, float pY) {
		super();
		this.mDescription = pDescription;
		this.mX = pX; 
		this.mY = pY;
		this.mID = pID;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String pDescription) {
		this.mDescription = pDescription;
	}

	public int getStatus() {
		return mStatus;
	}
	
	public void setStatus(int pStatus) {
		this.mStatus = pStatus;
	}
	

	public float getX(){
		return mX;
		
	}
	
	public void setX(float pX){
		mX = pX;
	}
	public float getY(){
		return mY;
		
	}
	
	public void setY(float pY){
		mY = pY;
	}
	
	public int getID() {
		return mID;
	}
	
	public interface ITaskChangeListener {

		void onTaskAdded(Task pTask);
		void onTaskDeleted(Task pTask );
		void onTaskPositionUpdated(Task pTask, float pOldX, float pOldY, float pNewX, float pNewY);
		void onTaskDescriptionUpdated(Task pTask, String pOldDescription, String pNewDescription);
	}
	
	

}
