package asf.dungeon.model.floorgen;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Tile;

/**
 *
 * makes a floor that is similiar to the classic "Rouge" maps where
 * there are range rooms connected by hallways
 *
 * Created by Danny on 11/4/2014.
 */
public class CellularAutomataGen implements FloorMapGenerator{


        private int minFloorWidth = 45;
        private int maxFloorWidth = 60;
        private int minFloorHeight = 40;
        private int maxFloorHeight = 65;


        private float chanceToStartAlive = 0.45f;   //.48f
        private int numberOfGenerations = 3; // the more generations, the more smooth and open the floor becomes
        private int wallLimit = 4; // higher number means more floors  [0,9]
        private int floorLimit = 4; // lower number means more walls [4,8].

        private int maxCrates = 10;
        private int cratePlacementLimit = 5; // lower number more crates [0-8]


        @Override
        public FloorMap generate(Dungeon dungeon, int floorIndex) {


                int floorWidth = dungeon.rand.range(minFloorWidth, maxFloorWidth);
                int floorHeight = dungeon.rand.range(minFloorHeight, maxFloorHeight);

                Tile[][] tiles = new Tile[floorWidth][floorHeight];

                for (int x = 0; x < tiles.length; x++){
                        for (int y = 0; y < tiles[0].length; y++){
                                if (dungeon.rand.bool(chanceToStartAlive)){
                                        tiles[x][y] = Tile.makeWall();
                                }else{
                                        tiles[x][y] = Tile.makeFloor();
                                }
                        }
                }

                for (int k = 0; k < numberOfGenerations; k++){
                        generate(tiles);
                }

                UtFloorGen.ensureEdgesAreWalls(tiles);

                UtFloorGen.floodFillSmallerAreas(tiles);

                UtFloorGen.placeUpStairs(dungeon, tiles, floorIndex);
                UtFloorGen.placeDownStairs(dungeon, tiles, floorIndex);
                UtFloorGen.printFloorTile(tiles);

                FloorMap floorMap = new FloorMap(floorIndex, tiles);

                UtFloorGen.spawnCharacters(dungeon, floorMap);
                UtFloorGen.spawnTreasuresNearWalls(dungeon, floorMap, maxCrates, cratePlacementLimit);
                return floorMap;
        }


        private void generate(Tile[][] tiles){

                for (int x = 0; x < tiles.length; x++){
                        for (int y = 0; y < tiles[0].length; y++){
                                evolveWalls(tiles, x, y);
                        }
                }
        }



        private void evolveWalls(Tile[][] tiles, int x, int y){
                int numWalls = UtFloorGen.countWalls(tiles, x, y);

                if(UtFloorGen.isWall(tiles, x, y)) {
                        if (wallLimit > numWalls) tiles[x][y] = Tile.makeFloor();
                }else {
                        if (floorLimit < numWalls) tiles[x][y] = Tile.makeWall();
                }

        }





}
