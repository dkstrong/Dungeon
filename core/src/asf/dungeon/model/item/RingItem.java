package asf.dungeon.model.item;

import asf.dungeon.model.M;
import asf.dungeon.model.ModelId;

/**
 * Created by Danny on 11/18/2014.
 */
public class RingItem extends EquipmentItem{

        public RingItem(ModelId modelId, String name, String desc) {
                M.generateNameDesc(this);
        }

        @Override
        public String toString() {
                return getName();
        }

}
