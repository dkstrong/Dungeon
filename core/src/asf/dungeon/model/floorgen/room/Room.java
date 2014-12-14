package asf.dungeon.model.floorgen.room;

import asf.dungeon.model.Sector;
import asf.dungeon.model.item.KeyItem;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/4/2014.
 */
public class Room extends Sector {

        protected Array<Doorway> doorways = new Array<Doorway>(true, 4, Doorway.class);
        protected KeyItem.Type containsKey = null; // used by key spawner
        protected int containsStairsTo = -2;
        protected float difficulty  =0f;


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





}
