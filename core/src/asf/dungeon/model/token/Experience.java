package asf.dungeon.model.token;

import asf.dungeon.model.Direction;

/**
 * Created by Danny on 11/11/2014.
 */
public class Experience implements TokenComponent{
        private Token token;
        private int xp;
        private int level = 1;
        private int vitality = 1; // each point of vitality adds two hp, vitality also determines effeciency when using scrolls
        private int strength = 1; // determines damage sent/received by melee and ranged attscks.
        private int agility = 1; // determins how fast token moves, chance of dodging

        public Experience(int level, int vitality, int strength, int agility) {
                this.level = level;
                this.vitality = vitality;
                this.strength = strength;
                this.agility = agility;
        }

        public void setToken(Token token) {
                this.token = token;
                this.setLevel(level);
                this.setVitality(vitality);
                this.setStrength(strength);
                this.setAgility(agility);
        }

        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                return true;
        }

        @Override
        public boolean update(float delta) {
                return false;
        }

        public void setStats(int level, int vitality, int strength, int agility){
                this.setLevel(level);
                this.setVitality(vitality);
                this.setStrength(strength);
                this.setAgility(agility);
        }

        public int getXpAtStartOfLevel(){
                if(level <=1 ) return 0;
                return 10 + ((level - 2)*15);
        }

        public int getRequiredXpToLevelUp(){
                return 10 + ((level - 1)*15);
        }

        public void addXpFrom(Experience otherExperience){
                if(otherExperience == null)
                        return; // killed something with no experience, so no experience to gain
                xp += 1;
                if(xp >= getRequiredXpToLevelUp()){
                        level +=1;
                        setVitality(vitality+1);
                        setStrength(strength+1);
                        setAgility(agility+1);
                }
        }

        public int getXp() {
                return xp;
        }

        public void setLevel(int level) {
                this.level = level;
                xp = getXpAtStartOfLevel();
        }

        public int getLevel() {
                return level;
        }

        public int getVitality() {
                return vitality;
        }

        public void setVitality(int vitality) {
                this.vitality = vitality;
                token.getDamage().setMaxHealth(vitality*2);
        }

        public void setStrength(int strength) {

                this.strength = strength;

                token.getAttack().setAttackDuration(2); // should get slightly shorter with each level
                token.getAttack().setAttackRange(3); // should get slightly longer with each level

        }

        public int getStrength() {
                return strength;
        }

        public void setAgility(int agility) {
                this.agility = agility;
                // TODO: changing experience ratings should effect derrived stats
                token.getMove().setMoveSpeed(agility+1);
                token.getAttack().setAttackCooldownDuration(agility / 6f);
                token.getAttack().setProjectileSpeed(2); // should get slighlty higher with each level

        }

        public int getAgility() {
                return agility;
        }




}
