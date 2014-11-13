package asf.dungeon.model;


import asf.dungeon.model.token.Effect;
import asf.dungeon.model.token.Journal;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/5/2014.
 */
public class PotionItem implements Item.Consumable {
        private final Color color;
        private final Type type;

        public PotionItem(Dungeon dungeon, Type type) {
                this.color = dungeon.getMasterJournal().getPotionColor(type);
                this.type = type;
        }

        @Override
        public ModelId getModelId() {
                return ModelId.Potion;
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
        public String getNameFromJournal(Token token){
                Journal journal = token.get(Journal.class);
                if(journal != null && journal.knows(type))
                        return getName();
                return color.name()+" Potion";
        }
        public String getDescriptionFromJournal(Token token){
                Journal journal = token.get(Journal.class);
                if(journal != null && journal.knows(type))
                        return "This is a "+getName();
                return "A mysterious "+color.name()+" potion. The effects of drinking this are not known.";
        }

        @Override
        public void consume(Token token) {
                StatusEffects statusEffects = token.get(StatusEffects.class);
                if(statusEffects == null)
                        return;
                switch(type){
                        case Health:
                                statusEffects.addStatusEffect(Effect.Heal, 3, 3);
                                break;
                        case Experience:
                                break;
                        case Invisibility:
                                statusEffects.addStatusEffect(Effect.Invisibility, 10,1);
                                break;
                        case Purity:
                                statusEffects.removeNegativeStatusEffects();
                                break;
                        case Poison:
                                statusEffects.addStatusEffect(Effect.Poison, 10, 5);
                                break;
                        case Paralyze:
                                statusEffects.addStatusEffect(Effect.Paralyze, 5,1);
                                break;
                        case MindVision:
                                statusEffects.addStatusEffect(Effect.MindVision, 5,1);
                                break;
                        case Strength:
                                break;
                        case Might:
                                break;
                        case Speed:
                                statusEffects.addStatusEffect(Effect.Speed, 10,1);
                                break;
                }
                Journal journal = token.get(Journal.class);
                if(journal != null)
                        journal.learn(type);

        }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                PotionItem that = (PotionItem) o;

                if (type != that.type) return false;

                return true;
        }

        @Override
        public int hashCode() {
                return type != null ? type.hashCode() : 0;
        }

        public static enum Type{
                Health, Experience, Invisibility, Purity, Poison, Paralyze, MindVision, Strength, Might, Speed;
        }

        public static enum Color{
                LightBlue(com.badlogic.gdx.graphics.Color.TEAL,"Models/Loot/Potion/potion_silver_blue.png"),
                Red(com.badlogic.gdx.graphics.Color.RED,"Models/Loot/Potion/potion_silver_red.png"),
                Blue(com.badlogic.gdx.graphics.Color.BLUE,"Models/Loot/Potion/potion_silver_blue.png"),
                Green(com.badlogic.gdx.graphics.Color.GREEN,"Models/Loot/Potion/potion_silver_green.png"),
                Yellow(com.badlogic.gdx.graphics.Color.YELLOW,"Models/Loot/Potion/potion_silver_blue.png"),
                Magenta(com.badlogic.gdx.graphics.Color.MAGENTA,"Models/Loot/Potion/potion_silver_blue.png"),
                Black(com.badlogic.gdx.graphics.Color.BLACK,"Models/Loot/Potion/potion_silver_blue.png"),
                Brown(com.badlogic.gdx.graphics.Color.OLIVE,"Models/Loot/Potion/potion_silver_blue.png"),
                Amber(com.badlogic.gdx.graphics.Color.ORANGE,"Models/Loot/Potion/potion_silver_blue.png"),
                White(com.badlogic.gdx.graphics.Color.WHITE,"Models/Loot/Potion/potion_silver_blue.png"),
                Silver(com.badlogic.gdx.graphics.Color.GRAY,"Models/Loot/Potion/potion_silver_blue.png"),
                Purple(com.badlogic.gdx.graphics.Color.PURPLE,"Models/Loot/Potion/potion_silver_blue.png");

                public final com.badlogic.gdx.graphics.Color color;
                public final String textureAssetLocation;

                Color(com.badlogic.gdx.graphics.Color color, String textureAssetLocation) {
                        this.color = color;
                        this.textureAssetLocation = textureAssetLocation;
                }
        }
}
