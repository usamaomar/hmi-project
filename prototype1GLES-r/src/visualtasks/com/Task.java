package visualtasks.com;

import java.io.Serializable;
import java.util.Comparator;

public class Task implements  Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int STATUS_ACTIVE = 0;
	public static final int STATUS_COMPLETED = STATUS_ACTIVE + 1;
	public static final int STATUS_DELETED = STATUS_COMPLETED + 1;
	private int mID;
	private String mDescription;
	
	private float mUrgency;
	private float mX, mY;

	private int mStatus;
	private boolean mSelected;
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
	
	
	public float getUrgency() {
		return mUrgency;
	}
	
	public void setUrgency(float pUrgency) {
		this.mUrgency = pUrgency;
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
	
	public void setSelected(boolean pSelected) {
		this.mSelected = pSelected;
	}
	
	public boolean isSelected() {
		return mSelected;
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

	

	
	
	static public class DefaultComparator extends UrgencyComparator {

		@Override
		public int compare(Task lhs, Task rhs) {
			// TODO Auto-generated method stub
			if (lhs.isSelected() && !rhs.isSelected()){
				return -1;
			} else if (!lhs.isSelected() && rhs.isSelected()){
				return 1;
			} else return super.compare(lhs, rhs);
			
		}
		
	}
	
	static public class UrgencyComparator implements Comparator<Task> {

		@Override
		public int compare(Task lhs, Task rhs) {
			// TODO Auto-generated method stub
			return (int) Math.signum(lhs.getUrgency() - rhs.getUrgency());
		}
		
	}
	

}
