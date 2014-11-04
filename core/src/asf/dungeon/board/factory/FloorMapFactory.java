package asf.dungeon.board.factory;

import asf.dungeon.board.CharacterToken;
import asf.dungeon.board.CrateToken;
import asf.dungeon.board.Dungeon;
import asf.dungeon.board.FloorMap;
import asf.dungeon.board.FloorTile;
import com.badlogic.gdx.math.MathUtils;
import asf.dungeon.board.logic.LocalPlayerLogicProvider;
import asf.dungeon.board.logic.SimpleLogicProvider;


/**
 * Created by danny on 10/21/14.
 */
public class FloorMapFactory {


        public FloorMap generate(Dungeon dungeon, int floorIndex){

                if(floorIndex == 0){
                        FloorMap floorMap = smallRoom(floorIndex);
                        spawnTokens(dungeon, floorMap);
                        return floorMap;
                }else if(floorIndex == 1){
                        FloorMap floorMap = mediumRoom(floorIndex);
                        spawnTokens(dungeon, floorMap);
                        return floorMap;
                }else{
                        FloorMap floorMap = mazeTileMap(floorIndex);
                        spawnTokens(dungeon, floorMap);
                        return floorMap;
                }


        }

        private void spawnTokens(Dungeon dungeon, FloorMap floorMap){
                if(floorMap.index == 0){
                        CharacterToken knightToken = dungeon.newCharacterToken(floorMap,"knight",new LocalPlayerLogicProvider(0,"Player 1"));

                        while(!knightToken.teleportToLocation( MathUtils.random.nextInt(floorMap.getWidth()),MathUtils.random.nextInt(floorMap.getHeight()))){
                        }
                        knightToken.setAttackDamage(3);
                        knightToken.setDeathRemovalCountdown(Float.NaN);
                        knightToken.setMoveSpeed(50);
                }

                String[] characters;
                if(floorMap.index == 0)
                         characters = new String[]{"priest"};
                else{
                        characters = new String[]{"archer","berzerker","diablous","female_mage","mage","priest"}; // "cerberus"
                }
                //characters = new String[]{};

                for(String characterName : characters){
                        CharacterToken characterToken = dungeon.newCharacterToken(floorMap,characterName, new SimpleLogicProvider());
                        while(!characterToken.teleportToLocation(MathUtils.random.nextInt(floorMap.getWidth()),MathUtils.random.nextInt(floorMap.getHeight()))){
                        }
                }

                // TODO: make sure crates dont spawn on stairs
                String[] crates = new String[]{"CeramicPitcher","CeramicPitcher","CeramicPitcher"};

                for(String crateName : crates){
                        CrateToken crateToken = dungeon.newCrateToken(floorMap,crateName);
                        while(!crateToken.teleportToLocation(MathUtils.random.nextInt(floorMap.getWidth()),MathUtils.random.nextInt(floorMap.getHeight()))){
                        }
                }

        }

        public static FloorMap mazeTileMap(int floorIndex){
                FloorTile[][] tiles = MazeGenerator.generateTiles(7, 5);
                // upper stairs is on bottom left
                outerloop:
                for(int x=0; x<tiles.length; x++){
                        for(int y=0; y<tiles[x].length; y++){
                                if(!tiles[x][y].isBlockMovement() && !tiles[x][y].isBlockVision()){
                                        tiles[x][y] = new FloorTile(true, floorIndex-1);
                                        break outerloop;
                                }
                        }
                }

                // lower stairs is on top right
                outerloop:
                for (int x = tiles.length - 1; x >= 0; x--) {
                        for (int y = tiles[x].length - 1; y >= 0; y--) {
                                if(!tiles[x][y].isBlockMovement() && !tiles[x][y].isBlockVision()){
                                        tiles[x][y] = new FloorTile(false, floorIndex+1);
                                        break outerloop;
                                }
                        }
                }

                FloorMap floorMap = new FloorMap(floorIndex,tiles);
                return floorMap;
        }

        public static FloorMap mediumRoom(int floorIndex){

                String[] tileData = new String[]{
                        "---------------------------------",
                        "|................|..............|",
                        "|...|||...|......||.2..|........|",
                        "|.........|||.....|.............|",
                        "|..||.......|.....||-||||.......|",
                        "|.||||..|||||......||.....|.....|",
                        "|||........||.............|||...|",
                        "|...........|||.........|||.....|",
                        "|..|.........|....|.............|",
                        "|..||........|....||............|",
                        "|...........|||.........|||.....|",
                        "|.....0......|..................|",
                        "---------------------------------"

                };

                FloorMap floorMap = new FloorMap(floorIndex, convertTileData(floorIndex,tileData));

                return floorMap;
        }

        public static FloorMap smallRoom(int floorIndex){

                String[] tileData = new String[]{
                        "----------------",
                        "|..............|",
                        "|...|||...|1...|",
                        "|.........|||..|",
                        "|..||.......|..|",
                        "|..||..|....|..|",
                        "|.....|||......|",
                        "|......|...||..|",
                        "|..............|",
                        "|--------------|"

                };

                FloorMap floorMap = new FloorMap(floorIndex, convertTileData(floorIndex,tileData));

                return floorMap;
        }

        public static FloorMap tinyRoom(int floorIndex){

                String[] tileData = new String[]{
                        "----------",
                        "|.......||",
                        "|.|.|..|.|",
                        "|.|.|||..|",
                        "|.|...|..|",
                        "|.|......|",
                        "|.|..||..|",
                        "|........|",
                        "----------"

                };

                FloorMap floorMap = new FloorMap(floorIndex,convertTileData(floorIndex,tileData));

                return floorMap;
        }

        private static FloorTile[][] convertTileData(int floorIndex, String[] tileData){
                FloorTile[][] tiles;
                tiles = new FloorTile[tileData[0].length()][tileData.length];
                for (int y = 0; y < tileData.length; y++) {
                        for (int x = 0; x < tileData[0].length(); x++) {
                                char charAt = tileData[tileData.length - y - 1].charAt(x);

                                if (charAt == '-' || charAt == '|') { // Wall
                                        tiles[x][y] = new FloorTile(true, true);
                                } else if(charAt == '+'){ // Door
                                        tiles[x][y] = new FloorTile(false, true);
                                } else{ // Floor or Stair
                                        try{
                                                int stairsTo = Integer.parseInt(charAt+"");
                                                tiles[x][y] = new FloorTile(stairsTo<floorIndex, stairsTo);
                                        }catch(NumberFormatException ex){
                                                tiles[x][y] = new FloorTile(false, false);
                                        }

                                }
                        }
                }
                return tiles;

        }
}
