package asf.dungeon.model.floorgen.room;

import asf.dungeon.model.Sector;
import asf.dungeon.model.Symbol;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/4/2014.
 */
public class Room extends Sector {

        protected Array<Doorway> doorways = new Array<Doorway>(true, 2, Doorway.class);
        protected Symbol containsSymbol = null; // used by key spawner
        protected int containsStairsTo = -2;


        public Room(int x1, int y1, int x2, int y2) {
                super(x1, y1, x2, y2);
        }


        public boolean isGoalRoom(int floorIndex){
                return containsStairsTo >=0 && containsStairsTo > floorIndex;
        }

        public boolean isStartRoom(int floorIndex){
                return containsStairsTo >=-1 && containsStairsTo < floorIndex;
        }

        public boolean isDeadEnd(){
                return doorways.size <=1;
        }

        public float getIntensity(int floorIndex){
                float intensity = 0.5f;
                if(containsSymbol !=null) intensity+=containsSymbol.getIntensity();
                if(isStartRoom(floorIndex)) intensity -= 0.4f;
                if(isGoalRoom(floorIndex)) intensity -= 0.2f;
                if(isDeadEnd()) intensity += 0.2f;
                return intensity < 0f ? 0f : intensity;
        }


        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                if (!super.equals(o)) return false;

                Room room = (Room) o;

                if (containsStairsTo != room.containsStairsTo) return false;
                if (containsSymbol != null ? !containsSymbol.equals(room.containsSymbol) : room.containsSymbol != null) return false;
                if (!doorways.equals(room.doorways)) return false;

                return true;
        }

        @Override
        public int hashCode() {
                int result = super.hashCode();
                result = 31 * result + doorways.hashCode();
                result = 31 * result + (containsSymbol != null ? containsSymbol.hashCode() : 0);
                result = 31 * result + containsStairsTo;
                return result;
        }
}
