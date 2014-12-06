package asf.dungeon.model;


/**
 * Created by danny on 10/21/14.
 */
public class Pair  {
        public int x;
        public int y;

        public Pair() {

        }
        public Pair(Pair copy){
                x = copy.x;
                y = copy.y;
        }

        public Pair(int x, int y) {
                this.x = x;
                this.y = y;
        }

        public Pair set(int x, int  y){
                this.x=x;
                this.y=y;
                return this;
        }
        public Pair set(Pair pair) {
                x = pair.x;
                y =pair.y;
                return this;
        }

        public Direction direction(Pair to){
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

        public int distance(Pair targetLocation) {
                return distance(targetLocation.x, targetLocation.y);
        }

        /**
         * distance between these two tile locations
         * note that distance is determind as number of moves to go from location A to location B with diagonal turned on
         *
         * this is often a different distance value than what is conventionally known as "distance"
         * @param targetX
         * @param targetY
         * @return
         */
        public int distance(int targetX, int targetY) {
                int xDistance = Math.abs(targetX - x);
                int yDistance = Math.abs(targetY - y);
                return xDistance+yDistance;
        }

        public Pair add(Direction dir, int scale){
                if(scale ==0 || dir == null) return this;
                if(scale <0){
                        dir = dir.opposite();
                        scale*=-1;
                }

                for(int i=0; i < scale; i++)
                        add(dir);

                return this;
        }

        public Pair add(Direction dir){
                if(dir == null){
                        return this;
                }
                switch(dir){
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

        public boolean equals(int x, int y){
                return this.x == x && this.y==y;
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
                return "{"+x+", "+y+"}";
        }


}
