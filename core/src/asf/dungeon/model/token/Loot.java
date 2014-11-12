package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Item;

/**
 * Created by Danny on 11/11/2014.
 */
public class Loot implements TokenComponent{
        private final Token token;
        private boolean removed = false;

        private Item item;

        public Loot(Token token, Item item) {
                this.token = token;
                this.item = item;
                token.setBlocksPathing(false);
        }

        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                return true;
        }

        @Override
        public boolean update(float delta) {
                if(removed )
                        token.dungeon.removeToken(token);
                return false;
        }

        protected void becomeRemoved(){
                removed = true;
        }

        public Item getItem(){
                return item;
        }
}
