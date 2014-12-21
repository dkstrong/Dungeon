package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;

/**
 * Created by Daniel Strong on 12/20/2014.
 */
public class Stairs implements TokenComponent{
        public final Token token;
        public final int stairsTo;

        public Stairs(Token token, int stairsTo) {
                this.token = token;
                this.stairsTo = stairsTo;
                token.setBlocksPathing(false);
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {

        }

        @Override
        public boolean update(float delta) {
                return false;
        }

        public boolean isStairsUp() {
                return stairsTo < token.floorMap.index;
        }

        public Pair getLocation(){
                return token.location;
        }

        public Direction getDirection(){
                return token.direction;
        }

}