package asf.dungeon.model;

import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.item.ConsumableItem;
import asf.dungeon.model.token.PressurePlate;
import asf.dungeon.model.token.Stairs;
import asf.dungeon.model.token.Token;
import asf.dungeon.utility.UtDebugPrint;
import asf.dungeon.utility.UtMath;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;

import java.util.List;

import static asf.dungeon.utility.UtDebugPrint.out;

/**
 * A single floor of the dungeon. contains information about the tiles that make up the floor
 * and the tokens on this floor.
 */
public class FloorMap  implements UtDebugPrint.Debuggable{
        public final int index;
        public final FloorType floorType;
        public final Tile[][] tiles;
        public final Pathfinder pathfinder;
        private final MonsterSpawner monsterSpawner;
        protected SnapshotArray <Token> tokens = new SnapshotArray<Token>(true, 16, Token.class);

        public FloorMap(int index, Tile[][] tiles) {
                this(index, tiles, null);
        }
        public FloorMap(int index, Tile[][] tiles,MonsterSpawner monsterSpawner) {
                //UtFloorGen.printFloorTile(tiles);
                this.index = index;
                floorType = index == 0 ? FloorType.Grassy : FloorType.Dungeon;
                this.tiles = tiles;
                pathfinder = new Pathfinder(this);
                this.monsterSpawner = monsterSpawner;
        }

        protected void update(Dungeon dungeon, float delta){

                if(monsterSpawner != null) monsterSpawner.spawnMonsters(dungeon, this);

                Token[] tokensSnapshot = tokens.begin();
                for (int i = 0, n = tokens.size; i < n; i++) {
                        tokensSnapshot[i].updateComponents(delta);
                }
                tokens.end();
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

        public PressurePlate getPressurePlateAt(int x, int y){
                for (Token token : tokens) {
                        if(token.location.x == x && token.location.y == y){
                                PressurePlate plate = token.get(PressurePlate.class);
                                if(plate != null)
                                        return plate;
                        }
                }
                return null;
        }

        public Stairs getStairsAt(int x, int y){
                for (Token token : tokens) {
                        if(token.stairs != null && token.location.x == x && token.location.y == y)
                                return token.stairs;
                }
                return null;
        }

        public Stairs getStairsUp(){
                for (Token token : tokens) {
                        if(token.stairs != null && token.stairs.isStairsUp())
                                return token.stairs;
                }
                return null;
        }

        public Stairs getStairsDown(){
                for (Token token : tokens) {
                        if(token.stairs != null && !token.stairs.isStairsUp())
                                return token.stairs;
                }
                return null;
        }

        public boolean hasTokensAt(int x, int y){
                for(Token token : tokens){
                        if(token.location.x ==x && token.location.y == y){
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
                        if(token.logic != null && token.logic.getTeam() == team)
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
                        if(sector.contains(token.location))
                                tokensAt.add(token);
                }
                return tokensAt;
        }

        /**
         * returns list of tokens that are directly North, South, East, or West of this location.
         * The returned array should not be stored as it will be reused next time this method is called
         */
        public Array<Token> getManhattanNeighborTokens(Pair loc){
                tokensAt.clear();
                for (Token token : tokens) {
                        if(token.location.x == loc.x && (token.location.y == loc.y-1 || token.location.y== loc.y +1)  ){
                                tokensAt.add(token);
                        }else if(token.location.y == loc.y && (token.location.x == loc.x-1 || token.location.x == loc.x+1)){
                                tokensAt.add(token);
                        }
                }
                return tokensAt;
        }

        /**
         * returns list of tokens within provided extent of the location. (not manhattan distance)
         * The returned array should not be stored as it will be reused next time this method is called
         */
        public Array<Token> getTokensInExtent(Pair loc, int extent){
                tokensAt.clear();
                for (Token token : tokens) {
                        Pair tLoc = token.location;
                        if(tLoc.x >= loc.x - extent && tLoc.x <= loc.x+extent && tLoc.y >= loc.y-extent && tLoc.y <= loc.y+extent){
                                tokensAt.add(token);
                        }
                }
                return tokensAt;
        }

        public Array<Token> getTargetableTokens(Token token, ConsumableItem.TargetsTokens usingItem){
                tokensAt.clear();
                for (Token t : tokens) {
                        Pair tLoc = t.location;
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
                if(token.fogMapping == null){
                       throw new IllegalArgumentException("the token must have fog mapping enabled");
                }
                FogMap fogMap = token.fogMapping.getCurrentFogMap();

                tokensAt.clear();
                for (Token t : tokens) {
                        Pair tLoc = t.location;
                        if(fogMap.isVisible(tLoc.x, tLoc.y)){
                                tokensAt.add(t);
                        }
                }
                return tokensAt;
        }

        public Array<Token> getCrateAndLootTokens() {
                tokensAt.clear();
                for (Token t : tokens) {
                        if(t.crateInventory != null || t.loot != null){
                                tokensAt.add(t);
                        }
                }
                return tokensAt;
        }

        /**
         * DO NOT MODIFY
         * @return
         */
        public Array<Token> getTokens() {
                return tokens;
        }

        public Array<Token> getAttackableTokens(int notOnTeam) {
                tokensAt.clear();
                for (Token t : tokens) {
                        if(t.logic != null && t.logic.getTeam() != notOnTeam
                           && t.damage != null && t.damage.isAttackable()
                           )
                                tokensAt.add(t);
                }
                return tokensAt;
        }

        public boolean hasCharacterTokensAt(int x, int y){
                for (Token t : tokens)
                        if(t.logic != null && t.location.x == x && t.location.y == y) return true;
                return false;
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
         * this is here for the use the dungeon debug session window.
         * @return
         */
        public Array<Token> getTokensOnFloor(Array<Token> store){
                store.clear();
                for (int i = 0; i < tokens.size; i++) {
                        store.add(tokens.get(i));
                }
                return store;
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
                if (tile == null || tile.blockMovement)
                        return true;
                for (Token token : tokens) {
                        if (token.blocksPathing && token.isLocatedAt(x,y))
                                return true;
                }
                return false;
        }

        /**
         * determines if this tile blocks vision, the location of the mover (vantage point) must also be provided
         * because some tiles block vision if standing outside the tile, but once standing in the tile they do not
         *
         * NOTE: THIS DOES NOT CHECK FOR LOS OF SIGHT, USE LOS.hasLineOfSigh() to check for sight!, this only
         * checks for visibility of this tile assuming no obstacles.
         *
         * @param vantageX
         * @param vantageY
         * @param x
         * @param y
         * @return
         */
        public boolean isLocationVisionBlocked(int vantageX, int vantageY, int x, int y){
                if(x == vantageX && y == vantageY){
                        return false;
                }
                Tile tile = getTile(x, y);
                return tile == null || tile.blockVision;

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
                        if(goalTile != null && !goalTile.blockMovement) return store.set(goal.x + xSign, goal.y);
                        int ySign = UtMath.sign(yRange);
                        goalTile = getTile(goal.x, goal.y+ySign);
                        if(goalTile != null && !goalTile.blockMovement) return store.set(goal.x, goal.y+ySign);
                        goalTile = getTile(goal.x, goal.y-ySign);
                        if(goalTile != null && !goalTile.blockMovement) return store.set(goal.x, goal.y-ySign);
                        goalTile = getTile(goal.x-xSign, goal.y);
                        if(goalTile != null && !goalTile.blockMovement) return store.set(goal.x-xSign, goal.y);
                }else{
                        int ySign = UtMath.sign(yRange);
                        goalTile = getTile(goal.x, goal.y+ySign);
                        if(goalTile != null && !goalTile.blockMovement) return store.set(goal.x, goal.y+ySign);
                        int xSign = UtMath.sign(xRange);
                        goalTile = getTile(goal.x+xSign, goal.y);
                        if(goalTile != null && !goalTile.blockMovement) return store.set(goal.x+xSign, goal.y);
                        goalTile = getTile(goal.x-xSign, goal.y);
                        if(goalTile != null && !goalTile.blockMovement) return store.set(goal.x-xSign, goal.y);
                        goalTile = getTile(goal.x, goal.y-ySign);
                        if(goalTile != null && !goalTile.blockMovement) return store.set(goal.x, goal.y-ySign);
                }
                store.set(goal);
                return null;
        }

        public String toString(){
                return "FloorMap: "+index;
        }

        @Override
        public List<String> toDebugInfo() {
                List<String> out= UtDebugPrint.object(this);
                out.add("Map Display");
                out.addAll(toTileStrings());
                return out;
        }

        public List<String> toTileStrings(){
                List<String> out = out();
                for (int y = tiles[0].length - 1; y >= 0; y--) {
                        String s = "";
                        for (int x = 0; x < tiles.length; x++) {
                                Tile tile = tiles[x][y];
                                if (tile == null)
                                        s += " ";
                                else if(tile.isFloor()){
                                        boolean printedToken = false;
                                        for(int i=0; i < tokens.size; i++){
                                                Token token = tokens.items[i];
                                                if(token.location.x != x || token.location.y != y) continue;
                                                char c= token.toCharacter();
                                                //if(Character.isLetter(c) ){
                                                        s+= c;
                                                        printedToken = true;
                                                        break;
                                                //}

                                        }
                                        if(!printedToken) s += tile.toCharacter();
                                }
                                else
                                        s += tile.toCharacter();
                        }
                        out.add(s);
                }
                return out;
        }


        public interface MonsterSpawner{
                public void spawnMonsters(Dungeon dungeon, FloorMap floorMap);
        }

}
