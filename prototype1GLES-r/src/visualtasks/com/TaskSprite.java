package visualtasks.com;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;

import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class TaskSprite extends Sprite {

	private  Font mFont;
	private Text mText;
	

	private final static float SCALE_FACTOR = 0.1f;
	private static final float SCALE_MAX = 1f;
	private static final float SCALE_DEFAULT = 0.5f;
	private static final float SCALE_MIN = 0.1f;
	
	private PhysicsWorld mPhysicsWorld;
	private FixtureDef mFixtureDef;
	 private PhysicsConnector pc;
	private boolean deleted = false;
	private boolean bodyNeedsUpdate = false;
	private boolean selected = false;
	private long id;
	private Body body;
	private Scene mScene;
	private VertexBufferObjectManager mVBO;
	private float velocity;
	
	public TaskSprite(long id, Scene pScene, FixtureDef fixtureDef, PhysicsWorld physicsWorld, Font pFont, ITextureRegion pTextureRegion,	VertexBufferObjectManager vBOM) {
		super(120,120,pTextureRegion, vBOM);
		mVBO =  vBOM;
		this.id = id;
		this.mPhysicsWorld = physicsWorld;
		this.mFixtureDef = fixtureDef;
		this.mScene = pScene;
		this.mFont = pFont;
		createBody();
		velocity = this.getScaleX()*this.getScaleX()*0.5f;
		setScale(SCALE_DEFAULT);
//		mScene.registerUpdateHandler(this);
		
		
	}

	public static final int STATUS_ACTIVE = 0;
	public static final int STATUS_COMPLETED = STATUS_ACTIVE + 1;
	public static final int STATUS_DELETED = STATUS_COMPLETED + 1;
	
	public void setDeleted(){
		this.removeBody();
		deleted = true;
	}
	
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public boolean isSelected() {
		return selected;
	}
	@Override
	public void setX(float pX) {
		
		if (this.getBody()  == null){
			super.setX(pX);
		} else{
			this.getBody().setTransform(pX/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, this.getY()/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
		}
	}
	
	private Body getBody(){
//		PhysicsConnectorManager pcm = mPhysicsWorld.getPhysicsConnectorManager();
//		return pcm.findBodyByShape(this);
		return body;
	}
	@Override
	public void setY(float pY) {
		if (this.getBody() == null){
			super.setY(pY);
		} else{
			this.getBody().setTransform(this.getX()/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,pY/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
		}
	}
	
	@Override
	public float getY() {
		if(this.getBody() == null){
			return super.getY();
		} else{
			return this.getBody().getPosition().y * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
		}
	}
	
	@Override
	public float getX() {
		if(this.getBody() == null){
			return super.getX();
		} else{
			return this.getBody().getPosition().x * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
		}
	}
	
	public long getId() {
		return id;
	}
	
	public void setText(String text){
		if (mText != null) this.detachChild(mText);
		this.mText = new Text(0, 0, this.mFont, text,	new TextOptions(AutoWrap.WORDS, this.getWidth(), HorizontalAlign.CENTER),mVBO);
		
		this.attachChild(mText);
		mText.setPosition(this.getWidth() / 2f - mText.getWidth() / 2f,	this.getHeight() / 2f - mText.getHeight() / 2f);
	}
	public String getText(){
		return (mText == null? "" : mText.getText().toString());
	}
	
	
	@Override
	public void setScale(float pScale) {
		pScale = Math.max(Math.min(pScale, this.SCALE_MAX),this.SCALE_MIN);
		super.setScale(pScale);
		bodyNeedsUpdate = true;
		
	}
	
	@Override
	public void setScale(float pScaleX, float pScaleY) {
		// TODO Auto-generated method stub
		pScaleX = Math.max(Math.min(pScaleX, this.SCALE_MAX),this.SCALE_MIN);
		pScaleY = Math.max(Math.min(pScaleY, this.SCALE_MAX),this.SCALE_MIN);
		super.setScale(pScaleX, pScaleY);
		bodyNeedsUpdate = true;
	}
	
	private void createBody(){
		body = PhysicsFactory.createCircleBody(mPhysicsWorld, this, BodyType.DynamicBody, mFixtureDef, PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
    	  pc = new PhysicsConnector(this, body, true, true, PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
    	 mPhysicsWorld.registerPhysicsConnector(pc);
	}
	
	private void removeBody(){
		if(this.getBody() != null){
			mPhysicsWorld.unregisterPhysicsConnector(pc);
			mPhysicsWorld.destroyBody(this.getBody());
						
		}
	}
	
	public int getStatus(){
		int status = 2;
		
		if (deleted) status = STATUS_DELETED;
		else if (this.getY() > Visualtasks.BORDER) status = STATUS_ACTIVE;
		else if (this.getY() < Visualtasks.BORDER) status = STATUS_COMPLETED;
		
		return status;
	}
	
	
	
	@Override
	protected void onManagedUpdate(float pSecondsElapsed) {
		if (bodyNeedsUpdate){
			this.removeBody();
			this.createBody();
			velocity = this.getScaleX()*this.getScaleX();
			bodyNeedsUpdate = false;
		}
		switch(this.getStatus()){
		case STATUS_DELETED:
			this.removeBody();
			break;
		case STATUS_ACTIVE:
		case STATUS_COMPLETED:
			if(this.isSelected()){
				body.setLinearVelocity(0, 0);
				body.setActive(true);
				if(this.getY() - this.getHeight()/2 < Visualtasks.BORDER+3) {
					body.setActive(false);
				}
			}
			else{
					final Vector2 velocityv = Vector2Pool.obtain(0, -velocity);
			 		body.setLinearVelocity(velocityv);
			 		body.setActive(true);
			 		Vector2Pool.recycle(velocityv);
			}
			break;
		}
		
		
		//set alpha
		switch(this.getStatus()){
		case STATUS_DELETED:
			break;
		case STATUS_ACTIVE:
			if(this.getAlpha() != 1f)
				this.setAlpha(1f);

			break;
		case STATUS_COMPLETED:
			this.setAlpha(0.3f);
			break;
		}
		//check if new body needed
		super.onManagedUpdate(pSecondsElapsed);
	}

	
		
	public  float getUrgency(){
		return SCALE_MAX-(this.getScaleX()/SCALE_FACTOR);
	}
	
	public void setUrgency(float urgency){
		 this.setScale((SCALE_MAX-urgency) * SCALE_FACTOR);
	}
	
}
