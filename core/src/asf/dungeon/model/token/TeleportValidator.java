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
         * used for finding ideal locations to spawn this token, it tries
         * to avoid spawning too close to doors, and spawning on other tokens.
         *
         * @param fm
         * @param x
         * @param y
         * @param dir
         * @return
         */
        public boolean isGoodSpawnLocation(FloorMap fm, int x, int y, Direction dir);

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
