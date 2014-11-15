package asf.dungeon.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import asf.dungeon.DungeonApp;

public class DesktopLauncher implements DungeonApp.Resolver{
	public static void main (String[] arg) {
                LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
                config.title = "Dungeon";

                String osName = System.getProperty("os.name").toLowerCase();
                if(osName.contains("win")){
                        config.width = 1280;
                        config.height = 720;
                }else{
                        config.width = 800;
                        config.height = 480;
                }

                config.foregroundFPS = 30;
                config.backgroundFPS = 30;
                config.vSyncEnabled = false;
                config.samples = 2;
                config.r = 5;
                config.g = 6;
                config.b = 5;
                config.a = 0;


                DesktopLauncher launcher = new DesktopLauncher();

                DungeonApp dungeonGame = new DungeonApp();
                dungeonGame.setPlatformActionResolver(launcher);

                new LwjglApplication(dungeonGame, config);
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
}
