package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/11/2014.
 */
public class Attack implements TokenComponent{
        private final Token token;
        private int attackRange = 3; // how far away this character can attack using ranged attacks
        private float attackDuration = 2; // how long the "is attacking" phase lasts.
        private float attackCooldownDuration = 1; // how long since the last attack ended until a new attack can begin.

        private boolean ableRangedAttack = true;
        private boolean rangedKeepDistance = true;             // if true the character will hold position while in range of targeted token and it is alive, if false character will persue and get close inbetween shots

        //state variables

        private boolean inAttackRangeOfContinousMoveToken = false;
        private float attackU = 0;                              // 0 = not attacking, >0 attacking, once attackU >=attackDuration then attackU is reset to 0
        private Token meleeAttackTarget;                             // the token that is being attacked, this also marks if this token is in the "attacking" state
        private float attackProjectileU =Float.NaN;
        private float attackProjectileMaxU = Float.NaN;
        private Token projectileAttackTarget;
        private boolean rangedAttack = false;
        private float attackCoolDown = 0;                       // time until this token can send another attack, after attacking this value is reset to attackCooldownDuration
        private boolean sentAttackResult = false;

        public Attack(Token token) {
                this.token = token;
        }

        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                attackU = 0;
                meleeAttackTarget = null;
                return true;
        }

        @Override
        public boolean update(float delta) {
                attackProjectileU+=delta;
                if(attackProjectileU >= attackProjectileMaxU){
                        //Gdx.app.log("Character Token","send ranged damage to: "+projectileAttackTarget.getName());
                        sendDamageToAttackTarget(projectileAttackTarget, true);
                        projectileAttackTarget = null;
                        attackProjectileU = Float.NaN;
                }

                attackCoolDown -= delta; // the attack cooldown timer always decreases as long as not dead

                inAttackRangeOfContinousMoveToken = false;
                Token targetToken = token.getTarget().getToken();
                if (ableRangedAttack &&  targetToken!= null && targetToken.getDamage() != null) {
                        Damage targetTokenDamage = targetToken.getDamage();
                        if(targetTokenDamage.isAttackable()){
                                int distance = token.location.distance(token.getTarget().getToken().location);
                                if(distance <= attackRange && token.direction.isDirection(token.location, token.getTarget().getToken().location)){
                                        inAttackRangeOfContinousMoveToken = true;
                                }
                        }
                }

                if (attack(delta, false, inAttackRangeOfContinousMoveToken)) { // attempt to attack anything in range, but do not auto attack non targetted tokens
                        return true; // can not moveor pick up loot while attacking, so just return out
                }else if(rangedKeepDistance && inAttackRangeOfContinousMoveToken){
                        return true; // still in range of target, dont move
                }



                return false;
        }

        protected boolean attack(float delta, boolean initiateMelee, boolean initiateRanged) {
                if (attackCoolDown > 0) {
                        // attack is on cooldown
                        return false;
                }
                if (!isAttacking()) {

                        if(initiateRanged && !this.hasProjectile() && rangedAttack == false){
                                attackU = 0;
                                meleeAttackTarget = token.getTarget().getToken();
                                rangedAttack = true;
                                return true;
                        }
                        //
                        if(initiateMelee){
                                Array<Token> tokensAt = token.floorMap.getTokensAt(token.location, token.direction);
                                if (tokensAt.size > 0) {
                                        for (Token t : tokensAt) {
                                                if (t.getDamage() != null) {
                                                        meleeAttackTarget = t;
                                                        if (meleeAttackTarget.getDamage().isAttackable()) {
                                                                // TODO: this check might make it impossible for melee token to attack ranged token if ranged token is waiting at <.75f
                                                                Move move = meleeAttackTarget.get(Move.class);
                                                                if(move != null){
                                                                        if(move.moveU < .75f){
                                                                                meleeAttackTarget = null;
                                                                                continue; // if the target moveU is less than .75 that means theyre not that far in to the square, need to wait some
                                                                        }
                                                                }
                                                                break; // we can attack this target, no need to keep looping
                                                        } else {
                                                                meleeAttackTarget = null;
                                                        }
                                                }
                                        }
                                        if (meleeAttackTarget != null) {
                                                attackU = 0;
                                                meleeAttackTarget.getDamage().setHitDuration(attackDuration, token);
                                                sentAttackResult = false;
                                                return true;
                                        } else {
                                                //  no token in blocking tile that can be attacked
                                                return false;
                                        }
                                } else {
                                        // no tokens in blocking tile can be attacked
                                        return false;
                                }
                        }
                        return false;

                } else {
                        attackU += delta;
                        if (attackU >= attackDuration) {
                                if(!sentAttackResult){
                                        sendAttackResult();
                                }
                                meleeAttackTarget = null;
                                attackCoolDown = attackCooldownDuration;
                                sentAttackResult = false;

                        }else if(attackU >= attackDuration/2f && !sentAttackResult){
                                sendAttackResult();
                                sentAttackResult = true;
                        }
                        return true;
                }

        }

        private void sendAttackResult(){

                if(rangedAttack){
                        // TODO: check to make sure the shot can still be lined up, if it cant then dont launch
                        // the projectile
                        attackProjectileU = 0;
                        attackProjectileMaxU = token.getLocation().distance(meleeAttackTarget.getLocation()) / 2f;
                        projectileAttackTarget = meleeAttackTarget;
                        meleeAttackTarget.getDamage().setHitDuration(attackProjectileMaxU, token);
                        if(token.listener != null)
                                token.listener.onAttack(projectileAttackTarget, true);
                        rangedAttack = false;
                }else{
                        sendDamageToAttackTarget(meleeAttackTarget, false);
                        if(token.listener != null)
                                token.listener.onAttack(meleeAttackTarget, false);
                }




        }
        private void sendDamageToAttackTarget(Token targetToken, boolean ranged) {

                targetToken.getDamage().receiveDamageFrom(token);

                if(targetToken.getDamage().isDead()){
                        Experience targetTokenExperience = targetToken.get(Experience.class);
                        if(targetTokenExperience != null){
                                Experience experience = token.get(Experience.class);


                                int gainXp = targetTokenExperience.getLevelRating() - experience.getLevelRating() +1 ;
                                if(gainXp <0)
                                        gainXp = 0;
                                experience.setXp(experience.getXp()+gainXp);
                        }
                }
        }

        public boolean isAttacking() {return meleeAttackTarget != null;}

        public boolean hasProjectile(){return  !Float.isNaN(attackProjectileU);}

        /**
         *
         * @return percentage value between this token and its attack target. value less than 0 means that the attack animation is still happening and the projectile hasnt launched yet
         */
        public float getEffectiveProjectileU(){return attackProjectileU /attackProjectileMaxU;}

        public float getAttackDuration() {
                return attackDuration;
        }

        public boolean isAttackingRanged(){
                return isAttacking() && rangedAttack;
        }

        public Token getAttackTarget(){
                return meleeAttackTarget;
        }

        public float getAttackCooldownDuration() {
                return attackCooldownDuration;
        }

        public float getAttackCoolDown() {
                return attackCoolDown;
        }

        /**
         * this is here for more temporary purposes, normally the token should know
         * if it can do a ranged attack by what weapon he is holding
         * @param ableRangedAttack
         */
        public void setAbleRangedAttack(boolean ableRangedAttack) {
                this.ableRangedAttack = ableRangedAttack;
        }

        public boolean isInRangeOfAttackTarget(){
                return inAttackRangeOfContinousMoveToken;
        }
}
