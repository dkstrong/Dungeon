package asf.dungeon.model.item;


import asf.dungeon.model.Dungeon;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/5/2014.
 */
public class KeyItem extends AbstractItem implements Item {
        public static enum Type{
                Silver, Gold, Red;
        }

        private final Type type;

        public KeyItem(Dungeon dungeon, Type type) {
                this.type = type;
        }

        @Override
        public ModelId getModelId() {
                switch(type){
                        case Silver:
                                return ModelId.Key;
                        case Gold:
                                return ModelId.Key2;
                        case Red:
                                return ModelId.Key3;
                }
                throw new AssertionError(type);
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

        @Override
        public void identifyItem(Token token) {

        }

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
