package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FloorType;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;

/**
 * Created by Daniel Strong on 12/20/2014.
 */
public class Stairs implements TokenComponent, Teleportable{
        public final Token token;
        public final int stairsTo;

        public Stairs(Token token, int stairsTo) {
                this.token = token;
                this.stairsTo = stairsTo;
                token.blocksPathing = false;
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

        @Override
        public boolean canTeleport(FloorMap fm, int x, int y, Direction direction) {
                return true;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {
                if(isStairsUp()){
                        token.modelId = token.floorMap.floorType == FloorType.Grassy ? ModelId.StairsUp : ModelId.StairsUp;
                }else{
                        token.modelId = token.floorMap.floorType == FloorType.Grassy ? ModelId.Church : ModelId.StairsDown;
                }
        }
}
