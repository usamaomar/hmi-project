package visualtasks.com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
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
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.Texture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.ResourceBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.ui.dialog.StringInputDialogBuilder;
import org.andengine.util.call.Callback;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import brr.AndroidStrategy.MapControl.FlingGestureDetector;
import brr.AndroidStrategy.MapControl.FlingGestureDetector.IFlingGestureListener;
import brr.AndroidStrategy.MapControl.SmoothScrollCamera;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import data.TaskDbHandler;

public class Visualtasks extends SimpleBaseGameActivity  implements OnDismissListener{
	// ===========================================================
	// Constants
	// ===========================================================

	private static final float CAMERA_ZOOM_FACTOR = 1f;
	private static final int BACKGROUND_HEIGHT = 2400;
	public static final float BORDER = 1000f;
	public static final float START_HEIGHT = 800;
	
	public static final int DIALOG_NEW_TASK_ID = 0;
	public static final int DIALOG_EDIT_TASK_ID = DIALOG_NEW_TASK_ID + 1;
	public static final int DIALOG_CONTEXT_ID = DIALOG_EDIT_TASK_ID + 1;
	public static final String KEY_TASK_LIST = "key_task_list"; 
	public static final String KEY_TASK_ID = "key_task_id";
	public static final String KEY_TASK_X = "tX";
	public static final String KEY_TASK_Y = "tY";
	
	private int status = 0;
	public static final int STATUS_DEFAULT = 0;
	public static final int STATUS_TOUCHING_TASK = 1;
	public static final int STATUS_DIALOG_SHOWED = 2;
	public static final int STATUS_TOUCHING_SCENE = 3;
	private TaskDbHandler mDbHandler;
	private HashMap<Long, TaskSprite> mIdToSprite;
	private Font mFont;
	private ITextureRegion mBackgroundTextureRegion;
	private Scene mScene;
	private TiledTextureRegion mTaskTextureRegion;
	// private Camera mCamera;
	private TouchController mTouchController;
	private TaskSpritesTouchListener mTaskSpriteController;
	private SmoothScrollCamera mCamera;
	private ITextureRegion mAddButtonTextureRegion;
	private ITextureRegion mDelButtonTextureRegion;
	private Sprite mAddButton;
	private Sprite mDelButton;
	protected PhysicsWorld mPhysicsWorld;
	private final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0f, 0f);
	
	private BitmapTextureAtlas mBitmapTextureAtlas;
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
		
		
		super.onResumeGame();
	}
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		final DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		this.mCamera = new SmoothScrollCamera(START_HEIGHT, metrics.widthPixels , metrics.heightPixels, 200f, 4f);
//		this.mSmoothCamera = new ZoomCamera(0, START_HEIGHT, metrics.widthPixels , metrics.heightPixels);
		this.mCamera.setBounds(0, 0, metrics.widthPixels, BACKGROUND_HEIGHT);
		this.mCamera.setBoundsEnabled(true);
		this.mCamera.setZoomFactor(CAMERA_ZOOM_FACTOR);
		
		final EngineOptions engineOptions = new EngineOptions(true,ScreenOrientation.LANDSCAPE_FIXED, 
				new FillResolutionPolicy(), this.mCamera);
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);
		
		return engineOptions;
		
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		this.setStatus(STATUS_DEFAULT);
		
	}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		this.setStatus(STATUS_DIALOG_SHOWED);
		dialog.setOnDismissListener(this);
		super.onPrepareDialog(id, dialog);
	}
	
	
	
	
	
	
	
	
	@Override
	protected void onCreateResources() {
		//load font 
		final ITexture strokeFontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		this.mFont = FontFactory.createStroke(this.getFontManager(), strokeFontTexture,  Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 56, true, Color.WHITE,2, Color.BLACK);
		this.mFont.load();
		
		
		try {
			BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
			// create textures
			final Texture mBackgroundTexture = new ResourceBitmapTexture(this.getTextureManager(), this.getResources(), R.drawable.background);
			final Texture mTaskTexture = new ResourceBitmapTexture(this.getTextureManager(), this.getResources(), R.drawable.bubble_tiled);
			final Texture mAddTexture = new ResourceBitmapTexture(this.getTextureManager(), this.getResources(), R.drawable.add);
			final Texture mDeleteTexture = new ResourceBitmapTexture(this.getTextureManager(), this.getResources(), R.drawable.delete);
			
			// create regions
			this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1500, 300, TextureOptions.BILINEAR);
			this.mBackgroundTextureRegion = TextureRegionFactory.extractFromTexture(mBackgroundTexture);
			this.mTaskTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "bubble_tiled.png", 0, 0, 5, 1);
			this.mAddButtonTextureRegion = TextureRegionFactory.extractFromTexture(mAddTexture);
			this.mDelButtonTextureRegion = TextureRegionFactory.extractFromTexture(mDeleteTexture);
			//load textures
			mBackgroundTexture.load();
			mTaskTexture.load();
			mAddTexture.load();
			mDeleteTexture.load();
			mBitmapTextureAtlas.load();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}

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
		
		this.mScene.registerUpdateHandler(mCamera);
		
		//bg stuff
//		final ParallaxBackground parallaxBackground = new ParallaxBackground(0, 0, 0);
//		parallaxBackground.attachParallaxEntity(new ParallaxEntity(0, new Sprite(0, 0, this.mBackgroundTextureRegion, this.getVertexBufferObjectManager())));
//		this.mScene.setBackground(parallaxBackground);
		Sprite backgroundSprite = new Sprite(0,0, this.mBackgroundTextureRegion, this.getVertexBufferObjectManager());
		backgroundSprite.setWidth(this.getResources().getDisplayMetrics().widthPixels);
		backgroundSprite.setHeight(BACKGROUND_HEIGHT);
		this.mScene.attachChild(backgroundSprite);
		
		//end bg stuff
		
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		/**
		 * Gravity wordt hier gezet, moet op 0 gezet worden wanneer er een bubbel collide met border body (body's zweven dan nog wel even verder door impuls?)
		 */
		mPhysicsWorld.setGravity(new Vector2(0, -0.1f));
		
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, BACKGROUND_HEIGHT - 2, mCamera.getWidth(), 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, mCamera.getWidth(), 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, BACKGROUND_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(mCamera.getWidth() - 2, 0, 2, BACKGROUND_HEIGHT, vertexBufferObjectManager);
		final Rectangle border = new Rectangle(0, BORDER, mCamera.getWidth(), 1, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0f, 0f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, border, BodyType.StaticBody, wallFixtureDef);

		border.setVisible(false);
		
//		this.mScene.attachChild(ground);
//		this.mScene.attachChild(roof);
//		this.mScene.attachChild(left);
//		this.mScene.attachChild(right);
//		this.mScene.attachChild(border);
		
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		
		
		//hudButton
		final HUD hud = new HUD();//{
//			on
//			
//			@Override
//			protected void onManagedUpdate(float pSecondsElapsed) {
//				if(mHudEnabled && getTouchAreas().isEmpty()){
//					for(IEntity child: this.mChildren){
//						this.registerTouchArea((ITouchArea)child);
//					}
//					
//				}
//				if(!mHudEnabled && !getTouchAreas().isEmpty()){
//					this.clearTouchAreas();
//				}
//				super.onManagedUpdate(pSecondsElapsed);
//			}
//		};
		
		mAddButton = new Sprite(0 , mCamera.getHeight() - this.mAddButtonTextureRegion.getHeight(), this.mAddButtonTextureRegion, this.getVertexBufferObjectManager()) {
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
				if(pSceneTouchEvent.isActionUp() || pSceneTouchEvent.isActionCancel()|| pSceneTouchEvent.isActionOutside()) {
					this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
					final float[] coord = mCamera.getSceneCoordinatesFromCameraSceneCoordinates(this.getSceneCenterCoordinates());
					final Bundle bundle = new Bundle();
					
					bundle.putFloat(KEY_TASK_X, coord[0]);
					bundle.putFloat(KEY_TASK_Y, coord[1]);
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							Visualtasks.this.showDialog(DIALOG_NEW_TASK_ID, bundle);
						}});
					
					
					//terugzetten van knop
					this.setPosition(0 , mCamera.getHeight() - mAddButtonTextureRegion.getHeight());
				} else {
					this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
				}
				return true;
			}
		};
		hud.registerTouchArea(mAddButton);
		hud.attachChild(mAddButton);
		
		mDelButton = new Sprite(0, mCamera.getHeight() - this.mDelButtonTextureRegion.getHeight(), this.mDelButtonTextureRegion, this.getVertexBufferObjectManager()) {
			
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
								
				if(pSceneTouchEvent.isActionUp() || pSceneTouchEvent.isActionCancel()|| pSceneTouchEvent.isActionOutside()) {
					final float x = pSceneTouchEvent.getX() - this.getWidth() / 2;
					final float y = pSceneTouchEvent.getY() - this.getHeight() / 2;
					
					this.setPosition(x,y );
					
					//check voor collision en verwijderen van bubble als dat het geval is.
					
					TaskSprite spriteToDelete = Visualtasks.this.getTaskSpriteAtPosition(mCamera.getSceneCoordinatesFromCameraSceneCoordinates(
							this.getSceneCenterCoordinates()));
							
					if (spriteToDelete != null){
						Visualtasks.this.deleteTask(spriteToDelete);
					}
					
					//terugzetten van knop
					this.setPosition(0, mCamera.getHeight() - mDelButtonTextureRegion.getHeight());
				} else {
					this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
				}
				return true;
			}
		};
		hud.registerTouchArea(mDelButton);
		hud.attachChild(mDelButton);
		
//		mZoomCamera.setZoomFactor(1.5f);
		
		this.mCamera.setHUD(hud);
		hud.setOnSceneTouchListenerBindingOnActionDownEnabled(true);
		
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
	
	protected void setStatus(int status) {
		this.status = status;
		switch(status){
		case STATUS_DEFAULT:
			if(mCamera != null && mCamera.hasHUD()){
				mCamera.setAutoScrollBackEnabled(true);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mCamera.getHUD().detachChildren();
						mCamera.getHUD().clearTouchAreas();
						mCamera.getHUD().attachChild(mAddButton);
						mCamera.getHUD().registerTouchArea(mAddButton);
					}
				});
			}
			
			break;
		case STATUS_TOUCHING_TASK:
			if(mCamera != null && mCamera.hasHUD()){
				mCamera.setAutoScrollBackEnabled(false);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mCamera.getHUD().detachChildren();
						mCamera.getHUD().clearTouchAreas();
						mCamera.getHUD().attachChild(mDelButton);
						mCamera.getHUD().registerTouchArea(mDelButton);
					}
				});
			}
			break;
		case STATUS_DIALOG_SHOWED:
			if(mCamera != null){
				mCamera.setAutoScrollBackEnabled(false);
			}
		}
		
		
	}
	
	protected int getStatus() {
		return status;
	}
	
	@Override
	protected Dialog onCreateDialog(int id, final Bundle bundle) {
		switch(id){
		case DIALOG_NEW_TASK_ID:
			final float pX = bundle != null && bundle.containsKey(KEY_TASK_X) ? bundle.getFloat(KEY_TASK_X) : (this.mCamera.getXMax()-this.mCamera.getXMin())/2f;
			final float pY = bundle != null &&bundle.containsKey(KEY_TASK_Y) ? bundle.getFloat(KEY_TASK_Y) : (this.mCamera.getYMax()-this.mCamera.getYMin())/2f;
			
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
							Visualtasks.this.removeDialog(DIALOG_NEW_TASK_ID);
							
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
								Visualtasks.this.removeDialog(DIALOG_EDIT_TASK_ID);
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
	        
//	        Button complete = (Button) dialog.findViewById(R.id.complete);
//	        complete.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View arg0) {
//						Visualtasks.this.dismissDialog(DIALOG_CONTEXT_ID);
//						Visualtasks.this.deleteTask(taskSprite2);
//						Toast.makeText(getApplicationContext(), taskSprite2.getText() + " completed.", Toast.LENGTH_SHORT).show();
//
//				}});
	        
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
		return mIdToSprite.get(id);
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
					task.animate(150); //<====== (hier onder even gecomment zodat er tijd is voor animatie, maar app stopt na gedeelte van animatie)
					//mScene.unregisterTouchArea(task);
					//mScene.detachChild(task);
					
				}
			});
			
		}
	}
	
	
			
		
	class TouchController implements IOnSceneTouchListener,IScrollDetectorListener, IPinchZoomDetectorListener, IHoldDetectorListener, IFlingGestureListener{

		private int lastTouchId;
		private ContinuousHoldDetector mHoldDetector;
		private float mPinchZoomStartedCameraZoomFactor;
		private Scene mScene;
//		private PinchZoomDetector mPinchZoomDetector;
//		private SurfaceScrollDetector mSurfaceScrollDetector;
		private  FlingGestureDetector mGestureDetector;
		private float zoomfac;
		
		
		public TouchController(Scene pScene) {
			this.mScene = pScene;
			
			// register mapscroller
			
			
			Visualtasks.this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					final FlingGestureDetector det = new FlingGestureDetector(Visualtasks.this);
					det.setEnabled(true);
					det.setOnFlingGestureListener(TouchController.this);
					 TouchController.this.mGestureDetector = det;
					 det.setEnabled(true);
					 
				}
			});
		
			
			
//			this.mHoldDetector = new ContinuousHoldDetector(this);
//			this.mHoldDetector.setTriggerHoldMinimumMilliseconds(TRIGGER_HOLD_MIN_MILISECONDS);
//			this.mScene.registerUpdateHandler(mHoldDetector);
//			this.mSurfaceScrollDetector = new SurfaceScrollDetector(this);
//			this.mPinchZoomDetector = new PinchZoomDetector(this);
			this.mScene.setTouchAreaBindingOnActionDownEnabled(true);
			this.mScene.setTouchAreaBindingOnActionMoveEnabled(true);
			
			
//			this.mGestureDetector.setEnabled(true);
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
				mCamera.setZoomFactor(zoomfac);
			}
		}

		@Override
		public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
			//mZoomCamera.setZoomFactor(mPinchZoomStartedCameraZoomFactor * pZoomFactor);
			if(1.0 < zoomfac && zoomfac < 2.0){
				mCamera.setZoomFactor(zoomfac);
			}
		}

		@Override
		public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
			mPinchZoomStartedCameraZoomFactor = mCamera.getZoomFactor();
		}
		
		@Override
		public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
//			if (this.mPinchZoomDetector != null) {
//				this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);
//
//				if (this.mPinchZoomDetector.isZooming()) {
//					this.mSurfaceScrollDetector.setEnabled(false);
//					this.mHoldDetector.setEnabled(false);
//
//				} else {
//					if (pSceneTouchEvent.isActionDown()) {
//
//						this.mSurfaceScrollDetector.setEnabled(true);
//						this.mHoldDetector.setEnabled(true);
//					}
//					this.mHoldDetector.onTouchEvent(pSceneTouchEvent);
//					if(!mHoldDetector.isHolding())
//						this.mSurfaceScrollDetector.onTouchEvent(pSceneTouchEvent);
//
//				}
//			} else {
//				this.mSurfaceScrollDetector.onTouchEvent(pSceneTouchEvent);
			if(this.mGestureDetector != null )
				this.mGestureDetector.onSceneTouchEvent(pScene, pSceneTouchEvent);
			mCamera.onSceneTouchEvent(mScene, pSceneTouchEvent);
//			}
			switch(pSceneTouchEvent.getAction()){
			case TouchEvent.ACTION_DOWN:
			case TouchEvent.ACTION_MOVE:
				
				Visualtasks.this.setStatus(Visualtasks.STATUS_TOUCHING_SCENE);
				break;
			case TouchEvent.ACTION_UP:
			case TouchEvent.ACTION_CANCEL:
				
				Visualtasks.this.setStatus(Visualtasks.STATUS_DEFAULT);
				break;
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
				final float zoomFactor = Visualtasks.this.mCamera.getZoomFactor();
//				Visualtasks.this.mSmoothCamera.setCenterDirect(mSmoothCamera.getCenterX()-pDistanceX , mSmoothCamera.getCenterY()-pDistanceY );
				Visualtasks.this.mCamera.setCenter(mCamera.getCenterX()-pDistanceX , mCamera.getCenterY()-pDistanceY );
			}
		}

		@Override
		public void onScrollFinished(final ScrollDetector pScrollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
			if(pPointerID != lastTouchId){
				lastTouchId = pPointerID;
				pScrollDetector.reset();
			}
			else {
				final float zoomFactor = Visualtasks.this.mCamera.getZoomFactor();
				Visualtasks.this.mCamera.setCenter(mCamera.getCenterX()-pDistanceX , mCamera.getCenterY()-pDistanceY );
			}
			
		}

		@Override
		public void onScrollStarted(final ScrollDetector pScrollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
			lastTouchId = pPointerID;
			
		}

		@Override
		public void onFling(TouchEvent pSceneTouchEvent, float velocityX,
				float velocityY) {
//			this.mMapScroller.setSpeedX(velocityX * .8f);
			mCamera.setSpeedY(velocityY * .8f);
		}
		
	}
	
	class TaskSpritesTouchListener implements IOnAreaTouchListener,IPinchZoomDetectorListener, IHoldDetectorListener, IScrollDetectorListener, IFlingGestureListener{
		

		private static final int TRIGGER_HOLD_MIN_MILISECONDS = 300;
		 
		private ContinuousHoldDetector mHoldDetector;
		
		
		private FlingGestureDetector mFlingGestureDetector;
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
			Visualtasks.this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					final FlingGestureDetector det = new FlingGestureDetector(Visualtasks.this);
					det.setEnabled(true);
					det.setOnFlingGestureListener(TaskSpritesTouchListener.this);
					TaskSpritesTouchListener.this.mFlingGestureDetector = det;
					 det.setEnabled(true);
					 
				}
			});
			
			
			
			
		}
		
		
		
		
		
		@Override
		public boolean onAreaTouched(TouchEvent pAreaTouchEvent, ITouchArea arg1,float arg2, float arg3) {
			if (arg1 instanceof TaskSprite){
				TaskSprite ts = (TaskSprite) arg1;
				
				switch(pAreaTouchEvent.getAction()){
				case TouchEvent.ACTION_DOWN:
					mSelectedSprite = ts;
					Visualtasks.this.setStatus(STATUS_TOUCHING_TASK);
					
				case TouchEvent.ACTION_OUTSIDE:
					
					
				case TouchEvent.ACTION_MOVE:
					
					break;
				case TouchEvent.ACTION_CANCEL:
				case TouchEvent.ACTION_UP:
					if (mSelectedSprite != null){
//						mSelectedSprite = null;
						Visualtasks.this.setStatus(STATUS_DEFAULT);
					
					}
					
						
				}
				
				if(mSelectedSprite != null){
					mSelectedSprite.onAreaTouched(pAreaTouchEvent, mSelectedSprite, 0, 0);
					Visualtasks.this.toastOnUIThread("y"+ mSelectedSprite.getBody().getPosition().y, Toast.LENGTH_SHORT);
				}
				this.mPinchZoomDetector.onTouchEvent(pAreaTouchEvent);
				this.mHoldDetector.onTouchEvent(pAreaTouchEvent);
//				this.mScrollDetector.onTouchEvent(pAreaTouchEvent);
				this.mFlingGestureDetector.onTouchEvent(pAreaTouchEvent);
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
//			Visualtasks.this.toastOnUIThread("hold", Toast.LENGTH_SHORT);
	
			
		}

		@Override
		public void onHoldFinished(HoldDetector arg0, long arg1, int arg2,
				float arg3, float arg4) {
//			Visualtasks.this.toastOnUIThread("holdfinished", Toast.LENGTH_SHORT);
			
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
						Visualtasks.this.removeDialog(Visualtasks.DIALOG_EDIT_TASK_ID);
						Visualtasks.this.showDialog(Visualtasks.DIALOG_EDIT_TASK_ID, bundle);
						
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



		@Override
		public void onFling(TouchEvent pSceneTouchEvent, float velocityX,
				float velocityY) {
//			Visualtasks.this.toastOnUIThread("fling", Toast.LENGTH_SHORT);
			if (mSelectedSprite != null){
				

				mSelectedSprite.setState(TaskSprite.STATE_SCROLLING);
				mSelectedSprite.setSpeedY(velocityY);
				Visualtasks.this.toastOnUIThread("fling"+ velocityY, Toast.LENGTH_SHORT);
				//mSelectedSprite.getBody().setLinearVelocity(new Vector2(velocityX*0.005f, velocityY*0.005f));
				

			}
		}
		
	}

	

	

	

	

}


