package asf.dungeon.model.floorgen.room;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.UtFloorGen;
import asf.dungeon.utility.UtMath;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Daniel Strong on 12/13/2014.
 */
public class UtRoomCarve {

        public static void fillAndCarve(Dungeon dungeon, int floorIndex, Tile[][] tiles, Array<Room> rooms){
                fillRooms(tiles, rooms);
                fillTunnels(dungeon, tiles, rooms);
                carveDoors(dungeon, floorIndex, tiles, rooms);
                carveStairs(dungeon, floorIndex, tiles, rooms);
        }


        public static void fillRooms(Tile[][] tiles, Array<Room> rooms) {
                for (int i = 0; i < rooms.size; i++) {
                        fillRoom(tiles, rooms.get(i));
                }

        }

        public static void fillRoom(Tile[][] tiles, Room room) {
                for (int x = room.x1; x <= room.x2; x++) {
                        for (int y = room.y1; y <= room.y2; y++) {
                                if (x == room.x1 || x == room.x2 || y == room.y1 || y == room.y2) {
                                        tiles[x][y] = Tile.makeWall();
                                } else {
                                        tiles[x][y] = Tile.makeFloor();
                                }
                        }
                }

        }

        public static void fillTunnels(Dungeon dungeon, Tile[][] tiles, Array<Room> rooms){
                for (int i = 1; i < rooms.size; i++) {
                        Room prevRoom = rooms.get(i - 1);
                        Room room = rooms.get(i);
                        fillTunnel(dungeon, tiles, room, prevRoom, true);
                }
        }

        /**
         * @param tiles
         * @param room
         * @param prevRoom
         * @param randomLDirection if true the L shape direction will be randoml chosen, if false then the L shape will be chosen to try and make the smallest hallway
         */
        public static void fillTunnel(Dungeon dungeon, Tile[][] tiles, Room room, Room prevRoom, boolean randomLDirection) {
                int startX = room.getCenterX();
                int startY = room.getCenterY();
                int endX = prevRoom.getCenterX();
                int endY = prevRoom.getCenterY();

                if (startX == endX || startY == endY) {
                        fillTunnel(tiles, startX, startY, endX, endY);
                } else {
                        // diagonal (convert in to a horizontal and vertical)
                        if (randomLDirection) {
                                randomLDirection = dungeon.rand.random.nextBoolean();
                        } else {
                                float xRange = UtMath.range(room.getCenterX(), prevRoom.getCenterX());
                                float yRange = UtMath.range(room.getCenterY(), prevRoom.getCenterY());
                                randomLDirection = xRange > yRange;
                        }

                        if (randomLDirection) { // horizontal then vertical
                                fillTunnel(tiles, startX, startY, endX, startY);
                                fillTunnel(tiles, endX, startY, endX, endY);
                        } else { // vertical then horizontal
                                fillTunnel(tiles, startX, startY, startX, endY);
                                fillTunnel(tiles, startX, endY, endX, endY);

                        }
                }

        }

        public static void fillTunnel(Tile[][] tiles, int startX, int startY, int endX, int endY) {
                if (startY == endY) {
                        // horizontal
                        if (endX < startX) {
                                int temp = startX;
                                startX = endX;
                                endX = temp;
                        }
                        int y = startY;
                        for (int x = startX; x <= endX; x++) {
                                if (tiles[x][y + 1] == null)
                                        tiles[x][y + 1] = Tile.makeWall();
                                if (tiles[x][y - 1] == null)
                                        tiles[x][y - 1] = Tile.makeWall();
                                tiles[x][y] = Tile.makeFloor(); // make a floor for the hallway

                        }
                } else if (startX == endX) {
                        // vertical
                        if (endY < startY) {
                                int temp = startY;
                                startY = endY;
                                endY = temp;
                        }
                        int x = startX;
                        for (int y = startY; y <= endY; y++) {
                                if (tiles[x + 1][y] == null)
                                        tiles[x + 1][y] = Tile.makeWall();
                                if (tiles[x - 1][y] == null)
                                        tiles[x - 1][y] = Tile.makeWall();
                                tiles[x][y] = Tile.makeFloor(); // make a floor for the hallway
                        }
                } else {
                        throw new IllegalArgumentException("must provide horizontal or vertical only coordinates");
                }

                if (tiles[startX + 1][startY + 1] == null) tiles[startX + 1][startY + 1] = Tile.makeWall();
                if (tiles[startX - 1][startY + 1] == null) tiles[startX - 1][startY + 1] = Tile.makeWall();
                if (tiles[startX + 1][startY - 1] == null) tiles[startX + 1][startY - 1] = Tile.makeWall();
                if (tiles[startX - 1][startY - 1] == null) tiles[startX - 1][startY - 1] = Tile.makeWall();
                if (tiles[endX + 1][endY + 1] == null) tiles[endX + 1][endY + 1] = Tile.makeWall();
                if (tiles[endX - 1][endY + 1] == null) tiles[endX - 1][endY + 1] = Tile.makeWall();
                if (tiles[endX + 1][endY - 1] == null) tiles[endX + 1][endY - 1] = Tile.makeWall();
                if (tiles[endX - 1][endY - 1] == null) tiles[endX - 1][endY - 1] = Tile.makeWall();
        }



        public static void carveDoors(Dungeon dungeon, int floorIndex, Tile[][] tiles, Array<Room> rooms) {
                for (int i = 0; i < rooms.size; i++) {
                        carveDoorsInRoom(dungeon, floorIndex, tiles, rooms, rooms.items[i]);
                }
        }

        public static void carveDoorsInRoom(Dungeon dungeon, int floorIndex, Tile[][] tiles,  Array<Room> rooms, Room room) {
                // should be called after fillRoom() and fillTunnel()
                // This is a lot of code for a seemingly simple thing. but it checks to ensure
                // that these scenarios do not happen
                // 1 - two doors placed next to eachother
                // 2 - prevent the scenario of a tunnel going through a wall "long ways" causing the entire wall becoming a string of doors
                // 3 - prevent a door to be placed at the ends of double wide halls

                // top and bottom of room
                for (int y = room.y1; y <= room.y2; y += room.getHeight()) {
                        for (int x = room.x1; x <= room.x2; x++) {
                                if (tiles[x][y] != null && tiles[x][y].isFloor()) {
                                        if (UtFloorGen.countDoors(tiles, x, y) == 0
                                                && UtFloorGen.isWall(tiles, x + 1, y)
                                                && UtFloorGen.isWall(tiles, x - 1, y)) {
                                                tiles[x][y] = Tile.makeDoor();
                                                placeDoorway(dungeon, rooms, x,y);
                                        }
                                        // this is still a "doorway" as in a hole in the room, just the wall
                                        // was modified by a hallway such that an actual door doesnt make sense
                                }
                        }
                }
                // left and right of room
                for (int x = room.x1; x <= room.x2; x += room.getWidth()) {
                        for (int y = room.y1; y <= room.y2; y++) {
                                if (tiles[x][y] != null && tiles[x][y].isFloor()) {
                                        if (UtFloorGen.countDoors(tiles, x, y) == 0
                                                && UtFloorGen.isWall(tiles, x, y - 1)
                                                && UtFloorGen.isWall(tiles, x, y + 1)) {
                                                tiles[x][y] = Tile.makeDoor();
                                                placeDoorway(dungeon, rooms, x,y);
                                        }
                                        // this is still a "doorway" as in a hole in the room, just the wall
                                        // was modified by a hallway such that an actual door doesnt make sense
                                }
                        }
                }
        }

        private static void placeDoorway(Dungeon dungeon, Array<Room> rooms, int x, int y){
                Doorway doorway = new Doorway(x,y);
                for (Room room : rooms) {
                        if(room.contains(x,y)){
                             room.doorways.add(doorway);
                        }
                }

        }

        public static boolean carveStairs(Dungeon dungeon, int floorIndex, Tile[][] tiles, Array<Room> rooms) {
                int i = 0;
                boolean valid = false;
                while (i < rooms.size && !valid) {
                        valid = carveStairsInRoom(dungeon, tiles, rooms.get(i++), floorIndex, floorIndex + 1);
                }
                if (!valid) return false;
                valid = false;
                int j = rooms.size - 1;
                while (j >= i && !valid) {
                        valid = carveStairsInRoom(dungeon, tiles, rooms.get(j--), floorIndex, floorIndex - 1);
                }
                return valid;
        }

        public static boolean carveStairsInRoom(Dungeon dungeon, Tile[][] tiles, Room room, int floorIndex, int floorIndexTo) {
                if (room.x1 + 2 >= room.x2 - 2 || room.y1 + 2 >= room.y2 - 2) {
                        return false; // room too small
                }

                int x,y;
                do{
                        x = dungeon.rand.range(room.x1 + 2, room.x2 - 2);
                        y = dungeon.rand.range(room.y1 + 2, room.y2 - 2);
                }while(tiles[x][y]== null || !tiles[x][y].isFloor());

                tiles[x][y] = Tile.makeStairs(floorIndex, floorIndexTo);
                room.containsStairsTo = floorIndexTo;

                return true;
        }

}
