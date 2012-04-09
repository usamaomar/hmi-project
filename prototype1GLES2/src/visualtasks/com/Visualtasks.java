package visualtasks.com;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.andengine.engine.Engine;
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
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.ui.dialog.StringInputDialogBuilder;
import org.andengine.util.call.Callback;

import android.app.Dialog;
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

	private static final float CAMERA_HEIGHT = 800;
	private static final float CAMERA_WIDTH = 1280;
	private static final float CAMERA_ZOOM_FACTOR = 2f;
	
	private static final int TRIGGER_HOLD_MIN_MILISECONDS = 300;
	
	public static final int DIALOG_NEW_TASK_ID = 0;
	public static final int DIALOG_EDIT_TASK_ID = DIALOG_NEW_TASK_ID + 1;
	public static final int DIALOG_CONTEXT_ID = DIALOG_EDIT_TASK_ID + 1;
	public static final String KEY_TASK_LIST = "key_task_list"; 
	public static final String KEY_TASK = "key_task";
	public static final String KEY_TASK_X = "tX";
	public static final String KEY_TASK_Y = "tY";
	
	
	private ArrayList<Task> mTaskList;
	
	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private Font mFont;
	private ITextureRegion mParallaxLayerFront;
	private Scene mScene;
	private ITextureRegion mTaskTextureRegion;
	// private Camera mCamera;
	private TouchController mTouchController;
	private ZoomCamera mZoomCamera;


	private HashMap<Task, TaskSprite> mTaskToSprite = new HashMap<Task, TaskSprite>();
	
    private Bundle mSavedInstanceState;
    
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	

	
	

	@Override
	public Engine onCreateEngine(EngineOptions pEngineOptions) {

		if (mTaskList == null) this.mTaskList = new ArrayList<Task>();
		
		return super.onCreateEngine(pEngineOptions);
	}

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		this.mSavedInstanceState = pSavedInstanceState;
		if (this.mSavedInstanceState != null && mSavedInstanceState.containsKey(KEY_TASK_LIST)){
			this.mTaskList = (ArrayList<Task>) mSavedInstanceState.getSerializable(KEY_TASK_LIST);
			
		}
		
		super.onCreate(pSavedInstanceState);
	}
	
	
	
	
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mZoomCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		this.mZoomCamera.setBounds(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		this.mZoomCamera.setZoomFactor(CAMERA_ZOOM_FACTOR);
		this.mZoomCamera.setBoundsEnabled(true);
		
		final EngineOptions engineOptions = new EngineOptions(true,ScreenOrientation.LANDSCAPE_FIXED, 
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mZoomCamera);
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);
		
		return engineOptions;
		
	}

	
	
	
	
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
		this.mTaskTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "bubble3.png",0, 0);
		this.mBitmapTextureAtlas.load();
		
		//load font 
		this.mFont = new StrokeFont(this.getFontManager(), strokeFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 56, true, Color.WHITE,2, Color.BLACK);
		this.mFont.load();
		
		//load background
		this.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 2048, 2048, TextureOptions.DEFAULT);
		this.mParallaxLayerFront = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "windows7.jpg", 0, 0);
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
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront, this.getVertexBufferObjectManager())));
		this.mScene.attachChild(new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront, this.getVertexBufferObjectManager()));
		
		//end bg stuff

		
		
		
		//this.mScene.registerUpdateHandler(mHoldDetector);
		
		return this.mScene;
	}
	
		
	@Override
	public synchronized void onGameCreated() {
		updateAllTaskSpritesForTasks(this.mTaskList);
		super.onGameCreated();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.text:
			
			showDialog(DIALOG_NEW_TASK_ID);
			// spawnTask();
			break;
		}
		return true;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	protected Dialog onCreateDialog(int id, final Bundle bundle) {
		switch(id){
		case DIALOG_NEW_TASK_ID:
			final float pX = bundle != null && bundle.containsKey(KEY_TASK_X) ? bundle.getFloat(KEY_TASK_X) : (this.mZoomCamera.getXMax()-this.mZoomCamera.getXMin())/2f;
			final float pY = bundle != null &&bundle.containsKey(KEY_TASK_Y) ? bundle.getFloat(KEY_TASK_Y) : (this.mZoomCamera.getYMax()-this.mZoomCamera.getYMin())/2f;
			
			return new StringInputDialogBuilder(this,R.string.dialog_task_new_title,0, R.string.dialog_task_new_message, android.R.drawable.ic_dialog_info, "",
				    new Callback<String>() {
						@Override
						public void onCallback(String pCallbackValue) {
							Visualtasks.this.addTask(pCallbackValue, pX, pY);
							Visualtasks.this.removeDialog(DIALOG_NEW_TASK_ID);
						}},   
					new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface arg0) {
							// nothing
							
						}})
			.create();
			
		case DIALOG_EDIT_TASK_ID:
			final Task task = bundle != null && bundle.containsKey(KEY_TASK) ? (Task)bundle.getSerializable(KEY_TASK) : null;
			if (task != null){
				return new StringInputDialogBuilder(this,R.string.dialog_task_new_title,0, R.string.dialog_task_new_message, android.R.drawable.ic_dialog_info,
						task.getDescription(), 
						new Callback<String>() { 							
							@Override
							public void onCallback(String pCallbackValue) {
								
									task.setDescription(pCallbackValue);
									Visualtasks.this.updateTaskSpriteForTask(task);
									Visualtasks.this.sortTasks();
									Visualtasks.this.removeDialog(DIALOG_EDIT_TASK_ID);
								
							}},   
						new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								// nothing
							}})
				.create();
			}
		case DIALOG_CONTEXT_ID:
			final Task task1 = bundle != null && bundle.containsKey(KEY_TASK) ? (Task)bundle.getSerializable(KEY_TASK) : null;
			//set up dialog
	        Dialog dialog = new Dialog(this);
	        dialog.setContentView(R.layout.contextmenu);
	        dialog.setTitle(R.string.dialog_edit_task);
	        dialog.setCancelable(true);
	
	        //set up buttons
	        Button editTitle = (Button) dialog.findViewById(R.id.edit_name);
	        editTitle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Visualtasks.this.dismissDialog(DIALOG_CONTEXT_ID);
					Visualtasks.this.showDialog(DIALOG_EDIT_TASK_ID, bundle);
				}});
	        
	        Button delete = (Button) dialog.findViewById(R.id.delete);
	        delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
						Visualtasks.this.dismissDialog(DIALOG_CONTEXT_ID);
						Visualtasks.this.deleteTask(task1);
					
					
				}});
	        
	        Button complete = (Button) dialog.findViewById(R.id.complete);
	        complete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Visualtasks.this.dismissDialog(DIALOG_CONTEXT_ID);
					Visualtasks.this.deleteTask(task1);
					
				}});
	        
	        dialog.setOnCancelListener( new OnCancelListener() {
	        	 
	            public void onCancel(DialogInterface dialog) {
	                // Cancel
	            }
	        });
	        
	        return dialog;
		}
		return super.onCreateDialog(id, bundle);
	}


	public void sortTasks(){
		Collections.sort(mTaskList);
		
		float urgencyOffset = mTaskList.isEmpty()? 0 :mTaskList.get(mTaskList.size()-1).getUrgency();
		int i = 0;
		for (Task task : mTaskList){
			task.setUrgency(task.getUrgency() - urgencyOffset);
			if(mTaskToSprite.containsKey(task)){
				TaskSprite taskSprite = mTaskToSprite.get(task);
				this.mScene.detachChild(taskSprite);
				this.mScene.unregisterTouchArea(taskSprite);
				this.mScene.attachChild(taskSprite);
				this.mScene.registerTouchArea(taskSprite);
			}
			i++;
		}
	}
	private void addTask(String description, float pX, float pY){
		Task task = new Task(0,description, pX, pY);
		this.mTaskList.add(task);
		this.updateTaskSpriteForTask(task);
		this.sortTasks();
	}
	
	private void updateAllTaskSpritesForTasks(final ArrayList<Task> pTasks){
		for (Task task : pTasks){
			updateTaskSpriteForTask(task);
		}
		this.sortTasks();
	}
	private void updateTaskSpriteForTask(final Task pTask){
		this.removeTaskSpriteForTask(pTask);
		final TaskSprite taskSprite = new TaskSprite(this, pTask, mFont, mTaskTextureRegion, getVertexBufferObjectManager());
		
		mTaskToSprite.put(pTask, taskSprite);
		Visualtasks.this.mScene.attachChild(taskSprite);
		Visualtasks.this.mScene.registerTouchArea(taskSprite);
			
	}
	
	private void deleteTask(Task pTask){
		this.mTaskList.remove(pTask);
		this.removeTaskSpriteForTask(pTask);
	}
	
	private void removeTaskSpriteForTask(Task pTask){
		if(mTaskToSprite.containsKey(pTask)){
			final TextSprite textSprite = Visualtasks.this.mTaskToSprite.get(pTask);
			Visualtasks.this.mTaskToSprite.remove(pTask);
			Visualtasks.this.mScene.detachChild(textSprite);
			Visualtasks.this.mScene.unregisterTouchArea(textSprite);
			textSprite.dispose();
			this.mTaskToSprite.remove(pTask);
//			TODO Do something with sprite
		}
		
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(KEY_TASK_LIST, mTaskList);
		super.onSaveInstanceState(outState);
		
	}

	protected void onSceneHold(float pHoldX, float pHoldY){
		final Bundle bundle = new Bundle();
		bundle.putFloat(KEY_TASK_X, pHoldX);
		bundle.putFloat(KEY_TASK_Y, pHoldY);
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				Visualtasks.this.showDialog(DIALOG_NEW_TASK_ID, bundle);
			}});
	}
	
	

	
	class TouchController implements IOnSceneTouchListener,IScrollDetectorListener, IPinchZoomDetectorListener, IHoldDetectorListener{

		private int lastTouchId;
		private HoldDetector mHoldDetector;
		private float mPinchZoomStartedCameraZoomFactor;
		private Scene mScene;
		//private PinchZoomDetector mPinchZoomDetector;
		private SurfaceScrollDetector mSurfaceScrollDetector;
		public TouchController(Scene pScene) {
			this.mScene = pScene;
			this.mHoldDetector = new HoldDetector(this);
			this.mHoldDetector.setTriggerHoldMinimumMilliseconds(TRIGGER_HOLD_MIN_MILISECONDS);
			this.mSurfaceScrollDetector = new SurfaceScrollDetector(this);
			//this.mPinchZoomDetector = new PinchZoomDetector(this);
			this.mScene.setTouchAreaBindingOnActionDownEnabled(false);
			this.mScene.setTouchAreaBindingOnActionMoveEnabled(false);
			
			
			//this.mPinchZoomDetector.setEnabled(false);
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
			Visualtasks.this.onSceneHold(pHoldX, pHoldY);
			pHoldDetector.setEnabled(true);
		}
		
		@Override
		public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
			mZoomCamera.setZoomFactor(mPinchZoomStartedCameraZoomFactor * pZoomFactor);
		}

		@Override
		public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
			mZoomCamera.setZoomFactor(mPinchZoomStartedCameraZoomFactor * pZoomFactor);
		}

		@Override
		public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
			mPinchZoomStartedCameraZoomFactor = mZoomCamera.getZoomFactor();
		}
		
		@Override
		public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		//	if (this.mPinchZoomDetector != null) {
		//		this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);

		//		if (this.mPinchZoomDetector.isZooming()) {
//					this.mSurfaceScrollDetector.setEnabled(false);
//					this.mHoldDetector.setEnabled(false);
//
			//	} else {
//					if (pSceneTouchEvent.isActionDown()) {
//
//						this.mSurfaceScrollDetector.setEnabled(true);
//						this.mHoldDetector.setEnabled(true);
//					}
					this.mHoldDetector.onTouchEvent(pSceneTouchEvent);
					if(!mHoldDetector.isHolding())
						this.mSurfaceScrollDetector.onTouchEvent(pSceneTouchEvent);

			//	}
			//} else {
			//	this.mSurfaceScrollDetector.onTouchEvent(pSceneTouchEvent);
		//	}

			return true;
		}

		@Override
		public void onScroll(final ScrollDetector pScrollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
			if(pPointerID != lastTouchId){
				lastTouchId = pPointerID;
				pScrollDetector.reset();
			}
			else {
				final float zoomFactor = Visualtasks.this.mZoomCamera.getZoomFactor();
				Visualtasks.this.mZoomCamera.offsetCenter(-pDistanceX , -pDistanceY );
			}
		}

		@Override
		public void onScrollFinished(final ScrollDetector pScrollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
			if(pPointerID != lastTouchId){
				lastTouchId = pPointerID;
				pScrollDetector.reset();
			}
			else {
				final float zoomFactor = Visualtasks.this.mZoomCamera.getZoomFactor();
				Visualtasks.this.mZoomCamera.offsetCenter(-pDistanceX, -pDistanceY );
			}
			
		}

		@Override
		public void onScrollStarted(final ScrollDetector pScrollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
			lastTouchId = pPointerID;
			
		}
		
	}
	
	
	
	

}
