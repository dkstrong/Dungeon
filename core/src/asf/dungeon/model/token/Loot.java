package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.item.Item;

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
        public void teleport(FloorMap fm, int x, int y, Direction direction) {

        }

        @Override
        public boolean update(float delta) {
                if(removed )
                        token.dungeon.removeToken(token);
                return false;
        }

        protected void becomeRemoved(){
                removed = true;
                item = null;
        }

        public Item getItem(){
                return item;
        }
}
