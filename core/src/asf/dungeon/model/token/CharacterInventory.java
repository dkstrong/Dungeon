package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
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
public class CharacterInventory implements Inventory, Teleportable {
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
                if (token.logic == null) {
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

        public boolean containsKey(KeyItem.Type key){
                for (Item item : items) {
                        if(item instanceof KeyItem){
                                KeyItem k = (KeyItem) item;
                                if(k.getType() == key) // TODO: or should i do k.getType == key.getType?
                                        return true;
                        }
                }
                return false;
        }

        public boolean containsKey(KeyItem key){
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
                if (token.attack != null && (token.attack.isAttacking() || token.attack.hasProjectile()))
                        return false;

                if(token.statusEffects != null && (token.statusEffects.has(StatusEffect.Paralyze) || token.statusEffects.has(StatusEffect.Frozen))  )
                        return false;
                return timeSinceComabt > 5f;
        }

        private boolean hasRequiredStatsToChangeEquipment(EquipmentItem currentSlot, EquipmentItem otherSlot1, EquipmentItem otherSlot2, EquipmentItem item){
                int str = token.experience.getStrength();
                int agi = token.experience.getAgility();
                int ine = token.experience.getIntelligence();
                if(item != null){
                        str += item.strengthMod;
                        agi += item.agilityMod;
                        ine += item.intelligenceMod;
                }
                if(currentSlot != null){
                        if(currentSlot.cursed) return false;
                        str -= currentSlot.strengthMod;
                        agi -= currentSlot.agilityMod;
                        ine -= currentSlot.intelligenceMod;
                }
                if(item != null){
                        if(str < item.requiredStrength) return false;
                        if(agi < item.requiredAgility) return false;
                        if(ine < item.requiredIntelligence) return false;
                }
                if(otherSlot1 != null){
                        if(str < otherSlot1.requiredStrength) return false;
                        if(agi < otherSlot1.requiredAgility) return false;
                        if(ine < otherSlot1.requiredIntelligence) return false;
                }

                if(otherSlot2 != null){
                        if(str < otherSlot2.requiredStrength) return false;
                        if(agi < otherSlot2.requiredAgility) return false;
                        if(ine < otherSlot2.requiredIntelligence) return false;
                }
                return true;
        }

        public boolean equip(EquipmentItem item) {
                if (item == null || !canChangeEquipment())
                        return false;
                if (item instanceof WeaponItem) {
                        if(!hasRequiredStatsToChangeEquipment(weaponSlot, armorSlot, ringSlot, item)) return false;
                        weaponSlot = (WeaponItem) item;
                        token.experience.recalcStats();
                } else if (item instanceof ArmorItem) {
                        if(!hasRequiredStatsToChangeEquipment(armorSlot, weaponSlot, ringSlot, item)) return false;
                        armorSlot = (ArmorItem) item;
                        token.experience.recalcStats();
                } else if (item instanceof RingItem) {
                        if(!hasRequiredStatsToChangeEquipment(ringSlot, weaponSlot, armorSlot, item)) return false;
                        ringSlot = (RingItem) item;
                        token.experience.recalcStats();
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
                        token.experience.recalcStats();
                } else if (armorSlot == item) {
                        if(!hasRequiredStatsToChangeEquipment(armorSlot, weaponSlot, ringSlot, null)) return false;
                        armorSlot = null;
                        token.experience.recalcStats();
                } else if (ringSlot == item) {
                        if(!hasRequiredStatsToChangeEquipment(ringSlot, weaponSlot, armorSlot, null)) return false;
                        ringSlot = null;
                        token.experience.recalcStats();
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

        public boolean dropItem(Item item){
                boolean valid = discard(item);
                if(!valid) return false;
                Pair dropLoc = new Pair(token.location);
                if(token.move.isMoving()){
                        dropLoc.addFree(token.direction.opposite());
                        Tile tile = token.floorMap.getTile(dropLoc);
                        if(tile == null || !tile.isFloor() || token.floorMap.hasTokensAt(dropLoc.x,dropLoc.y)){
                                dropLoc.set(token.location);
                        }
                }
                token.dungeon.newLootToken(token.floorMap, item, dropLoc.x, dropLoc.y);
                return true;
        }

        public boolean throwItem(Item item){
                boolean valid = discard(item);
                if(!valid) return false;
                Pair dropLoc = new Pair(token.location);
                dropLoc.addFree(token.direction);

                if(token.floorMap.isLocationBlocked(dropLoc)){
                        dropLoc.set(token.location); // or maybe also doo addFree(token.direction.opposite()) ?
                }
                Token lootToken = token.dungeon.newLootToken(token.floorMap, item, token.location.x, token.location.y);
                lootToken.loot.becomeThrown(dropLoc.x, dropLoc.y);
                return true;
        }

        public boolean throwItem(Item item, int targetLocationX, int targetLocationY){
                boolean valid = discard(item);
                if(!valid) return false;
                Token lootToken = token.dungeon.newLootToken(token.floorMap, item, token.location.x, token.location.y);
                lootToken.loot.becomeThrown(targetLocationX, targetLocationY);
                return true;
        }

        public boolean discard(Item item) {
                if (token.damage.isDead()) {
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

        private static final transient UseItemOutcome out = new UseItemOutcome();

        @Override
        public boolean update(float delta) {
                timeSinceComabt += delta;

                if(keyTurnU != 1){
                        keyTurnU += delta;
                        if(keyTurnU < 1){
                                return true;
                        }else{
                                keyTurnU = 1;
                                keyTurnOnTile.setDoorLocked(false);
                                token.inventory.useKey((KeyItem) keyTurnOnTile.doorSymbol);
                        }

                }


                if (token.command != null && token.command.consumeItem != null) {
                        ConsumableItem consumableItem = token.command.consumeItem;

                        boolean hasConsumed = false;
                        out.damage = 0;
                        if (consumableItem instanceof ConsumableItem.TargetsTokens) {
                                if (token.command.targetItemToken != null) {
                                        ConsumableItem.TargetsTokens citt = (ConsumableItem.TargetsTokens) consumableItem;
                                        if (citt.canConsume(token, token.command.targetItemToken)) {
                                                out.targetToken = token.command.targetItemToken;
                                                out.targetItem = null;
                                                citt.consume(token, token.command.targetItemToken, out);
                                                hasConsumed = true;
                                        }
                                }
                        }

                        if (consumableItem instanceof ConsumableItem.TargetsItems) {
                                if (token.command.targetItem != null) {
                                        ConsumableItem.TargetsItems citi = (ConsumableItem.TargetsItems) consumableItem;
                                        if (citi.canConsume(token, token.command.targetItem)) {
                                                out.targetToken = null;
                                                out.targetItem = token.command.targetItem;
                                                citi.consume(token, token.command.targetItem, out);
                                                token.command.targetItem = null;
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

                        token.command.consumeItem = null;
                        token.command.targetItemToken = null;
                        token.command.targetItem = null;
                }


                return false;
        }

        private static final boolean doubleTapToOpenDoor = false;
        protected boolean showDoorLockedMessage = true;
        private float keyTurnU = 1;
        private Tile keyTurnOnTile;

        public boolean isKeyTurn(){
                return keyTurnU != 1;
        }

        /**
         * should only be called by Move when move blocked
         * @param nextLocation
         * @return
         */
        protected boolean useKey(Pair nextLocation) {
                if(doubleTapToOpenDoor){
                        return useKeyDoubleTap(nextLocation);
                }

                Tile nextTile = token.floorMap.getTile(nextLocation);
                if(nextTile.isDoor() && nextTile.isDoorLocked()){
                        if(nextTile.doorSymbol instanceof KeyItem){
                                boolean hasKey = token.inventory.containsKey((KeyItem) nextTile.doorSymbol);
                                if(hasKey){
                                        keyTurnU = 0;
                                        showDoorLockedMessage = true;
                                        keyTurnOnTile = nextTile;
                                        //nextTile.setDoorLocked(false);
                                        //token.inventory.useKey((KeyItem) nextTile.doorSymbol);
                                }else{
                                        // cant open door, player does not have key for door
                                        if(showDoorLockedMessage){ // if statement prevents spamming onPathBlocked
                                                showDoorLockedMessage = false;
                                                if (token.listener != null)
                                                        token.listener.onPathBlocked(nextLocation, nextTile);
                                        }

                                }
                        }else{
                                // cant interact with non key locked door, player needs to solve puzzle and door will become unlocked
                                if(showDoorLockedMessage){ // if statement prevents spamming onPathBlocked
                                        showDoorLockedMessage = false;
                                        if (token.listener != null)
                                                token.listener.onPathBlocked(nextLocation, nextTile);
                                }
                        }
                        return true;
                }
                showDoorLockedMessage = true;
                return false;
        }

        private boolean useKeyDoubleTap(Pair nextLocation) {
                Tile nextTile = token.floorMap.getTile(nextLocation);
                if (nextTile.isDoor() && nextTile.isDoorLocked()) {
                        boolean key = false;
                        if (nextTile.doorSymbol instanceof KeyItem) {
                                key = token.inventory.containsKey((KeyItem) nextTile.doorSymbol);
                        }
                        if (token.command.isUseKey() && nextTile == token.command.getUseKeyOnTile()) {
                                //token.getTarget().setUseKey(false);
                                if (!key) {
                                        //Gdx.app.log("Move","Try to use key but do not have a key");
                                        //token.command.setLocation(token.location); // cant open door no key stop trying to move in to the door its pointless
                                        //if(token.listener != null)
                                        //        token.listener.onPathBlocked(nextLocation, nextTile);
                                        return true;

                                } else {
                                        //Gdx.app.log("Move","Unlocking door");
                                        keyTurnU = 0;
                                        keyTurnOnTile = nextTile;
                                        //nextTile.setDoorLocked(false);
                                        //token.inventory.useKey((KeyItem) nextTile.doorSymbol);

                                        return true;

                                }
                        } else {
                                //token.command.setLocation(token.location); // cant open door no key stop trying to move in to the door its pointless
                                if (token.command.canUseKeyOnTile == null) {
                                        //Gdx.app.log("Move","Ran in to locked door, but does not have open command, tile: "+token.command.canUseKeyOnTile);
                                        token.command.canUseKeyOnTile = nextTile;
                                        if (token.listener != null)
                                                token.listener.onPathBlocked(nextLocation, nextTile);
                                }


                                return true;

                        }
                }
                return false;
        }
        /**
         * should only be called by after unlocking door in update
         *
         * @param key
         */
        private void useKey(KeyItem key) {
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

        @Override
        public boolean canTeleport(FloorMap fm, int x, int y, Direction direction) {
                return keyTurnU == 1;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {

        }


        public static class UseItemOutcome {
                public Token targetToken;
                public Item targetItem;
                public boolean didSomething;
                public int damage;
        }
}
