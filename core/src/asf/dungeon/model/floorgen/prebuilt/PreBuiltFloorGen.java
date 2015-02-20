package asf.dungeon.model.floorgen.prebuilt;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FloorType;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.floorgen.UtFloorGen;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.model.token.Boulder;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.Fountain;
import asf.dungeon.model.token.PressurePlate;
import asf.dungeon.model.token.SpikeTrap;
import asf.dungeon.model.token.Stairs;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.fsm.FsmLogic;
import asf.dungeon.model.token.logic.fsm.Monster;
import asf.dungeon.model.token.logic.fsm.QuestNPC;
import asf.dungeon.model.token.puzzle.CombinationDoorPuzzle;
import asf.dungeon.model.token.quest.PotionQuest;
import asf.dungeon.model.token.quest.SignPostQuest;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/4/2014.
 */
public class PreBuiltFloorGen implements FloorMapGenerator {

        public FloorMap generate(Dungeon dungeon, FloorType floorType, int floorIndex){
                FloorMap floorMap;
                if(floorIndex == 0){
                        floorMap = puzzleRoom(dungeon, floorIndex);
                }else if(floorIndex == 1){
                        floorMap = openRoom(dungeon, floorIndex);
                }else
                if(floorIndex ==2 ){
                        floorMap = smallRoom(dungeon,floorIndex);
                }else if(floorIndex == 3){
                        floorMap = mediumRoom(dungeon,floorIndex);
                }else{
                        floorMap = tinyRoom(dungeon,floorIndex);
                }
                //UtFloorGen.spawnCharacters(dungeon, floorMap);
                UtFloorGen.spawnRandomCrates(dungeon, floorMap);
                return floorMap;
        }

        public static FloorMap puzzleRoom(Dungeon dungeon, int floorIndex){

                String[] tileData = new String[]{
                        "-------------------",
                        "|.................|",
                        "|.^...............|",
                        "|.................|",
                        "|.....---+---.....|",
                        "|.....|.....|.....|",
                        "|.....|.....|.....|",
                        "|.....+.k...|.....|",
                        "|.....|.....|.....|",
                        "|.....|--/--|.....|",
                        "|.................|",
                        "|...............&.|",
                        "|.................|",
                        "|-----------------|"

                };

                FloorMap floorMap = new FloorMap(null, floorIndex, convertTileData(dungeon, floorIndex, tileData));
                spawnTokensFromTileData(dungeon, floorMap, tileData, null);

                CombinationDoorPuzzle puzzle = new CombinationDoorPuzzle();

                Token plateToken = new Token(dungeon, "Pressure Plate", null);
                plateToken.add(new PressurePlate(plateToken, puzzle));
                dungeon.newToken(plateToken, floorMap, 10, 10);

                puzzle.addPiece(plateToken, true);
                puzzle.lockDoor(dungeon, floorMap, floorMap.getTile(9, 9));


                Token boulderToken = new Token(dungeon, "Boulder", ModelId.Boulder);
                boulderToken.add(new Boulder(boulderToken));
                dungeon.newToken(boulderToken, floorMap, 5, 11);

                return floorMap;
        }

        public static FloorMap mediumRoom(Dungeon dungeon, int floorIndex){

                String[] tileData = new String[]{
                        "---------------------------------",
                        "|................|..............|",
                        "|...|||...|......||.&..|........|",
                        "|.........|||.....|.............|",
                        "|..||.......|.....||-||||.......|",
                        "|.||||..|||||......||.....|.....|",
                        "|||........||.............|||...|",
                        "|...........|||.........|||.....|",
                        "|..|.........|....|.............|",
                        "|..||........|....||............|",
                        "|...........|||.........|||.....|",
                        "|.....^......|..................|",
                        "---------------------------------"

                };

                FloorMap floorMap = new FloorMap(null, floorIndex, convertTileData(dungeon, floorIndex, tileData));
                spawnTokensFromTileData(dungeon, floorMap, tileData, null);
                return floorMap;
        }

        public static FloorMap openRoom(Dungeon dungeon, int floorIndex){

                String[] tileData = new String[]{
                        "-------------------",
                        "|.................|",
                        "|.^...............|",
                        "|.................|",
                        "|.....---+---.....|",
                        "|.....|.....|.....|",
                        "|.....|.....|.....|",
                        "|.....|.....|.....|",
                        "|.....|.....|.....|",
                        "|.....|-----|.....|",
                        "|.................|",
                        "|...............&.|",
                        "|.................|",
                        "|-----------------|"

                };

                FloorMap floorMap = new FloorMap(null, floorIndex, convertTileData(dungeon, floorIndex, tileData));
                spawnTokensFromTileData(dungeon, floorMap, tileData, null);
                return floorMap;
        }
        public static FloorMap smallRoom(Dungeon dungeon, int floorIndex){

                String[] tileData = new String[]{
                        "----------------",
                        "|..............|",
                        "|...|||...|&...|",
                        "|.........|||..|",
                        "|..||.......|..|",
                        "|..||..|....|..|",
                        "|.....|||......|",
                        "|.^....|...||..|",
                        "|..............|",
                        "|--------------|"

                };

                FloorMap floorMap = new FloorMap(null, floorIndex, convertTileData(dungeon, floorIndex, tileData));
                spawnTokensFromTileData(dungeon, floorMap, tileData, null);
                return floorMap;
        }

        public static FloorMap tinyRoom(Dungeon dungeon, int floorIndex){

                String[] tileData = new String[]{
                        "----------",
                        "|.......||",
                        "|.|.|.^|&|",
                        "|.|.|||..|",
                        "|.|...|..|",
                        "|.|......|",
                        "|.|..||..|",
                        "|........|",
                        "----------"

                };

                FloorMap floorMap = new FloorMap(null, floorIndex,convertTileData(dungeon, floorIndex, tileData));
                spawnTokensFromTileData(dungeon, floorMap, tileData, null);

                return floorMap;
        }

        protected static Tile[][] convertTileData(Dungeon dungeon, int floorIndex, String[] tileData){
                Tile[][] tiles;
                tiles = new Tile[tileData[0].length()][tileData.length];
                for (int y = 0; y < tileData.length; y++) {
                        for (int x = 0; x < tileData[0].length(); x++) {
                                char charAt = tileData[tileData.length - y - 1].charAt(x);

                                if (charAt == '-' || charAt == '|') { // Wall
                                        tiles[x][y] = Tile.makeWall();
                                } else if(charAt == ':'){ // Pit
                                        tiles[x][y] = Tile.makePit();
                                } else if(charAt == '+'){ // Door
                                        tiles[x][y] = Tile.makeDoor();
                                } else if(charAt == '/'){ // Locked Door
                                        tiles[x][y] = Tile.makeDoor(new KeyItem(dungeon, KeyItem.Type.Silver));
                                }  else if(charAt != ' '){ // Floor
                                        tiles[x][y] = Tile.makeFloor();
                                }
                        }
                }
                return tiles;
        }

        protected static String[] randomizeTileData(Dungeon dungeon, FloorMap floorMap, String[] tileData, String[] roomMask){
                String[] td = new String[tileData.length];
                for(int i=0; i < td.length; i++) td[i] = tileData[i];
                final Array<Pair> pairs = new Array<Pair>(false, 16, Pair.class);
                for (int y = 0; y < tileData.length; y++) {
                        for (int x = 0; x < tileData[0].length(); x++) {
                                char charAt = tileData[tileData.length - y - 1].charAt(x);
                                // check to see if this is a valid token to randomize (eg crates)
                                if(charAt != 'c' && charAt != 'C' && charAt != 'h'&& charAt != 'k')
                                        continue;
                                // replace this token with a floor tile
                                td[tileData.length-y-1] = td[td.length-y-1].substring(0, x)+'.'+td[td.length - y - 1].substring(x+1);

                                // choose new random location from the roomMask to place this token at
                                // this is done by finding all locations with the same room mask,
                                // then randomly picking one of them
                                char room = roomMask[tileData.length-y-1].charAt(x);
                                pairs.clear();
                                for (int ry = 0; ry < roomMask.length; ry++)
                                        for (int rx = 0; rx < roomMask[0].length(); rx++)
                                                if(roomMask[roomMask.length - ry - 1].charAt(rx) == room)
                                                        pairs.add(new Pair(rx,ry));
                                Pair newLoc = pairs.items[dungeon.rand.random.nextInt(pairs.size)];

                                // new location now has this token instead of whatever it was
                                td[tileData.length-newLoc.y-1] = td[td.length-newLoc.y-1].substring(0, newLoc.x)+charAt+td[td.length - newLoc.y - 1].substring(newLoc.x+1);
                        }
                }
                return td;
        }

        protected static void spawnTokensFromTileData(Dungeon dungeon, FloorMap floorMap, String[] tileData, String[] signPostMessages){
                int nextSignPostMessage = 0;
                for (int y = 0; y < tileData.length; y++) {
                        for (int x = 0; x < tileData[0].length(); x++) {
                                char charAt = tileData[tileData.length - y - 1].charAt(x);

                                if(charAt == '^'){ // Stairs Up
                                        Token stairsToken = new Token(dungeon, "Stairs", null);
                                        stairsToken.add(new Stairs(stairsToken, floorMap.index - 1));
                                        stairsToken.direction = Direction.East;
                                        dungeon.newToken(stairsToken, floorMap, x,y);
                                } else if(charAt == '&'){ // Stairs Down
                                        Token stairsToken = new Token(dungeon, "Stairs", null);
                                        stairsToken.add(new Stairs(stairsToken, floorMap.index+1));
                                        stairsToken.direction = Direction.East;
                                        dungeon.newToken(stairsToken, floorMap, x,y);
                                } else if(charAt == 'c') { // crate (empty)
                                        dungeon.newCrateToken(floorMap, "Wooden Barrel", ModelId.Barrel, null, x, y);
                                } else if(charAt == 'C') { // crate (empty)
                                        dungeon.newCrateToken(floorMap, "Wooden Crate", ModelId.Crate, null, x, y);
                                } else if(charAt == 'h') { // health potion
                                        dungeon.newCrateToken(floorMap, "Wooden Barrel", ModelId.Barrel, new PotionItem(dungeon, PotionItem.Type.Health, 1), x, y);
                                } else if(charAt == 's') { // Spike Trap
                                        Token spikeTrap = new Token(dungeon, "Spike Trap", ModelId.SpikeTrap);
                                        spikeTrap.add(new SpikeTrap(spikeTrap));
                                        dungeon.newToken(spikeTrap, floorMap, x, y);
                                } else if(charAt == 'b') { // Boulder
                                        Token boulderToken = new Token(dungeon, "Boulder", ModelId.Boulder);
                                        boulderToken.add(new Boulder(boulderToken));
                                        dungeon.newToken(boulderToken, floorMap, x, y);
                                } else if(charAt == 't') { // Traveler
                                        Token traveler = new Token(dungeon, "Traveler", ModelId.Priest);
                                        dungeon.newQuestCharacterToken(traveler, new FsmLogic(2, null, QuestNPC.PauseThenMove), new PotionQuest(), floorMap, x, y);
                                } else if(charAt == 'f') { // fountain
                                        Token fountainToken = new Token(dungeon, "Fountain", ModelId.Fountain);
                                        fountainToken.add(new Fountain(fountainToken, dungeon.rand.potionType()));
                                        dungeon.newToken(fountainToken, floorMap, x, y);
                                } else if(charAt == 'p') { // sign post
                                        Token signPost = new Token(dungeon, "Sign Post", ModelId.SignPost);
                                        signPost.add(new SignPostQuest(signPostMessages[nextSignPostMessage++]));
                                        dungeon.newToken(signPost, floorMap, x,y);
                                } else if(charAt == 'k') { // key
                                        KeyItem keyItem = new KeyItem(dungeon, KeyItem.Type.Silver);
                                        if(dungeon.rand.random.nextBoolean()){
                                                dungeon.newCrateToken(floorMap, ModelId.Barrel.name(), ModelId.Barrel, keyItem, x, y);
                                        }else{
                                                dungeon.newLootToken(floorMap, keyItem, x, y);
                                        }
                                } else if(charAt == 'm') { // monster
                                        ModelId modelId = dungeon.rand.random.nextBoolean() ? ModelId.Skeleton : ModelId.RockMonster;
                                        Token token = dungeon.newCharacterToken(floorMap, modelId.name(),
                                                modelId,
                                                new FsmLogic(1, null, Monster.Sleep),
                                                new Experience(1, 8, 4, 6, 1,1),
                                                x,y);
                                        if(modelId == ModelId.Archer){
                                                WeaponItem weapon = new WeaponItem(dungeon,  2,2,1, true,3,1);
                                                token.inventory.add(weapon);
                                                token.inventory.equip(weapon);
                                        }

                                } else if(charAt == 'M') { // monster trap
                                        ModelId modelId = ModelId.RockMonster;
                                        Token token = dungeon.newTrapCharacterToken(floorMap, "Rock Monster Trap",
                                                modelId,
                                                new FsmLogic(1, null, Monster.Sleep),
                                                new Experience(1, 8, 4, 1, 1,1),
                                                x,y);


                                }
                        }
                }
        }
}
