package asf.dungeon.model;

/**
 * Created by Danny on 11/20/2014.
 */
public class Sector {
        public int x1, y1, x2, y2;

        public Sector(int x1, int y1, int x2, int y2) {
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;
        }


        public void set(int x1, int y1, int x2, int y2) {
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;
        }

        public int getWidth() {
                return x2 - x1;
        }

        public int getHeight() {
                return y2 - y1;
        }

        public int getCenterX() {
                return (x1 + x2) / 2;
        }

        public int getCenterY() {
                return (y1 + y2) / 2;
        }

        public boolean intersects(Sector sector) {
                return (x1 <= sector.x2 && x2 >= sector.x1 && y1 <= sector.y2 && y2 >= sector.y1);
        }

        public boolean contains(Pair loc){ return contains(loc.x,loc.y); }

        public boolean contains(int x, int y){ return x >=x1 && x<=x2 && y>=y1 && y>=y2; }

        public int getRandomX(DungeonRand rand){ return rand.range(x1,x2); }

        public int getRandomY(DungeonRand rand){ return rand.range(y1,y2); }

}
