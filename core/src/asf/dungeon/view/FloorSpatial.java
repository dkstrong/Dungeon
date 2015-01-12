package asf.dungeon.view;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Tile;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.fogmap.FogMapNull;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.model.token.Stairs;
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
public class FloorSpatial implements Spatial {

        public final Vector3 tileDimensions = new Vector3(5, 5.75f, 5);
        public final Box tileBox = new Box(new Vector3(-tileDimensions.x / 2f, 0, -tileDimensions.z / 2f),new Vector3(tileDimensions.x / 2f, tileDimensions.y, tileDimensions.z / 2f));
        public final Shape tileSphere = new Sphere(tileDimensions.y/2f, 0, tileDimensions.y/4f, 0);
        private DungeonWorld world;
        private boolean initialized;
        private FloorMap floorMap;
        public FogMap fogMap;
        private Array<DecalNode> decalNodes;
        private TextureRegion[][] floorTexRegions;
        private TextureRegion[][] wallTexRegions;
        private TextureRegion[][] pitTexRegions;
        private TextureAttribute[] doorLockedTexAttribute;
        private float[] fogAlpha;
        private static Vector3 worldCoordsTemp = new Vector3();
        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
                world.assetManager.load("Textures/Floor/floorTilesPressurePlates.png", Texture.class);
                world.assetManager.load("Textures/Floor/wallTiles.png", Texture.class);
                world.assetManager.load("Textures/Floor/pitTiles.png", Texture.class);

                world.assetManager.load("Models/Dungeon/Door/Door.g3db", Model.class);
        }

        @Override
        public void init(AssetManager assetManager) {
                initialized = true;
                fogAlpha = new float[5];
                decalNodes = new Array<DecalNode>(false, 1024, DecalNode.class);

                Texture floorTex = world.assetManager.get("Textures/Floor/floorTilesPressurePlates.png", Texture.class);
                floorTexRegions = TextureRegion.split(floorTex, 64, 64);

                Texture wallTex = world.assetManager.get("Textures/Floor/wallTiles.png", Texture.class);
                wallTexRegions = TextureRegion.split(wallTex, 64, 64);

                Texture pitTex = world.assetManager.get("Textures/Floor/pitTiles.png", Texture.class);
                pitTexRegions = TextureRegion.split(pitTex, 64, 64);

                doorLockedTexAttribute = new TextureAttribute[8];
                // locked by key

                doorLockedTexAttribute[KeyItem.Type.Silver.ordinal()] = TextureAttribute.createDiffuse(world.pack.findRegion("Models/Dungeon/Door/DoorLockedSilver"));
                doorLockedTexAttribute[KeyItem.Type.Silver.ordinal()+3] = TextureAttribute.createDiffuse(world.pack.findRegion("Models/Dungeon/Door/DoorSilver"));
                doorLockedTexAttribute[KeyItem.Type.Gold.ordinal()] = TextureAttribute.createDiffuse(world.pack.findRegion("Models/Dungeon/Door/DoorLockedGold"));
                doorLockedTexAttribute[KeyItem.Type.Gold.ordinal()+3] = TextureAttribute.createDiffuse(world.pack.findRegion("Models/Dungeon/Door/DoorGold"));
                doorLockedTexAttribute[KeyItem.Type.Red.ordinal()] = TextureAttribute.createDiffuse(world.pack.findRegion("Models/Dungeon/Door/DoorLockedRed"));
                doorLockedTexAttribute[KeyItem.Type.Red.ordinal()+3] = TextureAttribute.createDiffuse(world.pack.findRegion("Models/Dungeon/Door/DoorRed"));
                doorLockedTexAttribute[doorLockedTexAttribute.length-2] = TextureAttribute.createDiffuse(world.pack.findRegion("Models/Dungeon/Door/DoorLocked"));
                doorLockedTexAttribute[doorLockedTexAttribute.length-1] = TextureAttribute.createDiffuse(world.pack.findRegion("Models/Dungeon/Door/Door"));

                setFloorMap(floorMap);
        }

        public void setFloorMap(FloorMap floorMap){
                this.floorMap = floorMap;
                if(!isInitialized())
                        return;

                if( world.getLocalPlayerToken() != null && world.getLocalPlayerToken().fogMapping != null){
                        fogMap = world.getLocalPlayerToken().fogMapping.getFogMap(floorMap);
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
                DecalNodePit.topWallVisibleY = -tileDimensions.y;
                DecalNodePit.sideWallVisibleY = -tileDimensions.y/2f;

                Stairs stairsDown = floorMap.getStairsDown();

                decalNodes.clear();
                for (int x = 0; x < floorMap.getWidth(); x++) {
                        for (int y = 0; y < floorMap.getHeight(); y++) {
                                Tile tile = floorMap.getTile(x,y);
                                if(tile != null){
                                        if(tile.isPit()){
                                                makeDecalPit(tile, x,y);
                                        }else if(tile.isWall()){
                                                makeDecalWall(tile, x, y);
                                        }else{
                                                makeDecalFloor(tile, x,y, stairsDown);
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

        public Color getDecalNodeColorAt(int x, int y){
                for (DecalNode decalNode : decalNodes) {
                        if(decalNode.x==x && decalNode.y ==y){
                                return decalNode.decal.getColor();
                        }
                }
                throw new IllegalArgumentException("No decal node at the coordinates "+x+", "+y);
        }

        @Override
        public void dispose() {
                initialized = false;
        }

        @Override
        public boolean isInitialized() {
                return initialized;
        }

        private static int isFloorOrPit(Tile t){
                return t.isFloor() || t.isPit() ? 1 : 0;
        }

        private Direction whichDirectionToFaceDoor(int x, int y){
                Tile nw = floorMap.getTile(x-1,y+1);
                Tile n = floorMap.getTile(x,y+1);
                Tile ne = floorMap.getTile(x+1,y+1);
                int northCount = isFloorOrPit(nw) + isFloorOrPit(n) + isFloorOrPit(ne);
                if(northCount == 3) return Direction.North;
                Tile e = floorMap.getTile(x+1,y);
                Tile se = floorMap.getTile(x+1,y-1);
                int eastCount = isFloorOrPit(ne) + isFloorOrPit(e) + isFloorOrPit(se);
                if(eastCount == 3) return Direction.East;
                Tile s = floorMap.getTile(x,y-1);
                Tile sw = floorMap.getTile(x-1,y-1);
                int southCount = isFloorOrPit(se) + isFloorOrPit(s) + isFloorOrPit(sw);
                if(southCount == 3) return Direction.South;
                Tile w = floorMap.getTile(x-1,y);
                int westCount = isFloorOrPit(sw) + isFloorOrPit(w) + isFloorOrPit(nw);
                if(westCount == 3) return Direction.West;

                if(northCount == 2) return Direction.North;
                if(eastCount == 2) return Direction.East;
                if(southCount == 2) return Direction.South;
                if(westCount == 2) return Direction.West;


                if(isFloorOrPit(n)==1 && isFloorOrPit(s)==1) return Direction.South;
                return Direction.West;
        }

        private void makeDecalFloor(Tile tile, int x, int y, Stairs stairs) {
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
                }else if(stairs != null && stairs.getLocation().equals(x,y)){
                        decalNode = new DecalNodeStairsDown();
                }else{
                        decalNode = new DecalNodeFloor();
                }


                decalNode.x = x;
                decalNode.y = y;
                decalNode.tile = tile;
                decalNodes.add(decalNode);

                TextureRegion tr;
                if(floorMap.getPressurePlateAt(x,y) == null){
                        // choose a textue region without a pressure plate

                        tr = floorTexRegions[MathUtils.random(1, floorTexRegions.length - 1)][MathUtils.random.nextInt(floorTexRegions[0].length)];
                }else{
                        // chose a texture region with a pressure plate
                        tr = floorTexRegions[0][MathUtils.random.nextInt(floorTexRegions[0].length)];
                }

                decalNode.decal = new Decal();
                decalNode.decal.setTextureRegion(tr);
                decalNode.decal.setBlending(DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                decalNode.decal.setDimensions(tileDimensions.x, tileDimensions.z);
                decalNode.decal.setColor(0,0,0,1);
                decalNode.decal.rotateX(-90);
                decalNode.decal.translate(worldCoordsTemp.x, 0, worldCoordsTemp.z);
        }

        private TextureRegion wallRegion(){
                return wallTexRegions[MathUtils.random(0,3)][MathUtils.random(0,1)];
        }

        private TextureRegion wallDarkRegion(){
                return wallTexRegions[MathUtils.random(0,3)][MathUtils.random(2,3)];
        }

        private TextureRegion pitBottomRegion(){
                return pitTexRegions[MathUtils.random(0,3)][MathUtils.random(0,1)];
        }

        private TextureRegion pitSideRegion(){
                return pitTexRegions[MathUtils.random(0,3)][MathUtils.random(2,3)];
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
                decalNode.decal.setTextureRegion(wallRegion());
                decalNode.decal.setBlending(DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                decalNode.decal.setDimensions(tileDimensions.x, tileDimensions.z);
                decalNode.decal.setColor(0,0,0,1);
                decalNode.decal.rotateX(-90);
                decalNode.decal.translate(worldCoordsTemp.x, 0, worldCoordsTemp.z);
                // visiblyY = tileDimensions.y

                // north
                decalNode.decals[0] = new Decal();
                decalNode.decals[0].setTextureRegion(wallRegion());
                decalNode.decals[0].setBlending(DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                decalNode.decals[0].setDimensions(tileDimensions.x, tileDimensions.y);
                decalNode.decals[0].setColor(0,0,0,1);
                decalNode.decals[0].translate(worldCoordsTemp.x, -tileDimensions.y / 2f, worldCoordsTemp.z - tileDimensions.z / 2f);
                // visiblyY = tileDimensions.y/2f

                // south
                decalNode.decals[1] = new Decal();
                decalNode.decals[1].setTextureRegion(wallDarkRegion());
                decalNode.decals[1].setBlending(DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                decalNode.decals[1].setDimensions(tileDimensions.x, tileDimensions.y);
                decalNode.decals[1].setColor(0,0,0,1);
                decalNode.decals[1].translate(worldCoordsTemp.x, -tileDimensions.y/2f, worldCoordsTemp.z+tileDimensions.z/2f);
                // visiblyY = tileDimensions.y/2f

                // east
                decalNode.decals[2] = new Decal();
                decalNode.decals[2].setTextureRegion(wallDarkRegion());
                decalNode.decals[2].setBlending(DecalMaterial.NO_BLEND, DecalMaterial.NO_BLEND);
                decalNode.decals[2].setDimensions(tileDimensions.z, tileDimensions.y);
                decalNode.decals[2].setColor(0, 0, 0, 1);
                decalNode.decals[2].rotateY(-90);
                decalNode.decals[2].translate(worldCoordsTemp.x+tileDimensions.x/2f, -tileDimensions.y/2f, worldCoordsTemp.z);
                // visiblyY = tileDimensions.y/2f

                // west
                decalNode.decals[3] = new Decal();
                decalNode.decals[3].setTextureRegion(wallRegion());
                decalNode.decals[3].setBlending(DecalMaterial.NO_BLEND, DecalMaterial.NO_BLEND);
                decalNode.decals[3].setDimensions(tileDimensions.z, tileDimensions.y);
                decalNode.decals[3].setColor(0, 0, 0, 1);
                decalNode.decals[3].rotateY(-90);
                decalNode.decals[3].translate(worldCoordsTemp.x-tileDimensions.x/2f, -tileDimensions.y/2f, worldCoordsTemp.z);
                // visiblyY = tileDimensions.y/2f


        }

        private void makeDecalPit(Tile tile, int x, int y){
                DecalNodePit decalNode = new DecalNodePit();
                decalNode.x = x;
                decalNode.y = y;
                decalNode.tile = tile;
                decalNode.decals = new Decal[4];
                decalNodes.add(decalNode);

                world.getWorldCoords(x, y, worldCoordsTemp);

                // top
                decalNode.decal = new Decal();
                decalNode.decal.setTextureRegion(pitBottomRegion());
                decalNode.decal.setBlending(DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                decalNode.decal.setDimensions(tileDimensions.x, tileDimensions.z);
                decalNode.decal.setColor(0,0,0,1);
                decalNode.decal.rotateX(-90);
                decalNode.decal.translate(worldCoordsTemp.x, 0, worldCoordsTemp.z);
                // visiblyY = tileDimensions.y

                // north
                Tile northTile = floorMap.getTile(x,y+1);
                if(northTile == null || !northTile.isPit()){
                        decalNode.decals[0] = new Decal();
                        decalNode.decals[0].setTextureRegion(pitSideRegion());
                        decalNode.decals[0].setBlending(DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                        decalNode.decals[0].setDimensions(tileDimensions.x, tileDimensions.y);
                        decalNode.decals[0].setColor(0,0,0,1);
                        decalNode.decals[0].translate(worldCoordsTemp.x, -tileDimensions.y / 2f, worldCoordsTemp.z - tileDimensions.z / 2f);
                }

                // south
                Tile southTile = floorMap.getTile(x,y-1);
                if(southTile == null || !southTile.isPit()){
                        decalNode.decals[1] = new Decal();
                        decalNode.decals[1].setTextureRegion(pitSideRegion());
                        decalNode.decals[1].setBlending(DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                        decalNode.decals[1].setDimensions(tileDimensions.x, tileDimensions.y);
                        decalNode.decals[1].setColor(0,0,0,1);
                        decalNode.decals[1].translate(worldCoordsTemp.x, -tileDimensions.y/2f, worldCoordsTemp.z+tileDimensions.z/2f);
                }

                // east
                Tile eastTile = floorMap.getTile(x+1, y);
                if(eastTile == null || !eastTile.isPit()){
                        decalNode.decals[2] = new Decal();
                        decalNode.decals[2].setTextureRegion(pitSideRegion());
                        decalNode.decals[2].setBlending(DecalMaterial.NO_BLEND, DecalMaterial.NO_BLEND);
                        decalNode.decals[2].setDimensions(tileDimensions.z, tileDimensions.y);
                        decalNode.decals[2].setColor(0, 0, 0, 1);
                        decalNode.decals[2].rotateY(-90);
                        decalNode.decals[2].translate(worldCoordsTemp.x+tileDimensions.x/2f, -tileDimensions.y/2f, worldCoordsTemp.z);
                }

                // west
                Tile westTile = floorMap.getTile(x-1, y);
                if(westTile==null || !westTile.isPit()){
                        decalNode.decals[3] = new Decal();
                        decalNode.decals[3].setTextureRegion(pitSideRegion());
                        decalNode.decals[3].setBlending(DecalMaterial.NO_BLEND, DecalMaterial.NO_BLEND);
                        decalNode.decals[3].setDimensions(tileDimensions.z, tileDimensions.y);
                        decalNode.decals[3].setColor(0, 0, 0, 1);
                        decalNode.decals[3].rotateY(-90);
                        decalNode.decals[3].translate(worldCoordsTemp.x-tileDimensions.x/2f, -tileDimensions.y/2f, worldCoordsTemp.z);
                }

        }


        private abstract static class DecalNode{
                public Decal decal;
                public int x;
                public int y;
                public Tile tile;

                protected abstract void render(FloorSpatial floor, float delta);


        }

        // TODO: could do a small optimzation by not calling decal.setColor() when the color hasnt actually changed.
        // decal.setColor calls native code which should be avoided on android

        private static class DecalNodeFloor extends DecalNode{


                @Override
                protected void render(FloorSpatial floor, float delta) {
                        Color color = decal.getColor();
                        FogState fogState = floor.fogMap.getFogState(x,y);
                        float fog = MathUtils.lerp(color.g, floor.fogAlpha[fogState.ordinal()], delta);
                        if(fog > 0 && (floor.world.hudSpatial.isMapViewMode() || floor.world.getLocalPlayerToken().location.distance(x,y)<16)){
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

        private static class DecalNodeStairsDown extends DecalNodeFloor{
                // This DecalNode is bassicaly the same as DecalNodeFloor just it uses VisibleY to make sure that the deacl
                // is drawn below the stairs model
                public static float visibleY = -3.753f;

                @Override
                protected void render(FloorSpatial floor, float delta) {
                        Color color = decal.getColor();
                        FogState fogState = floor.fogMap.getFogState(x,y);
                        float fog = MathUtils.lerp(color.g, floor.fogAlpha[fogState.ordinal()], delta);
                        if(fog > 0 && (floor.world.hudSpatial.isMapViewMode() || floor.world.getLocalPlayerToken().location.distance(x,y)<16)){
                                if(decal.getPosition().y != visibleY){
                                        decal.setY(visibleY);

                                }
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
                public Decal[] decals; // side wall decals, the top wall decal is the superclass decal
                public static float topWallVisibleY;
                public static float sideWallVisibleY;

                @Override
                protected void render(FloorSpatial floor, float delta) {
                        Color color = decal.getColor();
                        FogState fogState = floor.fogMap.getFogState(x,y);
                        float fog = MathUtils.lerp(color.g, floor.fogAlpha[fogState.ordinal()], delta);
                        if(fog > 0 && (floor.world.hudSpatial.isMapViewMode() || floor.world.getLocalPlayerToken().location.distance(x,y)<16)){
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

        private static class DecalNodePit extends DecalNode{
                public Decal[] decals; // side wall decals, the top wall decal is the superclass decal
                public static float topWallVisibleY;
                public static float sideWallVisibleY;

                @Override
                protected void render(FloorSpatial floor, float delta) {
                        Color color = decal.getColor();
                        FogState fogState = floor.fogMap.getFogState(x,y);
                        float fog = MathUtils.lerp(color.g, floor.fogAlpha[fogState.ordinal()], delta);
                        if(fog > 0 && (floor.world.hudSpatial.isMapViewMode() || floor.world.getLocalPlayerToken().location.distance(x,y)<16)){
                                if(decal.getPosition().y != topWallVisibleY){
                                        decal.setY(topWallVisibleY);
                                        for (Decal wallDecal : decals){
                                                if(wallDecal != null)
                                                        wallDecal.setY(sideWallVisibleY);
                                        }

                                }
                                if(fogState == FogState.MagicMapped)
                                        color.set(fog * 0.9f, fog, fog * 1.2f, 1);
                                else
                                        color.set(fog,fog,fog,1);
                                decal.setColor(color);
                                floor.world.decalBatch.add(decal);
                                for (Decal wallDecal : decals) {
                                        if(wallDecal != null){
                                                wallDecal.setColor(color);
                                                floor.world.decalBatch.add(wallDecal);
                                        }
                                }
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
                protected void render(FloorSpatial floor, float delta) {
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
                        if(fog > 0 && (floor.world.hudSpatial.isMapViewMode() || floor.world.getLocalPlayerToken().location.distance(x,y)<16)){
                                if(fogState == FogState.MagicMapped)
                                        color.set(fog * 0.9f, fog, fog * 1.2f, 1);
                                else
                                        color.set(fog,fog,fog,1);
                                decal.setColor(color);
                                floor.world.decalBatch.add(decal);


                                TextureAttribute doorTexAttribute = getDoorTexAttribute(tile, floor.doorLockedTexAttribute);
                                modelInstance.materials.get(0).set(doorTexAttribute);

                                colorAttribute.color.r = UtMath.clamp(color.r+.1f, 0f,1f);
                                colorAttribute.color.g = UtMath.clamp(color.g+.1f, 0f,1f);
                                colorAttribute.color.b = UtMath.clamp(color.b+.1f, 0f,1f);
                                floor.world.modelBatch.render(modelInstance, floor.world.environment);
                        }else{
                                color.g = fog;
                        }


                }

                private TextureAttribute getDoorTexAttribute(Tile doorTile, TextureAttribute[] doorLockedTexAttribute){
                        if(doorTile.doorSymbol == null){
                                return doorLockedTexAttribute[doorLockedTexAttribute.length-1];
                        }

                        if(doorTile.doorSymbol instanceof KeyItem){
                                KeyItem keyItem = (KeyItem) doorTile.doorSymbol;
                                if(doorTile.isDoorLocked())
                                        return doorLockedTexAttribute[keyItem.getType().ordinal()];
                                else
                                        return doorLockedTexAttribute[keyItem.getType().ordinal()+3];
                        }else{
                                return doorLockedTexAttribute[doorLockedTexAttribute.length-2];
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
