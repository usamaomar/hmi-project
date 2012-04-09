package visualtasks.com;

public interface ITextSpriteListener {


	void onTextSpriteScaleChanged(TextSprite pTextSprite, float mStartScaleX,float mStartScaleY, float pZoomFactor);

	void onTextSpritePositionChanged(TextSprite pTextSprite, float pDistanceX,float pDistanceY);

	void onTextSpriteHold(TextSprite mTextSprite);
	
}
