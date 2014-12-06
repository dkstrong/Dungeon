package asf.dungeon.model.floorgen;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FxId;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.fsm.FSMLogic;
import asf.dungeon.model.token.logic.fsm.Monster;

/**
 * Created by Danny on 11/4/2014.
 */
public class BalanceTestFloorGen implements FloorMapGenerator, FloorMap.MonsterSpawner{

        public FloorMap generate(Dungeon dungeon, int floorIndex){
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
                                x = dungeon.rand.random.nextInt(floorMap.getWidth());
                                y = dungeon.rand.random.nextInt(floorMap.getHeight());
                        }while(floorMap.getTile(x,y) == null || !floorMap.getTile(x,y).isFloor() || floorMap.hasTokensAt(x,y));

                        ModelId modelId = ModelId.Archer;

                        Token token = dungeon.newCharacterToken(floorMap, "Monster",
                                modelId,
                                new FSMLogic(1, null, Monster.Sleep),
                                //new FullAgroLogic(1),
                                new Experience(
                                        1,  // level
                                        8,  // vitality
                                        4,  // str
                                        6,  // agi
                                        1,  // int
                                        1), // luck
                                x,y);

                        //EquipmentItem sword = EquipmentItem.makeWeapon("Sword", 1);
                        //token.getInventory().add(sword);
                        //token.getInventory().equip(sword);

                        if(modelId == ModelId.Archer){
                                WeaponItem weapon = new WeaponItem(ModelId.Sword,"Bow", 1, FxId.Arrow);
                                token.getInventory().add(weapon);
                                token.getInventory().equip(weapon);
                        }

                }
        }
}
