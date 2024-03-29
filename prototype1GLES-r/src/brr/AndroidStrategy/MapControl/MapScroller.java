
package brr.AndroidStrategy.MapControl;



import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;

import android.view.MotionEvent;

public class MapScroller implements IOnSceneTouchListener, IUpdateHandler
{

	private boolean enabled = true;
	private SmoothCamera camera;
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
	public MapScroller(SmoothCamera camera, float startY, float secondsPauseBeforeReturn)
	{
		this.camera = camera;
		this.startY = startY;
		this.secondsPauseBeforeReturn = secondsPauseBeforeReturn;
	}
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent touchEvent)
	{
		if(this.enabled){
			MotionEvent evt = touchEvent.getMotionEvent();
			
			if(evt.getAction() == MotionEvent.ACTION_DOWN)
			{
				this.scrollState = MapScroller.STATE_TOUCHDOWN;
				this.lastX = evt.getRawX();
				this.lastY = evt.getRawY();	
				this.speedX = 0;
				this.speedY = 0;
			}
			if(this.scrollState == MapScroller.STATE_TOUCHDOWN)
			{
				this.camera.setCenterDirect(camera.getCenterX() + (lastX - evt.getRawX()) / this.camera.getZoomFactor(), camera.getCenterY() + (lastY - evt.getRawY()) / this.camera.getZoomFactor());
				this.lastX = evt.getRawX();
				this.lastY = evt.getRawY();	
			}
			if(evt.getAction() == MotionEvent.ACTION_UP)
			{
				this.scrollState = MapScroller.STATE_SCROLLING;
			}
		}
		
		return false;
	}


	@Override
	public void onUpdate(float pSecondsElapsed)
	{
		if(this.enabled){
			if(this.scrollState == MapScroller.STATE_SCROLLING && (
					this.speedX == 0 && this.speedY == 0 ||
					this.camera.getYMax() == this.camera.getBoundsYMax()
					|| this.camera.getYMin() == this.camera.getBoundsYMin())
					){
				this.secondsAfterScrollEnded = 0;
				this.scrollState = this.STATE_SCROLLING_ENDED;
				
			}
			
			switch(this.scrollState){
			case MapScroller.STATE_SCROLLING:
				
				//Log.v("AndEngine", "SpeedX: " + String.valueOf(this.speedX) + " SpeedY: " + String.valueOf(this.speedY));
				this.camera.setCenterDirect(camera.getCenterX() - speedX * pSecondsElapsed / this.camera.getZoomFactor(), camera.getCenterY() - speedY * pSecondsElapsed / this.camera.getZoomFactor());
				
				this.speedX *= (1.0f - 1.2f * pSecondsElapsed);
				this.speedY *= (1.0f - 1.2f * pSecondsElapsed);
				
				if(speedX < 10 && speedX > -10) speedX = 0;
				if(speedY < 10 && speedY > -10) speedY = 0;
				break;
			case MapScroller.STATE_SCROLLING_ENDED:
				this.speedX = 0;
				this.speedY = 0;
				if(secondsAfterScrollEnded >= secondsPauseBeforeReturn){
					this.camera.setCenter(camera.getCenterX(), startY + this.camera.getHeight()/2f);
					this.scrollState = MapScroller.STATE_START;
				}
				else{
					this.secondsAfterScrollEnded += pSecondsElapsed;
				}
				break;
				
			}
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
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public boolean isEnabled() {
		return enabled;
	}
}
