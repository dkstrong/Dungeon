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
                                token.addHealth(3);
                                break;
                        case Experience:
                                break;
                        case Invisibility:
                                break;
                        case Purity:
                                break;
                        case Poison:
                                break;
                        case Paralyze:
                                break;
                        case MindVision:
                                break;
                        case Strength:
                                break;
                        case Might:
                                break;
                }
                if(token.isJournalEnabled())
                        token.getJournal().learn(type);

        }

        public static enum Type{
                Health, Experience, Invisibility, Purity, Poison, Paralyze, MindVision, Strength, Might;
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
