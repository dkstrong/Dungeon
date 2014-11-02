package asf.dungeon;

import asf.dungeon.view.DungeonWorld;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public class DungeonApp implements ApplicationListener {


        private Screen screen;
        private DungeonWorld worldManager;
        private Resolver platformActionResolver;

        @Override
        public void create() {
                //Gdx.input.setCatchMenuKey(true);
                //Gdx.input.setCatchBackKey(true);

                //this.setScreen(new MainMenuScreen(this));
                loadWorld();
        }

        @Override
        public void resize(int width, int height) {
                if(worldManager != null)
                        worldManager.resize(width, height);
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
                        this.screen.show();
                        this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                }
        }


        protected Screen createLoadingScreen(){
                return new LoadingScreen(this);
        }

        protected Screen createPauseScreen(){
                return new PauseScreen(this);
        }

        public void loadWorld(){
                if(worldManager != null)
                        throw new IllegalStateException("world is already loaded");

                Gdx.graphics.setContinuousRendering(true);
                worldManager = new DungeonWorld(this);
                //worldManager.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

                setScreen(createLoadingScreen());

        }

        public void onSimulationStarted() {
                setScreen(null);
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
