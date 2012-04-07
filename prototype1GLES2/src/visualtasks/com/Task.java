package visualtasks.com;

import java.io.Serializable;

public class Task implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String mDescription;
	
	private float[] mCoord ;

	
	
	public Task(String pDescription, float[] pCoord) {
		super();
		this.mDescription = pDescription;
		this.mCoord = pCoord;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String pDescription) {
		this.mDescription = pDescription;
	}

	public float[] getCoord() {
		return mCoord;
	}

	public void setCoord(float[] pCoord) {
		this.mCoord = pCoord;
	}

	public float getXCoord(){
		return mCoord[0];
		
	}
	public float getYCoord(){
		return mCoord[1];
	}
	
	
	
	

}
