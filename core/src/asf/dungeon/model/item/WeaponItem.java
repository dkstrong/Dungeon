package asf.dungeon.model.item;

import asf.dungeon.model.FxId;
import asf.dungeon.model.M;
import asf.dungeon.model.ModelId;

/**
 * Created by Danny on 11/18/2014.
 */
public class WeaponItem extends EquipmentItem{

        public static final WeaponItem NULL = new WeaponItem(null, "Unarmed" ,0,2,1);

        private int damage;
        private float attackDuration =2;
        private float attackCooldown =1;
        private FxId projectileFx;
        private float range = 3;
        private float projectileSpeed =2;

        public WeaponItem(ModelId modelId, String name, int damage) {
                super(modelId, name, "A weapon");
                this.damage = damage;
                range = 0;
        }

        public WeaponItem(ModelId modelId, String name, int damage, float attackDuration, float attackCooldown) {
                super(modelId, name, "A weapon");
                this.damage = damage;
                this.attackDuration = attackDuration;
                this.attackCooldown = attackCooldown;
                range = 0;
        }

        public WeaponItem(ModelId modelId, String name, int damage, FxId projectileFx){
                super(modelId, name, "A ranged weapon");
                this.damage = damage;
                this.projectileFx = projectileFx;
        }

        public WeaponItem(ModelId modelId, String name, int damage, float attackDuration, float attackCooldown, FxId projectileFx, float range, float projectileSpeed){
                super(modelId, name, "A ranged weapon");
                this.damage = damage;
                this.attackDuration = attackDuration;
                this.attackCooldown = attackCooldown;
                this.projectileFx = projectileFx;
                this.range = range;
                this.projectileSpeed = projectileSpeed;
        }

        public void setStats(float attackDuration, float attackCooldown){
                this.attackDuration = attackDuration;
                this.attackCooldown = attackCooldown;
        }
        public void setRangedStats(float range, float projectileSpeed){
                this.range = range;
                this.projectileSpeed = projectileSpeed;
        }

        @Override
        public String getVagueName() {
                return M.Unidentified+" "+M.Weapon;
        }

        @Override
        public String getVagueDescription() {
                return M.UnidentifiedWeaponDesc;
        }

        public int getDamage() {
                return damage;
        }

        public void setDamage(int damage) {
                this.damage = damage;
        }

        public boolean isRanged() {
                return range > 0;
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

        public float getRange() {
                return range;
        }

        /**
         * the maximum range for this weapon. only applies to ranged weapons
         *
         * use 0 to specify a non ranged weapon
         * range is typically a value greater than 1f
         * @param range
         */
        public void setRange(float range) {
                this.range = range;
        }

        public float getAttackDuration() {
                return attackDuration;
        }

        /**
         * how long it takes for the attack animaiton to happen
         * also affects how long the being hit animation lasts for target.
         *
         * TODO: i may want the "being hit" duration to be its own stat.
         *
         *
         * TODO: changing this value in the middle of an attack animaiton might cause
         * the player being able to move before animation is over and other weirdnesses.
         * will need to expirement. shouldnt be an issue if i dont allow changing weapons during combat
         *
         * @param attackDuration
         */
        public void setAttackDuration(float attackDuration) {
                this.attackDuration = attackDuration;
        }

        public float getAttackCooldown() {
                return attackCooldown;
        }

        /**
         * the time from finishing one attack animation until the next one can begin
         *
         * for ranged attacks the cooldown timer starts after the projectile has hit something
         *
         * @param attackCooldown
         */
        public void setAttackCooldown(float attackCooldown) {
                this.attackCooldown = attackCooldown;
        }

        public float getProjectileSpeed() {
                return projectileSpeed;
        }

        /**
         * how quickly the projectile reaches its target. only applies to ranged weapons
         * @param projectileSpeed
         */
        public void setProjectileSpeed(float projectileSpeed) {
                this.projectileSpeed = projectileSpeed;
        }
}
