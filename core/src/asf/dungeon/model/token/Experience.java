package asf.dungeon.model.token;

import asf.dungeon.model.item.ArmorItem;
import asf.dungeon.model.item.RingItem;
import asf.dungeon.model.item.WeaponItem;
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
        public boolean update(float delta) {
                return false;
        }

        public void addXp(int amount){
                xp += amount;
                if(xp >= getRequiredXpToLevelUp()){
                        level +=1;
                        vitality+= 1;
                        strength +=1;
                        agility+=1;
                        intelligence+=1;
                        recalcStats();
                }
        }

        protected void addXpFrom(Experience otherExperience){
                if(otherExperience == null)
                        return; // killed something with no experience, so no experience to gain
                addXp(1); // TODO: come up with a good XP amount based on level/xp differende

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



        protected void recalcStats(){
                // equipment
                WeaponItem weapon = token.inventory.getWeaponSlot();
                ArmorItem armor = token.inventory.getArmorSlot();
                RingItem ring = token.inventory.getRingSlot();

                token.attack.setWeapon(weapon);


                // status effects
                // TODO: here is also where i need to look at StatusEffects and apply the effects of any status effect


                // experience
                vitalityMod = (weapon == null ? 0 : weapon.vitalityMod) + (armor == null ? 0 : armor.vitalityMod)+ (ring == null ? 0 : ring.vitalityMod);
                strengthMod = (weapon == null ? 0 : weapon.strengthMod) + (armor == null ? 0 : armor.strengthMod)+ (ring == null ? 0 : ring.strengthMod);
                agilityMod = (weapon == null ? 0 : weapon.agilityMod) + (armor == null ? 0 : armor.agilityMod)+ (ring == null ? 0 : ring.agilityMod);
                intelligenceMod = (weapon == null ? 0 : weapon.intelligenceMod) + (armor == null ? 0 : armor.intelligenceMod)+ (ring == null ? 0 : ring.intelligenceMod);
                luckMod = (weapon == null ? 0 : weapon.luckMod) + (armor == null ? 0 : armor.luckMod)+ (ring == null ? 0 : ring.luckMod);

                int vitality = getVitality();
                int strength = getStrength();
                int agility = getAgility();
                int intelligence = getIntelligence();
                int luck = getLuck();


                // vitality
                token.damage.setMaxHealth(vitality);

                // strength

                // agility

                if(agility < 2) token.move.setMoveSpeed(1.55f);
                if(agility < 3) token.move.setMoveSpeed(1.75f);
                if(agility < 5) token.move.setMoveSpeed(1.9f);
                if(agility < 10) token.move.setMoveSpeed(2.35f);
                else if(agility < 15) token.move.setMoveSpeed(2.8f);
                else if(agility < 20) token.move.setMoveSpeed(3.25f);
                else token.move.setMoveSpeed(UtMath.scalarLimitsInterpolation(agility, 20f, 100f, 3.45f, 4f));

                if(token.statusEffects != null){
                        if(token.statusEffects.has(StatusEffect.Speed)){
                                token.move.setMoveSpeed(token.move.getMoveSpeed() * 1.35f);
                        }
                }


                // intelligence

                if(token.statusEffects != null && token.statusEffects.has(StatusEffect.Blind)){
                        token.damage.setSightRadius(1);
                }else{
                        token.damage.setSightRadius(Math.round(UtMath.scalarLimitsInterpolation(intelligence, 1f, 40f, 4f, 10f))); // default is 6
                }

                //Gdx.app.log(token.getName(),"int: "+intelligence+" sight: "+token.damage.getSightRadius()+"");

                // luck


                if(token.fogMapping!= null && token.floorMap != null){
                        token.fogMapping.computeFogMap();
                }


        }

}
