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
