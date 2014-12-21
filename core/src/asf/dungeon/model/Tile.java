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
        private boolean door;
        private Symbol doorSymbol;
        private boolean doorForcedOpen;

        private Tile(boolean blockMovement, boolean blockVision) {
                this.blockMovement = blockMovement;
                this.blockVision = blockVision;
        }

        private Tile(boolean doorLocked, Symbol doorSymbol) {
                this.door = true;
                this.blockVision = true;
                this.blockMovement = doorLocked;
                this.doorSymbol = doorSymbol;
        }


        public boolean isBlockMovement() {
                return blockMovement;
        }

        public boolean isBlockVision() {
                return blockVision;
        }

        public boolean isWall() { return !isDoor()  && blockMovement; }

        public boolean isFloor() { return !isDoor() && !blockMovement && !blockVision; }

        public boolean isDoor() {
                return door;
        }

        public boolean isDoorOpened() {
                return !blockVision;
        }

        public boolean isDoorLocked() {
                return blockMovement;
        }

        public Symbol getDoorSymbol() {
                return doorSymbol;
        }

        public void setDoorOpened(boolean opened) { blockVision = !opened && !doorForcedOpen; }

        public void setDoorLocked(boolean locked) {
                blockMovement = locked;
        }

        public void setDoorLocked(boolean locked, Symbol doorSymbol) {
                blockMovement = locked;
                this.doorSymbol = doorSymbol;
        }

        public boolean isDoorForcedOpen() { return doorForcedOpen; }

        public void setDoorForcedOpen(boolean doorForcedOpen) {
                this.doorForcedOpen = doorForcedOpen;
                blockVision = !doorForcedOpen;
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

        public static Tile makeDoor(Symbol keyType) { return new Tile(true, keyType); }

        @Override
        public String toString() {
                return String.valueOf(toCharacter());
        }

        public char toCharacter(){
                if (isFloor())
                        return '.';

                if (isWall())
                        return '|';

                if (isDoor())
                        if(isDoorLocked())
                                if(getDoorSymbol() instanceof KeyItem)
                                        return '/';
                                else
                                        return '\\';
                        else
                                return '+';

                return '?';
        }
}
