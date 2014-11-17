package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Pair;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/11/2014.
 */
public class Attack implements TokenComponent{
        private final Token token;
        // derrived stats
        private int attackRange = 3; // how far away this character can attack using ranged attacks
        private float attackDuration = 2; // how long the "is attacking" phase lasts.
        private float attackCooldownDuration = 1; // how long since the last attack ended until a new attack can begin.
        private float projectileSpeed = 2;

        private boolean ableRangedAttack = false;
        private static final transient boolean rangedKeepDistance = true;             // if true the character will hold position while in range of targeted token and it is alive, if false character will persue and get close inbetween shots

        //state variables

        private boolean inAttackRangeOfContinousMoveToken = false;
        private float attackU = 0;                              // 0 = not attacking, >0 attacking, once attackU >=attackDuration then attackU is reset to 0
        private Token meleeAttackTarget;                             // the token that is being attacked, this also marks if this token is in the "attacking" state
        private float attackProjectileU =Float.NaN;
        private float attackProjectileMaxU = Float.NaN;
        private Token projectileAttackTarget;
        private final Pair projectileAttackCoord = new Pair();
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
                if(ableRangedAttack && !token.getDamage().isHit() && (token.getMove() == null || token.getMove().moveU > .75f)){
                // cant initiate attack if being hit, also must have moveU > .75f to ensure near center of tile and can be hit by melee
                        Token targetToken = token.getCommand().getTargetToken();
                        if (targetToken!= null && targetToken.getDamage() != null && targetToken.getDamage().isAttackable()) {
                                // has target, and target is attackable
                                int distance = token.location.distance(token.getCommand().getTargetToken().location);
                                if(distance <= attackRange &&
                                        token.direction.isDirection(token.location, token.getCommand().getTargetToken().location)){
                                        // within attack range, and is facing towards target  // TODO: may want to remove the facing check unless i add a turn time for doing uTurns.
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
                                meleeAttackTarget = token.getCommand().getTargetToken();
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
                                                                // TODO: this check makes melle attacks look more reasonable.
                                                                // however it can create a scenario where a ranged character is standing still
                                                                // and can never be attacked because they are targeting the melee character
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
                        if(inAttackRangeOfContinousMoveToken){ // target is still in range, were going to hit him
                                projectileAttackCoord.set(meleeAttackTarget.getLocation());
                                projectileAttackTarget = meleeAttackTarget;
                                meleeAttackTarget.getDamage().setHitDuration(attackProjectileMaxU, token);
                        }else{ // target got out of range, were going to gurantee miss
                                token.getFloorMap().getNextClosestLegalLocation(token.getLocation(), meleeAttackTarget.getLocation(), projectileAttackCoord);
                                projectileAttackTarget = null;
                        }
                        attackProjectileU = 0;
                        attackProjectileMaxU = token.getLocation().distance(projectileAttackCoord) / projectileSpeed;
                        if(token.listener != null)
                                token.listener.onAttack(projectileAttackTarget,projectileAttackCoord, true);
                        rangedAttack = false;
                }else{
                        sendDamageToAttackTarget(meleeAttackTarget, false);
                        if(token.listener != null)
                                token.listener.onAttack(meleeAttackTarget,meleeAttackTarget.getLocation() ,false);
                }




        }

        private void sendDamageToAttackTarget(Token targetToken, boolean ranged) {
                if(targetToken == null) return;

                boolean dodge;
                int damage;

                if(targetToken.getExperience() == null){
                        dodge = false;
                        damage = token.getExperience().getStrength();
                }else{
                        int speedDifference = token.getExperience().getAgility() - targetToken.getExperience().getStrength();
                        if(speedDifference >0)
                                dodge = MathUtils.randomBoolean(.95f);
                        else
                                dodge = MathUtils.randomBoolean(.15f);
                        if(dodge){
                                damage = 0;
                        }else{
                                damage = token.getExperience().getStrength() - targetToken.getExperience().getStrength();
                                if(damage == 0) damage = 1;
                                damage += MathUtils.random(1,3);
                        }
                }


                if(damage  >0)
                        targetToken.getDamage().addHealth(-damage);
                else
                        damage = 0;

                if(targetToken.getExperience() != null && token.listener != null)
                        token.listener.onAttacked(token, targetToken, damage, dodge);

                if(targetToken.getDamage().isDead()){
                        token.getExperience().addXpFrom(targetToken.getExperience());
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

        /**
         * how long it takes for the attack animaiton to happen
         * also affects how long the being hit animation lasts for target.
         *
         * TODO: i may want the "being hit" duration to be its own stat.
         *
         * TODO:
         * changing this value in the middle of an attack animaiton might cause
         * the player being able to move before animation is over and other weirdnesses.
         * will need to expirement
         *
         * @param attackDuration
         */
        protected void setAttackDuration(float attackDuration){
                this.attackDuration = attackDuration;
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

        /**
         * the time from finishing one attack animation until the next one can begin
         * for ranged attacks the timer starts at time of releasing the projectile
         *
         * however you can not send another projectile until the current one has landed
         *
         * // TODO: need derrived stat to control how fast projectiles move
         *
         * @param attackCooldownDuration
         */
        protected void setAttackCooldownDuration(float attackCooldownDuration){
                this.attackCooldownDuration = attackCooldownDuration;
        }

        public float getProjectileSpeed() {
                return projectileSpeed;
        }

        /**
         * how quickly the projectile reaches its target.
         * @param projectileSpeed
         */
        protected void setProjectileSpeed(float projectileSpeed) {
                this.projectileSpeed = projectileSpeed;
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

        /**
         * how far (manhattan distance) can be when doing ranged attacks
         * @param attackRange
         */
        protected void setAttackRange(int attackRange) {
                this.attackRange = attackRange;
        }

        public int getAttackRange() {
                return attackRange;
        }
}
