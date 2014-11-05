package asf.dungeon.board.factory;

import asf.dungeon.board.Dungeon;
import asf.dungeon.board.FloorMap;
import asf.dungeon.board.FloorTile;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 *
 * makes a floor that is similiar to the classic "Rouge" maps where
 * there are random rooms connected by hallways
 *
 * Created by Danny on 11/4/2014.
 */
public class ConnectedRoomsGenerator implements FloorMapGenerator{

        private int minRoomSize = 6;
        private int maxRoomSize = 10;
        private int minFloorWidth = 40;
        private int maxFloorWidth = 50;
        private int minFloorHeight = 30;
        private int maxFloorHeight = 50;

        @Override
        public FloorMap generate(Dungeon dungeon, int floorIndex) {

                FloorTile[][] floorTiles = generateTiles();
                UtFloorGen.printFloorTile(floorTiles);

                FloorMap floorMap = new FloorMap(floorIndex, floorTiles);

                UtFloorGen.spawnTokens(dungeon, floorMap);
                return floorMap;
        }

        public FloorTile[][] generateTiles(){
                int floorWidth = MathUtils.random(minFloorWidth, maxFloorWidth);
                int floorHeight = MathUtils.random(minFloorHeight, maxFloorHeight);

                FloorTile[][] tiles = new FloorTile[floorWidth][floorHeight];
                int numRooms = numRooms = Math.round(floorWidth /maxRoomSize * floorHeight / maxRoomSize * .5f);

                Array<Room> rooms = new Array<Room>(true, numRooms, Room.class);

                while(rooms.size <= numRooms){
                        Room newRoom = new Room(0,0,maxRoomSize,maxRoomSize);
                        do{
                                int roomWidth = MathUtils.random(minRoomSize, maxRoomSize);
                                int roomHeight = MathUtils.random(minRoomSize,maxRoomSize);
                                int x = MathUtils.random.nextInt(tiles.length);
                                int y = MathUtils.random.nextInt(tiles[0].length);

                                newRoom.set(x, y, x+roomWidth, y+roomHeight);

                        }while(!isValidLocation(tiles, newRoom, rooms));
                        rooms.add(newRoom);
                        fillRoom(tiles, newRoom);
                }

                for (int i = 1; i < rooms.size; i++) {
                        Room prevRoom = rooms.get(i-1);
                        Room room = rooms.get(i);
                        fillTunnel(tiles, room, prevRoom);
                }

                return tiles;
        }

        private static boolean isValidLocation(FloorTile[][] tiles, Room testRoom, Array<Room> rooms){
                if(testRoom.x2 >= tiles.length || testRoom.y2 >= tiles[0].length)
                        return false;

                for (Room room : rooms) {
                        if(testRoom.intersects(room)){
                                return false;
                        }
                }


                return true;
        }


        private void fillTunnel(FloorTile[][] tiles, Room room, Room prevRoom){
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

        private void fillTunnel(FloorTile[][] tiles, int startX, int startY, int endX, int endY){
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
                                        tiles[x][y+1] = FloorTile.makeWall();
                                if(tiles[x][y-1] == null)
                                        tiles[x][y-1] = FloorTile.makeWall();

                                if(tiles[x][y] == null) {
                                        tiles[x][y] = FloorTile.makeFloor(); // make a floor for the hallway
                                }else if(tiles[x][y].isWall()){
                                        int countWall = 0;
                                        if(tiles[x][y+1].isWall()) countWall++;
                                        if(tiles[x][y-1].isWall()) countWall++;
                                        if(countWall == 2)
                                                tiles[x][y] = FloorTile.makeDoor(); // if the hall goes through a wall, convert wall in to a door
                                        else
                                                tiles[x][y] = FloorTile.makeFloor(); // the hall is going through the broadside of a wall, so were really just extending the room
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
                                        tiles[x+1][y] = FloorTile.makeWall();
                                if(tiles[x-1][y] == null)
                                        tiles[x-1][y] = FloorTile.makeWall();

                                if(tiles[x][y] == null) {
                                        tiles[x][y] = FloorTile.makeFloor(); // make a floor for the hallway
                                }else if(tiles[x][y].isWall()){
                                        int countWall = 0;
                                        if(tiles[x+1][y].isWall()) countWall++;
                                        if(tiles[x+1][y].isWall()) countWall++;
                                        if(countWall == 2)
                                                tiles[x][y] = FloorTile.makeDoor(); // if the hall goes through a wall, convert wall in to a door
                                        else
                                                tiles[x][y] = FloorTile.makeFloor(); // the hall is going through the broadside of a wall, so were really just extending the room
                                }
                        }
                }else{
                        throw new IllegalArgumentException("must provide horizontal or vertical only coordinates");
                }

                if(tiles[startX+1][startY+1] == null)tiles[startX+1][startY+1] = FloorTile.makeWall();
                if(tiles[startX-1][startY+1] == null)tiles[startX-1][startY+1] = FloorTile.makeWall();
                if(tiles[startX+1][startY-1] == null)tiles[startX+1][startY-1] = FloorTile.makeWall();
                if(tiles[startX-1][startY-1] == null)tiles[startX-1][startY-1] = FloorTile.makeWall();
                if(tiles[endX+1][endY+1] == null)tiles[endX+1][endY+1] = FloorTile.makeWall();
                if(tiles[endX-1][endY+1] == null)tiles[endX-1][endY+1] = FloorTile.makeWall();
                if(tiles[endX+1][endY-1] == null)tiles[endX+1][endY-1] = FloorTile.makeWall();
                if(tiles[endX-1][endY-1] == null)tiles[endX-1][endY-1] = FloorTile.makeWall();
        }

        private void fillRoom(FloorTile[][] tiles, Room room){
                for(int x=room.x1; x<= room.x2; x++){
                        for(int y=room.y1; y<=room.y2; y++){
                                if(x == room.x1 || x== room.x2 || y==room.y1 || y==room.y2){
                                        tiles[x][y] = FloorTile.makeWall();
                                }else{
                                        tiles[x][y] = FloorTile.makeFloor();
                                }
                        }
                }

        }

}
