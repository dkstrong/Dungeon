package asf.dungeon;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.SongId;
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

        public final MusicManager music = new MusicManager();
        private Screen screen;
        private DungeonWorld dungeonWorld;
        private Resolver platformActionResolver;
        protected Stage stage;
        protected Skin skin;
        protected I18NBundle i18n;

        @Override
        public void create() {
                Gdx.input.setCatchMenuKey(true);
                Gdx.input.setCatchBackKey(true);

                //this.setScreen(new MainMenuScreen(this));
                returnToMainMenu();

                //DungeonWorld.Settings settings = new DungeonWorld.Settings();
                //settings.playerModel = ModelId.Archer;
                //settings.startDebugSession = true;
                //loadWorld(settings);

        }

        public void returnToMainMenu(){
                if (dungeonWorld != null){
                        dungeonWorld.saveDungeon();
                        unloadWorld();
                }

                this.setScreen(new MainMenuScreen(this));
                music.setPlaylist(SongId.MainTheme, SongId.Arabesque, SongId.RitualNorm);
                music.playSong(SongId.MainTheme);
                music.setPaused(false);

        }

        @Override
        public void resize(int width, int height) {
                if (dungeonWorld != null)
                        dungeonWorld.resize(width, height);
                if (stage != null)
                        stage.getViewport().update(width, height, true);
                if (screen != null)
                        screen.resize(width, height);
        }

        @Override
        public void render() {
                //System.out.println("render "+Gdx.graphics.getDeltaTime()+", raw: "+Gdx.graphics.getRawDeltaTime());
                float delta = Gdx.graphics.getDeltaTime();
                if (dungeonWorld != null)
                        dungeonWorld.render(delta);
                if (screen != null)
                        screen.render(delta);
                music.update(delta);
        }

        @Override
        public void pause() {
                if (Gdx.app.getType() != Application.ApplicationType.Desktop) {
                        // only autopause for android/ios/html
                        if (dungeonWorld != null)
                                dungeonWorld.saveDungeon();
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
                setScreen(null);
                music.dispose();
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
                if (dungeonWorld != null)
                        throw new IllegalStateException("world is already loaded");

                Gdx.graphics.setContinuousRendering(true);
                dungeonWorld = new DungeonWorld(this, settings);
                //dungeonWorld.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

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
                if (dungeonWorld != null)
                        dungeonWorld.dispose();
                dungeonWorld = null;
        }

        /**
         * pauses the app and shows the generic pause menu screen
         * mainly only used the app loses focus.
         * @param paused
         */
        public void setAppPaused(boolean paused) {
                if (dungeonWorld == null || dungeonWorld.isPaused() == paused)
                        return;

                dungeonWorld.setPaused(paused);

                if (paused) {
                        setScreen(createPauseScreen());
                } else {
                        setScreen(null);
                }

                music.setPaused(screen != null);
        }

        public boolean isAppPaused() {
                if (dungeonWorld == null)
                        return false;

                return dungeonWorld.isPaused();

        }

        public void setAppGameOver(){
                if (dungeonWorld == null)
                         return;

                this.setScreen(new GameOverScreen(this, dungeonWorld));
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

                public DebugSession getDebugSession();
        }

        public interface DebugSession{
                public void updateDebugInfo(Dungeon dungeon);
        }
}
