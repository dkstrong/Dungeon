package asf.dungeon.model.floorgen.debug;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.floorgen.UtFloorGen;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.model.token.Stairs;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/4/2014.
 */
public class PreBuiltFloorGen implements FloorMapGenerator {

        public FloorMap generate(Dungeon dungeon, int floorIndex){
                FloorMap floorMap;
                if(floorIndex == 0){
                        floorMap = openRoom(dungeon,floorIndex);
                }else
                if(floorIndex ==1 ){
                        floorMap = smallRoom(dungeon,floorIndex);
                }else if(floorIndex == 2){
                        floorMap = mediumRoom(dungeon,floorIndex);
                }else{
                        floorMap = tinyRoom(dungeon,floorIndex);
                }
                UtFloorGen.spawnCharacters(dungeon, floorMap);
                UtFloorGen.spawnRandomCrates(dungeon, floorMap);
                return floorMap;
        }

        public static FloorMap mediumRoom(Dungeon dungeon, int floorIndex){

                String[] tileData = new String[]{
                        "---------------------------------",
                        "|................|..............|",
                        "|...|||...|......||.&..|........|",
                        "|.........|||.....|.............|",
                        "|..||.......|.....||-||||.......|",
                        "|.||||..|||||......||.....|.....|",
                        "|||........||.............|||...|",
                        "|...........|||.........|||.....|",
                        "|..|.........|....|.............|",
                        "|..||........|....||............|",
                        "|...........|||.........|||.....|",
                        "|.....^......|..................|",
                        "---------------------------------"

                };

                FloorMap floorMap = new FloorMap(floorIndex, convertTileData(floorIndex,tileData));
                spawnTokensFromTileData(dungeon, floorMap, tileData);
                return floorMap;
        }

        public static FloorMap openRoom(Dungeon dungeon, int floorIndex){

                String[] tileData = new String[]{
                        "-------------------",
                        "|.................|",
                        "|.^...............|",
                        "|.................|",
                        "|.....---+---.....|",
                        "|.....|.....|.....|",
                        "|.....|.....|.....|",
                        "|.....|.....|.....|",
                        "|.....|.....|.....|",
                        "|.....|-----|.....|",
                        "|.................|",
                        "|...............&.|",
                        "|.................|",
                        "|-----------------|"

                };

                FloorMap floorMap = new FloorMap(floorIndex, convertTileData(floorIndex,tileData));
                spawnTokensFromTileData(dungeon, floorMap, tileData);
                return floorMap;
        }
        public static FloorMap smallRoom(Dungeon dungeon, int floorIndex){

                String[] tileData = new String[]{
                        "----------------",
                        "|..............|",
                        "|...|||...|&...|",
                        "|.........|||..|",
                        "|..||.......|..|",
                        "|..||..|....|..|",
                        "|.....|||......|",
                        "|.^....|...||..|",
                        "|..............|",
                        "|--------------|"

                };

                FloorMap floorMap = new FloorMap(floorIndex, convertTileData(floorIndex,tileData));
                spawnTokensFromTileData(dungeon, floorMap, tileData);
                return floorMap;
        }

        public static FloorMap tinyRoom(Dungeon dungeon, int floorIndex){

                String[] tileData = new String[]{
                        "----------",
                        "|.......||",
                        "|.|.|.^|&|",
                        "|.|.|||..|",
                        "|.|...|..|",
                        "|.|......|",
                        "|.|..||..|",
                        "|........|",
                        "----------"

                };

                FloorMap floorMap = new FloorMap(floorIndex,convertTileData(floorIndex,tileData));
                spawnTokensFromTileData(dungeon, floorMap, tileData);

                return floorMap;
        }

        protected static Tile[][] convertTileData(int floorIndex, String[] tileData){
                Tile[][] tiles;
                tiles = new Tile[tileData[0].length()][tileData.length];
                for (int y = 0; y < tileData.length; y++) {
                        for (int x = 0; x < tileData[0].length(); x++) {
                                char charAt = tileData[tileData.length - y - 1].charAt(x);

                                if (charAt == '-' || charAt == '|') { // Wall
                                        tiles[x][y] = Tile.makeWall();
                                } else if(charAt == '+'){ // Door
                                        tiles[x][y] = Tile.makeDoor();
                                } else if(charAt == '/'){ // Locked Door
                                        tiles[x][y] = Tile.makeDoor(new KeyItem(KeyItem.Type.Silver));
                                }  else{ // Floor
                                        tiles[x][y] = Tile.makeFloor();
                                }
                        }
                }
                return tiles;
        }

        protected static void spawnTokensFromTileData(Dungeon dungeon, FloorMap floorMap, String[] tileData){
                for (int y = 0; y < tileData.length; y++) {
                        for (int x = 0; x < tileData[0].length(); x++) {
                                char charAt = tileData[tileData.length - y - 1].charAt(x);

                                if(charAt == '^'){ // Stairs Up
                                        Token stairsToken = new Token(dungeon, "Stairs", null);
                                        stairsToken.add(new Stairs(stairsToken, floorMap.index - 1));
                                        stairsToken.setDirection(Direction.East);
                                        dungeon.newToken(stairsToken, floorMap, x,y);
                                } else if(charAt == '&'){ // Stairs Down
                                        Token stairsToken = new Token(dungeon, "Stairs", null);
                                        stairsToken.add(new Stairs(stairsToken, floorMap.index+1));
                                        stairsToken.setDirection(Direction.East);
                                        dungeon.newToken(stairsToken, floorMap, x,y);
                                }
                        }
                }
        }
}
