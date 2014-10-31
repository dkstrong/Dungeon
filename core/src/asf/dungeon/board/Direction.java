package asf.dungeon.board;

import com.badlogic.gdx.math.Quaternion;
import asf.dungeon.board.pathfinder.Pair;

/**
 * Created by danny on 10/22/14.
 */
public enum Direction {
        North(new Quaternion().setFromAxisRad(0, 1, 0, 3.1459f)), // 180
        South(new Quaternion().setFromAxisRad(0,1,0, 0)),  // 0
        East(new Quaternion().setFromAxisRad(0,1,0, 1.5708f)),  // 90
        West(new Quaternion().setFromAxisRad(0,1,0, 4.71239f)); // 270

        public final Quaternion quaternion;

        Direction(Quaternion quaternion) {
                this.quaternion = quaternion;
        }

        public boolean isOpposite(Direction dir){
                switch(dir){
                        case North:
                                return this == South;
                        case South:
                                return this==North;
                        case East:
                                return this == West;
                        case West:
                                return this==East;
                }

                throw new IllegalArgumentException(dir+"");
        }

        public static Direction getDirection(Pair from, Pair to){
                if(to.x > from.x)
                        return Direction.East;
                else if(to.x < from.x)
                        return Direction.West;
                else if(to.y > from.y)
                        return Direction.North;
                else if(to.y < from.y)
                        return Direction.South;
                return null;
        }


}
