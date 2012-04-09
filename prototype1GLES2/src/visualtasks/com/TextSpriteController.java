package visualtasks.com;

import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.input.touch.detector.HoldDetector;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ClickDetector.IClickDetectorListener;
import org.andengine.input.touch.detector.HoldDetector.IHoldDetectorListener;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class TextSpriteController implements IOnAreaTouchListener, IClickDetectorListener,IPinchZoomDetectorListener, IHoldDetectorListener, IScrollDetectorListener {
	private static final int TRIGGER_HOLD_MIN_MILISECONDS = 300;
	
	private HoldDetector mHoldDetector; 
	private PinchZoomDetector mPinchZoomDetector;
	private ScrollDetector mScrollDetector;
	private ITextSpriteListener mITextSpriteListener;
	private TextSprite mTextSprite;
	private boolean mPinchToScaleEnabled, mScrollToMoveEnabled; 
		
	private float mStartScaleX, mStartScaleY;
	public TextSpriteController(ITextSpriteListener pITextSpriteListener, boolean pinchToScaleEnabled, boolean scrollToMoveEnabled) {
		this.mITextSpriteListener = pITextSpriteListener;
		this.mPinchToScaleEnabled = pinchToScaleEnabled;
		this.mScrollToMoveEnabled = scrollToMoveEnabled;
		
		this.mHoldDetector = new HoldDetector(this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);
		this.mScrollDetector = new ScrollDetector(this);
		
		this.mPinchZoomDetector.setEnabled(mPinchToScaleEnabled);
		this.mScrollDetector.setEnabled(mScrollToMoveEnabled);
		this.mHoldDetector.setTriggerHoldMinimumMilliseconds(TRIGGER_HOLD_MIN_MILISECONDS);
		
		
		
	}
	
	@Override
	public void onPinchZoom(PinchZoomDetector arg0, TouchEvent arg1, float pZoomFactor) {
		if (mTextSprite != null){
			mTextSprite.setScale(pZoomFactor * mStartScaleX, pZoomFactor * mStartScaleY);
			
		}
	
	}
	
	@Override
	public void onPinchZoomFinished(PinchZoomDetector arg0, TouchEvent arg1,float pZoomFactor) {
		if (mTextSprite != null){
			mITextSpriteListener.onTextSpriteScaleChanged(mTextSprite, mStartScaleX, mStartScaleY, pZoomFactor);
		}
	}
	
	@Override
	public void onPinchZoomStarted(PinchZoomDetector arg0, TouchEvent arg1) {
		if (mTextSprite != null){
			this.mStartScaleX = mTextSprite.getScaleX();
			this.mStartScaleY = mTextSprite.getScaleY();
		}
	
	}
	
	@Override
	public void onClick(ClickDetector arg0, int arg1, float arg2, float arg3) {
		// TODO Auto-generated method stub
	
	}
	@Override
	public void onHold(HoldDetector arg0, long arg1, int arg2, float arg3,
			float arg4) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onHoldFinished(HoldDetector arg0, long arg1, int arg2,
			float arg3, float arg4) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onHoldStarted(HoldDetector arg0, int arg1, float arg2,
			float arg3) {
		if (mTextSprite != null){
			this.mHoldDetector.setEnabled(false);
			mITextSpriteListener.onTextSpriteHold(this.mTextSprite);
			this.mHoldDetector.setEnabled(true);
		}
	}
	
	
	
	@Override
	public void onScroll(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
		if(mTextSprite != null){
			mTextSprite.setPosition(mTextSprite.getX() + pDistanceX, mTextSprite.getY() + pDistanceY);
		}
	}
	@Override
	public void onScrollFinished(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
		if(mTextSprite != null){
			mTextSprite.setPosition(mTextSprite.getX() + pDistanceX, mTextSprite.getY() + pDistanceY);
			mITextSpriteListener.onTextSpritePositionChanged(mTextSprite, pDistanceX, pDistanceY);
		}
		
	}
	@Override
	public void onScrollStarted(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
		
				
	}
	
	

	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent, ITouchArea pITouchArea,
			float arg2, float arg3) {
		if (pITouchArea instanceof TextSprite){
			mTextSprite = (TextSprite) pITouchArea;
			switch(pSceneTouchEvent.getAction()){
			case TouchEvent.ACTION_OUTSIDE:
				break;
			case TouchEvent.ACTION_DOWN:
				
			case TouchEvent.ACTION_MOVE:
			default:
				this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);
				if (mPinchZoomDetector.isZooming()){
					this.mHoldDetector.setEnabled(false);
					this.mScrollDetector.setEnabled(false);
				} else{
					this.mHoldDetector.setEnabled(true);
					this.mHoldDetector.onTouchEvent(pSceneTouchEvent);
					if (this.mHoldDetector.isHolding()){
						this.mScrollDetector.setEnabled(false);
						this.mPinchZoomDetector.setEnabled(false);
						
					} else {
						this.mScrollDetector.setEnabled(true);
						this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
						this.mPinchZoomDetector.setEnabled(true);
					}
				}
			}
				
				
		}
		return true;
	}

}

