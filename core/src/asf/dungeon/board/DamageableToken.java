package asf.dungeon.board;

/**
 * A token that can be attacked by attempting to move on to its tile.
 * DamageableTokens block pathing
 *
 * Typically this is stuff like crates and characters
 *
 * Created by danny on 10/25/14.
 */
public abstract class DamageableToken extends Token {

        //configuration variables
        /**
         * the health of the token. when health reaches zero it is considered dead
         */
        private int health;
        /**
         * how long since being killed (health ==0) until in the state of being fully dead. once fully dead this token can no longer block the path even if blocksPathing = true;
         */
        private float deathDuration = 3;
        /**
         * if this token can currently be attackd (eg, not invincible)
         */
        private boolean attackable = true;
        // state variables
        private float hitU = 0;                                 // being hit overrides moving and attacking, hitU >0 means currently in hit animation, hit animaiton ends when hitU>=hitDuration
        private float hitDuration = 1;                          // hitDuration is set by the token that attacked it, while being "hit" this token can not move or attack
        private Token hitSource;                                // the token that attacked, this also marks if this token is in the "being hit" state.
        private float deathCountdown = 0;                         // the time that must pass before this token is "fully dead", invalid if health>0
        private float deathRemovalCountdown = 10;           // the time after being fully dead to remove this token from the Dungeon. Use Float.NaN to never remove it. The main purpose of this is for performance

        protected DamageableToken(Dungeon dungeon, FloorMap floorMap, int id, String name, int initialHealth) {
                super(dungeon, floorMap,  id, name);
                this.health = initialHealth;
        }

        /**
         * teleports token to this new location, if the new location
         * can not be moved to (normally because it is already occupied) then the move will not work.
         *
         * this generally is used for setting up the dungeon, could eventually be used for stairways to lead to other rooms.
         *
         * @param direction
         * @return true if moved, false if did not move
         */
        @Override
        public boolean teleportToLocation( int x, int y, Direction direction) {
                boolean teleported = super.teleportToLocation( x,y, direction);
                if(teleported){
                        hitU =0;
                        hitSource = null;
                }
                return teleported;

        }

        @Override
        protected void incremenetU(float delta){
                if (isFullyDead()) {
                        deathCountdown -= delta;
                        if(deathCountdown < -deathRemovalCountdown ){
                                dungeon.removeToken(this);
                        }
                        return;
                } else if (isDead()) {
                        deathCountdown -= delta;
                        return;
                }else if (isHit()) {
                        hitU += delta;
                        if (hitU > hitDuration) {
                                hitU = 0;
                                hitSource = null;
                        }
                        return;
                }
        }

        protected abstract void receiveDamageFrom(Token token);

        /**
         * apply pure damage to this token (ignores stats)
         * @param damage
         */
        public void applyDamage(int damage){
                if (isDead()) {
                        return;
                }
                health -= damage;
                if (health < 0)
                        health = 0;

                if (isDead()) {
                        deathCountdown = deathDuration;
                        onDied();
                }
        }

        protected void onDied(){
        }


        /**
         * if the token prevents other tokens from sharing the same tile.
         * @return
         */
        @Override
        public  boolean isBlocksPathing(){
                return super.isBlocksPathing() && !isFullyDead();
        }

        public boolean isAttackable(){return attackable && !isDead(); }

        /**
         * interacting is the state of not being idle (moving, attacking, being dead)
         *
         * @return
         */
        public boolean isInteracting(){
                return isHit() || isDead();
        }

        public boolean isHit(){
                return hitSource != null;
        }

        /**
         * health <=0, may or may not be fully dead
         * @return
         */
        public boolean isDead(){
                return health <=0;
        }

        /**
         * when a token is fully dead it not longer has properties like blocking the pathway.
         *
         * @return
         */
        public boolean isFullyDead(){
                return isDead() && deathCountdown <= 0;
        }

        public int getHealth(){
                return health;
        }

        protected void setHitDuration(float hitDuration, Token hitSource){
                this.hitDuration = hitDuration;
                this.hitSource = hitSource;
                this.hitU = 0;
        }

        /**
         * how long the being hit animaiton takes
         * @return
         */
        public float getHitDuration(){
                return hitDuration;
        }

        public Token getHitSource() {
                return hitSource;
        }

        /**
         * how long the death animaiton takes
         * @param deathDuration
         */
        public void setDeathDuration(float deathDuration){
                if (isDead())
                        throw new IllegalStateException("can not change this value while dead");
                this.deathDuration = deathDuration;
        }

        /**
         * how long the death animaiton takes
         * @return
         */
        public float getDeathDuration(){
                return deathDuration;
        }

        public float getDeathRemovalCountdown() {
                return deathRemovalCountdown;
        }

        public void setDeathRemovalCountdown(float deathRemovalCountdown) {
                this.deathRemovalCountdown = deathRemovalCountdown;
        }
}
