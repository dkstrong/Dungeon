package asf.dungeon.board.factory;

import asf.dungeon.board.Dungeon;
import asf.dungeon.board.FloorMap;
import asf.dungeon.board.FloorTile;

/**
 * Created by Danny on 11/4/2014.
 */
public class PreBuiltFloorGenerator implements FloorMapGenerator{

        public FloorMap generate(Dungeon dungeon, int floorIndex){
                FloorMap floorMap;
                if(floorIndex ==0 ){
                        floorMap = smallRoom(floorIndex);
                }else if(floorIndex == 1){
                        floorMap = mediumRoom(floorIndex);
                }else{
                        floorMap = tinyRoom(floorIndex);
                }
                UtFloorGen.spawnTokens(dungeon, floorMap);
                return floorMap;
        }

        public static FloorMap mediumRoom(int floorIndex){

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

                return floorMap;
        }

        public static FloorMap smallRoom(int floorIndex){

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

                return floorMap;
        }

        public static FloorMap tinyRoom(int floorIndex){

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
                                } else if(charAt == '^'){ // Stairs Up
                                        int stairsTo = floorIndex-1;
                                        if(stairsTo >=0)
                                                tiles[x][y] = new FloorTile(true, stairsTo);
                                        else
                                                tiles[x][y] = new FloorTile(false, false);
                                } else if(charAt == '&'){ // Stairs Down
                                        int stairsTo = floorIndex+1;
                                        if(stairsTo >=0)
                                                tiles[x][y] = new FloorTile(false, stairsTo);
                                        else
                                                tiles[x][y] = new FloorTile(false, false);
                                } else{ // Floor
                                        tiles[x][y] = new FloorTile(false, false);
                                }
                        }
                }
                return tiles;

        }
}
