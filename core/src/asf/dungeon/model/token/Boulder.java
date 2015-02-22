package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * A boulder that can be pushed when Move goes in to its place
 * Created by Daniel Strong on 12/17/2014.
 */
public class Boulder implements TokenComponent , TeleportValidator, TeleportListener {
        protected Token token;
        private float moveSpeed = 1.5f;
        private float moveSpeedDiagonal = 1.06066017177f;
        private float moveU = 1;
        private Vector2 floatLocation = new Vector2();
        private Tile fillsPit;

        public Boulder(Token token) {
                this.token = token;
        }

        @Override
        public boolean isGoodSpawnLocation(FloorMap fm, int x, int y, Direction dir) {
                return true;
        }

        @Override
        public boolean canTeleport(FloorMap fm, int x, int y, Direction direction){
                if(fillsPit != null){
                        Array<Token> tokensAt = token.floorMap.getTokensAt(token.location);
                        for (Token t : tokensAt) {
                                if(t!=token)
                                        return false; // a token is standing on top of this boulder, it cant be teleported
                        }
                }

                return true;
        }

        @Override
        public void onTeleport(FloorMap fm, int x, int y, Direction direction) {
                moveU = 1;
                floatLocation.set(x, y);

                if(fillsPit != null)
                        fillsPit.setPitFilled(false);

                Tile tile = token.floorMap.getTile(token.location);
                if(tile.isPit() && !tile.isPitFilled()){
                        tile.setPitFilled(true);
                        fillsPit = tile;
                }else{
                        fillsPit = null;
                }
                token.blocksPathing = fillsPit == null;
        }

        @Override
        public boolean update(float delta) {
                if (moveU >= 1) return false;
                moveU += delta * (token.direction.isDiagonal() ? moveSpeedDiagonal : moveSpeed);
                if (moveU > 1){
                        moveU = 1;
                        Tile tile = token.floorMap.getTile(token.location);
                        if(tile.isPit() && !tile.isPitFilled()){
                                tile.setPitFilled( true);
                                fillsPit = tile;
                                token.blocksPathing = false;
                        }
                }

                updateFloatLocation();
                return true;
        }

        private void setMoveSpeed(float moveSpeed) {
                this.moveSpeed = moveSpeed;
                // 0.70710678118 = sqrt(.5)
                this.moveSpeedDiagonal = this.moveSpeed * 0.70710678118f;

        }

        protected boolean push(Token pushedBy) {
                if (moveU != 1) return false;
                if(fillsPit != null) return false;
                Direction pushDir = pushedBy.location.direction(token.location);
                Pair newLoc = new Pair();
                // Attempt to push the boulder forward, then left, then right, if none of these directions work then it is stuck
                setMoveSpeed(pushedBy.move.getMoveSpeed());

                if(!isLocationBlocked(newLoc.set(token.location).addFree(pushDir))){
                        moveU = 0;
                        token.location.set(newLoc);
                        token.direction = pushDir;
                        return true;
                }else if(isLocationBlocked(newLoc.set(token.location).addFree(pushDir.rotate(-90))) &&
                        !isLocationBlocked(newLoc.set(token.location).addFree(pushDir.rotate(90)))){
                        moveU = 0;
                        token.location.set(newLoc);
                        token.direction = pushDir.rotate(90);
                        return true;
                }else if(
                        isLocationBlocked(newLoc.set(token.location).addFree(pushDir.rotate(90))) &&
                        !isLocationBlocked(newLoc.set(token.location).addFree(pushDir.rotate(-90)))){
                        moveU = 0;
                        token.location.set(newLoc);
                        token.direction = pushDir.rotate(-90);
                        return true;
                }
                return false;
        }

        private boolean isLocationBlocked(Pair location){
                Tile tile = token.floorMap.getTile(location);
                if(tile == null || (!tile.isPit() && tile.blockMovement))
                        return true;

                for (Token t : token.floorMap.getTokens()) {
                        if (t.blocksPathing && t.isLocatedAt(location))
                                return true;
                }

                Tile t = token.floorMap.getTile(location);
                if(t.isDoor() && !t.isDoorOpened())
                        return true;

                return false;
        }

        public Vector2 getFloatLocation() {
                return floatLocation;
        }

        private void updateFloatLocation() {
                floatLocation.set(getLocationFloatX(), getLocationFloatY());
        }

        private float getLocationFloatX() {
                Direction direction = token.direction;
                if (moveU == 1 || direction == Direction.South || direction == Direction.North)
                        return token.location.x;
                else if (direction == Direction.East || direction == Direction.NorthEast || direction == Direction.SouthEast)
                        return MathUtils.lerp(token.location.x - 1, token.location.x, moveU);
                else if (direction == Direction.West || direction == Direction.NorthWest || direction == Direction.SouthWest)
                        return MathUtils.lerp(token.location.x + 1, token.location.x, moveU);
                throw new AssertionError("unexpected state");
        }

        private float getLocationFloatY() {
                Direction direction = token.direction;
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

        public boolean isFillsPit(){return fillsPit != null;}




}
