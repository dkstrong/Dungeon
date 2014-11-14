package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Item;

/**
 * Created by Danny on 11/13/2014.
 */
public class QuickSlot implements TokenComponent {


        private Item item;


        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                return true;
        }

        @Override
        public boolean update(float delta) {
                return false;
        }

        public Item getItem() {
                return item;
        }

        /**
         * be sure to do your own check to make sure that this item is in the token's inventory. otherwise
         * it will just be a useless quickslot
         * @param quickSlotItem
         */
        public void setItem(Item quickSlotItem) {
                this.item = quickSlotItem;
        }
}
