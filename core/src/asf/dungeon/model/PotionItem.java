package asf.dungeon.model;


/**
 * Created by Danny on 11/5/2014.
 */
public class PotionItem implements ConsumableItem {
        private final Color color;
        private final Type type;

        public PotionItem(Dungeon dungeon, Type type) {
                this.color = dungeon.getMasterJournal().getPotionColor(type);
                this.type = type;
        }

        @Override
        public ModelId getModelId() {
                return ModelId.HealthPotion;
        }

        @Override
        public String getName() {
                return type.name();
        }

        @Override
        public String getDescription() {
                return "This is a "+getName()+". Go ahead. Drink it.";
        }

        public Type getType() {
                return type;
        }

        public Color getColor(){
                return color;
        }

        @Override
        public String getNameFromJournal(CharacterToken token){

                if(!token.isJournalEnabled() || token.getJournal().knows(type))
                        return getName();
                return color.name()+" Potion";
        }
        public String getDescriptionFromJournal(CharacterToken token){
                if(!token.isJournalEnabled() || token.getJournal().knows(type))
                        return "This is a "+getName();
                return "A mysterious "+color.name()+" potion. The effects of drinking this are not known.";
        }

        @Override
        public void consume(CharacterToken token) {
                switch(type){
                        case Health:
                                token.addStatusEffect(StatusEffect.Heal, 1, 3);
                                break;
                        case Experience:
                                break;
                        case Invisibility:
                                token.addStatusEffect(StatusEffect.Invisibility, 10,1);
                                break;
                        case Purity:
                                token.removeNegativeStatusEffects();
                                break;
                        case Poison:
                                token.addStatusEffect(StatusEffect.Poison, 10, 5);
                                break;
                        case Paralyze:
                                token.addStatusEffect(StatusEffect.Paralyze, 5,1);
                                break;
                        case MindVision:
                                token.addStatusEffect(StatusEffect.MindVision, 5,1);
                                break;
                        case Strength:
                                break;
                        case Might:
                                break;
                        case Speed:
                                token.addStatusEffect(StatusEffect.Speed, 10,1);
                                break;
                }
                if(token.isJournalEnabled())
                        token.getJournal().learn(type);

        }

        public static enum Type{
                Health, Experience, Invisibility, Purity, Poison, Paralyze, MindVision, Strength, Might, Speed;
        }

        public static enum Color{
                LightBlue(com.badlogic.gdx.graphics.Color.TEAL),
                Red(com.badlogic.gdx.graphics.Color.RED),
                Blue(com.badlogic.gdx.graphics.Color.BLUE),
                Green(com.badlogic.gdx.graphics.Color.GREEN),
                Yellow(com.badlogic.gdx.graphics.Color.YELLOW),
                Magenta(com.badlogic.gdx.graphics.Color.MAGENTA),
                Black(com.badlogic.gdx.graphics.Color.BLACK),
                Brown(com.badlogic.gdx.graphics.Color.OLIVE),
                Amber(com.badlogic.gdx.graphics.Color.ORANGE),
                White(com.badlogic.gdx.graphics.Color.WHITE),
                Silver(com.badlogic.gdx.graphics.Color.GRAY),
                Purple(com.badlogic.gdx.graphics.Color.PURPLE);

                public final com.badlogic.gdx.graphics.Color color;

                Color(com.badlogic.gdx.graphics.Color color) {
                        this.color = color;
                }
        }
}
