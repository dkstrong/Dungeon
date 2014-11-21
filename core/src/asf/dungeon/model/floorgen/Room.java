package asf.dungeon.model.floorgen;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Sector;
import asf.dungeon.model.Tile;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.utility.UtMath;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/4/2014.
 */
public class Room extends Sector {

        private KeyItem.Type requiresKey = null;
        private KeyItem.Type containsKey = null;

        Room(int x1, int y1, int x2, int y2) {
                super(x1, y1, x2, y2);
        }


        static void fillRooms(Tile[][] tiles, Array<Room> rooms) {
                for (int i = 0; i < rooms.size; i++) {
                        Room.fillRoom(tiles, rooms.get(i));
                }

        }

        static void fillRoom(Tile[][] tiles, Room room) {
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

        static void fillTunnels(Dungeon dungeon, Tile[][] tiles, Array<Room> rooms){
                for (int i = 1; i < rooms.size; i++) {
                        Room prevRoom = rooms.get(i - 1);
                        Room room = rooms.get(i);
                        Room.fillTunnel(dungeon, tiles, room, prevRoom, true);
                }
        }

        /**
         * @param tiles
         * @param room
         * @param prevRoom
         * @param randomLDirection if true the L shape direction will be randoml chosen, if false then the L shape will be chosen to try and make the smallest hallway
         */
        static void fillTunnel(Dungeon dungeon, Tile[][] tiles, Room room, Room prevRoom, boolean randomLDirection) {
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

        static void fillTunnel(Tile[][] tiles, int startX, int startY, int endX, int endY) {
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

        static boolean carveDoorsKeysStairs(Dungeon dungeon, int floorIndex, Tile[][] tiles, Array<Room> rooms, boolean makeDoors, boolean includeLockedDoors) {
                if (makeDoors){
                        // will create 2 silver rooms, 2 gold rooms, 1 red room, then the rest silver.
                        // actual amount of locked rooms depends on how the floor was generated
                        int[] count = new int[3];
                        KeyItem.Type type = KeyItem.Type.Silver;
                        boolean forceType = false;
                        for (int i = 0; i < rooms.size; i++) {
                                Room room = rooms.get(i);
                                float chance = includeLockedDoors ? .45f : 0;
                                Room.fillRoomWithDoors(dungeon, tiles, room, chance,type);
                                if(room.requiresKey != null){
                                        ++count[room.requiresKey.ordinal()];
                                        if(type == KeyItem.Type.Red && room.requiresKey == KeyItem.Type.Red){
                                                // there can only be one red room
                                                type = KeyItem.Type.Silver;
                                                forceType = true;
                                        }else if(forceType == false && count[room.requiresKey.ordinal()] >=2){
                                                if(type == KeyItem.Type.Silver) type = KeyItem.Type.Gold;
                                                else if(type == KeyItem.Type.Gold) type = KeyItem.Type.Red;
                                        }
                                }

                        }

                }

                boolean valid = Room.fillRoomsWithStairs(dungeon, tiles, rooms, floorIndex);

                return valid;

        }

        static void fillRoomWithDoors(Dungeon dungeon, Tile[][] tiles, Room room, float chanceToBeLockedRoom, KeyItem.Type keyType) {
                // should be called after fillRoom() and fillTunnel()
                // This is a lot of code for a seemingly simple thing. but it checks to ensure
                // that these scenarios do not happen
                // 1 - two doors placed next to eachother
                // 2 - prevent the scenario of a tunnel going through a wall "long ways" causing the entire wall becoming a string of doors
                // 3 - prevent a door to be placed at the ends of double wide halls


                int doorCount = 0;
                Tile door = null;

                // TODO: door count needs to increment even on situations
                // where a door could not be created. for instance:
                //   - when a door wasnt spawned because itd be put next to another door
                //   - when a door wasnt spawned because it was extended through a hall skimming it (? i beleive i need this?)

                // top and bottom of room
                for (int y = room.y1; y <= room.y2; y += room.getHeight()) {
                        for (int x = room.x1; x <= room.x2; x++) {
                                if (tiles[x][y] != null && tiles[x][y].isFloor()) {
                                        if (UtFloorGen.countDoors(tiles, x, y) == 0 && UtFloorGen.isWall(tiles, x + 1, y) && UtFloorGen.isWall(tiles, x - 1, y)) {
                                                door = Tile.makeDoor();
                                                tiles[x][y] = door;
                                                doorCount++;
                                        }
                                }
                        }
                }
                // left and right of room
                for (int x = room.x1; x <= room.x2; x += room.getWidth()) {
                        for (int y = room.y1; y <= room.y2; y++) {
                                if (tiles[x][y] != null && tiles[x][y].isFloor()) {
                                        if (UtFloorGen.countDoors(tiles, x, y) == 0 && UtFloorGen.isWall(tiles, x, y - 1) && UtFloorGen.isWall(tiles, x, y + 1)) {
                                                door = Tile.makeDoor();
                                                tiles[x][y] = door;
                                                doorCount++;
                                        }
                                }
                        }
                }

                if(doorCount ==1 && chanceToBeLockedRoom >0){
                        boolean lockedRoom = dungeon.rand.bool(chanceToBeLockedRoom);
                        if(lockedRoom){
                                room.requiresKey  = keyType;
                                door.setDoorLocked(true, keyType);
                        }
                }

        }

        static boolean fillRoomsWithStairs(Dungeon dungeon, Tile[][] tiles, Array<Room> rooms, int floorIndex) {
                int i = 0;
                boolean valid = false;
                while (i < rooms.size && !valid) {
                        valid = fillRoomWithStairs(dungeon, tiles, rooms.get(i++), floorIndex, floorIndex + 1);
                }

                if (!valid) return false;
                valid = false;
                int j = rooms.size - 1;
                while (j > i && !valid) {
                        valid = fillRoomWithStairs(dungeon,tiles, rooms.get(j--), floorIndex, floorIndex - 1);
                }
                return valid;
        }

        private static boolean fillRoomWithStairs(Dungeon dungeon, Tile[][] tiles, Room room, int floorIndex, int floorIndexTo) {
                if(room.requiresKey != null && floorIndexTo < floorIndex){
                        return false; // we dont want to lock the player in on the first room.
                }
                if (room.x1 + 2 >= room.x2 - 2 || room.y1 + 2 >= room.y2 - 2) {
                        return false;
                }
                for (int i = 0; i < 10; i++) {
                        int x = dungeon.rand.range(room.x1 + 2, room.x2 - 2);
                        int y = dungeon.rand.range(room.y1 + 2, room.y2 - 2);
                        if (tiles[x][y].isFloor()) {
                                tiles[x][y] = Tile.makeStairs(floorIndex, floorIndexTo);
                                return true;
                        }
                }
                return false;
        }

        static boolean spawnKeys(Dungeon dungeon, FloorMap floorMap, Array<Room> rooms){
                // this basic spawner logic ensures that all rooms can be opened using the keys
                // from this floor, there will also be exactly the same number as keys as doors.
                // keys may not be spawned in the most interesting places
                // but the floor will be passable
                int[] numKeys = new int[3];
                for (Room room : rooms) {
                        if(room.requiresKey != null)
                        numKeys[room.requiresKey.ordinal()] ++;
                }

                KeyItem.Type[] values = KeyItem.Type.values();
                for (KeyItem.Type keyType : values) {
                        int numKeysToSpawn = numKeys[keyType.ordinal()];
                        int spawned =0;
                        int tries = 0;
                        while(spawned<numKeysToSpawn){
                                Room randomRoom = rooms.get(dungeon.rand.random.nextInt(rooms.size));
                                if(randomRoom.containsKey != null)
                                        continue; // can only have one key per room
                                if(keyType == KeyItem.Type.Silver){
                                        if(spawned ==0 && randomRoom.requiresKey != null)
                                                continue; // atleast one silver key must be in an unlocked room
                                }else if(keyType == KeyItem.Type.Gold){
                                        if(spawned == 0 && randomRoom.requiresKey != KeyItem.Type.Silver)
                                                continue; // first gold key must spawn in silver room
                                        else if(randomRoom.requiresKey == null || randomRoom.requiresKey == KeyItem.Type.Red)
                                                continue; // second gold key must spawn in a silver or gold room
                                }else if(keyType == KeyItem.Type.Red){
                                        if(randomRoom.requiresKey != KeyItem.Type.Gold)
                                                continue; // red keys can only spawn in gold rooms
                                }

                                boolean spawnInsideCrate = dungeon.rand.random.nextBoolean();
                                boolean valid = spawnLootInRoom(dungeon, floorMap, randomRoom, spawnInsideCrate ? ModelId.CeramicPitcher: null,new KeyItem(dungeon, keyType) );
                                if(valid){
                                        randomRoom.containsKey = keyType;
                                        spawned++;
                                        tries = 0;
                                }else{
                                        tries++;
                                        if(tries > 20)
                                                return false; // its likely that a winnable key combination can be created
                                }
                        }
                }
                return true;

        }

        private static boolean spawnLootInRoom(Dungeon dungeon, FloorMap floorMap, Room room, ModelId spawnInCrate, Item item) {
                for (int i = 0; i < 20; i++) {
                        int x = dungeon.rand.range(room.x1 + 1, room.x2 - 1);
                        int y = dungeon.rand.range(room.y1 + 1, room.y2 - 1);
                        Tile tile = floorMap.getTile(x,y);
                        if(!tile.isFloor()){
                            continue;
                        }
                        if(floorMap.hasTokensAt(x,y))
                                continue;

                        if(spawnInCrate != null){
                                dungeon.newCrateToken(floorMap,spawnInCrate.name(), spawnInCrate, item, x, y);
                                return true;
                        }else{
                                dungeon.newLootToken(floorMap, item, x, y);
                                return true;
                        }
                }
                return false;
        }





}
