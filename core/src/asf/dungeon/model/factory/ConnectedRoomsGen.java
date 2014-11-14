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
public class ConnectedRoomsGen implements FloorMapGenerator{

        private int minRoomSize = 6;
        private int maxRoomSize = 10;
        private int minFloorWidth = 40;
        private int maxFloorWidth = 50;
        private int minFloorHeight = 30;
        private int maxFloorHeight = 50;
        private int maxRooms = 8;
        private boolean makeDoors = false;
        //private int floorIndex;

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
                        Room.fillRoom(tiles, newRoom);
                }

                // fill tunnels and doors
                // TODO: there is a bug where its the second to last or the second room or something
                // that doesnt get a connected hallway.
                for (int i = 1; i < rooms.size; i++) {
                        Room prevRoom = rooms.get(i-1);
                        Room room = rooms.get(i);
                        Room.fillTunnel(tiles, room, prevRoom, true, makeDoors);
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






}
