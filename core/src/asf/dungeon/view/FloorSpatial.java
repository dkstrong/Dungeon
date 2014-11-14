package asf.dungeon.view;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Tile;
import asf.dungeon.utility.UtMath;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalMaterial;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.fogmap.FogState;
import asf.dungeon.model.Pair;
import asf.dungeon.view.shape.CustomBox;

import java.util.Iterator;

/**
 * Created by danny on 10/29/14.
 */
public class FloorSpatial implements Spatial {
        public final Vector3 tileDimensions = new Vector3(5, 5.75f, 5);
        public final CustomBox tokenCustomBox = new CustomBox(new Vector3(-tileDimensions.x / 2f, 0, -tileDimensions.z / 2f), new Vector3(tileDimensions.x / 2f, tileDimensions.y, tileDimensions.z / 2f));
        private DungeonWorld world;
        private FloorMap floorMap;
        private FogMap fogMap;
        private Array<DecalNode> decalNodes;
        private Texture floorTex, wallTex, wallTexDark, stairDownTex, stairUpTex;
        private TextureRegion[][] floorTexRegions, wallTexRegions;
        private float[] fogAlpha;
        private boolean initialized;
        private Array<DecalNodeProp> decalNodeProps, decalNodePropsTemp;


        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
                makeCommonAssets();

        }

        @Override
        public void init(AssetManager assetManager) {
                this.initialized = true;
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
                        fogMap = null;
                }

                fogAlpha[FogState.Dark.ordinal()] = 0;
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
                        DecalNode decalNode = decalNodes.get(i);
                        // TODO: if there is no local player token, then all decals will be rendered
                        //  should maybe have a back up test using distance to camera
                        if(world.getLocalPlayerToken()!= null && world.getLocalPlayerToken().getLocation().distance(decalNode.x, decalNode.y) > 20){
                                continue;
                        }
                        Decal decal = decalNode.decal;
                        Color color = decal.getColor();
                        boolean tileVisited;
                        float fog;
                        if(fogMap == null){
                                tileVisited = true;
                                fog = fogAlpha[FogState.Visible.ordinal()];
                        }else{
                                FogState fogState = fogMap.getFogState(decalNode.x, decalNode.y);
                                tileVisited = fogState != FogState.Dark;
                                fog = fogAlpha[fogState.ordinal()];
                        }

                        if(tileVisited && decal.getPosition().y != decalNode.visibleY){
                                decal.getPosition().y = decalNode.visibleY; // if this is the first time this decal tile is "visited" then it needs to "pop up" in to position
                                decal.setPosition(decal.getPosition());
                        }

                        fog = MathUtils.lerp(color.r, fog, delta);
                        color.set(fog,fog,fog,1);
                        decal.setColor(color);
                        world.decalBatch.add(decalNode.decal);

                        DecalNodeProp prop = decalNode.prop;
                        if(prop != null){
                                if(decalNode.tile.isDoor()){
                                        if(decalNode.tile.isDoorOpened()){
                                                prop.animController.setAnimation("Open",1);
                                        }else{
                                                prop.animController.setAnimation("Open",1,-1,null);
                                        }
                                }

                                if(prop.animController != null)
                                        prop.animController.update(delta);

                                prop.colorAttribute.color.a = fog;

                                world.modelBatch.render(prop.modelInstance, world.environment);
                        }
                }
        }

        @Override
        public boolean isInitialized() {
                return initialized;
        }

        @Override
        public void dispose() {
                if(floorTex != null)
                        floorTex.dispose();

                if(wallTex != null)
                        wallTex.dispose();

                if(wallTexDark!=null)
                        wallTexDark.dispose();

                if(stairDownTex != null)
                        stairDownTex.dispose();

                if(stairUpTex!= null)
                        stairUpTex.dispose();
        }



        private void makeCommonAssets(){

                //Texture texture = new Texture(Gdx.files.internal("Models/tiles.png"));
                //TextureRegion[][] tiles = TextureRegion.split(texture, 32, 32);

                fogAlpha = new float[3];
                decalNodes = new Array<DecalNode>(128);
                decalNodeProps = new Array<DecalNodeProp>(8);
                decalNodePropsTemp = new Array<DecalNodeProp>(8);


                //pixmap.setColor(new Color(0.45f, 0.45f, 0, 1));
                //pixmap.fill();

                floorTex = new Texture(Gdx.files.internal("Textures/Dungeon/floorTiles.png"));
                floorTexRegions = TextureRegion.split(floorTex, 128, 128);


                //pixmap.setColor(new Color(0,0,.55f,1));
                //pixmap.fill();
                wallTex =new Texture(Gdx.files.internal("Textures/Dungeon/wallTiles.png"));
                wallTexRegions = TextureRegion.split(wallTex, 256, 256);

                Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pixmap.setColor(new Color(0,0,.35f,1));
                pixmap.fill();
                wallTexDark = new Texture(pixmap);

                pixmap.setColor(new Color(0,0.085f,.085f,1));
                pixmap.fill();
                stairDownTex = new Texture(pixmap);

                pixmap.setColor(new Color(0.85f,0.85f,0,1));
                pixmap.fill();
                stairUpTex = new Texture(pixmap);

                pixmap.dispose();

                world.assetManager.load("Models/Dungeon/Stairs/StairsUp.g3db", Model.class);
                world.assetManager.load("Models/Dungeon/Stairs/StairsDown.g3db", Model.class);
                world.assetManager.load("Models/Dungeon/Door/Door.g3db", Model.class);
        }




        private void makeDecals(){
                decalNodes.clear();
                decalNodePropsTemp.addAll(decalNodeProps);
                decalNodeProps.clear();

                for (int x = 0; x < floorMap.getWidth(); x++) {
                        for (int y = 0; y < floorMap.getHeight(); y++) {
                                Tile tile = floorMap.getTile(x,y);
                                if(tile != null){
                                        if(tile.isBlockMovement()){
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
                if(nw.isFloor() && n.isFloor() && ne.isFloor())
                        return Direction.North;
                Tile e = floorMap.getTile(x+1,y);
                Tile se = floorMap.getTile(x+1,y-1);
                if(ne.isFloor() && e.isFloor() && se.isFloor())
                        return Direction.East;
                Tile s = floorMap.getTile(x,y-1);
                Tile sw = floorMap.getTile(x-1,y-1);
                if(se.isFloor() && s.isFloor() && sw.isFloor())
                        return Direction.South;
                Tile w = floorMap.getTile(x-1,y);
                if(sw.isFloor() && w.isFloor() && nw.isFloor())
                        return Direction.West;

                if(n.isFloor() && s.isFloor())
                        return Direction.South;

                return Direction.West;
        }

        private Vector3 worldCoordsTemp = new Vector3();

        private void makeDecalFloor(Tile tile, int x, int y) {
                getWorldCoords(x, y, worldCoordsTemp);
                DecalNodeProp prop;
                Texture tex;
                float visibleY = 0;
                // "Models/Dungeon/Door/Door.g3db"
                if(tile.isDoor()){
                        tex = floorTex;
                        String asset = "Models/Dungeon/Door/Door.g3db";
                        prop = makeDecalNodeProp(asset);
                        Quaternion rot = whichDirectionToFaceDoor(x,y).quaternion;
                        prop.modelInstance.transform.set(
                                worldCoordsTemp.x,worldCoordsTemp.y,worldCoordsTemp.z,
                                rot.x,rot.y,rot.z,rot.w,
                                1,1,1
                        );
                        decalNodeProps.add(prop);
                }else if(tile.isStairs()){

                        String asset;
                        if(tile.isStairsUp(floorMap.index)){
                                tex = stairUpTex;
                                asset = "Models/Dungeon/Stairs/StairsUp.g3db";
                        }else{
                                tex = stairDownTex;
                                asset = "Models/Dungeon/Stairs/StairsDown.g3db";
                                visibleY = -3.753f;
                        }
                        prop = makeDecalNodeProp(asset);
                        Quaternion rot = Direction.random().quaternion;
                        prop.modelInstance.transform.set(
                                worldCoordsTemp.x,worldCoordsTemp.y,worldCoordsTemp.z,
                                rot.x,rot.y,rot.z,rot.w,
                                1,1,1
                        );
                        decalNodeProps.add(prop);
                }else{
                        tex = floorTex;
                        prop = null;
                }
                TextureRegion region;
                if(tex == floorTex){
                        region = floorTexRegions[MathUtils.random.nextInt(floorTexRegions.length)][MathUtils.random.nextInt(floorTexRegions[0].length)];
                }else{
                        region = new TextureRegion(tex);
                }

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
                TextureRegion topEegion = wallTexRegions[MathUtils.random.nextInt(wallTexRegions.length)][MathUtils.random.nextInt(wallTexRegions[0].length)];
                Decal decal = Decal.newDecal(
                        tileDimensions.x,
                        tileDimensions.z,
                        topEegion,
                        DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                // GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA
                decal.rotateX(-90);
                decal.translate(worldCoordsTemp.x, 0, worldCoordsTemp.z);
                decal.setColor(0,0,0,1);
                decalNodes.add(new DecalNode(decal, x, y, tile,tileDimensions.y));

                // north
                Tile northTile = null;//floorMap.getTile(x,y+1);
                if(northTile == null || !northTile.isBlockMovement()){
                        TextureRegion region = wallTexRegions[MathUtils.random.nextInt(wallTexRegions.length)][MathUtils.random.nextInt(wallTexRegions[0].length)];
                        decal = Decal.newDecal(
                                tileDimensions.x,
                                tileDimensions.y,
                                region,
                                DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                        //decal.rotateX(-90);
                        decal.translate(worldCoordsTemp.x, -tileDimensions.y/2f, worldCoordsTemp.z-tileDimensions.z/2f);
                        decal.setColor(0,0,0,1);
                        decalNodes.add(new DecalNode(decal, x, y, tile,tileDimensions.y/2f));
                }

                // south
                Tile southTile = null;//floorMap.getTile(x,y-1);
                if(southTile == null || !southTile.isBlockMovement()){
                        TextureRegion region = wallTexRegions[MathUtils.random.nextInt(wallTexRegions.length)][MathUtils.random.nextInt(wallTexRegions[0].length)];
                        decal = Decal.newDecal(
                                tileDimensions.x,
                                tileDimensions.y,
                                region, // dark
                                DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                        //decal.rotateX(-90);
                        decal.translate(worldCoordsTemp.x, -tileDimensions.y/2f, worldCoordsTemp.z+tileDimensions.z/2f);
                        decal.setColor(0,0,0,1);
                        decalNodes.add(new DecalNode(decal, x, y, tile,tileDimensions.y/2f));
                }

                // east
                Tile eastTile = null;//floorMap.getTile(x+1,y);
                if(eastTile == null || !eastTile.isBlockMovement()){
                        TextureRegion region = wallTexRegions[MathUtils.random.nextInt(wallTexRegions.length)][MathUtils.random.nextInt(wallTexRegions[0].length)];
                        decal = Decal.newDecal(
                                tileDimensions.z,
                                tileDimensions.y,
                                region,
                                DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                        decal.rotateY(-90);
                        decal.translate(worldCoordsTemp.x+tileDimensions.x/2f, -tileDimensions.y/2f, worldCoordsTemp.z);
                        decal.setColor(0,0,0,1);
                        decalNodes.add(new DecalNode(decal, x, y, tile,tileDimensions.y/2f));
                }

                // west
                Tile westTile = null;//floorMap.getTile(x-1,y);
                if(westTile == null || !westTile.isBlockMovement()){
                        TextureRegion region = wallTexRegions[MathUtils.random.nextInt(wallTexRegions.length)][MathUtils.random.nextInt(wallTexRegions[0].length)];
                        decal = Decal.newDecal(
                                tileDimensions.z,
                                tileDimensions.y,
                                region, // dark
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

        private static class DecalNodeProp {
                public final String assetLocation;
                public ModelInstance modelInstance;
                public AnimationController animController;
                public ColorAttribute colorAttribute;

                private DecalNodeProp(String assetLocation) {
                        this.assetLocation = assetLocation;
                }
        }

        private DecalNodeProp makeDecalNodeProp(String assetLocation){
                Iterator<DecalNodeProp> i = decalNodePropsTemp.iterator();
                while(i.hasNext()){
                        DecalNodeProp next = i.next();
                        if(assetLocation.equals(next.assetLocation)){
                                i.remove();
                                return next;
                        }
                }

                DecalNodeProp prop = new DecalNodeProp(assetLocation);

                prop.modelInstance = new ModelInstance(world.assetManager.get(assetLocation, Model.class));
                if(prop.modelInstance.model.animations.size >0){
                        prop.animController = new AnimationController(prop.modelInstance);
                }


                Material material = prop.modelInstance.materials.get(0);
                material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
                prop.colorAttribute = (ColorAttribute)material.get(ColorAttribute.Diffuse);




                return prop;

        }
}
