package visualtasks.com;

import java.util.ArrayList;

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
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
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
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.ui.dialog.StringInputDialogBuilder;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.call.Callback;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class Visualtasks extends SimpleBaseGameActivity implements IOnSceneTouchListener, IScrollDetectorListener, IPinchZoomDetectorListener, IHoldDetectorListener{
	// ===========================================================
	// Constants
	// ===========================================================

	private static final float CAMERA_HEIGHT = 720;
	private static final float CAMERA_WIDTH = 1080;
	private static final int TRIGGER_HOLD_MIN_MILISECONDS = 500;

	// ===========================================================
	// Fields
	// ===========================================================

	

	private BitmapTextureAtlas mBackgroundTextureAtlas;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private Font mFont;
	private BitmapTextureAtlas mFontTexture;
	private HoldDetector mHoldDetector;
	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;
	private Scene mScene;
	// private Camera mCamera;
	private SurfaceScrollDetector mScrollDetector;
	private ArrayList<TaskSprite> mTasksList = new ArrayList<TaskSprite>();
	private ITextureRegion mParallaxLayerFront;
	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;

	// ===========================================================
	// Constructors
	// ===========================================================

	private ITextureRegion mTaskTextureRegion;
	private ZoomCamera mZoomCamera;
	private ITextureRegion mBgTexture;
    private BitmapTextureAtlas mBackgroundTexture;
    private ITextureRegion mBackgroundTextureRegion;

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	public Visualtasks() {
		mTasksList = new ArrayList<TaskSprite>();
	}

	@Override
	public Engine onCreateEngine(EngineOptions pEngineOptions) {
		// TODO Auto-generated method stub
		
		return super.onCreateEngine(pEngineOptions);
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
		this.mTaskTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "bubble.png",0, 0);
		this.mBitmapTextureAtlas.load();
		
		//load font 
		this.mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 56, true, Color.WHITE);
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
		this.mScene.setOnAreaTouchTraversalFrontToBack();
		
		//bg stuff
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront, vertexBufferObjectManager)));
		this.mScene.attachChild(new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront, vertexBufferObjectManager));
		//end bg stuff

		this.mScene.setTouchAreaBindingOnActionDownEnabled(true);
		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);
		this.mHoldDetector = new HoldDetector(this);
		this.mHoldDetector.setTriggerHoldMinimumMilliseconds(TRIGGER_HOLD_MIN_MILISECONDS);
		//this.mScene.registerUpdateHandler(mHoldDetector);

		this.mScene.setOnSceneTouchListener(this);
//


		return this.mScene;
	}

	


	@Override
	public void onHold(HoldDetector pHoldDetector, long pHoldTimeMilliseconds,
			int pPointerID, float pHoldX, float pHoldY) {
		
		
	}

	@Override
	public void onHoldFinished(HoldDetector pHoldDetector,
			long pHoldTimeMilliseconds, int pPointerID, float pHoldX,
			float pHoldY) {
		pHoldDetector.setEnabled(false);
		showPopUp(pHoldX, pHoldY);
		pHoldDetector.setEnabled(true);
	}

	@Override
	public void onHoldStarted(HoldDetector pHoldDetector, int pPointerID,
			float pHoldX, float pHoldY) {
		
		
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.text:
			showPopUp();
			// spawnTask();
			break;
		}
		return true;
	}

	

	@Override
	public boolean onSceneTouchEvent(final Scene pScene,
			final TouchEvent pSceneTouchEvent) {
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
				this.mScrollDetector.onTouchEvent(pSceneTouchEvent);

			}
		} else {
			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		}

		return true;
	}

	@Override
	public void onScrollStarted(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = this.mZoomCamera.getZoomFactor();
		this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}

	@Override
	public void onScroll(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = this.mZoomCamera.getZoomFactor();
		this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}
	
	@Override
	public void onScrollFinished(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = this.mZoomCamera.getZoomFactor();
		this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}

	@Override
	public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
		this.mPinchZoomStartedCameraZoomFactor = this.mZoomCamera.getZoomFactor();
	}

	@Override
	public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		this.mZoomCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
	}

	@Override
	public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		this.mZoomCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
	}

	private void showPopUp() {
		final float centerX = (CAMERA_WIDTH - this.mTaskTextureRegion.getWidth()) / 2f;
		final float centerY = (CAMERA_HEIGHT - this.mTaskTextureRegion.getHeight()) / 2f;
		this.showPopUp(centerX, centerY);
	}

	private void showPopUp(final float pX, final float pY) {
		final Context context = this;
		this.runOnUiThread(new Runnable(){
			
			@Override
			public void run() {
				Dialog dialog = new StringInputDialogBuilder(context,R.string.dialog_task_new_title,0,
					    R.string.dialog_task_new_message, android.R.drawable.ic_dialog_info,
					    new Callback<String>() {
												
							@Override
							public void onCallback(String pCallbackValue) {
								spawnTask(pCallbackValue, pX, pY);
								
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
	
	
	// ===========================================================
	// Methods
	// ===========================================================
	public void spawnTask(String name, float pX, float pY) {

		final TaskSprite sprite = new TaskSprite(name,0, 0, mFont,
				mTaskTextureRegion, this.getVertexBufferObjectManager());
		sprite.setPosition(pX - sprite.getWidth() / 2f, pY - sprite.getHeight()
				/ 2f);

		//sprite.setText(name);

		mTasksList.add(0, sprite);

		this.mScene.attachChild(sprite);
		this.mScene.registerTouchArea(sprite);
	}

	class TaskSprite extends Sprite {
		private final static int INVALID_TOUCHING_ID = -1;

	
		private final static float START_SCALE = 0.5f;
		private float dX, dY, dX2, dY2;
		private boolean isZooming, isTouched;

		private final Font mFont;
		private float mScaleX, mScaleY;
		private Text mText;
		private Integer touchingID = INVALID_TOUCHING_ID;
		private Integer touchingID2 = INVALID_TOUCHING_ID;

		public TaskSprite(String text, float pX, float pY, Font pFont,ITextureRegion pTextureRegion, VertexBufferObjectManager vBOM) {
			super(pX,pY,pTextureRegion, vBOM);
			this.mFont = pFont;
			
			this.mText = new Text(0, 0, this.mFont, text,	new TextOptions(AutoWrap.WORDS, this.getWidth(), 4, HorizontalAlign.CENTER),vBOM);
			
			mText.setPosition(this.getWidth() / 2f - mText.getWidth() / 2f,	this.getHeight() / 2f - mText.getHeight() / 2f);
			this.attachChild(mText);
			this.setScale(START_SCALE);
			

		}

		private void bringToTop() {
			synchronized (mTasksList) {
			    mScene.detachChild(this);
				mScene.unregisterTouchArea(this);
				mScene.attachChild(this);
				mScene.registerTouchArea(this);

			}
		}

	

		@Override
		public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
				float pTouchAreaLocalX, float pTouchAreaLocalY) {

			switch (pSceneTouchEvent.getAction()) {
			case TouchEvent.ACTION_DOWN:
				synchronized (touchingID) {
					if (!isTouched) {
						dX = pSceneTouchEvent.getX() - this.getX();
						dY = pSceneTouchEvent.getY() - this.getY();
						touchingID = pSceneTouchEvent.getPointerID();
						isTouched = true;
						this.bringToTop();// sort all tasks again
						this.mScaleX = this.getScaleX();
						this.mScaleY = this.getScaleY();
						this.setScaleX(1.1f * mScaleX);
						this.setScaleY(1.1f * mScaleY);

					} else if (!isZooming) {
						dX2 = pSceneTouchEvent.getX() - this.getX();
						dY2 = pSceneTouchEvent.getY() - this.getY();
						touchingID2 = pSceneTouchEvent.getPointerID();
						isZooming = true;
					}
				}
				;
				break;
			case TouchEvent.ACTION_MOVE:
				synchronized (touchingID) {
					// Test if zooming
					if (touchingID != INVALID_TOUCHING_ID
							&& touchingID2 != INVALID_TOUCHING_ID) {
						float diffStartX = Math.abs(dX - dX2);
						float diffStartY = Math.abs(dY - dY2);
						float startDist = (float) Math.sqrt(diffStartX
								* diffStartX + diffStartY * diffStartY); // calculate
																			// distance
						float currDist = (float) Math.sqrt(diffStartX
								* diffStartX + diffStartY * diffStartY); // calculate
																			// distance
					} else if (touchingID == pSceneTouchEvent.getPointerID()) {
						setPosition(pSceneTouchEvent.getX() - dX,
								pSceneTouchEvent.getY() - dY);
					}
				}
				;
				break;
			case TouchEvent.ACTION_UP:
			case TouchEvent.ACTION_CANCEL:
				synchronized (touchingID) {
					if (isZooming
							&& pSceneTouchEvent.getPointerID() == touchingID) {
						isZooming = false;
						// set the second pointer as first
						touchingID = touchingID2;
						touchingID2 = INVALID_TOUCHING_ID;
						dX = dX2;
						dY = dY2;
					} else if (isZooming
							&& pSceneTouchEvent.getPointerID() == touchingID2) {
						isZooming = false;
						touchingID2 = INVALID_TOUCHING_ID;

					} else if (isTouched
							&& pSceneTouchEvent.getPointerID() == touchingID) {
						touchingID = INVALID_TOUCHING_ID;
						isTouched = false;
						this.setScaleX(mScaleX);
						this.setScaleY(mScaleY);
					}

				}
			}

			return true;
		}
		
		private void updateTextPosition(){
			
			
		}

	}
	
	


}
