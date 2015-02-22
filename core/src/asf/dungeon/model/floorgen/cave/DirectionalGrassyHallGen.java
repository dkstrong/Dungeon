package asf.dungeon.model.floorgen.cave;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FloorType;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.floorgen.UtFloorGen;
import asf.dungeon.model.token.Stairs;
import asf.dungeon.model.token.Token;

/**
 *
 * based off of DirectionalCaveHallGen. Meant to be the first level of the game, only really
 * intended to be used with FloorType.Grassy
 *
 * Created by Danny on 11/4/2014.
 */
public class DirectionalGrassyHallGen implements FloorMapGenerator {


        private int minFloorWidth = 18;
        private int maxFloorWidth = 25;
        private int minFloorHeight = 45;
        private int maxFloorHeight = 55;

        private float roughness = .15f; // [0,1] How much the cave varies in width. This should be a rough value, and should not reflect exactly in the level created.
        private float windyness = .8f; // [0,1] How much the cave varies in positioning. How much a path through it needs to 'wind' and 'swerve'.

        private static final int finishYOffset = 4;

        @Override
        public FloorMap generate(Dungeon dungeon, FloorType floorType, int floorIndex) {


                int floorWidth = dungeon.rand.range(minFloorWidth, maxFloorWidth);
                int floorHeight = dungeon.rand.range(minFloorHeight, maxFloorHeight);

                int spawnX = Math.round(floorWidth/ 2f);
                int spawnY = 2;

                Tile[][] tiles = new Tile[floorWidth][floorHeight];

                generate(dungeon, tiles, spawnX, spawnY + 5);
                //generate(dungeon, tiles, spawnX, spawnY + 5); // TOOD: if i do second genertion it should end on the path of the first generation
                generateStartingArea(dungeon, tiles, spawnX, spawnY);
                int endX = generateFinishingArea(dungeon, tiles, spawnX);
                UtFloorGen.ensureFloorsAreSurroundedWithWalls(tiles);
                //UtFloorGen.floodFillSmallerAreas(tiles);
                //UtFloorGen.printFloorTile(tiles);

                FloorMap floorMap = new FloorMap(floorType, floorIndex, tiles, null);
                placeUpStairs(dungeon, floorMap, spawnX, spawnY, endX);
                //UtFloorGen.spawnCharacters(dungeon, floorMap);
                UtFloorGen.spawnRandomCrates(dungeon, floorMap,5,4);
                return floorMap;
        }

        private void placeUpStairs(Dungeon dungeon, FloorMap floorMap, int spawnX, int spawnY, int endX){
                Token stairsToken = new Token(dungeon, "Stairs", null);
                stairsToken.add(new Stairs(stairsToken, floorMap.index - 1));
                stairsToken.direction = Direction.East;
                dungeon.addToken(stairsToken, floorMap, spawnX, spawnY);

                Token stairsDownToken = new Token(dungeon, "Stairs", null);
                stairsDownToken.add(new Stairs(stairsDownToken, floorMap.index + 1));
                stairsDownToken.direction = Direction.South;
                dungeon.addToken(stairsDownToken, floorMap, endX, floorMap.tiles[0].length - finishYOffset);
        }

        private void generateStartingArea(Dungeon dungeon, Tile[][] tiles, int x, int y){
                for(int i=x-2; i < x+2; i++){
                        tiles[i][0] = Tile.makeWall();
                        for(int j =1; j <y+3; j++){
                                tiles[i][j] = Tile.makeFloor();
                        }
                }

                for(int i=x-1; i < x+1; i++){
                        for(int j =y+3; j <y+5; j++){
                                tiles[i][j] = Tile.makeFloor();
                        }
                }

                for(int i=x-2; i < x+2; i++){
                        for(int j =y+5; j <y+7; j++){
                                tiles[i][j] = Tile.makeFloor();
                        }
                }
        }

        private int generateFinishingArea(Dungeon dungeon, Tile[][] tiles, int spawnX){
                int finishX = spawnX;
                final int finishY = tiles[0].length-finishYOffset;
                int xExtent =Math.round(tiles.length / 2f);
                Tile testTile = tiles[finishX][finishY];
                if(testTile== null || !testTile.isFloor()){
                        for(int i=0; i < xExtent; i++){
                                if(spawnX+i < tiles.length){
                                        Tile t = tiles[spawnX+i][finishY];
                                        if(t != null && t.isFloor()){
                                                finishX=spawnX+i;
                                                break;
                                        }
                                }
                                if(spawnX-i >=0){
                                        Tile t = tiles[spawnX-i][finishY];
                                        if(t != null && t.isFloor()){
                                                finishX=spawnX-i;
                                                break;
                                        }
                                }
                        }
                }

                for(int i= 0; i < tiles.length; i++){
                        for(int j = tiles[0].length-1; j > finishY-7; j--){
                                tiles[i][j] = null;
                                tiles[i][j] = null;
                        }
                }

                for(int i=0; i < 6; i++){
                        for(int j = finishY-1; j > finishY-7; j--){
                                tiles[finishX+i][j] = Tile.makeFloor();
                                tiles[finishX-i][j] = Tile.makeFloor();
                        }
                }

                for(int j =tiles[0].length-2; j >=finishY; j--){
                        tiles[finishX][j] = Tile.makeInvisibleWall();
                        tiles[finishX+1][j] = Tile.makeInvisibleWall();
                        tiles[finishX-1][j] = Tile.makeInvisibleWall();
                }

                for(int i=2; i <= 3; i++){
                        for(int j =tiles[0].length-2; j >=finishY; j--){
                                tiles[finishX+i][j] = Tile.makeFloor();
                                tiles[finishX-i][j] = Tile.makeFloor();
                        }
                }


                tiles[finishX][finishY] = Tile.makeFloor();


                return finishX;

        }


        private void generate(Dungeon dungeon, Tile[][] tiles, int x, int startY){
                int currentWidth = 3;
                float windynessMod = 0;

                for(int y= startY; y < tiles[0].length; y++){
                        tiles[x][y] = Tile.makeFloor();
                        if(dungeon.rand.bool(roughness)){
                                int val = dungeon.rand.range(1, 2) * dungeon.rand.sign();
                                currentWidth += val;
                                if(currentWidth <3) currentWidth =3;
                                else if(currentWidth > tiles.length-2) currentWidth = tiles.length -2;

                        }
                        windynessMod = y / tiles[0].length * 0.2f;

                        if(dungeon.rand.bool(windyness+windynessMod)){
                                int val = dungeon.rand.range(1, 2) * dungeon.rand.sign();
                                x+= val;
                        }
                        if(x <0) x= 0;
                        else if(x > tiles.length-currentWidth) x = tiles.length-currentWidth;

                        carveRow(tiles, x, y, currentWidth);

                }
        }
        private void carveRow(Tile[][] tiles, int x, int y, int rowWidth){
                for(int i=x; i < x+rowWidth; i++){
                        tiles[i][y] = Tile.makeFloor();
                }
        }








}
