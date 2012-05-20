package data;

import java.util.Comparator;

import android.database.Observable;

public class Task implements Comparable<Task> {

	/**
	 * 
	 */

	public static final int STATUS_ACTIVE = 0;
	public static final int STATUS_COMPLETED = STATUS_ACTIVE + 1;
	public static final int STATUS_DELETED = STATUS_COMPLETED + 1;
	private long mID;
	private String mDescription;
	private float mUrgency;
	private float mX, mY;
	private int mStatus;

	
	public Task() {
		// TODO Auto-generated constructor stub
	}
	
	public Task(int pID, String pDescription, float pX, float pY, float pUrgency, int pStatus) {
		this.mDescription = pDescription;
		this.mX = pX; 
		this.mY = pY;
		this.mID = pID;
		this.mUrgency = pUrgency;
		this.mStatus = pStatus;
	}
	
	public Task(String pDescription, float pX, float pY, float pUrgency, int pStatus) {
		this.mDescription = pDescription;
		this.mX = pX; 
		this.mY = pY;
		this.mUrgency = pUrgency;
		this.mStatus = pStatus;
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
	
	
	public void setY(float pY){
		mY = pY;
	}
	
	public long getID() {
		return mID;
	}
	
	public void setID(Long id) {
		this.mID = id;
	}

	

	@Override
	public int compareTo(Task another) {
		// TODO Auto-generated method stub
		return Long.valueOf(this.getID()).compareTo(another.getID());
	}
}
