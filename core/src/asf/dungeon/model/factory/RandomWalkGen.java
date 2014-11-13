package asf.dungeon.model.factory;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import com.badlogic.gdx.math.MathUtils;

/**
 *
 * makes a floor that is similiar to the classic "Rouge" maps where
 * there are random rooms connected by hallways
 *
 * Created by Danny on 11/4/2014.
 */
public class RandomWalkGen implements FloorMapGenerator{


        private int minFloorWidth = 45;
        private int maxFloorWidth = 60;
        private int minFloorHeight = 40;
        private int maxFloorHeight = 65;





        @Override
        public FloorMap generate(Dungeon dungeon, int floorIndex) {


                Tile[][] tiles = generateTiles(floorIndex);
                UtFloorGen.printFloorTile(tiles);

                FloorMap floorMap = new FloorMap(floorIndex, tiles);

                UtFloorGen.spawnCharacters(dungeon, floorMap);
                UtFloorGen.spawnRandomCrates(dungeon, floorMap);
                return floorMap;
        }

        public Tile[][] generateTiles(int floorIndex){
                int floorWidth = MathUtils.random(minFloorWidth, maxFloorWidth);
                int floorHeight = MathUtils.random(minFloorHeight, maxFloorHeight);

                Tile[][] tiles = new Tile[floorWidth][floorHeight];

                for (int x = 0; x < tiles.length; x++){
                        for (int y = 0; y < tiles[0].length; y++){
                                tiles[x][y] = Tile.makeWall();
                        }
                }
                 int maxFloorTiles = Math.round(floorWidth*floorHeight*.5f);
                int countFlooTiles = 0;

                Pair loc = new Pair(MathUtils.random(0, tiles.length-1),MathUtils.random(0, tiles[0].length-1));
                while(countFlooTiles < maxFloorTiles){
                        if(tiles[loc.x][loc.y].isWall()){
                                tiles[loc.x][loc.y] = Tile.makeFloor();
                                countFlooTiles++;
                        }
                        do{
                                loc.add(Direction.random());
                        }while(loc.x < 0 || loc.x >= tiles.length || loc.y <0 || loc.y >=tiles[0].length);
                }




                UtFloorGen.ensureEdgesAreWalls(tiles);
                UtFloorGen.floodFillSmallerAreas(tiles);
                UtFloorGen.placeUpStairs(tiles, floorIndex);
                UtFloorGen.placeDownStairs(tiles, floorIndex);
                return tiles;
        }








}
