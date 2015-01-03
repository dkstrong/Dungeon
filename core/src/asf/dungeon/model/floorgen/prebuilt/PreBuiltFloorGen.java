package asf.dungeon.model.floorgen.prebuilt;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FxId;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.floorgen.UtFloorGen;
import asf.dungeon.model.item.KeyItem;
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

/**
 * Created by Danny on 11/4/2014.
 */
public class PreBuiltFloorGen implements FloorMapGenerator {

        public FloorMap generate(Dungeon dungeon, int floorIndex){
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
                UtFloorGen.spawnCharacters(dungeon, floorMap);
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
                        "|.....+.....|.....|",
                        "|.....|.....|.....|",
                        "|.....|-----|.....|",
                        "|.................|",
                        "|...............&.|",
                        "|.................|",
                        "|-----------------|"

                };

                FloorMap floorMap = new FloorMap(floorIndex, convertTileData(floorIndex,tileData));
                spawnTokensFromTileData(dungeon, floorMap, tileData);

                CombinationDoorPuzzle puzzle = new CombinationDoorPuzzle();

                Token plateToken = new Token(dungeon, "Pressure Plate", null);
                plateToken.add(new PressurePlate(plateToken, puzzle));
                dungeon.newToken(plateToken, floorMap, 10, 10);

                puzzle.addPiece(plateToken, true);
                puzzle.lockDoor(dungeon, floorMap, floorMap.getTile(9, 9));


                Token boulderToken = new Token(dungeon, "Boulder", null);
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

                FloorMap floorMap = new FloorMap(floorIndex, convertTileData(floorIndex,tileData));
                spawnTokensFromTileData(dungeon, floorMap, tileData);
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

                FloorMap floorMap = new FloorMap(floorIndex, convertTileData(floorIndex,tileData));
                spawnTokensFromTileData(dungeon, floorMap, tileData);
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

                FloorMap floorMap = new FloorMap(floorIndex, convertTileData(floorIndex,tileData));
                spawnTokensFromTileData(dungeon, floorMap, tileData);
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

                FloorMap floorMap = new FloorMap(floorIndex,convertTileData(floorIndex,tileData));
                spawnTokensFromTileData(dungeon, floorMap, tileData);

                return floorMap;
        }

        protected static Tile[][] convertTileData(int floorIndex, String[] tileData){
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
                                        tiles[x][y] = Tile.makeDoor(new KeyItem(KeyItem.Type.Silver));
                                }  else if(charAt != ' '){ // Floor
                                        tiles[x][y] = Tile.makeFloor();
                                }
                        }
                }
                return tiles;
        }

        protected static void spawnTokensFromTileData(Dungeon dungeon, FloorMap floorMap, String[] tileData){
                for (int y = 0; y < tileData.length; y++) {
                        for (int x = 0; x < tileData[0].length(); x++) {
                                char charAt = tileData[tileData.length - y - 1].charAt(x);

                                if(charAt == '^'){ // Stairs Up
                                        Token stairsToken = new Token(dungeon, "Stairs", null);
                                        stairsToken.add(new Stairs(stairsToken, floorMap.index - 1));
                                        stairsToken.setDirection(Direction.East);
                                        dungeon.newToken(stairsToken, floorMap, x,y);
                                } else if(charAt == '&'){ // Stairs Down
                                        Token stairsToken = new Token(dungeon, "Stairs", null);
                                        stairsToken.add(new Stairs(stairsToken, floorMap.index+1));
                                        stairsToken.setDirection(Direction.East);
                                        dungeon.newToken(stairsToken, floorMap, x,y);
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
                                } else if(charAt == 'k') { // key
                                        KeyItem keyItem = new KeyItem(KeyItem.Type.Silver);
                                        if(dungeon.rand.random.nextBoolean()){
                                                dungeon.newCrateToken(floorMap, ModelId.Barrel.name(), ModelId.Barrel, keyItem, x, y);
                                        }else{
                                                dungeon.newLootToken(floorMap, keyItem, x, y);
                                        }
                                } else if(charAt == 'm') { // monster
                                        ModelId modelId = dungeon.rand.random.nextBoolean() ? ModelId.Skeleton : ModelId.Berzerker;
                                        Token token = dungeon.newCharacterToken(floorMap, modelId.name(),
                                                modelId,
                                                new FsmLogic(1, null, Monster.Sleep),
                                                new Experience(1, 8, 4, 6, 1,1),
                                                x,y);
                                        if(modelId == ModelId.Archer){
                                                WeaponItem weapon = new WeaponItem(ModelId.Sword,"Bow", 1, FxId.Arrow);
                                                token.getInventory().add(weapon);
                                                token.getInventory().equip(weapon);
                                        }

                                }
                        }
                }
        }
}
