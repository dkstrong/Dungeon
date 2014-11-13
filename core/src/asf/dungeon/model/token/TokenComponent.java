package asf.dungeon.model.token;

import asf.dungeon.model.Direction;

import java.io.Serializable;

/**
 * Created by Danny on 11/11/2014.
 */
public interface TokenComponent {

        /**
         * return true if this component allows teleportation to this location
         * @param x
         * @param y
         * @param direction
         * @return
         */
        public boolean teleportToLocation(int x, int y, Direction direction);

        /**
         * return true if this component consumes the update and does not let the lower comonents update
         * @param delta
         * @return
         */
        public boolean update(float delta);
}
