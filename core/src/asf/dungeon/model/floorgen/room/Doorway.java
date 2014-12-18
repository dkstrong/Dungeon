package asf.dungeon.model.floorgen.room;

import asf.dungeon.model.item.KeyItem;

/**
 * Created by Daniel Strong on 12/13/2014.
 */
public class Doorway {
        int x;
        int y;
        protected KeyItem.Type requiresKey = null;


        public Doorway(int x, int y) {
                this.x = x;
                this.y = y;
        }
}
