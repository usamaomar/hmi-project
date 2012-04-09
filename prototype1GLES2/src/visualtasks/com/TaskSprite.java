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
import org.andengine.ui.activity.BaseGameActivity;

import android.os.Bundle;

public class TaskSprite extends TextSprite implements IClickDetectorListener,IPinchZoomDetectorListener, IHoldDetectorListener, IScrollDetectorListener{
	
	private static final int TRIGGER_HOLD_MIN_MILISECONDS = 300;
	
	private HoldDetector mHoldDetector; 
	private PinchZoomDetector mPinchZoomDetector;
	private ScrollDetector mScrollDetector;

	private boolean mPinchToScaleEnabled, mScrollToMoveEnabled; 
	private Visualtasks mContext;
	private Task mTask;
	private float mStartScaleX, mStartScaleY;
	public TaskSprite(Visualtasks pContext, Task pTask, Font pFont,ITextureRegion pTextureRegion, VertexBufferObjectManager vBOM) {
		super(pTask.getDescription(),pFont, pTextureRegion, vBOM);
		this.mTask = pTask;
		this.mContext = pContext;
		init();
		// TODO Auto-generated constructor stub
	}
	
	private void init(){
		this.mHoldDetector = new HoldDetector(this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);
		this.mScrollDetector = new ScrollDetector(this);
		this.mHoldDetector.setTriggerHoldMinimumMilliseconds(TRIGGER_HOLD_MIN_MILISECONDS);
	}
		
	@Override
	protected void onManagedUpdate(float pSecondsElapsed) {
		
		this.setPosition(mTask.getX(), mTask.getY());
		this.setText(mTask.getDescription());
		super.onManagedUpdate(pSecondsElapsed);
	}
	
	
	@Override
	public void onPinchZoom(PinchZoomDetector arg0, TouchEvent arg1, float pZoomFactor) {
		this.setScale(pZoomFactor * mStartScaleX, pZoomFactor * mStartScaleY);
				
	
	}
	
	@Override
	public void onPinchZoomFinished(PinchZoomDetector arg0, TouchEvent arg1,float pZoomFactor) {
		
//				mITextSpriteListener.onTextSpriteScaleChanged(this, mStartScaleX, mStartScaleY, pZoomFactor);
		
	}
	
	@Override
	public void onPinchZoomStarted(PinchZoomDetector arg0, TouchEvent arg1) {
		
			this.mStartScaleX = this.getScaleX();
			this.mStartScaleY = this.getScaleY();
		
	
	}
	
	@Override
	public void onClick(ClickDetector arg0, int arg1, float arg2, float arg3) {
		
			//this.setPosition(this.getX() + pDistanceX, this.getY() + pDistanceY);
		
	
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
		
			this.mHoldDetector.setEnabled(false);
			final Bundle bundle = new Bundle();
			bundle.putSerializable(Visualtasks.KEY_TASK, this.mTask);
			this.mContext.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					TaskSprite.this.mContext.showDialog(Visualtasks.DIALOG_CONTEXT_ID, bundle);
					
				}});
			this.mHoldDetector.setEnabled(true);
		
	}
	
	
	
	@Override
	public void onScroll(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
		
		mTask.setX(this.getX() + pDistanceX);
		mTask.setY(this.getY() + pDistanceY);
		
	}
	@Override
	public void onScrollFinished(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
		
				mTask.setX(this.getX() + pDistanceX);
				mTask.setY(this.getY() + pDistanceY);
	
		
	}
	@Override
	public void onScrollStarted(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
		
				
	}
	
	

	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float arg2, float arg3) {
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
				
				
		
		return true;
	}

}


