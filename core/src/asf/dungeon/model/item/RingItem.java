package asf.dungeon.model.item;

import asf.dungeon.model.Dungeon;

/**
 * Created by Danny on 11/18/2014.
 */
public class RingItem extends EquipmentItem{

        public RingItem(Dungeon dungeon) {
                dungeon.m.generateNameDesc(this);
        }

        @Override
        public String toString() {
                return getAbbrName();
        }

}
