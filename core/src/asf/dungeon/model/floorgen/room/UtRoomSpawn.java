package asf.dungeon.model.floorgen.room;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Pathfinder;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.UtFloorGen;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

/**
 * Created by Daniel Strong on 12/13/2014.
 */
public class UtRoomSpawn {

        public static void carveLockedDoorsAndSpawnKeys(Dungeon dungeon, FloorMap floorMap, Array<Room> rooms){
                // this algorithm works by making a path between each end room and the start room
                // and analyzing its doorways using floodfill to determine if they should be locked
                // then puts a key somewhere on the level on the other side of the door

                Tile [][] tilesCopy = new Tile[floorMap.tiles.length][];
                for(int x = 0; x < floorMap.tiles.length; x++) tilesCopy[x] = floorMap.tiles[x].clone();

                // find the start room, and all end rooms (rooms with only one doorway)
                Room startRoom=null;
                Array<Room> endRooms = new Array<Room>(true, 4, Room.class);
                for (Room room : rooms) {
                        if(room.isStartRoom(floorMap.index))
                                startRoom = room;
                        else if(room.isGoalRoom(floorMap.index) || room.isDeadEnd())
                                endRooms.add(room);
                }
                // TODO: ensure goal room is at the top of the endRooms list
                if(startRoom == null || endRooms.size <= 0)
                        throw new IllegalStateException("Floor must have an up stairs and a down stairs");

                startRoom.difficulty = -1f;
                // iterator over each end room, path is created from start to end, but we iterate over it
                // from end to beginning for effeciency
                Pair startLoc = new Pair(startRoom.getCenterX(), startRoom.getCenterY());
                Pair endLoc = new Pair();
                Array<Pair> path = new Array<Pair>(true, 128, Pair.class);
                Array<Pair> lootPath = new Array<Pair>(true, 128, Pair.class); // reusable temp array is passed to findRoomToPlaceKey
                ObjectSet<Room> lootRooms = new ObjectSet <Room>(8); // reusable temp set is passed to findRoomToPlaceKey
                // TODO: need to come up with a system for when red/gold/silver keys should appear
                // this will  also be mixed into other types of "keys" such as levers and mini puzzles, quest npcs, etc
                KeyItem.Type currentKey = KeyItem.Type.Red;

                endRoomLoop:
                for (Room endRoom : endRooms) {
                        // generate a path from
                        endLoc.set(endRoom.getCenterX(), endRoom.getCenterY());
                        boolean valid = floorMap.pathfinder.generate(null, startLoc, endLoc, path, Pathfinder.PathingPolicy.Manhattan, false, Integer.MAX_VALUE);
                        if(!valid) throw new IllegalStateException("there is no valid path between start and end rooms");
                        Room lastRoom = null; // need to store the last room, when going throgh hallways we use last room instead of currentroom

                        while(path.size > 2){ // if the path is less than 3 units then theres no way there are valid doors
                                Pair lastLoc = path.pop();
                                Pair currentLoc = path.get(path.size-1);

                                if(!floorMap.getTile(currentLoc).isDoor())
                                        continue;
                                Room currentRoom = getRoomOfDoorLocation(rooms, currentLoc, lastLoc, lastRoom);
                                lastRoom = currentRoom;
                                if(currentRoom == null){
                                        throw new AssertionError("a door with no room? oh my! currentLoc: "+currentLoc+", lastLoc: "+lastLoc);
                                }
                                Doorway currentDoorway = getDoorway(rooms, currentLoc);
                                if(currentDoorway.requiresKey != null)
                                        continue endRoomLoop; // this pathway is already pretty well locked off, no need for any more locked doors here..
                                if(!currentDoorway.lockable)
                                        continue;
                                boolean isChokepoint = isDoorwayToDeadEnd(floorMap.tiles, tilesCopy, currentLoc, lastLoc);
                                // if this is a chokepoint then this could be an ideal door location, this means
                                // that you cant get to the other side of the door through another room or hallway.

                                // if isChokepoint == true then this also means tilesCopy now contains information
                                // about which tiles could be valid locations for a key (invalid floor locations
                                // are now null tiles in the copy.)

                                if(isChokepoint && dungeon.rand.bool(.45f)){
                                        currentDoorway.requiresKey = currentKey;
                                        if(currentKey == KeyItem.Type.Red) currentKey = KeyItem.Type.Gold;
                                        else currentKey = KeyItem.Type.Silver;
                                        // this increments the difficult more for silver rooms than red rooms
                                        // this is to make it more likely for keys to spawn in silver rooms
                                        // TODO: but i may want to tweak this as better loot and stronger mosnters should already spawn in red rooms..
                                        currentRoom.difficulty += (3-currentDoorway.requiresKey.ordinal()) / 10f;
                                        Room lootRoom = findRoomToPlaceKeyBetter(lootPath, lootRooms,
                                                dungeon, floorMap, rooms, currentLoc,
                                                currentRoom, currentDoorway, tilesCopy);

                                        boolean spawnInsideCrate = dungeon.rand.random.nextBoolean();
                                        lootRoom.containsKey = currentDoorway.requiresKey;
                                        spawnLootInRoom(dungeon, floorMap, lootRoom, spawnInsideCrate ? ModelId.CeramicPitcher: null,new KeyItem(dungeon, lootRoom.containsKey),tilesCopy );
                                        continue endRoomLoop;
                                }




                        }
                }

                // now we actually lock the doors on the actual floormap that were flagged to be locked
                for (Room room : rooms) {
                        for (Doorway doorway : room.doorways) {
                                if(doorway.requiresKey == null) continue;
                                Tile tile = floorMap.getTile(doorway.x, doorway.y);
                                tile.setDoorLocked(true, doorway.requiresKey);
                        }
                }
        }

        private static Room findRoomToPlaceKeyBetter(Array<Pair> lootPath ,ObjectSet<Room> lootRooms, Dungeon dungeon, FloorMap floorMap, Array<Room> rooms, Pair currentLoc,Room currentRoom, Doorway currentDoorway, Tile[][] validLocations){
                lootRooms.clear();
                for(int x=0; x < validLocations.length; x++){
                        for(int y=0; y<validLocations[0].length; y++){
                                Tile tile = validLocations[x][y];
                                if(tile == null || !tile.isFloor())
                                        continue;
                                for (Room room : rooms) {
                                        if(room.contains(x,y)){
                                                lootRooms.add(room);
                                        }
                                }
                        }
                }

                Pair endLoc = new Pair();
                ObjectSet.ObjectSetIterator<Room> i = lootRooms.iterator();
                roomLoop:
                while(i.hasNext){
                        Room r = i.next();
                        if(r == currentRoom || r.containsKey != null){
                                i.remove();
                                continue roomLoop;
                        }
                        endLoc.set(r.getCenterX(), r.getCenterY());
                        boolean valid = floorMap.pathfinder.generate(null, currentLoc, endLoc, lootPath, Pathfinder.PathingPolicy.Manhattan, false, Integer.MAX_VALUE);
                        if(!valid) {
                                i.remove();
                                continue roomLoop;
                        }
                        if(currentRoom.containsKey == null)
                                continue roomLoop; // room doesnt contain a key, as long as theres a path to the loot room then its a valid candidate

                        while(lootPath.size >0){
                                Pair lootCurrentLoc = lootPath.pop();
                                Tile tile = floorMap.getTile(lootCurrentLoc);
                                if(!tile.isDoor())
                                        continue;
                                for (Room roomWithDoors : rooms) {
                                        for (Doorway doorway : roomWithDoors.doorways) {
                                                if(doorway.requiresKey == currentRoom.containsKey && doorway.x == lootCurrentLoc.x && doorway.y == lootCurrentLoc.y){
                                                        // invalid room to place key, lets continue to the next
                                                        // room on the list
                                                        i.remove();
                                                        continue roomLoop;
                                                }
                                        }
                                }
                        }
                }

                // TODO: instead of using just difficulty, need to also factor distance from currentRoom (the room being locked)
                // and/or other factors. RIght now keys tend to spawn in uninteresting places
                if(lootRooms.size <=0)
                        throw new IllegalStateException("no valid room to place key in");
                // now pick a room from the lootRooms set to be the room that will contain the key
                // we will choose the room using a weighted random algorithm so that the
                // key is more likely to spawn in more difficult rooms
                float totalDifficulty = 0f;
                for (Room lootRoom : lootRooms) {
                        totalDifficulty+=lootRoom.difficulty;
                }
                float rand = dungeon.rand.random.nextFloat() * totalDifficulty;
                float curDifficulty = 0f;
                for (Room lootRoom : lootRooms) {
                        curDifficulty+=lootRoom.difficulty;
                        if(curDifficulty >= rand)
                                return lootRoom;
                }
                throw new AssertionError("not loot room was found");
        }

        private static boolean isDoorwayToDeadEnd(Tile[][] tiles, Tile[][] storeTiles, Pair currentLoc, Pair lastLoc){
                for(int x=0; x< tiles.length; x++)
                        for(int y=0; y<tiles[0].length; y++)
                                storeTiles[x][y] = tiles[x][y];

                storeTiles[currentLoc.x][currentLoc.y] = null;
                UtFloorGen.floodFill(storeTiles, lastLoc.x, lastLoc.y);
                //UtFloorGen.printFloorTile(storeTiles,currentLoc);

                if(UtFloorGen.isWall(storeTiles, currentLoc.x-1, currentLoc.y) &&
                        UtFloorGen.isWall(storeTiles, currentLoc.x+1, currentLoc.y) &&
                        UtFloorGen.isWall(storeTiles, currentLoc.x, currentLoc.y-1) &&
                        UtFloorGen.isWall(storeTiles, currentLoc.x, currentLoc.y+1)){
                        return false; // it loops
                }

                return true;

        }


        private static Doorway getDoorway(Array<Room> rooms, Pair currentLoc){
                for (Room room : rooms) {
                        for (Doorway doorway : room.doorways) {
                                if(doorway.x == currentLoc.x && doorway.y == currentLoc.y){
                                        return doorway;
                                }
                        }
                }
                return null;
        }
        private static Room getRoomOfDoorLocation(Array<Room> rooms, Pair currentLoc, Pair lastLoc, Room lastRoom){
                // finds out what room currentLoc is in
                // some rooms can overlap on their borders, for those cases we look at both
                // current loc and last loc to see what room the mover is walking out of, the room
                // being walked out of is the room that will be returned.

                // this only works if currentLoc is the location of a valid doorway (on the border of a room, has 2 floor tiles EW or NS of it with 2 wall tiles in the other direction)

                for (Room room : rooms) {
                        if(room.contains(currentLoc) && room.contains(lastLoc)){
                                return room;
                        }
                }
                return lastRoom;
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

        public  static boolean spawnLootInRoom(Dungeon dungeon, FloorMap floorMap, Room room, ModelId spawnInCrate, Item item, Tile[][] validLocations) {
                for (int i = 0; i < 20; i++) {
                        int x = dungeon.rand.range(room.x1 + 1, room.x2 - 1);
                        int y = dungeon.rand.range(room.y1 + 1, room.y2 - 1);
                        if(validLocations != null){
                                if(validLocations[x][y] == null)
                                        continue;
                        }
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
