package visualtasks.com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.camera.hud.HUD;
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
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
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
import org.andengine.util.adt.list.SmartList;
import org.andengine.util.call.Callback;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import data.TaskDbHandler;

public class Visualtasks extends SimpleBaseGameActivity  {
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
	private TaskDbHandler mDbHandler;
	private HashMap<Long, TaskSprite> mIdToSprite;
	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private Font mFont;
	private ITextureRegion mParallaxLayerFront;
	private Scene mScene;
	private ITextureRegion mTaskTextureRegion;
	// private Camera mCamera;
	private TouchController mTouchController;
	private TaskSpritesTouchListener mTaskSpriteController;
	private ZoomCamera mZoomCamera;
	private BitmapTextureAtlas mHUDTexture;
	private TiledTextureRegion mAddButtonTextureRegion;
	private TiledTextureRegion mDelButtonTextureRegion;
	protected PhysicsWorld mPhysicsWorld;
	private final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0f, 0f);

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
		mIdToSprite = new HashMap<Long, TaskSprite>();
		super.onCreate(pSavedInstanceState);
	}
	

	@Override
	public synchronized void onResumeGame() {
		Cursor c = mDbHandler.getAllTasks();
		c.moveToFirst();
		while(!c.isAfterLast()){
			 final String description = c.getString(c.getColumnIndexOrThrow(TaskDbHandler.KEY_DESCRIPTION)); // 
			 final float x = c.getFloat(c.getColumnIndexOrThrow(TaskDbHandler.KEY_POSX)); //
			 final float y = c.getFloat(c.getColumnIndexOrThrow(TaskDbHandler.KEY_POSY)); //
			 final int status = c.getInt(c.getColumnIndexOrThrow(TaskDbHandler.KEY_STATUS)); //
			 final float urgency = c.getFloat(c.getColumnIndexOrThrow(TaskDbHandler.KEY_URGENCY)); //
			 final long id = c.getLong(c.getColumnIndexOrThrow(TaskDbHandler.KEY_ID)); //
			 final TaskSprite tSprite = this.addTask(id, description, x, y);
			 if (tSprite != null)
				 tSprite.setUrgency(urgency);
		        c.moveToNext();
		}
		
//		for(Task task : mDbHandler.getAllTasks()){
//			mTaskList.put(task.getID(), task);
//			new TextSpriteUpdateHandler(mScene, this.mPhysicsWorld, task);
			
//		}

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
		this.mAddButtonTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mHUDTexture, this, "addButton.png", 0, 0, 1, 1);
		this.mHUDTexture.load();
		
		this.mHUDTexture = new BitmapTextureAtlas(this.getTextureManager(), 150, 150,TextureOptions.BILINEAR);
		this.mDelButtonTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mHUDTexture, this, "delFish.png", 0, 0, 1, 1);
		this.mHUDTexture.load();
	}
	
	
	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
	
		this.mTouchController = new TouchController(mScene);
		this.mTaskSpriteController = new TaskSpritesTouchListener(mScene);
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
		final Rectangle border = new Rectangle(0, 160, CAMERA_WIDTH, 1, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, border, BodyType.StaticBody, wallFixtureDef);

		border.setVisible(false);
		
		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);
		this.mScene.attachChild(border);
		
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		
		
		//hudButton
		final HUD hud = new HUD();
		
		final TiledSprite addButton = new TiledSprite(this.mAddButtonTextureRegion.getWidth()/2 , CAMERA_HEIGHT - this.mAddButtonTextureRegion.getHeight(), this.mAddButtonTextureRegion, this.getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				/**if(pSceneTouchEvent.isActionDown()) {
						Visualtasks.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								showDialog(DIALOG_NEW_TASK_ID);
							}
						});
				}**/
				if(pSceneTouchEvent.isActionUp()) {
					this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
					final Bundle bundle = new Bundle();
					bundle.putFloat(KEY_TASK_X, pSceneTouchEvent.getX() - 150);
					bundle.putFloat(KEY_TASK_Y, pSceneTouchEvent.getY() - 150);
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							Visualtasks.this.showDialog(DIALOG_NEW_TASK_ID, bundle);
						}});
					
					
					//terugzetten van knop
					this.setPosition(mAddButtonTextureRegion.getWidth()/2 , CAMERA_HEIGHT - mAddButtonTextureRegion.getHeight());
				} else {
					this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
				}
				return true;
			}
		};
		hud.registerTouchArea(addButton);
		hud.attachChild(addButton);
		
		final TiledSprite delButton = new TiledSprite(this.mDelButtonTextureRegion.getWidth()*2, CAMERA_HEIGHT - this.mDelButtonTextureRegion.getHeight(), this.mDelButtonTextureRegion, this.getVertexBufferObjectManager()) {
			
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				if(pSceneTouchEvent.isActionUp()) {
					final float x = pSceneTouchEvent.getX() - this.getWidth() / 2;
					final float y = pSceneTouchEvent.getY() - this.getHeight() / 2;
					
					this.setPosition(x,y );
					
					//check voor collision en verwijderen van bubble als dat het geval is.
					
					TaskSprite spriteToDelete = Visualtasks.this.getTaskSpriteAtPosition(mZoomCamera.getSceneCoordinatesFromCameraSceneCoordinates(
							this.getSceneCenterCoordinates()));
							
					if (spriteToDelete != null){
						Visualtasks.this.deleteTask(spriteToDelete);
					}
					
					//terugzetten van knop
					this.setPosition(mDelButtonTextureRegion.getWidth()*2, CAMERA_HEIGHT - mDelButtonTextureRegion.getHeight());
				} else {
					this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
				}
				return true;
			}
		};
		hud.registerTouchArea(delButton);
		hud.attachChild(delButton);
		
		mZoomCamera.setZoomFactor(1.5f);
		this.mZoomCamera.setHUD(hud);
		
		//end hudButton**/
		
		return this.mScene;
	}
	
	
	private TaskSprite getTaskSpriteAtPosition(float[] position){
		for(TaskSprite tSprite: mIdToSprite.values()){
			if(tSprite.contains(position[0], position[1])){
				return tSprite;
			}
		}
		return null;
	}
	
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
		List<TaskSprite> taskList = new ArrayList<TaskSprite>();
		taskList.addAll(mIdToSprite.values());
		
		for(TaskSprite tSprite : taskList){
			mDbHandler.updateTask(tSprite.getId(), tSprite.getText(), tSprite.getX(), tSprite.getY(), tSprite.getStatus(), tSprite.getUrgency());
			
		}
		
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
				final TaskSprite taskSprite = mIdToSprite.get(taskId);
				return new StringInputDialogBuilder(this,R.string.dialog_task_new_title,0, R.string.dialog_task_new_message, android.R.drawable.ic_dialog_info,
						taskSprite.getText(),
						new Callback<String>() { 							
							@Override
							public void onCallback(String pCallbackValue) {
								
									taskSprite.setText(pCallbackValue);
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
			final TaskSprite taskSprite2 = mIdToSprite.get(taskId2);
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
						Visualtasks.this.deleteTask(taskSprite2);
						Toast.makeText(getApplicationContext(), taskSprite2.getText() + " deleted.", Toast.LENGTH_SHORT).show();
					
				}});
	        
	        Button complete = (Button) dialog.findViewById(R.id.complete);
	        complete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
						Visualtasks.this.dismissDialog(DIALOG_CONTEXT_ID);
						Visualtasks.this.deleteTask(taskSprite2);
						Toast.makeText(getApplicationContext(), taskSprite2.getText() + " completed.", Toast.LENGTH_SHORT).show();

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


	
	
	

	
	protected void addTask(String description, float pX, float pY) {
		Long id = mDbHandler.addTask(description, pX, pY, TaskSprite.STATUS_ACTIVE, 0f);
		this.addTask(id, description, pX, pY);
		
	}



	protected void onSceneHold(float pHoldX, float pHoldY){
		/**final Bundle bundle = new Bundle();
		bundle.putFloat(KEY_TASK_X, pHoldX);
		bundle.putFloat(KEY_TASK_Y, pHoldY);
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				Visualtasks.this.showDialog(DIALOG_NEW_TASK_ID, bundle);
			}});**/
	}
	
	

	private TaskSprite addTask(long id, String description, float pX, float pY){
		if(!mIdToSprite.containsKey(id)){
			TaskSprite taskSprite = new TaskSprite(id,  mScene, FIXTURE_DEF, mPhysicsWorld, mFont, mTaskTextureRegion, getVertexBufferObjectManager());
			
			taskSprite.setText(description);
			taskSprite.setX(pX);
			taskSprite.setY(pY);
			mIdToSprite.put(id, taskSprite);
			
			mScene.attachChild(taskSprite);
			mScene.registerTouchArea(taskSprite);
			return taskSprite;
		}
		return null;
//		mDbHandler.addTask(task);
//		new TextSpriteUpdateHandler(mScene, this.mPhysicsWorld, task);
		
	}
	
	private void deleteTask(final TaskSprite task){
		task.setDeleted();
		long id = task.getId();
		if(mIdToSprite.containsKey(id)){
			mIdToSprite.remove(id);
			mDbHandler.deleteTask(id);
			runOnUpdateThread(new Runnable() {
				
				@Override
				public void run() {
					mScene.unregisterTouchArea(task);
					mScene.detachChild(task);
					
				}
			});
			
		}
	}
	
	
			
		
	class TouchController implements IOnSceneTouchListener,IScrollDetectorListener, IPinchZoomDetectorListener, IHoldDetectorListener{

		private int lastTouchId;
		private ContinuousHoldDetector mHoldDetector;
		private float mPinchZoomStartedCameraZoomFactor;
		private Scene mScene;
		private PinchZoomDetector mPinchZoomDetector;
		private SurfaceScrollDetector mSurfaceScrollDetector;
		private float zoomfac;
		
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
			//mZoomCamera.setZoomFactor(mPinchZoomStartedCameraZoomFactor * pZoomFactor);
			zoomfac = mPinchZoomStartedCameraZoomFactor * pZoomFactor ;
			if(1.0 < zoomfac && zoomfac < 2.0){
				mZoomCamera.setZoomFactor(zoomfac);
			}
		}

		@Override
		public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
			//mZoomCamera.setZoomFactor(mPinchZoomStartedCameraZoomFactor * pZoomFactor);
			if(1.0 < zoomfac && zoomfac < 2.0){
				mZoomCamera.setZoomFactor(zoomfac);
			}
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
	
	class TaskSpritesTouchListener implements IOnAreaTouchListener,IPinchZoomDetectorListener, IHoldDetectorListener, IScrollDetectorListener{
		

		private static final int TRIGGER_HOLD_MIN_MILISECONDS = 300;
		 
		private ContinuousHoldDetector mHoldDetector;
		
		
		private PinchZoomDetector mPinchZoomDetector;
		private ScrollDetector mScrollDetector;
		private float mStartScaleX, mStartScaleY;
	
		
		private Scene mScene;
		private TaskSprite mSelectedSprite;
				
		
		public TaskSpritesTouchListener(Scene scene) {
			mScene = scene;
			init();
			
		}
		
		
		
		private void init(){
			this.mHoldDetector = new ContinuousHoldDetector(this);
			this.mPinchZoomDetector = new PinchZoomDetector(this);
			this.mScrollDetector = new ScrollDetector(this);
			this.mHoldDetector.setTriggerHoldMinimumMilliseconds(TRIGGER_HOLD_MIN_MILISECONDS);
			mScene.registerUpdateHandler(mHoldDetector);
			
			
			
			
		}
		
		
		
		
		
		@Override
		public boolean onAreaTouched(TouchEvent pAreaTouchEvent, ITouchArea arg1,float arg2, float arg3) {
			if (arg1 instanceof TaskSprite){
				TaskSprite ts = (TaskSprite) arg1;
				
				switch(pAreaTouchEvent.getAction()){
				case TouchEvent.ACTION_DOWN:
					mSelectedSprite = ts;
					mSelectedSprite.setSelected(true);
				case TouchEvent.ACTION_OUTSIDE:
					
				
				case TouchEvent.ACTION_MOVE:
					
					break;
				case TouchEvent.ACTION_CANCEL:
				case TouchEvent.ACTION_UP:
					if (mSelectedSprite != null){
						mSelectedSprite.setSelected(false);
						mSelectedSprite = null;
						
					}
						
				}
				this.mPinchZoomDetector.onTouchEvent(pAreaTouchEvent);
				this.mHoldDetector.onTouchEvent(pAreaTouchEvent);
				this.mScrollDetector.onTouchEvent(pAreaTouchEvent);
				
			}
			
			return true;
		}

		@Override
		public void onScroll(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
			final TaskSprite tSprite = mSelectedSprite;
			if (tSprite != null){	
				runOnUpdateThread(new Runnable() {
					
					@Override
					public void run() {
						tSprite.setX(tSprite.getX() + pDistanceX);
						tSprite.setY(tSprite.getY() + pDistanceY);
					}
				});
			}
		}

		@Override
		public void onScrollFinished(ScrollDetector pScrollDetector,  final int pPointerID, final float pDistanceX, final float pDistanceY) {
			final TaskSprite tSprite = mSelectedSprite;
			if (tSprite != null){
				runOnUpdateThread(new Runnable() {
					
					@Override
					public void run() {
						tSprite.setX(tSprite.getX() + pDistanceX);
						tSprite.setY(tSprite.getY() + pDistanceY);
						
					}
				});
			
				
				
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
		public void onHoldStarted(HoldDetector arg0, int arg1, float arg2, float arg3) {
			if (mSelectedSprite != null){
			
				
				this.mHoldDetector.setEnabled(false);
				final Bundle bundle = new Bundle();
				bundle.putLong(Visualtasks.KEY_TASK_ID, mSelectedSprite.getId());
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
				
				mSelectedSprite.setScale(pZoomFactor * mStartScaleX);
				
			}
			
		}

		@Override
		public void onPinchZoomFinished(PinchZoomDetector arg0,
				TouchEvent arg1, float pZoomFactor) {
			if (mSelectedSprite != null){
				mSelectedSprite.setScale(pZoomFactor * mStartScaleX);
				
			}
		}

		@Override
		public void onPinchZoomStarted(PinchZoomDetector arg0, TouchEvent arg1) {
			if (mSelectedSprite != null){
				this.mStartScaleX = mSelectedSprite.getScaleX();
				this.mStartScaleY = mSelectedSprite.getScaleY();
			}
		}
		
	}

	

	

}


