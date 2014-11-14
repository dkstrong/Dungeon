package asf.dungeon;

import asf.dungeon.model.ModelId;
import asf.dungeon.view.DungeonWorld;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 *
 *
 * the skin used for the stages is loaded and stored and dungeon app for effecient reuse as its also used by the in game hud.
 *
 */
public class DungeonApp implements ApplicationListener {

        private Screen screen;
        private DungeonWorld worldManager;
        private Resolver platformActionResolver;
        protected Stage stage;
        public Skin skin;

        @Override
        public void create() {
                Gdx.input.setCatchMenuKey(true);
                Gdx.input.setCatchBackKey(true);


                //this.setScreen(new MainMenuScreen(this));

                DungeonWorld.Settings settings = new DungeonWorld.Settings();
                settings.playerModel=ModelId.Archer;

                loadWorld(settings);
        }

        @Override
        public void resize(int width, int height) {
                if(worldManager != null)
                        worldManager.resize(width, height);
                if(stage != null)
                        stage.getViewport().update(width, height, true);
                if(screen != null)
                        screen.resize(width,height);
        }

        @Override
        public void render() {
                float delta = Gdx.graphics.getDeltaTime();
                if(worldManager != null)
                        worldManager.render(delta);
                if(screen != null)
                        screen.render(delta);


        }

        @Override
        public void pause() {
                if(worldManager != null)
                        worldManager.saveDungeon();
                setWorldPaused(true);
                if(screen != null)
                        screen.pause();
        }

        @Override
        public void resume() {
                if(screen != null)
                        screen.resume();
                // we dont resume the worldRenderManager because we only want it to be
                // resume by the user input on the menu screen
        }

        @Override
        public void dispose() {
                unloadWorld();
                if(screen != null)
                        screen.hide();
        }

        public void setScreen(Screen screen){
                if(this.screen != null)
                        this.screen.hide();
                this.screen = screen;
                if(this.screen != null){
                        if(stage == null){
                                stage = new Stage(new ScreenViewport());
                                skin = new Skin(Gdx.files.internal("Skins/BasicSkin/uiskin.json"));
                        }
                        this.screen.show();
                        this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                }else{
                        if(stage != null){
                                stage.dispose();
                                stage = null;
                                skin.dispose();
                                skin = null;
                        }
                }
        }

        public void loadWorld(DungeonWorld.Settings settings){
                if(worldManager != null)
                        throw new IllegalStateException("world is already loaded");

                Gdx.graphics.setContinuousRendering(true);
                worldManager = new DungeonWorld(this, settings);
                //worldManager.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

                setScreen(createLoadingScreen());

        }

        public void onSimulationStarted() {
                setScreen(null);
        }

        protected Screen createLoadingScreen(){
                return new LoadingScreen(this);
        }

        protected Screen createPauseScreen(){
                return new PauseScreen(this);
        }

        public void unloadWorld(){
                if(worldManager != null)
                        worldManager.dispose();
                worldManager = null;
        }

        public void setWorldPaused(boolean paused){
                if(worldManager == null || worldManager.isPaused() == paused)
                        return;

                worldManager.setPaused(paused);

                if(paused){
                        setScreen(createPauseScreen());
                }else{
                        Gdx.graphics.setContinuousRendering(true);
                        setScreen(null);
                }
        }

        public boolean isWorldPaused(){
                if(worldManager == null)
                        return false;

                return worldManager.isPaused();

        }

        public void exitApp(){
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

        public interface Resolver{
                public int getInterstitialAdState();

                public void loadInterstitialAd();

                public void showInterstitialAd();
        }
}
