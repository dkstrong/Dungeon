package asf.dungeon.model;

/**
 *
 * an item sitting freely on the ground, it does not block pathing, when a character steps on
 * it the item is then consumed. this could mean adding to inventory or doing some immediate
 * effect depending on what the item is.
 *
 * Created by danny on 10/25/14.
 */
public class LootToken extends Token {
        private boolean removed = false;
        private Item item;

        protected LootToken(Dungeon dungeon, FloorMap floorMap, int id, Item item) {
                super(dungeon, floorMap, id, item.getName(), item.getModelId());
                this.item = item;
                blocksPathing = false;
        }

        @Override
        protected void incremenetU(float delta) {

                if(removed )
                        dungeon.removeToken(this);

        }

        protected void becomeRemoved(){
                removed = true;
        }

        public boolean isRemoved(){
                return removed;
        }

        public Item getItem(){
                return item;
        }

}
