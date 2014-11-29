package asf.dungeon.model;

import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.item.ConsumableItem;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.UtMath;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;

/**
 * A single floor of the dungeon. contains information about the tiles that make up the floor
 * and the tokens on this floor.
 */
public class FloorMap  {
        public final int index;
        public final Tile[][] tiles;
        private final Pathfinder pathfinder;
        private final MonsterSpawner monsterSpawner;
        protected SnapshotArray <Token> tokens = new SnapshotArray<Token>(true, 16, Token.class);

        public FloorMap(int index, Tile[][] tiles) {
                this(index, tiles, null);
        }
        public FloorMap(int index, Tile[][] tiles,MonsterSpawner monsterSpawner) {
                //UtFloorGen.printFloorTile(tiles);
                this.index = index;
                this.tiles = tiles;
                pathfinder = new Pathfinder(this);
                pathfinder.pathingPolicy = Pathfinder.PathingPolicy.Manhattan;
                pathfinder.avoidZigZagging = false;
                this.monsterSpawner = monsterSpawner;
        }

        protected void update(Dungeon dungeon, float delta){

                if(monsterSpawner != null) monsterSpawner.spawnMonsters(dungeon, this);

                Token[] tokensSnapshot = tokens.begin();
                for (int i = 0, n = tokens.size; i < n; i++) {
                        tokensSnapshot[i].incremenetU(delta);
                }
                tokens.end();
        }


        public boolean computePath(Pair start, Pair goal, Array<Pair> store) {
                return pathfinder.generate(start, goal, store);
        }

        /**
         * DO NOT MODIFY
         * @return
         */
        public Tile[][] getTiles() {
                return tiles;
        }

        public int getWidth() {
                return tiles.length;
        }

        public int getHeight() {
                return tiles[0].length;
        }


        public Tile getTile(Pair loc){
                if(loc.x >= getWidth() || loc.x <0 || loc.y >= getHeight() || loc.y<0){
                        return null;
                }
                return tiles[loc.x][loc.y];
        }
        public Tile getTile(int x, int y){
                if(x >= getWidth() || x <0 || y >= getHeight() || y<0){
                        return null;
                }
                return tiles[x][y];
        }


        public Pair getLocationOfUpStairs(){

                for(int x=0; x< getWidth(); x++){
                        for(int y=0; y<getHeight(); y++){
                                if(tiles[x][y]!=null && tiles[x][y].isStairs()){
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
                                if(tiles[x][y]!=null && tiles[x][y].isStairs()){
                                        if(tiles[x][y].getStairsTo() > index){
                                                return new Pair(x,y);
                                        }
                                }
                        }
                }

                return null;
        }


        public boolean hasTokensAt(int x, int y){
                for(Token token : tokens){
                        if(token.getLocation().x ==x && token.getLocation().y == y){
                                return true;
                        }
                }
                return false;
        }

        private final Array<Token> tokensAt = new Array<Token>(8);

        /**
         * all tokens on this floor, the returned array should not be stored as it will be reused next time this method is called
         * @return
         */
        public Array<Token> getTokensOnTeam(int team) {
                tokensAt.clear();
                for (Token token : tokens) {
                        if(token.getLogic() != null && token.getLogic().getTeam() == team)
                                tokensAt.add(token);
                }
                return tokensAt;
        }

        /**
         * all tokes contained within this sector.
         * THe returned array should not be stored as it will be reused next time this method is called
         * @return
         */
        public Array<Token> getTokensAt(Sector sector) {
                tokensAt.clear();
                for (Token token : tokens) {
                        if(sector.contains(token.getLocation()))
                                tokensAt.add(token);
                }
                return tokensAt;
        }

        /**
         * returns list of tokens within provided extent of the location. (not manhatten distance)
         * The returned array should not be stored as it will be reused next time this method is called
         */
        public Array<Token> getTokensInExtent(Pair loc, int extent){
                tokensAt.clear();
                for (Token token : tokens) {
                        Pair tLoc = token.getLocation();
                        if(tLoc.x >= loc.x - extent && tLoc.x <= loc.x+extent && tLoc.y >= loc.y-extent && tLoc.y <= loc.y+extent){
                                tokensAt.add(token);
                        }
                }
                return tokensAt;
        }

        public Array<Token> getTargetableTokens(Token token, ConsumableItem.TargetsTokens usingItem){
                tokensAt.clear();
                for (Token t : tokens) {
                        Pair tLoc = t.getLocation();
                        if(usingItem.canConsume(token, t)){
                                tokensAt.add(t);
                        }
                }
                return tokensAt;
        }

        /**
         * returns list of tokens that are visible according to the fogmap
         * if the supplied source token does not have a fogmap, this is the same as doing getTokensInExtent()
         * The returned array should not be stored as it will be reused next time this method is called
         */
        public Array<Token> getVisibleTokens(Token token){
                if(token.getFogMapping() == null){
                        return getTokensInExtent(token.getLocation(), token.getDamage() == null ? 3 : token.getDamage().getSightRadius());
                }
                FogMap fogMap = token.getFogMapping().getCurrentFogMap();

                tokensAt.clear();
                for (Token t : tokens) {
                        Pair tLoc = t.getLocation();
                        if(fogMap.isVisible(tLoc.x, tLoc.y)){
                                tokensAt.add(t);
                        }
                }
                return tokensAt;
        }

        public Array<Token> getCrateTokens() {
                tokensAt.clear();
                for (Token t : tokens) {
                        if(t.getSimpleInventory() != null){
                                tokensAt.add(t);
                        }
                }
                return tokensAt;
        }

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
         * list of tokens at the supplied location after moving once in the supplied direction
         * note that the Array that is returned shouldnt be stored as it will be reused next time this method is called
         *
         * @param loc
         * @return
         */
        public Array<Token> getTokensAt(Pair loc, Direction lookDir) {
                int x = loc.x;
                int y = loc.y;
                if (lookDir == Direction.North) {
                        y = loc.y + 1;
                } else if (lookDir == Direction.South) {
                        y = loc.y - 1;
                } else if (lookDir == Direction.East) {
                        x = loc.x + 1;
                } else if (lookDir == Direction.West) {
                        x = loc.x - 1;
                } else if(lookDir == Direction.NorthEast){
                        y = loc.y+1;
                        x = loc.x+1;
                } else if(lookDir == Direction.NorthWest){
                        y = loc.y+1;
                        x = loc.x-1;
                } else if(lookDir == Direction.SouthEast){
                        y = loc.y-1;
                        x = loc.x+1;
                } else if(lookDir == Direction.SouthWest){
                        y = loc.y-1;
                        x = loc.x-1;
                } else{
                        throw new AssertionError(lookDir);
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
                return isLocationBlocked(loc.x, loc.y);
        }

        public boolean isLocationBlocked(int x, int y) {
                Tile tile = getTile(x,y);
                if (tile == null || tile.isBlockMovement())
                        return true;
                for (Token token : tokens) {
                        if (token.isBlocksPathing() && token.isLocatedAt(x,y))
                                return true;
                }
                return false;
        }

        /**
         * determines if this tile blocks vision, the location of the mover (vantage point) must also be provided
         * because some tiles block vision if standing outside the tile, but once standing in the tile they do not
         * @param vantageX
         * @param vantageY
         * @param x
         * @param y
         * @return
         */
        public boolean isLocationVisionBlocked(int vantageX, int vantageY, int x, int y){
                Tile tile = getTile(x, y);
                if(tile != null && tile.isStairs()){
                        if(x == vantageX && y == vantageY){
                                return false;
                        }
                }
                return tile == null || tile.isBlockVision();

        }

        /**
         * check each adjacent tile and pick the closest tile to the start location that is not blocking movement.
         * if there are no legal adjacent tiles then the store will be set to the value of goal
         * return store if found a legal neighbor, null if no legal neighbor found
         */
        public Pair getNextClosestLegalLocation(Pair start, Pair goal, Pair store){
                Tile goalTile;
                int xRange = start.x - goal.x;
                int yRange = start.y - goal.y;
                if(Math.abs(xRange) > Math.abs(yRange)){
                        int xSign = UtMath.sign(xRange);
                        goalTile = getTile(goal.x+xSign, goal.y);
                        if(goalTile != null && !goalTile.isBlockMovement()) return store.set(goal.x + xSign, goal.y);
                        int ySign = UtMath.sign(yRange);
                        goalTile = getTile(goal.x, goal.y+ySign);
                        if(goalTile != null && !goalTile.isBlockMovement()) return store.set(goal.x, goal.y+ySign);
                        goalTile = getTile(goal.x, goal.y-ySign);
                        if(goalTile != null && !goalTile.isBlockMovement()) return store.set(goal.x, goal.y-ySign);
                        goalTile = getTile(goal.x-xSign, goal.y);
                        if(goalTile != null && !goalTile.isBlockMovement()) return store.set(goal.x-xSign, goal.y);
                }else{
                        int ySign = UtMath.sign(yRange);
                        goalTile = getTile(goal.x, goal.y+ySign);
                        if(goalTile != null && !goalTile.isBlockMovement()) return store.set(goal.x, goal.y+ySign);
                        int xSign = UtMath.sign(xRange);
                        goalTile = getTile(goal.x+xSign, goal.y);
                        if(goalTile != null && !goalTile.isBlockMovement()) return store.set(goal.x+xSign, goal.y);
                        goalTile = getTile(goal.x-xSign, goal.y);
                        if(goalTile != null && !goalTile.isBlockMovement()) return store.set(goal.x-xSign, goal.y);
                        goalTile = getTile(goal.x, goal.y-ySign);
                        if(goalTile != null && !goalTile.isBlockMovement()) return store.set(goal.x, goal.y-ySign);
                }
                store.set(goal);
                return null;
        }




        public interface MonsterSpawner{
                public void spawnMonsters(Dungeon dungeon, FloorMap floorMap);
        }

}
