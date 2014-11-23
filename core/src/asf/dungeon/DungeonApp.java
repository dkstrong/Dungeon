package asf.dungeon;

import asf.dungeon.model.ModelId;
import asf.dungeon.view.DungeonWorld;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Locale;

/**
 * the skin used for the stages is loaded and stored and dungeon app for effecient reuse as its also used by the in game hud.
 */
public class DungeonApp implements ApplicationListener {

        private Screen screen;
        private DungeonWorld worldManager;
        private Resolver platformActionResolver;
        protected Stage stage;
        protected Skin skin;
        protected I18NBundle i18n;

        @Override
        public void create() {
                Gdx.input.setCatchMenuKey(true);
                Gdx.input.setCatchBackKey(true);



                this.setScreen(new MainMenuScreen(this));

                DungeonWorld.Settings settings = new DungeonWorld.Settings();
                settings.playerModel = ModelId.Archer;

                //loadWorld(settings);
        }

        @Override
        public void resize(int width, int height) {
                if (worldManager != null)
                        worldManager.resize(width, height);
                if (stage != null)
                        stage.getViewport().update(width, height, true);
                if (screen != null)
                        screen.resize(width, height);
        }

        @Override
        public void render() {
                float delta = Gdx.graphics.getDeltaTime();
                if (worldManager != null)
                        worldManager.render(delta);
                if (screen != null)
                        screen.render(delta);


        }

        @Override
        public void pause() {
                if (Gdx.app.getType() != Application.ApplicationType.Desktop) {
                        // only autopause for android/ios/html
                        if (worldManager != null)
                                worldManager.saveDungeon();
                        setAppPaused(true);
                }
                if (screen != null)
                        screen.pause();
        }

        @Override
        public void resume() {
                // we dont resume the world, instead the user should resume
                // at their own convenience

                if (screen != null)
                        screen.resume();
        }

        @Override
        public void dispose() {
                unloadWorld();
                if (screen != null)
                        screen.hide();
        }

        public void setScreen(Screen screen) {
                if (this.screen != null)
                        this.screen.hide();
                this.screen = screen;
                if (this.screen != null) {
                        if (stage == null) {
                                stage = new Stage(new ScreenViewport());
                                skin = new Skin(Gdx.files.internal("Skins/BasicSkin/uiskin.json"));
                                FileHandle baseFileHandle = Gdx.files.internal("i18n/Menu");
                                Locale locale = new Locale("en");
                                i18n = I18NBundle.createBundle(baseFileHandle, locale);
                        }
                        this.screen.show();
                        this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                } else {
                        if (stage != null) {
                                stage.dispose();
                                stage = null;
                                skin.dispose();
                                skin = null;
                                i18n = null; // does not need to be disposed
                        }
                }
        }

        public void loadWorld(DungeonWorld.Settings settings) {
                if (worldManager != null)
                        throw new IllegalStateException("world is already loaded");

                Gdx.graphics.setContinuousRendering(true);
                worldManager = new DungeonWorld(this, settings);
                //worldManager.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

                setScreen(createLoadingScreen());

        }

        public void onSimulationStarted() {
                setScreen(null);
        }

        protected Screen createLoadingScreen() {
                return new LoadingScreen(this);
        }

        protected Screen createPauseScreen() {
                return new PauseScreen(this);
        }

        public void unloadWorld() {
                if (worldManager != null)
                        worldManager.dispose();
                worldManager = null;
        }

        /**
         * pauses the app and shows the generic pause menu screen
         * mainly only used the app loses focus.
         * @param paused
         */
        public void setAppPaused(boolean paused) {
                if (worldManager == null || worldManager.isPaused() == paused)
                        return;

                worldManager.setPaused(paused);

                if (paused) {
                        setScreen(createPauseScreen());
                } else {
                        Gdx.graphics.setContinuousRendering(true);
                        setScreen(null);
                }
        }

        public boolean isAppPaused() {
                if (worldManager == null)
                        return false;

                return worldManager.isPaused();

        }

        public void exitApp() {
                // TODO: are you sure you want to quit etc
                Gdx.app.exit();
        }

        public Screen getScreen() {
                return screen;
        }

        public Resolver getPlatformActionResolver() {
                return platformActionResolver;
        }

        public void setPlatformActionResolver(Resolver platformActionResolver) {
                this.platformActionResolver = platformActionResolver;
        }

        public interface Resolver {
                public int getInterstitialAdState();

                public void loadInterstitialAd();

                public void showInterstitialAd();
        }
}
