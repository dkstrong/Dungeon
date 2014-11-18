package asf.dungeon.model.item;


import asf.dungeon.model.Dungeon;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/5/2014.
 */
public class KeyItem extends AbstractItem implements Item {
        public static enum Type{
                Silver(ModelId.Key),
                Gold(ModelId.Key2),
                Red(ModelId.Key3);
                // TODO: transient?
                private final ModelId modelId;

                Type(ModelId modelId) {
                        this.modelId = modelId;
                }
        }

        private final Type type;

        public KeyItem(Dungeon dungeon, Type type) {
                this.type = type;
        }

        @Override
        public ModelId getModelId() {
                return type.modelId;
        }

        @Override
        public String getName() {
                return type.name()+" Key";
        }

        @Override
        public String getDescription() {
                return "This key can be used to open a locked "+ type.name()+" door.";
        }

        @Override
        public String getVagueName() { return getName();}

        @Override
        public String getVagueDescription() { return getDescription();}

        @Override
        public boolean isIdentified(Token token) { return true; }

        public Type getType() {
                return type;
        }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                KeyItem that = (KeyItem) o;

                if (type != that.type) return false;

                return true;
        }

        @Override
        public int hashCode() {
                return type != null ? type.hashCode() : 0;
        }

}
