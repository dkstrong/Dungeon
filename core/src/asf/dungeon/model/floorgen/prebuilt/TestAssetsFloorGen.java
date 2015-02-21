package asf.dungeon.model.floorgen.prebuilt;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FloorType;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Sector;
import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.item.BookItem;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.ScrollItem;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.Fountain;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.TokenFactory;
import asf.dungeon.model.token.logic.fsm.FsmLogic;
import asf.dungeon.model.token.logic.fsm.Monster;
import asf.dungeon.model.token.logic.fsm.QuestNPC;
import asf.dungeon.model.token.quest.PotionQuest;
import asf.dungeon.model.token.quest.SignPostQuest;

/**
 * Created by Danny on 11/4/2014.
 */
public class TestAssetsFloorGen implements FloorMapGenerator, FloorMap.MonsterSpawner {


        public FloorMap generate(Dungeon dungeon, FloorType floorType, int floorIndex) {

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


                FloorMap floorMap = new FloorMap(floorType, floorIndex, PreBuiltFloorGen.convertTileData(dungeon, floorIndex, tileData), this);
                PreBuiltFloorGen.spawnTokensFromTileData(dungeon, floorMap, tileData, null);

                PotionItem.Type[] potionValues = PotionItem.Type.values();
                for (int y = 1; y <= potionValues.length; y++) {
                        dungeon.newToken(TokenFactory.loot(dungeon, new PotionItem(dungeon, potionValues[y - 1], 24)), floorMap, 1, y);
                }

                ScrollItem.Type[] scrollValues = ScrollItem.Type.values();
                for (int y = 1; y <= scrollValues.length; y++) {
                        dungeon.newToken(TokenFactory.loot(dungeon, new ScrollItem(dungeon, scrollValues[y - 1], 24)), floorMap, 3, y);
                }

                BookItem.Type[] bookValues = BookItem.Type.values();
                for (int y = 1; y <= bookValues.length; y++) {
                        dungeon.newToken(TokenFactory.loot(dungeon, new BookItem(dungeon, bookValues[y - 1])), floorMap, 5, y);
                }

                for (int x = 4; x < bookValues.length + 4; x++) {
                        Token t = TokenFactory.crate(dungeon,dungeon.rand.random.nextBoolean() ? ModelId.Barrel : ModelId.Chest, new BookItem(dungeon, bookValues[x - 4]));
                        dungeon.newToken(t, floorMap, x, tileData.length - 2);
                }

                for (int y = 0; y < potionValues.length; y++) {
                        Token fountainToken = new Token(dungeon, "Fountain", ModelId.Fountain);
                        fountainToken.add(new Fountain(fountainToken, potionValues[y]));
                        dungeon.newToken(fountainToken, floorMap, tileData[0].length() - 2, tileData.length - y - 2);
                }

                Token t = TokenFactory.questCharacterToken(dungeon, "Traveler", ModelId.Priest, new FsmLogic(2, new Sector(7, 1, 15, 4), QuestNPC.PauseThenMove), new PotionQuest());
                dungeon.newToken(t, floorMap, 10, 2);

                Token signPost = new Token(dungeon, "Sign Post", ModelId.SignPost);
                signPost.add(new SignPostQuest("Hello I am a Sign Post."));
                dungeon.newToken(signPost, floorMap, 4, tileData.length - 3);

                return floorMap;
        }


        @Override
        public void spawnMonsters(Dungeon dungeon, FloorMap floorMap) {
                int countTeam1 = floorMap.getTokensOnTeam(1).size;
                if (countTeam1 == 0) {
                        int x, y;
                        do {
                                x = dungeon.rand.range(7, 12);
                                y = dungeon.rand.range(6, 10);
                        } while (floorMap.getTile(x, y) == null || !floorMap.getTile(x, y).isFloor() || floorMap.hasTokensAt(x, y));


                        ModelId modelId = dungeon.rand.random.nextBoolean() ? ModelId.RockMonster : ModelId.RockMonster;

                        //new FullAgroLogic(1)
                        Token t = TokenFactory.characterToken(dungeon, "Monster", modelId, new FsmLogic(1, new Sector(7, 6, 12, 10), Monster.Sleep),new Experience(1,8,4,6,1,1) );
                        dungeon.newToken(t, floorMap,x,y);

                }
        }
}
