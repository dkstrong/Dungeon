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
                        "           |||||||                             ",
                        " |||||||   |...CC|                             ",
                        "||.....||  |..^.C|   |||||||||         ||||||| ",
                        "|.......|  |...b.|   |C......|         |...kC| ",
                        "|.......|  |..M..|||||.....c.|         |.c..C| ",
                        "|.......|  |...k:/...+.....c.|||||||||||.....| ",
                        "||.....||  |..c..|||||.......+.........+.....| ",
                        " ||+||||   |||||||   |....c..|||||/|||||...m.| ",
                        "  |....|             |.......|   |.|   |....C| ",
                        "||||||+|||||||| |||||||||||||||| |.|   ||||||| ",
                        "|cc..........C| |..m...........| |.|    |||||||",
                        "|.............|||.||||||||||||.| |.|    |...m.|",
                        "|C............+...|||||||||  |.| |.||||||...c.|",
                        "|C.........CCc||||||.cc..C|  |.| |......+.....|",
                        "|||||+|||||||||    |....m.|  |.| ||||||||....c|",
                        "    |.|            |..k...|  |.|        |....C|",
                        "   ||.||           |......|  |/|||||||| ||..|||",
                        "   |...| |||||||||||....c.|  |........|  |..|  ",
                        "||||..c| |......cC||||+||||  |||||.m..||||m.|  ",
                        "|..+..c| |.......c|  |.|         |.......+..|  ",
                        "|.||||||||.m......|  |.||||||||| |cC...||||.|  ",
                        "|......../........|  |.|b.mchbC| ||||||| |..|  ",
                        "||||||||||........|  |.|...cbcc|         |.||  ",
                        "         |.m..&...|  |.|::.cc..|||||||||||.|   ",
                        "         |Cc....h.|  |.+::.....+...........|   ",
                        "         ||||||||||  |||::...b.|||||||||||||   ",
                        "                       |...m...|               ",
                        "                       |||||||||               "
                };

                String[] roomMask = new String[]{
                        "            |||||||                             ",
                        "            |1..11|                             ",
                        "            |1.^.1|   |||||||||         ||||||| ",
                        "            |1....|   |2222222|         |33333| ",
                        "            |111.p|||||.222222|         |33333| ",
                        "            |111......+.22222.|||||||||||.3333| ",
                        "            |11111|||||.22222.+.........+.3333| ",
                        "            |||||||   |222222.|||||/|||||.33m3| ",
                        "                      |2222222|   |.|   |33333| ",
                        "             |||||||||||||||||||| |.|   ||||||| ",
                        "             |......m...........| |.|    |||||||",
                        "             |.||||||||||||||||.| |.|    |444m4|",
                        "             |.|    ||||||||  |.| |.||||||.4444|",
                        "             |.|    |666666|  |.| |......+.4444|",
                        "             |.|    |6666m6|  |.| ||||||||.4444|",
                        "             |.|    |666666|  |.|        |4..44|",
                        "             |.|    |666666|  |/|||||||| ||..|||",
                        "          ||||+||||||6...66|  |.....555|  |..|  ",
                        "          |7.....77||||+||||  |||||5m55||||m.|  ",
                        "          |77...777|  |.|         |5555...+..|  ",
                        "          |7m7.7777|  |.||||||||| |55555||||.|  ",
                        "          |77777777|  |.|b.mchbC| ||||||| |..|  ",
                        "          |777...77|  |.|...cbcc|         |.||  ",
                        "          |7m7.&.77|  |.|::.cc..|||||||||||.|   ",
                        "          |777...77|  |.+::.....+...........|   ",
                        "          ||||||||||  |||::...b.|||||||||||||   ",
                        "                        |...m...|               ",
                        "                        |||||||||               "
                };

                String[] signPostMessages = {"This place is cursed"};
                FloorMap floorMap = new FloorMap(floorIndex, PreBuiltFloorGen.convertTileData(floorIndex, tileData), this);
                //tileData = PreBuiltFloorGen.randomizeTileData(dungeon, floorMap, tileData, roomMask);
                PreBuiltFloorGen.spawnTokensFromTileData(dungeon, floorMap, tileData,signPostMessages);


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
                                token.inventory.add(weapon);
                                token.inventory.equip(weapon);
                        }

                }
        }
}
