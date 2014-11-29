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

        private final Color color;
        private final Type type;
        private int charges;

        public PotionItem(Dungeon dungeon, Type type, int charges) {
                this.color = dungeon.getMasterJournal().getPotionColor(type);
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
                return color.name() + " Potion";
        }

        @Override
        public String getVagueDescription() {
                return "A mysterious " + color.name() + " potion. The effects of drinking this are not known.";
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
                return color;
        }

        public void addChargesFrom(StackableItem otherItem) {
                PotionItem otherPotion = (PotionItem) otherItem;
                this.charges += otherPotion.charges;
                otherPotion.charges = 0;
        }

        @Override
        public int getCharges() {
                return charges;
        }

        @Override
        public void consume(Token token, Inventory.Character.UseItemOutcome out) {
                out.didSomething = true;
                charges--;
                StatusEffects statusEffects = token.get(StatusEffects.class);
                if (statusEffects == null)
                        return;
                switch (type) {
                        case Health:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Heal, 4, 8);
                                break;
                        case Invisibility:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Invisibility, 10, 1);
                                break;
                        case Purity:
                                statusEffects.removeNegativeStatusEffects();
                                break;
                        case Poison:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Poison, 5, 5);
                                break;
                        case Paralyze:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Paralyze, 5, 1);
                                break;
                        case MindVision:
                                statusEffects.addStatusEffect(StatusEffects.Effect.MindVision, 5, 1);
                                break;
                        case Might:
                                break;
                        case Speed:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Speed, 10, 1);
                                break;
                }
                identifyItem(token);

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
