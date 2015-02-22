package asf.dungeon.view;

import asf.dungeon.DungeonApp;
import asf.dungeon.LoadingScreen;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.DungeonLoader;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.SongId;
import asf.dungeon.model.token.Boulder;
import asf.dungeon.model.token.Damage;
import asf.dungeon.model.token.Decor;
import asf.dungeon.model.token.Fountain;
import asf.dungeon.model.token.PressurePlate;
import asf.dungeon.model.token.SpikeTrap;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.Torch;
import asf.dungeon.model.token.quest.SignPostQuest;
import asf.dungeon.utility.UtMath;
import asf.dungeon.view.token.AbstractTokenSpatial;
import asf.dungeon.view.token.BoulderSpatial;
import asf.dungeon.view.token.CharacterTokenSpatial;
import asf.dungeon.view.token.CrateTokenSpatial;
import asf.dungeon.view.token.DecorTokenSpatial;
import asf.dungeon.view.token.FountainTokenSpatial;
import asf.dungeon.view.token.LootTokenSpatial;
import asf.dungeon.view.token.SignPostTokenSpatial;
import asf.dungeon.view.token.SpikeTrapTokenSpatial;
import asf.dungeon.view.token.StairsSpatial;
import asf.dungeon.view.token.TorchTokenSpatial;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by Daniel Strong on 10/20/14.
 */
public class DungeonWorld implements Dungeon.Listener, Disposable {
        // shared resources
        protected final DungeonApp dungeonApp;
        protected final Settings settings;
        public final Camera cam;
        public final Environment environment;
        public final Stage stage;
        public final DecalBatch decalBatch;
        public final ModelBatch modelBatch;
        public final AssetManager assetManager;
        private final ObjectSet<LoadedNotifyable> loadables;
        private final Array<Spatial> spatials;
        private boolean loading;
        private boolean simulationStarted = false;
        private boolean paused = false;
        private final InputMultiplexer inputMultiplexer;
        // game stuff
        public final Dungeon dungeon;

        protected final CamControl camControl;
        public final AssetMappings assetMappings;
        public TextureAtlas pack;
        public I18NBundle i18n;
        public final FloorSpatial floorSpatial;
        public final HudSpatial hudSpatial;
        public final SfxManager sounds;
        public final FxManager fxManager;

        public DungeonWorld(DungeonApp dungeonApp, Settings settings) {
                this.dungeonApp = dungeonApp;
                this.settings = settings;
                camControl = new PerspectiveCamControl();
                cam = camControl.getCamera();
                environment = new Environment();
                environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.64f, 0.64f, 0.64f, 1f));
                environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
                stage = new Stage(new ScreenViewport());
                decalBatch = new DecalBatch(1000, new CameraGroupStrategy(camControl.getCamera()));
                modelBatch = new ModelBatch();
                assetManager = new AssetManager();
                assetManager.load("Packs/Game.atlas", TextureAtlas.class);
                loadables = new ObjectSet<LoadedNotifyable>(24);
                spatials = new Array<Spatial>(true, 16, Spatial.class);
                assetMappings = new AssetMappings();
                sounds = new SfxManager(this);
                fxManager = new FxManager(this);
                loading = true;


                //FileHandle baseFileHandle = Gdx.files.internal("i18n/Game/Game");
                //Locale locale = new Locale("en");
                //i18n = I18NBundle.createBundle(baseFileHandle, locale);

                hudSpatial = new HudSpatial();
                addSpatial(hudSpatial);

                InternalInputAdapter internalInput = new InternalInputAdapter();
                inputMultiplexer = new InputMultiplexer(internalInput, stage);
                Gdx.input.setInputProcessor(internalInput);

                floorSpatial = new FloorSpatial();
                addSpatial(floorSpatial);

                //addSpatial(new SkyboxSpatial());

                if (settings.loadedDungeon != null) {
                        dungeon = settings.loadedDungeon;
                        dungeon.setListener(this);
                } else {
                        dungeon = DungeonLoader.createDungeon(settings);
                        dungeon.setListener(this);
                        saveDungeon();
                }


        }


        public void notifyOnLoaded(LoadedNotifyable loadedNotifyable) {
                loadables.add(loadedNotifyable);
                loading = true;
        }

        protected <T extends Spatial> T addSpatial(T spatial) {
                spatial.preload(this);
                spatials.add(spatial);
                loading = true;
                return spatial;
        }

        protected void removeSpatial(Spatial spatial) {
                spatials.removeValue(spatial, true);
                spatial.dispose();
        }

        public Pair getMapCoords(Vector3 worldCoords, Pair storeMapCoords) {
                storeMapCoords.x = (int)((worldCoords.x ) / floorSpatial.tileDimensions.x);
                storeMapCoords.y = (int)((UtMath.abs(worldCoords.z)) / floorSpatial.tileDimensions.z);
                return storeMapCoords;
        }

        public Pair getMapCoords(float worldX, float worldY, float worldZ, Pair storeMapCoords) {
                storeMapCoords.x = (int)((worldX ) / floorSpatial.tileDimensions.x);
                storeMapCoords.y = (int)((UtMath.abs(worldZ)) / floorSpatial.tileDimensions.z);
                return storeMapCoords;
        }

        public Vector3 getWorldCoords(float mapCoordsX, float mapCoordsY, Vector3 storeWorldCoords) {
                storeWorldCoords.x =( (mapCoordsX * floorSpatial.tileDimensions.x) ) + (floorSpatial.tileDimensions.x /2f);
                storeWorldCoords.y = 0;
                storeWorldCoords.z =(-(mapCoordsY * floorSpatial.tileDimensions.z) ) - (floorSpatial.tileDimensions.z/2f);
                return storeWorldCoords;
        }

        public Vector3 getWorldCoords(Vector2 mapCoords, Vector3 storeWorldCoords) {
                return getWorldCoords(mapCoords.x, mapCoords.y, storeWorldCoords);
        }

        public Vector3 getWorldCoords(Pair mapCoords, Vector3 storeWorldCoords) {
                return getWorldCoords(mapCoords.x, mapCoords.y, storeWorldCoords);
        }

        public void getScreenCoords(Vector2 mapCoords, Vector3 storeScreenCoords) {
                cam.project(getWorldCoords(mapCoords.x, mapCoords.y, storeScreenCoords));
        }

        public void getScreenCoords(float mapCoordsX, float mapCoordsY, Vector3 storeScreenCoords) {
                cam.project(getWorldCoords(mapCoordsX, mapCoordsY, storeScreenCoords));
        }

        protected Token getLocalPlayerToken() {
                return hudSpatial.localPlayerToken;
        }

        public AbstractTokenSpatial getTokenSpatial(Token token) {
                for (Spatial spatial : spatials) {
                        if (spatial instanceof AbstractTokenSpatial) {
                                AbstractTokenSpatial ts = (AbstractTokenSpatial) spatial;
                                if (ts.getToken() == token) {
                                        return ts;
                                }
                        }
                }
                return null;
        }

        public Token getToken(Ray ray, Token ignoreToken) {
                Token result = null;
                float closestDist2 = Float.MAX_VALUE;

                for (Spatial spatial : spatials) {
                        if (!spatial.isInitialized())
                                continue;
                        if (spatial instanceof AbstractTokenSpatial) {
                                AbstractTokenSpatial tokenSpatial = (AbstractTokenSpatial) spatial;

                                if (tokenSpatial.getToken() == ignoreToken) {
                                        continue;
                                }

                                Damage damage = tokenSpatial.getToken().damage;
                                if(damage != null && damage.isDead())
                                        continue;

                                final float dist2 = tokenSpatial.intersects(ray);
                                if (dist2 >= 0f && dist2 < closestDist2) {
                                        result = tokenSpatial.getToken();
                                        closestDist2 = dist2;
                                }

                        }
                }

                return result;
        }

        private final Array<RaycastResult> intersectedTokens=  new Array<RaycastResult>(true, 8, RaycastResult.class);
        public static class RaycastResult{
                public Token token;
                public float dist2;

                public RaycastResult(Token token, float dist2) {
                        this.token = token;
                        this.dist2 = dist2;
                }
        }
        public Array<RaycastResult> getTokens(Ray ray, Token ignoreToken){
                intersectedTokens.clear();
                for (int i = 0; i < spatials.size; i++) {
                        Spatial spatial = spatials.items[i];
                        if (!spatial.isInitialized())
                                continue;
                        if (spatial instanceof AbstractTokenSpatial) {
                                AbstractTokenSpatial tokenSpatial = (AbstractTokenSpatial) spatial;

                                if (tokenSpatial.getToken() == ignoreToken) {
                                        continue;
                                }

                                Damage damage = tokenSpatial.getToken().damage;
                                if(damage != null && damage.isDead())
                                        continue;

                                final float dist2 = tokenSpatial.intersects(ray);
                                if(dist2 >= 0f){
                                        intersectedTokens.add(new RaycastResult(tokenSpatial.getToken(), dist2));
                                }
                        }
                }
                return intersectedTokens;
        }

        protected void goToLoadingScreen(){
                if(!simulationStarted) return;
                dungeonApp.setScreen(new LoadingScreen(dungeonApp));
                Gdx.input.setInputProcessor(null);
        }

        protected void returnFromLoadingScreen(){
                dungeonApp.setScreen(null);
                Gdx.input.setInputProcessor(inputMultiplexer);
                Gdx.gl.glClearColor(0.01f, 0.01f, 0.01f, 1);
        }

        public void render(final float delta) {

                if (loading) {
                        if(paused)
                                Gdx.graphics.requestRendering();
                        if (assetManager.update()) {
                                loading = false;
                                if (!simulationStarted) {
                                        if (settings.startDebugSession) {
                                                if(dungeonApp.getPlatformActionResolver()!=null)
                                                        dungeonApp.getPlatformActionResolver().showDebugWindow();
                                        }

                                        //Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                                        pack =  assetManager.get("Packs/Game.atlas", TextureAtlas.class);
                                        sounds.init();
                                        fxManager.init();
                                        simulationStarted = true;

                                        dungeonApp.onSimulationStarted(); // inform the dungeon app to close the loading screen
                                        returnFromLoadingScreen();
                                        setPaused(false); // call this to apply the gameplay input processors

                                        dungeonApp.music.setPlaylist(SongId.MainTheme, SongId.Arabesque, SongId.RitualNorm);
                                        dungeonApp.music.playSong(SongId.RitualNorm);

                                        //assetMappings.preload3dModels(assetManager); // load the remaining assets while we play the game
                                }

                                final Iterator<LoadedNotifyable> i = loadables.iterator();
                                while (i.hasNext()) {
                                        final LoadedNotifyable next = i.next();
                                        if (next.onLoaded()) {
                                                i.remove();
                                        } else {
                                                loading = true;
                                        }
                                }

                                for (final Spatial spatial : spatials) {
                                        if (!spatial.isInitialized())
                                                spatial.init(assetManager);
                                }


                        }
                }


                if (simulationStarted && floorSpatial.isInitialized()) {
                        if (!paused) {
                                hudSpatial.updateInput(delta);
                                dungeon.update(delta);
                                for (final Spatial spatial : spatials) {
                                        if (spatial.isInitialized())
                                                spatial.update(delta);
                                }
                                camControl.update(delta);
                        }

                        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

                        modelBatch.begin(cam);
                        fxManager.beginRender();

                        final float effectiveDelta = paused ? 0 : delta;
                        for (final Spatial spatial : spatials) {
                                if (spatial.isInitialized())
                                        spatial.render(effectiveDelta);
                        }


                        modelBatch.end();
                        decalBatch.flush();
                        fxManager.endRender();

                        stage.draw();
                }

        }

        /**
         * when paused the renderManager stops calling update() but continues to call render()
         *
         * @param paused
         */
        public void setPaused(boolean paused) {
                if (!paused && Gdx.input.getInputProcessor() != inputMultiplexer) {
                        // the pause menu screen changes the input processor, so we need to change it
                        // back when unpaussing
                        Gdx.input.setInputProcessor(inputMultiplexer);
                }


                // the || statement ensure if libgdx "unpauses" the game but the inventory window
                // is open that the game remains paused
                this.paused = paused || (hudSpatial != null && hudSpatial.isWindowVisible());

                sounds.setPaused(this.paused);

                if(Gdx.graphics.isContinuousRendering() == this.paused)
                        Gdx.graphics.setContinuousRendering(!this.paused);
        }

        public boolean isPaused() {
                return paused;
        }

        public void resize(int width, int height) {
                stage.getViewport().update(width, height, true);
                hudSpatial.resize(width, height);
                camControl.resize(width, height);
        }

        @Override
        public void dispose() {
                for (Spatial spatial : spatials) {
                        spatial.dispose();
                }
                decalBatch.dispose();
                modelBatch.dispose();
                sounds.dispose();
                fxManager.dispose();
                assetManager.dispose();
                stage.dispose();
                Gdx.input.setInputProcessor(null);
        }

        public void saveDungeon() {
                try {
                        DungeonLoader.saveDungeon(dungeon, settings.getSaveFileName());
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        @Override
        public void onNewPlayerToken(Token playerToken) {
                if (playerToken != null) {
                        inputMultiplexer.addProcessor(hudSpatial);
                        hudSpatial.setToken(playerToken);
                } else {
                        inputMultiplexer.removeProcessor(hudSpatial);
                        hudSpatial.setToken(null); // player died, remove him from the hud
                }

        }

        @Override
        public void onFloorMapChanged(FloorMap newFloorMap) {
                fxManager.clearAll();
                floorSpatial.setFloorMap(newFloorMap);
        }

        @Override
        public void onTokenAdded(Token token) {
                if (token == hudSpatial.localPlayerToken) {
                        // dont re add the token, he already has a token spatial.
                } else if(token.logic != null){
                        addSpatial(new CharacterTokenSpatial(DungeonWorld.this, token));
                }else if(token.loot != null){
                        addSpatial(new LootTokenSpatial(DungeonWorld.this, token));
                }else if(token.crateInventory != null){
                        addSpatial(new CrateTokenSpatial(DungeonWorld.this, token));
                }else if(token.get(SpikeTrap.class) != null){
                        addSpatial(new SpikeTrapTokenSpatial(DungeonWorld.this, token));
                }else if(token.get(Fountain.class) != null){
                        addSpatial(new FountainTokenSpatial(DungeonWorld.this, token));
                }else if(token.get(Torch.class) != null){
                        addSpatial(new TorchTokenSpatial(DungeonWorld.this, token));
                }else if(token.get(SignPostQuest.class) != null){
                        addSpatial(new SignPostTokenSpatial(DungeonWorld.this, token));
                }else if(token.stairs != null){
                        addSpatial(new StairsSpatial(DungeonWorld.this, token));
                }else if(token.get(Boulder.class) != null){
                        addSpatial(new BoulderSpatial(DungeonWorld.this, token));
                }else if(token.get(PressurePlate.class) != null){
                        // Pressure plate has no dedicated spatial,  it is incorporated with FloorSpatial
                }else if(token.get(Decor.class) != null){
                        addSpatial(new DecorTokenSpatial(DungeonWorld.this, token));
                }else{
                        //addSpatial(new DecorTokenSpatial(DungeonWorld.this, token));
                        throw new AssertionError(token);
                }

        }

        @Override
        public void onTokenRemoved(Token token) {
                if (token == hudSpatial.localPlayerToken) {
                        if (!token.damage.isFullyDead()) {
                                camControl.getChaseTarget().visU = 0; // this forces the player spatial to turn black and fade back in
                        } else {
                                //dungeonApp.setAppGameOver();
                        }

                        return;
                }

                AbstractTokenSpatial removeSpatial = getTokenSpatial(token);

                if (removeSpatial != null)
                        removeSpatial(removeSpatial);
        }


        private class InternalInputAdapter extends InputAdapter {

                // The internal input adapter only processes input during the loading phase

                @Override
                public boolean keyDown(int keycode) {
                        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                                        dungeonApp.exitApp();
                                }
                                return true;
                        }
                        return false;
                }

                @Override
                public boolean keyUp(int keycode) {
                        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                                dungeonApp.setAppPaused(true);
                                return true;
                        } else if (keycode == Input.Keys.F12) {
                                if(dungeonApp.getPlatformActionResolver()!=null)
                                        dungeonApp.getPlatformActionResolver().showDebugWindow();
                                return true;
                        }
                        return false;
                }

        }



        public interface LoadedNotifyable {
                /**
                 * @return true if the asset that was needed was obtained, false otherwise
                 */
                public boolean onLoaded();
        }

        public static class Settings {
                public boolean balanceTest = false;
                public Random random;
                public Dungeon loadedDungeon;
                public ModelId playerModel;
                public boolean startDebugSession;

                private String getSaveFileName() {
                        return playerModel.name();
                }

                public void loadDungeon() {
                        loadedDungeon = DungeonLoader.loadDungeon(getSaveFileName());
                }

                public Token getLocalPlayerToken() {
                        if (loadedDungeon == null)
                                return null;
                        return loadedDungeon.localPlayerToken;
                }
        }
}
