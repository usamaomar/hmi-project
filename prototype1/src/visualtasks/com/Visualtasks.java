package visualtasks.com;

import java.util.ArrayList;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.ZoomCamera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.HoldDetector;
import org.anddev.andengine.input.touch.detector.HoldDetector.IHoldDetectorListener;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.HorizontalAlign;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class Visualtasks extends BaseGameActivity implements
		IOnSceneTouchListener, IScrollDetectorListener,
		IPinchZoomDetectorListener, IHoldDetectorListener {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_HEIGHT = 720;
	private static final int CAMERA_WIDTH = 1080;
	private static final int TRIGGER_HOLD_MIN_MILISECONDS = 500;

	// ===========================================================
	// Fields
	// ===========================================================

	// wrap text within boundaries
	public static String getNormalizedText(Font font, String ptext,
			float textWidth) {
		// no need to normalize, its just one word, so return
		if (!ptext.contains(" "))
			return ptext;
		String[] words = ptext.split(" ");
		StringBuilder normalizedText = new StringBuilder();
		StringBuilder line = new StringBuilder();

		for (int i = 0; i < words.length; i++) {
			if (font.getStringWidth((line + words[i])) > (textWidth)) {
				normalizedText.append(line).append('\n');
				line = new StringBuilder();
			}

			if (line.length() == 0)
				line.append(words[i]);
			else
				line.append(' ').append(words[i]);

			if (i == words.length - 1)
				normalizedText.append(line);
		}
		return normalizedText.toString();
	}

	private BitmapTextureAtlas mBackgroundTextureAtlas;
	private TextureRegion mBackgroundTextureRegion;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TextureRegion mFaceTextureRegion;
	private Font mFont;
	private BitmapTextureAtlas mFontTexture;
	private HoldDetector mHoldDetector;
	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;
	// private Camera mCamera;
	private Scene mScene;
	private SurfaceScrollDetector mScrollDetector;
	private ArrayList<TaskSprite> mTasksList = new ArrayList<TaskSprite>();
	private TextureRegion mTaskTextureRegion;

	// ===========================================================
	// Constructors
	// ===========================================================

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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public void onHold(HoldDetector arg0, long arg1, final float arg2,
			final float arg3) {

	}

	@Override
	public void onHoldFinished(HoldDetector arg0, long arg1, final float arg2,
			final float arg3) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mHoldDetector.setEnabled(false);
				showPopUp(arg2, arg3);
				mHoldDetector.setEnabled(true);
			}
		});

	}

	@Override
	public void onLoadComplete() {

	}

	// Methods for menu

	@Override
	public Engine onLoadEngine() {

		this.mZoomCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		final Engine engine = new Engine(new EngineOptions(true,
				ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), this.mZoomCamera));

		try {
			if (MultiTouch.isSupported(this)) {
				engine.setTouchController(new MultiTouchController());
			} else {
				Toast.makeText(
						this,
						"Sorry your device does NOT support MultiTouch!\n\n(No PinchZoom is possible!)",
						Toast.LENGTH_LONG).show();
			}
		} catch (final MultiTouchException e) {
			Toast.makeText(
					this,
					"Sorry your Android Version does NOT support MultiTouch!\n\n(No PinchZoom is possible!)",
					Toast.LENGTH_LONG).show();
		}

		return engine;
	}

	@Override
	public void onLoadResources() {
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mFontTexture = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mFont = new Font(this.mFontTexture, Typeface.create(
				Typeface.DEFAULT, Typeface.BOLD), 56, true, Color.WHITE);

		this.mBackgroundTextureAtlas = new BitmapTextureAtlas(2048, 1024,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		this.mTaskTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "bubble.png",
						0, 0);
		this.mBackgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBackgroundTextureAtlas, this,
						"achtergrond1.png", 0, 0);
		this.mEngine.getTextureManager().loadTextures(this.mBitmapTextureAtlas,
				this.mFontTexture);
		this.mEngine.getFontManager().loadFont(this.mFont);

	}

	@Override
	public Scene onLoadScene() {

		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setOnAreaTouchTraversalFrontToBack();
		this.mScene.setBackground(new ColorBackground(0.09804f, 0.6274f,
				0.8784f));
		// Sprite backgroundSprite = new Sprite(0,0,this.CAMERA_WIDTH,
		// this.CAMERA_HEIGHT, mBackgroundTextureRegion);

		// this.mScene.setBackground(new SpriteBackground(backgroundSprite));

		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mHoldDetector = new HoldDetector(this);
		this.mHoldDetector
				.setTriggerHoldMinimumMilliseconds(TRIGGER_HOLD_MIN_MILISECONDS);
		this.mScene.registerUpdateHandler(mHoldDetector);

		if (MultiTouch.isSupportedByAndroidVersion()) {
			try {
				this.mPinchZoomDetector = new PinchZoomDetector(this);
			} catch (final MultiTouchException e) {
				this.mPinchZoomDetector = null;
			}
		} else {
			this.mPinchZoomDetector = null;
		}
		this.mScene.setOnSceneTouchListener(this);

		this.mScene.setTouchAreaBindingEnabled(true);

		return this.mScene;

	}

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
	public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector,
			final TouchEvent pTouchEvent, final float pZoomFactor) {
		
			this.mZoomCamera
					.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor
							* pZoomFactor);
		
	}

	@Override
	public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector,
			final TouchEvent pTouchEvent, final float pZoomFactor) {
		
			this.mZoomCamera
					.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor
							* pZoomFactor);
		
	}

	@Override
	public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector,
			final TouchEvent pTouchEvent) {

		this.mPinchZoomStartedCameraZoomFactor = this.mZoomCamera
				.getZoomFactor();

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
	public void onScroll(final ScrollDetector pScollDetector,
			final TouchEvent pTouchEvent, final float pDistanceX,
			final float pDistanceY) {
		synchronized (this.mZoomCamera) {
			final float zoomFactor = this.mZoomCamera.getZoomFactor();
			this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY
					/ zoomFactor);
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private void showPopUp() {
		final int centerX = (CAMERA_WIDTH - this.mTaskTextureRegion.getWidth()) / 2;
		final int centerY = (CAMERA_HEIGHT - this.mTaskTextureRegion
				.getHeight()) / 2;
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
	}

	// ===========================================================
	// Methods
	// ===========================================================
	public void spawnTask(String name, float pX, float pY) {

		final TaskSprite sprite = new TaskSprite(0, 0, mFont,
				mTaskTextureRegion);
		sprite.setPosition(pX - sprite.getWidth() / 2f, pY - sprite.getHeight()
				/ 2f);

		sprite.setText(name);

		mTasksList.add(0, sprite);

		this.mScene.attachChild(sprite);
		this.mScene.registerTouchArea(sprite);
		this.mScene.setTouchAreaBindingEnabled(true);
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
		private ChangeableText mText;
		private Integer touchingID = INVALID_TOUCHING_ID;
		private Integer touchingID2 = INVALID_TOUCHING_ID;

		public TaskSprite(float pX, float pY, Font pFont,
				TextureRegion pTextureRegion) {
			super(pX, pY, pTextureRegion);
			this.mFont = pFont;
			this.mText = new ChangeableText(0, 0, pFont, "Task",
					HorizontalAlign.CENTER, MAX_CHARACTERS);
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

		public String getText() {
			return this.mText.getText();
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

		public void setText(String pText) {
			this.mText.setText(getNormalizedText(mFont, pText, this.getWidth()
					- 2 * TEXT_PADDING));
			mText.setPosition(this.getWidth() / 2f - mText.getWidth() / 2f,
					this.getHeight() / 2f - mText.getHeight() / 2f);
		}

		private void setTouched() {

		}

	}

}
