package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FloorType;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;

/**
 * Created by Daniel Strong on 12/20/2014.
 */
public class Stairs implements TokenComponent, TeleportListener {
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
        public void onTeleport(FloorMap fm, int x, int y, Direction direction) {
                // TODO: this should be passed into the constructor of Token instead..
                // i need to clean up how stairs are created to do this right..
                if(isStairsUp()){
                        token.modelId = token.floorMap.floorType == FloorType.Grassy ? ModelId.StairsUp :
                                        token.floorMap.floorType == FloorType.Church ? ModelId.ChurchDoor :
                                                ModelId.StairsUp;
                }else{
                        token.modelId = token.floorMap.floorType == FloorType.Grassy ? ModelId.Church :
                                        token.floorMap.floorType == FloorType.Church ? ModelId.StairsDown :
                                        ModelId.StairsDown;
                }
        }
}
