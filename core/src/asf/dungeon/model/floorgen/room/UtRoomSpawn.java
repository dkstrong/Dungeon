package asf.dungeon.model.floorgen.room;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Pathfinder;
import asf.dungeon.model.Tile;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

/**
 * Created by Daniel Strong on 12/13/2014.
 */
public class UtRoomSpawn {

        public static void generateDoorGraph(Dungeon dungeon, FloorMap floorMap, Array<Room> rooms){
                Room startRoom=null;
                Array<Room> endRooms = new Array<Room>(true, 4, Room.class);

                for (Room room : rooms) {
                        if(room.isStartRoom(floorMap.index))
                                startRoom = room;
                        else if(room.isGoalRoom(floorMap.index) || room.isDeadEnd())
                                endRooms.add(room);
                }

                if(startRoom == null || endRooms.size <= 0)
                        throw new IllegalStateException("Floor must have an up stairs and a down stairs");
                Pair startLoc = new Pair(startRoom.getCenterX(), startRoom.getCenterY());
                Pair endLoc = new Pair();
                Array<Pair> path = new Array<Pair>(true, 64, Pair.class);
                Array<Room> currentRooms = new Array<Room>(true, 2, Room.class);
                Array<Room> lockedRooms = new Array<Room>(true, 8, Room.class);
                Array<Room> lootRooms = new Array<Room>(true, 8, Room.class);
                KeyItem.Type nextKey = KeyItem.Type.Silver;
                for (Room endRoom : endRooms) {
                        endLoc.set(endRoom.getCenterX(), endRoom.getCenterY());
                        boolean valid = floorMap.pathfinder.generate(null, startLoc, endLoc,path, Pathfinder.PathingPolicy.Manhattan, false, Integer.MAX_VALUE);
                        if(!valid) throw new IllegalStateException("there is no valid path between start and end stairs");

                        // step through the path from start room to this end room
                        // randomly create locked doors along the way
                        Doorway lastDoorWay;
                        while(path.size >0){
                                Pair pair = path.pop();
                                getRooms(rooms, pair, currentRooms);

                                for (Room currentRoom : currentRooms) {
                                       if(currentRoom == null)
                                                continue; // moving along a hallway
                                        Doorway nextDoorway=null;
                                        for (Doorway doorway : currentRoom.doorways) {
                                                if(pair.equals(doorway.x, doorway.y)){
                                                        nextDoorway = doorway;
                                                        break;
                                                }
                                        }

                                        if(nextDoorway!=null && nextDoorway.lockable && currentRoom.isDeadEnd() && !currentRoom.isStartRoom(floorMap.index)){
                                                nextDoorway.requiresKey = nextKey;
                                                lockedRooms.add(currentRoom);
                                                currentRoom.difficulty += (nextKey.ordinal()/10f);
                                                if(nextKey == KeyItem.Type.Silver) nextKey = KeyItem.Type.Gold;
                                                else nextKey = KeyItem.Type.Red;
                                        }

                                        if(nextDoorway!= null)
                                                lastDoorWay = nextDoorway;
                                }



                        }
                }


                for (Room lockedRoom : lockedRooms) { // for each room marked to be locked
                        for (Doorway lockedDoorway : lockedRoom.doorways) { // for each doorway in this room that is locked
                                if(lockedDoorway.requiresKey == null) continue;
                                if(floorMap.getTile(lockedDoorway.x, lockedDoorway.y).isDoorLocked()) continue; // already locked
                                lootRooms.clear();
                                Room lastLootRoom = null;
                                for (Room room : rooms) { // search for a room to place loot in, must have a valid path
                                        if(room == lockedRoom) continue;
                                        if(room.containsKey != null) continue;
                                        startLoc.set(lockedRoom.getCenterX(), lockedRoom.getCenterY());
                                        endLoc.set(room.getCenterX(), room.getCenterY());
                                        boolean valid = floorMap.pathfinder.generate(null, startLoc, endLoc,path, Pathfinder.PathingPolicy.Manhattan, false, Integer.MAX_VALUE);
                                        if(!valid) continue; // cant reach this room, so cant place the key in here!
                                        while(path.size > 0){
                                                Pair pair = path.pop();
                                                getRooms(rooms, pair, currentRooms);
                                                for (Room currentRoom : currentRooms) {
                                                        if(currentRoom == lockedRoom || currentRoom==lastLootRoom) continue;
                                                        lastLootRoom = currentRoom;
                                                        lootRooms.add(currentRoom);

                                                }
                                        }

                                }
                                if(lootRooms.size<=0) // if there are no valid rooms to palce key then this floor is invalid
                                        throw new IllegalStateException("no valid room to plae key in");

                                Room lootRoom = chooseRoomByWeight(dungeon, lootRooms);
                                boolean spawnInsideCrate = dungeon.rand.random.nextBoolean();
                                lootRoom.containsKey = lockedDoorway.requiresKey;
                                boolean valid = spawnLootInRoom(dungeon, floorMap, lootRoom, spawnInsideCrate ? ModelId.CeramicPitcher: null,new KeyItem(dungeon, lockedDoorway.requiresKey) );
                                floorMap.getTile(lockedDoorway.x, lockedDoorway.y).setDoorLocked(true, lockedDoorway.requiresKey);

                        }



                }


        }

        private static Room chooseRoomByWeight(Dungeon dungeon, Array<Room> lootRooms){
                float totalDifficulty = 0f;
                for (Room lootRoom : lootRooms) {
                        totalDifficulty += lootRoom.difficulty;
                }
                float rand = dungeon.rand.random.nextFloat() * totalDifficulty;
                float curDifficulty=0f;
                for (Room lootRoom : lootRooms) {
                        curDifficulty+=lootRoom.difficulty;
                        if(curDifficulty >=rand)
                                return lootRoom;
                }
                throw new AssertionError("no loot room was found");
        }

        private static void getRooms(Array<Room> rooms, Pair pair, Array<Room> storeRoomsOnPair){
                storeRoomsOnPair.clear();
                for (int i = 0; i < rooms.size; i++) {
                        Room room = rooms.get(i);
                        if(room.contains(pair)){
                                storeRoomsOnPair.add(room);
                        }
                }
        }

        public  static boolean spawnKeys(Dungeon dungeon, FloorMap floorMap, Array<Room> rooms){
                // this basic spawner logic ensures that all rooms can be opened using the keys
                // from this floor, there will also be exactly the same number as keys as doors.
                // keys may not be spawned in the most interesting places
                // but the floor will be passable
                int[] numKeys = new int[3];
                Room startRoom=null, goalRoom=null;


                for (Room room : rooms) {
                        for (Doorway doorway : room.doorways) {
                                if(doorway.requiresKey != null)
                                        numKeys[doorway.requiresKey.ordinal()]++;
                        }
                        if(room.isStartRoom(floorMap.index))
                                startRoom = room;
                        else if(room.isGoalRoom(floorMap.index))
                                goalRoom = room;

                }

                if(startRoom == null || goalRoom == null)
                        throw new IllegalStateException("Floor must have an up stairs and a down stairs");
                Pair startLoc = new Pair(startRoom.getCenterX(), startRoom.getCenterY());
                Pair goalLoc = new Pair(goalRoom.getCenterX(), goalRoom.getCenterY());

                Array<Pair> path = new Array<Pair>(true, 64, Pair.class);
                boolean valid = floorMap.pathfinder.generate(null, startLoc, goalLoc,path, Pathfinder.PathingPolicy.Manhattan, false, Integer.MAX_VALUE);
                if(!valid)
                        throw new IllegalStateException("there is no valid path between start and end stairs");


                Room currentRoom = startRoom;
                Iterator<Pair> iterator = path.iterator();
                while(iterator.hasNext()){
                        Pair pair = iterator.next();

                }

                Room randomRoom = rooms.get(dungeon.rand.random.nextInt(rooms.size));
                KeyItem.Type keyType = KeyItem.Type.Silver;
                boolean spawnInsideCrate = dungeon.rand.random.nextBoolean();
                valid = spawnLootInRoom(dungeon, floorMap, randomRoom, spawnInsideCrate ? ModelId.CeramicPitcher: null,new KeyItem(dungeon, keyType) );
                if(valid){
                        randomRoom.containsKey = keyType;
                }

                return true;

        }

        public  static boolean spawnLootInRoom(Dungeon dungeon, FloorMap floorMap, Room room, ModelId spawnInCrate, Item item) {
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

        public static Pair getRandomLocToSpawnCharacter(Dungeon dungeon, FloorMap floorMap, Room room){
                int x,y;
                do{

                        x = dungeon.rand.range(room.x1, room.x2);
                        y = dungeon.rand.range(room.y1, room.y2);
                }while(floorMap.getTile(x,y) == null || !floorMap.getTile(x,y).isFloor() || floorMap.hasTokensAt(x,y));
                return new Pair(x,y);
        }
}
