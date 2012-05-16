package visualtasks.com;

import java.io.IOException;
import java.io.InputStream;

import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
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
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.io.in.IInputStreamOpener;

import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class TaskSprite extends TextSprite implements IClickDetectorListener,IPinchZoomDetectorListener, IHoldDetectorListener, IScrollDetectorListener{
	
	private final static float SCALE_FACTOR = 1.0f;
	private static final float SCALE_MAX = 5f;
	private static final float SCALE_DEFAULT = 1f;
	private static final float SCALE_MIN = 0.5f;
	private float VELOCITY;
	private static int BORDER = 7;
	private static final int TRIGGER_HOLD_MIN_MILISECONDS = 300;
	private boolean isTouched; 
	private Visualtasks mContext;
	private ContinuousHoldDetector mHoldDetector;
	//private final PhysicsHandler mPhysicsHandler;
	private final Body faceBody;

	private PinchZoomDetector mPinchZoomDetector;
	private ScrollDetector mScrollDetector;
	private float mStartScaleX, mStartScaleY;
	private Task mTask;
	private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1f, 1f, 1f);
	private Body body;
	private BitmapTexture mTexture;
	private ITextureRegion mFaceTextureRegion;
	
	public TaskSprite(Visualtasks pContext, Task pTask, Font pFont,ITextureRegion pTextureRegion, VertexBufferObjectManager vBOM) {
		super(pTask.getDescription(),pFont, pTextureRegion, vBOM);
		this.mTask = pTask;
		this.mContext = pContext;
		/**
		this.mPhysicsHandler = new PhysicsHandler(this);
		this.registerUpdateHandler(this.mPhysicsHandler);
		VELOCITY = this.getScaleX()*this.getScaleX()*20;
		this.mPhysicsHandler.setVelocity(0, -VELOCITY);
		**/
		
		// {dit gedeelte zorgt er voor dat body kleiner is dan sprite zodat wanneer je verkleint die body niet te groot is.
		try {
			this.mTexture = new BitmapTexture(mContext.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return mContext.getAssets().open("gfx/addButton.png");
				}
			});

			this.mTexture.load();
			this.mFaceTextureRegion = TextureRegionFactory.extractFromTexture(this.mTexture);
		} catch (IOException e) {
			Log.d("Exception", "!");
		}

		final Sprite face = new Sprite(0, 0, this.mFaceTextureRegion, this.getVertexBufferObjectManager());
		// eind }
		
		
		body = PhysicsFactory.createCircleBody(pContext.mPhysicsWorld, face, BodyType.DynamicBody, FIXTURE_DEF);
		this.setUserData(body);
		
		faceBody = (Body) this.getUserData();
		pContext.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true));
		
		VELOCITY = this.getScaleX()*this.getScaleX();
		final Vector2 velocityv = Vector2Pool.obtain(0, -VELOCITY);
		faceBody.setFixedRotation(true);
		faceBody.setLinearVelocity(velocityv);
		Vector2Pool.recycle(velocityv);
		
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
				//this.mPhysicsHandler.setEnabled(false);
				//body.setActive(false); // zorgt ervoor dat bubbel niet meer naar boven drijft, maar ook dat er geen collision meer is.
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
				//this.mPhysicsHandler.setEnabled(true);
				//body.setActive(true);
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
		VELOCITY = this.getScaleX()*this.getScaleX();
		//Log.d("y = ",""+ body.getPosition().y);
		
		if(body.getPosition().y > BORDER) {
			/**this.mPhysicsHandler.setVelocityY(0);
			this.mPhysicsHandler.setVelocityX(0);**/
			final Vector2 velocityv = Vector2Pool.obtain(0, -VELOCITY);
			faceBody.setLinearVelocity(velocityv);
			Vector2Pool.recycle(velocityv);
		} else {
			//this.mPhysicsHandler.setVelocityY(-VELOCITY);
			final Vector2 velocityv = Vector2Pool.obtain(0, 0);
			faceBody.setLinearVelocity(velocityv);
			Vector2Pool.recycle(velocityv);
		}

			
		super.onManagedUpdate(pSecondsElapsed);
		
		//this.setPosition(mTask.getX(), mTask.getY());
		//body.setTransform(new Vector2(200, 200), 0);
		/**
		this.setText(mTask.getDescription());
		setSpritePosition(mTask.getX(), mTask.getY());
			if(!this.mPinchZoomDetector.isZooming()){
				this.setScale(this.getScaleFromUrgency(mTask.getUrgency()));
			}
		
		super.onManagedUpdate(pSecondsElapsed);**/
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
		
		mTask.setX(mTask.getX() + pDistanceX);
		mTask.setY(mTask.getY() + pDistanceY);
		setSpritePosition(mTask.getX(), mTask.getY());

		
	}
	@Override
	public void onScrollFinished(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
		
		mTask.setX(mTask.getX() + pDistanceX);
		mTask.setY(mTask.getY() + pDistanceY);
		setSpritePosition(mTask.getX(), mTask.getY());
		
	}
	
	

	@Override
	public void onScrollStarted(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
		
				
	}
	
	private void setSpritePosition(float x, float y) {
		body.setTransform(new Vector2(x, y).mul(1 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT), 0);
		//Log.d(x+",","y");
	}

}


