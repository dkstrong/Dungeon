package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.item.EquipmentItem;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/11/2014.
 */
public interface Inventory extends TokenComponent {

        public Item get();
        public int size();
        public void add(Item item);
        public boolean discard(Item item);

        public class Character implements Inventory{
                private final Token token;
                private final Array<Item> items;
                private EquipmentItem weaponSlot;
                private EquipmentItem armorSlot;
                private EquipmentItem ringSlot;
                private Item quickSlot;

                public Character(Token token, Item... items) {
                        this.token = token;
                        if(token.getLogic() == null){
                                this.items = new Array<Item>(true, items.length, Item.class); // this is probably a crate, inventory will probaby not grow in size
                        }else{
                                this.items = new Array<Item>(true, 16, Item.class);
                        }
                        this.items.addAll(items);
                }

                public Item get() {
                        if (items.size > 0) return items.get(0);
                        return null;
                }

                public Item get(int index) {
                        if (items.size > index) return items.get(index);
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

                public EquipmentItem getWeaponSlot() {
                        return weaponSlot;
                }
                public EquipmentItem getArmorSlot() {
                        return armorSlot;
                }

                public EquipmentItem getRingSlot() {
                        return ringSlot;
                }

                public Item getQuickSlot() {
                        return quickSlot;
                }

                public void setQuickSlot(Item quickSlot) {
                        this.quickSlot = quickSlot;
                }

                public void equip(EquipmentItem equipmentItem) {
                        if(equipmentItem == null)
                                return;
                        if(equipmentItem.getType() == EquipmentItem.Type.Weapon) {
                                weaponSlot = equipmentItem;
                                token.getExperience().recalcStats();
                        }else if(equipmentItem.getType() == EquipmentItem.Type.Armor) {
                                armorSlot = equipmentItem;
                                token.getExperience().recalcStats();
                        }else if(equipmentItem.getType() == EquipmentItem.Type.Ring) {
                                ringSlot = equipmentItem;
                                token.getExperience().recalcStats();
                        }

                }

                public void unequip(EquipmentItem equipmentItem){
                        if(equipmentItem == null)
                                return;
                        if(equipmentItem.getType() == EquipmentItem.Type.Weapon) {
                                if(weaponSlot == equipmentItem){
                                        weaponSlot = null;
                                        token.getExperience().recalcStats();
                                }
                        }else if(equipmentItem.getType() == EquipmentItem.Type.Armor) {
                                if(armorSlot == equipmentItem){
                                        armorSlot = null;
                                        token.getExperience().recalcStats();
                                }
                        }else if(equipmentItem.getType() == EquipmentItem.Type.Ring) {
                                if(ringSlot == equipmentItem){
                                        ringSlot = null;
                                        token.getExperience().recalcStats();
                                }
                        }

                }


                public int size() {
                        return items.size;
                }

                public void add(Item item) {
                        items.add(item);

                        if(quickSlot == null && item.isIdentified(token)){
                                // if the quickslot has nothing in it, then put the newly picked up item there...
                                quickSlot = item;
                        }
                        if (token.listener != null)
                                token.listener.onInventoryAdd(item);
                }

                public boolean discard(Item item) {
                        if (token.getDamage().isDead()) {
                                return false;
                        }
                        boolean valid = items.removeValue(item, true);
                        if (valid) {
                                // if this item was equipped, unequip it
                                if(item instanceof EquipmentItem) unequip((EquipmentItem) item);

                                // change the quick slot to another item that .equals it (eg to another health potion) otherwise set to null
                                if (quickSlot != null && quickSlot == item) {
                                        quickSlot= null;
                                        for (Item i : items) {
                                                if(i.equals(item)){
                                                        quickSlot=i;
                                                        break;
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
                                discard(token.getCommand().consumeItem);
                                token.getCommand().consumeItem = null;
                        }
                        return false;
                }
        }

        public class Simple implements Inventory{
                private Token token;
                private Item item;

                public Simple(Token token, Item item) {
                        this.token = token;
                        this.item = item;
                }
                public Item get() {
                        return item;
                }

                public int size() {
                        return item == null ? 0 : 1;
                }

                public void add(Item item) {
                        if(this.item != null)
                                return;
                        this.item = item;

                        if (token.listener != null)
                                token.listener.onInventoryAdd(item);
                }

                public boolean discard(Item item) {
                        if(this.item != item || item == null)
                                return false;
                        this.item = null;
                        if (token.listener != null)
                                token.listener.onInventoryRemove(item);
                        return true;
                }

                @Override
                public boolean teleportToLocation(int x, int y, Direction direction) {
                        return true;
                }

                @Override
                public boolean update(float delta) { return false; }
        }


}
