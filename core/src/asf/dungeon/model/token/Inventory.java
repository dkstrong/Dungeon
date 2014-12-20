package asf.dungeon.model.token;

import asf.dungeon.model.item.Item;

/**
 * Created by Danny on 11/11/2014.
 */
public interface Inventory extends TokenComponent {

        public Item getItemToDrop();

        public int size();

        public boolean isFull();

        /**
         * @param item
         * @return true if the item was succesfully added, false if the inventory was full or some other issue prevented adding the item
         */
        public boolean add(Item item);

        public boolean dropItem(Item item);

        /**
         * @param item
         * @return true if the item was succesfully discard, false if it could not be discarded (eg it is cursed)
         */
        public boolean discard(Item item);


}
