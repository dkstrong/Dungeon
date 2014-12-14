package asf.dungeon.model.floorgen.room;

import asf.dungeon.model.item.KeyItem;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Daniel Strong on 12/13/2014.
 */
public class Doorway {
        int x;
        int y;
        protected Room start;
        protected boolean lockable; // if the door is not lockable this means its actually a floor tile, the layout of the room is preventing the doorway from actually containing a door
        protected Array<Room> destinations =new Array<Room>(true, 4, Room.class);
        protected KeyItem.Type requiresKey = null;


        public Doorway(int x, int y, Room start, boolean lockable) {
                this.x = x;
                this.y = y;
                this.start = start;
                this.lockable = lockable;
        }
}
