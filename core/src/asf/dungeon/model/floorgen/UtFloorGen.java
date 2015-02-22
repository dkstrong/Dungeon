package asf.dungeon.model.floorgen;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FloorType;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.token.Decor;
import asf.dungeon.model.token.Stairs;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.TokenFactory;
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
                        characters = new ModelId[]{ModelId.Rat, ModelId.Rat, ModelId.Rat};
                else {
                        characters = new ModelId[]{ModelId.Rat, ModelId.RockMonster, ModelId.Goblin, ModelId.Skeleton, ModelId.Mage, ModelId.Priest}; // "cerberus"
                }
                //characters = new String[]{};
                int x, y;
                for (ModelId modelId : characters) {
                        Token t = TokenFactory.characterToken(dungeon, modelId, new FsmLogic(1, null, Monster.Sleep), floorMap);
                        for(int tries =0; tries < 20; tries++){
                                x = dungeon.rand.random.nextInt(floorMap.getWidth());
                                y = dungeon.rand.random.nextInt(floorMap.getHeight());
                                if (!t.isGoodSpawnLocation(floorMap, x, y, t.direction)) continue;
                                dungeon.addToken(t, floorMap, x, y);
                                break;
                        }
                }


        }

        public static void spawnDecor(Dungeon dungeon, FloorMap floorMap) {
                if(floorMap .floorType == FloorType.Grassy){
                      // rocks and stuff
                }else if(floorMap.floorType == FloorType.Church ){
                        spawnDecorInside(dungeon, floorMap, 0.75f);
                }else if(floorMap.floorType == FloorType.Dungeon){
                        spawnDecorInside(dungeon, floorMap, 0.5f);
                }
        }

        private static void spawnDecorInside(Dungeon dungeon, FloorMap floorMap, float amountOfStuff){
                final int maxBenches = Math.round(7 * amountOfStuff);
                final int maxTables = Math.round(4 * amountOfStuff);

                int x,y;
                Direction dir;
                for (int i = 0; i < maxBenches; i++) {
                        Token token = new Token(dungeon, "Decor",  ModelId.Bench);
                        token.add(new Decor(token));

                        for(int tries=0; tries < 20; tries++){
                                do{
                                        x = dungeon.rand.random.nextInt(floorMap.getWidth());
                                        y = dungeon.rand.random.nextInt(floorMap.getHeight());
                                }while(!UtFloorGen.isFloor(floorMap.tiles,x, y) || countWalls(floorMap.tiles, x, y) != 3);

                                dir = dungeon.rand.direction();
                                if(token.isGoodSpawnLocation(floorMap, x, y, dir)){
                                        dungeon.addToken(token, floorMap, x, y, dir);
                                        break;
                                }
                        }
                }

                SpawnGroup sg = new SpawnGroup();
                for(int i=0; i < maxTables; i++){
                        Token token = new Token(dungeon, "Decor", ModelId.Chair);
                        token.add(new Decor(token));
                        token.location.set(-1,0);
                        token.direction = Direction.East;

                        Token token1 = new Token(dungeon, "Decor", ModelId.Table1);
                        token1.add(new Decor(token));
                        token1.location.set(0,0);

                        Token token2 = new Token(dungeon, "Decor", ModelId.Chair);
                        token2.add(new Decor(token));
                        token2.location.set(1, 0);
                        token2.direction = Direction.West;
                        sg.setTokens(token, token1, token2);

                        for(int tries=0; tries < 20; tries++){
                                do{
                                        x = dungeon.rand.random.nextInt(floorMap.getWidth());
                                        y = dungeon.rand.random.nextInt(floorMap.getHeight());
                                }while(!UtFloorGen.isFloor(floorMap.tiles,x, y) || countWalls(floorMap.tiles, x, y) != 3);

                                dir = dungeon.rand.direction();
                                if(sg.spawnIfPossible(dungeon, floorMap,x,y, dir)){
                                        break;
                                }
                        }
                }
        }

        /**
         *
         * @param dungeon the dungeon reference
         * @param floorMap the floormap to spawn the crates on
         * @param maxCrates the maximum number of crates to try and spawn (may spawn less, but never more)
         * @param wallLimit [0-5] the number of walls the crate prefers to be near (may sometimes spawn with less nearby walls, but never more)
         */
        public static void spawnRandomCrates(Dungeon dungeon, FloorMap floorMap, int maxCrates, int wallLimit) {
                int x, y;
                float nearWall;
                final float wallLimitFloat = (float)wallLimit;
                for (int i = 0; i < maxCrates; i++) {
                        Token token = TokenFactory.crate(dungeon, dungeon.rand.crateModel(floorMap), null);
                        for(int tries =0; tries < 20; tries++){
                                x = dungeon.rand.random.nextInt(floorMap.getWidth());
                                y = dungeon.rand.random.nextInt(floorMap.getHeight());
                                nearWall = countWalls(floorMap.tiles,x,y)/wallLimitFloat;
                                if(nearWall > 1 || !dungeon.rand.bool(nearWall) ||  !token.isGoodSpawnLocation(floorMap,x,y,token.direction)  ) continue;
                                dungeon.addToken(token, floorMap, x, y);
                                break;
                        }
                }
        }

        public static boolean isWalkable(Tile[][] tiles, int x, int y) {
                if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length)
                        return false;
                return tiles[x][y] != null && (tiles[x][y].isFloor() || tiles[x][y].isDoor() || tiles[x][y].isPit());
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
         * counts how many floor, door, or pit tiles surround this tile (not counting this tile)
         * @param tiles
         * @param x
         * @param y
         * @return
         */
        public static int countFloors(Tile[][] tiles, int x, int y){
                int countWalkable = 0;
                if (isWalkable(tiles, x - 1, y)) countWalkable++;
                if (isWalkable(tiles, x + 1, y)) countWalkable++;
                if (isWalkable(tiles, x, y + 1)) countWalkable++;
                if (isWalkable(tiles, x, y - 1)) countWalkable++;
                if (isWalkable(tiles, x - 1, y + 1)) countWalkable++;
                if (isWalkable(tiles, x + 1, y + 1)) countWalkable++;
                if (isWalkable(tiles, x - 1, y - 1)) countWalkable++;
                if (isWalkable(tiles, x + 1, y - 1)) countWalkable++;
                return countWalkable;
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

        public static void ensureFloorsAreSurroundedWithWalls(Tile[][] tiles){
                for (int x = 0; x < tiles.length; x++) {
                        for (int y = 0; y < tiles[0].length; y++) {
                                if(tiles[x][y] != null && tiles[x][y].isFloor()){
                                        if (x == 0) tiles[x][y] = Tile.makeWall();
                                        if (x == tiles.length - 1) tiles[x][y] = Tile.makeWall();
                                        if (y == 0) tiles[x][y] = Tile.makeWall();
                                        if (y == tiles[0].length - 1) tiles[x][y] = Tile.makeWall();

                                        if(x > 0 && tiles[x-1][y] == null) tiles[x-1][y] = Tile.makeWall();
                                        if(x<tiles.length-2 && tiles[x+1][y] == null) tiles[x+1][y] = Tile.makeWall();
                                        if(y > 0 && tiles[x][y-1] == null) tiles[x][y-1] = Tile.makeWall();
                                        if(y<tiles[0].length-2 && tiles[x][y+1] == null) tiles[x][y+1] = Tile.makeWall();

                                }else if(tiles[x][y] == null && countFloors(tiles, x, y) > 0){
                                        if (x == 0) tiles[x][y] = Tile.makeWall();
                                        if (x == tiles.length - 1) tiles[x][y] = Tile.makeWall();
                                        if (y == 0) tiles[x][y] = Tile.makeWall();
                                        if (y == tiles[0].length - 1) tiles[x][y] = Tile.makeWall();
                                }
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
                        stairsToken.direction = Direction.East;
                        dungeon.addToken(stairsToken, floorMap, x, y);
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
                        stairsToken.direction = Direction.East;
                        dungeon.addToken(stairsToken, floorMap, x, y);
                        return;
                } while (true);
        }


}
