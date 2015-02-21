package asf.dungeon.model.floorgen.room;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FloorType;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.floorgen.UtFloorGen;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.Fountain;
import asf.dungeon.model.token.SpikeTrap;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.TokenFactory;
import asf.dungeon.model.token.logic.fsm.FsmLogic;
import asf.dungeon.model.token.logic.fsm.Monster;
import asf.dungeon.model.token.logic.fsm.QuestNPC;
import asf.dungeon.model.token.quest.PotionQuest;
import com.badlogic.gdx.utils.Array;

/**
 * makes a floor that is similiar to the classic "Rouge" maps where
 * there are range rooms connected by hallways
 * <p/>
 * Created by Danny on 11/4/2014.
 */
public class ConnectedRoomsGen implements FloorMapGenerator, FloorMap.MonsterSpawner {

        private int minRoomSize = 6;
        private int maxRoomSize = 9;
        private int minFloorWidth = 40;
        private int maxFloorWidth = 50;
        private int minFloorHeight = 30;
        private int maxFloorHeight = 50;
        private int maxRooms = 8;

        @Override
        public FloorMap generate(Dungeon dungeon, FloorType floorType, int floorIndex) {
                 minRoomSize = 6;
                 maxRoomSize = 9;
                 minFloorWidth = 40;
                 maxFloorWidth = 50;
                 minFloorHeight = 30;
                 maxFloorHeight = 50;
                 maxRooms = 8;
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

                UtRoomCarve.fillAndCarve(dungeon, floorIndex, tiles, rooms);


                FloorMap floorMap = new FloorMap(floorType, floorIndex, tiles, this);
                UtRoomSpawn.spawnStairs(dungeon, floorMap, rooms);
                UtRoomSpawn.carveLockedDoorsAndSpawnKeys(dungeon, floorMap, rooms);
                UtFloorGen.spawnCharacters(dungeon, floorMap);
                UtFloorGen.spawnRandomCrates(dungeon, floorMap);

                if(floorIndex ==0){
                        Room roomEnd = rooms.get(rooms.size-1);

                        Token traveler = TokenFactory.questCharacterToken(dungeon,"Traveler", ModelId.Priest, new FsmLogic(2,roomEnd, QuestNPC.PauseThenMove),new PotionQuest());
                        Pair loc = UtRoomSpawn.getRandomLocToSpawnCharacter(dungeon, floorMap, roomEnd, null);
                        dungeon.newToken(traveler, floorMap, loc.x, loc.y);

                        Token spikeTrap = new Token(dungeon, "Spike Trap", ModelId.SpikeTrap);
                        spikeTrap.add(new SpikeTrap(spikeTrap));
                        loc = UtRoomSpawn.getRandomLocToSpawnCharacter(dungeon, floorMap, roomEnd, null);
                        dungeon.newToken(spikeTrap, floorMap, loc.x, loc.y);

                        Token trainingDummy = new Token(dungeon, "Training Dummy", ModelId.TrainingDummy);
                        loc = UtRoomSpawn.getRandomLocToSpawnCharacter(dungeon, floorMap, roomEnd, null);
                        dungeon.newToken(trainingDummy, floorMap, loc.x, loc.y);


                }else{
                        Room roomEnd = rooms.get(dungeon.rand.random.nextInt(rooms.size));
                        Token fountainToken = new Token(dungeon, "Fountain", ModelId.Fountain);
                        fountainToken.add(new Fountain(fountainToken, PotionItem.Type.Health));
                        Pair loc = UtRoomSpawn.getRandomLocToSpawnCharacter(dungeon, floorMap, roomEnd, null);
                        dungeon.newToken(fountainToken, floorMap, loc.x, loc.y);
                        //Gdx.app.log("ConnectedRoomGen","fountain at: "+loc);
                }







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
                        ModelId modelId = dungeon.rand.random.nextBoolean() ? ModelId.Rat : ModelId.Skeleton;
                        do{
                                x = dungeon.rand.random.nextInt(floorMap.getWidth());
                                y = dungeon.rand.random.nextInt(floorMap.getHeight());
                        }while(floorMap.getTile(x,y) == null || !floorMap.getTile(x,y).isFloor() || floorMap.hasTokensAt(x,y));

                        Token token = TokenFactory.characterToken(dungeon,modelId.name(), modelId, new FsmLogic(1, null, Monster.Sleep),new Experience(1, 8, 4, 6, 1,1) );


                        if(modelId == ModelId.Archer){
                                WeaponItem weapon = new WeaponItem(dungeon, 2,2,1, true,3,1);
                                token.inventory.add(weapon);
                                token.inventory.equip(weapon);
                        }

                        dungeon.newToken(token, floorMap, x, y);

                }
        }
}
