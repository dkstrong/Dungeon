package asf.dungeon.view;

import asf.dungeon.DungeonApp;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.DungeonLoader;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.token.Damage;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.ModelFactory;
import asf.dungeon.view.shape.Box;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.IOException;
import java.util.Random;

/**
 * Created by danny on 10/20/14.
 */
public class DungeonWorld implements Disposable {
        // shared resources
        protected final DungeonApp dungeonApp;
        protected final Settings settings;
        protected final Camera cam;
        protected final Environment environment;
        protected final Stage stage;
        protected final DecalBatch decalBatch;
        protected final ModelBatch modelBatch;
        protected final AssetManager assetManager;
        private final Array<Spatial> spatials;
        private final InternalInputAdapter internalInput;
        private boolean loading;
        private boolean simulationStarted = false;
        private boolean paused = false;
        private final InputMultiplexer inputMultiplexer;
        // game stuff
        protected Dungeon dungeon;

        protected final CamControl camControl;
        protected AssetMappings assetMappings;
        protected I18NBundle i18n;
        protected FloorSpatial floorSpatial;
        protected SelectionMark selectionMark;
        protected HudSpatial hudSpatial;
        protected FxManager fxManager;

        public DungeonWorld(DungeonApp dungeonApp, Settings settings) {
                this.dungeonApp = dungeonApp;
                this.settings = settings;
                camControl = new CamControl();
                cam = camControl.cam;
                environment = new Environment();
                environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.64f, 0.64f, 0.64f, 1f));
                environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
                stage = new Stage(new ScreenViewport());
                decalBatch = new DecalBatch(1000, new CameraGroupStrategy(camControl.cam));
                modelBatch = new ModelBatch();
                assetManager = new AssetManager();
                spatials = new Array<Spatial>(true, 16, Spatial.class);
                fxManager = new FxManager(this);
                assetMappings = new AssetMappings();
                loading = true;

                //FileHandle baseFileHandle = Gdx.files.internal("i18n/Game/Game");
                //Locale locale = new Locale("en");
                //i18n = I18NBundle.createBundle(baseFileHandle, locale);

                hudSpatial = new HudSpatial();
                addSpatial(hudSpatial);

                internalInput = new InternalInputAdapter();
                inputMultiplexer = new InputMultiplexer(internalInput, stage);
                Gdx.input.setInputProcessor(internalInput);

                floorSpatial = new FloorSpatial();
                addSpatial(floorSpatial);


                //addSpatial(new ActorSpatial("Models/skydome.g3db", null, null));

                selectionMark = addSpatial(new GenericSpatial(new ModelInstance(ModelFactory.box(5, 1, 5, Color.RED)), new Box(), environment)).setControl(new SelectionMark(this));

                if (settings.loadedDungeon != null) {
                        dungeon = settings.loadedDungeon;
                        dungeon.setListener(internalInput);
                } else {
                        dungeon = DungeonLoader.createDungeon(settings);
                        dungeon.setListener(internalInput);
                        //saveDungeon();
                }

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
                return floorSpatial.getMapCoords(worldCoords, storeMapCoords);
        }

        public Vector3 getWorldCoords(float mapCoordsX, float mapCoordsY, Vector3 storeWorldCoords) {
                return floorSpatial.getWorldCoords(mapCoordsX, mapCoordsY, storeWorldCoords);
        }

        public Vector3 getWorldCoords(Pair mapCoords, Vector3 storeWorldCoords) {
                return floorSpatial.getWorldCoords(mapCoords, storeWorldCoords);
        }

        public void getScreenCoords(float mapCoordsX, float mapCoordsY, Vector3 storeScreenCoords) {
                camControl.cam.project(getWorldCoords(mapCoordsX, mapCoordsY, storeScreenCoords));
        }

        protected Token getLocalPlayerToken() {
                return hudSpatial.localPlayerToken;
        }

        protected TokenSpatial getTokenSpatial(Token token) {
                for (Spatial spatial : spatials) {
                        if (spatial instanceof TokenSpatial) {
                                TokenSpatial ts = (TokenSpatial) spatial;
                                if (ts.getToken() == token) {
                                        return ts;
                                }
                        }
                }
                return null;
        }

        public Token getToken(Ray ray, Token ignoreToken) {
                Token result = null;
                float distance = -1;

                for (Spatial spatial : spatials) {
                        if (!spatial.isInitialized())
                                continue;
                        if (spatial instanceof TokenSpatial) {
                                TokenSpatial tokenSpatial = (TokenSpatial) spatial;

                                if (tokenSpatial.getToken() == ignoreToken) {
                                        continue;
                                }

                                Damage damage = tokenSpatial.getToken().getDamage();
                                if (damage != null) {
                                        if (damage.isDead())
                                                continue;

                                        final float dist2 = tokenSpatial.intersects(ray);
                                        if (dist2 >= 0f && (distance < 0f || dist2 <= distance)) {
                                                result = tokenSpatial.getToken();
                                                distance = dist2;
                                        }
                                }

                        }
                }

                return result;
        }

        public void render(final float delta) {
                if (loading && assetManager.update() && !paused) {
                        loading = false;
                        if (!simulationStarted) {
                                Gdx.gl.glClearColor(0.01f, 0.01f, 0.01f, 1);
                                //Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                                fxManager.init();
                                simulationStarted = true;

                                dungeonApp.onSimulationStarted(); // inform the dungeon app to close the loading screen
                                Gdx.input.setInputProcessor(inputMultiplexer);
                                setPaused(false); // call this to apply the gameplay input processors
                        }

                        for (final Spatial spatial : spatials) {
                                if (!spatial.isInitialized())
                                        spatial.init(assetManager);
                        }
                }


                if (simulationStarted) {
                        if (!paused) {
                                hudSpatial.updateInput(delta);
                                if(!paused){
                                        dungeon.update(delta);
                                        for (final Spatial spatial : spatials) {
                                                if (spatial.isInitialized())
                                                        spatial.update(delta);
                                        }
                                        camControl.update(delta);
                                }
                        }

                        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

                        modelBatch.begin(camControl.cam);
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
                } else {

                }


        }

        /**
         * when paused the renderManager stops calling update() but continues to call render()
         *
         * @param paused
         */
        public void setPaused(boolean paused) {
                this.paused = paused;


                if (!paused) {
                        Gdx.input.setInputProcessor(inputMultiplexer);
                }

                if (!paused && hudSpatial != null && hudSpatial.isWindowVisible()) {
                        this.paused = true; // this shouldnt ever happen, but in the event it does i have a back up check here to make sure the game isnt unpaused while window is up
                }

                Gdx.graphics.setContinuousRendering(!paused);
        }

        public boolean isPaused() {
                return paused;
        }

        public void resize(int width, int height) {
                stage.getViewport().update(width, height, true);
                hudSpatial.resize(width, height);
                camControl.resize(width,height);
        }

        @Override
        public void dispose() {
                for (Spatial spatial : spatials) {
                        spatial.dispose();
                }
                decalBatch.dispose();
                modelBatch.dispose();
                fxManager.dispose();
                assetManager.dispose();
                stage.dispose();
                Gdx.input.setInputProcessor(null);
        }

        private class InternalInputAdapter extends InputAdapter implements Dungeon.Listener {

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
                        }
                        return false;
                }

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
                        floorSpatial.setFloorMap(newFloorMap);
                }

                @Override
                public void onTokenAdded(Token token) {
                        if (token == hudSpatial.localPlayerToken) {
                                // dont re add the token, he already has a token spatial.
                        } else {
                                addSpatial(new TokenSpatial(DungeonWorld.this, token));
                        }

                }

                @Override
                public void onTokenRemoved(Token token) {
                        if (token == hudSpatial.localPlayerToken) {
                                if(!token.getDamage().isFullyDead()){
                                        camControl.chaseTarget.visU = 0; // this forces the player spatial to turn black and fade back in
                                }else{
                                        //dungeonApp.setAppGameOver();
                                }

                                return;
                        }

                        TokenSpatial removeSpatial = getTokenSpatial(token);

                        if (removeSpatial != null)
                                removeSpatial(removeSpatial);
                }
        }

        public void saveDungeon() {
                try {
                        DungeonLoader.saveDungeon(dungeon, settings.getSaveFileName());
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public static class Settings {
                public boolean balanceTest = false;
                public Random random;
                public Dungeon loadedDungeon;
                public ModelId playerModel;

                private String getSaveFileName() {
                        return playerModel.name();
                }

                public void loadDungeon() {
                        loadedDungeon = DungeonLoader.loadDungeon(getSaveFileName());
                }

                public Token getLocalPlayerToken() {
                        if (loadedDungeon == null)
                                return null;
                        return loadedDungeon.getLocalPlayerToken();
                }
        }
}
