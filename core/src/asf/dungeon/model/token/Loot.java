package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;
import asf.dungeon.model.item.Item;
import asf.dungeon.utility.UtMath;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Danny on 11/11/2014.
 */
public class Loot implements TokenComponent, TeleportValidator {
        private final Token token;
        private Item item;
        private Pair throwDestination;
        private float throwU;
        private float throwMaxU=Float.NaN;


        public Loot(Token token, Item item) {
                this.token = token;
                this.item = item;
                token.blocksPathing = false;
        }

        @Override
        public boolean isGoodSpawnLocation(FloorMap fm, int x, int y, Direction dir) {
                return true;
        }

        @Override
        public boolean canTeleport(FloorMap fm, int x, int y, Direction direction) {
                return throwDestination == null;
        }

        @Override
        public boolean update(float delta) {
                if(throwDestination != null){
                        throwU+=delta;
                        if(throwU > throwMaxU){
                                token.location.set(throwDestination);
                                throwDestination = null;
                        }
                        return true;
                }
                return false;
        }

        public boolean isBeingThrown(){
                return throwDestination != null;
        }

        public float getFloatLocationX(){
                if(throwDestination == null) return token.location.x;
                return MathUtils.lerp(token.location.x, throwDestination.x, UtMath.hermiteT(throwU/throwMaxU));
        }

        public float getFloatLocationY(){
                if(throwDestination == null) return token.location.y;
                return MathUtils.lerp(token.location.y, throwDestination.y, UtMath.hermiteT(throwU/throwMaxU));
        }

        protected void becomeThrown(int destX, int destY){
                throwDestination = new Pair(destX, destY);
                throwU =0;
                throwMaxU = token.distance(throwDestination) / 3f;
        }

        protected void becomeRemoved(){
                token.dungeon.removeToken(token);
                item = null;
        }

        public boolean canbePickedUp(){
                return item!=null && throwDestination == null;
        }

        public Item getItem(){
                return item;
        }
}
