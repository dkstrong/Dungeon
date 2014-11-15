package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.item.Item;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Danny on 11/11/2014.
 */
public class Damage implements TokenComponent{
        private final Token token;
        private int health;
        private int maxHealth;
        private float deathDuration = 3; // how long since being killed (health ==0) until in the state of being fully dead. once fully dead this token can no longer block the path even if blocksPathing = true;

        private boolean attackable = true; // if this token can currently be attackd (eg, not invincible)

        // state variables
        private float hitU = 0;                                 // being hit overrides moving and attacking, hitU >0 means currently in hit animation, hit animaiton ends when hitU>=hitDuration
        private float hitDuration = 1;                          // hitDuration is set by the token that attacked it, while being "hit" this token can not move or attack
        private Token hitSource;                                // the token that attacked, this also marks if this token is in the "being hit" state.
        private float deathCountdown = 0;                         // the time that must pass before this token is "fully dead", invalid if health>0
        private float deathRemovalCountdown = 10;           // the time after being fully dead to remove this token from the Dungeon. Use Float.NaN to never remove it. The main purpose of this is for performance

        public Damage(Token token, int initialHealth) {
                this.token = token;
                token.setBlocksPathing(true);
                this.health = initialHealth;
                this.maxHealth = initialHealth;
        }

        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                hitU = 0;
                hitSource = null;
                return true;
        }

        @Override
        public boolean update(float delta) {
                if (isFullyDead()) {
                        token.setBlocksPathing(false);
                        deathCountdown -= delta;
                        if (deathCountdown < -deathRemovalCountdown) {
                                token.dungeon.removeToken(token);
                        }
                        return true;
                } else if (isDead()) {
                        deathCountdown -= delta;
                        return true;
                } else if (isHit()) {
                        hitU += delta;
                        if (hitU > hitDuration) {
                                hitU = 0;
                                hitSource = null;
                        }
                        return true;
                }
                return false;
        }

        /**
         * adds or subtracts health, will kill or revive token
         * if health reaches or recovers from zero
         *
         * @param value
         */
        public void addHealth(int value) {
                if (value > 0) {
                        boolean wasDead = isDead();
                        this.health += value;
                        if (health > maxHealth)
                                health = maxHealth;

                        if (wasDead && !isDead()) {
                                deathCountdown = 0;
                                onRevive();
                        }
                } else if (value < 0) {
                        if (isDead()) {
                                return;
                        }
                        health += value;
                        if (health < 0)
                                health = 0;

                        if (isDead()) {
                                deathCountdown = deathDuration;
                                onDied();
                        }
                }


        }

        protected void onRevive() {
                token.setBlocksPathing(true);
        }

        protected void onDied() {

                Inventory inventory = token.get(Inventory.class);
                if(inventory != null){
                        Item item = inventory.getItem();
                        if(item != null)
                                token.dungeon.newLootToken(token.getFloorMap(), item, token.getLocation().x, token.getLocation().y);
                }
        }

        public boolean isAttackable() {
                return attackable && !isDead();
        }

        public boolean isHit() {
                return hitSource != null;
        }

        /**
         * health <=0, may or may not be fully dead
         *
         * @return
         */
        public boolean isDead() {
                return health <= 0;
        }

        /**
         * when a token is fully dead it not longer has properties like blocking the pathway.
         *
         * @return
         */
        public boolean isFullyDead() {
                return isDead() && deathCountdown <= 0;
        }

        public int getHealth() {
                return health;
        }

        public int getMaxHealth() {
                return maxHealth;
        }

        protected void setHitDuration(float hitDuration, Token hitSource) {
                if (this.hitSource != null)
                        return; // dont accept new hit duration of a second attacker. this prevents from being put into a helpless state when attacked by 2 enemies at once

                this.hitDuration = hitDuration;
                this.hitSource = hitSource;
                this.hitU = 0;
        }

        /**'
         * how long the being hit animaiton takes
         *
         * @return
         */
        public float getHitDuration() {
                return hitDuration;
        }

        public Token getHitSource() {
                return hitSource;
        }

        /**
         * how long the death animaiton takes
         *
         * @param deathDuration
         */
        public void setDeathDuration(float deathDuration) {
                if (isDead())
                        throw new IllegalStateException("can not change this value while dead");
                this.deathDuration = deathDuration;
        }

        /**
         * how long the death animaiton takes
         *
         * @return
         */
        public float getDeathDuration() {
                return deathDuration;
        }

        public float getDeathRemovalCountdown() {
                return deathRemovalCountdown;
        }

        public void setDeathRemovalCountdown(float deathRemovalCountdown) {
                this.deathRemovalCountdown = deathRemovalCountdown;
        }

        protected void receiveDamageFrom(Token attacker) {
                Experience attackerExperience = attacker.get(Experience.class);
                Experience experience = token.get(Experience.class);
                if(experience == null){
                        addHealth(-attackerExperience.getStrengthRating());
                        // dont notify listener, not a major thing happening here..
                        return;
                }

                int speedDifference = experience.getSpeedRating() - attackerExperience.getSpeedRating();
                // if negative, im slower than my attacker
                boolean dodge;
                if(speedDifference >=0){
                        dodge = MathUtils.randomBoolean(.5f);
                }else{
                        dodge = MathUtils.randomBoolean(.15f);
                }

                int damage;
                if(!dodge){
                        damage = attackerExperience.getStrengthRating() - experience.getStrengthRating();
                        if(damage  >0){
                                addHealth(-damage);
                        }else{
                                damage = 0;
                        }
                }else{
                        damage = 0;
                }

                if(token.listener != null)
                        token.listener.onAttacked(attacker, token, damage, dodge);
        }
}
