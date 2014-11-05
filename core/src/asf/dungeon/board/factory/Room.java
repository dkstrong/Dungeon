package asf.dungeon.board.factory;

import asf.dungeon.board.Pair;

/**
 * Created by Danny on 11/4/2014.
 */
public class Room {

        int x1,y1,x2,y2;

        Room(int x1, int y1, int x2, int y2) {
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;
        }

        void set(int x1, int y1, int x2, int y2) {
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;
        }

        public int getCenterX(){
                return (x1+x2)/2;
        }

        int getCenterY(){
                return (y1+y2)/2;
        }

        boolean intersects(Room room){
                return (x1 <= room.x2 && x2 >= room.x1 && y1 <= room.y2 && y2 >= room.y1);
        }
}
