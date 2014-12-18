package asf.dungeon.model.floorgen.room;

import asf.dungeon.model.floorgen.Symbol;

/**
 * Created by Daniel Strong on 12/13/2014.
 */
public class Doorway {
        int x;
        int y;
        protected Symbol requiresSymbol = null;


        public Doorway(int x, int y) {
                this.x = x;
                this.y = y;
        }
}
