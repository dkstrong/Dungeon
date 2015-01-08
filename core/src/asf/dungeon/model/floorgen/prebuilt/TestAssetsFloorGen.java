package asf.dungeon.model.floorgen.prebuilt;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Sector;
import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.item.BookItem;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.ScrollItem;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.Fountain;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.fsm.FsmLogic;
import asf.dungeon.model.token.logic.fsm.Monster;
import asf.dungeon.model.token.logic.fsm.QuestNPC;
import asf.dungeon.model.token.quest.PotionQuest;
import asf.dungeon.model.token.quest.SignPostQuest;

/**
 * Created by Danny on 11/4/2014.
 */
public class TestAssetsFloorGen implements FloorMapGenerator, FloorMap.MonsterSpawner{


        public FloorMap generate(Dungeon dungeon, int floorIndex){

                String[] tileData = new String[]{
                        "-------------------",
                        "|.................|",
                        "|.^...............|",
                        "|.........|.......|",
                        "|.....---/----....|",
                        "|.....|......|....|",
                        "|.....|......|....|",
                        "|.....+....../....|",
                        "|.....|......|....|",
                        "|.....|.k..k.|....|",
                        "|.....|--+---|....|",
                        "|...............&.|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|-----------------|"

                };



                FloorMap floorMap = new FloorMap(floorIndex, PreBuiltFloorGen.convertTileData(floorIndex, tileData), this);
                PreBuiltFloorGen.spawnTokensFromTileData(dungeon, floorMap, tileData);

                PotionItem.Type[] potionValues = PotionItem.Type.values();
                for(int y = 1; y<=potionValues.length; y++){
                        dungeon.newLootToken(floorMap, new PotionItem(dungeon, potionValues[y-1], 24), 1,y);
                }

                ScrollItem.Type[] scrollValues = ScrollItem.Type.values();
                for(int y = 1; y<=scrollValues.length; y++){
                        dungeon.newLootToken(floorMap, new ScrollItem(dungeon, scrollValues[y-1], 24), 3,y);
                }

                BookItem.Type[] bookValues = BookItem.Type.values();
                for(int y = 1; y<=bookValues.length; y++){
                        dungeon.newLootToken(floorMap, new BookItem(dungeon, bookValues[y-1]), 5,y);
                }

                for(int x = 4; x < bookValues.length+4; x++){
                        dungeon.newCrateToken(floorMap, "Crate", dungeon.rand.random.nextBoolean() ? ModelId.Barrel : ModelId.Chest, new BookItem(dungeon, bookValues[x-4]), x,  tileData.length - 2);
                }

                for(int y = 0; y< potionValues.length; y++){
                        Token fountainToken = new Token(dungeon, "Fountain", ModelId.Fountain);
                        fountainToken.add(new Fountain(fountainToken, potionValues[y]));
                        dungeon.newToken(fountainToken, floorMap, tileData[0].length()-2, tileData.length - y-2);
                }

                Token traveler = new Token(dungeon, "Traveler", ModelId.Priest);
                dungeon.newQuestCharacterToken(traveler, new FsmLogic(2,new Sector(7,1,15,4), QuestNPC.PauseThenMove), new PotionQuest(), floorMap, 10, 2);

                Token signPost = new Token(dungeon, "Sign Post", ModelId.SignPost);
                signPost.add(new SignPostQuest("Hello I am a Sign Post."));
                dungeon.newToken(signPost, floorMap, 4,tileData.length-3);

                return floorMap;
        }


        @Override
        public void spawnMonsters(Dungeon dungeon, FloorMap floorMap) {
                int countTeam1 = floorMap.getTokensOnTeam(1).size;
                if(countTeam1 == 0){
                        int x, y;
                        do{
                                x = dungeon.rand.range(7,12);
                                y = dungeon.rand.range(6,10);
                        }while(floorMap.getTile(x,y) == null || !floorMap.getTile(x,y).isFloor() || floorMap.hasTokensAt(x,y));


                        ModelId modelId = dungeon.rand.random.nextBoolean() ? ModelId.RockMonster : ModelId.RockMonster;

                        Token token = dungeon.newCharacterToken(floorMap, "Monster",
                                modelId,
                                new FsmLogic(1, new Sector(7,6,12,10), Monster.Sleep),
                                //new FullAgroLogic(1),
                                new Experience(
                                        1,  // level
                                        8,  // vitality
                                        4,  // str
                                        6,  // agi
                                        1,  // int
                                        1), // luck
                                x,y);


                }
        }
}
