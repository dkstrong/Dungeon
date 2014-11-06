package asf.dungeon.model.factory;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Tile;

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

        private static Tile[][] convertTileData(int floorIndex, String[] tileData){
                Tile[][] tiles;
                tiles = new Tile[tileData[0].length()][tileData.length];
                for (int y = 0; y < tileData.length; y++) {
                        for (int x = 0; x < tileData[0].length(); x++) {
                                char charAt = tileData[tileData.length - y - 1].charAt(x);

                                if (charAt == '-' || charAt == '|') { // Wall
                                        tiles[x][y] = Tile.makeWall();
                                } else if(charAt == '+'){ // Door
                                        tiles[x][y] = Tile.makeDoor();
                                } else if(charAt == '^'){ // Stairs Up
                                        if(floorIndex >0)
                                                tiles[x][y] = Tile.makeStairs(floorIndex, floorIndex - 1);
                                } else if(charAt == '&'){ // Stairs Down
                                        tiles[x][y] = Tile.makeStairs(floorIndex, floorIndex + 1);
                                } else{ // Floor
                                        tiles[x][y] = Tile.makeFloor();
                                }
                        }
                }
                return tiles;

        }
}
