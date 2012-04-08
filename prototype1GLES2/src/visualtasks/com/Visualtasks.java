package visualtasks.com;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.input.touch.detector.ClickDetector.IClickDetectorListener;
import org.andengine.input.touch.detector.HoldDetector;
import org.andengine.input.touch.detector.HoldDetector.IHoldDetectorListener;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.StrokeFont;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.ui.dialog.StringInputDialogBuilder;
import org.andengine.util.call.Callback;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Visualtasks extends SimpleBaseGameActivity {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final float CAMERA_HEIGHT = 720;
	private static final float CAMERA_WIDTH = 1080;
	private static final int TRIGGER_HOLD_MIN_MILISECONDS = 500;
	
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private Font mFont;
	private Scene mScene;
	// private Camera mCamera;
	private TouchController mTouchController;
	private ArrayList<Task> mTasksList;
	private HashMap<Task, TaskSprite> mTaskToSprite ;
	private ITextureRegion mParallaxLayerFront;
	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;
	private ITextureRegion mTaskTextureRegion;
	private ZoomCamera mZoomCamera;
	

    
    
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	public Visualtasks() {
		mTasksList = new ArrayList<Task>();
		mTaskToSprite = new HashMap<Task,TaskSprite>();
	}

	
	

//	@Override
//	public void onHoldFinished(HoldDetector arg0, long arg1, final float arg2,
//			final float arg3) {
//		runOnUiThread(new Runnable() {
//
//			@Override
//			public void run() {
//				mHoldDetector.setEnabled(false);
//				showPopUp(arg2, arg3);
//				mHoldDetector.setEnabled(true);
//			}
//		});
//
//	}

	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mZoomCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		final EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), this.mZoomCamera);
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);
		
		return engineOptions;
		
	}

	// Methods for menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	@Override
	protected void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		final ITexture strokeFontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		this.mTaskTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "bubble.png",0, 0);
		this.mBitmapTextureAtlas.load();
		
		//load font 
		this.mFont = new StrokeFont(this.getFontManager(), strokeFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 56, true, Color.WHITE,2, Color.BLACK);
		this.mFont.load();
		
		//load background
		this.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 2048, 2048, TextureOptions.DEFAULT);
		this.mParallaxLayerFront = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "achtergrond1.png", 0, 0);
		mAutoParallaxBackgroundTexture.load();
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
	
		this.mTouchController = new TouchController(mScene);
		this.mScene.setOnSceneTouchListener(this.mTouchController);
		this.mScene.setOnAreaTouchTraversalFrontToBack();
		//bg stuff
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront, vertexBufferObjectManager)));
		this.mScene.attachChild(new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront, vertexBufferObjectManager));
		//end bg stuff

		
		
		
		//this.mScene.registerUpdateHandler(mHoldDetector);

		return this.mScene;
	}

	
	
	

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.text:
			showNewTaskPopUp();
			// spawnTask();
			break;
		}
		return true;
	}

	

	
	
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("tasks", mTasksList);
		super.onSaveInstanceState(outState);
		
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Serializable serializable = savedInstanceState.getSerializable("tasks");
		if (serializable instanceof ArrayList<?>){
			mTasksList = (ArrayList<Task>)serializable;
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	private void showNewTaskPopUp() {
		final float centerX = (CAMERA_WIDTH - this.mTaskTextureRegion.getWidth()) / 2f;
		final float centerY = (CAMERA_HEIGHT - this.mTaskTextureRegion.getHeight()) / 2f;
		Task task = new Task("",new float[]{centerX,centerY});
		this.showEditTaskPopUp(task);
	}

	
	
	
	
	private void showEditTaskPopUp(final Task task) {
		final Context ct = this;
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				
				Dialog dialog = new StringInputDialogBuilder(ct,R.string.dialog_task_new_title,0,
					    R.string.dialog_task_new_message, android.R.drawable.ic_dialog_info,
					    task.getDescription(),
					    new Callback<String>() {
												
							@Override
							public void onCallback(String pCallbackValue) {
								task.setDescription(pCallbackValue);
								updateTask(task);
								
							}
					
					},   new OnCancelListener() {

						@Override
						public void onCancel(DialogInterface arg0) {
							// nothing
							
						}}
					).create();
				dialog.show();
			}});
		
		
	}
	
	private void updateTask(Task task){
		if(!mTasksList.contains(task)){
			this.mTasksList.add(0, task);
		}
		if(mTaskToSprite.containsKey(task)){
			TextSprite taskSprite = mTaskToSprite.get(task);
			taskSprite.setPosition(task.getXCoord(), task.getYCoord());
			// update text
		} else {
			TaskSprite taskSprite = new TaskSprite(task, mFont,mTaskTextureRegion, this.getVertexBufferObjectManager());
			
			
			this.mTaskToSprite.put(task, taskSprite);
			
			this.mScene.attachChild(taskSprite);
			this.mScene.registerTouchArea(taskSprite);
		}
		
		
	}
	
	
	/**
	 * Context menu after holding on task
	 * @param task The task to be edited
	 */
	private void contextMenu(final Task task) {
		final Context ct = this;
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
		
				//set up dialog
		        Dialog dialog = new Dialog(ct);
		        dialog.setContentView(R.layout.contextmenu);
		        dialog.setTitle(R.string.dialog_edit_task);
		
		        //set up buttons
		        Button editTitle = (Button) dialog.findViewById(R.id.edit_name);
		        editTitle.setOnClickListener(new OnClickListener() {
		
					@Override
					public void onClick(View arg0) {
						showEditTaskPopUp(task);
						
					}
					
		        });
		        
		        Button delete = (Button) dialog.findViewById(R.id.delete);
		        delete.setOnClickListener(new OnClickListener() {
		
					@Override
					public void onClick(View arg0) {
						// afhandelen delete task
						
					}
					
		        });
		        
		        Button complete = (Button) dialog.findViewById(R.id.complete);
		        complete.setOnClickListener(new OnClickListener() {
		
					@Override
					public void onClick(View arg0) {
						// afhandelen completen task
						
					}
					
		        });
		        
   
		        dialog.show();
			}});
	}
	
	
	
	

	
	
	
	
	
	class TouchController implements IOnSceneTouchListener,IScrollDetectorListener, IPinchZoomDetectorListener, IHoldDetectorListener{

		private PinchZoomDetector mPinchZoomDetector;
		private SurfaceScrollDetector mSurfaceScrollDetector;
		private HoldDetector mHoldDetector;
		private Scene mScene;
		private float mPinchZoomStartedCameraZoomFactor;

		public TouchController(Scene pScene) {
			this.mScene = pScene;
			this.mHoldDetector = new HoldDetector(this);
			this.mHoldDetector.setTriggerHoldMinimumMilliseconds(TRIGGER_HOLD_MIN_MILISECONDS);
			this.mSurfaceScrollDetector = new SurfaceScrollDetector(this);
			this.mPinchZoomDetector = new PinchZoomDetector(this);
			this.mScene.setTouchAreaBindingOnActionDownEnabled(true);
			
		}
		
		@Override
		public void onHold(HoldDetector pHoldDetector, long pHoldTimeMilliseconds, int pPointerID, float pHoldX, float pHoldY) {
			
		}

		@Override
		public void onHoldFinished(HoldDetector pHoldDetector, long pHoldTimeMilliseconds, int pPointerID, float pHoldX, float pHoldY) {
			//verplaatst naar onHoldStarted(), vond ik logischer
			/**pHoldDetector.setEnabled(false);
			showPopUp(pHoldX, pHoldY);
			pHoldDetector.setEnabled(true);**/
		}

		@Override
		public void onHoldStarted(HoldDetector pHoldDetector, int pPointerID, float pHoldX, float pHoldY) {
			pHoldDetector.setEnabled(false);
			showNewTaskPopUp();
			pHoldDetector.setEnabled(true);
		}
		
		@Override
		public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
			if (this.mPinchZoomDetector != null) {
				this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);

				if (this.mPinchZoomDetector.isZooming()) {
					this.mSurfaceScrollDetector.setEnabled(false);
					this.mHoldDetector.setEnabled(false);

				} else {
					if (pSceneTouchEvent.isActionDown()) {

						this.mSurfaceScrollDetector.setEnabled(true);
						this.mHoldDetector.setEnabled(true);
					}
					this.mHoldDetector.onTouchEvent(pSceneTouchEvent);
					if(!mHoldDetector.isHolding())
						this.mSurfaceScrollDetector.onTouchEvent(pSceneTouchEvent);

				}
			} else {
				this.mSurfaceScrollDetector.onTouchEvent(pSceneTouchEvent);
			}

			return true;
		}

		@Override
		public void onScrollStarted(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
			final float zoomFactor = mZoomCamera.getZoomFactor();
			mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
		}

		@Override
		public void onScroll(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
			final float zoomFactor = mZoomCamera.getZoomFactor();
			mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
		}
		
		@Override
		public void onScrollFinished(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
			final float zoomFactor = mZoomCamera.getZoomFactor();
			mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
		}

		@Override
		public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
			mPinchZoomStartedCameraZoomFactor = mZoomCamera.getZoomFactor();
		}

		@Override
		public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
			mZoomCamera.setZoomFactor(mPinchZoomStartedCameraZoomFactor * pZoomFactor);
		}

		@Override
		public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
			mZoomCamera.setZoomFactor(mPinchZoomStartedCameraZoomFactor * pZoomFactor);
		}
		
	}
	
	class TaskSprite extends TextSprite implements IClickDetectorListener,IPinchZoomDetectorListener, IHoldDetectorListener, IScrollDetectorListener {
		private HoldDetector mHoldDetector; 
		private PinchZoomDetector mPinchZoomDetector;
		private ScrollDetector mScrollDetector;
		
		private Task mTask;
		
		private float mStartScaleX, mStartScaleY;
		public TaskSprite(Task pTask, Font pFont, ITextureRegion pTextureRegion, VertexBufferObjectManager pVBOM) {
			super(pTask.getDescription(),pTask.getXCoord(), pTask.getYCoord(), pFont, pTextureRegion, pVBOM);
			this.mTask = pTask;
			
			this.mHoldDetector = new HoldDetector(this);
			this.mPinchZoomDetector = new PinchZoomDetector(this);
			this.mScrollDetector = new ScrollDetector(this);
			this.mHoldDetector.setTriggerHoldMinimumMilliseconds(TRIGGER_HOLD_MIN_MILISECONDS);
			
			
			
			
		}
		@Override
		public void onPinchZoom(PinchZoomDetector arg0, TouchEvent arg1, float pZoomFactor) {
			this.setScale(pZoomFactor * mStartScaleX, pZoomFactor * mStartScaleY);
			
		
		}
		
		@Override
		public void onPinchZoomFinished(PinchZoomDetector arg0, TouchEvent arg1,
				float pZoomFactor) {
			this.setScale(pZoomFactor * mStartScaleX,pZoomFactor * mStartScaleY);
		
		}
		
		@Override
		public void onPinchZoomStarted(PinchZoomDetector arg0, TouchEvent arg1) {
			this.mStartScaleX = this.getScaleX();
			this.mStartScaleY = this.getScaleY();
			
		
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
			this.mHoldDetector.setEnabled(false);
			//showEditTaskPopUp(this.mTask);
			contextMenu(this.mTask);
			this.mHoldDetector.setEnabled(true);
			
		}
		
		
		@Override
		public boolean onAreaTouched(TouchEvent pSceneTouchEvent,float pTouchAreaLocalX, float pTouchAreaLocalY) {
			if (this.mPinchZoomDetector != null) {
				this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);

				if (this.mPinchZoomDetector.isZooming()) {
					this.mScrollDetector.setEnabled(false);
					this.mHoldDetector.setEnabled(false);

				} else {
					if (pSceneTouchEvent.isActionDown()) {

						this.mScrollDetector.setEnabled(true);
						this.mHoldDetector.setEnabled(true);
					}
					this.mHoldDetector.onTouchEvent(pSceneTouchEvent);
					if(!mHoldDetector.isHolding())
						this.mScrollDetector.onTouchEvent(pSceneTouchEvent);

				}
			} 
			return true;
		}
		@Override
		public void onScroll(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
			
			this.setPosition(this.getX() + pDistanceX, this.getY() + pDistanceY);
			
		}
		@Override
		public void onScrollFinished(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
			this.setPosition(this.getX() + pDistanceX, this.getY() + pDistanceY);
			
		}
		@Override
		public void onScrollStarted(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
			
			this.setPosition(this.getX() + pDistanceX, this.getY() + pDistanceY);
			
			
		}
	
	}

}
