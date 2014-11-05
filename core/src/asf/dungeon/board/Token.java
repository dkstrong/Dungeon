package asf.dungeon.board;

import asf.dungeon.board.pathfinder.Tile;

/**
 * Created by danny on 10/22/14.
 */
public abstract class Token {
        public final Dungeon dungeon;
        private final int id;
        /**
         * the name of this character or item for the interfce
         */
        protected String name;
        /**
         * whether or not other tokens can stand on the same tile as this one. typically crates and characters block pathing, but pickups do not.
         */
        protected boolean blocksPathing = true;

        // state variables
        protected final Pair location = new Pair();                     // the current tile that this token is considered to be standing on
        protected FloorMap floorMap;
        protected Direction direction = Direction.South;          // the direction that this token is facing, this affects certain gameplay mechanics.

        protected Token(Dungeon dungeon, FloorMap floorMap, int id, String name) {
                this.dungeon = dungeon;
                this.floorMap = floorMap;
                this.id = id;
                this.name = name;
        }

        public final boolean teleportToLocation(int x, int y) {
                return teleportToLocation(x,y, Direction.South);
        }

        /**
         * teleports token to this new location, if the new location
         * can not be moved to (normally because it is already occupied) then the move will not work.
         * <p/>
         * this generally is used for setting up the dungeon, could eventually be used for stairways to lead to other rooms.
         *
         * @param x
         * @param y
         * @param direction
         * @return true if moved, false if did not move
         */
        public boolean teleportToLocation(int x, int y, Direction direction){

                FloorTile tile = floorMap.tiles[x][y];
                if (tile == null || tile.isBlockMovement() || tile.isDoor()) {
                        return false;
                }
                if(blocksPathing){
                        for (Token t : floorMap.tokens) {
                                //t != this &&
                                if (t.isBlocksPathing() && t.isLocatedAt(x,y)) {
                                        return false;
                                }
                        }

                }else{


                }
                location.set(x,y);
                this.direction = direction;
                return true;
        }

        public boolean teleportToFloor(int f){
                if(f == floorMap.index)
                        return false;
                boolean down = f > floorMap.index;
                floorMap = dungeon.generateFloor(f);
                Pair stairLoc;
                if(down){
                        stairLoc = floorMap.getLocationOfUpStairs();
                }else{
                        stairLoc = floorMap.getLocationOfDownStairs();
                }

                teleportToLocation(stairLoc.x, stairLoc.y, Direction.North);
                dungeon.moveTokenToFloor(this, floorMap);

                return true;
        }

        protected abstract void incremenetU(float delta);


        /**
         * DO NOT MODIFY
         * @return
         */
        public Pair getLocation() {
                return location;
        }

        public boolean isLocatedAt(Pair loc) {
                return location.equals(loc);
        }

        public boolean isLocatedAt(int x, int y) {
                return location.x == x && location.y == y;
        }

        public float getLocationFloatX() {
                return location.x;
        }

        public float getLocationFloatY() {
                return location.y;
        }

        public int getDistance(Pair targetLocation){
                return location.distance(targetLocation);
        }

        public int getId() {
                return id;
        }

        public Direction getDirection() {
                return direction;
        }

        public String getName() {
                return name;
        }


        /**
         * if the token prevents other tokens from sharing the same tile.
         *
         * @return
         */
        public boolean isBlocksPathing() {
                return blocksPathing;
        }

        public FloorMap getFloorMap(){
                return floorMap;
        }


        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Token token = (Token) o;

                if (id != token.id) return false;

                return true;
        }

        @Override
        public int hashCode() {
                return id;
        }

        @Override
        public String toString() {
                return "Token{" +
                        "id=" + id +
                        ", name='" + name + '\'' +
                        ", location=" + location + "(floor "+floorMap.index+ ")"+
                        ", direction=" + direction +
                        '}';
        }
}
