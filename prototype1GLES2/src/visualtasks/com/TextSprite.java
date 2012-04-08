package visualtasks.com;

import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;

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
	private IOnAreaTouchListener mIOnAreaTouchListener;
	
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
	

	public void setIOnAreaTouchListener(IOnAreaTouchListener pIOnAreaTouchListener) {
		this.mIOnAreaTouchListener = pIOnAreaTouchListener;
	}
	
	private void setPositionInCenter() {
		this.setPosition(this.getX() - this.getWidth() / 2f, this.getY() - this.getHeight()/ 2f);
		
	}



	private void updateText(){
		mText.setPosition(this.getWidth() / 2f - mText.getWidth() / 2f,	this.getHeight() / 2f - mText.getHeight() / 2f);
	}
	
	public void setText(String pText){
		this.mText.setText(pText);
	}

	public String getText(){
		return this.mText.getText().toString();
	}
	
	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent,float pTouchAreaLocalX, float pTouchAreaLocalY) {
		// TODO Auto-generated method stub
		return mIOnAreaTouchListener.onAreaTouched(pSceneTouchEvent, this, pTouchAreaLocalX, pTouchAreaLocalY);
		
	}
}