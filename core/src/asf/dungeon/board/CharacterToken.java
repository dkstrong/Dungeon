package asf.dungeon.board;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import asf.dungeon.board.logic.LogicProvider;
import asf.dungeon.board.pathfinder.Tile;

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
        // final variables
        private static final String logName = null;//"berzerker";
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
        private boolean consumesItems = true;
        // state variables
        private final Array<Pair> path = new Array<Pair>(32);   // the current path that this token is trying to follow, it may be regularly refreshed with new paths
        private final Pair pathedTarget = new Pair();           // the last element of path. this is compared against continuesMoveTarget to prevent spamming the pathfinder coder
        private Pair continuousMoveLocation;                      // the location that this token wants to move to, token will move and attack through tiles along the way to get to its destination
        private Direction continuousMoveDir;                    // alternative to continuousMoveLocation
        private Token continuousMoveToken;                    // alternative to continuousMoveLocation and continousMoveDir, will constantly try to move to location of this token
        private final Pair continuousMoveTokenLastLoc = new Pair();
        private float continuousMoveTokenLostVisionCountdown = 0;
        private float moveU = 1;                                // 1 = fully on the location, less then 1 means moving on to the location still even though it occupies it, use direction variable to determine which way token is walking towards the location
        private float attackU = 0;                              // 0 = not attacking, >0 attacking, once attackU >=attackDuration then attackU is reset to 0
        private DamageableToken attackTarget;                             // the token that is being attacked, this also marks if this token is in the "attacking" state
        private boolean attackDamageApplied = false;            // flag to determine if the damage has been applied from the current "attacking" state, invalid if attackTarget == null
        private float attackCoolDown = 0;                       // time until this token can send another attack, after attacking this value is reset to attackCooldownDuration
        private Map<FloorMap, FogMap> fogMaps;


        protected CharacterToken(Dungeon dungeon, FloorMap floorMap, int id, String name) {
                super(dungeon, floorMap, id, name, 10);
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
                boolean teleported = super.teleportToLocation( x, y, direction);
                if (teleported) {
                        moveU = 1;
                        path.clear();
                        continuousMoveLocation = null;
                        continuousMoveDir = null;
                        pathedTarget.set(location);
                        attackU = 0;
                        attackTarget = null;

                        if(fogMaps != null){
                                FogMap fogMap = fogMaps.get(floorMap);
                                if(fogMap == null){
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

                if(dir == null && (isAttacking() || isHit())){
                        // this is here to lock play in to combat when releasin key

                        if(isHit()){
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

        public Direction getMoveDir(){
                return continuousMoveDir;
        }

        public void setContinousMoveToken(Token target){
                if(this.continuousMoveToken == target){
                        return;
                }

                if(isFogMappingEnabled()){
                        FogMap fogMap = getFogMap(floorMap);
                        if(!fogMap.isVisible(target.location.x, target.location.y)){
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
        public Token getContinuousMoveToken(){
                return continuousMoveToken;
        }

        private void calcPathToLocation(Pair targetLocation) {
                if (targetLocation.equals(pathedTarget)) {
                        //Gdx.app.log(name, "already pathing: " + targetLocation);
                        return; // already targeting this loaction, dont calc again
                }


                if(location.equals(targetLocation)){
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


                Array<Pair> newPath = floorMap.computePath(new Pair(location), new Pair(targetLocation));
                if (newPath == null) {
                        return; // no path was found
                }

                // apply the path that was found

                if (isMoving() && newPath.size > 1 && !floorMap.isLocationBlocked(newPath.get(1))) {
                        // if new path causes the token to reverse direction, the reverse hapens
                        // immediatly without having to finish passing through the tile.
                        // this reverses the direction and fixes the path to account for this
                        Pair nextLocation = newPath.get(1);
                        Direction newDir = Direction.getDirection(newPath.get(0), nextLocation);
                        if (newDir.isOpposite(direction)) {
                                moveU = 1 - moveU;
                                direction = newDir;
                                path.clear();
                                path.addAll(newPath);
                                path.removeIndex(0);
                                location.set(nextLocation); // set location to path[1] (which is now 0 after removing the original 0)
                                // attackCoolDown == attackCooldownDuration;  // i could do this here to punish making uturns, and give an advantage for coming up from behind
                                return;
                        }
                }


                path.clear();
                path.addAll(newPath);
                pathedTarget.set(path.get(path.size - 1));
        }

        protected void incremenetU(float delta) {
                super.incremenetU(delta);
                if (isDead()) {
                        return;
                }
                attackCoolDown -= delta;
                if (isHit()) {
                        return;
                }

                if (logicProvider != null)
                        logicProvider.updateLogic(delta);

                if(continuousMoveToken != null){
                        // set the move targt location to the target tokens location
                        // if the target token is in the fog, then set target location to their last known location
                        // if target token is lost in the fog for over the second, then continue to its last known location but give up pursuit

                        if (continuousMoveLocation == null)
                                continuousMoveLocation = new Pair(continuousMoveToken.location);

                        FogMap fogMap;
                        if(isFogMappingEnabled())
                                fogMap = getFogMap(floorMap);
                        else
                                fogMap = null;

                        if(fogMap== null || fogMap.isVisible(continuousMoveToken.location.x, continuousMoveToken.location.y)){
                                continuousMoveLocation.set(continuousMoveToken.location);
                                continuousMoveTokenLastLoc.set(continuousMoveToken.location);
                                continuousMoveTokenLostVisionCountdown = 1;
                        }else{
                                continuousMoveLocation.set(continuousMoveTokenLastLoc);
                                continuousMoveTokenLostVisionCountdown -=delta;
                                if(continuousMoveTokenLostVisionCountdown <0)
                                        continuousMoveToken = null; // lost sight for over a second, no longer chasing

                        }

                }

                if (continuousMoveLocation != null)
                        calcPathToLocation(continuousMoveLocation);

                float deltaMoveU = delta * moveSpeed * 0.25f;
                moveU += deltaMoveU;

                if (isAttacking()) {
                        moveU = 1;
                        attack(delta);
                        return;
                }

                if(moveU > 1) {
                        if (path.size > 1) {
                                Pair nextLocation = path.get(1);

                                // if there are more pairs to move to, then move towards it
                                if (!floorMap.isLocationBlocked(nextLocation)) {
                                        // there is nothing on the next pair, just move there
                                        FloorTile prevFloorTile = floorMap.getTile(location);
                                        if(prevFloorTile.isDoor())
                                                prevFloorTile.setDoorOpened(false);

                                        FloorTile nextFloorTile = floorMap.getTile(nextLocation);
                                        if(nextFloorTile.isDoor())
                                                nextFloorTile.setDoorOpened(true);

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

                                        attack(delta);

                                }

                        } else if (path.size == 1 || continuousMoveLocation == null || isLocatedAt(continuousMoveLocation)) {
                                // idle
                                if (path.size > 0) {
                                        path.clear();
                                        pathedTarget.set(location);
                                        FloorTile tile = floorMap.getTile(location);
                                        if(tile.isStairs()){
                                                teleportToFloor(tile.getStairsTo());
                                                return;
                                        }
                                }
                                moveU = 1;
                                continuousMoveLocation = null;
                                continuousMoveDir = null;

                        }
                        pickUpItems();

                }


        }

        private boolean attack(float delta) {
                if (attackCoolDown > 0) {
                        // attack is on cooldown
                        return false;
                }
                if (!isAttacking()) {
                        Array<Token> tokensAt = floorMap.getTokensAt(location, direction);
                        if (tokensAt.size > 0) {
                                for (Token t : tokensAt) {
                                        if (t instanceof DamageableToken) {
                                                attackTarget = (DamageableToken) t;
                                                if (attackTarget.isAttackable()) {
                                                        break;
                                                } else {
                                                        attackTarget = null;
                                                }
                                        }
                                }
                                if (attackTarget != null) {
                                        attackU = 0;
                                        attackDamageApplied = false;
                                        attackTarget.setHitDuration(attackDuration, this);
                                } else {
                                        //  no token in blocking tile that can be attacked
                                        return false;
                                }
                        } else {
                                // no tokens in blocking tile can be attacked
                                return false;
                        }
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
                }

                return true;

        }

        private void pickUpItems() {
                if (!consumesItems)
                        return;
                Array<Token> tokensAt = floorMap.getTokensAt(location, LootToken.class);
                for (Token t : tokensAt) {
                        LootToken lootToken = (LootToken) t;
                        lootToken.becomeRemoved();
                        // TODO: consume item, add to inventory, do its effect, whatever
                }
        }


        private void sendDamageToAttackTarget() {
                attackTarget.receiveDamageFrom(this);
                // TODO: hook to check for death of target, maybe add XP or other stuff
        }

        private void computeFogMap() {
                if (fogMaps == null)
                        return;
                fogMaps.get(floorMap).update();
        }

        @Override
        protected void receiveDamageFrom(Token token) {
                if (token instanceof CharacterToken) {
                        CharacterToken characterToken = (CharacterToken) token;
                        // TODO: use attackDamage, armor, maybe some random chance
                        // to come up with a damage value, then apply it via applyDamage()
                        applyDamage(characterToken.attackDamage);
                }

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
                        fogMaps= new HashMap<FloorMap, FogMap>(16);
                        fogMaps.put(floorMap, new FogMap(floorMap, this));
                        computeFogMap();
                } else if (!enabled && fogMaps != null) {
                        fogMaps = null;
                }
        }

        public FogMap getFogMap(FloorMap floorMap) {
                return fogMaps.get(floorMap);
        }

        public boolean isFogMappingEnabled(){
                return fogMaps != null;
        }


}
