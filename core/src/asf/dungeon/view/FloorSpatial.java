package asf.dungeon.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalMaterial;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import asf.dungeon.board.FloorMap;
import asf.dungeon.board.FloorTile;
import asf.dungeon.board.FogMap;
import asf.dungeon.board.FogState;
import asf.dungeon.board.pathfinder.Pair;
import asf.dungeon.board.pathfinder.Tile;
import asf.dungeon.view.shape.CustomBox;
import asf.dungeon.utility.MoreMath;

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
        private float[] fogAlpha;
        private boolean initialized;


        @Override
        public void preload(DungeonWorld world) {
                this.world = world;
                makeCommonAssets();
        }

        @Override
        public void init(AssetManager assetManager) {
                this.initialized = true;
        }

        public void setFloorMap(FloorMap floorMap){
                Gdx.app.log("FloorDecals","setFloorMap: "+floorMap.index);
                this.floorMap = floorMap;
                if(world.localPlayerToken!= null){
                        if(world.localPlayerToken.getFogMap(floorMap) == null){
                                throw new AssertionError("should not be null");
                        }
                        fogMap = world.localPlayerToken.getFogMap(floorMap);
                }

                fogAlpha = new float[3];
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
                storeMapCoords.y = (int)((MoreMath.abs(worldCoords.z )) / tileDimensions.z);
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
                        if(world.localPlayerToken.getLocation().distance(decalNode.x, decalNode.y) > 20){
                                continue;
                        }
                        Decal decal = decalNode.decal;
                        Color color = decal.getColor();

                        FogState fogState = fogMap.getFogState(decalNode.x, decalNode.y);
                        if(fogState != FogState.Dark && decal.getPosition().y != decalNode.visibleY){
                                decal.getPosition().y = decalNode.visibleY;
                                decal.setPosition(decal.getPosition());
                        }

                        float fog = fogAlpha[fogState.ordinal()];



                        fog = MathUtils.lerp(color.r, fog, delta);
                        color.set(fog,fog,fog,1);
                        decal.setColor(color);
                        world.decalBatch.add(decalNode.decal);
                }
        }

        @Override
        public float intersects(Ray ray) {
                return -1;
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

        private static class DecalNode{
                public Decal decal;
                public int x;
                public int y;
                public Tile tile; // floor tiles start in the sky, then once visited they move to y=0, this is to prevent their sillouete from giving away what they are
                public float visibleY;

                public DecalNode(Decal decal, int x, int y, Tile tile, float visibleY) {
                        this.decal = decal;
                        this.x = x;
                        this.y = y;
                        this.tile = tile;
                        this.visibleY = visibleY;
                }
        }

        private void makeCommonAssets(){
                Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pixmap.setColor(new Color(0.45f, 0.45f, 0, 1));
                pixmap.fill();
                floorTex = new Texture(pixmap);

                pixmap.setColor(new Color(0,0,.55f,1));
                pixmap.fill();
                wallTex =new Texture(pixmap);

                pixmap.setColor(new Color(0,0,.35f,1));
                pixmap.fill();
                wallTexDark = new Texture(pixmap);

                pixmap.setColor(new Color(0,0.85f,.85f,1));
                pixmap.fill();
                stairDownTex = new Texture(pixmap);

                pixmap.setColor(new Color(0.85f,0.85f,0,1));
                pixmap.fill();
                stairUpTex = new Texture(pixmap);

                pixmap.dispose();
        }

        private void makeDecals(){
                decalNodes = new Array<DecalNode>();

                for (int x = 0; x < floorMap.getWidth(); x++) {
                        for (int y = 0; y < floorMap.getHeight(); y++) {
                                FloorTile tile = floorMap.getTile(x,y);
                                if(tile.isBlockMovement()){
                                        makeDecalWall(tile, x, y);
                                }else{
                                        makeDecalFloor(tile, x,y);
                                }

                        }
                }


        }

        private Vector3 temp = new Vector3();

        private void makeDecalFloor(FloorTile tile, int x, int y) {

                Texture tex;

                if(!tile.isStairs()){
                        tex = floorTex;
                }else{
                        if(tile.getStairsTo() < floorMap.index){
                                tex = stairUpTex;
                        }else if(tile.getStairsTo() > floorMap.index){
                                tex = stairDownTex;
                        }else{
                                throw new AssertionError(tile.getStairsTo());
                        }
                }


                getWorldCoords(x, y, temp);



                Decal decal = Decal.newDecal(
                        tileDimensions.x,
                        tileDimensions.z,
                        new TextureRegion(tex),
                        DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                // GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA
                decal.rotateX(-90);
                decal.translate(temp.x, 0, temp.z);
                decal.setColor(0,0,0,1);

                decalNodes.add(new DecalNode(decal, x, y,tile,0));

        }

        private void makeDecalWall(Tile tile, int x, int y){

                getWorldCoords(x, y, temp);



                // top
                Decal decal = Decal.newDecal(
                        tileDimensions.x,
                        tileDimensions.z,
                        new TextureRegion(wallTex),
                        DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                // GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA
                decal.rotateX(-90);
                decal.translate(temp.x, 0, temp.z);
                decal.setColor(0,0,0,1);
                decalNodes.add(new DecalNode(decal, x, y, tile,tileDimensions.y));

                // north
                Tile northTile = null;//floorMap.getTile(x,y+1);
                if(northTile == null || !northTile.isBlockMovement()){
                        decal = Decal.newDecal(
                                tileDimensions.x,
                                tileDimensions.y,
                                new TextureRegion(wallTex),
                                DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                        //decal.rotateX(-90);
                        decal.translate(temp.x, -tileDimensions.y/2f, temp.z-tileDimensions.z/2f);
                        decal.setColor(0,0,0,1);
                        decalNodes.add(new DecalNode(decal, x, y, tile,tileDimensions.y/2f));
                }

                // south
                Tile southTile = null;//floorMap.getTile(x,y-1);
                if(southTile == null || !southTile.isBlockMovement()){
                        decal = Decal.newDecal(
                                tileDimensions.x,
                                tileDimensions.y,
                                new TextureRegion(wallTexDark),
                                DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                        //decal.rotateX(-90);
                        decal.translate(temp.x, -tileDimensions.y/2f, temp.z+tileDimensions.z/2f);
                        decal.setColor(0,0,0,1);
                        decalNodes.add(new DecalNode(decal, x, y, tile,tileDimensions.y/2f));
                }

                // east
                Tile eastTile = null;//floorMap.getTile(x+1,y);
                if(eastTile == null || !eastTile.isBlockMovement()){
                        decal = Decal.newDecal(
                                tileDimensions.z,
                                tileDimensions.y,
                                new TextureRegion(wallTex),
                                DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                        decal.rotateY(-90);
                        decal.translate(temp.x+tileDimensions.x/2f, -tileDimensions.y/2f, temp.z);
                        decal.setColor(0,0,0,1);
                        decalNodes.add(new DecalNode(decal, x, y, tile,tileDimensions.y/2f));
                }

                // west
                Tile westTile = null;//floorMap.getTile(x-1,y);
                if(westTile == null || !westTile.isBlockMovement()){
                        decal = Decal.newDecal(
                                tileDimensions.z,
                                tileDimensions.y,
                                new TextureRegion(wallTexDark),
                                DecalMaterial.NO_BLEND,DecalMaterial.NO_BLEND);
                        decal.rotateY(-90);
                        decal.translate(temp.x-tileDimensions.x/2f, -tileDimensions.y/2f, temp.z);
                        decal.setColor(0,0,0,1);
                        decalNodes.add(new DecalNode(decal, x, y, tile,tileDimensions.y/2f));
                }

        }
}
