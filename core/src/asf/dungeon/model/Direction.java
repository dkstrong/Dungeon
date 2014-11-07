package asf.dungeon.model;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;

/**
 * Created by danny on 10/22/14.
 */
public enum Direction {
        North(new Quaternion().setFromAxisRad(0, 1, 0, 3.1459f), Input.Keys.UP), // 180
        South(new Quaternion().setFromAxisRad(0, 1, 0, 0), Input.Keys.DOWN),  // 0
        East(new Quaternion().setFromAxisRad(0, 1, 0, 1.5708f), Input.Keys.RIGHT),  // 90
        West(new Quaternion().setFromAxisRad(0, 1, 0, 4.71239f), Input.Keys.LEFT); // 270

        public final Quaternion quaternion;
        public final int keyCode;

        Direction(Quaternion quaternion, int keyCode) {
                this.quaternion = quaternion;
                this.keyCode = keyCode;
        }

        public boolean isOpposite(Direction dir) {
                switch (dir) {
                        case North:
                                return this == South;
                        case South:
                                return this == North;
                        case East:
                                return this == West;
                        case West:
                                return this == East;
                }

                throw new IllegalArgumentException(dir + "");
        }

        /**
         * determines if the angle indicated by "from" and "to" are within +- 89 degrees of this direction
         *
         * this check is similiar to comparing with getDirection(), however this method will consider
         * diagonal angles and can validate directions that are for example both North and East
         *
         * @param from
         * @param to
         * @return true if the supplied angle represents this direction
         */
        public boolean isDirection(Pair from, Pair to){
                if(this == East)
                        return to.x > from.x;
                else if(this == West)
                        return to.x < from.x;
                else if(this == North)
                        return to.y > from.y;
                else if(this == South)
                        return to.y < from.y;
                return false;
        }

        public static Direction getDirection(Pair from, Pair to) {
                if (to.x > from.x)
                        return Direction.East;
                else if (to.x < from.x)
                        return Direction.West;
                else if (to.y > from.y)
                        return Direction.North;
                else if (to.y < from.y)
                        return Direction.South;
                return null;
        }

        public static Direction getDirection(int keycode){
                if(keycode == Input.Keys.UP){
                        return Direction.North;
                }else if(keycode == Input.Keys.DOWN){
                        return Direction.South;
                }else if(keycode == Input.Keys.LEFT){
                        return Direction.West;
                }else if(keycode == Input.Keys.RIGHT){
                        return Direction.East;
                }
                return null;
        }

        public static Direction random() {
                int i = MathUtils.random.nextInt(4);
                if (i == 0)
                        return Direction.North;
                else if (i == 1)
                        return Direction.South;
                else if (i == 2)
                        return Direction.East;
                else
                        return Direction.West;
        }


}
