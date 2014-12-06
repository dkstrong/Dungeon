package asf.dungeon.model.item;


import asf.dungeon.model.Dungeon;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.token.Inventory;
import asf.dungeon.model.token.Journal;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/5/2014.
 */
public class PotionItem extends AbstractItem implements ConsumableItem, StackableItem , QuickItem{


        public static enum Type {
                Health, Invisibility, Purity, Poison, Paralyze, Blindness, MindVision, Hallucination, Might, Speed;
        }

        public static enum Color {
                 Red, Blue, Green, Yellow, Magenta, Black, Brown, Amber, Teal, Silver;

        }

        private final Dungeon dungeon;
        private final Type type;
        private int charges;

        public PotionItem(Dungeon dungeon, Type type, int charges) {
                this.dungeon = dungeon;
                this.type = type;
                this.charges = charges;
        }

        @Override
        public ModelId getModelId() {
                return ModelId.Potion;
        }

        @Override
        public String getName() {
                return type.name() + " Potion";
        }

        @Override
        public String getDescription() {
                return "This is a " + getName() + ". Go ahead. Drink it.";
        }

        @Override
        public String getVagueName() {
                Color color = getColor();
                return color.name() + " Potion";
        }

        @Override
        public String getVagueDescription() {
                Color color = getColor();
                return "A mysterious " + color.name().toLowerCase() + " potion. The effects of drinking this are not known.";
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

        public Color getColor() {
                return dungeon.getMasterJournal().getPotionColor(type);
        }

        public void stack(StackableItem otherItem) {
                PotionItem otherPotion = (PotionItem) otherItem;
                this.charges += otherPotion.charges;
                otherPotion.charges = 0;
        }

        @Override
        public PotionItem unStack(int numCharges) {
                if(numCharges >= charges) throw new IllegalStateException("numCharges for unstacked item must be less than current charges");
                PotionItem newPotion = new PotionItem(dungeon, type, numCharges);
                charges -= numCharges;
                return newPotion;
        }

        @Override
        public int getCharges() {
                return charges;
        }

        @Override
        public void consume(Token token, Inventory.Character.UseItemOutcome out) {
                out.didSomething = doPotionEffects(token, type);
                charges--;
                identifyItem(token);
        }

        public static boolean doPotionEffects(Token token, PotionItem.Type type){
                StatusEffects statusEffects = token.get(StatusEffects.class);
                if (statusEffects == null)
                        return false;
                switch (type) {
                        case Health:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Heal, 4, 8);
                                return true;
                        case Poison:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Poison, 5, 5);
                                return true;
                        case Invisibility:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Invisibility, 10);
                                return true;
                        case Purity:
                                statusEffects.removeNegativeStatusEffects();
                                return true;
                        case Paralyze:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Paralyze, 5);
                                return true;
                        case Blindness:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Blind, 10);
                                return true;
                        case MindVision:
                                statusEffects.addStatusEffect(StatusEffects.Effect.MindVision, 5);
                                return true;
                        case Hallucination:
                                // TODO: I... forget what this was supposed to do.. I think show loot on the ground that isnt actually there?
                                return true;
                        case Might:
                                // TODO: Attack should check for this, should increase damage dealt and reduce damage received
                                return true;
                        case Speed:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Speed, 10, 1);
                                return true;
                }
                throw new AssertionError(type);
        }






        @Override
        public boolean canStackWith(StackableItem other) {
                if(other instanceof PotionItem){
                        PotionItem otherPotion = (PotionItem) other;
                        return type == otherPotion.type;
                }
                return false;
        }



}
