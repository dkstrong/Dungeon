package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.item.Item;

/**
* Created by Daniel Strong on 12/8/2014.
*/
public class CrateInventory implements Inventory {
        private Token token;
        private Item item;

        public CrateInventory(Token token, Item item) {
                this.token = token;
                this.item = item;
        }

        public Item getItemToDrop() {
                return item;
        }

        public int size() {
                return item == null ? 0 : 1;
        }

        public boolean isFull() {
                return item != null;
        }

        public boolean add(Item item) {
                if (isFull())
                        return false;
                this.item = item;

                if (token.listener != null)
                        token.listener.onInventoryChanged();
                return true;
        }

        public boolean drop(Item item){
                boolean valid = discard(item);
                if(!valid) return false;

                token.dungeon.newLootToken(token.getFloorMap(), item, token.getLocation().x, token.getLocation().y);

                return true;
        }

        public boolean discard(Item item) {
                if (this.item != item || item == null)
                        return false;
                this.item = null;
                if (token.listener != null)
                        token.listener.onInventoryChanged();
                return true;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {

        }

        @Override
        public boolean update(float delta) {
                return false;
        }
}