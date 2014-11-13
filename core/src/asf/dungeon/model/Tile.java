package asf.dungeon.model;


import java.io.Serializable;

/**
 * Created by danny on 10/26/14.
 */
public class Tile  {
        private int movementCost;
        private boolean blockMovement;
        private boolean blockVision;
        private int stairsTo;
        private boolean door;
        private boolean opened;
        private boolean locked;
        private int keyId;

        private Tile(boolean blockMovement, boolean blockVision) {
                this.blockMovement = blockMovement;
                this.blockVision = blockVision;
                this.stairsTo = -1;
        }

        private Tile(boolean blockVision, int stairsTo) {
                this.blockMovement = false;
                this.blockVision = blockVision;
                this.stairsTo = stairsTo;
        }

        private Tile(boolean doorOpened, boolean doorLocked, int keyId) {
                this.door =true;
                this.opened = doorOpened;
                this.blockVision = !opened;
                this.locked = doorLocked;
                this.blockMovement = locked;
                this.keyId = keyId;
                this.stairsTo = -1;
        }


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

        public void setDoorOpened(boolean opened) {
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


        public int getMovementCost() {
                return movementCost;
        }

        public static Tile makeFloor() {
                return new Tile(false, false);
        }

        public static Tile makeWall() {
                return new Tile(true, true);
        }

        public static Tile makeDoor() {return new Tile(false, false, 0);}

        public static Tile makeStairs(int currentFloorIndex, int stairsTo) {
                return new Tile(stairsTo < currentFloorIndex, stairsTo);
        }

        @Override
        public String toString() {
                if (isFloor())
                        return "."; // Floor

                if (isWall())
                        return "|";   // Wall

                if (isStairs()) {
                        if(isBlockVision())
                                return "^";
                        else
                                return "&";
                }


                if (isDoor())
                        return "+";  // Door

                return "?";
        }
}
