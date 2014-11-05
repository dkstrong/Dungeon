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

        public boolean isWall(){
                return blockMovement && blockVision;
        }

        public boolean isFloor(){
                return stairsTo <0 && !blockMovement && !blockVision;
        }

        public boolean isDoor(){
                return stairsTo <0 && !blockMovement && blockVision;
        }

        public boolean isStairs(){
                return stairsTo >= 0;
        }

        public int getStairsTo(){
                return stairsTo;
        }

        /**
         * if the stairs go to an upper floor or a lower floor
         * @param currentFloorIndex
         * @return
         */
        public boolean isStairsUp(int currentFloorIndex){
                return stairsTo < currentFloorIndex;
        }

        @Override
        public int getMovementCost() {
                return movementCost;
        }

        public static FloorTile makeFloor(){
                return new FloorTile(false, false);
        }

        public static FloorTile makeWall(){
                return new FloorTile(true, true);
        }

        public static FloorTile makeDoor(){
                return new FloorTile(false, true);
        }

        public static FloorTile makeStairs(int currentFloorIndex, int stairsTo){
                return new FloorTile(stairsTo<currentFloorIndex, stairsTo);
        }

        @Override
        public String toString() {
                if(blockMovement == false && blockVision == false)
                        return "."; // Floor

                if(blockMovement == true && blockVision == true)
                        return "|";   // Wall

                if(isStairs()){
                        return "^"; // TODO: this wont displat & for stair down
                }


                if(blockMovement == false && blockVision == true)
                        return "+";  // Door

                return "?";
        }
}
