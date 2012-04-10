package visualtasks.com;

import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.input.touch.detector.ClickDetector.IClickDetectorListener;
import org.andengine.input.touch.detector.HoldDetector;
import org.andengine.input.touch.detector.HoldDetector.IHoldDetectorListener;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.os.Bundle;

public class TaskSprite extends TextSprite implements IClickDetectorListener,IPinchZoomDetectorListener, IHoldDetectorListener, IScrollDetectorListener{
	
	private final static float SCALE_FACTOR = 0.5f;
	private static final float SCALE_MAX = 5f;
	private static final float SCALE_DEFAULT = 1f;
	private static final float SCALE_MIN = 0.5f;
	private static final int TRIGGER_HOLD_MIN_MILISECONDS = 300;
	private boolean isTouched; 
	private Visualtasks mContext;
	private HoldDetector mHoldDetector;

	private PinchZoomDetector mPinchZoomDetector;
	private ScrollDetector mScrollDetector;
	private float mStartScaleX, mStartScaleY;
	private Task mTask;
	public TaskSprite(Visualtasks pContext, Task pTask, Font pFont,ITextureRegion pTextureRegion, VertexBufferObjectManager vBOM) {
		super(pTask.getDescription(),pFont, pTextureRegion, vBOM);
		this.mTask = pTask;
		this.mContext = pContext;
		init();
		// TODO Auto-generated constructor stub
	}
	
	private float getScaleFromUrgency(Float urgency){
		return (SCALE_DEFAULT-urgency) * SCALE_FACTOR;
	}
		
	private float getUrgencyFromScale(Float scale){
		return SCALE_DEFAULT-(scale/SCALE_FACTOR);
	}
	
	
	private void init(){
		this.setScale(SCALE_FACTOR);
		this.mHoldDetector = new HoldDetector(this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);
		this.mScrollDetector = new ScrollDetector(this);
		this.mHoldDetector.setTriggerHoldMinimumMilliseconds(TRIGGER_HOLD_MIN_MILISECONDS);
	}
	
	
	
	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float arg2, float arg3) {
			switch(pSceneTouchEvent.getAction()){
			case TouchEvent.ACTION_OUTSIDE:
				
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
			
			switch(pSceneTouchEvent.getAction()){
			case TouchEvent.ACTION_DOWN:
			case TouchEvent.ACTION_MOVE:
				if(this.mTask.isSelected() != true){
					this.mTask.setSelected(true);
					mContext.reorderTasks();
				}
				
				break;
			case TouchEvent.ACTION_UP:
			case TouchEvent.ACTION_CANCEL:
				if(this.mTask.isSelected() != false){
					this.mTask.setSelected(false);
					mContext.reorderTasks();
				}
			}
				
		
		return true;
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
					TaskSprite.this.mContext.removeDialog(Visualtasks.DIALOG_CONTEXT_ID);
					TaskSprite.this.mContext.showDialog(Visualtasks.DIALOG_CONTEXT_ID, bundle);
					
				}});
			this.mHoldDetector.setEnabled(true);
		
	}
	@Override
	protected void onManagedUpdate(float pSecondsElapsed) {
		this.setPosition(mTask.getX(), mTask.getY());
		this.setText(mTask.getDescription());
			
			if(!this.mPinchZoomDetector.isZooming()){
				this.setScale(this.getScaleFromUrgency(mTask.getUrgency()));
			}
		
		super.onManagedUpdate(pSecondsElapsed);
		
	}
	@Override
	public void onPinchZoom(PinchZoomDetector arg0, TouchEvent arg1, float pZoomFactor) {
		// bound x and y
		this.setScale(pZoomFactor * mStartScaleX,  pZoomFactor * mStartScaleY);
				
	
	}
	@Override
	public void onPinchZoomFinished(PinchZoomDetector arg0, TouchEvent arg1,float pZoomFactor) {
		final float scaleX = Math.max(Math.min(this.getScaleX(), SCALE_MAX * SCALE_FACTOR),SCALE_MIN * SCALE_FACTOR);
		
		final float urgency = this.getUrgencyFromScale(scaleX);
		this.mTask.setUrgency(urgency);
		mContext.reorderTasks();
		
	}
	
	
	
	@Override
	public void onPinchZoomStarted(PinchZoomDetector arg0, TouchEvent arg1) {
		
			this.mStartScaleX = this.getScaleX();
			this.mStartScaleY = this.getScaleY();
		
	
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

}


