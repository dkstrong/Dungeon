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
        private boolean door;
        private boolean opened;
        private boolean locked;
        private int keyId;

        private FloorTile(boolean blockMovement, boolean blockVision) {
                this.blockMovement = blockMovement;
                this.blockVision = blockVision;
                this.stairsTo = -1;
        }

        private FloorTile(boolean blockVision, int stairsTo) {
                this.blockMovement = false;
                this.blockVision = blockVision;
                this.stairsTo = stairsTo;
        }

        private FloorTile(boolean doorOpened, boolean doorLocked, int keyId) {
                this.door =true;
                this.opened = doorOpened;
                this.blockVision = !opened;
                this.locked = doorLocked;
                this.blockMovement = locked;
                this.keyId = keyId;
                this.stairsTo = -1;
        }

        @Override
        public boolean isBlockMovement() {
                return blockMovement;
        }

        public boolean isBlockVision() {
                return blockVision;
        }

        public boolean isWall() {return !isDoor() && !isStairs() && blockMovement;}

        public boolean isFloor() {return !isDoor() && !isStairs() && !blockMovement && !blockVision;}

        public boolean isDoor() {
                return door;
        }

        public boolean isDoorOpened() {
                return opened;
        }

        public boolean isDoorLocked() {
                return locked;
        }

        public int getKeyId(){
                return keyId;
        }

        protected void setDoorOpened(boolean opened) {
                this.opened = opened;
                blockVision = !this.opened;
        }

        protected void setDoorLocked(boolean locked) {
                this.locked = locked;
                blockMovement = locked;
        }

        public boolean isStairs() {
                return stairsTo >= 0;
        }

        public int getStairsTo() {
                return stairsTo;
        }

        /**
         * if the stairs go to an upper floor or a lower floor
         *
         * @param currentFloorIndex
         * @return
         */
        public boolean isStairsUp(int currentFloorIndex) {
                return stairsTo < currentFloorIndex;
        }

        @Override
        public int getMovementCost() {
                return movementCost;
        }

        public static FloorTile makeFloor() {
                return new FloorTile(false, false);
        }

        public static FloorTile makeWall() {
                return new FloorTile(true, true);
        }

        public static FloorTile makeDoor() {return new FloorTile(false, false, 0);}

        public static FloorTile makeStairs(int currentFloorIndex, int stairsTo) {
                return new FloorTile(stairsTo < currentFloorIndex, stairsTo);
        }

        @Override
        public String toString() {
                if (isFloor())
                        return "."; // Floor

                if (isWall())
                        return "|";   // Wall

                if (isStairs()) {
                        return "^"; // TODO: this wont displat & for stair down
                }


                if (isDoor())
                        return "+";  // Door

                return "?";
        }
}
