package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.item.ArmorItem;
import asf.dungeon.model.item.ConsumableItem;
import asf.dungeon.model.item.EquipmentItem;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.QuickItem;
import asf.dungeon.model.item.RingItem;
import asf.dungeon.model.item.StackableItem;
import asf.dungeon.model.item.WeaponItem;
import com.badlogic.gdx.utils.Array;

import java.util.Arrays;

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

        public boolean drop(Item item);

        /**
         * @param item
         * @return true if the item was succesfully discard, false if it could not be discarded (eg it is cursed)
         */
        public boolean discard(Item item);


}
