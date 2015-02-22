package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;

/**
 * TokenComponents that implement this interface can define rules
 * for where the token can/should teleport to
 *
 * Created by Daniel Strong on 1/2/2015.
 */
public interface TeleportValidator {

        /**
         * return true if this component allows teleporting ot this location
         * @param fm
         * @param x
         * @param y
         * @param direction
         * @return
         */
        public boolean canTeleport(FloorMap fm, int x, int y, Direction direction);


}
