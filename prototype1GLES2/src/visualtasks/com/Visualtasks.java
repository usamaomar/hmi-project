package visualtasks.com;

import java.util.ArrayList;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
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
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

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

	// ===========================================================
	// Constructors
	// ===========================================================

	private ITextureRegion mTaskTextureRegion;
	private ZoomCamera mZoomCamera;

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
		
		//this.mBackgroundTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),2048, 1024,TextureOptions.BILINEAR_PREMULTIPLYALPHA);

	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setOnAreaTouchTraversalFrontToBack();
		
		this.mScene.setBackground(new Background(0.09804f, 0.6274f,0.8784f));
		// Sprite backgroundSprite = new Sprite(0,0,this.CAMERA_WIDTH,
		// this.CAMERA_HEIGHT, mBackgroundTextureRegion);

		// this.mScene.setBackground(new SpriteBackground(backgroundSprite));

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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHoldFinished(HoldDetector pHoldDetector,
			long pHoldTimeMilliseconds, int pPointerID, float pHoldX,
			float pHoldY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHoldStarted(HoldDetector pHoldDetector, int pPointerID,
			float pHoldX, float pHoldY) {
		// TODO Auto-generated method stub
		
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
		AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
		helpBuilder.setTitle("Choose title");
		final EditText input = new EditText(this);
		input.setSingleLine();
		input.setText("");
		helpBuilder.setView(input);
		helpBuilder.setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String name = input.getText().toString();
						spawnTask(name, pX, pY);
						// Toast.makeText(example, "Title is: " + name,
						// Toast.LENGTH_LONG).show();
					}
				});

		// Remember, create doesn't show the dialog
		AlertDialog helpDialog = helpBuilder.create();
		helpDialog.show();
//		new StringInputDialogBuilder(this,
//			    R.string.dialog_task_new_title,
//			    R.string.dialog_task_new_message,
//			    R.string.dialog_task_new_message,
//			    android.R.drawable.ic_dialog_info,
//			    new Callback<String>() {
//										
//					@Override
//					public void onCallback(String pCallbackValue) {
//						spawnTask(pCallbackValue, pX, pY);
//						
//					}
//			
//			},   new OnCancelListener() {
//
//				@Override
//				public void onCancel(DialogInterface arg0) {
//					// nothing
//					
//				}}
//			).create();
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
		this.mScene.setTouchAreaBindingOnActionMoveEnabled(true);
	}

	class TaskSprite extends Sprite {
		private final static int INVALID_TOUCHING_ID = -1;

		private final static int MAX_CHARACTERS = 50;
		private final static int MAX_LINES = 3;
		private final static float START_SCALE = 0.5f;
		private final static int TEXT_PADDING = 8;
		private float dX, dY, dX2, dY2;
		private boolean isZooming, isTouched, isLongTouching;

		private final Font mFont;
		private float mScaleX, mScaleY;
		private Text mText;
		private Integer touchingID = INVALID_TOUCHING_ID;
		private Integer touchingID2 = INVALID_TOUCHING_ID;

		public TaskSprite(String text, float pX, float pY, Font pFont,ITextureRegion pTextureRegion, VertexBufferObjectManager vBOM) {
			super(pX,pY,pTextureRegion, vBOM);
			this.mFont = pFont;
			
			this.mText = new Text(0, 0, this.mFont, text,	new TextOptions(AutoWrap.LETTERS, this.getWidth(), 4, HorizontalAlign.CENTER),vBOM);
			
			mText.setPosition(this.getWidth() / 2f - mText.getWidth() / 2f,	this.getHeight() / 2f - mText.getHeight() / 2f);
			this.attachChild(mText);
			this.setScale(START_SCALE);
			

		}

		private void bringToTop() {
			synchronized (mTasksList) {
				// mTasksList.remove(this);
				// mTasksList.add(this);
				// this.getParent().sortChildren(new Comparator<IEntity>(){
				//
				// @Override
				// public int compare(IEntity arg0, IEntity arg1) {
				// if (arg0 instanceof TaskSprite && arg1 instanceof
				// TaskSprite){
				// Integer i0 = mTasksList.indexOf((TaskSprite) arg0);
				// Integer i1 = mTasksList.indexOf((TaskSprite) arg1);
				// return i0.compareTo(i1);
				// }
				// return 0;
				// }
				//
				// });

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
						isLongTouching = true;

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
