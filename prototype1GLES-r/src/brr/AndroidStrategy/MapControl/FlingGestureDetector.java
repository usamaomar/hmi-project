package brr.AndroidStrategy.MapControl;

import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.BaseDetector;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

/**
 * @author rkpost
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 11:36:26 - 11.10.2010
 */
public class FlingGestureDetector extends BaseDetector {
	// ===========================================================
	// Constants
	// ===========================================================

//	private static final float SWIPE_MIN_DISTANCE_DEFAULT = 120;

	// ===========================================================
	// Fields
	// ===========================================================

	private final GestureDetector mGestureDetector;
	private IFlingGestureListener mOnFlingGestureListener;
	private TouchEvent lastTouchEvent;
	// ===========================================================
	// Constructors
	// ===========================================================


	public FlingGestureDetector(final Context pContext) {
		this.mGestureDetector = new GestureDetector(pContext, new InternGestureListener());
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	
	public void setOnFlingGestureListener(IFlingGestureListener mOnFlingGestureListener) {
		this.mOnFlingGestureListener = mOnFlingGestureListener;
	}
	

	@Override
	public void reset() {

	}

	@Override
	public boolean onManagedTouchEvent(final TouchEvent pSceneTouchEvent) {
		lastTouchEvent = pSceneTouchEvent;
		return this.mGestureDetector.onTouchEvent(pSceneTouchEvent.getMotionEvent());
		
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private class InternGestureListener extends SimpleOnGestureListener
	{
		
		
		
		
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{	
			
			mOnFlingGestureListener.onFling(lastTouchEvent, velocityX, velocityY);
//			this.scroller.setSpeedX(velocityX * .8f);
//			this.scroller.setSpeedY(velocityY * .8f);
			return true;
		}

	}
	
	public interface IFlingGestureListener {
		public void onFling(TouchEvent pSceneTouchEvent, float velocityX, float velocityY);
	}
	
	
}