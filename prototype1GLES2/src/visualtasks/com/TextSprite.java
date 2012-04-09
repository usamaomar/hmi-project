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
	
	
	
	private final Font mFont;
	private Text mText;
	
	
	public TextSprite(String pText, float pX, float pY, Font pFont,ITextureRegion pTextureRegion, VertexBufferObjectManager vBOM) {
		super(pX,pY,pTextureRegion, vBOM);
		this.mFont = pFont;
				
		this.mText = new Text(0, 0, this.mFont, pText,	new TextOptions(AutoWrap.WORDS, this.getWidth(), 4, HorizontalAlign.CENTER),vBOM);
		
		this.attachChild(mText);
		
		init();
		
	}
	
	public TextSprite(String pText,Font pFont,ITextureRegion pTextureRegion, VertexBufferObjectManager vBOM) {
		this(pText, 0, 0, pFont, pTextureRegion, vBOM);
	
	}
	public TextSprite(Font pFont,ITextureRegion pTextureRegion, VertexBufferObjectManager vBOM) {
		this("", 0, 0, pFont, pTextureRegion, vBOM);
	
	}
	
	
	private void init(){
		mText.setPosition(this.getWidth() / 2f - mText.getWidth() / 2f,	this.getHeight() / 2f - mText.getHeight() / 2f);
		
		
	}
	
	
	

	
	
	


	
	
	public void setText(String pText){
		this.mText.setText(pText);
	}

	public String getText(){
		return this.mText.getText().toString();
	}
	
	
}