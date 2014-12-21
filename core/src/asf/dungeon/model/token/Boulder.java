package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * A boulder that can be pushed when Move goes in to its place
 * Created by Daniel Strong on 12/17/2014.
 */
public class Boulder implements TokenComponent {
        private Token token;
        private float moveSpeed = 1.5f;
        private float moveSpeedDiagonal = 1.06066017177f;
        private float moveU = 1;
        private Vector2 floatLocation = new Vector2();

        public Boulder(Token token) {
                this.token = token;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {
                moveU = 1;
                floatLocation.set(x, y);
        }

        @Override
        public boolean update(float delta) {
                if (moveU >= 1) return false;
                moveU += delta;
                if (moveU > 1)
                        moveU = 1;
                updateFloatLocation();
                return true;
        }

        private void setMoveSpeed(float moveSpeed) {
                this.moveSpeed = moveSpeed;
                // 0.70710678118 = sqrt(.5)
                this.moveSpeedDiagonal = this.moveSpeed * 0.70710678118f;

        }


        protected void push(Token pushedBy) {
                if (moveU != 1) return;
                Direction pushDir = pushedBy.location.direction(token.location);
                Pair newLoc = new Pair();
                // Attempt to push the boulder forward, then left, then right, if none of these directions work then it is stuck
                setMoveSpeed(pushedBy.getMove().getMoveSpeed());

                if(!token.floorMap.isLocationBlocked(newLoc.set(token.location).addFree(pushDir))){
                        moveU = 0;
                        token.location.set(newLoc);
                        token.direction = pushDir;
                }else if(!token.floorMap.isLocationBlocked(newLoc.set(token.location).addFree(pushDir.rotate(90)))){
                        moveU = 0;
                        token.location.set(newLoc);
                        token.direction = pushDir.rotate(90);
                }else if(!token.floorMap.isLocationBlocked(newLoc.set(token.location).addFree(pushDir.rotate(-90)))){
                        moveU = 0;
                        token.location.set(newLoc);
                        token.direction = pushDir.rotate(-90);
                }

        }

        public Vector2 getFloatLocation() {
                return floatLocation;
        }

        private void updateFloatLocation() {
                floatLocation.set(getLocationFloatX(), getLocationFloatY());
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




}
