package asf.dungeon.model.floorgen;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FxId;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.InteractChat;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.fsm.FSMLogic;
import asf.dungeon.model.token.logic.fsm.Monster;
import com.badlogic.gdx.Gdx;
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

                Room roomEnd = rooms.get(rooms.size-1);
                Token chatToken = new Token(dungeon, "Priest", ModelId.Priest);
                InteractChat chat = new InteractChat();
                chat.setMessage("Hello my name is Priest");
                chat.setChoices(new String[]{"Hello","Goodbye"});
                chatToken.add(chat);
                Pair loc = Room.getRandomLocToSpawnCharacter(dungeon, floorMap, roomEnd);
                dungeon.newToken(chatToken, floorMap, loc.x, loc.y);

                Token fountainToken = new Token(dungeon, "Fountain", ModelId.Fountain);
                InteractChat fountainInteract = new InteractChat();
                fountainInteract.setMessage("A mysterious fountain. Who know what would happen if you drank from it.");
                fountainInteract.setChoices(new String[]{"Do it","Don't do it"});
                fountainToken.add(fountainInteract);
                loc = Room.getRandomLocToSpawnCharacter(dungeon, floorMap, roomEnd);
                dungeon.newToken(fountainToken, floorMap, loc.x, loc.y);
                Gdx.app.log("ConnectedRoomGen","fountain at: "+loc);




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
                if(countTeam1 <2){
                        int x, y;
                        ModelId modelId = dungeon.rand.random.nextBoolean() ? ModelId.Skeleton : ModelId.Skeleton;
                        do{
                                x = dungeon.rand.random.nextInt(floorMap.getWidth());
                                y = dungeon.rand.random.nextInt(floorMap.getHeight());
                        }while(floorMap.getTile(x,y) == null || !floorMap.getTile(x,y).isFloor() || floorMap.hasTokensAt(x,y));

                        Token token = dungeon.newCharacterToken(floorMap, modelId.name(),
                                modelId,
                                new FSMLogic(1, null, Monster.Sleep),
                                new Experience(1, 8, 4, 6, 1,1),
                                x,y);

                        if(modelId == ModelId.Archer){
                                WeaponItem weapon = new WeaponItem(ModelId.Sword,"Bow", 1);
                                weapon.setRanged(true);
                                weapon.setProjectileFx(FxId.Arrow);
                                token.getInventory().add(weapon);
                                token.getInventory().equals(weapon);
                        }

                }
        }
}
