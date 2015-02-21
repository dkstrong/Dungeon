package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;

/**
 * Created by Daniel Strong on 2/20/2015.
 */
public class Decor implements TokenComponent, Teleportable{
        private final Token token;

        public Decor(Token token) {
                this.token = token;
        }

        @Override
        public boolean canTeleport(FloorMap fm, int x, int y, Direction direction) {
                //if(token.floorMap != null) return false;
                final Mask mask= token.modelId.decorMask;
                if(mask != null){
                        mask.setRotation(direction);
                        for (Pair maskPoint : mask.maskPoints) {
                                final int testX = x+maskPoint.x;
                                final int testY = y+maskPoint.y;
                                Tile tile = fm.getTile(testX,testY);
                                if(tile == null || tile.isDoor() || tile.isPit() || tile.blockMovement) return false;
                                if(fm.hasTokensAt(testX, testY)) return false;
                        }
                }
                return true;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {
                // TODO: this doesnt remove old invisble walls- would need to store the last teleported location
                // and turn the invisble walls back in to floors to do this
                final Mask mask= token.modelId.decorMask;
                if(mask != null){
                        mask.setRotation(direction);
                        for (Pair maskPoint : mask.maskPoints) {
                                final int tx = x+maskPoint.x;
                                final int ty = y+maskPoint.y;
                                fm.tiles[tx][ty] = Tile.makeInvisibleWall();
                        }
                }

        }

        @Override
        public boolean update(float delta) {
                return false;
        }

        /**
         * a Decor mask explains what surrounding tiles are required to become
         * invisble walls for this decor to be placed. the origin of the Mask is 0,0
         * and the maskpoints are relative to that location
         */
        public static class Mask{
                public Direction dir;
                public Pair[] maskPoints;

                public Mask(Pair... maskPoints) {
                        dir = Direction.South;
                        this.maskPoints = maskPoints;
                }

                public void setRotation(Direction newDir){
                        for (Pair maskPoint : maskPoints) {
                                maskPoint.rotate(dir, newDir);
                        }
                        dir = newDir;
                }
        }

}
