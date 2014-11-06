package asf.dungeon.model;

/**
 * Created by Danny on 11/5/2014.
 */
public class HealthPotion implements Item {
        @Override
        public String getName() {
                return "Health Potion";
        }

        @Override
        public ModelId getModelId() {
                return ModelId.HealthPotion;
        }

        @Override
        public boolean isConsumable() {
                return true;
        }

        @Override
        public void consume(CharacterToken token) {
                token.addHealth(3);
        }
}
