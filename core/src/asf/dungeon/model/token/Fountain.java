package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;

/**
 * Created by Danny on 12/1/2014.
 */
public class Fountain implements TokenComponent {
        private boolean consumed = false;
        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {

        }

        @Override
        public boolean update(float delta) {
                return false;
        }

        public boolean isConsumed() {
                return consumed;
        }

        public void setConsumed(boolean consumed) {
                this.consumed = consumed;
        }
}
