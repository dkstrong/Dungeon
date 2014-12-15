package asf.dungeon.view;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.fogmap.FogMapNull;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.utility.UtMath;
import asf.dungeon.view.shape.Box;
import asf.dungeon.view.shape.Shape;
import asf.dungeon.view.shape.Sphere;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalMaterial;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

/**
 * Created by Daniel Strong on 10/29/14.
 */
public class FloorSpatial implements Spatial {
        /**
         * actual dimensions of a tile
         */
        public final Vector3 tileDimensions = new Vector3(5, 5.75f, 5);
        /**
         * a box that represents the shape size of a tile on the screen
         */
        public final Box tileBox = new Box(new Vector3(-tileDimensions.x / 2f, 0, -tileDimensions.z / 2f),
                new Vector3(tileDimensions.x / 2f, tileDimensions.y, tileDimensions.z / 2f));
        /**
         * a sphere that represents the size of a tile on the screen
         */
        public final Shape tileSphere = new Sphere(5.75f); // TODO: this sphere's center should be 0,2.875,0 like the tilebox
        private DungeonWorld world;
        private FloorMap floorMap;
        private FogMap fogMap;
        private Array<DecalNode> decalNodes;
        private TextureRegion[][] floorTexRegions;
        private TextureRegion[] wallTexRegions, wallDarkTexRegions;
        private TextureAttribute doorTexAttribute;
        private TextureAttribute[] doorLockedTexAttribute;
        private float[] fogAlpha;
        private boolean initialized;
        private Array<DecalNodeProp> decalNodeProps, decalNodePropsTemp;


        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
                preloadCommonAssets();

        }

        @Override
        public void init(AssetManager assetManager) {
                this.initialized = true;
                initCommonAssets();
                setFloorMap(floorMap);
        }

        public void setFloorMap(FloorMap floorMap){
                //Gdx.app.log("FloorDecals","setFloorMap: "+floorMap.index);
                this.floorMap = floorMap;
                if(!isInitialized())
                        return;

                if( world.getLocalPlayerToken() != null && world.getLocalPlayerToken().getFogMapping() != null){
                        fogMap = world.getLocalPlayerToken().getFogMapping().getFogMap(floorMap);
                        if(fogMap == null){
                                throw new AssertionError("should not be null");
                        }
                }else{
                        //fogMap = null;
                        // creating a dummy fogmap, this case should only happen
                        Gdx.app.error("FloorSpatial","no fog map, creating a dummy fog map");
                        fogMap = new FogMapNull(floorMap, null);
                }

                fogAlpha[FogState.Dark.ordinal()] = 0;
                fogAlpha[FogState.MagicMapped.ordinal()] = .5f;
                fogAlpha[FogState.Visited.ordinal()] = .55f;
                fogAlpha[FogState.Visible.ordinal()] = 1;

                makeDecals();
        }



        public FloorMap getFloorMap(){
                return floorMap;
        }


        public Pair getMapCoords(Vector3 worldCoords, Pair storeMapCoords) {
                storeMapCoords.x = (int)((worldCoords.x ) / tileDimensions.x);
                storeMapCoords.y = (int)((UtMath.abs(worldCoords.z)) / tileDimensions.z);
                return storeMapCoords;
        }

        public Vector3 getWorldCoords(Pair mapCoords, Vector3 storeWorldCoords){
                storeWorldCoords.x =( (mapCoords.x * tileDimensions.x) ) + (tileDimensions.x /2f);
                storeWorldCoords.y = 0;
                storeWorldCoords.z =(-(mapCoords.y * tileDimensions.z) ) - (tileDimensions.z/2f);
                return storeWorldCoords;
        }

        public Vector3 getWorldCoords(float mapCoordsX, float mapCoordsY, Vector3 storeWorldCoords){
                storeWorldCoords.x =( (mapCoordsX * tileDimensions.x) ) + (tileDimensions.x /2f);
                storeWorldCoords.y = 0;
                storeWorldCoords.z =(-(mapCoordsY * tileDimensions.z) ) - (tileDimensions.z/2f);
                return storeWorldCoords;
        }



        public void update(float delta){

        }

        @Override
        public void render(float delta) {
                for (int i = 0; i < decalNodes.size; i++) {
                        DecalNode decalNode = decalNodes.items[i];

                        Color color = decalNode.decal.getColor();
                        FogState fogState = fogMap.getFogState(decalNode.x, decalNode.y);
                        float fog = MathUtils.lerp(color.g, fogAlpha[fogState.ordinal()], delta);

                        boolean renderDecal = fog > 0 && (world.hudSpatial.isMapViewMode() || world.getLocalPlayerToken().getLocation().distance(decalNode.x, decalNode.y) < 16);
                        // && world.cam.frustum.sphereInFrustumWithoutNearFar(decalNode.decal.getPosition(), 5)

                        if(renderDecal){
                                if(fogState != FogState.Dark && decalNode.decal.getPosition().y != decalNode.visibleY){
                                        decalNode.decal.getPosition().y = decalNode.visibleY; // if this is the first time this decal tile is "visited" then it needs to "pop up" in to position
                                        decalNode.decal.setPosition(decalNode.decal.getPosition());
                                }

                                if(fogState == FogState.MagicMapped){
                                        color.set(fog*0.9f,fog,fog*1.2f,1);
                                }else{
                                        color.set(fog,fog,fog,1);
                                }

                                decalNode.decal.setColor(color);
                                world.decalBatch.add(decalNode.decal);
                        }else{
                                color.g = fog;
                        }

                        DecalNodeProp prop = decalNode.prop;
                        if(prop != null){
                                if(decalNode.tile.isDoor()){
                                        if(renderDecal){
                                                if(decalNode.tile.isDoorLocked()){
                                                        prop.modelInstance.materials.get(0).set(doorLockedTexAttribute[decalNode.tile.getKeyType().ordinal()]);
                                                }else{
                                                        prop.modelInstance.materials.get(0).set(doorTexAttribute);
                                                }
                                        }

                                        if(decalNode.tile.isDoorOpened() && !prop.animToggle){
                                                prop.animController.setAnimation("Open",1,1,prop);
                                                prop.animController.paused = false;
                                                prop.animToggle = true;
                                        }else if(!decalNode.tile.isDoorOpened() && prop.animToggle){
                                                prop.animController.setAnimation("Open",1,-1,prop);
                                                prop.animController.paused = false;
                                                prop.animToggle = false;
                                        }
                                        prop.animController.update(delta);
                                }

                                // props need to be a little more visible than tiles
                                if(renderDecal){
                                        prop.colorAttribute.color.r = UtMath.clamp(color.r+.1f, 0f,1f);
                                        prop.colorAttribute.color.g = UtMath.clamp(color.g+.1f, 0f,1f);
                                        prop.colorAttribute.color.b = UtMath.clamp(color.b+.1f, 0f,1f);
                                        world.modelBatch.render(prop.modelInstance, world.environment);
                                }

                        }


                }


        }

        @Override
        public boolean isInitialized() {
                return initialized;
        }

        @Override
        public void dispose() {
        }



        private void preloadCommonAssets(){

                world.assetManager.load("Textures/Dungeon/floorTiles.png", Texture.class);
                world.assetManager.load("Textures/Dungeon/wallTiles.png", Texture.class);

                world.assetManager.load("Models/Dungeon/Stairs/StairsUp.g3db", Model.class);
                world.assetManager.load("Models/Dungeon/Stairs/StairsDown.g3db", Model.class);

                world.assetManager.load("Models/Dungeon/Door/Door.g3db", Model.class);
                world.assetManager.load("Models/Dungeon/Door/Door.png", Texture.class);
                world.assetManager.load("Models/Dungeon/Door/DoorLockedSilver.png", Texture.class);
                world.assetManager.load("Models/Dungeon/Door/DoorLockedGold.png", Texture.class);
                world.assetManager.load("Models/Dungeon/Door/DoorLockedRed.png", Texture.class);

        }


        private void initCommonAssets(){

                fogAlpha = new float[5];
                decalNodes = new Array<DecalNode>(false, 128, DecalNode.class);
                decalNodeProps = new Array<DecalNodeProp>(false, 8, DecalNodeProp.class);
                decalNodePropsTemp = new Array<DecalNodeProp>(false, 8, DecalNodeProp.class);

                Texture  floorTex = world.assetManager.get("Textures/Dungeon/floorTiles.png", Texture.class);
                floorTexRegions = TextureRegion.split(floorTex, 128, 128);

                Texture  wallTex = world.assetManager.get("Textures/Dungeon/wallTiles.png", Texture.class);
                TextureRegion[][] tr2 = TextureRegion.split(wallTex, 256, 256);
                wallTexRegions = tr2[0];
                wallDarkTexRegions = tr2[1];

                doorTexAttribute = TextureAttribute.createDiffuse(world.assetManager.get("Models/Dungeon/Door/Door.png", Texture.class));
                doorLockedTexAttribute = new TextureAttribute[3];
                doorLockedTexAttribute[KeyItem.Type.Silver.ordinal()] = TextureAttribute.createDiffuse(world.assetManager.get("Models/Dungeon/Door/DoorLockedSilver.png", Texture.class));
                doorLockedTexAttribute[KeyItem.Type.Gold.ordinal()] = TextureAttribute.createDiffuse(world.assetManager.get("Models/Dungeon/Door/DoorLockedGold.png", Texture.class));
                doorLockedTexAttribute[KeyItem.Type.Red.ordinal()] = TextureAttribute.createDiffuse(world.assetManager.get("Models/Dungeon/Door/DoorLockedRed.png", Texture.class));
        }




        private void makeDecals(){
                decalNodes.clear();
                decalNodePropsTemp.addAll(decalNodeProps);
                decalNodeProps.clear();

                for (int x = 0; x < floorMap.getWidth(); x++) {
                        for (int y = 0; y < floorMap.getHeight(); y++) {
                                Tile tile = floorMap.getTile(x,y);
                                if(tile != null){
                                        if(tile.isWall()){
                                                makeDecalWall(tile, x, y);
                                        }else{
                                                makeDecalFloor(tile, x,y);
                                        }
                                }


                        }
                }

                decalNodePropsTemp.clear();
        }



        private Direction whichDirectionToFaceDoor(int x, int y){
                Tile nw = floorMap.getTile(x-1,y+1);
                Tile n = floorMap.getTile(x,y+1);
                Tile ne = floorMap.getTile(x+1,y+1);
                int northCount = (nw.isFloor()? 1 :0) + (n.isFloor()? 1 :0) + (ne.isFloor() ? 1 :0);
                if(northCount == 3) return Direction.North;
                Tile e = floorMap.getTile(x+1,y);
                Tile se = floorMap.getTile(x+1,y-1);
                int eastCount = (ne.isFloor()? 1 :0) + (e.isFloor()? 1 :0) + (se.isFloor() ? 1 :0);
                if(eastCount == 3) return Direction.East;
                Tile s = floorMap.getTile(x,y-1);
                Tile sw = floorMap.getTile(x-1,y-1);
                int southCount = (se.isFloor()? 1 :0) + (s.isFloor()? 1 :0) + (sw.isFloor() ? 1 :0);
                if(southCount == 3) return Direction.South;
                Tile w = floorMap.getTile(x-1,y);
                int westCount = (sw.isFloor()? 1 :0) + (w.isFloor()? 1 :0) + (nw.isFloor() ? 1 :0);
                if(westCount == 3) return Direction.West;

                if(northCount == 2) return Direction.North;
                if(eastCount == 2) return Direction.East;
                if(southCount == 2) return Direction.South;
                if(westCount == 2) return Direction.West;


                if(n.isFloor() && s.isFloor()) return Direction.South;
                return Direction.West;
        }

        private Vector3 worldCoordsTemp = new Vector3();

        private void makeDecalFloor(Tile tile, int x, int y) {
                getWorldCoords(x, y, worldCoordsTemp);
                DecalNodeProp prop;
                float visibleY = 0;
                if(tile.isDoor()){
                        prop = makeDecalNodeProp("Models/Dungeon/Door/Door.g3db");

                        Quaternion rot = world.assetMappings.getRotation(whichDirectionToFaceDoor(x,y));
                        prop.modelInstance.transform.set(
                                worldCoordsTemp.x,worldCoordsTemp.y,worldCoordsTemp.z,
                                rot.x,rot.y,rot.z,rot.w,
                                1,1,1
                        );
                        decalNodeProps.add(prop);
                }else if(tile.isStairs()) {

                        if (tile.isStairsUp(floorMap.index)){
                                prop = makeDecalNodeProp("Models/Dungeon/Stairs/StairsUp.g3db");
                        }else{
                                prop = makeDecalNodeProp("Models/Dungeon/Stairs/StairsDown.g3db");
                                visibleY = -3.753f;
                        }

                        // TODO: may want to make it so stairs rotate based on what they are near
                        Quaternion rot = world.assetMappings.getRotation(Direction.East);

                        prop.modelInstance.transform.set(
                                worldCoordsTemp.x,worldCoordsTemp.y,worldCoordsTemp.z,
                                rot.x,rot.y,rot.z,rot.w,
                                1,1,1
                        );
                        decalNodeProps.add(prop);
                }else{
                        prop = null;
                }
                TextureRegion region = floorTexRegions[MathUtils.random.nextInt(floorTexRegions.length)][MathUtils.random.nextInt(floorTexRegions[0].length)];
                Decal decal = Decal.newDecal(
                        tileDimensions.x,
                        tileDimensions.z,
                        region,
                        DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                // GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA
                decal.rotateX(-90);
                decal.translate(worldCoordsTemp.x, 0, worldCoordsTemp.z);
                decal.setColor(0,0,0,1);

                DecalNode decalNode = new DecalNode(decal, x, y, tile, visibleY);
                decalNode.prop =prop;
                decalNodes.add(decalNode);

        }

        private void makeDecalWall(Tile tile, int x, int y){

                getWorldCoords(x, y, worldCoordsTemp);

                // top
                Decal decal = Decal.newDecal(
                        tileDimensions.x,
                        tileDimensions.z,
                        wallTexRegions[MathUtils.random.nextInt(wallTexRegions.length)],
                        DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                // GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA
                decal.rotateX(-90);
                decal.translate(worldCoordsTemp.x, 0, worldCoordsTemp.z);
                decal.setColor(0,0,0,1);
                decalNodes.add(new DecalNode(decal, x, y, tile,tileDimensions.y));

                // north
                Tile northTile = null;//floorMap.getTile(x,y+1);
                if(northTile == null || !northTile.isBlockMovement()){
                        decal = Decal.newDecal(
                                tileDimensions.x,
                                tileDimensions.y,
                                wallTexRegions[MathUtils.random.nextInt(wallTexRegions.length)],
                                DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                        //decal.rotateX(-90);
                        decal.translate(worldCoordsTemp.x, -tileDimensions.y/2f, worldCoordsTemp.z-tileDimensions.z/2f);
                        decal.setColor(0,0,0,1);
                        decalNodes.add(new DecalNode(decal, x, y, tile,tileDimensions.y/2f));
                }

                // south
                Tile southTile = null;//floorMap.getTile(x,y-1);
                if(southTile == null || !southTile.isBlockMovement()){
                        decal = Decal.newDecal(
                                tileDimensions.x,
                                tileDimensions.y,
                                wallDarkTexRegions[MathUtils.random.nextInt(wallDarkTexRegions.length)], // dark
                                DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                        //decal.rotateX(-90);
                        decal.translate(worldCoordsTemp.x, -tileDimensions.y/2f, worldCoordsTemp.z+tileDimensions.z/2f);
                        decal.setColor(0,0,0,1);
                        decalNodes.add(new DecalNode(decal, x, y, tile,tileDimensions.y/2f));
                }

                // east
                Tile eastTile = null;//floorMap.getTile(x+1,y);
                if(eastTile == null || !eastTile.isBlockMovement()){
                        decal = Decal.newDecal(
                                tileDimensions.z,
                                tileDimensions.y,
                                wallTexRegions[MathUtils.random.nextInt(wallTexRegions.length)],
                                DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                        decal.rotateY(-90);
                        decal.translate(worldCoordsTemp.x+tileDimensions.x/2f, -tileDimensions.y/2f, worldCoordsTemp.z);
                        decal.setColor(0,0,0,1);
                        decalNodes.add(new DecalNode(decal, x, y, tile,tileDimensions.y/2f));
                }

                // west
                Tile westTile = null;//floorMap.getTile(x-1,y);
                if(westTile == null || !westTile.isBlockMovement()){
                        decal = Decal.newDecal(
                                tileDimensions.z,
                                tileDimensions.y,
                                wallDarkTexRegions[MathUtils.random.nextInt(wallDarkTexRegions.length)], // dark
                                DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                        decal.rotateY(-90);
                        decal.translate(worldCoordsTemp.x-tileDimensions.x/2f, -tileDimensions.y/2f, worldCoordsTemp.z);
                        decal.setColor(0,0,0,1);
                        decalNodes.add(new DecalNode(decal, x, y, tile,tileDimensions.y/2f));
                }

        }

        private static class DecalNode{
                public Decal decal;
                public int x;
                public int y;
                public Tile tile;
                public float visibleY; // floor tiles start in the sky, then once visited they move to y=0, this is to prevent their sillouete from giving away what they are
                public DecalNodeProp prop;

                public DecalNode(Decal decal, int x, int y, Tile tile, float visibleY) {
                        this.decal = decal;
                        this.x = x;
                        this.y = y;
                        this.tile = tile;
                        this.visibleY = visibleY;
                }
        }

        private static class DecalNodeProp implements AnimationController.AnimationListener {
                public final String assetLocation;
                public ModelInstance modelInstance;
                public AnimationController animController;
                public ColorAttribute colorAttribute;
                private boolean animToggle;

                private DecalNodeProp(String assetLocation) {
                        this.assetLocation = assetLocation;
                }

                private void activate(){
                        if(assetLocation.contains("Door.g3db")){
                                if(animToggle){
                                        animController.setAnimation("Open",1,-50,this);
                                        animController.paused = false;
                                        animToggle = false;
                                }else{
                                        animController.paused = true;
                                }


                        }
                }

                @Override
                public void onEnd(AnimationController.AnimationDesc animation) {
                        animController.paused = true;
                }

                @Override
                public void onLoop(AnimationController.AnimationDesc animation) {

                }
        }

        private DecalNodeProp makeDecalNodeProp(String assetLocation){
                Iterator<DecalNodeProp> i = decalNodePropsTemp.iterator();
                while(i.hasNext()){
                        DecalNodeProp next = i.next();
                        if(assetLocation.equals(next.assetLocation)){
                                i.remove();
                                next.activate();
                                return next;
                        }
                }

                DecalNodeProp prop = new DecalNodeProp(assetLocation);

                prop.modelInstance = new ModelInstance(world.assetManager.get(assetLocation, Model.class));

                if(prop.modelInstance.model.animations.size >0){
                        prop.animController = new AnimationController(prop.modelInstance);
                }


                Material material = prop.modelInstance.materials.get(0);
                //material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
                prop.colorAttribute = (ColorAttribute)material.get(ColorAttribute.Diffuse);


                prop.activate();

                return prop;

        }
}
