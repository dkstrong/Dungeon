package asf.dungeon.model.item;

import asf.dungeon.model.M;

/**
 * Created by Danny on 11/18/2014.
 */
public class ArmorItem extends EquipmentItem{

        private int armor;

        public ArmorItem(int armor) {
                this.armor = armor;
                M.generateNameDesc(this);
        }

        public int getArmorRating() {
                return armor;
        }

        @Override
        public String toString() {
                return String.format("%s (%s)", getName(), armor);
        }

}
