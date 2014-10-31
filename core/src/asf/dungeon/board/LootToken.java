package asf.dungeon.board;

/**
 *
 * an item sitting freely on the ground, it does not block pathing, when a character steps on
 * it the item is then consumed. this could mean adding to inventory or donig some immediate
 * effect depending on what the item is.
 *
 * Created by danny on 10/25/14.
 */
public class LootToken extends Token {
        private float removed = Float.NaN;

        protected LootToken(Dungeon dungeon, FloorMap floorMap, int id, String name) {
                super(dungeon, floorMap, id, name);
                blocksPathing = false;
        }

        @Override
        protected void incremenetU(float delta) {
                removed -=delta;
                if(removed < 0){
                        dungeon.removeToken(this);
                }
        }

        protected void becomeRemoved(){
                if(isRemoved())
                        return;
                removed = .5f;
        }

        public boolean isRemoved(){
                return !Float.isNaN(removed);
        }

}
