package asf.dungeon.model.floorgen.cave;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FloorType;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.floorgen.UtFloorGen;

/**
 * makes a floor that is similiar to the classic "Rouge" maps where
 * there are range rooms connected by hallways
 * <p/>
 * Created by Danny on 11/4/2014.
 */
public class RandomWalkGen implements FloorMapGenerator {


        private int minFloorWidth = 25;
        private int maxFloorWidth = 35;
        private int minFloorHeight = 20;
        private int maxFloorHeight = 30;


        @Override
        public FloorMap generate(Dungeon dungeon, FloorType floorType, int floorIndex) {


                int floorWidth = dungeon.rand.range(minFloorWidth, maxFloorWidth);
                int floorHeight = dungeon.rand.range(minFloorHeight, maxFloorHeight);

                Tile[][] tiles = new Tile[floorWidth][floorHeight];
                boolean regen;
                float percentage = .55f;
                Pair loc = new Pair();
                Pair locTemp = new Pair();
                do {
                        regen = false;
                        for (int x = 0; x < tiles.length; x++) {
                                for (int y = 0; y < tiles[0].length; y++) {
                                        tiles[x][y] = Tile.makeWall();
                                }
                        }
                        int maxFloorTiles = Math.round(floorWidth * floorHeight * percentage);
                        int countFloorTiles = 0;
                        loc.set(
                                dungeon.rand.range(0, tiles.length - 1),
                                dungeon.rand.range(0, tiles[0].length - 1));
                        mid:
                        while (countFloorTiles < maxFloorTiles) {
                                if (tiles[loc.x][loc.y].isWall()) {
                                        tiles[loc.x][loc.y] = Tile.makeFloor();
                                        countFloorTiles++;
                                }
                                locTemp.set(loc);
                                int tries = 0;
                                do {
                                        loc.set(locTemp);
                                        loc.addFree(dungeon.rand.direction());
                                        if(++tries > 20){
                                                regen = true;
                                                break mid;
                                        }

                                } while (loc.x < 0 || loc.x >= tiles.length || loc.y < 0 || loc.y >= tiles[0].length);
                        }
                } while (regen);

                UtFloorGen.ensureEdgesAreWalls(tiles);

                FloorMap floorMap = new FloorMap(floorType, floorIndex, tiles);
                UtFloorGen.placeUpStairs(dungeon, floorMap);
                UtFloorGen.placeDownStairs(dungeon, floorMap);
                UtFloorGen.spawnCharacters(dungeon, floorMap);
                UtFloorGen.spawnRandomCrates(dungeon, floorMap);
                return floorMap;
        }


}
