package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;

/**
 * Created by Danny on 11/30/2014.
 */
public class InteractChat implements Interact {
        private String message;


        @Override
        public boolean interact(Token sourceToken) {
                if(sourceToken.listener != null)
                        sourceToken.listener.onInteract(this);
                return true;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {

        }

        @Override
        public boolean update(float delta) {
                return false;
        }

        public String getMessage() {
                return message;
        }

        public void setMessage(String message) {
                this.message = message;
        }
}
