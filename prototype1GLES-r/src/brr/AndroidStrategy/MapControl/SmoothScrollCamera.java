
package brr.AndroidStrategy.MapControl;



import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;

import android.view.MotionEvent;

public class SmoothScrollCamera extends SmoothCamera implements IOnSceneTouchListener
{

	
	private int scrollState = 0;
	private final static int STATE_START = 0;
	private final static int STATE_TOUCHDOWN = 1;
	private final static int STATE_SCROLLING = 2;
	private final static int STATE_SCROLLING_ENDED = 3;
	
	private float speedX;
	private float speedY;
	private float lastX;
	private float lastY;
	private float startY;
	private float secondsAfterScrollEnded;
	private float secondsPauseBeforeReturn;
	private boolean autoScrollBackEnabled = true;
	
	public SmoothScrollCamera(float startY, float width, float height, float maxVelocityY, float secondsPauseBeforeReturn)
	{
		super(0, startY, width, height, 0, maxVelocityY, 0);
		this.startY = startY;
		this.secondsPauseBeforeReturn = secondsPauseBeforeReturn;
		
	}
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent touchEvent)
	{
		
		MotionEvent evt = touchEvent.getMotionEvent();
		
		if(evt.getAction() == MotionEvent.ACTION_DOWN)
		{
			this.scrollState = SmoothScrollCamera.STATE_TOUCHDOWN;
			this.lastX = evt.getRawX();
			this.lastY = evt.getRawY();	
			this.speedX = 0;
			this.speedY = 0;
		}
		if(this.scrollState == SmoothScrollCamera.STATE_TOUCHDOWN)
		{
			
			this.setCenterDirect(this.getCenterX() + (lastX - evt.getRawX()) / this.getZoomFactor(), this.getCenterY() + (lastY - evt.getRawY()) / this.getZoomFactor());
			this.lastX = evt.getRawX();
			this.lastY = evt.getRawY();	
		}
		if(evt.getAction() == MotionEvent.ACTION_UP)
		{
			this.scrollState = SmoothScrollCamera.STATE_SCROLLING;
		}
		
		
		return false;
	}


	@Override
	public void onUpdate(float pSecondsElapsed)
	{
		super.onUpdate(pSecondsElapsed);
		
		if(this.scrollState == SmoothScrollCamera.STATE_SCROLLING && (
				this.speedX == 0 && this.speedY == 0 ||
				this.getYMax() == this.getBoundsYMax()
				|| this.getYMin() == this.getBoundsYMin())
				){
			this.secondsAfterScrollEnded = 0;
			this.scrollState = this.STATE_SCROLLING_ENDED;
			
		}
		
		switch(this.scrollState){
		case SmoothScrollCamera.STATE_SCROLLING:
			
			//Log.v("AndEngine", "SpeedX: " + String.valueOf(this.speedX) + " SpeedY: " + String.valueOf(this.speedY));
			this.setCenterDirect(this.getCenterX() - speedX * pSecondsElapsed / this.getZoomFactor(), this.getCenterY() - speedY * pSecondsElapsed / this.getZoomFactor());
			
			this.speedX *= (1.0f - 1.2f * pSecondsElapsed);
			this.speedY *= (1.0f - 1.2f * pSecondsElapsed);
			
			if(speedX < 10 && speedX > -10) speedX = 0;
			if(speedY < 10 && speedY > -10) speedY = 0;
			break;
		case SmoothScrollCamera.STATE_SCROLLING_ENDED:
			this.speedX = 0;
			this.speedY = 0;
			
			if(autoScrollBackEnabled){
				if(secondsAfterScrollEnded >= secondsPauseBeforeReturn){
				
					this.setCenter(this.getCenterX(), startY + this.getHeight()/2f);
					this.scrollState = SmoothScrollCamera.STATE_START;
				}else{
					this.secondsAfterScrollEnded += pSecondsElapsed;
				}
			}else{
				this.secondsAfterScrollEnded = 0;
			}
			
			break;
			
		}
	
	}

	@Override
	public void reset()
	{
		
	}

	public float getSpeedX()
	{
		return speedX;
	}

 	public void setSpeedX(float speedX)
	{
 		this.speedX = speedX;
	}

	public float getSpeedY()
	{
		return speedY;
	}

	public void setSpeedY(float speedY)
	{
		this.speedY = speedY;
	}
	
	public void cancelCurScrolling()
	{
		this.scrollState = STATE_SCROLLING_ENDED;
	}
	
	public void setStartY(float startY)
	{
		this.startY = startY;
	}
	
	public float getStartY() {
		return startY;
	}
	
	public void setAutoScrollBackEnabled(boolean autoScrollBackEnabled) {
		this.autoScrollBackEnabled = autoScrollBackEnabled;
	}
	public boolean isAutoScrollBackEnabled() {
		return autoScrollBackEnabled;
	}
	
}
