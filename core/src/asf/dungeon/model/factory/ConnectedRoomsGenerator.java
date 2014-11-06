package asf.dungeon.model.factory;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Tile;
import com.badlogic.gdx.math.MathUtils;
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
        private int maxRooms = 8;
        private boolean makeDoors = false;
        private int floorIndex;

        @Override
        public FloorMap generate(Dungeon dungeon, int floorIndex) {


                Tile[][] tiles = generateTiles(floorIndex);
                UtFloorGen.printFloorTile(tiles);

                FloorMap floorMap = new FloorMap(floorIndex, tiles);

                UtFloorGen.spawnTokens(dungeon, floorMap);
                return floorMap;
        }

        public Tile[][] generateTiles(int floorIndex){
                int floorWidth = MathUtils.random(minFloorWidth, maxFloorWidth);
                int floorHeight = MathUtils.random(minFloorHeight, maxFloorHeight);

                Tile[][] tiles = new Tile[floorWidth][floorHeight];
                int numRooms = Math.round(floorWidth /maxRoomSize * floorHeight / maxRoomSize * .5f);
                if(numRooms > maxRooms)
                        numRooms = maxRooms;

                numRooms -= MathUtils.random.nextInt(Math.round(numRooms*.25f));


                Array<Room> rooms = new Array<Room>(true, numRooms, Room.class);

                // make and fill rooms
                while(rooms.size < numRooms){
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

                // fill tunnels and doors
                // TODO: there is a bug where its the second to last or the second room or something
                // that doesnt get a connected hallway.
                for (int i = 1; i < rooms.size; i++) {
                        Room prevRoom = rooms.get(i-1);
                        Room room = rooms.get(i);
                        fillTunnel(tiles, room, prevRoom);
                }

                // fill stairways

                Room room = rooms.get(0);
                boolean done = false;
                do{
                        int x = MathUtils.random(room.x1+2, room.x2-2);
                        int y = MathUtils.random(room.y1+2, room.y2-2);
                        if(tiles[x][y].isFloor()){
                                tiles[x][y] = Tile.makeStairs(floorIndex, floorIndex + 1);
                                done = true;
                        }
                }while(!done);

                if(floorIndex >0){
                        room = rooms.get(rooms.size-1);
                        done = false;
                        do{
                                int x = MathUtils.random(room.x1+2, room.x2-2);
                                int y = MathUtils.random(room.y1+2, room.y2-2);
                                if(tiles[x][y].isFloor()){
                                        tiles[x][y] = Tile.makeStairs(floorIndex, floorIndex - 1);
                                        done = true;
                                }
                        }while(!done);
                }




                return tiles;
        }

        private static boolean isValidLocation(Tile[][] tiles, Room testRoom, Array<Room> rooms){
                if(testRoom.x2 >= tiles.length || testRoom.y2 >= tiles[0].length)
                        return false;

                for (Room room : rooms) {
                        if(testRoom.intersects(room)){
                                return false;
                        }
                }


                return true;
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

        private void fillRoom(Tile[][] tiles, Room room){
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
