package asf.dungeon.board;

import asf.dungeon.board.pathfinder.Tile;

/**
 * Created by danny on 10/26/14.
 */
public class FloorTile implements Tile {
        private int movementCost;
        private boolean blockMovement;
        private boolean blockVision;
        private int stairsTo;

        public FloorTile(boolean blockMovement, boolean blockVision) {
                this.blockMovement = blockMovement;
                this.blockVision=blockVision;
                this.stairsTo = -1;
        }

        public FloorTile(boolean blockVision, int stairsTo) {
                this.blockMovement = false;
                this.blockVision=blockVision;
                this.stairsTo = stairsTo;
        }

        @Override
        public boolean isBlockMovement() {
                return blockMovement;
        }

        public boolean isBlockVision(){return blockVision; }

        public boolean isStairs(){
                return stairsTo >= 0;
        }

        public int getStairsTo(){
                return stairsTo;
        }

        @Override
        public int getMovementCost() {
                return movementCost;
        }


}
