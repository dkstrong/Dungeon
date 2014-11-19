package asf.dungeon.model.item;

import asf.dungeon.model.ModelId;

/**
 * Created by Danny on 11/18/2014.
 */
public class ArmorItem extends EquipmentItem{

        private int armor;

        public ArmorItem(ModelId modelId, String name, int armor) {
                super(modelId, name, "Some armor", "Unidentified Armor", "Mysterious armor, who knows what it will do once equipped?");
                this.armor = armor;
        }

        public int getArmorRating() {
                return armor;
        }

        @Override
        public String toString() {
                return String.format("%s (%s)", getName(), armor);
        }

}
