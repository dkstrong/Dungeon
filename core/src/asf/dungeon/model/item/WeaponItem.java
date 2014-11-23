package asf.dungeon.model.item;

import asf.dungeon.model.FxId;
import asf.dungeon.model.M;
import asf.dungeon.model.ModelId;

/**
 * Created by Danny on 11/18/2014.
 */
public class WeaponItem extends EquipmentItem{

        private int damage;
        private boolean ranged;
        private FxId projectileFx;

        public WeaponItem(ModelId modelId, String name, int damage) {
                super(modelId, name, "A weapon");
                this.damage = damage;
        }

        @Override
        public String getVagueName() {
                return M.Unidentified+" "+M.Weapon;
        }

        @Override
        public String getVagueDescription() {
                return M.UnidentifiedWeaponDesc;
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

        public FxId getProjectileFx() {
                return projectileFx;
        }

        public void setProjectileFx(FxId projectileFx) {
                this.projectileFx = projectileFx;
        }

        @Override
        public String toString() {
                return String.format("%s (%s)", getName(), damage);
        }

}
