package asf.dungeon.model.item;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.token.CharacterInventory;
import asf.dungeon.model.token.Journal;
import asf.dungeon.model.token.StatusEffect;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/24/2014.
 */
public class BookItem extends AbstractItem implements ConsumableItem.TargetsItems{



        public static enum Type{
                Identify,
                MagicMapping, // reveals whole floor, but not items or monsters
                ItemDetection, // reveals locations of crates
                Sleep, // reader falls asleep for x seconds
                EnchantWeapon,
                EnchantArmor,
                EnchantRing,
                ExtraQuickSlot,
                AggravateMonsters, // monsters from around the floor come to attack you
                RemoveCurse,
                Experience
        }

        public static enum Symbol{
                Alpha, Beta, Gamma, Delta, Epsilon, Zeta, Eta, Theta, Iota, Kappa, Lambda;
        }

        public boolean isPrimarilySelfConsume(){
                return type == Type.MagicMapping || type == Type.ItemDetection || type == Type.Sleep || type== Type.ExtraQuickSlot || type == Type.AggravateMonsters || type == Type.Experience;
        }

        @Override
        public void consume(Token token, CharacterInventory.UseItemOutcome out) {

                switch(type){
                        case MagicMapping:
                                // reveal map and crates
                                token.getFogMapping().getCurrentFogMap().revealMapWithMagic();
                                out.didSomething = true;
                                break;
                        case ItemDetection:
                                // reveal location of crates
                                token.getStatusEffects().add(StatusEffect.ItemVision, 15);
                                out.didSomething = true;
                                break;
                        case Sleep:
                                // cause player to fall asleep for period of time
                                token.getStatusEffects().add(StatusEffect.Paralyze, 5);
                                out.didSomething = true;
                                break;
                        case ExtraQuickSlot:
                                // adds an extra quick slot to the players inventory
                                CharacterInventory inventory = token.getInventory();
                                int numSlots = inventory.numQuickSlots();
                                out.didSomething = numSlots < 3;
                                if(out.didSomething)
                                        inventory.setNumQuickSlots(numSlots+1);
                                break;
                        case AggravateMonsters:
                                // tokens in entire floor immediatly get aggro for player
                                token.getStatusEffects().add(StatusEffect.LuresMonsters, 60);
                                out.didSomething = true;
                                break;
                        case Experience:
                                // player gains some experience
                                token.getExperience().addXp(5);
                                out.didSomething = true;
                                break;
                        case Identify:
                        case EnchantWeapon:
                        case EnchantArmor:
                        case EnchantRing:
                        case RemoveCurse:
                                //new Error().printStackTrace();
                                out.didSomething = false;
                                // do nothing
                                break;
                        default:
                                throw new AssertionError(type);
                }
                identifyItem(token);

        }

        @Override
        public void consume(Token token, Item targetItem, CharacterInventory.UseItemOutcome out) {
                out.didSomething = true;
                if(type == Type.Identify){
                        targetItem.identifyItem(token);
                }else if(type == Type.EnchantWeapon){
                        WeaponItem weapon = (WeaponItem) targetItem;
                        // TODO: only books that i still need to code are the EnchantXXX books
                }else if(type == Type.EnchantArmor){
                        ArmorItem armor = (ArmorItem) targetItem;
                }else if(type == Type.EnchantRing){
                        RingItem ring = (RingItem) targetItem;
                }else if(type == Type.RemoveCurse){
                        EquipmentItem equipment = (EquipmentItem ) targetItem;
                        equipment.setCursed(false);
                        // TODO: this is a weird place to invoke onInventoryChanged, i might want to come up with a way to channel this through inventory
                        if(token.getListener() != null)
                                token.getListener().onInventoryChanged();
                }else{
                        throw new AssertionError(type);
                }
                identifyItem(token);

        }



        @Override
        public boolean canConsume(Token token, Item targetItem) {
                if(this == targetItem || targetItem == null ||  !token.getInventory().contains(targetItem))
                        return false;
                if(type == Type.Identify){
                        return !targetItem.isIdentified(token);
                }else if(type == Type.EnchantWeapon){
                        return targetItem instanceof WeaponItem;
                }else if(type == Type.EnchantArmor){
                        return targetItem instanceof ArmorItem;
                }else if(type == Type.EnchantRing){
                        return targetItem instanceof RingItem;
                }else if(type == Type.RemoveCurse){
                        if(targetItem instanceof EquipmentItem){
                                EquipmentItem equipment = (EquipmentItem ) targetItem;
                                return equipment.isCursed();
                        }
                }
                return false;
        }



        private final Symbol symbol;
        private final Type type;

        public BookItem(Dungeon dungeon, Type type) {
                this.symbol = dungeon.getMasterJournal().getBookSymbol(type);
                this.type = type;
        }

        // TODO: this needs to be ModelId.Rune
        @Override
        public ModelId getModelId() {
                return ModelId.Scroll;
        }

        @Override
        public String getName() {
                return "Tome of "+type.name();

        }

        @Override
        public String getDescription() {
                return "This is a "+getName()+". It can only be used once. Use it well.";
        }

        @Override
        public String getVagueName() {
                return "Unidentified Tome";
        }

        @Override
        public String getVagueDescription() {
                return "An unidentified Tome. Who knows what will happen once read out loud?";
        }

        @Override
        public boolean isIdentified(Token token) {

                Journal journal = token.get(Journal.class);
                return journal == null || journal.knows(type);

        }

        @Override
        public void identifyItem(Token token) {
                Journal journal = token.get(Journal.class);
                if (journal != null)
                        journal.learn(this);
        }

        public Type getType() {
                return type;
        }

        public Symbol getSymbol() {
                return symbol;
        }
}
