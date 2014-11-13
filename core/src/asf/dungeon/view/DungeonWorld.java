package asf.dungeon.view;

import asf.dungeon.DungeonApp;
import asf.dungeon.utility.DungeonLoader;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.factory.BinarySpaceGen;
import asf.dungeon.model.factory.CellularAutomataGen;
import asf.dungeon.model.factory.ConnectedRoomsGen;
import asf.dungeon.model.factory.DirectionalCaveHallGen;
import asf.dungeon.model.factory.FloorMapGenMultiplexer;
import asf.dungeon.model.factory.MazeGen;
import asf.dungeon.model.factory.PreBuiltFloorGen;
import asf.dungeon.model.factory.RandomWalkGen;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.factory.FloorMapGenerator;
import asf.dungeon.model.token.Damage;
import asf.dungeon.model.token.logic.LocalPlayerLogicProvider;
import asf.dungeon.model.Pair;
import asf.dungeon.utility.ModelFactory;
import asf.dungeon.view.shape.Box;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
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
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.IOException;

/**
 * Created by danny on 10/20/14.
 */
public class DungeonWorld implements Disposable {
        // shared resources
        protected final DungeonApp dungeonApp;
        protected final PerspectiveCamera cam;
        protected final Environment environment;
        protected final Stage stage;
        protected final DecalBatch decalBatch;
        protected final ModelBatch modelBatch;
        protected final AssetManager assetManager;
        private final Array<ProjectileSpatial> projectileSpatialPool;
        private final Array<Spatial> spatials;
        private final InternalInputAdapter internalInput;
        private boolean loading;
        private boolean simulationStarted = false;
        private boolean paused = false;
        private final InputMultiplexer inputMultiplexer;
        // game stuff
        protected Dungeon dungeon;

        private FloorSpatial floorDecals;
        protected SelectionMark selectionMark;
        private HudSpatial hudSpatial;

        private TokenSpatial cameraChaseTarget;
        private final Vector3 chaseCamOffset = new Vector3();

        public DungeonWorld(DungeonApp dungeonApp) {
                this.dungeonApp = dungeonApp;
                cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                cam.position.set(0f, 35f, 10f);   // 0.5f
                cam.lookAt(0, 0, 0);   //  -2.5f
                cam.near = .1f;
                cam.far = 300f;
                cam.update();
                chaseCamOffset.set(cam.position);
                environment = new Environment();
                environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.64f, 0.64f, 0.64f, 1f));
                environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
                stage = new Stage(new ScreenViewport());
                decalBatch = new DecalBatch(1000, new CameraGroupStrategy(cam));
                modelBatch = new ModelBatch();
                assetManager = new AssetManager();
                projectileSpatialPool = new Array<ProjectileSpatial>(false, 16, ProjectileSpatial.class);
                spatials = new Array<Spatial>(true, 16, Spatial.class);
                loading = true;

                internalInput = new InternalInputAdapter();

                hudSpatial = new HudSpatial();
                addSpatial(hudSpatial);

                inputMultiplexer = new InputMultiplexer(stage, hudSpatial);
                Gdx.input.setInputProcessor(internalInput);

                floorDecals = new FloorSpatial();
                addSpatial(floorDecals);

                //addSpatial(new ActorSpatial("Models/skydome.g3db", null, null));

                selectionMark = addSpatial(new GenericSpatial(new ModelInstance(ModelFactory.box(5, 1, 5, Color.RED)), new Box(), environment)).setControl(new SelectionMark(this));
                addSpatial(new ProjectileSpatial(this, environment));

                //dungeon = DungeonLoader.loadDungeon(internalInput);

                if(dungeon == null){
                        FloorMapGenMultiplexer floorMapGenMultiplexer = new FloorMapGenMultiplexer(new FloorMapGenerator[]{
                                new PreBuiltFloorGen(), new ConnectedRoomsGen(), new BinarySpaceGen(), new DirectionalCaveHallGen(), new RandomWalkGen(), new CellularAutomataGen(),
                                new PreBuiltFloorGen(),
                                new ConnectedRoomsGen(),new MazeGen(7,4),new ConnectedRoomsGen(),new MazeGen(15,18)
                        },new FloorMapGenerator[]{
                                new ConnectedRoomsGen(), new MazeGen(10,10)
                        });

                        dungeon = DungeonLoader.createDungeon(internalInput, floorMapGenMultiplexer);  // this triggers the creation of various TokenSpatials...
                        dungeon.setCurrentFloor(0);
                        try {
                                DungeonLoader.saveDungeon(dungeon);
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                }else{

                }





        }

        private <T extends Spatial> T addSpatial(T spatial) {
                spatial.preload(this);
                spatials.add(spatial);
                loading = true;
                if(spatial instanceof ProjectileSpatial){
                        projectileSpatialPool.add((ProjectileSpatial) spatial);
                }
                return spatial;
        }

        private void removeSpatial(Spatial spatial) {
                spatials.removeValue(spatial, true);
                if(spatial instanceof ProjectileSpatial){
                        projectileSpatialPool.removeValue((ProjectileSpatial)spatial, true);
                }
                spatial.dispose();
        }

        protected void shootProjectile(Token source, Token target){
                for (ProjectileSpatial projectileSpatial : projectileSpatialPool) {
                        if(!projectileSpatial.isActive()){
                                projectileSpatial.shootProjectile(source, target);
                                return;
                        }
                }

                ProjectileSpatial projectileSpatial = new ProjectileSpatial(this, environment);
                addSpatial(projectileSpatial);
                projectileSpatial.shootProjectile(source, target);
        }

        protected void shootProjectile(Token source, Pair destLoc){
                for (ProjectileSpatial projectileSpatial : projectileSpatialPool) {
                        if(!projectileSpatial.isActive()){
                                projectileSpatial.shootProjectile(source, destLoc);
                                return;
                        }
                }

                ProjectileSpatial projectileSpatial = new ProjectileSpatial(this, environment);
                addSpatial(projectileSpatial);
                projectileSpatial.shootProjectile(source, destLoc);
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

        public void getScreenCoords(float mapCoordsX, float mapCoordsY, Vector3 storeScreenCoords){
                getWorldCoords(mapCoordsX, mapCoordsY, storeScreenCoords);
                cam.project(storeScreenCoords);
        }

        protected Token getLocalPlayerToken(){
                return hudSpatial.localPlayerToken;
        }
        protected HudSpatial getHud(){return hudSpatial;}
        protected TokenSpatial getTokenSpatial(Token token){
                for (Spatial spatial : spatials) {
                        if(spatial instanceof TokenSpatial){
                                TokenSpatial ts = (TokenSpatial ) spatial;
                                if(ts.token == token){
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
                        if(!spatial.isInitialized())
                                continue;
                        if (spatial instanceof TokenSpatial) {
                                TokenSpatial tokenSpatial = (TokenSpatial) spatial;

                                if (tokenSpatial.token == ignoreToken) {
                                        continue;
                                }

                                Damage damage = tokenSpatial.token.get(Damage.class);
                                if (damage != null) {
                                        if(damage.isDead())
                                                continue;

                                        final float dist2 = tokenSpatial.intersects(ray);
                                        if (dist2 >= 0f && (distance < 0f || dist2 <= distance)) {
                                                result = tokenSpatial.token;
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
                                dungeon.update(delta);
                                for (final Spatial spatial : spatials) {
                                        if (spatial.isInitialized())
                                                spatial.update(delta);
                                }

                                if (cameraChaseTarget != null) {
                                        cam.position.set(
                                                cameraChaseTarget.translation.x + chaseCamOffset.x,
                                                chaseCamOffset.y,
                                                cameraChaseTarget.translation.z + chaseCamOffset.z);
                                        cam.update();
                                }

                        }

                        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

                        modelBatch.begin(cam);

                        final float effectiveDelta = paused ? 0 : delta;
                        for (final Spatial spatial : spatials) {
                                if (spatial.isInitialized())
                                        spatial.render(effectiveDelta);

                        }
                        modelBatch.end();
                        decalBatch.flush();
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
                if(!paused){
                        Gdx.input.setInputProcessor(inputMultiplexer);
                }
        }

        public boolean isPaused() {
                return paused;
        }

        public void resize(int width, int height) {
                stage.getViewport().update(width, height, true);
                        hudSpatial.resize(width,height);
                cam.viewportWidth = width;
                cam.viewportHeight = height;
                cam.update();
        }

        @Override
        public void dispose() {
                for (Spatial spatial : spatials) {
                        spatial.dispose();
                }
                decalBatch.dispose();
                modelBatch.dispose();
                assetManager.dispose();
                stage.dispose();
                Gdx.input.setInputProcessor(null);
        }

        private class InternalInputAdapter extends InputAdapter implements Dungeon.Listener {

                // The internal input adapter only processes input during the loading phase

                @Override
                public boolean keyDown(int keycode) {
                        if (keycode == Input.Keys.ESCAPE || keycode ==  Input.Keys.BACK) {
                                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                                        dungeonApp.exitApp();
                                }
                                return true;
                        }
                        return false;
                }

                @Override
                public boolean keyUp(int keycode) {
                        if (keycode == Input.Keys.ESCAPE || keycode ==  Input.Keys.BACK) {
                                dungeonApp.setWorldPaused(true);
                                return true;
                        }
                        return false;
                }

                @Override
                public void onTokenAdded(Token token) {

                        if (token == hudSpatial.localPlayerToken) {
                                //Gdx.app.log("DungeonWorld", "moving view to move with the player token");
                                floorDecals.setFloorMap(hudSpatial.localPlayerToken.getFloorMap());
                        } else  {
                                TokenSpatial tokenSpatial = addSpatial(new TokenSpatial(DungeonWorld.this, token, floorDecals.tokenCustomBox, environment));
                                LocalPlayerLogicProvider logicProvider = token.get(LocalPlayerLogicProvider.class);
                                //Gdx.app.log("DungeonWorld", "Local character token is added");
                                if(logicProvider != null && logicProvider.getId() == 0){
                                        hudSpatial.setToken(token);
                                        cameraChaseTarget = tokenSpatial;
                                        floorDecals.setFloorMap(hudSpatial.localPlayerToken.getFloorMap());
                                }
                        }

                }

                @Override
                public void onTokenRemoved(Token token) {
                        //Gdx.app.log("DungeonWorld", "tokenRemoved: " + token);
                        if (token == hudSpatial.localPlayerToken) {
                                dungeon.setCurrentFloor(hudSpatial.localPlayerToken.getFloorMap().index);
                                return;
                        }

                        TokenSpatial removeSpatial = getTokenSpatial(token);

                        if (removeSpatial != null)
                                removeSpatial(removeSpatial);
                }
        }
}
