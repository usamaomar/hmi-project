package visualtasks.com;

import org.andengine.entity.IEntity;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.input.touch.detector.ClickDetector.IClickDetectorListener;
import org.andengine.input.touch.detector.HoldDetector;
import org.andengine.input.touch.detector.HoldDetector.IHoldDetectorListener;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;

import android.os.SystemClock;

/**
 * 
 * 
 * Inner class TaskSprite
 * 
 *
 **/


class TextSprite extends Sprite{
	
	private final static float START_SCALE = 0.5f;
	
	private final Font mFont;
	private Text mText;

	public TextSprite(String pText, float pX, float pY, Font pFont,ITextureRegion pTextureRegion, VertexBufferObjectManager vBOM) {
		super(pX,pY,pTextureRegion, vBOM);
		this.mFont = pFont;
				
		this.mText = new Text(0, 0, this.mFont, pText,	new TextOptions(AutoWrap.WORDS, this.getWidth(), 4, HorizontalAlign.CENTER),vBOM);
		
		this.attachChild(mText);
		this.setScale(START_SCALE);
		this.updateText();
	}
	
	
	public TextSprite(String pDescription,Font pFont,ITextureRegion pTextureRegion, VertexBufferObjectManager vBOM) {
		this(pDescription, 0, 0, pFont, pTextureRegion, vBOM);
		this.setPositionInCenter();
		
	}
	

	private void setPositionInCenter() {
		this.setPosition(this.getX() - this.getWidth() / 2f, this.getY() - this.getHeight()/ 2f);
		
	}



	private void bringToTop() {
		final IEntity parent = this.getParent();
			
		if (parent instanceof Scene){
			final Scene parentScene = (Scene) parent;
			parentScene.detachChild(this);
						
			parentScene.unregisterTouchArea(this);
			parentScene.attachChild(this);

		}
	}



	
	private void updateText(){
		mText.setPosition(this.getWidth() / 2f - mText.getWidth() / 2f,	this.getHeight() / 2f - mText.getHeight() / 2f);
	}
	

	
	
}