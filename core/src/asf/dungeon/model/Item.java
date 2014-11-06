package asf.dungeon.model;

/**
 * Created by Danny on 11/5/2014.
 */
public interface Item {

        public String getName();

        public ModelId getModelId();

        public boolean isConsumable();

        public void consume(CharacterToken token);
}
