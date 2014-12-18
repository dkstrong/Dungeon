package asf.dungeon.view;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Tile;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.fogmap.FogMapNull;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.utility.BetterAnimationController;
import asf.dungeon.utility.BetterModelInstance;
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
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalMaterial;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Daniel Strong on 12/15/2014.
 */
public class FastFloorSpatial implements Spatial {

        public final Vector3 tileDimensions = new Vector3(5, 5.75f, 5);
        public final Box tileBox = new Box(new Vector3(-tileDimensions.x / 2f, 0, -tileDimensions.z / 2f),new Vector3(tileDimensions.x / 2f, tileDimensions.y, tileDimensions.z / 2f));
        public final Shape tileSphere = new Sphere(5.75f, 0, 2.875f, 0);
        private DungeonWorld world;
        private boolean initialized;
        private FloorMap floorMap;
        private FogMap fogMap;
        private Array<DecalNode> decalNodes;
        private TextureRegion[][] floorTexRegions;
        private TextureRegion[] wallTexRegions, wallDarkTexRegions;
        private TextureAttribute[] doorLockedTexAttribute;
        private float[] fogAlpha;
        private static Vector3 worldCoordsTemp = new Vector3();
        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
                world.assetManager.load("Textures/Dungeon/floorTiles.png", Texture.class);
                world.assetManager.load("Textures/Dungeon/wallTiles.png", Texture.class);

                world.assetManager.load("Models/Dungeon/Stairs/StairsUp.g3db", Model.class);
                world.assetManager.load("Models/Dungeon/Stairs/StairsDown.g3db", Model.class);

                world.assetManager.load("Models/Dungeon/Door/Door.g3db", Model.class);
        }

        @Override
        public void init(AssetManager assetManager) {
                initialized = true;
                fogAlpha = new float[5];
                decalNodes = new Array<DecalNode>(false, 1024, DecalNode.class);

                Texture floorTex = world.assetManager.get("Textures/Dungeon/floorTiles.png", Texture.class);
                floorTexRegions = TextureRegion.split(floorTex, 128, 128);

                Texture  wallTex = world.assetManager.get("Textures/Dungeon/wallTiles.png", Texture.class);
                TextureRegion[][] tr2 = TextureRegion.split(wallTex, 256, 256);
                wallTexRegions = tr2[0];
                wallDarkTexRegions = tr2[1];

                doorLockedTexAttribute = new TextureAttribute[5];
                // locked by key

                doorLockedTexAttribute[KeyItem.Type.Silver.ordinal()] = TextureAttribute.createDiffuse(world.pack.findRegion("Models/Dungeon/Door/DoorLockedSilver"));
                doorLockedTexAttribute[KeyItem.Type.Gold.ordinal()] = TextureAttribute.createDiffuse(world.pack.findRegion("Models/Dungeon/Door/DoorLockedGold"));
                doorLockedTexAttribute[KeyItem.Type.Red.ordinal()] = TextureAttribute.createDiffuse(world.pack.findRegion("Models/Dungeon/Door/DoorLockedRed"));
                doorLockedTexAttribute[doorLockedTexAttribute.length-2] = TextureAttribute.createDiffuse(world.pack.findRegion("Models/Dungeon/Door/DoorLocked"));
                doorLockedTexAttribute[doorLockedTexAttribute.length-1] = TextureAttribute.createDiffuse(world.pack.findRegion("Models/Dungeon/Door/Door"));

                setFloorMap(floorMap);
        }

        public void setFloorMap(FloorMap floorMap){
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
                DecalNodeWall.topWallVisibleY = tileDimensions.y;
                DecalNodeWall.sideWallVisibleY = tileDimensions.y/2f;

                decalNodes.clear();
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

        }

        @Override
        public void update(float delta) {

        }

        @Override
        public void render(float delta) {
                for (DecalNode decalNode : decalNodes) {
                        decalNode.render(this, delta);
                }
        }

        @Override
        public void dispose() {
                initialized = false;
        }

        @Override
        public boolean isInitialized() {
                return initialized;
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

        private void makeDecalFloor(Tile tile, int x, int y) {
                world.getWorldCoords(x, y, worldCoordsTemp);
                DecalNodeFloor decalNode;
                if(tile.isDoor()){
                        DecalNodeDoor door = new DecalNodeDoor();
                        decalNode=  door;
                        door.modelInstance = new BetterModelInstance(world.assetManager.get("Models/Dungeon/Door/Door.g3db", Model.class));
                        door.animController = new BetterAnimationController(door.modelInstance);
                        door.animController.paused = true;
                        Material material = door.modelInstance.materials.get(0);
                        //material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
                        door.colorAttribute = (ColorAttribute)material.get(ColorAttribute.Diffuse);
                        Quaternion rot = world.assetMappings.getRotation(whichDirectionToFaceDoor(x,y));
                        door.modelInstance.transform.set(
                                worldCoordsTemp.x,worldCoordsTemp.y,worldCoordsTemp.z,
                                rot.x,rot.y,rot.z,rot.w,
                                1,1,1
                        );
                }else if(tile.isStairs()){
                        BetterModelInstance modelInstance;
                        if(tile.isStairsUp(floorMap.index)){
                                DecalNodeStairsUp stairsUp = new DecalNodeStairsUp();
                                decalNode = stairsUp;
                                stairsUp.modelInstance = new BetterModelInstance(world.assetManager.get("Models/Dungeon/Stairs/StairsUp.g3db", Model.class));
                                Material material = stairsUp.modelInstance.materials.get(0);
                                stairsUp.colorAttribute = (ColorAttribute)material.get(ColorAttribute.Diffuse);
                                modelInstance = stairsUp.modelInstance;
                        }else{
                                DecalNodeStairsDown stairsDown = new DecalNodeStairsDown();
                                decalNode = stairsDown;
                                stairsDown.modelInstance = new BetterModelInstance(world.assetManager.get("Models/Dungeon/Stairs/StairsDown.g3db", Model.class));
                                Material material = stairsDown.modelInstance.materials.get(0);
                                stairsDown.colorAttribute = (ColorAttribute)material.get(ColorAttribute.Diffuse);
                                modelInstance = stairsDown.modelInstance;
                        }
                        Quaternion rot = world.assetMappings.getRotation(Direction.East);
                        modelInstance.transform.set(
                                worldCoordsTemp.x,worldCoordsTemp.y,worldCoordsTemp.z,
                                rot.x,rot.y,rot.z,rot.w,
                                1,1,1
                        );

                }else{
                        decalNode = new DecalNodeFloor();
                }
                decalNode.x = x;
                decalNode.y = y;
                decalNode.tile = tile;
                decalNodes.add(decalNode);



                decalNode.decal = new Decal();
                decalNode.decal.setTextureRegion(floorTexRegions[MathUtils.random.nextInt(floorTexRegions.length)][MathUtils.random.nextInt(floorTexRegions[0].length)]);
                decalNode.decal.setBlending(DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                decalNode.decal.setDimensions(tileDimensions.x, tileDimensions.z);
                decalNode.decal.setColor(0,0,0,1);
                decalNode.decal.rotateX(-90);
                decalNode.decal.translate(worldCoordsTemp.x, 0, worldCoordsTemp.z);
        }

        private void makeDecalWall(Tile tile, int x, int y){
                DecalNodeWall decalNode = new DecalNodeWall();
                decalNode.x = x;
                decalNode.y = y;
                decalNode.tile = tile;
                decalNode.decals = new Decal[4];
                decalNodes.add(decalNode);

                world.getWorldCoords(x, y, worldCoordsTemp);

                // top
                decalNode.decal = new Decal();
                decalNode.decal.setTextureRegion(wallTexRegions[MathUtils.random.nextInt(wallTexRegions.length)]);
                decalNode.decal.setBlending(DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                decalNode.decal.setDimensions(tileDimensions.x, tileDimensions.z);
                decalNode.decal.setColor(0,0,0,1);
                decalNode.decal.rotateX(-90);
                decalNode.decal.translate(worldCoordsTemp.x, 0, worldCoordsTemp.z);
                // visiblyY = tileDimensions.y

                // north
                decalNode.decals[0] = new Decal();
                decalNode.decals[0].setTextureRegion(wallTexRegions[MathUtils.random.nextInt(wallTexRegions.length)]);
                decalNode.decals[0].setBlending(DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                decalNode.decals[0].setDimensions(tileDimensions.x, tileDimensions.y);
                decalNode.decals[0].setColor(0,0,0,1);
                decalNode.decals[0].translate(worldCoordsTemp.x, -tileDimensions.y / 2f, worldCoordsTemp.z - tileDimensions.z / 2f);
                // visiblyY = tileDimensions.y/2f

                // south
                decalNode.decals[1] = new Decal();
                decalNode.decals[1].setTextureRegion(wallDarkTexRegions[MathUtils.random.nextInt(wallDarkTexRegions.length)]);
                decalNode.decals[1].setBlending(DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                decalNode.decals[1].setDimensions(tileDimensions.x, tileDimensions.y);
                decalNode.decals[1].setColor(0,0,0,1);
                decalNode.decals[1].translate(worldCoordsTemp.x, -tileDimensions.y/2f, worldCoordsTemp.z+tileDimensions.z/2f);
                // visiblyY = tileDimensions.y/2f

                // east
                decalNode.decals[2] = new Decal();
                decalNode.decals[2].setTextureRegion(wallTexRegions[MathUtils.random.nextInt(wallTexRegions.length)]);
                decalNode.decals[2].setBlending(DecalMaterial.NO_BLEND, DecalMaterial.NO_BLEND);
                decalNode.decals[2].setDimensions(tileDimensions.z, tileDimensions.y);
                decalNode.decals[2].setColor(0, 0, 0, 1);
                decalNode.decals[2].rotateY(-90);
                decalNode.decals[2].translate(worldCoordsTemp.x+tileDimensions.x/2f, -tileDimensions.y/2f, worldCoordsTemp.z);
                // visiblyY = tileDimensions.y/2f

                // west
                decalNode.decals[3] = new Decal();
                decalNode.decals[3].setTextureRegion(wallDarkTexRegions[MathUtils.random.nextInt(wallDarkTexRegions.length)]);
                decalNode.decals[3].setBlending(DecalMaterial.NO_BLEND, DecalMaterial.NO_BLEND);
                decalNode.decals[3].setDimensions(tileDimensions.z, tileDimensions.y);
                decalNode.decals[3].setColor(0, 0, 0, 1);
                decalNode.decals[3].rotateY(-90);
                decalNode.decals[3].translate(worldCoordsTemp.x-tileDimensions.x/2f, -tileDimensions.y/2f, worldCoordsTemp.z);
                // visiblyY = tileDimensions.y/2f


        }


        private abstract static class DecalNode{
                public int x;
                public int y;
                public Tile tile;

                protected abstract void render(FastFloorSpatial floor, float delta);


        }

        // TODO: could do a small optimzation by not calling decal.setColor() when the color hasnt actually changed.
        // decal.setColor calls native code which should be avoided on android

        private static class DecalNodeFloor extends DecalNode{
                public Decal decal;

                @Override
                protected void render(FastFloorSpatial floor, float delta) {
                        Color color = decal.getColor();
                        FogState fogState = floor.fogMap.getFogState(x,y);
                        float fog = MathUtils.lerp(color.g, floor.fogAlpha[fogState.ordinal()], delta);
                        if(fog > 0 && (floor.world.hudSpatial.isMapViewMode() || floor.world.getLocalPlayerToken().getLocation().distance(x,y)<16)){
                                if(fogState == FogState.MagicMapped)
                                        color.set(fog * 0.9f, fog, fog * 1.2f, 1);
                                else
                                        color.set(fog,fog,fog,1);
                                decal.setColor(color);
                                floor.world.decalBatch.add(decal);
                        }else{
                                color.g = fog;
                        }
                }
        }

        private static class DecalNodeWall extends DecalNode{
                public Decal decal; // top decal
                public Decal[] decals; // side wall decals
                public static float topWallVisibleY;
                public static float sideWallVisibleY;

                @Override
                protected void render(FastFloorSpatial floor, float delta) {
                        Color color = decal.getColor();
                        FogState fogState = floor.fogMap.getFogState(x,y);
                        float fog = MathUtils.lerp(color.g, floor.fogAlpha[fogState.ordinal()], delta);
                        if(fog > 0 && (floor.world.hudSpatial.isMapViewMode() || floor.world.getLocalPlayerToken().getLocation().distance(x,y)<16)){
                                if(decal.getPosition().y != topWallVisibleY){
                                        decal.setY(topWallVisibleY);
                                        for (Decal wallDecal : decals)
                                                wallDecal.setY(sideWallVisibleY);
                                }
                                if(fogState == FogState.MagicMapped)
                                        color.set(fog * 0.9f, fog, fog * 1.2f, 1);
                                else
                                        color.set(fog,fog,fog,1);
                                decal.setColor(color);
                                floor.world.decalBatch.add(decal);
                                for (Decal wallDecal : decals) {
                                        wallDecal.setColor(color);
                                        floor.world.decalBatch.add(wallDecal);
                                }
                        }else{
                                color.g = fog;
                        }
                }
        }

        private static class DecalNodeStairsUp extends DecalNodeFloor{
                public BetterModelInstance modelInstance;
                public ColorAttribute colorAttribute;

                @Override
                protected void render(FastFloorSpatial floor, float delta) {
                        Color color = decal.getColor();
                        FogState fogState = floor.fogMap.getFogState(x,y);
                        float fog = MathUtils.lerp(color.g, floor.fogAlpha[fogState.ordinal()], delta);
                        if(fog > 0 && (floor.world.hudSpatial.isMapViewMode() || floor.world.getLocalPlayerToken().getLocation().distance(x,y)<16)){
                                if(fogState == FogState.MagicMapped)
                                        color.set(fog * 0.9f, fog, fog * 1.2f, 1);
                                else
                                        color.set(fog,fog,fog,1);
                                decal.setColor(color);
                                floor.world.decalBatch.add(decal);

                                colorAttribute.color.r = UtMath.clamp(color.r+.1f, 0f,1f);
                                colorAttribute.color.g = UtMath.clamp(color.g+.1f, 0f,1f);
                                colorAttribute.color.b = UtMath.clamp(color.b+.1f, 0f,1f);
                                floor.world.modelBatch.render(modelInstance, floor.world.environment);
                        }else{
                                color.g = fog;
                        }
                }
        }

        private static class DecalNodeStairsDown extends DecalNodeFloor{
                public BetterModelInstance modelInstance;
                public ColorAttribute colorAttribute;
                public static float visibleY = -3.753f;

                @Override
                protected void render(FastFloorSpatial floor, float delta) {
                        Color color = decal.getColor();
                        FogState fogState = floor.fogMap.getFogState(x,y);
                        float fog = MathUtils.lerp(color.g, floor.fogAlpha[fogState.ordinal()], delta);
                        if(fog > 0 && (floor.world.hudSpatial.isMapViewMode() || floor.world.getLocalPlayerToken().getLocation().distance(x,y)<16)){
                                if(decal.getPosition().y != visibleY){
                                        decal.setY(visibleY);

                                }
                                if(fogState == FogState.MagicMapped)
                                        color.set(fog * 0.9f, fog, fog * 1.2f, 1);
                                else
                                        color.set(fog,fog,fog,1);
                                decal.setColor(color);
                                floor.world.decalBatch.add(decal);

                                colorAttribute.color.r = UtMath.clamp(color.r+.1f, 0f,1f);
                                colorAttribute.color.g = UtMath.clamp(color.g+.1f, 0f,1f);
                                colorAttribute.color.b = UtMath.clamp(color.b+.1f, 0f,1f);
                                floor.world.modelBatch.render(modelInstance, floor.world.environment);
                        }else{
                                color.g = fog;
                        }
                }
        }

        private static class DecalNodeDoor extends DecalNodeFloor implements BetterAnimationController.AnimationListener{
                public BetterModelInstance modelInstance;
                public ColorAttribute colorAttribute;
                public BetterAnimationController animController;
                public boolean animToggle;

                @Override
                protected void render(FastFloorSpatial floor, float delta) {
                        if(tile.isDoorOpened() && !animToggle){
                                animController.setAnimation("Open",1,1,this);
                                animController.paused = false;
                                animToggle = true;
                        }else if(!tile.isDoorOpened() && animToggle){
                                animController.setAnimation("Open",1,-1,this);
                                animController.paused = false;
                                animToggle = false;
                        }
                        animController.update(delta);

                        Color color = decal.getColor();
                        FogState fogState = floor.fogMap.getFogState(x,y);
                        float fog = MathUtils.lerp(color.g, floor.fogAlpha[fogState.ordinal()], delta);
                        if(fog > 0 && (floor.world.hudSpatial.isMapViewMode() || floor.world.getLocalPlayerToken().getLocation().distance(x,y)<16)){
                                if(fogState == FogState.MagicMapped)
                                        color.set(fog * 0.9f, fog, fog * 1.2f, 1);
                                else
                                        color.set(fog,fog,fog,1);
                                decal.setColor(color);
                                floor.world.decalBatch.add(decal);

                                if(tile.isDoorLocked())
                                        modelInstance.materials.get(0).set(tile.getDoorSymbol().getDoorTexAttribute(floor.doorLockedTexAttribute));
                                else{
                                        // TODO: locked doors should have a "used to be locked but not anymore" texture theyll use after being unlocked
                                        if(!tile.isDoorForcedOpen())
                                                modelInstance.materials.get(0).set(floor.doorLockedTexAttribute[floor.doorLockedTexAttribute.length-1]);
                                        else
                                                modelInstance.materials.get(0).set(tile.getDoorSymbol().getDoorTexAttribute(floor.doorLockedTexAttribute));
                                }


                                colorAttribute.color.r = UtMath.clamp(color.r+.1f, 0f,1f);
                                colorAttribute.color.g = UtMath.clamp(color.g+.1f, 0f,1f);
                                colorAttribute.color.b = UtMath.clamp(color.b+.1f, 0f,1f);
                                floor.world.modelBatch.render(modelInstance, floor.world.environment);
                        }else{
                                color.g = fog;
                        }


                }

                @Override
                public void onEnd(BetterAnimationController.AnimationDesc animation) {
                        animController.paused = true;
                }

                @Override
                public void onLoop(BetterAnimationController.AnimationDesc animation) {

                }
        }

}
