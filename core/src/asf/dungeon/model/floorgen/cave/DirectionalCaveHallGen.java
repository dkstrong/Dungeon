package asf.dungeon.model.floorgen.cave;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FloorType;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.floorgen.UtFloorGen;

/**
 *
 * Makes a cave like structure similiar to Cellular Automata, but the cave is much likea long hallway
 *
 * http://www.roguebasin.com/index.php?title=Basic_directional_dungeon_generation
 *
 * Created by Danny on 11/4/2014.
 */
public class DirectionalCaveHallGen implements FloorMapGenerator {


        private int minFloorWidth = 45;
        private int maxFloorWidth = 50;
        private int minFloorHeight = 30;
        private int maxFloorHeight = 45;


        private int numberOfGenerations = 2;
        private float roughness = .25f; // [0,1] How much the cave varies in width. This should be a rough value, and should not reflect exactly in the level created.
        private float windyness = .95f; // [0,1] How much the cave varies in positioning. How much a path through it needs to 'wind' and 'swerve'.


        @Override
        public FloorMap generate(Dungeon dungeon, FloorType floorType, int floorIndex) {


                int floorWidth = dungeon.rand.range(minFloorWidth, maxFloorWidth);
                int floorHeight = dungeon.rand.range(minFloorHeight, maxFloorHeight);

                Tile[][] tiles = new Tile[floorWidth][floorHeight];

//                for (int x = 0; x < tiles.length; x++){
//                        for (int y = 0; y < tiles[0].length; y++){
//                                tiles[x][y] = Tile.makeWall();
//                        }
//                }

                for(int k=0;k< numberOfGenerations; k++){
                        generate(dungeon, tiles);
                }



                UtFloorGen.ensureFloorsAreSurroundedWithWalls(tiles);
                UtFloorGen.floodFillSmallerAreas(tiles);

                FloorMap floorMap = new FloorMap(floorType, floorIndex, tiles);
                UtFloorGen.placeUpStairs(dungeon, floorMap);
                UtFloorGen.placeDownStairs(dungeon, floorMap);
                UtFloorGen.spawnCharacters(dungeon, floorMap);
                UtFloorGen.spawnRandomCrates(dungeon, floorMap,8,4);
                return floorMap;
        }


        private void generate(Dungeon dungeon, Tile[][] tiles){
                int currentWidth = 3;
                int x = Math.round(tiles.length/2f);
                for(int y= 1; y < tiles[0].length; y++){
                        tiles[x][y] = Tile.makeFloor();
                        if(dungeon.rand.bool(roughness)){
                                int val = dungeon.rand.range(1, 2) * dungeon.rand.sign();
                                currentWidth += val;
                                if(currentWidth <3) currentWidth =3;
                                else if(currentWidth > tiles.length-2) currentWidth = tiles.length -2;

                        }

                        if(dungeon.rand.bool(windyness)){
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
