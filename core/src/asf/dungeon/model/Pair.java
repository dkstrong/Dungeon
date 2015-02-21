package asf.dungeon.model;


import com.badlogic.gdx.math.MathUtils;

/**
 * bassically a Vector2 but uses integers. Useful for representing coordinates
 * on a board
 * Created by danny on 10/21/14.
 */
public class Pair {
        public int x;
        public int y;

        public Pair() {

        }

        public Pair(Pair copy) {
                x = copy.x;
                y = copy.y;
        }

        public Pair(int x, int y) {
                this.x = x;
                this.y = y;
        }

        public Pair set(int x, int y) {
                this.x = x;
                this.y = y;
                return this;
        }

        public Pair set(Pair pair) {
                x = pair.x;
                y = pair.y;
                return this;
        }

        public Pair rotate(float degrees){
                float radians = degrees * MathUtils.degreesToRadians;
                float cos = (float)Math.cos(radians);
                float sin = (float)Math.sin(radians);

                float newX = this.x * cos - this.y * sin;
                float newY = this.x * sin + this.y * cos;

                this.x = Math.round(newX);
                this.y = Math.round(newY);

                return this;
        }

        public Pair rotate(float degrees, int originX, int originY){
                float radians = degrees * MathUtils.degreesToRadians;
                float cos = (float)Math.cos(radians);
                float sin = (float)Math.sin(radians);

                float newX = cos * (x-originX) - sin * (y-originY) + originX;
                float newY = sin * (x-originX) + cos * (y-originY) + originY;

                this.x = Math.round(newX);
                this.y = Math.round(newY);

                return this;
        }

        public Pair rotate(Direction currentDirection, Direction targetDirection){
                int degrees = targetDirection.degrees - currentDirection.degrees;
                if(degrees == 0)
                        return this;
                else if(degrees == 90 || degrees == -270){
                        int xTemp = this.x;
                        this.x = -y;
                        y = xTemp;
                        return this;
                } else if(degrees == 180 || degrees == -180){
                        int xTemp = this.x;
                        this.x = -y;
                        y = -xTemp;
                        return this;
                } else if(degrees == -90 || degrees == 270){
                        int xTemp = this.x;
                        this.x = y;
                        y = -xTemp;
                        return this;
                }else{
                        // TODO: this should also handle 45 degree increments
                        // 45 degree increments wont work the way expected rotation works
                        // and does it in more of a manhattany way.
                        return rotate(degrees);
                }
        }

        public Direction direction(Pair to) {
                Pair from = this;
                if (to.x > from.x) {
                        if (to.y > from.y)
                                return Direction.NorthEast;
                        else if (to.y < from.y)
                                return Direction.SouthEast;
                        return Direction.East;
                } else if (to.x < from.x) {
                        if (to.y > from.y)
                                return Direction.NorthWest;
                        else if (to.y < from.y)
                                return Direction.SouthWest;
                        return Direction.West;
                } else if (to.y > from.y)
                        return Direction.North;
                else if (to.y < from.y)
                        return Direction.South;
                return null;
        }

        /**
         * distance between these two tile locations
         * note that distance is determined as  the number of moves to go from location A to location B with diagonal turned OFF
         * Sometimes called manhattan distance
         *
         * this is often a different distance value than what is conventionally known as "distance"
         *
         * @param targetLocation
         * @return
         */
        public int distance(Pair targetLocation) {
                return distance(targetLocation.x, targetLocation.y);
        }

        /**
         * distance between these two tile locations
         * note that distance is determined as  the number of moves to go from location A to location B with diagonal turned OFF
         * Sometimes called manhattan distance
         *
         * this is often a different distance value than what is conventionally known as "distance"
         *
         * @param targetX
         * @param targetY
         * @return
         */
        public int distance(int targetX, int targetY) {
                int xDistance = Math.abs(targetX - x);
                int yDistance = Math.abs(targetY - y);
                return xDistance + yDistance;
        }

        /**
         * distance between two tiles locations
         * this distance is determined as the number of moves to go from location A to location B with daigonal turned ON
         *
         * thi is often a different distance value from what is conventionally known as "distnace"
         * @param targetX
         * @param targetY
         * @return
         */
        public int distanceFree(int targetX, int targetY){
                int xDistance = Math.abs(targetX - x);
                int yDistance = Math.abs(targetY - y);
                return xDistance < yDistance ? xDistance : yDistance;
        }

        public static int distance(int startX, int startY, int targetX, int targetY){
                int xDistance = Math.abs(targetX - startX);
                int yDistance = Math.abs(targetY - startY);
                return xDistance + yDistance;
        }

        public static int distanceFree(int startX, int startY, int targetX, int targetY){
                int xDistance = Math.abs(targetX - startX);
                int yDistance = Math.abs(targetY - startY);
                return xDistance > yDistance ? xDistance : yDistance;
        }

        /**
         * adds directions to this coordinate with the rules of manhatten distance (going diagonal takes two moves).
         *
         * @param dir
         * @param scale
         * @param horizontalFirst
         * @return
         */
        public Pair multAdd(Direction dir, int scale, boolean horizontalFirst) {
                if(scale ==0) return this;
                if (scale < 0) {
                        dir = dir.opposite();
                        scale *= -1;
                }

                while(scale >=2){
                        add(dir,horizontalFirst);
                        add(dir,!horizontalFirst);
                        scale-=2;
                }
                if(scale == 1)
                        add(dir, horizontalFirst);
                return this;
        }



        /**
         * adds a direction to a location, but diagonals take 1 move instead of 2
         *
         * @param dir
         * @param scale
         * @return
         */
        public Pair multAddFree(Direction dir, int scale) {
                if (scale == 0 || dir == null) return this;
                if (scale < 0) {
                        dir = dir.opposite();
                        scale *= -1;
                }

                for (int i = 0; i < scale; i++)
                        addFree(dir);

                return this;
        }

        /**
         * adds directions to this coordinate with the rules of manhatten distance (going diagonal takes two moves).
         *
         * note that going diagonal can look like going horizontal or vertical when using this method
         * @param dir
         * @param horizontalFirst
         * @return
         */
        public Pair add(Direction dir, boolean horizontalFirst){
                if (dir == null) {
                        return this;
                }
                switch (dir) {
                        case North:
                                y++;
                                break;
                        case South:
                                y--;
                                break;
                        case East:
                                x++;
                                break;
                        case West:
                                x--;
                                break;
                        case NorthEast:
                                if(horizontalFirst) x++;
                                else y++;
                                break;
                        case NorthWest:
                                if(horizontalFirst) x--;
                                else y++;
                                break;
                        case SouthEast:
                                if(horizontalFirst) x++;
                                else y--;
                                break;
                        case SouthWest:
                                if(horizontalFirst) x--;
                                else y--;
                                break;
                }
                return this;
        }

        /**
         * adds a direction to a location, but diagonals take 1 move instead of 2
         *
         * @param dir
         * @return
         */
        public Pair addFree(Direction dir) {
                if (dir == null) {
                        return this;
                }
                switch (dir) {
                        case North:
                                y++;
                                break;
                        case South:
                                y--;
                                break;
                        case East:
                                x++;
                                break;
                        case West:
                                x--;
                                break;
                        case NorthEast:
                                y++;
                                x++;
                                break;
                        case NorthWest:
                                y++;
                                x--;
                                break;
                        case SouthEast:
                                y--;
                                x++;
                                break;
                        case SouthWest:
                                y--;
                                x--;
                                break;
                }
                return this;
        }

        public boolean equals(int x, int y) {
                return this.x == x && this.y == y;
        }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Pair pair = (Pair) o;

                if (x != pair.x) return false;
                if (y != pair.y) return false;

                return true;
        }

        @Override
        public int hashCode() {
                int result = x;
                result = 31 * result + y;
                return result;
        }

        @Override
        public String toString() {
                return "{" + x + ", " + y + "}";
        }


}
