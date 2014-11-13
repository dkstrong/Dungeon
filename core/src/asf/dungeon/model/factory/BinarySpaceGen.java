package asf.dungeon.model.factory;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Tile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.SnapshotArray;

/**
 *
 * floor is made by repeatidaly dividing the floor into 2 smaller parts
 *
 * http://www.roguebasin.com/index.php?title=Basic_BSP_Dungeon_generation
 *
 * Created by Danny on 11/4/2014.
 */
public class BinarySpaceGen implements FloorMapGenerator{


        private int minFloorWidth = 45;
        private int maxFloorWidth = 50;
        private int minFloorHeight = 30;
        private int maxFloorHeight = 45;


        private int numberOfSubdivisions = 2;
        private boolean makeDoors = false;


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


                RoomCell baseRoomCell = new RoomCell(0,0,floorWidth-1, floorHeight-1);

                for(int k=0;k< numberOfSubdivisions; k++){
                        subdivide(baseRoomCell);
                }

                fillRooms(tiles, baseRoomCell);
                fillTunnels(tiles, baseRoomCell);

                UtFloorGen.ensureEdgesAreWalls(tiles);
                UtFloorGen.placeUpStairs(tiles, floorIndex);
                UtFloorGen.placeDownStairs(tiles, floorIndex);
                return tiles;
        }

        private void subdivide(RoomCell baseCell){
                if(baseCell.isLeaf()){
                        baseCell.split(MathUtils.randomBoolean(), MathUtils.random(.25f, .75f));
                }else{
                        subdivide(baseCell.childCell1);
                        subdivide(baseCell.childCell2);
                }
        }


        private static class RoomCell {
                private RoomCell parentCell;
                private RoomCell childCell1;
                private RoomCell childCell2;
                private Room innerRoom;

                private int x1, y1, x2, y2;

                private RoomCell(int x1, int y1, int x2, int y2) {
                        this.x1 = x1;
                        this.y1 = y1;
                        this.x2 = x2;
                        this.y2 = y2;
                }

                private boolean isLeaf(){
                        return childCell1 == null && childCell2 == null;
                }

                private boolean hasLeafs(){
                        return childCell1.isLeaf() && childCell2.isLeaf();
                }

                private void split(boolean vertical, float splitRatio){
                        if(vertical){
                                int newX2 = Math.round(MathUtils.lerp(x1,x2,splitRatio));
                                childCell1= new RoomCell(x1, y1, newX2, y2);
                                childCell1.parentCell =this;

                                childCell2 = new RoomCell(newX2+1, y1, x2, y2);
                                childCell2.parentCell = this;

                        }else{
                                int newY2 = Math.round(MathUtils.lerp(y1,y2,splitRatio));
                                childCell1= new RoomCell(x1, y1, x2, newY2);
                                childCell1.parentCell =this;

                                childCell2 = new RoomCell(x1, newY2+1, x2, y2);
                                childCell2.parentCell = this;

                        }

                }

                private Room getInnerRoom(){
                        if(innerRoom != null)
                                return innerRoom;



                        int innerX1= MathUtils.random(x1,Math.round(MathUtils.lerp(x1,x2, .05f)));
                        int innerX2 = MathUtils.random(Math.round(MathUtils.lerp(x1,x2, .95f)), x2);

                        int innerY1= MathUtils.random(y1,Math.round(MathUtils.lerp(y1,y2, .05f)));
                        int innerY2 = MathUtils.random(Math.round(MathUtils.lerp(y1,y2, .95f)), y2);

                        innerRoom = new Room(innerX1, innerY1, innerX2, innerY2);
                        return innerRoom;
                }
        }


        private void fillRooms(Tile[][] tiles,RoomCell roomCell){
                if(roomCell.isLeaf()){
                        UtFloorGen.fillRoom(tiles, roomCell.getInnerRoom());
                }else{
                        fillRooms(tiles, roomCell.childCell1);
                        fillRooms(tiles, roomCell.childCell2);
                }

        }

        private void fillTunnels(Tile[][] tiles, RoomCell roomCell){
                if(roomCell.hasLeafs()){
                        Room r1= roomCell.childCell1.getInnerRoom();
                        Room r2 = roomCell.childCell2.getInnerRoom();
                        fillTunnel(tiles, r1, r2);
                }else if(!roomCell.isLeaf()){
                        fillTunnels(tiles,roomCell.childCell1);
                        fillTunnels(tiles,roomCell.childCell2);
                }
        }

        private void fillTunnel(Tile[][] tiles, Room room, Room prevRoom){
                int startX = room.getCenterX();
                int startY = room.getCenterY();
                int endX = prevRoom.getCenterX();
                int endY = prevRoom.getCenterY();

                if(startX == endX || startY == endY){
                        fillTunnel(tiles, startX, startY,endX, startY);
                }else{
                        // diagonal (convert in to a horizontal and vertical)
                        if(MathUtils.random.nextBoolean()){ // horizontal then vertical
                                fillTunnel(tiles, startX, startY,endX, startY);
                                fillTunnel(tiles, endX, startY,endX, endY);
                        }else{ // vertical then horizontal
                                fillTunnel(tiles, startX, startY, startX, endY);
                                fillTunnel(tiles, startX, endY,endX, endY);

                        }
                }

        }

        private void fillTunnel(Tile[][] tiles, int startX, int startY, int endX, int endY){
                if(startY==endY){
                        // horizontal
                        if(endX < startX){
                                int temp = startX;
                                startX = endX;
                                endX = temp;
                        }
                        int y = startY;
                        for(int x=startX; x<=endX; x++){
                                if(tiles[x][y+1] == null)
                                        tiles[x][y+1] = Tile.makeWall();
                                if(tiles[x][y-1] == null)
                                        tiles[x][y-1] = Tile.makeWall();

                                if(tiles[x][y] == null || !makeDoors) {
                                        tiles[x][y] = Tile.makeFloor(); // make a floor for the hallway
                                }else if(tiles[x][y].isWall()){
                                        int countWall = 0;
                                        if(tiles[x][y+1].isWall()) countWall++;
                                        if(tiles[x][y-1].isWall()) countWall++;
                                        if(countWall == 2)
                                                tiles[x][y] = Tile.makeDoor(); // if the hall goes through a wall, convert wall in to a door
                                        else
                                                tiles[x][y] = Tile.makeFloor(); // the hall is going through the broadside of a wall, so were really just extending the room
                                }

                        }
                }else if(startX == endX){
                        // vertical
                        if(endY < startY){
                                int temp = startY;
                                startY = endY;
                                endY = temp;
                        }
                        int x = startX;
                        for(int y=startY; y<=endY; y++){
                                if(tiles[x+1][y] == null)
                                        tiles[x+1][y] = Tile.makeWall();
                                if(tiles[x-1][y] == null)
                                        tiles[x-1][y] = Tile.makeWall();

                                if(tiles[x][y] == null || !makeDoors) {
                                        tiles[x][y] = Tile.makeFloor(); // make a floor for the hallway
                                }else if(tiles[x][y].isWall()){
                                        int countWall = 0;
                                        if(tiles[x+1][y].isWall()) countWall++;
                                        if(tiles[x-1][y].isWall()) countWall++;
                                        if(countWall == 2)
                                                tiles[x][y] = Tile.makeDoor(); // if the hall goes through a wall, convert wall in to a door
                                        else
                                                tiles[x][y] = Tile.makeFloor(); // the hall is going through the broadside of a wall, so were really just extending the room
                                }
                        }
                }else{
                        throw new IllegalArgumentException("must provide horizontal or vertical only coordinates");
                }

                if(tiles[startX+1][startY+1] == null)tiles[startX+1][startY+1] = Tile.makeWall();
                if(tiles[startX-1][startY+1] == null)tiles[startX-1][startY+1] = Tile.makeWall();
                if(tiles[startX+1][startY-1] == null)tiles[startX+1][startY-1] = Tile.makeWall();
                if(tiles[startX-1][startY-1] == null)tiles[startX-1][startY-1] = Tile.makeWall();
                if(tiles[endX+1][endY+1] == null)tiles[endX+1][endY+1] = Tile.makeWall();
                if(tiles[endX-1][endY+1] == null)tiles[endX-1][endY+1] = Tile.makeWall();
                if(tiles[endX+1][endY-1] == null)tiles[endX+1][endY-1] = Tile.makeWall();
                if(tiles[endX-1][endY-1] == null)tiles[endX-1][endY-1] = Tile.makeWall();
        }





}
