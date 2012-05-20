package visualtasks.com;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.input.touch.detector.ClickDetector.IClickDetectorListener;
import org.andengine.input.touch.detector.ContinuousHoldDetector;
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
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class TaskSprite extends TextSprite implements IClickDetectorListener,IPinchZoomDetectorListener, IHoldDetectorListener, IScrollDetectorListener{
	
	private final static float SCALE_FACTOR = 0.5f;
	private static final float SCALE_MAX = 5f;
	private static final float SCALE_DEFAULT = 1f;
	private static final float SCALE_MIN = 0.5f;
	private static float VELOCITY;
	private static int BOARDER = 200;
	private static final int TRIGGER_HOLD_MIN_MILISECONDS = 300;
	private boolean isTouched; 
	private Visualtasks mContext;
	private ContinuousHoldDetector mHoldDetector;
	private final PhysicsHandler mPhysicsHandler;
	private boolean isSelected;
	private PinchZoomDetector mPinchZoomDetector;
	private ScrollDetector mScrollDetector;
	private float mStartScaleX, mStartScaleY;
	private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0f, 0f);
	private Body body;
	
	
	public TaskSprite(String description, Visualtasks pContext, Font pFont,ITextureRegion pTextureRegion, VertexBufferObjectManager vBOM) {
		super(description,pFont, pTextureRegion, vBOM);
		this.mContext = pContext;
		this.mPhysicsHandler = new PhysicsHandler(this);
		this.registerUpdateHandler(this.mPhysicsHandler);
		VELOCITY = this.getScaleX()*this.getScaleX()*20;
		this.mPhysicsHandler.setVelocity(0, VELOCITY);
		
		body = PhysicsFactory.createCircleBody(pContext.mPhysicsWorld, this, BodyType.DynamicBody, FIXTURE_DEF);
		this.setUserData(body);
		pContext.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true));
		
		init();
	}
	
	private static float getScaleFromUrgency(Float urgency){
		return (SCALE_DEFAULT-urgency) * SCALE_FACTOR;
	}
		
	private static float getUrgencyFromScale(Float scale){
		return SCALE_DEFAULT-(scale/SCALE_FACTOR);
	}
	
	
	private void init(){
		this.setScale(SCALE_FACTOR);
		this.mHoldDetector = new ContinuousHoldDetector(this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);
		this.mScrollDetector = new ScrollDetector(this);
		this.mHoldDetector.setTriggerHoldMinimumMilliseconds(TRIGGER_HOLD_MIN_MILISECONDS);
		this.registerUpdateHandler(mHoldDetector);
		
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
				this.mPhysicsHandler.setEnabled(false);
			case TouchEvent.ACTION_MOVE:
				if(this.isSelected != true){
					this.isSelected = true;
//					mContext.reorderTasks();
				}
				
				break;
			case TouchEvent.ACTION_UP:
			case TouchEvent.ACTION_CANCEL:
				if(this.isSelected != false){
					this.isSelected = false;
//					mContext.reorderTasks();
				}
				this.mPhysicsHandler.setEnabled(true);
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
		
		
	}
	@Override
	public void onHoldFinished(HoldDetector arg0, long arg1, int arg2,
			float arg3, float arg4) {
	
		
	}
	
	@Override
	public void onHoldStarted(HoldDetector arg0, int arg1, float arg2,
			float arg3) {
		this.mHoldDetector.setEnabled(false);
		final Bundle bundle = new Bundle();
		bundle.putLong(Visualtasks.KEY_TASK_ID, 0);
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
		VELOCITY = this.getScaleX()*this.getScaleX()*20;
		
		if(this.mY < BOARDER) {
			this.mPhysicsHandler.setVelocityY(0);
			this.mPhysicsHandler.setVelocityX(0);
		} else {
			this.mPhysicsHandler.setVelocityY(-VELOCITY);
		}
			
		//super.onManagedUpdate(pSecondsElapsed);
		
		//this.setPosition(mTask.getX(), mTask.getY());
		//body.setTransform(new Vector2(200, 200), 0);
		
//		this.setText(mTask.getDescription());
//		setSpritePosition(mTask.getX(), mTask.getY());
			if(!this.mPinchZoomDetector.isZooming()){
//				this.setScale(this.getScaleFromUrgency(mTask.getUrgency()));
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
//		this.mTask.setUrgency(urgency);
//		mContext.reorderTasks();
		
	}
	
	
	
	@Override
	public void onPinchZoomStarted(PinchZoomDetector arg0, TouchEvent arg1) {
		
			this.mStartScaleX = this.getScaleX();
			this.mStartScaleY = this.getScaleY();
	}
	
	@Override
	public void onScroll(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
		
//		mTask.setX(mTask.getX() + pDistanceX);
//		mTask.setY(mTask.getY() + pDistanceY);
//		setSpritePosition(mTask.getX(), mTask.getY());

		
	}
	@Override
	public void onScrollFinished(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
		
//		mTask.setX(mTask.getX() + pDistanceX);
//		mTask.setY(mTask.getY() + pDistanceY);
//		setSpritePosition(mTask.getX(), mTask.getY());
		
	}
	
	

	@Override
	public void onScrollStarted(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
		
				
	}
	
	private void setSpritePosition(float x, float y) {
		body.setTransform(new Vector2(x, y).mul(1 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT), 0);
		Log.d(x+",","y");
	}

}


