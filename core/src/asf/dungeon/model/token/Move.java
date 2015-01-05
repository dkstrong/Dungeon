package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Pathfinder;
import asf.dungeon.model.Tile;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.item.KeyItem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/11/2014.
 */
public class Move implements TokenComponent , Teleportable{
        private final Token token;
        private float moveSpeed = 1.5f; // how fast the character moves between tiles, generally a value between 1 and 10, could be higher i suppose.
        private float moveSpeedDiagonal = 1.06066017177f;
        private boolean picksUpItems = true; // if this character will pick up items when standing on tiles with items
        private final Array<Pair> path = new Array<Pair>(true, 32, Pair.class);   // the current path that this token is trying to follow, it may be regularly refreshed with new paths
        private final Pair pathedTarget = new Pair();           // the last element of path. this is compared against continuesMoveTarget to prevent spamming the pathfinder coder


        private final Pair continuousMoveTokenLastLoc = new Pair();
        private float continuousMoveTokenLostVisionCountdown = 0;
        private float moveU = 1;                                // 1 = fully on the location, less then 1 means moving on to the location still even though it occupies it, use direction variable to determine which way token is walking towards the location
        private Vector2 floatLocation = new Vector2();


        public Move(Token token) {
                this.token = token;
        }

        @Override
        public boolean canTeleport(FloorMap fm, int x, int y, Direction direction) {
                return true;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {
                moveU = 1;
                path.clear();
                pathedTarget.set(x, y);
                floatLocation.set(x, y);
        }

        @Override
        public boolean update(float delta) {
                if (token.getCommand().getTargetToken() != null) {
                        // set the move targt location to the target tokens location
                        // if the target token is in the fog, then set target location to their last known location
                        // if target token is lost in the fog for over the second, then continue to its last known location but give up pursuit
                        Pair targetLocation = token.getCommand().getLocation();
                        Token targetToken = token.getCommand().getTargetToken();

                        FogMap fogMap;
                        if (token.getFogMapping() != null)
                                fogMap = token.getFogMapping().getFogMap(token.getFloorMap());
                        else
                                fogMap = null;
                        // TODO: how this is configured is that if fogmapping is turned off, then the target
                        // can not be lost in to the fog, Instead I should do it based on range as  a back up
                        // I may need to modify the monster ai to allow for this
                        boolean targetInvisisible = targetToken.getStatusEffects() != null && targetToken.getStatusEffects().has(StatusEffect.Invisibility);

                        if (!targetInvisisible && (fogMap == null || fogMap.isVisible(targetToken.getLocation().x, targetToken.getLocation().y))) {
                                targetLocation.set(targetToken.getLocation());
                                continuousMoveTokenLastLoc.set(targetToken.getLocation());
                                continuousMoveTokenLostVisionCountdown = 1;
                        } else {
                                targetLocation.set(continuousMoveTokenLastLoc);
                                continuousMoveTokenLostVisionCountdown -= delta;
                                if (continuousMoveTokenLostVisionCountdown < 0) {
                                        token.getCommand().setTargetToken(null); // lost sight for over a second, no longer chasing
                                        targetToken = null;
                                }

                        }

                        if (targetToken != null) {
                                Damage moveTokenDamage = targetToken.get(Damage.class);
                                if (moveTokenDamage != null) {
                                        if (moveTokenDamage.isDead()) {
                                                token.getCommand().setTargetToken(null); // target died, well keep moving to its last location but we wont target it anymore
                                                targetToken = null;
                                        }
                                }
                        }


                }


                calcPathToLocation(token.getCommand().getLocation());

                moveU += delta * (token.direction.isDiagonal() ? moveSpeedDiagonal : moveSpeed);


                FloorMap floorMap = token.getFloorMap();
                Pair location = token.getLocation();

                if (moveU > 1) {
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
                                        Direction newDirection = location.direction(nextLocation);
                                        if (newDirection != null)
                                                token.direction = newDirection;
                                        location.set(nextLocation);
                                        moveU -= 1;
                                        if (token.getFogMapping() != null)
                                                token.getFogMapping().computeFogMap();

                                } else {

                                        // path is blocked, will attempt to attack what is blocking the path
                                        // if can not attack (due to cooldown or some other reason) then the token will just kind of chill
                                        moveU = 1;

                                        Direction newDirection = location.direction(nextLocation);
                                        if (newDirection != null)
                                                token.direction = newDirection;

                                        boolean action;
                                        if (token.getInteractor() != null) {
                                                action = pushBoulder(nextLocation);
                                                if (!action)
                                                        action = token.getInteractor().interact(nextLocation);
                                        } else {
                                                action = false;
                                        }

                                        if (!action) {
                                                action = useKey(nextLocation);
                                                if (!action && token.getAttack() != null)
                                                        token.getAttack().attackTargetInDirection(delta); // auto attack anything in front of me, do not do ranged attack
                                        }


                                }

                        } else if (path.size == 1) {
                                // idle
                                path.clear();
                                pathedTarget.set(location);
                                Stairs stairs = floorMap.getStairsAt(location.x, location.y);
                                if (stairs != null && stairs.stairsTo >=0) {
                                        token.dungeon.moveToken(token, token.dungeon.generateFloor(stairs.stairsTo));
                                        updateFloatLocation();
                                        return true;
                                }
                                moveU = 1;
                                //pickUpLoot();
                        } else {
                                moveU = 1;
                        }


                }

                if (moveU > .9f && path.size > 0) {
                        pickUpLoot();
                }
                updateFloatLocation();
                return false;
        }

        private void updateFloatLocation() {
                floatLocation.set(getLocationFloatX(), getLocationFloatY());
        }

        private void calcPathToLocation(Pair targetLocation) {
                if (targetLocation.equals(pathedTarget)) {
                        //Gdx.app.log(name, "already pathing: " + targetLocation);
                        return; // already targeting this loaction, dont calc again
                }


                if (token.location.equals(targetLocation)) {
                        // if already on this location, avoid pathing ... (not sure if i need this)
                        path.clear();
                        path.add(token.location);
                        pathedTarget.set(targetLocation);
                        return;
                }

                boolean foundPath = token.floorMap.pathfinder.generate(token, new Pair(token.location), new Pair(targetLocation), path,
                        Pathfinder.PathingPolicy.CanDiagonalIfNotCuttingCorner, false, token.getInteractor() == null ? Integer.MAX_VALUE : 25);
                if (!foundPath) {
                        //Gdx.app.error("Token", "No path found");
                        path.clear();
                        path.add(token.location);
                        pathedTarget.set(targetLocation);
                        return; // no path was found
                }

                // apply the path that was found
                pathedTarget.set(path.get(path.size - 1));


                if (moveU != 1 && path.size > 1) {
                        // if new path causes the token to reverse direction, the reverse happens
                        // immediatly without having to finish passing through the tile.
                        // this reverses the direction and fixes the path to account for this
                        Pair nextLocation = path.get(1);
                        if (!token.floorMap.isLocationBlocked(nextLocation)) {
                                Direction newDir = token.location.direction(nextLocation);
                                if (newDir.isOpposite(token.direction)) {
                                        Tile prevTile = token.floorMap.getTile(token.location);
                                        if (prevTile.isDoor())
                                                prevTile.setDoorOpened(false);

                                        Tile nextTile = token.floorMap.getTile(nextLocation);
                                        if (nextTile.isDoor())
                                                nextTile.setDoorOpened(true);

                                        path.removeIndex(0);
                                        token.direction = newDir;
                                        token.location.set(nextLocation); // set location to path[1] (which is now 0 after removing the original 0)
                                        moveU = 1 - moveU;
                                        if (token.getFogMapping() != null)
                                                token.getFogMapping().computeFogMap();

                                        // attackCoolDown == attackCooldownDuration;  // i could do this here to punish making uturns, and give an advantage for coming up from behind
                                }
                        }


                }
        }

        private boolean pushBoulder(Pair nextLocation) {
                Array<Token> tokensAt = token.floorMap.getTokensAt(nextLocation);
                for (Token t : tokensAt) {
                        Boulder boulder = t.get(Boulder.class);
                        if (boulder != null) {
                                boulder.push(token);
                                return true;
                        }
                }
                return false;
        }

        private static final boolean doubleTapToOpenDoor = false;
        private Tile lastTriedTile = null;

        private boolean useKey(Pair nextLocation) {
                if(doubleTapToOpenDoor){
                        return useKeyDoubleTap(nextLocation);
                }

                Tile nextTile = token.floorMap.getTile(nextLocation);
                if(nextTile.isDoor() && nextTile.isDoorLocked()){
                        if(nextTile.getDoorSymbol() instanceof KeyItem){
                                boolean hasKey = token.inventory.containsKey((KeyItem) nextTile.getDoorSymbol());
                                if(hasKey){
                                        lastTriedTile = null;
                                        nextTile.setDoorLocked(false);
                                        token.getInventory().useKey((KeyItem) nextTile.getDoorSymbol());
                                }else{
                                        // cant open door, player does not have key for door
                                        if(nextTile != lastTriedTile){ // if statement prevents spamming onPathBlocked
                                                lastTriedTile = nextTile;
                                                if (token.listener != null)
                                                        token.listener.onPathBlocked(nextLocation, nextTile);
                                        }

                                }
                        }else{
                                // cant interact with non key locked door, player needs to solve puzzle and door will become unlocked
                                if (token.listener != null)
                                        token.listener.onPathBlocked(nextLocation, nextTile);
                        }
                        return true;
                }
                lastTriedTile = null;
                return false;
        }

        private boolean useKeyDoubleTap(Pair nextLocation) {
                Tile nextTile = token.floorMap.getTile(nextLocation);
                if (nextTile.isDoor() && nextTile.isDoorLocked()) {
                        boolean key = false;
                        if (nextTile.getDoorSymbol() instanceof KeyItem) {
                                key = token.getInventory().containsKey((KeyItem) nextTile.getDoorSymbol());
                        }
                        if (token.getCommand().isUseKey() && nextTile == token.getCommand().getUseKeyOnTile()) {
                                //token.getTarget().setUseKey(false);
                                if (!key) {
                                        //Gdx.app.log("Move","Try to use key but do not have a key");
                                        //token.getCommand().setLocation(token.location); // cant open door no key stop trying to move in to the door its pointless
                                        //if(token.listener != null)
                                        //        token.listener.onPathBlocked(nextLocation, nextTile);
                                        return true;

                                } else {
                                        //Gdx.app.log("Move","Unlocking door");
                                        nextTile.setDoorLocked(false);
                                        token.getInventory().useKey((KeyItem) nextTile.getDoorSymbol());
                                        //token.getCommand().setLocation(token.location);
                                        return true;

                                }
                        } else {
                                //token.getCommand().setLocation(token.location); // cant open door no key stop trying to move in to the door its pointless
                                if (token.getCommand().canUseKeyOnTile == null) {
                                        //Gdx.app.log("Move","Ran in to locked door, but does not have open command, tile: "+token.getCommand().canUseKeyOnTile);
                                        token.getCommand().canUseKeyOnTile = nextTile;
                                        if (token.listener != null)
                                                token.listener.onPathBlocked(nextLocation, nextTile);
                                }


                                return true;

                        }
                }
                return false;
        }


        private void pickUpLoot() {
                if (!picksUpItems)
                        return;
                Array<Token> tokensAt = token.floorMap.getTokensAt(token.location);
                for (Token t : tokensAt) {
                        Loot loot = t.getLoot();
                        if (loot != null && loot.canbePickedUp()) {
                                boolean valid = token.getInventory().add(loot.getItem());
                                if (valid)
                                        loot.becomeRemoved();
                                //else
                                // TODO: show message saying inventory is full
                        }

                        // TODO: i may want to change the qualifier for activating the spike trap to be different form picksUpItems
                        // generally the trap should be activated by the player and his allies
                        SpikeTrap spikeTrap = t.get(SpikeTrap.class);
                        if (spikeTrap != null && spikeTrap.isHidden()) {
                                spikeTrap.setTriggered();
                        }
                }
        }

        public Vector2 getFloatLocation() {
                return floatLocation;
        }

        private float getLocationFloatX() {
                Direction direction = token.getDirection();
                if (moveU == 1 || direction == Direction.South || direction == Direction.North)
                        return token.location.x;
                else if (direction == Direction.East || direction == Direction.NorthEast || direction == Direction.SouthEast)
                        return MathUtils.lerp(token.location.x - 1, token.getLocation().x, moveU);
                else if (direction == Direction.West || direction == Direction.NorthWest || direction == Direction.SouthWest)
                        return MathUtils.lerp(token.location.x + 1, token.getLocation().x, moveU);
                throw new AssertionError("unexpected state");
        }

        private float getLocationFloatY() {
                Direction direction = token.getDirection();
                if (moveU == 1 || direction == Direction.West || direction == Direction.East)
                        return token.location.y;
                else if (direction == Direction.North || direction == Direction.NorthEast || direction == Direction.NorthWest)
                        return MathUtils.lerp(token.location.y - 1, token.location.y, moveU);
                else if (direction == Direction.South || direction == Direction.SouthEast || direction == Direction.SouthWest)
                        return MathUtils.lerp(token.location.y + 1, token.location.y, moveU);
                throw new AssertionError("unexpected state");
        }

        public boolean isMoving() {
                return moveU != 1;
        }

        public float getMoveSpeed() {
                return moveSpeed;
        }

        protected void setMoveSpeed(float moveSpeed) {
                this.moveSpeed = moveSpeed;
                // 0.70710678118 = sqrt(.5)
                this.moveSpeedDiagonal = this.moveSpeed * 0.70710678118f;

        }

        public boolean isPicksUpItems() {
                return picksUpItems;
        }

        public void setPicksUpItems(boolean picksUpItems) {
                this.picksUpItems = picksUpItems;
        }
}
