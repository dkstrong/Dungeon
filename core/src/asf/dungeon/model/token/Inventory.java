package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/11/2014.
 */
public class Inventory implements TokenComponent {
        private final Token token;
        private final Array<Item> items = new Array<Item>(true, 16, Item.class);

        public Inventory(Token token, Item... items) {
                this.token = token;
                this.items.addAll(items);
        }

        public Item getItem() {
                if (items.size > 0)
                        return items.get(0);
                return null;
        }

        public Item getItem(int index) {
                if (items.size > index)
                        return items.get(index);
                return null;
        }

        public KeyItem getKeyItem(KeyItem.Type keyType){
                for (Item item : items) {
                        if(item instanceof KeyItem){
                                KeyItem key = (KeyItem) item;
                                if(key.getType() == keyType){
                                        return key;
                                }
                        }
                }
                return null;
        }

        public int size() {
                return items.size;
        }

        public void addItem(Item item) {
                items.add(item);
                QuickSlot quickSlot = token.get(QuickSlot.class);
                if(quickSlot != null){
                        if(quickSlot.getItem() == null){
                                // if the quickslot has nothing in it, then put the newly picked up item there...
                                // TODO: may want to have further qualifiers so that only relevant items will be automatically quickslotted
                                quickSlot.setItem(item);
                        }
                }
                if (token.listener != null)
                        token.listener.onInventoryAdd(item);
        }

        public boolean discardItem(Item item) {
                if (token.getDamage().isDead()) {
                        return false;
                }
                boolean valid = items.removeValue(item, true);
                if (valid) {
                        QuickSlot quickSlot = token.get(QuickSlot.class);
                        if (quickSlot != null) {
                                if (quickSlot.getItem() == item ){
                                        // change the quick slot to another item that .equals it (eg to another health potion) otherwise set to null
                                        quickSlot.setItem(null);
                                        for (Item i : items) {
                                                if(i.equals(item)){
                                                        quickSlot.setItem(i);
                                                        break;
                                                }
                                        }
                                }

                        }

                        if (token.listener != null)
                                token.listener.onInventoryRemove(item);
                } else
                        throw new AssertionError(token.getName() + " is not carrying " + item + " and can not discard it");

                return valid;
        }

        /**
         * the "quantity" of the item is done be doing a .equals against all items in the items
         * items are considered equal if all their effects are the same (as determined by the item itself)
         *
         * items that .equals eachother should appear stacked in the items screen or similiar.
         * @param item
         * @return
         */
        public int getQuantity(Item item) {
                int count = 0;
                for (Item i : items) {
                        if (item.equals(i))
                                count++;
                }
                return count;
        }

        /**
         * do not modify! use addItem() and discardItem(), this is required to properly announce this change to the listener
         *
         * @return
         */
        public Array<Item> getItems() {
                return items;
        }

        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                return true;
        }

        @Override
        public boolean update(float delta) {
                if(token.getCommand() != null && token.getCommand().consumeItem != null){
                        token.getCommand().consumeItem.consume(token);
                        if (token.listener != null)
                                token.listener.onUseItem(token.getCommand().consumeItem);
                        discardItem(token.getCommand().consumeItem);
                        token.getCommand().consumeItem = null;
                }
                return false;
        }


}
