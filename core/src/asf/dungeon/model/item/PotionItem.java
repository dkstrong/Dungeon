package asf.dungeon.model.item;


import asf.dungeon.model.Dungeon;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.token.Journal;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/5/2014.
 */
public class PotionItem extends AbstractItem implements Consumable {
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

                public final com.badlogic.gdx.graphics.Color color; // TODO: transient?
                public final String textureAssetLocation; // TODO: transient?

                Color(com.badlogic.gdx.graphics.Color color, String textureAssetLocation) {
                        this.color = color;
                        this.textureAssetLocation = textureAssetLocation;
                }
        }

        private final Color color;
        private final Type type;

        public PotionItem(Dungeon dungeon, Type type) {
                this.color = dungeon.getMasterJournal().getPotionColor(type);
                this.type = type;
        }

        @Override
        public ModelId getModelId() { return ModelId.Potion; }

        @Override
        public String getName() {
                return type.name()+" Potion";
        }

        @Override
        public String getDescription() {
                return "This is a "+getName()+". Go ahead. Drink it.";
        }

        @Override
        public String getVagueName() { return color.name()+" Potion"; }

        @Override
        public String getVagueDescription() { return "A mysterious "+color.name()+" potion. The effects of drinking this are not known.";}

        @Override
        public boolean isIdentified(Token token) { Journal journal = token.get(Journal.class); return journal == null || journal.knows(type); }

        public Type getType() { return type; }

        public Color getColor(){
                return color;
        }

        @Override
        public void consume(Token token) {
                StatusEffects statusEffects = token.get(StatusEffects.class);
                if(statusEffects == null)
                        return;
                switch(type){
                        case Health:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Heal, 3, 3);
                                break;
                        case Experience:
                                break;
                        case Invisibility:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Invisibility, 10,1);
                                break;
                        case Purity:
                                statusEffects.removeNegativeStatusEffects();
                                break;
                        case Poison:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Poison, 10, 5);
                                break;
                        case Paralyze:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Paralyze, 5,1);
                                break;
                        case MindVision:
                                statusEffects.addStatusEffect(StatusEffects.Effect.MindVision, 5,1);
                                break;
                        case Strength:
                                break;
                        case Might:
                                break;
                        case Speed:
                                statusEffects.addStatusEffect(StatusEffects.Effect.Speed, 10,1);
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


}
