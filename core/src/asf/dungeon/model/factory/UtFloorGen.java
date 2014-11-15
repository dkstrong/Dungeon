package asf.dungeon.model.factory;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.Tile;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.SimpleLogicProvider;
import com.badlogic.gdx.math.MathUtils;


/**
 * Created by Danny on 11/4/2014.
 */
public class UtFloorGen {

        public static void printFloorTile(Tile[][] tiles){

                for (int y = tiles[0].length - 1; y >= 0; y--) {
                        for (int x = 0; x < tiles.length; x++) {
                                Tile tile = tiles[x][y];
                                if(tile == null)
                                        System.out.print(" ");
                                else
                                        System.out.print(tile);
                        }
                        System.out.println();
                }




        }

        protected static void spawnCharacters(Dungeon dungeon, FloorMap floorMap){
                ModelId[] characters;
                if(floorMap.index == 0)
                        characters = new ModelId[]{};
                else{
                        characters = new ModelId[]{ModelId.Archer,ModelId.Berzerker,ModelId.Diablous,ModelId.FemaleMage,ModelId.Mage,ModelId.Priest}; // "cerberus"
                }
                //characters = new String[]{};

                for(ModelId modelId : characters){
                        Token characterToken = dungeon.newCharacterToken(floorMap,modelId.name(),modelId, new SimpleLogicProvider());
                        while(!characterToken.teleportToLocation(MathUtils.random.nextInt(floorMap.getWidth()),MathUtils.random.nextInt(floorMap.getHeight())) || floorMap.getTile(characterToken.getLocation()).isStairs() ){
                        }
                        characterToken.get(Experience.class).setStats(1, 7, 6, 7);
                }




        }

        protected static void spawnRandomCrates(Dungeon dungeon, FloorMap floorMap){
                ModelId[] crates = new ModelId[]{ModelId.CeramicPitcher,ModelId.CeramicPitcher,ModelId.CeramicPitcher,ModelId.CeramicPitcher,ModelId.CeramicPitcher};

                for(ModelId modelId : crates){

                        // new PotionItem(dungeon, PotionItem.Type.Health)
                        Token crateToken = dungeon.newCrateToken(floorMap,modelId.name(), modelId, new KeyItem(dungeon, KeyItem.Type.Silver));

                        while(!crateToken.teleportToLocation(MathUtils.random.nextInt(floorMap.getWidth()),MathUtils.random.nextInt(floorMap.getHeight())) || floorMap.getTile(crateToken.getLocation()).isStairs() ){
                        }

                }
        }

        /**
         *
         * @param dungeon
         * @param floorMap
         * @param maxTreasures  maximum amount of treasures to place
         * @param treasurePlacementLimt the quality of the location to place tresure (lower number makes more crates) [0-8]
         */
        protected static void spawnTreasuresNearWalls(Dungeon dungeon, FloorMap floorMap, int maxTreasures, int treasurePlacementLimt){
                ModelId modelId = ModelId.CeramicPitcher;
                int countSpawn = 0;
                for (int x = 0; x < floorMap.getWidth(); x++){
                        for (int y = 0; y < floorMap.getHeight(); y++){
                                if(!floorMap.getTile(x,y).isFloor())
                                        continue;

                                int numWalls = countWalls(floorMap.getTiles(), x, y);
                                if(treasurePlacementLimt <=numWalls){
                                        Token crateToken = dungeon.newCrateToken(floorMap,modelId.name(), modelId, new PotionItem(dungeon, PotionItem.Type.Health));
                                        crateToken.teleportToLocation(x,y);
                                        countSpawn++;
                                        if(countSpawn >= maxTreasures)
                                                return;

                                }

                        }
                }
        }


        protected static boolean isWall(Tile[][] tiles, int x, int y){
                if(x <0 || x>= tiles.length || y < 0 || y>=tiles[0].length)
                        return true;
                return tiles[x][y]==null || tiles[x][y].isWall();
        }

        protected static boolean isDoor(Tile[][] tiles, int x, int y){
                if(x <0 || x>= tiles.length || y < 0 || y>=tiles[0].length)
                        return false;
                return tiles[x][y]!=null && tiles[x][y].isDoor();
        }

        protected static boolean isFloor(Tile[][] tiles, int x, int y){
                if(x <0 || x>= tiles.length || y < 0 || y>=tiles[0].length)
                        return false;
                return tiles[x][y]!=null && tiles[x][y].isFloor();
        }

        /**
         * counts how many walls surround this tile (not counting this tile)
         * @param tiles
         * @param x
         * @param y
         * @return
         */
        protected static int countWalls(Tile[][] tiles, int x, int y){
                int numWalls = 0;
                if(isWall(tiles,x-1,y)) numWalls++;
                if(isWall(tiles,x+1,y)) numWalls++;
                if(isWall(tiles,x,y+1)) numWalls++;
                if(isWall(tiles,x,y-1)) numWalls++;
                if(isWall(tiles,x-1,y+1)) numWalls++;
                if(isWall(tiles,x+1,y+1)) numWalls++;
                if(isWall(tiles,x-1,y-1)) numWalls++;
                if(isWall(tiles,x+1,y-1)) numWalls++;
                return numWalls;
        }

        protected static int countDoors(Tile[][] tiles, int x, int y){
                int numDoors = 0;
                if(isDoor(tiles,x-1,y)) numDoors++;
                if(isDoor(tiles,x+1,y)) numDoors++;
                if(isDoor(tiles,x,y+1)) numDoors++;
                if(isDoor(tiles,x,y-1)) numDoors++;
                if(isDoor(tiles,x-1,y+1)) numDoors++;
                if(isDoor(tiles,x+1,y+1)) numDoors++;
                if(isDoor(tiles,x-1,y-1)) numDoors++;
                if(isDoor(tiles,x+1,y-1)) numDoors++;
                return numDoors;
        }



        protected static void ensureEdgesAreWalls(Tile[][] tiles){
                for (int x = 0; x < tiles.length; x++){
                        for (int y = 0; y < tiles[0].length; y++){
                                if (x == 0) tiles[x][y] = Tile.makeWall();
                                if (x == tiles.length-1) tiles[x][y] = Tile.makeWall();
                                if (y == 0) tiles[x][y] = Tile.makeWall();
                                if (y == tiles[0].length-1) tiles[x][y] = Tile.makeWall();

                        }
                }
        }

        protected static void floodFillSmallerAreas(Tile[][] tiles){
                // TODO: need to implement
        }

        /**
         * places "up" stairs in a random location, ensures that it will have atleast 1 buffer square from walls on all sides
         * @param tiles
         * @param floorIndex
         */
        protected static void placeUpStairs(Tile[][] tiles, int floorIndex){
                do{
                        int x = MathUtils.random(0, tiles.length-1);
                        int y = MathUtils.random(0, tiles[0].length-1);

                        if(!UtFloorGen.isFloor(tiles, x,y))
                                continue;

                        int numWalls = countWalls(tiles, x, y);
                        if(numWalls != 0)
                                continue;

                        tiles[x][y] = Tile.makeStairs(floorIndex, floorIndex - 1);
                        return;
                }while(true);
        }

        /**
         * places "down" stairs in a random location, ensures that it will have atleast 1 buffer square from walls on all sides
         * @param tiles
         * @param floorIndex
         */
        protected static void placeDownStairs(Tile[][] tiles, int floorIndex){
                do{
                        int x = MathUtils.random(0, tiles.length-1);
                        int y = MathUtils.random(0, tiles[0].length-1);
                        if(!UtFloorGen.isFloor(tiles, x,y))
                                continue;

                        int numWalls = countWalls(tiles, x, y);
                        if(numWalls != 0)
                                continue;

                        tiles[x][y] = Tile.makeStairs(floorIndex, floorIndex + 1);
                        return;
                }while(true);
        }

}
