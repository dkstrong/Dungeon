package asf.dungeon.model.item;

import asf.dungeon.model.ModelId;

/**
 * Created by Danny on 11/18/2014.
 */
public class WeaponItem extends EquipmentItem{

        private int damage;
        private boolean ranged;

        public WeaponItem(ModelId modelId, String name, int damage) {
                super(modelId, name, "A weapon", "Unidentified Weapon", "A mysterious weapon, who knows what it will do once equipped?");
                this.damage = damage;
        }

        public void setRanged(boolean ranged) {
                this.ranged = ranged;
        }

        public int getDamage() {
                return damage;
        }

        public void setDamage(int damage) {
                this.damage = damage;
        }

        public boolean isRanged() {
                return ranged;
        }

        @Override
        public String toString() {
                return String.format("%s (%s)", getName(), damage);
        }

}
