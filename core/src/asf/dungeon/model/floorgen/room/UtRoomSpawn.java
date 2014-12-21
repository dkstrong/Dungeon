package asf.dungeon.model.floorgen.room;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Pathfinder;
import asf.dungeon.model.Symbol;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.InvalidGenerationException;
import asf.dungeon.model.floorgen.UtFloorGen;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.model.token.Stairs;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.Torch;
import asf.dungeon.model.token.puzzle.CombinationDoorPuzzle;
import asf.dungeon.model.token.quest.TorchQuest;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

/**
 * Created by Daniel Strong on 12/13/2014.
 */
public class UtRoomSpawn {

        public static void carveLockedDoorsAndSpawnKeys(Dungeon dungeon, FloorMap floorMap, Array<Room> rooms) throws InvalidGenerationException{
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
                        throw new InvalidGenerationException("Floor must have an up stairs and a down stairs");

                //startRoom.intensity = -1f;
                // iterator over each end room, path is created from start to end, but we iterate over it
                // from end to beginning for effeciency
                Pair startLoc = new Pair(startRoom.getCenterX(), startRoom.getCenterY());
                Pair endLoc = new Pair();
                Array<Pair> path = new Array<Pair>(true, 128, Pair.class);
                Array<Pair> lootPath = new Array<Pair>(true, 128, Pair.class); // reusable temp array is passed to findRoomToPlaceKey
                Array<Room> lootRooms = new Array <Room>(true, 8, Room.class); // reusable temp set is passed to findRoomToPlaceKey
                // TODO: need to come up with a system for when red/gold/silver keys should appear
                // this will  also be mixed into other types of "keys" such as levers and mini puzzles, quest npcs, etc
                Symbol currentSymbol=null;

                endRoomLoop:
                for (Room endRoom : endRooms) {
                        // generate a path from
                        endLoc.set(endRoom.getCenterX(), endRoom.getCenterY());
                        boolean valid = floorMap.pathfinder.generate(null, startLoc, endLoc, path, Pathfinder.PathingPolicy.Manhattan, false, Integer.MAX_VALUE);
                        if(!valid){
                                continue endRoomLoop;
                        }
                        Room lastRoom = null; // need to store the last room, when going throgh hallways we use last room instead of currentroom

                        while(path.size > 2){ // if the path is less than 3 units then theres no way there are valid doors
                                Pair lastLoc = path.pop();
                                Pair currentLoc = path.get(path.size-1);
                                Tile currentTile = floorMap.getTile(currentLoc);
                                if(!currentTile.isDoor()){
                                        // were not interested in this door, but we save the room as lastRoom still
                                        Room currentRoom = getRoom(rooms, currentLoc);
                                        if(currentRoom != null) lastRoom = currentRoom;
                                        continue;
                                }

                                Room currentRoom = getRoomOfDoorLocation(rooms, currentLoc, lastLoc, lastRoom);
                                lastRoom = currentRoom;
                                if(currentRoom == null){
                                        //UtFloorGen.printFloorTile(floorMap.tiles, currentLoc);
                                        throw new InvalidGenerationException("a door with no room? oh my! currentLoc: "+currentLoc+", lastLoc: "+lastLoc);
                                }
                                Doorway currentDoorway = getDoorway(rooms, currentLoc);
                                if(currentDoorway.requiresSymbol != null)
                                        continue endRoomLoop; // this pathway is already pretty well locked off, no need for any more locked doors here..

                                boolean isChokepoint = isDoorwayToDeadEnd(floorMap.tiles, tilesCopy, currentLoc, lastLoc);
                                // if this is a chokepoint then this could be an ideal door location, this means
                                // that you cant get to the other side of the door through another room or hallway.

                                // if isChokepoint == true then this also means tilesCopy now contains information
                                // about which tiles could be valid locations for a key (invalid floor locations
                                // are now null tiles in the copy.)

                                if(isChokepoint && dungeon.rand.bool(.45f)){
                                        currentSymbol = nextSymbol(currentSymbol, currentTile);
                                        currentDoorway.requiresSymbol = currentSymbol;
                                        int numKeys =1;
                                        // TODO: in order to generate more than 1 key for this doorway we need to include
                                        // code that ensures there will be enough loot rooms to contain all the symbols.
                                        // ie. cant put a torch puzzle wiht 2 torches on the first door.
                                        for(int i=0; i < numKeys; i++){
                                                Room lootRoom = findRoomToPlaceKey(lootPath, lootRooms,
                                                        dungeon, floorMap, rooms, currentLoc,
                                                        currentRoom, currentDoorway, tilesCopy);
                                                lootRoom.containsSymbol = currentSymbol;
                                                UtRoomSpawn.spawnTokenForSymbol(dungeon, floorMap, lootRoom, tilesCopy, currentSymbol);
                                        }

                                        continue endRoomLoop;
                                }


                        }
                }

                // now we actually lock the doors on the actual floormap that were flagged to be locked
                for (Room room : rooms) {
                        for (Doorway doorway : room.doorways) {
                                if(doorway.requiresSymbol == null) continue;
                                Tile tile = floorMap.getTile(doorway.x, doorway.y);
                                doorway.requiresSymbol.lockDoor(dungeon, floorMap, doorway, tile);
                        }
                }
        }

        private static Symbol nextSymbol(Symbol currentSymbol, Tile currentTile){
                if(currentSymbol == null)
                        return new CombinationDoorPuzzle();
                else if(currentSymbol instanceof CombinationDoorPuzzle){
                        return new KeyItem(KeyItem.Type.Red);
                }else if(currentSymbol instanceof KeyItem){
                        return new KeyItem(KeyItem.Type.Gold);
                }else{
                        return new KeyItem(KeyItem.Type.Silver);
                }

        }

        public static void spawnTokenForSymbol(Dungeon dungeon, FloorMap floorMap, Room room, Tile[][] validLocations, Symbol symbol){
                if(symbol instanceof KeyItem){
                        Pair pair= UtRoomSpawn.getRandomLocToSpawnCharacter(dungeon, floorMap, room, validLocations);
                        if(dungeon.rand.random.nextBoolean()){
                                dungeon.newCrateToken(floorMap, ModelId.CeramicPitcher.name(), ModelId.CeramicPitcher, (KeyItem)symbol, pair.x, pair.y);
                        }else{
                                dungeon.newLootToken(floorMap, (KeyItem)symbol, pair.x, pair.y);
                        }
                }else if(symbol instanceof CombinationDoorPuzzle){
                        CombinationDoorPuzzle puzzle = (CombinationDoorPuzzle) symbol;
                        Token torchToken = new Token(dungeon, "Torch", ModelId.Torch);
                        torchToken.add(new Torch(torchToken, false, puzzle));
                        torchToken.add(new TorchQuest());
                        Pair loc = UtRoomSpawn.getRandomLocToSpawnCharacter(dungeon, floorMap, room, validLocations);
                        dungeon.newToken(torchToken, floorMap, loc.x, loc.y);
                        puzzle.addPiece(torchToken, true);
                }

        }

        private static Room findRoomToPlaceKey(Array<Pair> lootPath, Array<Room> lootRooms, Dungeon dungeon, FloorMap floorMap, Array<Room> rooms, Pair currentLoc, Room currentRoom, Doorway currentDoorway, Tile[][] validLocations){
                lootRooms.clear();
                // first we make a list of candiadte loot rooms based on information from flood filling (valid locations)
                for(int x=0; x < validLocations.length; x++){
                        for(int y=0; y<validLocations[0].length; y++){
                                Tile tile = validLocations[x][y];
                                if(tile == null || !tile.isFloor())
                                        continue;
                                for (Room room : rooms) {
                                        if(!lootRooms.contains(room, true) && room.contains(x,y)){
                                                lootRooms.add(room);
                                        }
                                }
                        }
                }
                if(lootRooms.size > 0){
                        // next we sanitize the candiadate loot rooms by remoing rooms with any one the following conditions
                        // 1. already contains some other special loot
                        // 2. has no path from the currentRoom (room being locked) to the lootRoom
                        // 3. has a path, but involves going through a locked door whose key is contained in the current room (this would create a circular dependency and it wouldnt be impossible to solve the floor)
                        // 4. the loot room and the current room are the same to prevent locking the key in its own room.
                        Pair endLoc = new Pair();
                        Iterator<Room> i = lootRooms.iterator();
                        roomLoop:
                        while(i.hasNext()){
                                Room r = i.next();
                                if(r == currentRoom || r.containsSymbol != null){
                                        i.remove();
                                        continue roomLoop;
                                }
                                endLoc.set(r.getCenterX(), r.getCenterY());
                                boolean valid = floorMap.pathfinder.generate(null, currentLoc, endLoc, lootPath, Pathfinder.PathingPolicy.Manhattan, false, Integer.MAX_VALUE);
                                if(!valid) {
                                        i.remove();
                                        continue roomLoop;
                                }
                                if(currentRoom.containsSymbol == null)
                                        continue roomLoop; // room doesnt contain a key, as long as theres a path to the loot room then its a valid candidate

                                while(lootPath.size >0){
                                        Pair lootCurrentLoc = lootPath.pop();
                                        Tile tile = floorMap.getTile(lootCurrentLoc);
                                        if(!tile.isDoor())
                                                continue;
                                        for (Room roomWithDoors : rooms) {
                                                for (Doorway doorway : roomWithDoors.doorways) {
                                                        if(doorway.requiresSymbol == currentRoom.containsSymbol && doorway.x == lootCurrentLoc.x && doorway.y == lootCurrentLoc.y){
                                                                // invalid room to place key, lets continue to the next
                                                                // room on the list
                                                                i.remove();
                                                                continue roomLoop;
                                                        }
                                                }
                                        }
                                }
                        }
                }



                // Now that we have a list of ideal candidate rooms to place the key in, now we pick one

                if(lootRooms.size <=0){
                        UtFloorGen.printFloorTile(validLocations, currentLoc);
                        throw new InvalidGenerationException("no valid room to place key in");
                }


                // TODO: the start and goal rooms should have an even more decreased chance to contain the key than any other room.
                // They should mainly just be a fall back if other rooms will not work
                float totalDifficulty = 0f;
                for (Room lootRoom : lootRooms) {
                        totalDifficulty+=lootRoom.getIntensity(floorMap.index);
                }
                float rand = dungeon.rand.random.nextFloat() * totalDifficulty;
                float curDifficulty = 0f;
                for (Room lootRoom : lootRooms) {
                        curDifficulty+=lootRoom.getIntensity(floorMap.index);
                        if(curDifficulty >= rand)
                                return lootRoom;
                }
                //UtFloorGen.printFloorTile(validLocations, currentLoc);
                throw new InvalidGenerationException("not loot room was found");
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

        private static Room getRoom(Array<Room> rooms, Pair currentLoc){
                // returns the first room found at this location
                // WARNING this method should not be used on edge tiles of rooms
                // (such as doorways) because rooms can overlap on edges. For those cases
                // use getRoomOfDoorLocation() instead!
                for (Room room : rooms) {
                        if(room.contains(currentLoc)){
                                 return room;
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

        private static Room chooseRoomByWeight(Dungeon dungeon, FloorMap floorMap, Array<Room> lootRooms){
                float totalDifficulty = 0f;
                for (Room lootRoom : lootRooms) {
                        totalDifficulty += lootRoom.getIntensity(floorMap.index);
                }
                float rand = dungeon.rand.random.nextFloat() * totalDifficulty;
                float curDifficulty=0f;
                for (Room lootRoom : lootRooms) {
                        curDifficulty+=lootRoom.getIntensity(floorMap.index);
                        if(curDifficulty >=rand)
                                return lootRoom;
                }
                throw new AssertionError("no loot room was found");
        }

        public static void spawnLootInRoom(Dungeon dungeon, FloorMap floorMap, Room room, ModelId spawnInCrate, Item item, Tile[][] validLocations) {

                Pair pair = getRandomLocToSpawnCharacter(dungeon, floorMap, room, validLocations);
                if(spawnInCrate != null)
                        dungeon.newCrateToken(floorMap,spawnInCrate.name(), spawnInCrate, item, pair.x, pair.y);
                else
                        dungeon.newLootToken(floorMap, item, pair.x, pair.y);

        }

        public static void spawnStairs(Dungeon dungeon, FloorMap floorMap, Array<Room> rooms){
                int i = 0;
                boolean valid = false;
                while (i < rooms.size && !valid) {
                        valid = spawnStairsInRoom(dungeon, floorMap, rooms.get(i++), floorMap.index + 1);
                }
                if (!valid) throw new InvalidGenerationException("Couldn't find location for down stairs");
                valid = false;
                int j = rooms.size - 1;
                while (j >= i && !valid) {
                        valid = spawnStairsInRoom(dungeon, floorMap, rooms.get(j--),  floorMap.index - 1);
                }
                if (!valid) throw new InvalidGenerationException("Couldn't find location for up stairs");
        }

        private static boolean spawnStairsInRoom(Dungeon dungeon, FloorMap floorMap, Room room, int floorIndexTo ){
                if (room.x1 + 2 >= room.x2 - 2 || room.y1 + 2 >= room.y2 - 2) {
                        return false; // room too small
                }

                int x,y;
                do{
                        x = dungeon.rand.range(room.x1 + 2, room.x2 - 2);
                        y = dungeon.rand.range(room.y1 + 2, room.y2 - 2);
                }while(floorMap.isLocationBlocked(x,y));

                Token stairsToken = new Token(dungeon, "Stairs", null);
                stairsToken.add(new Stairs(stairsToken, floorIndexTo));
                stairsToken.setDirection(Direction.East);
                dungeon.newToken(stairsToken, floorMap, x,y);

                room.containsStairsTo = floorIndexTo;

                return true;
        }

        public static Pair getRandomLocToSpawnCharacter(Dungeon dungeon, FloorMap floorMap, Room room, Tile[][] validLocations){
                Pair pair = new Pair();
                for(int tries= 0; tries < 20; ++tries){
                        pair.x = dungeon.rand.range(room.x1, room.x2);
                        pair.y = dungeon.rand.range(room.y1, room.y2);
                        if(validLocations != null && validLocations[pair.x][pair.y] == null) continue;
                        Tile tile = floorMap.getTile(pair.x, pair.y);
                        if(tile == null || !tile.isFloor()) continue;
                        if(floorMap.getTile(pair.x+1,pair.y).isDoor()) continue;
                        if(floorMap.getTile(pair.x-1,pair.y).isDoor()) continue;
                        if(floorMap.getTile(pair.x,pair.y+1).isDoor()) continue;
                        if(floorMap.getTile(pair.x,pair.y-1).isDoor()) continue;
                        if(floorMap.hasTokensAt(pair.x, pair.y)) continue;
                        return pair;

                }
                throw new InvalidGenerationException("There is nowhere to spawn token in this room");
        }


}
