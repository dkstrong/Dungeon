package asf.dungeon.model.floorgen;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FxId;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.item.BookItem;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.ScrollItem;
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.Stairs;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.fsm.FsmLogic;
import asf.dungeon.model.token.logic.fsm.Monster;
import com.badlogic.gdx.utils.ObjectIntMap;

import java.util.List;

import static asf.dungeon.utility.UtDebugPrint.out;


/**
 * Created by Danny on 11/4/2014.
 */
public class UtFloorGen {

        public static void printFloorTile(Tile[][] tiles, Pair markLoc) {

                for (int y = tiles[0].length - 1; y >= 0; y--) {
                        for (int x = 0; x < tiles.length; x++) {
                                if(x == markLoc.x && y == markLoc.y){
                                        System.out.print("*");
                                }else{
                                        Tile tile = tiles[x][y];
                                        if (tile == null)
                                                System.out.print(" ");
                                        else
                                                System.out.print(tile);
                                }

                        }
                        System.out.println();
                }
        }

        public static void printFloorTile(Tile[][] tiles) {

                for (int y = tiles[0].length - 1; y >= 0; y--) {
                        for (int x = 0; x < tiles.length; x++) {
                                Tile tile = tiles[x][y];
                                if (tile == null)
                                        System.out.print(" ");
                                else
                                        System.out.print(tile);
                        }
                        System.out.println();
                }
        }

        public static List<String> outFloorTiles(Tile[][] tiles) {
                List<String> out = out();
                for (int y = tiles[0].length - 1; y >= 0; y--) {
                        String s = "";
                        for (int x = 0; x < tiles.length; x++) {
                                Tile tile = tiles[x][y];
                                if (tile == null)
                                        s += " ";
                                else
                                        s += String.valueOf(tile);
                        }
                        out.add(s);
                }
                return out;
        }

        public static void spawnCharacters(Dungeon dungeon, FloorMap floorMap) {
                ModelId[] characters;
                if (floorMap.index == 0)
                        characters = new ModelId[]{ModelId.Goblin, ModelId.Goblin, ModelId.Goblin};
                else {
                        characters = new ModelId[]{ModelId.Archer, ModelId.Berzerker, ModelId.Diablous, ModelId.FemaleMage, ModelId.Mage, ModelId.Priest}; // "cerberus"
                }
                //characters = new String[]{};
                int x, y;
                for (ModelId modelId : characters) {
                        do {
                                x = dungeon.rand.random.nextInt(floorMap.getWidth());
                                y = dungeon.rand.random.nextInt(floorMap.getHeight());
                        } while (floorMap.getTile(x, y) == null || !floorMap.getTile(x, y).isFloor() || floorMap.hasTokensAt(x, y));

                        Token characterToken = dungeon.newCharacterToken(floorMap, modelId.name(), modelId,
                                new FsmLogic(1, null, Monster.Sleep),
                                new Experience(
                                        1, // level
                                        8,  // vitality
                                        4, //str
                                        6, // agi
                                        1, // int
                                        1), // luck
                                x, y);

                        if (modelId == ModelId.Archer) {
                                WeaponItem weapon = new WeaponItem(ModelId.Sword, "Bow", 1, FxId.Arrow);
                                characterToken.getInventory().add(weapon);
                                characterToken.getInventory().equals(weapon);
                        }

                }


        }

        public static void spawnRandomCrates(Dungeon dungeon, FloorMap floorMap) {
                int x, y;
                Item item;
                for (int i = 0; i < 5; i++) {


                        do {
                                x = dungeon.rand.random.nextInt(floorMap.getWidth());
                                y = dungeon.rand.random.nextInt(floorMap.getHeight());
                        } while (floorMap.getTile(x, y) == null || !floorMap.getTile(x, y).isFloor() || floorMap.hasTokensAt(x, y));

                        int randInt = dungeon.rand.random.nextInt(3);
                        if (randInt == 0 || true) {
                                //dungeon.rand.potionType()
                                item = new PotionItem(dungeon, PotionItem.Type.Health, 1);
                        } else if (randInt == 1) {
                                item = new ScrollItem(dungeon, ScrollItem.Type.Lightning, 1);
                        } else if (randInt == 2) {
                                item = new BookItem(dungeon, BookItem.Type.AggravateMonsters);
                        } else {
                                item = new BookItem(dungeon, BookItem.Type.Experience);
                        }

                        dungeon.newCrateToken(
                                floorMap,
                                ModelId.Crate.name(),
                                ModelId.Crate,
                                item,
                                x, y);
                }

        }

        /**
         * @param dungeon
         * @param floorMap
         * @param maxTreasures          maximum amount of treasures to place
         * @param treasurePlacementLimt the quality of the location to place tresure (lower number makes more crates) [0-8]
         */
        public static void spawnTreasuresNearWalls(Dungeon dungeon, FloorMap floorMap, int maxTreasures, int treasurePlacementLimt) {
                ModelId modelId = ModelId.CeramicPitcher;
                int countSpawn = 0;
                for (int x = 0; x < floorMap.getWidth(); x++) {
                        for (int y = 0; y < floorMap.getHeight(); y++) {
                                if (floorMap.getTile(x, y)==null || !floorMap.getTile(x, y).isFloor() || floorMap.hasTokensAt(x, y))
                                        continue;

                                int numWalls = countWalls(floorMap.getTiles(), x, y);
                                if (treasurePlacementLimt <= numWalls) {
                                        dungeon.newCrateToken(floorMap, modelId.name(), modelId, new PotionItem(dungeon, PotionItem.Type.Health, 1), x, y);

                                        countSpawn++;
                                        if (countSpawn >= maxTreasures)
                                                return;

                                }

                        }
                }
        }




        public static boolean isWall(Tile[][] tiles, int x, int y) {
                if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length)
                        return true;
                return tiles[x][y] == null || tiles[x][y].isWall();
        }

        public static boolean isDoor(Tile[][] tiles, int x, int y) {
                if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length)
                        return false;
                return tiles[x][y] != null && tiles[x][y].isDoor();
        }

        public static boolean isFloor(Tile[][] tiles, int x, int y) {
                if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length)
                        return false;
                return tiles[x][y] != null && tiles[x][y].isFloor();
        }

        /**
         * counts how many walls surround this tile (not counting this tile)
         *
         * @param tiles
         * @param x
         * @param y
         * @return
         */
        public static int countWalls(Tile[][] tiles, int x, int y) {
                int numWalls = 0;
                if (isWall(tiles, x - 1, y)) numWalls++;
                if (isWall(tiles, x + 1, y)) numWalls++;
                if (isWall(tiles, x, y + 1)) numWalls++;
                if (isWall(tiles, x, y - 1)) numWalls++;
                if (isWall(tiles, x - 1, y + 1)) numWalls++;
                if (isWall(tiles, x + 1, y + 1)) numWalls++;
                if (isWall(tiles, x - 1, y - 1)) numWalls++;
                if (isWall(tiles, x + 1, y - 1)) numWalls++;
                return numWalls;
        }

        public static int countDoors(Tile[][] tiles, int x, int y) {
                int numDoors = 0;
                if (isDoor(tiles, x - 1, y)) numDoors++;
                if (isDoor(tiles, x + 1, y)) numDoors++;
                if (isDoor(tiles, x, y + 1)) numDoors++;
                if (isDoor(tiles, x, y - 1)) numDoors++;
                if (isDoor(tiles, x - 1, y + 1)) numDoors++;
                if (isDoor(tiles, x + 1, y + 1)) numDoors++;
                if (isDoor(tiles, x - 1, y - 1)) numDoors++;
                if (isDoor(tiles, x + 1, y - 1)) numDoors++;
                return numDoors;
        }


        public static void ensureEdgesAreWalls(Tile[][] tiles) {
                for (int x = 0; x < tiles.length; x++) {
                        for (int y = 0; y < tiles[0].length; y++) {
                                if (x == 0) tiles[x][y] = Tile.makeWall();
                                if (x == tiles.length - 1) tiles[x][y] = Tile.makeWall();
                                if (y == 0) tiles[x][y] = Tile.makeWall();
                                if (y == tiles[0].length - 1) tiles[x][y] = Tile.makeWall();

                        }
                }
        }

        public static void floodFillSmallerAreas(Tile[][] tiles) {

                // first clone the tiles array to do the initial flood filling with and find all floodable zones
                Tile [][] tilesClone = new Tile[tiles.length][];
                for(int x = 0; x < tiles.length; x++) tilesClone[x] = tiles[x].clone();
                ObjectIntMap<Pair> zones = new ObjectIntMap<Pair>(4);
                Pair largestZone = null;
                int largestZoneCount=0;
                for (int x = 0; x < tilesClone.length; x++) {
                        for (int y = 0; y < tilesClone[0].length; y++) {
                                int count = floodFill(tilesClone, x,y);
                                if(count > 0){
                                        Pair pair = new Pair(x,y);
                                        if(count > largestZoneCount){
                                                largestZone = pair;
                                                largestZoneCount = count;
                                        }
                                        zones.put(pair, count);
                                }

                        }
                }
                // do the actual flood fill, all zones are flood filled except for the largest zone
                for (ObjectIntMap.Entry<Pair> zone : zones) {
                        if(zone.key != largestZone){
                                floodFill(tiles, zone.key.x, zone.key.y);
                        }
                }
        }

        /**
         * floodfills the provided tiles, turning floor, door, and stair tiles in to null.
         * null and wall tiles are used as boundaries for the flooding
         * @param tiles
         * @param x
         * @param y
         * @return number of tiles that were converted to null
         */
        public static int floodFill(Tile[][] tiles, int x, int y) {
                if (x < 0 || x > tiles.length || y < 0 || y > tiles[0].length) return 0;
                if (tiles[x][y] == null || tiles[x][y].isWall()) return 0;
                tiles[x][y] = null;
                int count = 1;
                count += floodFill(tiles, x - 1, y);
                count += floodFill(tiles, x + 1, y);
                count += floodFill(tiles, x, y - 1);
                count += floodFill(tiles, x, y + 1);
                return count;
        }


        public static void placeUpStairs(Dungeon dungeon, FloorMap floorMap) {
                do {
                        int x = dungeon.rand.range(0, floorMap.getWidth()- 1);
                        int y = dungeon.rand.range(0, floorMap.getHeight() - 1);

                        if(floorMap.isLocationBlocked(x,y))
                                continue;

                        int numWalls = countWalls(floorMap.tiles, x, y);
                        if (numWalls != 0)
                                continue;

                        Token stairsToken = new Token(dungeon, "Stairs", null);
                        stairsToken.add(new Stairs(stairsToken, floorMap.index - 1));
                        stairsToken.setDirection(Direction.East);
                        dungeon.newToken(stairsToken, floorMap, x,y);
                        return;
                } while (true);
        }

        public static void placeDownStairs(Dungeon dungeon, FloorMap floorMap) {
                do {
                        int x = dungeon.rand.range(0, floorMap.getWidth()- 1);
                        int y = dungeon.rand.range(0, floorMap.getHeight() - 1);
                        if(floorMap.isLocationBlocked(x,y))
                                continue;

                        int numWalls = countWalls(floorMap.tiles, x, y);
                        if (numWalls != 0)
                                continue;

                        Token stairsToken = new Token(dungeon, "Stairs", null);
                        stairsToken.add(new Stairs(stairsToken, floorMap.index + 1));
                        stairsToken.setDirection(Direction.East);
                        dungeon.newToken(stairsToken, floorMap, x,y);
                        return;
                } while (true);
        }


}
