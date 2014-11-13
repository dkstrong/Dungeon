package asf.dungeon.model;


import java.io.Serializable;

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

        public void set(int x, int  y){
                this.x=x;
                this.y=y;
        }

        public int getX() {
                return x;
        }

        public void setX(int x) {
                this.x = x;
        }

        public int getY() {
                return y;
        }

        public void setY(int y) {
                this.y = y;
        }

        public int distance(Pair targetLocation) {
                return distance(targetLocation.getX(), targetLocation.getY());
        }

        public int distance(int targetX, int targetY) {
                int xDistance = Math.abs(targetX - x);
                int yDistance = Math.abs(targetY - y);
                return xDistance + yDistance;
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
                }
                return this;
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

        public Pair set(Pair pair) {
                x = pair.x;
                y =pair.y;
                return this;
        }
}
