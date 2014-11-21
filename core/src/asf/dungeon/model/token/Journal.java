package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.item.EquipmentItem;
import asf.dungeon.model.item.PotionItem;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IdentityMap;

/**
 * Created by Danny on 11/5/2014.
 */
public class Journal implements TokenComponent{

        private final Token token;

        private final Array<PotionItem.Type> potions = new Array<PotionItem.Type>(false,16, PotionItem.Type.class);
        private final Array<EquipmentItem> equipment = new Array<EquipmentItem>(false,16, EquipmentItem.class);

        private final IdentityMap<EquipmentItem, Float> equipmentStudy = new IdentityMap<EquipmentItem, Float>(8);

        public Journal(Token token) {
                this.token = token;
        }

        public void learn(PotionItem.Type type){
                if(!knows(type))
                        potions.add(type);
        }

        public boolean knows(PotionItem.Type type){
                return potions.contains(type, true);
        }

        public void learn(EquipmentItem equipmentItem){
                if(!knows(equipmentItem))
                        this.equipment.add(equipmentItem);
        }

        public boolean knows(EquipmentItem equipmentItem){
                return this.equipment.contains(equipmentItem, true);
        }

        private void study(EquipmentItem item, float amount){
                if(item ==null || knows(item)){
                        return;
                }
                float currentStudy = equipmentStudy.get(item,0f);
                currentStudy+=amount;

                if(currentStudy >= item.getComplexity()){
                        learn(item);
                        equipmentStudy.remove(item);
                        if(token.listener != null)
                                token.listener.onLearnedThroughStudy(item);
                }else{
                        equipmentStudy.put(item, currentStudy);
                }
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {

        }

        @Override
        public boolean update(float delta) {
                float study = delta * token.getExperience().getIntelligence();
                study(token.getInventory().getWeaponSlot(), study);
                study(token.getInventory().getArmorSlot(), study);
                study(token.getInventory().getRingSlot(), study);
                return false;
        }
}
