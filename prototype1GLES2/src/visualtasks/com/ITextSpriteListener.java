package visualtasks.com;

public interface ITextSpriteListener {


	void afterTextSpriteScaleChanged(TextSprite pTextSprite, float mStartScaleX,float mStartScaleY, float pZoomFactor);

	void afterTextSpritePositionChanged(TextSprite pTextSprite, float pDistanceX,float pDistanceY);

	void onTextSpriteHold(TextSprite mTextSprite);
	
}
