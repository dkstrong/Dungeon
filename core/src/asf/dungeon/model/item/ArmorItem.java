package asf.dungeon.model.item;

import asf.dungeon.model.Dungeon;

/**
 * Created by Danny on 11/18/2014.
 */
public class ArmorItem extends EquipmentItem{

        public int armor;

        public ArmorItem(Dungeon dungeon, int armor) {
                this.armor = armor;
                dungeon.m.generateNameDesc(this);
        }

        @Override
        public String toString() {
                return String.format("%s (%s)", getAbbrName(), armor);
        }

}
