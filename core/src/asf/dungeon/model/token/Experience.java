package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.item.EquipmentItem;
import asf.dungeon.utility.UtMath;

/**
 * Created by Danny on 11/11/2014.
 */
public class Experience implements TokenComponent{
        private Token token;
        private int xp;
        private int level;
        private int vitality,vitalityMod; // number of max hit points
        private int strength,strengthMod; // determines damage sent/received by standard attacks
        private int agility,agilityMod; // determins how fast token moves, chance of dodging
        private int intelligence, intelligenceMod; // effectiveness of scrolls, how long it takes to idenfify item throug using it
        private int luck, luckMod; // chance of doing critical hit, chance of better items spawning


        public Experience(int level, int vitality, int strength, int agility, int intelligence, int luck) {
                this.level = level;
                this.vitality = vitality;
                this.strength = strength;
                this.agility = agility;
                this.intelligence = intelligence;
                this.luck = luck;
        }

        public void setToken(Token token) {
                this.token = token;
                recalcStats();
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {

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

        public int getIntelligence() { return intelligence+intelligenceMod; }

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
        public int getIntelligenceBase() { return intelligence;}
        public int getLuckBase() {
                return luck;
        }

        public int getVitalityMod() { return vitalityMod; }
        public int getStrengthMod() { return strengthMod; }
        public int getAgilityMod() { return agilityMod; }
        public int getIntelligenceMod() { return intelligenceMod; }
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
                        intelligence+=1;
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
                intelligenceMod = (weapon == null ? 0 : weapon.getIntelligenceMod()) + (armor == null ? 0 : armor.getIntelligenceMod())+ (ring == null ? 0 : ring.getIntelligenceMod());
                luckMod = (weapon == null ? 0 : weapon.getLuckMod()) + (armor == null ? 0 : armor.getLuckMod())+ (ring == null ? 0 : ring.getLuckMod());

                // TODO: here is also where i need to look at StatusEffects and apply the effects of any status effect

                int vitality = getVitality();
                int strength = getStrength();
                int agility = getAgility();
                int intelligence = getIntelligence();
                int luck = getLuck();

                // vitality
                token.getDamage().setMaxHealth(vitality);

                // strength
                token.getAttack().setAttackRange(Math.round(UtMath.scalarLimitsInterpolation(strength,1f,100f,2f,6f))); // default was 3

                // agility
                if(agility < 10) token.getMove().setMoveSpeed(1.5f);
                else if(agility < 15) token.getMove().setMoveSpeed(1.6f);
                else if(agility < 20) token.getMove().setMoveSpeed(1.7f);
                else token.getMove().setMoveSpeed(UtMath.scalarLimitsInterpolation(agility,20f,100f,1.7f,3f));

                // TODO: setting the attack duration here might somehow be related to the going idle while attacking, should check to see if somehow
                // stats are geting recalced in the middle of comabt
                token.getAttack().setAttackDuration(UtMath.scalarLimitsInterpolation(agility,1f,100f,2.25f,1.5f)); // default was 2
                token.getAttack().setAttackCooldownDuration(UtMath.scalarLimitsInterpolation(agility,1f,100f,1.5f,.75f)); // default was 1
                token.getAttack().setProjectileSpeed(UtMath.scalarLimitsInterpolation(agility,1f,100f,1.5f,5f)); // default was 2

                // intelligence

                token.getDamage().setSightRadius(Math.round(UtMath.scalarLimitsInterpolation(intelligence, 1f, 40f, 4f, 10f))); // default is 6
                //Gdx.app.log(token.getName(),"int: "+intelligence+" sight: "+token.getDamage().getSightRadius()+"");

                // luck
        }

}
