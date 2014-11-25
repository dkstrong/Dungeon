package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.item.KeyItem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/11/2014.
 */
public class Move implements TokenComponent{
        private final Token token;
        private float moveSpeed = 1.5f; // how fast the character moves between tiles, generally a value between 1 and 10, could be higher i suppose.
        private float moveSpeedDiagonal = 1.06066017177f;
        private boolean picksUpItems = true; // if this character will pick up items when standing on tiles with items
        private final Array<Pair> path = new Array<Pair>(true, 32, Pair.class);   // the current path that this token is trying to follow, it may be regularly refreshed with new paths
        private final Pair pathedTarget = new Pair();           // the last element of path. this is compared against continuesMoveTarget to prevent spamming the pathfinder coder

        private final Pair continuousMoveTokenLastLoc = new Pair();
        private float continuousMoveTokenLostVisionCountdown = 0;
        protected float moveU = 1;                                // 1 = fully on the location, less then 1 means moving on to the location still even though it occupies it, use direction variable to determine which way token is walking towards the location

        public Move(Token token) {
                this.token = token;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {
                moveU = 1;
                path.clear();
                pathedTarget.set(x,y);

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

                        if (fogMap == null || fogMap.isVisible(targetToken.getLocation().x, targetToken.getLocation().y)) {
                                targetLocation.set(targetToken.getLocation());
                                continuousMoveTokenLastLoc.set(targetToken.getLocation());
                                continuousMoveTokenLostVisionCountdown = 1;
                        } else {
                                targetLocation.set(continuousMoveTokenLastLoc);
                                continuousMoveTokenLostVisionCountdown -= delta;
                                if (continuousMoveTokenLostVisionCountdown < 0){
                                        token.getCommand().setTargetToken(null); // lost sight for over a second, no longer chasing
                                        targetToken = null;
                                }

                        }

                        if(targetToken != null){
                                Damage moveTokenDamage = targetToken.get(Damage.class);
                                if(moveTokenDamage!= null){
                                        if(moveTokenDamage.isDead()){
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
                                                token.direction = newDirection;
                                        location.set(nextLocation);
                                        moveU -= 1;
                                        if(token.getFogMapping() != null)
                                                token.getFogMapping().computeFogMap();

                                } else {

                                        // path is blocked, will attempt to attack what is blocking the path
                                        // if can not attack (due to colldown or some other reason) then the token will just kind of chill
                                        moveU = 1;

                                        //if (!token.isInteracting()) {
                                                Direction newDirection = Direction.getDirection(location, nextLocation);
                                                if (newDirection != null)
                                                        token.direction = newDirection;
                                        //}

                                        boolean action = useKey(nextLocation);
                                        if(!action)
                                                token.getAttack().attackTargetInDirection(delta); // auto attack anything in front of me, do not do ranged attack

                                }

                        } else if (path.size == 1) {
                                // idle
                                path.clear();
                                pathedTarget.set(location);
                                Tile tile = floorMap.getTile(location);
                                if (tile.isStairs() && tile.getStairsTo() >=0) {
                                        token.dungeon.moveToken(token, token.dungeon.generateFloor(tile.getStairsTo()));
                                        //token.teleportToFloor(tile.getStairsTo());
                                        return true;
                                }
                                moveU = 1;
                        }else{
                                moveU = 1;
                        }
                }
                return false;
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

                boolean foundPath = token.floorMap.computePath(new Pair(token.location), new Pair(targetLocation), path);
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
                                Direction newDir = Direction.getDirection(path.get(0), nextLocation);  // should this be locatioin,nextlocation?
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
                                        if(token.getFogMapping() != null)
                                                token.getFogMapping().computeFogMap();

                                        // attackCoolDown == attackCooldownDuration;  // i could do this here to punish making uturns, and give an advantage for coming up from behind
                                }
                        }


                }
        }


        private boolean useKey(Pair nextLocation){
                Tile nextTile = token.floorMap.getTile(nextLocation);
                if(nextTile.isDoor() && nextTile.isDoorLocked()){
                        KeyItem key = token.getInventory().getKeyItem(nextTile.getKeyType());
                        if(token.getCommand().isUseKey() && nextTile == token.getCommand().getUseKeyOnTile()){
                                //token.getTarget().setUseKey(false);
                                if(key == null){
                                        //Gdx.app.log("Move","Try to use key but do not have a key");
                                        token.getCommand().setLocation(token.location); // cant open door no key stop trying to move in to the door its pointless
                                        if(token.listener != null)
                                                token.listener.onPathBlocked(nextLocation, nextTile);
                                        return true;

                                }else{
                                        //Gdx.app.log("Move","Unlocking door");
                                        nextTile.setDoorLocked(false);
                                        token.getInventory().useKey(key);
                                        //token.getCommand().setLocation(token.location);
                                        return true;

                                }
                        }else{
                                //Gdx.app.log("Move","Ran in to locked door, but does not have open command");
                                token.getCommand().setLocation(token.location); // cant open door no key stop trying to move in to the door its pointless
                                token.getCommand().canUseKeyOnTile = key != null ? nextTile : null;
                                if(token.listener != null)
                                        token.listener.onPathBlocked(nextLocation, nextTile);
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
                        Loot loot = t.get(Loot.class);
                        if(loot != null){
                                if(!loot.isRemoved()){
                                        boolean valid = token.getInventory().add(loot.getItem());
                                        if(valid)
                                                loot.becomeRemoved();
                                        //else
                                        // TODO: show message saying inventory is full
                                }
                        }
                }
        }

        public float getLocationFloatX() {
                Direction direction = token.getDirection();
                if (moveU==1 || direction == Direction.South || direction == Direction.North)
                        return token.getLocation().x;
                else if (direction == Direction.East || direction == Direction.NorthEast || direction == Direction.SouthEast)
                        return MathUtils.lerp(token.getLocation().x - 1, token.getLocation().x, moveU);
                else if (direction == Direction.West || direction==Direction.NorthWest || direction == Direction.SouthWest)
                        return MathUtils.lerp(token.getLocation().x + 1, token.getLocation().x, moveU);
                throw new AssertionError("unexpected state");
        }

        public float getLocationFloatY() {
                Direction direction = token.getDirection();
                if (moveU==1 || direction == Direction.West || direction == Direction.East)
                        return token.getLocation().y;
                else if (direction == Direction.North || direction == Direction.NorthEast || direction == Direction.NorthWest)
                        return MathUtils.lerp(token.getLocation().y - 1, token.getLocation().y, moveU);
                else if (direction == Direction.South || direction == Direction.SouthEast || direction == Direction.SouthWest)
                        return MathUtils.lerp(token.getLocation().y + 1, token.getLocation().y, moveU);
                throw new AssertionError("unexpected state");
        }

        public boolean isMoving(){
                return moveU != 1;
        }

        public float getMoveSpeed() {
                return moveSpeed;
        }

        protected void setMoveSpeed(float moveSpeed){
                this.moveSpeed = moveSpeed;
                // 0.70710678118 = sqrt(.5)
                this.moveSpeedDiagonal = this.moveSpeed*0.70710678118f;

        }
}
