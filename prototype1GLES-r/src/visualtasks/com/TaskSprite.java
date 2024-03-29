package visualtasks.com;

import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;

import android.view.MotionEvent;
import brr.AndroidStrategy.MapControl.SmoothScrollCamera;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class TaskSprite extends AnimatedSprite implements  Comparable<TaskSprite>, IOnAreaTouchListener{

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
	private long id;
	private Body body;
	private Scene mScene;
	private VertexBufferObjectManager mVBO;
	private float velocity;
	private Visualtasks vt;

	private int mStatus = 0;
	public static final int STATUS_ACTIVE = 0;
	public static final int STATUS_COMPLETED = STATUS_ACTIVE + 1;
	public static final int STATUS_DELETED = STATUS_COMPLETED + 1;
	
	private int state = 0;
	public final static int STATE_DEFAULT = 0;
	public final static int STATE_TOUCHDOWN = 1;
	public final static int STATE_SCROLLING = 2;
	
	
	private float speedX;
	private float speedY;
	private float lastX;
	private float lastY;
		
	private float maxY;
	private float minY;
	private float maxX;
	private float minX;
	public TaskSprite(Visualtasks vt, long id, Scene pScene, FixtureDef fixtureDef, PhysicsWorld physicsWorld, Font pFont, TiledTextureRegion pTextureRegion,	VertexBufferObjectManager vBOM) {
		super(120,120,pTextureRegion, vBOM);
		mVBO =  vBOM;
		this.id = id;
		this.mPhysicsWorld = physicsWorld;
		this.mFixtureDef = fixtureDef;
		this.mScene = pScene;
		this.mFont = pFont;
		createBody();
		velocity = this.getScaleX()*this.getScaleX() * 2f;
		setScale(SCALE_DEFAULT);
//		mScene.registerUpdateHandler(this);
		body.setFixedRotation(true);
		this.vt = vt;
		
	}

	
	
	public void setDeleted(){
		this.removeBody();
		deleted = true;
	}
	
	@Override
	public boolean onAreaTouched(TouchEvent touchEvent, ITouchArea arg1, float arg2,
			float arg3) {
		
		
		MotionEvent evt = touchEvent.getMotionEvent();
		switch(touchEvent.getAction()){
		case TouchEvent.ACTION_DOWN:
			speedX = 0; speedY = 0;
			this.setState(STATE_TOUCHDOWN);
			this.lastX = evt.getRawX();
			this.lastY = evt.getRawY();
			break;
		case TouchEvent.ACTION_MOVE:
			
			
			this.setX(this.getX() - (lastX - evt.getRawX()));
			this.setY(this.getY() - (lastY - evt.getRawY()) );
			this.lastX = evt.getRawX();
			this.lastY = evt.getRawY();
			break;
		case TouchEvent.ACTION_CANCEL:
		case TouchEvent.ACTION_OUTSIDE:
		case TouchEvent.ACTION_UP:
			if(speedX != 0 || speedY != 0){
				this.setState(TaskSprite.STATE_SCROLLING);
			}
			else
				this.setState(TaskSprite.STATE_DEFAULT);
		}
		
		return false;
	}
	public void setState(int state) {
		this.state = state;
		switch(state){
		case STATE_DEFAULT:
//			this.getBody().setLinearVelocity(new Vector2(0,0));
			this.speedY = 0;
			this.speedX = 0;
			break;
		case STATE_SCROLLING:
			break;
		case STATE_TOUCHDOWN:
//			this.getBody().setLinearVelocity(new Vector2(0,0));
//			this.speedY = 0;
//			this.speedX = 0;
			break;
		}
	}
	
	public int getState() {
		return state;
	}
	

	@Override
	public void setX(float pX) {
		
		if (this.getBody()  == null){
			super.setX(pX);
		} else{
			this.getBody().setTransform(pX/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, this.getY()/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
		
		}
	}
	
	
	public Body getBody(){
//		PhysicsConnectorManager pcm = mPhysicsWorld.getPhysicsConnectorManager();
//		return pcm.findBodyByShape(this);
		return body;
	}
	@Override
	public void setY(float pY) {
//		if(this.getStatus() != STATUS_DELETED){
//			if (this.getY() > Visualtasks.BORDER) this.setStatus(STATUS_ACTIVE);
//			else if (this.getY() < Visualtasks.BORDER) this.setStatus(STATUS_COMPLETED);
//		}
		if (this.getBody() == null){
			super.setY(pY);
		} else{
			this.getBody().setTransform(this.getX()/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,pY/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
//			super.setY(pY);
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
//		int status = 2;
//		
//		if (deleted) status = STATUS_DELETED;
//		else if (this.getY() > Visualtasks.BORDER) status = STATUS_ACTIVE;
//		else if (this.getY() < Visualtasks.BORDER) status = STATUS_COMPLETED;
		
		return mStatus;
	}
	
	public void setStatus(int pStatus) {
		switch(pStatus){
		case STATUS_DELETED:
			this.removeBody();
			break;
		case STATUS_ACTIVE:
			if(this.getAlpha() != 1f)
				this.setAlpha(1f);
			vt.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					TaskSprite.this.setCurrentTileIndex(0);
					
				}
			});

			break;
		case STATUS_COMPLETED:
			this.setAlpha(0.7f);
			if(mStatus == STATUS_ACTIVE) {
				vt.toastOnUIThread(getText() + " completed", 0);
			}
			vt.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					TaskSprite.this.setCurrentTileIndex(5);
					
				}
			});
			
			break;
		}
		this.mStatus = pStatus;
	}
	
	@Override
	protected void onManagedUpdate(float pSecondsElapsed) {
		if (bodyNeedsUpdate){
			this.removeBody();
			this.createBody();
			velocity = this.getScaleX()*this.getScaleX();
			bodyNeedsUpdate = false;
		}
		
//		if (deleted) status = STATUS_DELETED;
//		else if (this.getY() > Visualtasks.BORDER) status = STATUS_ACTIVE;
//		else if (this.getY() < Visualtasks.BORDER) status = STATUS_COMPLETED;
		
		if(this.getStatus() != STATUS_DELETED){
			if (this.getY() > Visualtasks.BORDER) this.setStatus(STATUS_ACTIVE);
			else if (this.getY() < Visualtasks.BORDER) this.setStatus(STATUS_COMPLETED);
			
			switch(this.getState()){
			case STATE_DEFAULT:
				if(!vt.hasTaskAtBorder() || this.getStatus() == STATUS_COMPLETED){
					this.getBody().setLinearVelocity(0, -velocity);
//					this.setState(STATE_SCROLLING);
				}else{
					this.getBody().setLinearVelocity(0, 0);
				}
				
				break;
			case STATE_SCROLLING:
				final SmoothScrollCamera camera = vt.getCamera();
				if(this.speedY == 0 && this.speedX == 0 
						|| this.getY() >= camera.getBoundsYMax() - this.getHeightScaled()
//						|| this.getY() > camera.getYMax() - this.getHeightScaled()
						){
			  		this.setState(STATE_DEFAULT);
//					this.getBody().setLinearVelocity(0, 0);
					speedY = 0;
					speedX = 0;
				}else{
					this.setX(this.getX() + speedX * pSecondsElapsed);
					this.setY(this.getY() + speedY * pSecondsElapsed);
					
					this.speedY *=  (1.0f - 5f * pSecondsElapsed);
					this.speedX *= (1.0f - 5f * pSecondsElapsed);
					
					if(speedY < 10 && speedY > -10) speedY = 0;
					if(speedX < 10 && speedX > -10) speedX = 0;
					
				}
				break;
			case STATE_TOUCHDOWN:
				this.getBody().setLinearVelocity(0, 0);
				
				break;
			}
			
			//check position for borders
			SmoothScrollCamera camera = vt.getCamera();
			if(this.getX() < camera.getBoundsXMin()) {
				this.setX(camera.getBoundsXMin()+this.getWidthScaled()/2f);
				this.speedX = 0;
			}else if(this.getX() > camera.getBoundsXMax()){
				this.setX(camera.getBoundsXMax()-this.getWidthScaled()/2f);
				this.speedX = 0;
			}
			if(this.getY() < camera.getBoundsYMin()) {
				this.setY(camera.getBoundsYMin()+this.getHeightScaled()/2f);
				this.speedY = 0;
			}else if(this.getY() > camera.getBoundsYMax()){
				this.setY(camera.getBoundsYMax()-this.getHeightScaled()/2f);
				this.speedY = 0;
			}
			
			if(this.getState() != STATE_TOUCHDOWN){
				if(this.getStatus() == STATUS_ACTIVE && this.getY() < Visualtasks.BORDER + this.getHeightScaled()/2f){
					this.setY(Visualtasks.BORDER + this.getHeightScaled()/2f);
					this.speedY = 0;
				} else if(this.getStatus() == STATUS_COMPLETED && this.getY() > Visualtasks.BORDER - this.getHeightScaled()/2f){
					this.setY(Visualtasks.BORDER - this.getHeightScaled()/2f);
					this.speedY = 0;
				}
			}
		}
		
		// check position, change if necesarry
		
		
		
		
		//set alpha
		

		//check if new body needed
		super.onManagedUpdate(pSecondsElapsed);
	}

	
		
	public  float getUrgency(){
		return SCALE_MAX-(this.getScaleX()/SCALE_FACTOR);
	}
	
	public void setUrgency(float urgency){
		 this.setScale((SCALE_MAX-urgency) * SCALE_FACTOR);
	}
	
	public void setSpeedY(float speedY){
		this.speedY = speedY;
	}
	public void setSpeedX(float speedX) {
		this.speedX = speedX;
	}



	@Override
	public int compareTo(TaskSprite another) {
		if(this.getStatus() != another.getStatus()){
			return Float.compare(this.getStatus(), another.getStatus());
		} else if (this.getY() != another.getY()){
			return Float.compare(this.getY(), another.getY());
		} else return Double.compare(this.getId(), another.getId());
	}

	

	public boolean isFirstActiveTask(){
		
		return this.getStatus() == STATUS_ACTIVE 
				&& this.getY() <= Visualtasks.BORDER + this.getHeightScaled()/2f +5;
	}
}
