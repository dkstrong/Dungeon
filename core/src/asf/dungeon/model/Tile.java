package asf.dungeon.model;


import asf.dungeon.model.item.KeyItem;

/**
 * Created by danny on 10/26/14.
 */
public class Tile {
        private static transient final Tile floorTile = new Tile(false, false);
        private static transient final Tile wallTile = new Tile(true, true);

        private int movementCost;
        private boolean blockMovement;
        private boolean blockVision;
        private int stairsTo;
        private boolean door;
        private KeyItem.Type keyType;
        private boolean doorForcedOpen;

        private Tile(boolean blockMovement, boolean blockVision) {
                this.blockMovement = blockMovement;
                this.blockVision = blockVision;
                this.stairsTo = -2;
        }

        private Tile(boolean blockVision, int stairsTo) {
                this.blockMovement = false;
                this.blockVision = blockVision;
                this.stairsTo = stairsTo;
        }

        private Tile(boolean doorLocked, KeyItem.Type keyType) {
                this.door = true;
                this.blockVision = true;
                this.blockMovement = doorLocked;
                this.keyType = keyType;
                this.stairsTo = -2;
        }


        public boolean isBlockMovement() {
                return blockMovement;
        }

        public boolean isBlockVision() {
                return blockVision;
        }

        public boolean isWall() { return !isDoor() && !isStairs() && blockMovement; }

        public boolean isFloor() { return !isDoor() && !isStairs() && !blockMovement && !blockVision; }

        public boolean isDoor() {
                return door;
        }

        public boolean isDoorOpened() {
                return !blockVision;
        }

        public boolean isDoorLocked() {
                return blockMovement;
        }

        /**
         * if this door is unlocked by some other means than a key (such as a puzzle) then this value should be null
         * @return
         */
        public KeyItem.Type getKeyType() {
                return keyType;
        }

        public void setDoorOpened(boolean opened) { blockVision = !opened && !doorForcedOpen; }

        public void setDoorLocked(boolean locked) {
                blockMovement = locked;
        }

        public void setDoorLocked(boolean locked, KeyItem.Type keyType) {
                blockMovement = locked;
                this.keyType = keyType;
        }

        public boolean isDoorForcedOpen() { return doorForcedOpen; }

        public void setDoorForcedOpen(boolean doorForcedOpen) {
                this.doorForcedOpen = doorForcedOpen;
                blockVision = !doorForcedOpen;
        }

        public boolean isStairs() {
                return stairsTo >= -1;
        }

        public int getStairsTo() {
                return stairsTo;
        }

        public boolean isStairsUp(int currentFloorIndex) {
                return stairsTo < currentFloorIndex;
        }

        public int getMovementCost() {
                return movementCost;
        }

        public static Tile makeFloor() {
                return floorTile;
        }

        public static Tile makeWall() {
                return wallTile;
        }

        public static Tile makeDoor() { return new Tile(false, null); }

        public static Tile makeDoor(KeyItem.Type keyType) { return new Tile(true, keyType); }

        public static Tile makeStairs(int currentFloorIndex, int stairsTo) {
                return new Tile(stairsTo < currentFloorIndex, stairsTo);
        }

        @Override
        public String toString() {
                if (isFloor())
                        return ".";

                if (isWall())
                        return "|";

                if (isStairs()) {
                        if (isBlockVision())
                                return "^";
                        else
                                return "&";
                }

                if (isDoor())
                        if(isDoorLocked())
                                return "/";
                        else
                                return "+";

                return "?";
        }
}
