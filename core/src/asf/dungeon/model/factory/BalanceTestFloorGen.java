package asf.dungeon.model.factory;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.FullAgroLogic;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Danny on 11/4/2014.
 */
public class BalanceTestFloorGen implements FloorMapGenerator, FloorMap.MonsterSpawner{

        public FloorMap generate(Dungeon dungeon, int floorIndex){
                String[] tileData = new String[]{
                        "-------------------",
                        "|.................|",
                        "|.^...............|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|-----------------|"

                };

                FloorMap floorMap = new FloorMap(floorIndex, PreBuiltFloorGen.convertTileData(floorIndex, tileData), this);
                //UtFloorGen.spawnCharacters(dungeon, floorMap);
                //UtFloorGen.spawnRandomCrates(dungeon, floorMap);
                return floorMap;
        }


        @Override
        public void spawnMonsters(Dungeon dungeon, FloorMap floorMap) {
                int countTeam1 = floorMap.getTokensOnTeam(1).size;
                if(countTeam1 == 0){
                        int x, y;
                        do{
                                x = MathUtils.random.nextInt(floorMap.getWidth());
                                y = MathUtils.random.nextInt(floorMap.getHeight());
                        }while(floorMap.getTile(x,y) == null || !floorMap.getTile(x,y).isFloor() || floorMap.hasTokensAt(x,y));
                        Token token = dungeon.newCharacterToken(floorMap, "Monster",
                                ModelId.Berzerker,
                                new FullAgroLogic(1),
                                new Experience(1, 4, 9, 6),
                                x,y);

                }
        }
}
