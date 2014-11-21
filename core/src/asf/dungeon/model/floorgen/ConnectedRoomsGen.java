package asf.dungeon.model.floorgen;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Tile;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.AiLogic;
import com.badlogic.gdx.utils.Array;

/**
 * makes a floor that is similiar to the classic "Rouge" maps where
 * there are range rooms connected by hallways
 * <p/>
 * Created by Danny on 11/4/2014.
 */
public class ConnectedRoomsGen implements FloorMapGenerator, FloorMap.MonsterSpawner {

        private int minRoomSize = 6;
        private int maxRoomSize = 10;
        private int minFloorWidth = 40;
        private int maxFloorWidth = 50;
        private int minFloorHeight = 30;
        private int maxFloorHeight = 50;
        private int maxRooms = 8;
        private boolean makeDoors = true;
        //private int floorIndex;

        @Override
        public FloorMap generate(Dungeon dungeon, int floorIndex) {
                int floorWidth = dungeon.rand.range(minFloorWidth, maxFloorWidth);
                int floorHeight = dungeon.rand.range(minFloorHeight, maxFloorHeight);

                Tile[][] tiles = new Tile[floorWidth][floorHeight];
                int numRooms = Math.round(floorWidth / maxRoomSize * floorHeight / maxRoomSize * .5f);
                if (numRooms > maxRooms) numRooms = maxRooms;
                numRooms -= dungeon.rand.random.nextInt(Math.round(numRooms * .25f));

                Array<Room> rooms = new Array<Room>(true, numRooms, Room.class);
                // make rooms
                while (rooms.size < numRooms) {
                        Room newRoom = new Room(0, 0, maxRoomSize, maxRoomSize);
                        do {
                                int roomWidth = dungeon.rand.range(minRoomSize, maxRoomSize);
                                int roomHeight = dungeon.rand.range(minRoomSize, maxRoomSize);
                                int x = dungeon.rand.random.nextInt(tiles.length);
                                int y = dungeon.rand.random.nextInt(tiles[0].length);

                                newRoom.set(x, y, x + roomWidth, y + roomHeight);

                        } while (!isValidLocation(tiles, newRoom, rooms));
                        rooms.add(newRoom);
                }

                Room.fillRooms(tiles, rooms);
                Room.fillTunnels(dungeon, tiles, rooms);
                boolean valid = Room.carveDoorsKeysStairs(dungeon, floorIndex, tiles, rooms, true, true);
                if(!valid) throw new Error("could not generate valid stairs locations, need to regenrate");

                FloorMap floorMap = new FloorMap(floorIndex, tiles, this);
                UtFloorGen.spawnCharacters(dungeon, floorMap);
                UtFloorGen.spawnRandomCrates(dungeon, floorMap);
                valid = Room.spawnKeys(dungeon, floorMap, rooms);
                if(!valid) throw new Error("could not generate valid key locations, need to regenrate");
                return floorMap;
        }

        private static boolean isValidLocation(Tile[][] tiles, Room testRoom, Array<Room> rooms) {
                if (testRoom.x2 >= tiles.length || testRoom.y2 >= tiles[0].length)
                        return false;

                for (Room room : rooms) {
                        if (testRoom.intersects(room)) {
                                return false;
                        }
                }


                return true;
        }


        @Override
        public void spawnMonsters(Dungeon dungeon, FloorMap floorMap) {
                int countTeam1 = floorMap.getTokensOnTeam(1).size;
                if(countTeam1 == 0){
                        int x, y;
                        do{
                                x = dungeon.rand.random.nextInt(floorMap.getWidth());
                                y = dungeon.rand.random.nextInt(floorMap.getHeight());
                        }while(floorMap.getTile(x,y) == null || !floorMap.getTile(x,y).isFloor() || floorMap.hasTokensAt(x,y));

                        Token token = dungeon.newCharacterToken(floorMap, "Monster",
                                ModelId.Berzerker,
                                new AiLogic(1),
                                new Experience(1, 8, 4, 6, 1,1),
                                x,y);

                        //EquipmentItem sword = EquipmentItem.makeWeapon("Sword", 1);
                        //token.getInventory().add(sword);
                        //token.getInventory().equip(sword);

                }
        }
}
