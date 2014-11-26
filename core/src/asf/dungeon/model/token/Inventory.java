package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.item.ArmorItem;
import asf.dungeon.model.item.ConsumableItem;
import asf.dungeon.model.item.EquipmentItem;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
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
                private QuickItem[] quickSlots;
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

                public QuickItem getQuickSlot(int index) {
                        if(index < 0 || index >= numQuickSlots()) return null;
                        return quickSlots[index];
                }

                public int numQuickSlots(){
                        return quickSlots == null ? 0: quickSlots.length;
                }

                public void setNumQuickSlots(int num){
                        if(num > 3 || num < 0){
                              throw new IllegalArgumentException("num must be a value between 0 and 3");
                        }else if(quickSlots == null){
                                quickSlots = new QuickItem[num];
                        }else if(num > quickSlots.length){
                                quickSlots = Arrays.copyOf(quickSlots, num);
                        }else{
                                throw new UnsupportedOperationException("can not shrink the number of quick slots");
                        }

                        if(token.listener != null)
                                token.listener.onInventoryChanged();
                }

                public boolean isEquipped(Item item) {
                        if(item == null)
                                return false;
                        if(item == weaponSlot || item == armorSlot || item == ringSlot)
                                return true;
                        for (int i = 0; i < numQuickSlots(); i++) {
                                if(quickSlots[i] == item) return true;
                        }

                        return false;
                }

                public boolean equip(EquipmentItem item) {
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
                        }

                        if (token.listener != null)
                                token.listener.onInventoryChanged();
                        return true;

                }

                public boolean equip(QuickItem item) {
                        if (item == null || !canChangeEquipment())
                                return false;
                        for (int i = 0; i < numQuickSlots(); i++) {
                                if(quickSlots[i] == null){
                                        quickSlots[i] = item;
                                        if (token.listener != null)
                                                token.listener.onInventoryChanged();
                                        return true;
                                }
                        }
                        quickSlots[0] = item;
                        if (token.listener != null)
                                token.listener.onInventoryChanged();
                        return true;
                }

                public boolean equip(QuickItem item, int index) {
                        if(item == null || !canChangeEquipment() || index <0 || index >=numQuickSlots())
                                return false;

                        quickSlots[index] = item;

                        if (token.listener != null)
                                token.listener.onInventoryChanged();
                        return true;
                }

                public boolean unequip(Item item) {
                        // TODO: check to ensure that unequipping something wouldnt go over the inventory limit
                        if (item == null || !canChangeEquipment() || isFull())
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
                        } else  {
                                for (int i = 0; i < numQuickSlots(); i++) {
                                        if(quickSlots[i] == item)
                                                quickSlots[i] = null;
                                };
                        }
                        if (token.listener != null)
                                token.listener.onInventoryChanged();
                        return true;

                }


                public int size() {
                        return items.size;
                }

                public int sizeInBackpack(){
                        int subMax = 0;
                        if (weaponSlot != null) subMax++;
                        if (armorSlot != null) subMax++;
                        if (ringSlot != null) subMax++;
                        for (int i = 0; i < numQuickSlots(); i++) {
                                if(quickSlots[i] != null) subMax++;
                        }
                        return items.size-subMax;

                }

                public int maxBackpackSlots(){
                        return 16;
                }

                public boolean isFull() {
                        return sizeInBackpack() >= maxBackpackSlots();
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

                        if(item instanceof QuickItem){
                                equip((QuickItem)item); // TODO: if discarding an equipped item this will cause two calls in a row to onInventoryChanged(), the messy code to fix this would be messy so im just going to leave as is for now
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
