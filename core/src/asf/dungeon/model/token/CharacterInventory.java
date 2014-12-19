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
* Created by Daniel Strong on 12/8/2014.
*/
public class CharacterInventory implements Inventory {
        private final Token token;
        private final Array<Item> items;
        private WeaponItem weaponSlot;
        private ArmorItem armorSlot;
        private RingItem ringSlot;
        private QuickItem[] quickSlots;
        private float timeSinceComabt = Float.MAX_VALUE;
        private Item itemToDrop;

        public CharacterInventory(Token token, Item... items) {
                this.token = token;
                if (token.getLogic() == null) {
                        this.items = new Array<Item>(true, items.length, Item.class); // this is probably a crate, inventory will probaby not grow in size
                } else {
                        this.items = new Array<Item>(true, 16, Item.class);
                }
                this.items.addAll(items);

        }

        public Item getItemToDrop() {
                if(!items.contains(itemToDrop, true)) return null;
                return itemToDrop;
        }

        public void setItemToDrop(Item item){
                this.itemToDrop = item;
        }

        public Item get(int index) {
                if (items.size > index) return items.get(index);
                return null;
        }

        public boolean hasKey(KeyItem key){
                for (Item item : items) {
                        if(item instanceof KeyItem){
                                KeyItem k = (KeyItem) item;
                                if(k.equals(key)) // TODO: or should i do k.getType == key.getType?
                                        return true;
                        }
                }
                return false;
        }

        public PotionItem getPotionItem(PotionItem.Type potionType) {
                for (Item item : items) {
                        if (item instanceof PotionItem) {
                                PotionItem potion = (PotionItem) item;
                                if (potion.getType() == potionType) {
                                        return potion;
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
                if (index < 0 || index >= numQuickSlots()) return null;
                return quickSlots[index];
        }

        public boolean isQuickSlotsFull() {
                if (quickSlots == null) return true;
                for (QuickItem quickSlot : quickSlots) {
                        if (quickSlot == null) return false;
                }
                return true;
        }

        public boolean contains(Item targetItem) {
                return items.contains(targetItem, true);
        }

        public int numQuickSlots() {
                return quickSlots == null ? 0 : quickSlots.length;
        }

        public void setNumQuickSlots(int num) {
                if (num > 3 || num < 0) {
                        throw new IllegalArgumentException("num must be a value between 0 and 3");
                } else if (quickSlots == null) {
                        quickSlots = new QuickItem[num];
                } else if (num > quickSlots.length) {
                        quickSlots = Arrays.copyOf(quickSlots, num);
                } else {
                        throw new UnsupportedOperationException("can not shrink the number of quick slots");
                }

                if (token.listener != null)
                        token.listener.onInventoryChanged();
        }
        public int size() {
                return items.size;
        }

        public int sizeInBackpack() {
                int subMax = 0;
                if (weaponSlot != null) subMax++;
                if (armorSlot != null) subMax++;
                if (ringSlot != null) subMax++;
                for (int i = 0; i < numQuickSlots(); i++) {
                        if (quickSlots[i] != null) subMax++;
                }
                return items.size - subMax;

        }

        public int maxBackpackSlots() {
                return 16;
        }

        public boolean isFull() {
                return sizeInBackpack() >= maxBackpackSlots();
        }

        protected void resetCombatTimer() {
                timeSinceComabt = 0;
        }

        public boolean isEquipped(Item item) {
                if (item == null)
                        return false;
                if (item == weaponSlot || item == armorSlot || item == ringSlot)
                        return true;
                for (int i = 0; i < numQuickSlots(); i++) {
                        if (quickSlots[i] == item) return true;
                }

                return false;
        }

        public boolean canChangeEquipment() {
                if (token.getAttack() != null && (token.getAttack().isAttacking() || token.getAttack().hasProjectile()))
                        return false;

                if(token.getStatusEffects() != null && (token.getStatusEffects().has(StatusEffect.Paralyze) || token.getStatusEffects().has(StatusEffect.Frozen))  )
                        return false;
                return timeSinceComabt > 5f;
        }

        private boolean hasRequiredStatsToChangeEquipment(EquipmentItem currentSlot, EquipmentItem otherSlot1, EquipmentItem otherSlot2, EquipmentItem item){
                int str = token.getExperience().getStrength();
                int agi = token.getExperience().getAgility();
                int ine = token.getExperience().getIntelligence();
                if(item != null){
                        str += item.getStrengthMod();
                        agi += item.getAgilityMod();
                        ine += item.getIntelligenceMod();
                }
                if(currentSlot != null){
                        if(currentSlot.isCursed()) return false;
                        str -= currentSlot.getStrengthMod();
                        agi -= currentSlot.getAgilityMod();
                        ine -= currentSlot.getAgilityMod();
                }
                if(item != null){
                        if(str < item.getRequiredStrength()) return false;
                        if(agi < item.getRequiredAgility()) return false;
                        if(ine < item.getRequiredIntelligence()) return false;
                }
                if(otherSlot1 != null){
                        if(str < otherSlot1.getRequiredStrength()) return false;
                        if(agi < otherSlot1.getRequiredAgility()) return false;
                        if(ine < otherSlot1.getRequiredIntelligence()) return false;
                }

                if(otherSlot2 != null){
                        if(str < otherSlot2.getRequiredStrength()) return false;
                        if(agi < otherSlot2.getRequiredAgility()) return false;
                        if(ine < otherSlot2.getRequiredIntelligence()) return false;
                }
                return true;
        }

        public boolean equip(EquipmentItem item) {
                if (item == null || !canChangeEquipment())
                        return false;
                if (item instanceof WeaponItem) {
                        if(!hasRequiredStatsToChangeEquipment(weaponSlot, armorSlot, ringSlot, item)) return false;
                        weaponSlot = (WeaponItem) item;
                        token.getExperience().recalcStats();
                } else if (item instanceof ArmorItem) {
                        if(!hasRequiredStatsToChangeEquipment(armorSlot, weaponSlot, ringSlot, item)) return false;
                        armorSlot = (ArmorItem) item;
                        token.getExperience().recalcStats();
                } else if (item instanceof RingItem) {
                        if(!hasRequiredStatsToChangeEquipment(ringSlot, weaponSlot, armorSlot, item)) return false;
                        ringSlot = (RingItem) item;
                        token.getExperience().recalcStats();
                }

                if (token.listener != null)
                        token.listener.onInventoryChanged();
                return true;

        }

        public boolean equip(QuickItem item) {
                if (item == null )
                        return false;
                for (int i = 0; i < numQuickSlots(); i++) {
                        if (quickSlots[i] == null) {
                                quickSlots[i] = item;
                                if (token.listener != null)
                                        token.listener.onInventoryChanged();
                                return true;
                        }
                }
                if(quickSlots != null && quickSlots.length > 0){
                        quickSlots[0] = item;
                        if (token.listener != null)
                                token.listener.onInventoryChanged();
                        return true;
                }
                return false;
        }

        public boolean equip(QuickItem item, int index) {
                if (item == null || index < 0 || index >= numQuickSlots())
                        return false;

                quickSlots[index] = item;

                if (token.listener != null)
                        token.listener.onInventoryChanged();
                return true;
        }



        public boolean unequip(Item item) {
                if(item instanceof EquipmentItem){
                        return unequip((EquipmentItem)item, false);
                }else if(item instanceof QuickItem){
                        return unequip((QuickItem)item, false);
                }
                return false;
        }

        private boolean unequip(Item item, boolean forDiscard) {
                if(item instanceof EquipmentItem){
                        return unequip((EquipmentItem)item, forDiscard);
                }else if(item instanceof QuickItem){
                        return unequip((QuickItem)item, forDiscard);
                }
                return false;
        }

        private boolean unequip(EquipmentItem item, boolean forDiscard) {
                if (item == null || !canChangeEquipment())
                        return false;

                // if unequipping an item and the inventory is full, it can still go through if the item will be discarded
                if(isFull() && !forDiscard) return false;

                if (weaponSlot == item) {
                        if(!hasRequiredStatsToChangeEquipment(weaponSlot, armorSlot, ringSlot, null)) return false;
                        weaponSlot = null;
                        token.getExperience().recalcStats();
                } else if (armorSlot == item) {
                        if(!hasRequiredStatsToChangeEquipment(armorSlot, weaponSlot, ringSlot, null)) return false;
                        armorSlot = null;
                        token.getExperience().recalcStats();
                } else if (ringSlot == item) {
                        if(!hasRequiredStatsToChangeEquipment(ringSlot, weaponSlot, armorSlot, null)) return false;
                        ringSlot = null;
                        token.getExperience().recalcStats();
                }
                if (!forDiscard && token.listener != null)
                        token.listener.onInventoryChanged();
                return true;
        }

        private boolean unequip(QuickItem item, boolean forDiscard){
                if (item == null )
                        return false;
                if(isFull() && !forDiscard) return false;

                for (int i = 0; i < numQuickSlots(); i++) {
                        if (quickSlots[i] == item)
                                quickSlots[i] = null;
                }
                if (!forDiscard && token.listener != null)
                        token.listener.onInventoryChanged();
                return true;
        }

        public boolean add(Item item) {
                if (item == null)
                        return false;
                if (isFull())
                        return false;

                if (item instanceof StackableItem) {
                        // if item can be stacked, attempt to stack instead of adding it
                        StackableItem stackableItem = (StackableItem) item;
                        for (Item storedItem : items) {
                                if (storedItem instanceof StackableItem) {
                                        StackableItem storedStackable = (StackableItem) storedItem;
                                        if (storedStackable.canStackWith(stackableItem)) {
                                                storedStackable.stack(stackableItem);
                                                if (token.listener != null) token.listener.onInventoryChanged();
                                                return true;
                                        }
                                }
                        }
                }

                items.add(item);

                if (item instanceof QuickItem && !isQuickSlotsFull()) {
                        // TODO: if discarding an equipped item this will cause two calls in a row to onInventoryChanged(), the messy code to fix this would be messy so im just going to leave as is for now
                        equip((QuickItem) item);
                }

                if (token.listener != null) token.listener.onInventoryChanged();
                return true;
        }

        public boolean drop(Item item){
                boolean valid = discard(item);
                if(!valid) return false;
                // TODO: attempt to drop on an empty tiles N,S,E, or W of location firs,t drop on self as last resort
                token.dungeon.newLootToken(token.getFloorMap(), item, token.getLocation().x, token.getLocation().y);
                return true;
        }

        public boolean discard(Item item) {
                if (token.getDamage().isDead()) {
                        return false;
                }

                if (isEquipped(item)) {
                        boolean valid = unequip(item, true);
                        if (!valid) return false;
                }

                boolean valid = items.removeValue(item, true);
                if (token.listener != null)
                        token.listener.onInventoryChanged();

                return valid;
        }

        public <S extends StackableItem> S discardOrUnstack(S item, int charges) {
                if (charges == 0) throw new IllegalArgumentException("can not unstack 0 charges");
                if (charges > item.getCharges()) throw new IllegalArgumentException("can not unstack more charges than exists");
                if (charges == item.getCharges()) {
                        boolean valid = discard(item);
                        if (valid) return item;
                        else return null;
                } else {
                        StackableItem newItem = item.unStack(charges);
                        if (token.listener != null) token.listener.onInventoryChanged();
                        return (S) newItem;
                }

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
                        if (consumableItem instanceof ConsumableItem.TargetsTokens) {
                                if (token.getCommand().getTargetToken() != null) {
                                        ConsumableItem.TargetsTokens citt = (ConsumableItem.TargetsTokens) consumableItem;
                                        if (citt.canConsume(token, token.getCommand().getTargetToken())) {
                                                out.targetToken = token.getCommand().getTargetToken();
                                                out.targetItem = null;
                                                citt.consume(token, token.getCommand().getTargetToken(), out);
                                                hasConsumed = true;
                                        }
                                }
                        }

                        if (consumableItem instanceof ConsumableItem.TargetsItems) {
                                if (token.getCommand().targetItem != null) {
                                        ConsumableItem.TargetsItems citi = (ConsumableItem.TargetsItems) consumableItem;
                                        if (citi.canConsume(token, token.getCommand().targetItem)) {
                                                out.targetToken = null;
                                                out.targetItem = token.getCommand().targetItem;
                                                citi.consume(token, token.getCommand().targetItem, out);
                                                token.getCommand().targetItem = null;
                                                hasConsumed = true;
                                        }
                                }
                        }


                        if (!hasConsumed) {
                                //Gdx.app.log("CharacterInventory",token.getName()+" uses "+consumableItem);
                                out.targetItem = null;
                                out.targetToken = null;
                                consumableItem.consume(token, out);

                        }


                        if (token.listener != null)
                                token.listener.onUseItem(consumableItem, out);

                        if (consumableItem instanceof StackableItem) {
                                StackableItem stackableItem = (StackableItem) consumableItem;
                                if (stackableItem.getCharges() <= 0) {
                                        discard(consumableItem);
                                } else {
                                        // we still need to notify about the number of charges being changed
                                        if (token.listener != null) token.listener.onInventoryChanged();
                                }
                        } else {
                                discard(consumableItem);
                        }

                        token.getCommand().consumeItem = null;
                }


                return false;
        }

        /**
         * should only be called by Move after unlocking a door
         *
         * @param key
         */
        protected void useKey(KeyItem key) {
                KeyItem actualKey = null;
                for (Item item : items) {
                        if(item instanceof KeyItem){
                                KeyItem k = (KeyItem) item;
                                if(k.equals(key)){  // TODO: or should i do k.getType == key.getType?
                                        actualKey = k;
                                        break;
                                }
                        }
                }

                out.targetItem = null;
                out.targetToken = null;
                out.didSomething = true;
                if (token.listener != null)
                        token.listener.onUseItem(actualKey, out);
                discard(actualKey);
        }


        public static class UseItemOutcome {
                public Token targetToken;
                public Item targetItem;
                public boolean didSomething;
                public int damage;
        }
}
