package asf.dungeon.view;

import asf.dungeon.DungeonApp;
import asf.dungeon.board.CharacterToken;
import asf.dungeon.board.CrateToken;
import asf.dungeon.board.DamageableToken;
import asf.dungeon.board.Dungeon;
import asf.dungeon.board.LootToken;
import asf.dungeon.board.Token;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import asf.dungeon.board.factory.DungeonFactory;
import asf.dungeon.board.logic.LocalPlayerLogicProvider;
import asf.dungeon.board.pathfinder.Pair;
import asf.dungeon.view.shape.Box;
import asf.dungeon.utility.ModelFactory;

/**
 * Created by danny on 10/20/14.
 */
public class DungeonWorld implements Disposable{
        // shared resources
        private DungeonApp dungeonApp;
        public PerspectiveCamera cam;
        protected Environment environment;
        protected final Stage stage;
        protected final DecalBatch decalBatch;
        protected final ModelBatch modelBatch;
        protected final AssetManager assetManager;
        protected final Array<Spatial> spatials;
        private boolean loading;
        private boolean simulationStarted = false;
        private boolean paused = false;
        // game stuff
        public Dungeon dungeon;


        private FloorSpatial floorDecals;
        public SelectionMark selectionMark;

        public CharacterToken localPlayerToken;
        private TokenSpatial cameraChaseTarget;

        private Label label;

        private InternalInputAdapter internalInput;

        public DungeonWorld(DungeonApp dungeonApp) {
                this.dungeonApp = dungeonApp;
                cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                cam.position.set(0f, 35f, 10f);
                cam.lookAt(0, 0, 2.5f);
                cam.near = .1f;
                cam.far = 300f;
                cam.update();
                environment = new Environment();
                environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
                environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
                stage = new Stage();
                CameraGroupStrategy cameraGroupStrategy = new CameraGroupStrategy(cam);
                decalBatch = new DecalBatch(cameraGroupStrategy);
                modelBatch = new ModelBatch();
                assetManager = new AssetManager();
                spatials = new Array<Spatial>();
                loading = true;


                floorDecals = new FloorSpatial();
                addSpatial(floorDecals);

                internalInput = new InternalInputAdapter();
                Gdx.input.setInputProcessor(new InputMultiplexer(internalInput));
                addSpatial(new ActorSpatial("Models/skydome.g3db", new Box(), null));

                selectionMark = addSpatial(new ActorSpatial(new ModelInstance(ModelFactory.box(5, 1, 5, Color.RED)), new Box(), environment)).setControl(new SelectionMark(this));
                dungeon = DungeonFactory.dungeon(internalInput);
                //Texture texture = new Texture(Gdx.files.internal("Models/tiles.png"));
                //TextureRegion[][] tiles = TextureRegion.split(texture, 32, 32);

                BitmapFont font = new BitmapFont();
                label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
                stage.addActor(label);

        }

        public <T extends Spatial> T addSpatial(T spatial){
                spatial.preload(this);
                spatials.add(spatial);
                loading = true;
                return spatial;
        }

        public void removeSpatial(Spatial spatial){
                spatials.removeValue(spatial,true);
                spatial.dispose();
        }

        public Pair getMapCoords(Vector3 worldCoords, Pair storeMapCoords) {
                return floorDecals.getMapCoords(worldCoords, storeMapCoords);
        }

        public Vector3 getWorldCoords(float mapCoordsX, float mapCoordsY, Vector3 storeWorldCoords) {
                return floorDecals.getWorldCoords(mapCoordsX, mapCoordsY, storeWorldCoords);
        }

        public Vector3 getWorldCoords(Pair mapCoords, Vector3 storeWorldCoords) {
                return floorDecals.getWorldCoords(mapCoords, storeWorldCoords);
        }

        public Token getToken (Ray ray, Token ignoreToken) {
                Token result = null;
                float distance = -1;

                for(Spatial spatial : spatials){
                        if(spatial instanceof TokenSpatial){
                                TokenSpatial tokenSpatial = (TokenSpatial) spatial;
                                Token targetToken = tokenSpatial.getControl().getToken();
                                if(targetToken == ignoreToken){
                                        continue;
                                }

                                if(targetToken instanceof DamageableToken){
                                        final float dist2 = tokenSpatial.intersects(ray);
                                        if (dist2 >= 0f && (distance < 0f || dist2 <= distance)) {
                                                result = targetToken;
                                                distance = dist2;
                                        }
                                }

                        }
                }

                return result;
        }

        private void onSimulationStarted(){
                Gdx.gl.glClearColor(0.01f, 0.01f, 0.01f, 1);
        }

        private void update(final float delta){
                dungeon.update(delta);

                label.setText("FPS : " + Gdx.graphics.getFramesPerSecond() + " Knight: " + localPlayerToken.getHealth());

                if (cameraChaseTarget != null) {
                        // TODO: this is actually moving the camera before updating the chaseCamTarget
                        // there doesnt seem to be any noticable effects right now so leave it as it is
                        cam.position.set(cameraChaseTarget.translation.x + 0f, 35f, cameraChaseTarget.translation.z + 10f);

                        //cam.position.set(cameraChaseTarget.translation.x + 0f, 10f, cameraChaseTarget.translation.z + 10f);
                        //cam.lookAt(cameraChaseTarget.translation.x, 0, cameraChaseTarget.translation.z+2.5f);
                        cam.update();
                }
        }

        public void render(final float delta){
                if(loading){
                        if(assetManager.update() && !paused){
                                loading = false;
                                if(!simulationStarted){
                                        Gdx.gl.glClearColor(0, 0, 0, 1);
                                        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                                        onSimulationStarted();
                                        simulationStarted = true;

                                        dungeonApp.onSimulationStarted();
                                }

                                for (final Spatial spatial : spatials) {
                                        if(!spatial.isInitialized())
                                                spatial.init(assetManager);
                                }
                        }
                }

                if(simulationStarted){
                        if(!paused){
                                update(delta);
                                for (final Spatial spatial : spatials) {
                                        if(spatial.isInitialized())
                                                spatial.update(delta);
                                }
                        }

                        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
                        modelBatch.begin(cam);
                        for (final Spatial spatial : spatials) {
                                if(spatial.isInitialized())
                                        spatial.render(paused ? 0 : delta);

                        }
                        modelBatch.end();

                        decalBatch.flush();

                        stage.draw();
                }else{

                }


        }

        /**
         * when paused the renderManager stops calling update() but continues to call render()
         * @param paused
         */
        public void setPaused(boolean paused){
                this.paused = paused;
        }

        public boolean isPaused(){
                return paused;
        }

        public void resize(int width, int height){
                stage.getViewport().update(width,height,true);

                //spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
                cam.viewportWidth = width;
                cam.viewportHeight = height;
                cam.update();

        }

        @Override
        public void dispose() {
                for(Spatial spatial : spatials){
                        spatial.dispose();
                }
                decalBatch.dispose();
                modelBatch.dispose();
                assetManager.dispose();
                stage.dispose();
                Gdx.input.setInputProcessor(null);
        }

        private class InternalInputAdapter extends InputAdapter implements Dungeon.Listener {

                @Override
                public boolean scrolled(int amount) {
                        return false;
                }

                @Override
                public boolean keyUp(int keycode) {
                        if (isPaused())
                                return false;

                        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {

                                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                                        dungeonApp.exitApp();
                                } else {
                                        dungeonApp.setWorldPaused(!dungeonApp.isWorldPaused());
                                }
                                return true;
                        }

                        return false;
                }



                @Override
                public void onTokenAdded(Token token) {

                        if (token == localPlayerToken) {
                                Gdx.app.log("DungeonWorld", "moving view to move with the player token");
                                floorDecals.setFloorMap(localPlayerToken.getFloorMap());
                        } else if (token instanceof CharacterToken) {
                                CharacterToken characterToken = (CharacterToken) token;
                                TokenSpatial tokenSpatial = addSpatial(new TokenSpatial(DungeonWorld.this, characterToken, "Models/Characters/" + token.getName() + ".g3db", floorDecals.tokenCustomBox, environment));
                                if (characterToken.getLogicProvider() instanceof LocalPlayerLogicProvider) {
                                        Gdx.app.log("DungeonWorld", "Local character token is added");
                                        LocalPlayerLogicProvider localLogic = (LocalPlayerLogicProvider) characterToken.getLogicProvider();
                                        if (localLogic.getId() == 0) {
                                                localLogic.setWorld(DungeonWorld.this);
                                                localPlayerToken = characterToken;
                                                cameraChaseTarget = tokenSpatial;
                                                Gdx.input.setInputProcessor(new InputMultiplexer(internalInput, (LocalPlayerLogicProvider) localPlayerToken.getLogicProvider()));
                                                floorDecals.setFloorMap(localPlayerToken.getFloorMap());

                                        }
                                }
                        } else if (token instanceof CrateToken) {
                                CrateToken crateToken = (CrateToken) token;
                                addSpatial(new TokenSpatial(DungeonWorld.this, crateToken, "Models/Crates/" + crateToken.getName() + ".g3db", floorDecals.tokenCustomBox, environment));
                        } else if (token instanceof LootToken) {
                                LootToken lootToken = (LootToken) token;
                                addSpatial(new TokenSpatial(DungeonWorld.this, lootToken, "Models/Loot/" + lootToken.getName() + ".g3db", floorDecals.tokenCustomBox, environment));
                        }

                }

                @Override
                public void onTokenRemoved(Token token) {
                        Gdx.app.log("DungeonWorld","tokenRemoved: "+token);
                        if (token == localPlayerToken) {
                                dungeon.setCurrentFloor(localPlayerToken.getFloorMap());
                                return;
                        }
                        Spatial removeSpatial = null;
                        for (Spatial spatial : spatials) {
                                if (spatial instanceof TokenSpatial) {
                                        TokenSpatial tokenSpatial = (TokenSpatial) spatial;
                                        if (tokenSpatial.getControl().getToken() == token) {
                                                tokenSpatial.getControl().end();
                                                removeSpatial = spatial;
                                                break;
                                        }
                                }
                        }
                        if (removeSpatial != null)
                                removeSpatial(removeSpatial);
                }
        }
}
