package asf.dungeon.board;

import com.badlogic.gdx.utils.Array;
import asf.dungeon.board.pathfinder.Pathfinder;
import asf.dungeon.board.pathfinder.Tile;

import java.util.List;

/**
 * A single floor of the dungeon. contains information about the tiles that make up the floor
 * and the tokens on this floor.
 */
public class FloorMap {
        public final int index;
        public final FloorTile[][] tiles;
        private final Pathfinder pathfinder;
        protected Array<Token> tokens = new Array<Token>(true, 16, Token.class);

        public FloorMap(int index, FloorTile[][] tiles) {
                this.index = index;
                this.tiles = tiles;
                pathfinder = new Pathfinder(tiles);
                pathfinder.pathingPolicy = Pathfinder.PathingPolicy.Manhattan;
                pathfinder.avoidZigZagging = false;
                //pathfinder.dynamicMovementCostProvider = this;
        }

        protected void update(float delta){
                for (int i = 0; i < tokens.size; i++) {
                        tokens.items[i].incremenetU(delta);
                }
        }


        public Array<Pair> computePath(Pair start, Pair goal) {
                List<Pair> path = pathfinder.generate(start, goal);

                if(path == null){
                        return null;
                }

                // TODO: instead of creating a new array, pass in a store argument
                Array<Pair> pathArray = new Array<Pair>();
                for (Pair pair : path) {
                        pathArray.add(pair);
                }

                return pathArray;
        }

        public int getWidth() {
                return tiles.length;
        }

        public int getHeight() {
                return tiles[0].length;
        }


        public FloorTile getTile(Pair loc){
                if(loc.x >= getWidth() || loc.x <0 || loc.y >= getHeight() || loc.y<0){
                        return null;
                }
                return tiles[loc.x][loc.y];
        }
        public FloorTile getTile(int x, int y){
                if(x >= getWidth() || x <0 || y >= getHeight() || y<0){
                        return null;
                }
                return tiles[x][y];
        }

        public FloorTile getTile(int x, int y, Direction dir){
                if(dir == Direction.North){
                        y++;
                }else if(dir == Direction.South){
                        y--;
                }else if(dir==Direction.East){
                        x++;
                }else if(dir ==Direction.West){
                        x--;
                }
                if(x >= getWidth() || x <0 || y >= getHeight() || y<0){
                        return null;
                }
                return tiles[x][y];
        }


        public Pair getLocationOfUpStairs(){

                for(int x=0; x< getWidth(); x++){
                        for(int y=0; y<getHeight(); y++){
                                if(tiles[x][y].isStairs()){
                                        if(tiles[x][y].getStairsTo() < index){
                                                return new Pair(x,y);
                                        }
                                }
                        }
                }

                return null;
        }

        public Pair getLocationOfDownStairs(){

                for(int x=0; x< getWidth(); x++){
                        for(int y=0; y<getHeight(); y++){
                                if(tiles[x][y].isStairs()){
                                        if(tiles[x][y].getStairsTo() > index){
                                                return new Pair(x,y);
                                        }
                                }
                        }
                }

                return null;
        }


        private Array<Token> tokensAt = new Array<Token>(8);

        /**
         * list of tokens at the supplied location, note that the Array that is returned
         * shouldnt be stored as it will be reused next time this method is called
         *
         * @param loc
         * @return
         */
        public Array<Token> getTokensAt(Pair loc) {
                tokensAt.clear();
                for (Token token : tokens) {
                        if (token.isLocatedAt(loc)) {
                                tokensAt.add(token);
                        }
                }
                return tokensAt;
        }

        /**
         * list of tokens at the supplied location of the supplied class.
         * note that the array that is returned shoudlnt be stored as it iwl lbe reused next time this method is called
         *
         * @param loc
         * @param tokenClass
         * @return
         */
        public Array<Token> getTokensAt(Pair loc, Class<? extends Token> tokenClass) {
                tokensAt.clear();
                for (Token token : tokens) {
                        if (token.isLocatedAt(loc) && tokenClass.isAssignableFrom(token.getClass())) {
                                tokensAt.add(token);
                        }
                }
                return tokensAt;
        }

        /**
         * list of tokens at the supplied location after moving once in the supplied direction
         * note that the Array that is returned shouldnt be stored as it will be reused next time this method is called
         *
         * @param loc
         * @return
         */
        public Array<Token> getTokensAt(Pair loc, Direction dir) {
                int x = loc.x;
                int y = loc.y;
                if (dir == Direction.North) {
                        y = loc.y + 1;
                } else if (dir == Direction.South) {
                        y = loc.y - 1;
                } else if (dir == Direction.East) {
                        x = loc.x + 1;
                } else if (dir == Direction.West) {
                        x = loc.x - 1;
                } else {
                        throw new AssertionError(dir);
                }

                tokensAt.clear();
                for (Token token : tokens) {
                        if (token.isLocatedAt(x, y)) {
                                tokensAt.add(token);
                        }
                }
                return tokensAt;
        }
        /**
         * returns true if this location is blocking tile (eg a wall) or if there is a blocking token on it (eg a character)
         *
         * @param loc
         * @return
         */
        public boolean isLocationBlocked(Pair loc) {
                Tile tile = getTile(loc);
                if (tile == null || tile.isBlockMovement())
                        return true;
                for (Token token : tokens) {
                        if (token.isBlocksPathing() && token.isLocatedAt(loc))
                                return true;
                }
                return false;
        }

        /**
         * takes the provided location and moves 1 tile in the direction of dir, then stores that location in store
         * if the resulting location is outside of the map's boundaries then the values (-1,-1) will be stored.
         *
         * @param loc
         * @param dir
         * @param store the resulting location
         */
        public void getLocationInDirection(Pair loc, Direction dir, Pair store) {
                switch (dir) {
                        case North:
                                store.y = loc.y + 1;
                                if (store.y < getHeight()) {
                                        store.x = loc.x;
                                } else {
                                        store.y = -1;
                                        store.x = -1;
                                }
                                break;
                        case South:
                                store.y = loc.y - 1;
                                if (store.y >= 0) {
                                        store.x = loc.x;
                                } else {
                                        store.y = -1;
                                        store.x = -1;
                                }
                                break;
                        case East:
                                store.x = loc.x + 1;
                                if (store.x < getWidth()) {
                                        store.y = loc.y;
                                } else {
                                        store.x = -1;
                                        store.y = -1;
                                }
                                break;
                        case West:
                                store.x = loc.x - 1;
                                if (store.x >= 0) {
                                        store.y = loc.y;
                                } else {
                                        store.x = -1;
                                        store.y = -1;
                                }
                                break;
                        default:
                                throw new AssertionError(dir);
                }
        }


}
