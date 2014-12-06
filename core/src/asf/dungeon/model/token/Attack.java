package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;
import asf.dungeon.model.fogmap.LOS;
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.utility.UtMath;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/11/2014.
 */
public class Attack implements TokenComponent{

        private final Token token;
        private WeaponItem weapon = WeaponItem.NULL;
        private static transient final boolean rangedKeepDistance = true;             // if true the character will hold position while in range of targeted token and it is alive, if false character will persue and get close inbetween shots

        //state variables
        private float attackU = 0;                              // 0 = not attacking, >0 attacking, once attackU >=attackDuration then attackU is reset to 0
        private Token meleeAttackTarget;                             // the token that is being attacked, this also marks if this token is in the "attacking" state
        private float attackProjectileU =Float.NaN;
        private float attackProjectileMaxU = Float.NaN;
        private Token projectileAttackTarget;
        private final Pair projectileAttackCoord = new Pair();
        private boolean rangedAttack = false;
        private float attackCoolDown = 0;                       // time until this token can send another attack, after attacking this value is reset to attackCooldownDuration
        private boolean sentAttackResult = false;

        private boolean inAttackRangeOfCommandTarget;
        private boolean isHoldingRangedWeapon;

        public Attack(Token token) {
                this.token = token;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {
                attackU = 0;
                meleeAttackTarget = null;
                attackCoolDown = 0;
                sentAttackResult = false;
                projectileAttackTarget = null;
                attackProjectileU = Float.NaN;
                attackProjectileMaxU = Float.NaN;
                rangedAttack = false;
        }

        @Override
        public boolean update(float delta) {

                attackProjectileU+=delta;
                if(attackProjectileU >= attackProjectileMaxU){
                        if(projectileAttackTarget != null)
                                sendDamageToAttackTarget(projectileAttackTarget);
                        projectileAttackTarget = null;
                        attackProjectileU = Float.NaN;
                        rangedAttack = false;
                }

                if(isAttacking()){
                        attackU += delta;
                        if (attackU >= weapon.getAttackDuration()) {
                                if(!sentAttackResult){
                                        sendAttackResult();
                                }
                                meleeAttackTarget = null;
                                attackCoolDown = weapon.getAttackCooldown();
                                sentAttackResult = false;
                        }else if(attackU >= weapon.getAttackDuration()/2f && !sentAttackResult){
                                sendAttackResult();
                                sentAttackResult = true;
                        }
                        return true;
                }

                if(rangedAttack == false)
                        attackCoolDown -= delta; // the attack cooldown timer always decreases as long as not dead, not attacking, and has no projectile


                isHoldingRangedWeapon = token.getInventory().getWeaponSlot() != null && token.getInventory().getWeaponSlot().isRanged();

                inAttackRangeOfCommandTarget = calcCanRangedAttack(token.getCommand().getTargetToken());


                if(attackCommandTarget(delta)){
                        return true; // started or is currently doing attack animation
                }else if(rangedKeepDistance && inAttackRangeOfCommandTarget){
                        //Gdx.app.log("Attack","in range of target, dont move");
                        return true; // still in range of target, dont move
                }


                if(token.getDamage().isHit())
                        return true; // Damage doesnt block the update on isHit() because Attack can still do things while is hit, however everything else lower on the stack is blocked

                return false;
        }


        private boolean attackCommandTarget(float delta){

                if (isOnAttackCooldown()) {
                        return false; // attack is on cooldown
                }

                if(token.getDamage().isHit())
                        return false;

                if(inAttackRangeOfCommandTarget &&  rangedAttack == false ){
                        attackU = 0;
                        meleeAttackTarget = token.getCommand().getTargetToken();
                        rangedAttack = true;
                        return true;
                }


                return false;
        }

        /**
         * used by Move to do auto melee attacks on tokens in the way of pathing.
         *
         * Only melee heroes will auto attack, ranged heroes will not.
         * @param delta
         * @return
         */
        protected boolean attackTargetInDirection(float delta){
                if(isHoldingRangedWeapon)
                        return false;

                if (isOnAttackCooldown()) {
                        return false; // attack is on cooldown
                }

                // no need to do check for isAttacking or isHit, its impossible to be attacking or hit when this is called



                Array<Token> tokensAt = token.floorMap.getTokensAt(token.location, token.direction);
                if (tokensAt.size > 0) {
                        for (Token t : tokensAt) {
                                if(calcCanMeleeAttack(t)){
                                        attackU = 0;
                                        meleeAttackTarget = t;
                                        rangedAttack = false;
                                        attackU = 0;
                                        meleeAttackTarget.getDamage().setHitDuration(weapon.getAttackDuration(), token);
                                        sentAttackResult = false;
                                        return true;
                                }


                        }
                }
                return false;
        }




        private void sendAttackResult(){
                //  if ranged attack then launches projectile
                // if melee attack then sends damage

                // TODO: check to make sure target token is in range for both ranged and melee attacks
                // this needs to include also checking the floormap teleporting in the middle of being
                // attacked can get you away safely if done before the attack damage is sent.

                // for ranged attacks i may need to do this again for sendDamageToAttackTarget since
                // there is that small delay

                if(rangedAttack){
                        if(inAttackRangeOfCommandTarget){ // target is still in range, were going to hit him
                                projectileAttackCoord.set(meleeAttackTarget.getLocation());
                                // TODO: this is an awful way to calculate the duration of the projecctile
                                attackProjectileMaxU = token.getLocation().distance(projectileAttackCoord) / weapon.getProjectileSpeed();
                                projectileAttackTarget = meleeAttackTarget;
                                meleeAttackTarget.getDamage().setHitDuration(attackProjectileMaxU, token);
                        }else{ // target got out of range, were going to gurantee miss
                                token.getFloorMap().getNextClosestLegalLocation(token.getLocation(), meleeAttackTarget.getLocation(), projectileAttackCoord);
                                attackProjectileMaxU = token.getLocation().distance(projectileAttackCoord) / weapon.getProjectileSpeed();
                                projectileAttackTarget = null;
                        }
                        attackProjectileU = 0;

                        if(token.listener != null)
                                token.listener.onAttack(projectileAttackTarget,projectileAttackCoord, true);
                }else{
                        sendDamageToAttackTarget(meleeAttackTarget);
                        if(token.listener != null)
                                token.listener.onAttack(meleeAttackTarget,meleeAttackTarget.getLocation() ,false);
                }

        }

        private boolean calcCanRangedAttack(Token target){
                if(!isHoldingRangedWeapon)
                        return false;

                if(target == null || target.getDamage() == null || !target.getDamage().isAttackable())
                        return false;

                if(token.getLogic()!=null && target.getLogic() != null&&token.getLogic().getTeam() == target.getLogic().getTeam())
                        return false;

                int distance = token.location.distance(token.getCommand().getTargetToken().location);
                // not sure why i need distance-1, but in order to actually have the right range i have to subtract 1
                if(distance-1 > weapon.getRange())
                        return false;



                // NOTE: i used to have a check here to make sure this token is facing towards target token
                // i removed it since turning is instant anyway


                if(token.getFogMapping() != null){
                        if(!token.getFogMapping().getCurrentFogMap().isVisible(target.location.x, target.location.y))
                                return false;
                }else{
                        // no fogmapping, so we need to do a ray cast here
                        if(distance != 1 && !LOS.hasLineOfSight(token.getFloorMap(), token.location.x, token.location.y, target.location.x, target.location.y))
                                return false;
                }



                return true;
        }

        private boolean calcCanMeleeAttack(Token target){
                if(isHoldingRangedWeapon)
                        return false;

                if(target == null || target.getDamage() == null || !target.getDamage().isAttackable())
                        return false;

                if(token.getLogic()!=null && target.getLogic() != null&&token.getLogic().getTeam() == target.getLogic().getTeam())
                        return false;

                int distance = token.location.distance(target.location);
                if(distance > 1)
                        return false;


                return true;
        }



        public static class AttackOutcome{
                public int damage;
                public boolean dodge;
                public boolean critical;
        }
        private static final transient AttackOutcome out = new AttackOutcome();

        private void sendDamageToAttackTarget(Token targetToken) {


                if(targetToken.getInventory() != null){
                        // only reset self combat timer if attacking a character
                        token.getInventory().resetCombatTimer();
                        targetToken.getInventory().resetCombatTimer();
                }

                out.damage = 0;
                out.dodge = false;
                out.critical = false;

                if(targetToken.getExperience() == null){
                        out.dodge = false;
                        out.damage = token.getExperience().getStrength();
                }else{
                        float speedDifference = targetToken.getExperience().getAgility() - token.getExperience().getAgility();

                        if(speedDifference >0) {// target is faster,
                                float chance = UtMath.scalarLimitsInterpolation(speedDifference, 0f, 100f, 0f, .5f);
                                out.dodge = token.dungeon.rand.bool(chance);
                        }else{
                                out.dodge = token.dungeon.rand.bool(.025f + targetToken.getExperience().getLuck() / 100f);
                        }

                        if(out.dodge){
                                out.damage = 0;
                        }else{
                                // damage done has a minimum of weapon damage and maximum of strength
                                int strength = token.getExperience().getStrength();
                                int weaponDmg = weapon.getDamage();
                                if(weaponDmg <strength) out.damage = token.dungeon.rand.range(weaponDmg, strength);
                                else out.damage = weaponDmg;

                                // if lucky will do a critical strike
                                if(token.dungeon.rand.bool(getCriticalHitChance())){
                                        // If you are lucky, will do critical damage causing x2 output damage
                                        out.critical = true;
                                        if(out.damage <=0) out.damage = 1;
                                        out.damage *=2;
                                }

                                // damage absorb is the armor rating of worn armor.
                                int armorAbsorb = targetToken.getInventory().getArmorSlot() == null ? 0 : targetToken.getInventory().getArmorSlot().getArmorRating();

                                out.damage-=armorAbsorb;


                        }
                }


                if(out.damage  >0)
                        targetToken.getDamage().addHealth(-out.damage);
                else
                        out.damage = 0;

                if(targetToken.getExperience() != null && token.listener != null)
                        token.listener.onAttacked(token, targetToken, out);

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
                return weapon.getAttackDuration();
        }


        /**
         * if in attack animation or if has a projectile
         * @return
         */
        public boolean isAttackingRanged(){
                return isAttacking() && rangedAttack;
        }

        public Token getAttackTarget(){
                return meleeAttackTarget;
        }

        public float getAttackCooldownDuration() {
                return weapon.getAttackCooldown();
        }

        public float getProjectileSpeed() {
                return weapon.getProjectileSpeed();
        }

        public float getAttackCoolDown() {
                return attackCoolDown;
        }

        public boolean isOnAttackCooldown(){
                return attackCoolDown > 0;
        }

        public boolean isInRangeOfAttackTarget(){
                return inAttackRangeOfCommandTarget;
        }

        public int getAttackRange() {
                return weapon.getRange();
        }

        public float getCriticalHitChance(){
                return token.getExperience().getLuck() / 100f;
        }

        /**
         * the weapon itme that is used in attack calculations. If the token has an inventory this should match the equipped weapon,
         * unless there is no equipped weapon then attack uses a default "NULL" weapon.
         *
         * For determining attack stats use this weapon for determining what weapon is equipped use the weapon in inventory
         *
         * @return
         */
        public WeaponItem getWeapon(){
                return weapon;
        }

        protected void setWeapon(WeaponItem weapon){
                if(weapon == null) weapon  = WeaponItem.NULL;
                if(weapon == this.weapon) return;
                if(token.getAttack().isAttacking() || token.getAttack().isAttackingRanged())
                        throw new IllegalStateException("can not change weapon while attacking");
                this.weapon = weapon;
        }
}
