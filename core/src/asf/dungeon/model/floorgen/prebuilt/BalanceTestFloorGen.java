package asf.dungeon.model.floorgen.prebuilt;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FloorType;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.TokenFactory;
import asf.dungeon.model.token.logic.fsm.FsmLogic;
import asf.dungeon.model.token.logic.fsm.Monster;

/**
 * Created by Danny on 11/4/2014.
 */
public class BalanceTestFloorGen implements FloorMapGenerator, FloorMap.MonsterSpawner{

        public FloorMap generate(Dungeon dungeon, FloorType floorType, int floorIndex){
                String[] tileData = new String[]{
                        "---------------",
                        "|.............|",
                        "|.^...........|",
                        "|.............|",
                        "|.............|",
                        "|.............|",
                        "|.............|",
                        "|.............|",
                        "|.............|",
                        "|-------------|"

                };

                FloorMap floorMap = new FloorMap(floorType, floorIndex, PreBuiltFloorGen.convertTileData(dungeon, floorIndex, tileData), this);
                PreBuiltFloorGen.spawnTokensFromTileData(dungeon, floorMap, tileData, null);
                //UtFloorGen.spawnCharacters(dungeon, floorMap);
                //UtFloorGen.spawnRandomCrates(dungeon, floorMap);
                return floorMap;
        }


        @Override
        public void spawnMonsters(Dungeon dungeon, FloorMap floorMap) {
                int countTeam1 = floorMap.getTokensOnTeam(1).size;
                if(countTeam1 == 0){
                        int x, y;
                        ModelId modelId = dungeon.rand.random.nextBoolean() ? ModelId.Skeleton : ModelId.RockMonster;
                        Token t = TokenFactory.characterToken(dungeon, modelId, new FsmLogic(1, null, Monster.Explore), floorMap);
                        do{
                                x = dungeon.rand.random.nextInt(floorMap.getWidth());
                                y = dungeon.rand.random.nextInt(floorMap.getHeight());
                        }while(!t.canSpawn(floorMap,x,y,t.direction));

                        dungeon.addToken(t, floorMap, x, y);

                }
        }
}
