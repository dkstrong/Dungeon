package asf.dungeon.model.factory;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.PotionItem;
import asf.dungeon.model.Tile;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.LocalPlayerLogicProvider;
import asf.dungeon.model.token.logic.SimpleLogicProvider;
import com.badlogic.gdx.math.MathUtils;


/**
 * Created by Danny on 11/4/2014.
 */
public class UtFloorGen {

        protected static void printFloorTile(Tile[][] tiles){

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
                if(floorMap.index == 0){
                        boolean rangedHero = true;//MathUtils.random.nextBoolean();

                        Token knightToken = dungeon.newCharacterToken(floorMap,"Player 1", rangedHero ? ModelId.Archer : ModelId.Knight, new LocalPlayerLogicProvider(0,"Player 1"));

                        while(!knightToken.teleportToLocation( MathUtils.random.nextInt(floorMap.getWidth()),MathUtils.random.nextInt(floorMap.getHeight()))){
                        }
                        knightToken.get(Experience.class).setStats(1, 10, 6, 10);

                        knightToken.getAttack().setAbleRangedAttack(rangedHero);
                        knightToken.getDamage().setDeathRemovalCountdown(Float.NaN);
                        knightToken.getInventory().addItem(new PotionItem(dungeon, PotionItem.Type.Health));
                        knightToken.getInventory().addItem(new PotionItem(dungeon, PotionItem.Type.Health));

                        //knightToken.setMoveSpeed(50);
                }

                ModelId[] characters;
                if(floorMap.index == 0)
                        characters = new ModelId[]{ModelId.Knight}; //destLoc
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
                        Token crateToken = dungeon.newCrateToken(floorMap,modelId.name(), modelId, new PotionItem(dungeon, PotionItem.Type.Health));
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
                if(floorIndex <= 0)
                        return;

                do{
                        int x = MathUtils.random(0, tiles.length-1);
                        int y = MathUtils.random(0, tiles[0].length-1);
                        if(!tiles[x][y].isFloor())
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
                        if(!tiles[x][y].isFloor())
                                continue;

                        int numWalls = countWalls(tiles, x, y);
                        if(numWalls != 0)
                                continue;

                        tiles[x][y] = Tile.makeStairs(floorIndex, floorIndex + 1);
                        return;
                }while(true);
        }

        public static void fillRoom(Tile[][] tiles, Room room){
                for(int x=room.x1; x<= room.x2; x++){
                        for(int y=room.y1; y<=room.y2; y++){
                                if(x == room.x1 || x== room.x2 || y==room.y1 || y==room.y2){
                                        tiles[x][y] = Tile.makeWall();
                                }else{
                                        tiles[x][y] = Tile.makeFloor();
                                }
                        }
                }

        }
}
