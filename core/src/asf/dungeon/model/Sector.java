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

        public boolean contains(int x, int y){ return x >=x1 && x<=x2 && y>=y1 && y<=y2; }

        public int getRandomX(DungeonRand rand){ return rand.range(x1,x2); }

        public int getRandomY(DungeonRand rand){ return rand.range(y1,y2); }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Sector sector = (Sector) o;

                if (x1 != sector.x1) return false;
                if (x2 != sector.x2) return false;
                if (y1 != sector.y1) return false;
                if (y2 != sector.y2) return false;

                return true;
        }

        @Override
        public int hashCode() {
                int result = x1;
                result = 31 * result + y1;
                result = 31 * result + x2;
                result = 31 * result + y2;
                return result;
        }
}
