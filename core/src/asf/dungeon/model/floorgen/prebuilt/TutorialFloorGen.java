package asf.dungeon.model.floorgen.prebuilt;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FxId;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.fsm.FsmLogic;
import asf.dungeon.model.token.logic.fsm.Monster;

/**
 * Created by Danny on 11/4/2014.
 */
public class TutorialFloorGen implements FloorMapGenerator, FloorMap.MonsterSpawner{


        public FloorMap generate(Dungeon dungeon, int floorIndex){

                String[] tileData = new String[]{
                        "  |||||||                             ",
                        "  |.....|                             ",
                        "  |..^..|   |||||||||         ||||||| ",
                        "  |.....|   |.......|         |.....| ",
                        "  |.b.::|||||.......|         |..t..| ",
                        "  |b..:...........::|||||||||||.....| ",
                        "  |...::|||||..m..:.+.........+.....| ",
                        "  |||||||   |.....::|||||.|||||.....| ",
                        "            |.......|   |.|   |.....| ",
                        "   |||||||||||||||||||| |.|   ||||||| ",
                        "   |..................| |.|    |||||||",
                        "   |.||||||||||||||||.| |s|    |.....|",
                        "   |.|    ||||||||  |.| |.||||||..f..|",
                        "   |.|    |......|  |.| |......+.....|",
                        "   |.|    |....m.|  |.| ||||||||.....|",
                        "   |.|    |..k...|  |.|        |.....|",
                        "   |.|    |......|  |/|||||||| ||..|||",
                        "||||+||||||......|  |........|  |..|  ",
                        "|........||||+||||  |||||.m..||||..|  ",
                        "|....s...|  |.|         |..s....+..|  ",
                        "|.m......|  |.||||||||| |.....||||.|  ",
                        "|..&.....|  |.|.......| ||||||| |..|  ",
                        "|.....m..|  |.|.......|         |.||  ",
                        "|........|  |.|.......|||||||||||.|   ",
                        "|........|  |.+..m....+...........|   ",
                        "||||||||||  |||.......|||||||||||||   ",
                        "              |.......|               ",
                        "              |||||||||               "

                };



                FloorMap floorMap = new FloorMap(floorIndex, PreBuiltFloorGen.convertTileData(floorIndex, tileData), this);
                PreBuiltFloorGen.spawnTokensFromTileData(dungeon, floorMap, tileData);


                return floorMap;
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
                                new FsmLogic(1, null, Monster.Sleep),
                                new Experience(1, 8, 4, 6, 1,1),
                                x,y);

                        if(modelId == ModelId.Archer){
                                WeaponItem weapon = new WeaponItem(ModelId.SwordLarge,"Bow", 1, FxId.Arrow);
                                token.getInventory().add(weapon);
                                token.getInventory().equip(weapon);
                        }

                }
        }
}
