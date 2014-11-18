package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.item.EquipmentItem;
import asf.dungeon.model.item.PotionItem;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/5/2014.
 */
public class Journal implements TokenComponent{

        private final Array<PotionItem.Type> potions = new Array<PotionItem.Type>(false,16, PotionItem.Type.class);
        private final Array<EquipmentItem> equipment = new Array<EquipmentItem>(false,16, EquipmentItem.class);

        public Journal() {
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

        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                return true;
        }

        @Override
        public boolean update(float delta) {
                return false;
        }
}
