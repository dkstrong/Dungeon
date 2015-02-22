package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;

/**
 *
 *
 * Created by Daniel Strong on 1/2/2015.
 */
public interface TeleportListener {

        /**
         * modify the component with informaiton about its new location
         * @param fm
         * @param x
         * @param y
         * @param direction
         * @return
         */
        public void onTeleport(FloorMap fm, int x, int y, Direction direction);

}
