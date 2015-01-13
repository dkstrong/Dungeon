package asf.dungeon.model.item;


import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.M;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Symbol;
import asf.dungeon.model.Tile;
import asf.dungeon.model.token.Token;

/**
 *
 * // TODO: KeyItem just has a single member variable to its type, KeyItem itself could jsut be an enum
 *
 * Created by Danny on 11/5/2014.
 */
public class KeyItem extends AbstractItem implements Item, Symbol {


        public static enum Type{
                Silver, Gold, Red;
        }

        private final Type type;

        public static transient final Type[] typeValues = Type.values();

        public KeyItem(Type type) {
                this.type = type;
                M.generateNameDesc(this);
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

        @Override
        public float getIntensity() {
                return (type.ordinal()) / 10f;
        }

        @Override
        public void lockDoor(Dungeon dungeon, FloorMap floorMap,Tile tile) {
                tile.setDoorLocked(true, this);
        }


}
