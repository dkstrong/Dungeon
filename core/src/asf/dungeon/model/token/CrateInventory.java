package asf.dungeon.model.token;

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

        @Override
        public Item getItemToDrop() {
                return item;
        }

        @Override
        public int size() {
                return item == null ? 0 : 1;
        }

        @Override
        public boolean isFull() {
                return item != null;
        }

        @Override
        public boolean add(Item item) {
                if (isFull())
                        return false;
                this.item = item;

                if (token.listener != null)
                        token.listener.onInventoryChanged();
                return true;
        }

        /**
         * drops the item in the crate, if no item is in the crate then a item has a chance to be generated
         * based on the player tokens class and luck
         */
        public void dropItem(){
                if(item != null){
                        dropItem(item);
                        return;
                }else{
                        Token t = TokenFactory.lootDrop(token.dungeon, token.floorMap);
                        if(t != null)
                                token.dungeon.addToken(t, token.floorMap, token.location.x, token.location.y);
                }


        }

        @Override
        public boolean dropItem(Item item){
                boolean valid = discard(item);
                if(!valid) return false;

                token.dungeon.addToken(TokenFactory.loot(token.dungeon, item), token.floorMap, token.location.x, token.location.y);

                return true;
        }

        @Override
        public boolean discard(Item item) {
                if (this.item != item || item == null)
                        return false;
                this.item = null;
                if (token.listener != null)
                        token.listener.onInventoryChanged();
                return true;
        }

        @Override
        public boolean update(float delta) {
                return false;
        }
}
