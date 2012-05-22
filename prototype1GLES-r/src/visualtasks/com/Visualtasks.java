package visualtasks.com;

import java.util.HashMap;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsConnectorManager;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ContinuousHoldDetector;
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
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.ui.dialog.StringInputDialogBuilder;
import org.andengine.util.call.Callback;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import data.Task;
import data.TaskDbHandler;

public class Visualtasks extends SimpleBaseGameActivity {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final float CAMERA_HEIGHT = 723;
	private static final float CAMERA_WIDTH = 1024 ;
	private static final float CAMERA_ZOOM_FACTOR = 1f;
	
	private static final int TRIGGER_HOLD_MIN_MILISECONDS = 300;
	
	public static final int DIALOG_NEW_TASK_ID = 0;
	public static final int DIALOG_EDIT_TASK_ID = DIALOG_NEW_TASK_ID + 1;
	public static final int DIALOG_CONTEXT_ID = DIALOG_EDIT_TASK_ID + 1;
	public static final String KEY_TASK_LIST = "key_task_list"; 
	public static final String KEY_TASK_ID = "key_task_id";
	public static final String KEY_TASK_X = "tX";
	public static final String KEY_TASK_Y = "tY";
	private static final int ACTIVITY_CREATE=0;
	private static final int ACTIVITY_EDIT=1;
	
	private TaskDbHandler mDbHandler;

	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private Font mFont;
	private ITextureRegion mParallaxLayerFront;
	private Scene mScene;
	private ITextureRegion mTaskTextureRegion;
	// private Camera mCamera;
	private TouchController mTouchController;
	private TaskSpriteController mTaskSpriteController;
	private ZoomCamera mZoomCamera;
	private BitmapTextureAtlas mHUDTexture;
	private TiledTextureRegion mToggleButtonTextureRegion;
	protected PhysicsWorld mPhysicsWorld;

//	private HashMap<Long,Task> mTaskList;
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		mDbHandler = new TaskDbHandler(this);
//		mTaskList = new HashMap<Long, Task>();
		super.onCreate(pSavedInstanceState);
	}
	

	@Override
	public synchronized void onResumeGame() {
		
		for(Task task : mDbHandler.getAllTasks()){
//			mTaskList.put(task.getID(), task);
			mTaskSpriteController.updateSpriteForTask(task);
		}

		super.onResumeGame();
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
		this.mTaskTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "bubble.png",0, 0);
		this.mBitmapTextureAtlas.load();
		
		//load font 
		this.mFont = new StrokeFont(this.getFontManager(), strokeFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 56, true, Color.WHITE,2, Color.BLACK);
		this.mFont.load();
		
		//load background
		this.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 1280, 800, TextureOptions.DEFAULT);
		this.mParallaxLayerFront = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "bg5.png", 0, 0);
		mAutoParallaxBackgroundTexture.load();
				
				
		this.mHUDTexture = new BitmapTextureAtlas(this.getTextureManager(), 150, 150,TextureOptions.BILINEAR);
		this.mToggleButtonTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mHUDTexture, this, "addButton.png", 0, 0, 1, 1);
		this.mHUDTexture.load();
	}
	
	
	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
	
		this.mTouchController = new TouchController(mScene);
		this.mTaskSpriteController = new TaskSpriteController(mScene);
		this.mScene.setOnSceneTouchListener(this.mTouchController);
		this.mScene.setOnAreaTouchListener(mTaskSpriteController);
		this.mScene.setOnAreaTouchTraversalFrontToBack();
		
		//bg stuff
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront, this.getVertexBufferObjectManager())));
		this.mScene.attachChild(new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront, this.getVertexBufferObjectManager()));
		//end bg stuff
		
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);
		
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		
		
		//hudButton
				final HUD hud = new HUD();
				
				final TiledSprite toggleButton = new TiledSprite(0, CAMERA_HEIGHT - this.mToggleButtonTextureRegion.getHeight(), this.mToggleButtonTextureRegion, this.getVertexBufferObjectManager()) {
					@Override
					public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
						if(pSceneTouchEvent.isActionDown()) {
								Visualtasks.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										showDialog(DIALOG_NEW_TASK_ID);
									}
								});
						}
						return true;
					}
				};
				hud.registerTouchArea(toggleButton);
				hud.attachChild(toggleButton);
				
				mZoomCamera.setZoomFactor(1.5f);
				this.mZoomCamera.setHUD(hud);
				
				//end hudButton**/
		
		return this.mScene;
	}
	
	
	
//	public synchronized void setSelectedTask(Task task){
//		if (this.mSelectedTask != null){
//			this.mSelectedTask.setSelected(false);
//		}
//		this.mSelectedTask = task;
//		this.mSelectedTask.setSelected(true);
//	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.text:
			showDialog(DIALOG_NEW_TASK_ID);
			
			break;
		case R.id.listview:
			
//			Intent i = new Intent(this, ListActivity.class);
//	        startActivity(i);
			// spawnTask();
			break;
		}
		
		return true;
	}

	@Override
	protected void onPause() {
//		List<Task> taskList = new ArrayList<Task>();
//		taskList.addAll(mTaskList.values());
//		mDbHandler.updateAllTasks(taskList);		
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
			final Long taskId = bundle != null && bundle.containsKey(KEY_TASK_ID) ? bundle.getLong(KEY_TASK_ID) : null;
			if (taskId != null){
				final Task task = mDbHandler.getTask(taskId);
				return new StringInputDialogBuilder(this,R.string.dialog_task_new_title,0, R.string.dialog_task_new_message, android.R.drawable.ic_dialog_info,
						task.getDescription(),
						new Callback<String>() { 							
							@Override
							public void onCallback(String pCallbackValue) {
								
									task.setDescription(pCallbackValue);
									Visualtasks.this.updateTask(task);
									
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
			final Long taskId2 = bundle != null && bundle.containsKey(KEY_TASK_ID) ? bundle.getLong(KEY_TASK_ID) : null;
			final Task task2 = mDbHandler.getTask(taskId2);
			//set up dialog
	        Dialog dialog = new Dialog(this);
	        dialog.setContentView(R.layout.contextmenu);
	        dialog.setTitle(R.string.dialog_edit_task);
	        dialog.setCancelable(true);
	        dialog.setCanceledOnTouchOutside(true);
	
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
						Visualtasks.this.deleteTask(task2);
						Toast.makeText(getApplicationContext(), task2.getDescription() + " deleted.", Toast.LENGTH_SHORT).show();
					
				}});
	        
	        Button complete = (Button) dialog.findViewById(R.id.complete);
	        complete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
						Visualtasks.this.dismissDialog(DIALOG_CONTEXT_ID);
						Visualtasks.this.deleteTask(task2);
						Toast.makeText(getApplicationContext(), task2.getDescription() + " completed.", Toast.LENGTH_SHORT).show();

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


	
	
	

	
//	public void reorderTasks(){
//		runOnUpdateThread(new Runnable(){
//
//			@Override
//			public void run() {
//				synchronized(mTaskList){
//					Collections.sort(mTaskList, new Task.UrgencyComparator());
//					
//					final float minUrgency = mTaskList.isEmpty()? 0 :mTaskList.get(0).getUrgency();
//					final float maxUrgency = mTaskList.isEmpty()? 0 :mTaskList.get(mTaskList.size()-1).getUrgency();
//					float urgencyOffset = 0; //minUrgency > 0 ? minUrgency : maxUrgency < 0 ? maxUrgency : 0 ;
//					
//					
//					Collections.sort(mTaskList, new Task.DefaultComparator());
//					
//					
//					for (int i = mTaskList.size()-1;i>=0;i--){
//						final Task task = mTaskList.get(i);
//						task.setUrgency(task.getUrgency() - urgencyOffset);
//						if(mTaskToSprite.containsKey(task)){
//							final TaskSprite taskSprite = Visualtasks.this.mTaskToSprite.get(task);
//						
//							Visualtasks.this.mScene.detachChild(taskSprite);
//							Visualtasks.this.mScene.unregisterTouchArea(taskSprite);
//							Visualtasks.this.mScene.attachChild(taskSprite);
//							Visualtasks.this.mScene.registerTouchArea(taskSprite);
//						}
//					}
//				}
//				
//			}});
//		
			
//	}
	

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
	
	

	private void addTask(String description, float pX, float pY){
		Task task = new Task();
		task.setDescription(description);
		task.setX(pX);
		task.setY(pY);
				
		mDbHandler.addTask(task);
		mTaskSpriteController.createSpriteForTask(task);
		
	}
	
	private void deleteTask(Task task){
		mTaskSpriteController.removeSpriteForTask(task);
		mDbHandler.deleteTask(task);
	}
	

	private void updateTask(Task task){
		mDbHandler.updateTask(task);
		mTaskSpriteController.updateSpriteForTask(task);
	}
	
	class TouchController implements IOnSceneTouchListener,IScrollDetectorListener, IPinchZoomDetectorListener, IHoldDetectorListener{

		private int lastTouchId;
		private ContinuousHoldDetector mHoldDetector;
		private float mPinchZoomStartedCameraZoomFactor;
		private Scene mScene;
		private PinchZoomDetector mPinchZoomDetector;
		private SurfaceScrollDetector mSurfaceScrollDetector;
		public TouchController(Scene pScene) {
			this.mScene = pScene;
			this.mHoldDetector = new ContinuousHoldDetector(this);
			this.mHoldDetector.setTriggerHoldMinimumMilliseconds(TRIGGER_HOLD_MIN_MILISECONDS);
			this.mScene.registerUpdateHandler(mHoldDetector);
			this.mSurfaceScrollDetector = new SurfaceScrollDetector(this);
			this.mPinchZoomDetector = new PinchZoomDetector(this);
			this.mScene.setTouchAreaBindingOnActionDownEnabled(true);
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
	
	class TaskSpriteController implements IUpdateHandler, IOnAreaTouchListener,IPinchZoomDetectorListener, IHoldDetectorListener, IScrollDetectorListener{
		private HashMap<Long, TextSprite> mTaskIdToSprite;
		private HashMap<TextSprite, Task> mSpriteToTask;
		private final static float SCALE_FACTOR = 0.1f;
		private static final float SCALE_MAX = 5f;
		private static final float SCALE_DEFAULT = 0.2f;
		private static final float SCALE_MIN = 0.1f;
		private static final int BORDER = 11;
		private static final int TRIGGER_HOLD_MIN_MILISECONDS = 300;
		private boolean isTouched; 
		private ContinuousHoldDetector mHoldDetector;
		private PhysicsHandler mPhysicsHandler;
		private boolean isSelected;
		private PinchZoomDetector mPinchZoomDetector;
		private ScrollDetector mScrollDetector;
		private float mStartScaleX, mStartScaleY;
		private final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0f, 0f);
		
		private Scene mScene;
		private TextSprite mSelectedSprite;
		protected PhysicsWorld mPhysicsWorld;
		//private Body faceBody;
		private float VELOCITY;
		
		public TaskSpriteController(Scene scene) {
			mScene = scene;
			init();
			
		}
		private void init(){
			this.mHoldDetector = new ContinuousHoldDetector(this);
			this.mPinchZoomDetector = new PinchZoomDetector(this);
			this.mScrollDetector = new ScrollDetector(this);
			this.mTaskIdToSprite = new HashMap<Long, TextSprite>();
			this.mSpriteToTask = new HashMap<TextSprite, Task>();
			this.mHoldDetector.setTriggerHoldMinimumMilliseconds(TRIGGER_HOLD_MIN_MILISECONDS);
			mScene.registerUpdateHandler(mHoldDetector);
			
			this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
			
			this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		}

	
		
		private void updateSpriteForTask(Task task){
			if (mTaskIdToSprite.containsKey(task.getID())){
				TextSprite tSprite = mTaskIdToSprite.get(task.getID());
				updateBodyForSprite(tSprite);
				
				// check if description is updated
				if(!tSprite.getText().equals(task.getDescription())){
					removeSpriteForTask(task);
					createSpriteForTask(task);
				}else if(tSprite.getX() != task.getX() || tSprite.getY() != task.getY()){
					tSprite.setX(task.getX());
					tSprite.setY(task.getY());
					updateBodyForSprite(tSprite);
				}else if(this.getUrgencyFromScale(tSprite.getScaleX()) != task.getUrgency()){
					updateBodyForSprite(tSprite);
				}
				
			}
			else{
				createSpriteForTask(task);
			}
			
		}
		
		private void createSpriteForTask(Task task){
		   	 TextSprite tSprite = new TextSprite(task.getDescription(), mFont, mTaskTextureRegion, getVertexBufferObjectManager());
		   	 
	    	 tSprite.setPosition(task.getX(), task.getY());
	    	 tSprite.setScale(this.getScaleFromUrgency(task.getUrgency()));
	       	 mTaskIdToSprite.put(task.getID(),tSprite);
	       	 mSpriteToTask.put(tSprite, task);
	    	 this.mScene.attachChild(tSprite);
	    	 this.mScene.registerTouchArea(tSprite);
	    	 
	    	
	    	 updateBodyForSprite(tSprite);
	    	 
	    	 // 
	    	 
	    	 
	    	 
	    	
		}
		
		private void updateBodyForSprite(TextSprite tSprite){
			PhysicsConnectorManager pcm = Visualtasks.this.mPhysicsWorld.getPhysicsConnectorManager();
			 PhysicsConnector pc = pcm.findPhysicsConnectorByShape(tSprite);
			 
			 Body body = pcm.findBodyByShape(tSprite);
			 if (pc != null) {
				 Visualtasks.this.mPhysicsWorld.unregisterPhysicsConnector(pc);
			 }
			 
			 if (body != null){
				 Visualtasks.this.mPhysicsWorld.destroyBody(body);
			 }
			 
			 //physics
	    	 body = PhysicsFactory.createCircleBody(Visualtasks.this.mPhysicsWorld, tSprite, BodyType.DynamicBody, FIXTURE_DEF, PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
	    	 pc = new PhysicsConnector(tSprite, body, true, true, PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
	    	 Visualtasks.this.mPhysicsWorld.registerPhysicsConnector(pc);
	    	 	    	 
	    	 // save body in sprite to get acces to it
	    	 tSprite.setUserData(body);
	    	 
	    	 VELOCITY = tSprite.getScaleX()*tSprite.getScaleX();
	 		 Log.d("y = ",""+ body.getPosition().y);
	    	 //faceBody = (Body) tSprite.getUserData();
	 		
	 		if(body.getPosition().y > BORDER) {
	 			final Vector2 velocityv = Vector2Pool.obtain(0, -VELOCITY);
	 			body.setLinearVelocity(velocityv);
	 			Vector2Pool.recycle(velocityv);
	 		} else {
	 			final Vector2 velocityv = Vector2Pool.obtain(0, 0);
	 			body.setLinearVelocity(velocityv);
	 			Vector2Pool.recycle(velocityv);
	 		}
			
		}
		
		private void removeSpriteForTask(Task task){
			if (mTaskIdToSprite.containsKey(task.getID())){
				TextSprite tSprite = mTaskIdToSprite.get(task.getID());	
				mTaskIdToSprite.remove(task.getID());
				this.mScene.detachChild(tSprite);
				this.mScene.unregisterTouchArea(tSprite);
			}
		}
		
		private  float getScaleFromUrgency(Float urgency){
			return (SCALE_MAX-urgency) * SCALE_FACTOR;
		}
			
		private  float getUrgencyFromScale(Float scale){
			return SCALE_MAX-(scale/SCALE_FACTOR);
		}
		
		@Override
		public boolean onAreaTouched(TouchEvent pAreaTouchEvent, ITouchArea arg1,float arg2, float arg3) {
			if (arg1 instanceof TextSprite){
				TextSprite ts = (TextSprite) arg1;
				Body body = (Body) ts.getUserData();
				switch(pAreaTouchEvent.getAction()){
				case TouchEvent.ACTION_DOWN:
					mSelectedSprite = ts;
					
					
					
				case TouchEvent.ACTION_OUTSIDE:
					
				
				case TouchEvent.ACTION_MOVE:
					
					break;
				case TouchEvent.ACTION_CANCEL:
				case TouchEvent.ACTION_UP:
					mSelectedSprite = null;
						
				}
				this.mPinchZoomDetector.onTouchEvent(pAreaTouchEvent);
				this.mHoldDetector.onTouchEvent(pAreaTouchEvent);
				this.mScrollDetector.onTouchEvent(pAreaTouchEvent);
				
			}
			
			return true;
		}

		@Override
		public void onScroll(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
			if (mSelectedSprite != null){	
				Body body = (Body) mSelectedSprite.getUserData();
				float x = body.getPosition().x + pDistanceX/  PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT ;
				float y =  body.getPosition().y + pDistanceY/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
				body.setTransform(x, y, 0);
				
//				mSelectedSprite.setX(x);
//				mSelectedSprite.setY(y);
	//			Task task = mSpriteToTask.get(mSelectedSprite);
	//			task.setX(x);
	//			task.setY(y);
	//			mDbHandler.updateTask(task);
			}
		}

		@Override
		public void onScrollFinished(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
			if (mSelectedSprite != null){
				Body body = (Body) mSelectedSprite.getUserData();
				float x = body.getPosition().x + pDistanceX/  PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT ;
				float y =  body.getPosition().y + pDistanceY/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
				body.setTransform(x, y, 0);
				
				Task task = mSpriteToTask.get(mSelectedSprite);
				task.setX(x);
				task.setY(y);
				Visualtasks.this.updateTask(task);
			}
			
			
		}

		@Override
		public void onScrollStarted(ScrollDetector arg0, int arg1, float arg2,
				float arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onHold(HoldDetector arg0, long arg1, int arg2, float arg3,
				float arg4) {
			Visualtasks.this.toastOnUIThread("hold", Toast.LENGTH_SHORT);
	
			
		}

		@Override
		public void onHoldFinished(HoldDetector arg0, long arg1, int arg2,
				float arg3, float arg4) {
			Visualtasks.this.toastOnUIThread("holdfinished", Toast.LENGTH_SHORT);
			
		}

		@Override
		public void onHoldStarted(HoldDetector arg0, int arg1, float arg2,
				float arg3) {
			if (mSelectedSprite != null){
				Task task = mSpriteToTask.get(mSelectedSprite);
				
				this.mHoldDetector.setEnabled(false);
				final Bundle bundle = new Bundle();
				bundle.putLong(Visualtasks.KEY_TASK_ID, task.getID());
				Visualtasks.this.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						Visualtasks.this.removeDialog(Visualtasks.DIALOG_CONTEXT_ID);
						Visualtasks.this.showDialog(Visualtasks.DIALOG_CONTEXT_ID, bundle);
						
					}});
				this.mHoldDetector.setEnabled(true);;
			}
			
		}

		@Override
		public void onPinchZoom(PinchZoomDetector arg0, TouchEvent arg1,
				float pZoomFactor) {
			if (mSelectedSprite != null){
				mSelectedSprite.setScale(pZoomFactor * mStartScaleX,  pZoomFactor * mStartScaleY);
				updateBodyForSprite(mSelectedSprite);
			}
			
		}

		@Override
		public void onPinchZoomFinished(PinchZoomDetector arg0,
				TouchEvent arg1, float arg2) {
			if (mSelectedSprite != null){
				final float scaleX = Math.max(Math.min(mSelectedSprite.getScaleX(), SCALE_MAX * SCALE_FACTOR),SCALE_MIN * SCALE_FACTOR);
				mSelectedSprite.setScale(scaleX);
				updateBodyForSprite(mSelectedSprite);
				Task task = (Task) mSelectedSprite.getUserData();
				task.setUrgency(this.getUrgencyFromScale(scaleX));
				Visualtasks.this.updateTask(task);
			}
		}

		@Override
		public void onPinchZoomStarted(PinchZoomDetector arg0, TouchEvent arg1) {
			if (mSelectedSprite != null){
				this.mStartScaleX = mSelectedSprite.getScaleX();
				this.mStartScaleY = mSelectedSprite.getScaleY();
			}
		}



		@Override
		public void onUpdate(float arg0) {
			// TODO Auto-generated method stub
			
		}



		@Override
		public void reset() {
			// TODO Auto-generated method stub
			
		}
		
		
		
		
		
		
		
	}
	
	
	
	
	

}


