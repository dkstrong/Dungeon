package asf.dungeon.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import asf.dungeon.DungeonApp;

public class HtmlLauncher extends GwtApplication implements DungeonApp.Resolver {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(800, 480);
        }

        @Override
        public ApplicationListener getApplicationListener () {
                DungeonApp dungeonGame = new DungeonApp();
                dungeonGame.setPlatformActionResolver(this);

                return dungeonGame;
        }

        @Override
        public int getInterstitialAdState() {
                return 0;
        }

        @Override
        public void loadInterstitialAd() {

        }

        @Override
        public void showInterstitialAd() {

        }

        @Override
        public void showDebugWindow() {

        }
}