package asf.dungeon.android;

import android.os.Bundle;
import android.view.WindowManager;
import asf.dungeon.DungeonApp;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

/**
 * https://github.com/TheInvader360/tutorial-libgdx-google-ads/blob/9a4c9342d98c02e3c44e0b62fcfaa153d257130a/tutorial-libgdx-google-ads-android/src/com/theinvader360/tutorial/libgdx/google/ads/MainActivity.java
 * https://github.com/TheInvader360/tutorial-libgdx-google-ads/commit/0a5ea376d4eb92b8e87c13a03245adb40b53e811
 * <p/>
 * https://github.com/googleads/googleads-mobile-android-examples/blob/master/admob/interstitial/src/com/google/example/gms/ads/advanced/InterstitialSample.java
 */
public class AndroidLauncher extends AndroidApplication implements DungeonApp.Resolver{

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
                config.useAccelerometer = false;
                config.useCompass = false;
                //config.numSamples = 2;
                config.numSamples = 0;
                config.useImmersiveMode = true;
                config.depth = 1;


                DungeonApp dungeonGame = new DungeonApp();
                dungeonGame.setPlatformActionResolver(this);

                initialize(dungeonGame, config);

                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        }

        @Override
        public void showDebugWindow() {

        }

        @Override
        public void onResume() {
                super.onResume();
        }

        @Override
        public void onPause() {
                super.onPause();
        }

        @Override
        public void onDestroy() {
                super.onDestroy();
        }


}
