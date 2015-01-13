package asf.dungeon.model.item;

import asf.dungeon.model.FxId;
import asf.dungeon.model.M;

/**
 * Created by Danny on 11/18/2014.
 */
public class WeaponItem extends EquipmentItem{

        /**
         * a "null" weapon used by Attack as a stand in for unarmed characters when calculating attacks
         */
        public static final transient WeaponItem NULL = new WeaponItem("Unarmed",0,2,1,false,0,0);

        /**
         * the base damage that this weapon does, used in Attack component when calculating output damage
         */
        public int damage;
        /**
         * how long it takes for the attack animation to happen
         * also affects how long the being hit animation lasts for target.
         *
         * TODO: i may want the "being hit" duration to be its own stat.
         *
         *
         * TODO: changing this value in the middle of an attack animaiton might cause
         * the player being able to move before animation is over and other weirdnesses.
         * will need to expirement. shouldnt be an issue if i dont allow changing weapons during combat
         *
         */
        public float attackDuration; // 2
        /**
         * the time from finishing one attack animation until the next one can begin
         *
         * for ranged attacks the cooldown timer starts after the projectile has hit something
         */
        public float attackCooldown; // 1
        /**
         * the projectile effect to be used when weapon is fired, only applies to ranged weapons
         */
        public FxId projectileFx;
        /**
         * if this weapon is a bow or a staff - only valid if weapon is ranged (range > 0)
         */
        public boolean bow;
        /**
         * the maximum range for this weapon. only applies to ranged weapons
         *
         * use 0 to specify a non ranged weapon
         * range is typically a value greater than 1f
         */
        public float range; // 3
        /**
         * how quickly the projectile reaches its target. only applies to ranged weapons
         */
        public float projectileSpeed; // 2

        public WeaponItem() {
        }

        public WeaponItem(int damage) {
                this.damage = damage;
                range = 0;
                M.generateNameDesc(this);
        }

        // sword
        public WeaponItem(int damage, float attackDuration, float attackCooldown) {
                this.damage = damage;
                this.attackDuration = attackDuration;
                this.attackCooldown = attackCooldown;
                range = 0;
                M.generateNameDesc(this);
        }

        // bow or staff
        public WeaponItem(int damage, float attackDuration, float attackCooldown, boolean bow, float range, float projectileSpeed){
                this.damage = damage;
                this.attackDuration = attackDuration;
                this.attackCooldown = attackCooldown;
                this.bow = bow;
                this.range = range;
                if(range <1) throw new IllegalArgumentException("range must be greater than 0");
                this.projectileSpeed = projectileSpeed;
                M.generateNameDesc(this);
        }

        public WeaponItem(String name, int damage, float attackDuration, float attackCooldown, boolean bow, float range, float projectileSpeed){
                this.name = name;
                description = name;
                vagueName = name;
                vagueDescription = description;
                this.damage = damage;
                this.attackDuration = attackDuration;
                this.attackCooldown = attackCooldown;
                this.bow = bow;
                this.range = range;
                if(range > 0) this.projectileFx = bow ? FxId.Arrow : FxId.PlasmaBall;
                this.projectileSpeed = projectileSpeed;
        }

        public boolean isRanged() {
                return range > 0;
        }

        @Override
        public String toString() {
                return String.format("%s (%s)", getName(), damage);
        }

}
