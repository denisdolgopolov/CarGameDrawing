package d.dolg.cardrawing.activities;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import d.dolg.cardrawing.Cnst;
import d.dolg.cardrawing.GdxGame;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Cnst.GameTypes gameType = (Cnst.GameTypes) getIntent().getExtras().get(Cnst.game_type);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new GdxGame(), config);
	}
}
