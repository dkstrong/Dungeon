package asf.dungeon.model;


import asf.dungeon.model.item.KeyItem;

/**
 * Created by danny on 10/26/14.
 */
public class Tile {
        private static transient final Tile floorTile = new Tile(false, false);
        private static transient final Tile wallTile = new Tile(true, true);
        private static transient final Tile fauxWallTile = new Tile(true, false, true);

        public int movementCost;
        /**
         * do not modify directly, modify using mutators like setDoorLocked() and setPitFilled()
         */
        public boolean blockMovement;
        /**
         * do not modify directly, modify using mutators like setDoorLocked() and setPitFilled()
         */
        public boolean forceAsFloor = false;
        /**
         * do not modify directly, modify using mutators like setDoorOpened()
         */
        public boolean blockVision;
        private boolean pit;
        private boolean door;
        /**
         * do not modify directly, modify using setDoorLocked()
         */
        public Symbol doorSymbol;
        private boolean doorForcedOpen;

        private Tile(boolean pit) {
                this.pit = pit;
                blockMovement = this.pit;
                blockVision = false;
                movementCost = 20;
        }
        private Tile(boolean blockMovement, boolean blockVision) {
                this.blockMovement = blockMovement;
                this.blockVision = blockVision;
        }

        private Tile(boolean blockMovement, boolean blockVision, boolean forceAsFloor) {
                this.blockMovement = blockMovement;
                this.blockVision = blockVision;
                this.forceAsFloor = forceAsFloor;
        }

        private Tile(boolean doorLocked, Symbol doorSymbol) {
                this.door = true;
                this.blockVision = true;
                this.blockMovement = doorLocked;
                this.doorSymbol = doorSymbol;
        }

        public boolean isFauxWall() {return forceAsFloor;}

        public boolean isWall() { return !pit && !door && blockMovement && blockVision; }

        public boolean isFloor() { return !pit && !door && !blockMovement && !blockVision; }

        public boolean isPit(){return pit; }

        public boolean isPitFilled(){ return !blockMovement; }

        public void setPitFilled(boolean filled){ this.blockMovement = !filled; }

        public boolean isDoor() {
                return door;
        }

        public boolean isDoorOpened() { return !blockVision; }

        public boolean isDoorLocked() {
                return blockMovement;
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

        public static Tile makeFloor() {
                return floorTile;
        }

        public static Tile makeWall() {
                return wallTile;
        }

        public static Tile makeFauxWall() {return fauxWallTile; }

        public static Tile makePit() {
                return new Tile(true);
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

                if(isFauxWall())
                        return ';';

                if (isPit())
                        return ':';

                if (isDoor())
                        if(isDoorLocked())
                                if(doorSymbol instanceof KeyItem)
                                        return '/';
                                else
                                        return '\\';
                        else
                                return '+';

                return '?';
        }
}
