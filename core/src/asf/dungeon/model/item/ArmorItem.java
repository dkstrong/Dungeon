package asf.dungeon.model.item;

import asf.dungeon.model.M;
import asf.dungeon.model.ModelId;

/**
 * Created by Danny on 11/18/2014.
 */
public class ArmorItem extends EquipmentItem{

        private int armor;

        public ArmorItem(ModelId modelId, String name, int armor) {
                super(modelId, name, "Some armor");
                this.armor = armor;
        }
        @Override
        public String getVagueName() {
                return M.Unidentified+" "+M.Armor;
        }

        @Override
        public String getVagueDescription() {
                return M.UnidentifiedArmorDesc;
        }

        public int getArmorRating() {
                return armor;
        }

        @Override
        public String toString() {
                return String.format("%s (%s)", getName(), armor);
        }

}
