package asf.dungeon.model;

/**
 * Created by danny on 10/25/14.
 */
public class CrateToken extends DamageableToken {
        private Item item;

        protected CrateToken(Dungeon dungeon, FloorMap floormMap,  int id, String name, ModelId modelId, Item item) {
                super(dungeon, floormMap,id, name, modelId, 1);
                this.setDeathDuration(2.5f);
                this.setDeathRemovalCountdown(.25f);
                this.item = item;
        }

        @Override
        protected void receiveDamageFrom(CharacterToken token) {
                addHealth(-1);
        }

        @Override
        protected void onDied() {
                if(item != null)
                        dungeon.newLootToken(floorMap, item, location.x, location.y);
        }
}
