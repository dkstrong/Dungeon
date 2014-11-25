package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.item.ArmorItem;
import asf.dungeon.model.item.ConsumableItem;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.model.item.QuickItem;
import asf.dungeon.model.item.RingItem;
import asf.dungeon.model.item.StackableItem;
import asf.dungeon.model.item.WeaponItem;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/11/2014.
 */
public interface Inventory extends TokenComponent {

        public Item get();

        public int size();

        public boolean isFull();

        /**
         * @param item
         * @return true if the item was succesfully added, false if the inventory was full or some other issue prevented adding the item
         */
        public boolean add(Item item);

        /**
         * @param item
         * @return true if the item was succesfully discard, false if it could not be discarded (eg it is cursed)
         */
        public boolean discard(Item item);

        public static class Character implements Inventory {
                private final Token token;
                private final Array<Item> items;
                private WeaponItem weaponSlot;
                private ArmorItem armorSlot;
                private RingItem ringSlot;
                private QuickItem quickSlot;
                private float timeSinceComabt = Float.MAX_VALUE;

                public Character(Token token, Item... items) {
                        this.token = token;
                        if (token.getLogic() == null) {
                                this.items = new Array<Item>(true, items.length, Item.class); // this is probably a crate, inventory will probaby not grow in size
                        } else {
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

                public KeyItem getKeyItem(KeyItem.Type keyType) {
                        for (Item item : items) {
                                if (item instanceof KeyItem) {
                                        KeyItem key = (KeyItem) item;
                                        if (key.getType() == keyType) {
                                                return key;
                                        }
                                }
                        }
                        return null;
                }

                public WeaponItem getWeaponSlot() {
                        return weaponSlot;
                }

                public ArmorItem getArmorSlot() {
                        return armorSlot;
                }

                public RingItem getRingSlot() {
                        return ringSlot;
                }

                public QuickItem getQuickSlot() {
                        return quickSlot;
                }

                public boolean isEquipped(Item item) {
                        return item != null && (item == weaponSlot || item == armorSlot || item == ringSlot || item == quickSlot);
                }

                public boolean equip(Item item) {
                        if (item == null || !canChangeEquipment())
                                return false;
                        if (item instanceof WeaponItem) {
                                if (weaponSlot != null && weaponSlot.isCursed()) return false;
                                weaponSlot = (WeaponItem) item;
                                token.getExperience().recalcStats();
                        } else if (item instanceof ArmorItem) {
                                if (armorSlot != null && armorSlot.isCursed()) return false;
                                armorSlot = (ArmorItem) item;
                                token.getExperience().recalcStats();
                        } else if (item instanceof RingItem) {
                                if (ringSlot != null && ringSlot.isCursed()) return false;
                                ringSlot = (RingItem) item;
                                token.getExperience().recalcStats();
                        } else if (item instanceof QuickItem) {
                                quickSlot = (QuickItem) item;
                        }

                        if (token.listener != null)
                                token.listener.onInventoryChanged();
                        return true;

                }

                public boolean unequip(Item item) {
                        if (item == null || !canChangeEquipment())
                                return false;
                        if (weaponSlot == item) {
                                if (weaponSlot.isCursed()) return false;
                                weaponSlot = null;
                                token.getExperience().recalcStats();
                        } else if (armorSlot == item) {
                                if (armorSlot.isCursed()) return false;
                                armorSlot = null;
                                token.getExperience().recalcStats();
                        } else if (ringSlot == item) {
                                if (ringSlot.isCursed()) return false;
                                ringSlot = null;
                                token.getExperience().recalcStats();
                        } else if (quickSlot == item) {
                                quickSlot = null;
                        }
                        if (token.listener != null)
                                token.listener.onInventoryChanged();
                        return true;

                }


                public int size() {
                        return items.size;
                }

                public boolean isFull() {
                        if (items.size < 16) return false;
                        int subMax = 0;
                        if (weaponSlot != null) subMax++;
                        if (armorSlot != null) subMax++;
                        if (ringSlot != null) subMax++;
                        if (quickSlot != null) subMax++;
                        return items.size - subMax >= 16;
                }

                protected void resetCombatTimer() {
                        timeSinceComabt = 0;
                }

                public boolean canChangeEquipment() {
                        if(token.getAttack() != null && (token.getAttack().isAttacking() || token.getAttack().hasProjectile())){
                                return false;
                        }
                        return timeSinceComabt > 5f;
                }

                public boolean add(Item item) {
                        if(item == null)
                                return false;
                        if (isFull())
                                return false;

                        if (item instanceof StackableItem ) {
                                // if item can be stacked, attempt to stack instead of adding it
                                StackableItem stackableItem = (StackableItem) item;
                                for (Item storedItem : items) {
                                        if(storedItem instanceof StackableItem){
                                                StackableItem storedStackable = (StackableItem) storedItem;
                                                if(storedStackable.canStackWith(stackableItem)){
                                                        storedStackable.addChargesFrom(stackableItem);
                                                        if (token.listener != null) token.listener.onInventoryChanged();
                                                        return true;
                                                }
                                        }
                                }
                        }

                        items.add(item);

                        if (quickSlot == null && item.isIdentified(token) && item instanceof QuickItem) {
                                // if the quickslot has nothing in it, then put the newly picked up item there...
                                equip(item); // TODO: if discarding an equipped item this will cause two calls in a row to onInventoryChanged(), the messy code to fix this would be messy so im just going to leave as is for now
                        }

                        if (token.listener != null) token.listener.onInventoryChanged();
                        return true;
                }

                public boolean discard(Item item) {
                        if (token.getDamage().isDead()) {
                                return false;
                        }
                        if (isEquipped(item)) {
                                boolean valid = unequip(item); // TODO: if discarding an equipped item this will cause two calls in a row to onInventoryChanged(), the messy code to fix this would be messy so im just going to leave as is for now
                                if (!valid) return false;
                        }

                        boolean valid = items.removeValue(item, true);
                        if (valid && token.listener != null) token.listener.onInventoryChanged();

                        return valid;
                }

                @Override
                public void teleport(FloorMap fm, int x, int y, Direction direction) {

                }

                private static final transient UseItemOutcome out = new UseItemOutcome();

                @Override
                public boolean update(float delta) {
                        timeSinceComabt += delta;
                        if (token.getCommand() != null && token.getCommand().consumeItem != null) {
                                ConsumableItem consumableItem = token.getCommand().consumeItem;

                                boolean hasConsumed = false;
                                out.damage = 0;
                                if(consumableItem instanceof ConsumableItem.TargetsTokens){
                                        if(token.getCommand().getTargetToken() != null){
                                                ConsumableItem.TargetsTokens citt = (ConsumableItem.TargetsTokens) consumableItem;
                                                if(citt.canConsume(token, token.getCommand().getTargetToken())){
                                                        out.targetToken = token.getCommand().getTargetToken();
                                                        out.targetItem = null;
                                                        citt.consume(token, token.getCommand().getTargetToken(), out);
                                                        hasConsumed = true;
                                                }
                                        }
                                }

                                if(consumableItem instanceof ConsumableItem.TargetsItems){
                                        if(token.getCommand().getTargetToken() != null){
                                                ConsumableItem.TargetsItems citi = (ConsumableItem.TargetsItems) consumableItem;
                                                if(citi.canConsume(token, token.getCommand().targetItem)){
                                                        out.targetToken = null;
                                                        out.targetItem = token.getCommand().targetItem;
                                                        citi.consume(token, token.getCommand().targetItem, out);
                                                        token.getCommand().targetItem = null;
                                                        hasConsumed = true;
                                                }
                                        }
                                }


                                if(!hasConsumed){
                                        out.targetItem = null;
                                        out.targetToken = null;
                                        consumableItem.consume(token, out);

                                }


                                if (token.listener != null)
                                        token.listener.onUseItem(consumableItem, out);

                                if (consumableItem instanceof StackableItem) {
                                        StackableItem stackableItem = (StackableItem) consumableItem;
                                        if (stackableItem.getCharges() <= 0)
                                                discard(consumableItem);
                                } else {
                                        discard(consumableItem);
                                }

                                token.getCommand().consumeItem = null;
                        }


                        return false;
                }

                /**
                 * should only be called by Move after unlocking a door
                 * @param key
                 */
                protected void useKey(KeyItem key){

                        out.targetItem = null;
                        out.targetToken = null;
                        out.didSomething = true;
                        if(token.listener != null)
                                token.listener.onUseItem(key, out);
                        discard(key);
                }

                public static class UseItemOutcome{
                        public Token targetToken;
                        public Item targetItem;
                        public boolean didSomething;
                        public int damage;
                }
        }

        public static class Simple implements Inventory {
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


}
