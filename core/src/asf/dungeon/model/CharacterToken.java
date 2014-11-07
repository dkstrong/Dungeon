package asf.dungeon.model;

import asf.dungeon.model.logic.LocalPlayerLogicProvider;
import asf.dungeon.model.logic.LogicProvider;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A character token has health and can be attacked so it extends Damageable Token instead of Token
 * <p/>
 * Characters have continous move targets that they are always working towards, as they run in to
 * other tokens along their paths they will interact with the token depending on what type of token it is
 * <p/>
 * (for example DamagableTokens will be attacked, ItemTokens will be picked up)
 * <p/>
 * Created by danny on 10/24/14.
 */
public class CharacterToken extends DamageableToken {
        // configuration variables
        /**
         * change between different AI states by changing the logic provider. if logic provider is null then the token will just sit there.
         */
        private LogicProvider logicProvider;
        /**
         * how fast the character moves between tiles, generally a value between 1 and 10, could be higher i suppose.
         */
        private int moveSpeed = 6;
        /**
         * this value is used in determening how much damage will be dealt when attacking other tokens.
         */
        private int attackDamage = 1;
        /**
         * how far away this character can attack using ranged attacks
         */
        private int attackRange = 3;
        /**
         * how long the "is attacking" phase lasts.
         */
        private float attackDuration = 2;
        /**
         * how long since the last attack ended until a new attack can begin.
         */
        private float attackCooldownDuration = 1; // how long until another attack can be done
        /**
         * this vaue is used in determening how much damage will be received from other tokens.
         */
        private int armorValue = 1;
        /**
         * if this character will pick up items when standing on tiles with items
         */
        private boolean picksUpItems = true;
        // state variables
        private final Map<StatusEffect, Array<Float>> statusEffects = new EnumMap<StatusEffect, Array<Float>>(StatusEffect.class);
        private final Array<Pair> path = new Array<Pair>(true, 32, Pair.class);   // the current path that this token is trying to follow, it may be regularly refreshed with new paths
        private final Pair pathedTarget = new Pair();           // the last element of path. this is compared against continuesMoveTarget to prevent spamming the pathfinder coder
        private Pair continuousMoveLocation;                      // the location that this token wants to move to, token will move and attack through tiles along the way to get to its destination
        private Direction continuousMoveDir;                    // alternative to continuousMoveLocation
        private Token continuousMoveToken;                    // alternative to continuousMoveLocation and continousMoveDir, will constantly try to move to location of this token
        private final Pair continuousMoveTokenLastLoc = new Pair();
        private float continuousMoveTokenLostVisionCountdown = 0;
        private float moveU = 1;                                // 1 = fully on the location, less then 1 means moving on to the location still even though it occupies it, use direction variable to determine which way token is walking towards the location
        private Array<Item> inventory = new Array<Item>(true, 16, Item.class);
        private ConsumableItem consumeItem;                               // item to be  consumed on next update
        private boolean ableRangedAttack = true;
        private float attackU = 0;                              // 0 = not attacking, >0 attacking, once attackU >=attackDuration then attackU is reset to 0
        private DamageableToken attackTarget;                             // the token that is being attacked, this also marks if this token is in the "attacking" state
        private boolean attackDamageApplied = false;            // flag to determine if the damage has been applied from the current "attacking" state, invalid if attackTarget == null
        private float attackCoolDown = 0;                       // time until this token can send another attack, after attacking this value is reset to attackCooldownDuration
        private Map<FloorMap, FogMap> fogMaps;
        private Journal journal;
        private transient Listener listener;



        protected CharacterToken(Dungeon dungeon, FloorMap floorMap, int id, String name, ModelId modelId) {
                super(dungeon, floorMap, id, name, modelId, 10);


                for (StatusEffect statusEffect : StatusEffect.values) {
                        statusEffects.put(statusEffect, new Array<Float>(false, 8, Float.class));
                }

                this.setDeathDuration(3f);
                this.setDeathRemovalCountdown(10f);
        }

        /**
         * teleports token to this new location, if the new location
         * can not be moved to (normally because it is already occupied) then the move will not work.
         * <p/>
         * this generally is used for setting up the dungeon, could eventually be used for stairways to lead to other rooms.
         *
         * @param x
         * @param y
         * @param direction
         * @return true if moved, false if did not move
         */
        public boolean teleportToLocation(int x, int y, Direction direction) {
                boolean teleported = super.teleportToLocation(x, y, direction);
                if (teleported) {
                        moveU = 1;
                        path.clear();
                        continuousMoveLocation = null;
                        continuousMoveDir = null;
                        pathedTarget.set(location);
                        attackU = 0;
                        attackTarget = null;

                        if (fogMaps != null) {
                                FogMap fogMap = fogMaps.get(floorMap);
                                if (fogMap == null) {
                                        fogMap = new FogMap(floorMap, this);
                                        fogMaps.put(floorMap, fogMap);
                                }
                                fogMap.update();
                        }
                }

                return teleported;

        }

        /**
         * the location that the token should continuously attempt to move to;
         *
         * @param targetLocation
         * @return true if the token will move to this location (eg a path was found)
         */
        public void setMoveTarget(Pair targetLocation) {
                continuousMoveDir = null;
                continuousMoveLocation = targetLocation;
                continuousMoveToken = null;
        }

        public boolean hasMoveTarget() {
                return continuousMoveLocation != null;
        }

        public void setMoveDir(Direction dir) {

                continuousMoveToken = null;
                Direction oldDir = continuousMoveDir;
                continuousMoveDir = dir;

                if (dir == null && (isAttacking() || isHit())) {
                        // this is here to lock play in to combat when releasin key

                        if (isHit()) {
                                if (continuousMoveLocation == null) {
                                        continuousMoveLocation = new Pair(location).add(oldDir);
                                } else {
                                        continuousMoveLocation.set(location).add(oldDir);
                                }
                        }
                        return;
                }


                if (continuousMoveLocation == null) {
                        continuousMoveLocation = new Pair(location).add(continuousMoveDir);
                } else {
                        continuousMoveLocation.set(location).add(continuousMoveDir);
                }

        }

        public Direction getMoveDir() {
                return continuousMoveDir;
        }

        public void setContinousMoveToken(Token target) {
                if (this.continuousMoveToken == target) {
                        return;
                }

                if (isFogMappingEnabled()) {
                        FogMap fogMap = getFogMap(floorMap);
                        if (!fogMap.isVisible(target.location.x, target.location.y)) {
                                // cant target token that is not currently visible
                                continuousMoveToken = null;
                                return;
                        }
                }


                this.continuousMoveToken = target;
                continuousMoveDir = null;
                if (continuousMoveLocation == null) {
                        continuousMoveLocation = new Pair(target.location);
                } else {
                        continuousMoveLocation.set(target.location);
                }


        }

        public Token getContinuousMoveToken() {
                return continuousMoveToken;
        }

        public boolean useItem(Item item) {
                if (consumeItem != null || isDead())
                        return false; // already consuming an item

                if (item instanceof ConsumableItem) {
                        consumeItem = (ConsumableItem)item;
                        return true;
                }

                return false;

        }

        public void addItem(Item item){
                inventory.add(item);
                if(listener != null)
                        listener.onInventoryAdd(item);
        }

        public boolean discardItem(Item item) {
                if(isDead()){
                        return false;
                }
                boolean valid = inventory.removeValue(item, true);
                if (valid) {
                        if (listener != null)
                                listener.onInventoryRemove(item);
                } else
                        throw new AssertionError(getName() + " is not carrying " + item + " and can not discard it");

                return valid;
        }

        public void addStatusEffect(StatusEffect statusEffect, float duration){
                addStatusEffect(statusEffect,duration,1);
        }
        public void addStatusEffect(StatusEffect statusEffect, float duration, int value){
                if(duration < this.getStatusEffectDuration(statusEffect)){
                        return;
                }

                Array<Float> durations = statusEffects.get(statusEffect);

                float subDuration = duration/value;
                for(int i=0; i<value; i++){
                        durations.add(subDuration);
                }
                statusEffect.begin(this);
                if(listener != null)
                        listener.onStatusEffectChange(statusEffect, duration);
        }

        public void removeStatusEffect(StatusEffect statusEffect){
                Array<Float> durations = statusEffects.get(statusEffect);
                durations.clear();
                statusEffect.end(this);
                if(listener != null)
                        listener.onStatusEffectChange(statusEffect, 0);
        }

        public void removeNegativeStatusEffects(){
                removeStatusEffect(StatusEffect.Poison);
                removeStatusEffect(StatusEffect.Paralyze);
        }

        public void removeAllStatusEffects(){
                for (StatusEffect statusEffect : StatusEffect.values()) {
                        removeStatusEffect(statusEffect);
                }
        }

        protected void incremenetU(float delta) {


                super.incremenetU(delta);
                if (isDead()) {
                        return;
                }

                if (logicProvider != null)    // TODO: i might want to change the floorMap.update() so that all the logic providers are updated then do incrementU(). this would give the ai a more consistent way to interact with other character tokens
                        logicProvider.updateLogic(delta);

                for (StatusEffect statusEffect : StatusEffect.values) {
                        Array<Float> durations = statusEffects.get(statusEffect);
                        if(durations.size ==0){
                                continue;
                        }
                        durations.items[0] -= delta;
                        if(durations.items[0] <=0){
                                statusEffect.apply(this);
                                if(durations.size>1){
                                        durations.items[1] += durations.items[0]; // carry over the remaining duration to the next on the list
                                }
                                durations.removeIndex(0);

                                if(durations.size ==0){
                                        statusEffect.end(this);
                                        if(listener != null)
                                                listener.onStatusEffectChange(statusEffect, 0);
                                }
                        }
                }




                // TODO: maybe i could add this before the isDead() check to allow for resurrection items? would require doing additional checks on all items for the dead or alive state
                if (consumeItem != null) { // if an item was marked for consumption by useItem() then do so now
                        consumeItem.consume(this);
                        if(listener!=null)
                                listener.onConsumeItem(consumeItem);
                        discardItem(consumeItem);
                        consumeItem = null;
                }

                attackCoolDown -= delta; // the attack cooldown timer always decreases as long as not dead

                if (isHit()) {
                        return; // can not attack or move while being hit
                }


                if (attack(delta, false)) { // attempt to attack anything in range, but do not auto attack non targetted tokens
                        return; // can not moveor pick up loot while attacking, so just return out
                }

                if (continuousMoveToken != null) {
                        // set the move targt location to the target tokens location
                        // if the target token is in the fog, then set target location to their last known location
                        // if target token is lost in the fog for over the second, then continue to its last known location but give up pursuit

                        if (continuousMoveLocation == null)
                                continuousMoveLocation = new Pair(continuousMoveToken.location);

                        FogMap fogMap;
                        if (isFogMappingEnabled())
                                fogMap = getFogMap(floorMap);
                        else
                                fogMap = null;

                        if (fogMap == null || fogMap.isVisible(continuousMoveToken.location.x, continuousMoveToken.location.y)) {
                                continuousMoveLocation.set(continuousMoveToken.location);
                                continuousMoveTokenLastLoc.set(continuousMoveToken.location);
                                continuousMoveTokenLostVisionCountdown = 1;
                        } else {
                                continuousMoveLocation.set(continuousMoveTokenLastLoc);
                                continuousMoveTokenLostVisionCountdown -= delta;
                                if (continuousMoveTokenLostVisionCountdown < 0)
                                        continuousMoveToken = null; // lost sight for over a second, no longer chasing
                        }

                        if(continuousMoveToken instanceof DamageableToken){
                                DamageableToken damageableToken = (DamageableToken)continuousMoveToken;
                                if(damageableToken.isDead()){
                                        continuousMoveToken = null; // target died, well keep moving to its last location but we wont target it anymore
                                }
                        }

                }

                if (continuousMoveLocation != null)
                        calcPathToLocation(continuousMoveLocation);



                float deltaMoveU = delta * moveSpeed * 0.25f;
                moveU += deltaMoveU;

                if (moveU > 1) {

                        if (path.size >0)  // check for loot as long as not idle in square. TODO: when path is blocked and stuff in the "chilling" state, pickUpLoot will get spammed, but its better than always spamming it
                                pickUpLoot(); // we check for loot before applying moveU because we want to pick up loot only when fully in the tile and not when just entering a new one

                        if (path.size > 1) {
                                Pair nextLocation = path.get(1);
                                // if there are more pairs to move to, then move towards it
                                if (!floorMap.isLocationBlocked(nextLocation)) {
                                        // there is nothing on the next pair, just move there
                                        Tile prevTile = floorMap.getTile(location);
                                        if (prevTile.isDoor())
                                                prevTile.setDoorOpened(false);

                                        Tile nextTile = floorMap.getTile(nextLocation);
                                        if (nextTile.isDoor())
                                                nextTile.setDoorOpened(true);

                                        path.removeIndex(0);
                                        Direction newDirection = Direction.getDirection(location, nextLocation);
                                        if (newDirection != null)
                                                direction = newDirection;
                                        location.set(nextLocation);
                                        moveU -= 1;
                                        computeFogMap();


                                        if (continuousMoveDir != null)
                                                continuousMoveLocation.set(location).add(continuousMoveDir);


                                } else {

                                        // path is blocked, will attempt to attack what is blocking the path
                                        // if can not attack (due to colldown or some other reason) then the token will just kind of chill
                                        moveU = 1;
                                        if (!isInteracting()) {
                                                Direction newDirection = Direction.getDirection(location, nextLocation);
                                                if (newDirection != null)
                                                        direction = newDirection;
                                        }

                                        attack(delta, true); // auto attack anything in front of me

                                }

                        } else if (path.size == 1 || continuousMoveLocation == null || isLocatedAt(continuousMoveLocation)) {
                                // idle
                                if (path.size > 0) {
                                        path.clear();
                                        pathedTarget.set(location);
                                        Tile tile = floorMap.getTile(location);
                                        if (tile.isStairs()) {
                                                teleportToFloor(tile.getStairsTo());
                                                return;
                                        }
                                }
                                moveU = 1;
                                continuousMoveLocation = null;
                                continuousMoveDir = null;

                        }



                }


        }

        private boolean attack(float delta, boolean initiateMelee) {
                if (attackCoolDown > 0) {
                        // attack is on cooldown
                        return false;
                }
                if (!isAttacking()) {

                        if(ableRangedAttack && continuousMoveToken instanceof DamageableToken){
                                DamageableToken damageableToken = (DamageableToken)continuousMoveToken;
                                if(damageableToken.isAttackable()){
                                        int distance = location.distance(continuousMoveToken.location);
                                        //if(distance == 1 && initiateMelee){
                                                // do nothing, let this be handled by melee attack code
                                        //}else
                                        if(distance <= attackRange && direction.isDirection(location, continuousMoveToken.location)){
                                                attackU = 0;
                                                attackDamageApplied = false;
                                                attackTarget = damageableToken;
                                                attackTarget.setHitDuration(attackDuration, this);
                                                return true;
                                        }
                                }
                        }
                        //
                        if(initiateMelee){
                                Array<Token> tokensAt = floorMap.getTokensAt(location, direction);
                                if (tokensAt.size > 0) {
                                        for (Token t : tokensAt) {
                                                if (t instanceof DamageableToken) {
                                                        attackTarget = (DamageableToken) t;
                                                        if (attackTarget.isAttackable()) {
                                                                if(attackTarget instanceof CharacterToken){
                                                                        CharacterToken attackCharacterTarget = (CharacterToken)attackTarget;
                                                                        if(attackCharacterTarget.moveU < .75f){
                                                                                attackTarget = null;
                                                                                continue; // if the target moveU is less than .75 that means theyre not that far in to the square, need to wait some
                                                                        }
                                                                }
                                                                break; // we can attack this target, no need to keep looping
                                                        } else {
                                                                attackTarget = null;
                                                        }
                                                }
                                        }
                                        if (attackTarget != null) {
                                                attackU = 0;
                                                attackDamageApplied = false;
                                                attackTarget.setHitDuration(attackDuration, this);
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
                                if (!attackDamageApplied) {
                                        // normally this should be applied in the below if statement, however it is possibly feasible
                                        // that with bad framerates that applying damage might not work, so check to make sure its done here
                                        sendDamageToAttackTarget();
                                        attackDamageApplied = true;
                                }
                                attackTarget = null;
                                attackCoolDown = attackCooldownDuration;
                        } else if (!attackDamageApplied && attackU > attackDuration / 2f) {
                                sendDamageToAttackTarget();
                                attackDamageApplied = true;
                        }
                        return true;
                }



        }

        private void calcPathToLocation(Pair targetLocation) {
                if (targetLocation.equals(pathedTarget)) {
                        //Gdx.app.log(name, "already pathing: " + targetLocation);
                        return; // already targeting this loaction, dont calc again
                }


                if (location.equals(targetLocation)) {
                        // if already on this location, avoid pathing ... (not sure if i need this)
                        path.clear();
                        path.add(location);
                        pathedTarget.set(targetLocation);
                        return;
                }


                Tile targetTile = floorMap.getTile(targetLocation);
                if (targetTile == null || targetTile.isBlockMovement()) {
                        // trying to path into an illegal location, stop moving
                        path.clear();
                        path.add(location);
                        pathedTarget.set(targetLocation);
                        return;
                }


                boolean foundPath = floorMap.computePath(new Pair(location), new Pair(targetLocation), path);
                if (!foundPath) {
                        Gdx.app.error("Character Token", "No path found");
                        // TODO: perhaps i should "cancel" out movement ag path.clear() path.addLocation() pathedTarget.set(targetLocation);
                        return; // no path was found
                }

                // apply the path that was found
                pathedTarget.set(path.get(path.size - 1));


                if (isMoving() && path.size > 1) {
                        // if new path causes the token to reverse direction, the reverse happens
                        // immediatly without having to finish passing through the tile.
                        // this reverses the direction and fixes the path to account for this
                        Pair nextLocation = path.get(1);
                        if (!floorMap.isLocationBlocked(nextLocation)) {
                                Direction newDir = Direction.getDirection(path.get(0), nextLocation);  // should this be locatioin,nextlocation?
                                if (newDir.isOpposite(direction)) {
                                        Tile prevTile = floorMap.getTile(location);
                                        if (prevTile.isDoor())
                                                prevTile.setDoorOpened(false);

                                        Tile nextTile = floorMap.getTile(nextLocation);
                                        if (nextTile.isDoor())
                                                nextTile.setDoorOpened(true);

                                        path.removeIndex(0);
                                        direction = newDir;
                                        location.set(nextLocation); // set location to path[1] (which is now 0 after removing the original 0)
                                        moveU = 1 - moveU;
                                        computeFogMap();
                                        // attackCoolDown == attackCooldownDuration;  // i could do this here to punish making uturns, and give an advantage for coming up from behind
                                }
                        }


                }
        }



        private void pickUpLoot() {
                if (!picksUpItems)
                        return;
                Array<Token> tokensAt = floorMap.getTokensAt(location, LootToken.class);
                for (Token t : tokensAt) {
                        LootToken lootToken = (LootToken) t;
                        lootToken.becomeRemoved();
                        addItem(lootToken.getItem());
                }
        }


        private void sendDamageToAttackTarget() {
                attackTarget.receiveDamageFrom(this);
                // TODO: hook to check for death of target, maybe add XP or other stuff
        }

        @Override
        protected void receiveDamageFrom(CharacterToken characterToken) {
                // TODO: make use of attack damage, armor, chance to evade, etc, etc
                addHealth(-characterToken.attackDamage);
        }

        private void computeFogMap() {
                if (fogMaps == null)
                        return;
                fogMaps.get(floorMap).update();
        }


        protected void setHitDuration(float hitDuration, Token hitSource) {
                super.setHitDuration(hitDuration, hitSource);
                attackU = 0;
                attackTarget = null;
        }

        public float getLocationFloatX() {
                if (!isMoving() || direction == Direction.South || direction == Direction.North)
                        return location.x;
                else if (direction == Direction.East)
                        return MathUtils.lerp(location.x - 1, location.x, moveU);
                else if (direction == Direction.West)
                        return MathUtils.lerp(location.x + 1, location.x, moveU);
                throw new AssertionError("unexpected state");
        }

        public float getLocationFloatY() {
                if (!isMoving() || direction == Direction.West || direction == Direction.East)
                        return location.y;
                else if (direction == Direction.North)
                        return MathUtils.lerp(location.y - 1, location.y, moveU);
                else if (direction == Direction.South)
                        return MathUtils.lerp(location.y + 1, location.y, moveU);
                throw new AssertionError("unexpected state");
        }


        /**
         * interacting is the state of not being idle (moving, attacking, being dead)
         *
         * @return
         */
        public boolean isInteracting() {

                return isMoving() || isAttacking() || super.isInteracting();
        }

        /**
         * if the token is currently moving between two tiles
         *
         * @return
         */
        public boolean isMoving() {
                return moveU != 1;
        }

        public boolean isAttacking() {
                return attackTarget != null;
        }

        public boolean hasStatusEffect(StatusEffect statusEffect){
                return statusEffects.get(statusEffect).size >0;
        }

        public float getStatusEffectDuration(StatusEffect statusEffect){
                float duration = 0;
                Array<Float> durations = statusEffects.get(statusEffect);
                for (Float aFloat : durations) {
                        duration+=aFloat;
                }
                return duration;
        }

        /**
         * how fast the token moves between tiles
         *
         * @param moveSpeed
         */
        public void setMoveSpeed(int moveSpeed) {
                this.moveSpeed = moveSpeed;
        }

        /**
         * how fast the token moves between tiles
         *
         * @return
         */
        public int getMoveSpeed() {
                return moveSpeed;
        }

        /**
         * how long the attack animation takes
         *
         * @return
         */
        public float getAttackDuration() {
                return attackDuration;
        }

        /**
         * how long the attack animation should take (can not be set while currently attacking)
         *
         * @param attackDuration
         */
        public void setAttackDuration(float attackDuration) {
                if (this.isAttacking())
                        throw new IllegalStateException("can not change this value while attacking");
                this.attackDuration = attackDuration;
        }

        public int getAttackDamage() {
                return attackDamage;
        }

        public void setAttackDamage(int attackDamage) {
                this.attackDamage = attackDamage;
        }

        /**
         * TODO: this is here more for temporary/debugging purposes, normally the character token should know
         * if it can do a ranged attack based on what weapon he or she is holding.
         *
         * @param ableRangedAttack
         */
        public void setAbleRangedAttack(boolean ableRangedAttack) {
                this.ableRangedAttack = ableRangedAttack;
        }

        /**
         * do not modify! use addItem() and discardItem(), this is required to properly announce this change to the listener
         * @return
         */
        public Array<Item> getInventory() {
                return inventory;
        }

        public LogicProvider getLogicProvider() {
                return logicProvider;
        }

        /**
         * the AI that controls this token, set null to have the token just sit there idle.
         *
         * @param logicProvider
         */
        public void setLogicProvider(LogicProvider logicProvider) {
                this.logicProvider = logicProvider;
                if (this.logicProvider != null) {
                        this.logicProvider.setToken(this);
                } else
                        this.setMoveTarget(location);
        }

        public void setFogMappingEnabled(boolean enabled) {
                if (enabled && fogMaps == null) {
                        fogMaps = new HashMap<FloorMap, FogMap>(16);
                        fogMaps.put(floorMap, new FogMap(floorMap, this));
                        computeFogMap();
                } else if (!enabled && fogMaps != null) {
                        fogMaps = null;
                }
        }

        public FogMap getFogMap(FloorMap floorMap) {
                return fogMaps.get(floorMap);
        }

        /**
         *
         * @deprecated temporarily here for debugging
         */
        @Deprecated
        public Map<FloorMap, FogMap> getFogMaps(){
                return fogMaps;
        }

        public boolean isFogMappingEnabled() {
                return fogMaps != null;
        }

        public void setJournalEnabled(boolean enabled){
                if(enabled && journal == null){
                        journal = new Journal();
                }else if(!enabled && journal != null){
                        journal = null;
                }
        }

        public Journal getJournal(){
                return journal;
        }

        public boolean isJournalEnabled(){
                return journal != null;
        }


        public void setListener(Listener listener) {
                this.listener = listener;
        }

        public static interface Listener {
                public void onInventoryAdd(Item item);

                public void onInventoryRemove(Item item);

                public void onConsumeItem(ConsumableItem item);

                public void onStatusEffectChange(StatusEffect effect, float duration);
        }




}
