package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;

/**
 * A Teleportable is a TokenComponent that is affected by Teleportation. It can prevent teleportation to a certain location and it is affected by teleprotation.
 *
 * Created by Daniel Strong on 1/2/2015.
 */
public interface Teleportable {

        /**
         * return true if this component allows teleporting ot this location
         * @param fm
         * @param x
         * @param y
         * @param direction
         * @return
         */
        public boolean canTeleport(FloorMap fm, int x, int y, Direction direction);

        /**
         * modify the component with informaiton about its new location
         * @param fm
         * @param x
         * @param y
         * @param direction
         * @return
         */
        public void teleport(FloorMap fm, int x, int y, Direction direction);

}
