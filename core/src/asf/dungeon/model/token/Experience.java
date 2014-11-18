package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.item.EquipmentItem;

/**
 * Created by Danny on 11/11/2014.
 */
public class Experience implements TokenComponent{
        private Token token;
        private int xp;
        private int level;
        private int vitality,vitalityMod; // each point of vitality adds two hp, vitality also determines effeciency when using scrolls
        private int strength,strengthMod; // determines damage sent/received by melee and ranged attscks.
        private int agility,agilityMod; // determins how fast token moves, chance of dodging
        private int luck, luckMod; // chance of doing critical hit, chance of better items spawning


        public Experience(int level, int vitality, int strength, int agility, int luck) {
                this.level = level;
                this.vitality = vitality;
                this.strength = strength;
                this.agility = agility;
                this.luck = luck;
        }

        public void setToken(Token token) {
                this.token = token;
                recalcStats();
        }

        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                return true;
        }

        @Override
        public boolean update(float delta) {
                return false;
        }

        public int getXpAtStartOfLevel(){
                if(level <=1 ) return 0;
                return 10 + ((level - 2)*15);
        }

        public int getRequiredXpToLevelUp(){
                return 10 + ((level - 1)*15);
        }

        public int getXp() {
                return xp;
        }
        public int getLevel() {
                return level;
        }
        public int getVitality() {
                return vitality+vitalityMod;
        }
        public int getStrength() {
                return strength+strengthMod;
        }
        public int getAgility() {
                return agility+agilityMod;
        }
        public int getLuck() {
                return luck+luckMod;
        }

        public int getVitalityBase() {
                return vitality;
        }
        public int getStrengthBase() {
                return strength;
        }
        public int getAgilityBase() {
                return agility;
        }
        public int getLuckBase() {
                return luck;
        }

        public int getVitalityMod() { return vitalityMod; }
        public int getStrengthMod() { return strengthMod; }
        public int getAgilityMod() { return agilityMod; }
        public int getLuckMod() { return luckMod; }

        protected void addXpFrom(Experience otherExperience){
                if(otherExperience == null)
                        return; // killed something with no experience, so no experience to gain
                xp += 1;
                if(xp >= getRequiredXpToLevelUp()){
                        level +=1;
                        vitality+= 1;
                        strength +=1;
                        agility+=1;
                        recalcStats();
                }
        }

        protected void recalcStats(){
                EquipmentItem weapon = token.getInventory().getWeaponSlot();
                EquipmentItem armor = token.getInventory().getArmorSlot();
                EquipmentItem ring = token.getInventory().getRingSlot();

                vitalityMod = (weapon == null ? 0 : weapon.getVitalityMod()) + (armor == null ? 0 : armor.getVitalityMod())+ (ring == null ? 0 : ring.getVitalityMod());
                strengthMod = (weapon == null ? 0 : weapon.getStrengthMod()) + (armor == null ? 0 : armor.getStrengthMod())+ (ring == null ? 0 : ring.getStrengthMod());
                agilityMod = (weapon == null ? 0 : weapon.getAgilityMod()) + (armor == null ? 0 : armor.getAgilityMod())+ (ring == null ? 0 : ring.getAgilityMod());
                luckMod = (weapon == null ? 0 : weapon.getLuckMod()) + (armor == null ? 0 : armor.getLuckMod())+ (ring == null ? 0 : ring.getLuckMod());

                int vitality = getVitality();
                int strength = getStrength();
                int agility = getAgility();
                int luck = getLuck();

                // vitality
                token.getDamage().setMaxHealth(vitality*2);

                // strength
                token.getAttack().setAttackDuration(2); // should get slightly shorter with each level
                token.getAttack().setAttackRange(3); // should get slightly longer with each level

                // agility
                token.getMove().setMoveSpeed(agility+1);
                token.getAttack().setAttackCooldownDuration(agility / 6f);
                token.getAttack().setProjectileSpeed(2); // should get slighlty higher with each level
                // luck
        }

}
