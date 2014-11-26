package asf.dungeon.model.item;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.token.Inventory;
import asf.dungeon.model.token.Journal;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/24/2014.
 */
public class BookItem extends AbstractItem implements ConsumableItem.TargetsItems{



        public static enum Type{
                Identify,
                Map, // reveals whole floor, but not items or monsters
                ItemDetection, // reveals locations of crates
                Sleep, // reader falls asleep for x seconds
                EnchantWeapon,
                EnchantArmor,
                EnchantRing,
                AggravateMonsters, // monsters from around the floor come to attack you
                RemoveCurse,
                Experience // increases intelligence stat
        }

        public static enum Symbol{
                Alpha, Beta, Gamma, Delta, Epsilon, Zeta, Eta, Theta, Iota, Kappa
        }

        public boolean isPrimarilySelfConsume(){
                return type == Type.Map || type == Type.ItemDetection || type == Type.Sleep || type == Type.AggravateMonsters || type == Type.Experience;
        }

        @Override
        public void consume(Token token, Inventory.Character.UseItemOutcome out) {

                switch(type){
                        case Map:
                                // reveal map
                                token.getFogMapping().getCurrentFogMap().revealMap();
                                out.didSomething = true;
                                break;
                        case ItemDetection:
                                // reveal location of crates
                                FogMap fogMap = token.getFogMapping().getCurrentFogMap();
                                Array<Token> crateAndLootTokens = token.getFloorMap().getCrateAndLootTokens();
                                for (Token t : crateAndLootTokens) {
                                        fogMap.revealLocation(t.getLocation().x, t.getLocation().y);
                                }
                                out.didSomething = true;
                                break;
                        case Sleep:
                                // cause player to fall asleep for period of time
                                out.didSomething = true;
                                break;
                        case AggravateMonsters:
                                // tokens in entire floor immediatly get aggro for player
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
                                new Error().printStackTrace();
                                out.didSomething = false;
                                // do nothing
                                break;
                        default:
                                throw new AssertionError(type);
                }
                identifyItem(token);

        }

        @Override
        public void consume(Token token, Item targetItem, Inventory.Character.UseItemOutcome out) {
                out.didSomething = true;
                if(type == Type.Identify){
                        targetItem.identifyItem(token);
                }else if(type == Type.EnchantWeapon){
                        WeaponItem weapon = (WeaponItem) targetItem;
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
