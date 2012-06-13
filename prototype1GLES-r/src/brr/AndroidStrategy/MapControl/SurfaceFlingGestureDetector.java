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
public class SurfaceFlingGestureDetector extends BaseDetector {
	// ===========================================================
	// Constants
	// ===========================================================

//	private static final float SWIPE_MIN_DISTANCE_DEFAULT = 120;

	// ===========================================================
	// Fields
	// ===========================================================

	private final GestureDetector mGestureDetector;

	// ===========================================================
	// Constructors
	// ===========================================================


	public SurfaceFlingGestureDetector(final Context pContext, final MapScroller pMapScroller) {
		this.mGestureDetector = new GestureDetector(pContext, new FlingGestureListener(pMapScroller));
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	
	

	@Override
	public void reset() {

	}

	@Override
	public boolean onManagedTouchEvent(final TouchEvent pSceneTouchEvent) {
		return this.mGestureDetector.onTouchEvent(pSceneTouchEvent.getMotionEvent());
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public class FlingGestureListener extends SimpleOnGestureListener
	{
		private MapScroller scroller;
		
		public FlingGestureListener(MapScroller scroller)
		{
			this.scroller = scroller;
		}
		
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{	
			this.scroller.setSpeedX(velocityX * .8f);
			this.scroller.setSpeedY(velocityY * .8f);
			return true;
		}

	}
}