package asf.dungeon.model;

import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.quest.Quest;
import com.badlogic.gdx.utils.Array;

/**
 * http://www.raywenderlich.com/4946/introduction-to-a-pathfinding
 * https://github.com/xSmallDeadGuyx/SimpleAStar
 *
 * a star pathfinder for tile based maps. uses static variables to minimize memory footprint.
 * this means the Pathfinder class can not be used twice at once unless the static variables are removed
 */
public class Pathfinder {
        public static enum PathingPolicy{
                /**
                 * can only horizontal and vertical
                 */
                Manhattan,
                /**
                 * can move horizontal and vertical. Will move diagonal if not cutting a corner
                 */
                CanDiagonalIfNotCuttingCorner,
                /**
                 * can move horizontal, vertical, and diagonal
                 */
                CanCutCorners;
        }

        private final FloorMap floorMap;
        // transient variables that can only be used on this pathfinder
        private transient Tile[][] map;
        private transient int[][] gScore; // cost from start to current
        private transient int[][] hScore; // cost form current to goal
        private transient int[][] fScore;
        private transient Pair[][] cameFrom;
        // temp vars for pathfinding, shared across pathfinders
        private static transient final Array<Pair> openNodes = new Array<Pair>(true, 32, Pair.class);
        private static transient final Array<Pair> closedNodes = new Array<Pair>(true, 32, Pair.class);
        private static transient final Array<Pair> found = new Array<Pair>(12); // neighbors that have been found
        private static transient Token mover;
        private static transient Pair end;
        private static transient PathingPolicy pathingPolicy;
        private static transient boolean avoidZigZagging ; // this sometimes produces weird results with CanDiagonalIfNotCuttingCorner and CanCutCorners, really only mean too be used with Manhattan
        private static transient int maxPathSize;


        public Pathfinder(FloorMap floorMap) {
                this.floorMap = floorMap;

        }

        public static Pair toPair(int i, int j) {
                return new Pair(i, j);
        }

        private static void clearVars(int[][] vars){
                for (int x = 0; x < vars.length; x++) {
                        for (int y = 0; y < vars[x].length; y++) {
                                vars[x][y] = 0;
                        }
                }
        }

        private static void clearVars(Pair[][] vars){
                for (int x = 0; x < vars.length; x++) {
                        for (int y = 0; y < vars[x].length; y++) {
                                vars[x][y] = null;
                        }
                }
        }

        public boolean generate(Token mover, Pair start, Pair finish, Array<Pair> storePath) {
                return generate(mover, start, finish, storePath, PathingPolicy.Manhattan, false, mover.getInteractor() == null ? Integer.MAX_VALUE : 20);
        }

        /**
         *
         * @param mover the token that is moving, used for calculating traversal heuristics and avoiding certain locations
         * @param start start location of the path
         * @param finish end location of the path
         * @param storePath the array to store the generated path
         * @param pathingPolicy how the mover is allowed to move
         * @param avoidZigZagging if the mover should build momentum and avoid changing direction
         * @param maxPathSize the maximum path size, will fail if exceeds this size
         * @return true if found a path, false otherwise, storePath is only modified if true is returned.
         */
        public boolean generate(Token mover, Pair start, Pair finish, Array<Pair> storePath, PathingPolicy pathingPolicy, boolean avoidZigZagging, int maxPathSize) {
                //System.out.println(String.format("last open size: %s, last closed size: %s", openNodes.size, closedNodes.size));

                Pathfinder.mover = mover;
                Pathfinder.pathingPolicy = pathingPolicy;
                Pathfinder.avoidZigZagging = avoidZigZagging;
                Pathfinder.maxPathSize = maxPathSize;
                openNodes.clear();
                closedNodes.clear();
                end = finish;
                if(map == null){
                        map = this.floorMap.tiles;
                        gScore = new int[map.length][map[0].length];
                        fScore = new int[map.length][map[0].length];
                        hScore = new int[map.length][map[0].length];
                        cameFrom = new Pair[map.length][map[0].length];
                }else{
                        clearVars(gScore);
                        clearVars(fScore);
                        clearVars(hScore);
                        clearVars(cameFrom);
                }
                openNodes.add(start);
                gScore[start.x][start.y] = 0;
                hScore[start.x][start.y] = calculateHeuristic(start);
                fScore[start.x][start.y] = hScore[start.x][start.y];

                while(openNodes.size > 0) {
                        Pair current = getLowestNodeIn(openNodes);
                        if(current == null)
                                break;
                        if(current.equals(end)){
                                reconstructPath(current, storePath);
                                return true;
                        }

                        openNodes.removeValue(current, true);
                        closedNodes.add(current);

                        if(closedNodes.size >= maxPathSize)
                                break;

                        Array<Pair> neighbors = getNeighborNodes(current);
                        for(Pair n : neighbors) {
                                if(closedNodes.contains(n, false)) {
                                        continue;
                                }

                                int tempGscore = gScore[current.x][current.y] + distanceBetween(n, current);

                                boolean proceed = false;
                                if(!openNodes.contains(n, false)) {
                                        openNodes.add(n);
                                        proceed = true;
                                }
                                else if(tempGscore < gScore[n.x][n.y]){
                                        proceed = true;
                                }


                                if(proceed) {
                                        cameFrom[n.x][n.y] = current;
                                        gScore[n.x][n.y] = tempGscore;
                                        hScore[n.x][n.y] = calculateHeuristic(n);
                                        fScore[n.x][n.y] = gScore[n.x][n.y] + hScore[n.x][n.y];
                                }
                        }
                }
                return false;
        }

        private Array<Pair> reconstructPath(Pair n, Array<Pair> storePath) {
                if(cameFrom[n.x][n.y] != null) {
                        reconstructPath(cameFrom[n.x][n.y], storePath);
                        storePath.add(n);
                        return storePath;
                } else {
                        storePath.clear();
                        storePath.add(n);
                        return storePath;
                }
        }

        private boolean isWalkable(int x, int y){
                // target location is always walkable, this allows walking in to locked doors to unlock them
                if(x == end.x && y == end.y)
                        return true;
                if(x <0 || x >= map.length || y<0 || y >= map[0].length){
                        return false;
                }

                Tile tile = map[x][y];
                return tile!= null && !tile.isBlockMovement();
        }

        private Array<Pair> getNeighborNodes(Pair n) {
                found.clear();
                if(isWalkable(n.x + 1,n.y)) found.add(toPair(n.x + 1, n.y));
                if(isWalkable(n.x - 1,n.y)) found.add(toPair(n.x - 1, n.y));
                if(isWalkable(n.x,n.y+1)) found.add(toPair(n.x, n.y + 1));
                if(isWalkable(n.x,n.y-1)) found.add(toPair(n.x, n.y - 1));
                if(pathingPolicy == PathingPolicy.CanCutCorners) {
                        if(isWalkable(n.x + 1,n.y + 1) && (isWalkable(n.x + 1,n.y) || isWalkable(n.x,n.y+1))) found.add(toPair(n.x + 1, n.y + 1));
                        if(isWalkable(n.x - 1,n.y + 1) && (isWalkable(n.x - 1,n.y) || isWalkable(n.x,n.y+1))) found.add(toPair(n.x - 1, n.y + 1));
                        if(isWalkable(n.x - 1,n.y - 1) && (isWalkable(n.x - 1,n.y) || isWalkable(n.x,n.y-1))) found.add(toPair(n.x - 1, n.y - 1));
                        if(isWalkable(n.x + 1,n.y - 1) && (isWalkable(n.x + 1,n.y) || isWalkable(n.x,n.y-1))) found.add(toPair(n.x + 1, n.y - 1));
                }else if(pathingPolicy == PathingPolicy.CanDiagonalIfNotCuttingCorner){
                        if(isWalkable(n.x + 1,n.y + 1) && (isWalkable(n.x + 1,n.y) && isWalkable(n.x,n.y+1))) found.add(toPair(n.x + 1, n.y + 1));
                        if(isWalkable(n.x - 1,n.y + 1) && (isWalkable(n.x - 1,n.y) && isWalkable(n.x,n.y+1))) found.add(toPair(n.x - 1, n.y + 1));
                        if(isWalkable(n.x - 1,n.y - 1) && (isWalkable(n.x - 1,n.y) && isWalkable(n.x,n.y-1))) found.add(toPair(n.x - 1, n.y - 1));
                        if(isWalkable(n.x + 1,n.y - 1) && (isWalkable(n.x + 1,n.y) && isWalkable(n.x,n.y-1))) found.add(toPair(n.x + 1, n.y - 1));
                }
                return found;
        }

        private Pair getLowestNodeIn(Array<Pair> nodes) {
                int lowest = -1;
                Pair found = null;
                for(Pair n : nodes) {
                        int dist = cameFrom[n.x][n.y] == null ? -1 : gScore[cameFrom[n.x][n.y].x][cameFrom[n.x][n.y].y] + distanceBetween(n, cameFrom[n.x][n.y]) + calculateHeuristic(n);
                        if(dist <= lowest || lowest == -1) {
                                lowest = dist;
                                found = n;
                        }
                }
                return found;
        }

        private int distanceBetween(Pair n1, Pair n2) {
                // n1 = to
                // n2 = from
                int distance =(int) Math.round(10 * Math.sqrt(Math.pow(n1.x - n2.x, 2) + Math.pow(n1.y - n2.y, 2)));


                int movementCost = map[n1.x][n1.y].getMovementCost();
                if(!n1.equals(end) && (closedNodes.size < 5 || mover.getInteractor() == null) ){
                        // NPCs - we check all their nodes because they dont frequently repath
                        // players - only check the first 5 nodes because they frequently repath

                        // if the node is the goal node, dont add extra movement code because its the goal
                        Array<Token> tokensAt = floorMap.getTokensAt(n1);
                        for (Token t : tokensAt) {
                                if(t.getDamage() != null && t.getDamage().isAttackable())
                                        continue;
                                if(t.get(Quest.class) != null) // avoiding walking into quest tokens unless they are they target
                                        movementCost += 30;
                                if(t.isBlocksPathing()){
                                        if(t.getMove() == null) // avoid walking in to crates, unless they are the target
                                                movementCost += 30;
                                        else
                                                movementCost+=20; // avoid walking into monsters, unless they are the target
                                }
                        }
                }
                return distance + movementCost;
        }


        private int calcMomentumFactor(Pair n){
                // TODO: i could also look at how long the mover has been traveling
                // in this direction to build up momemntum and add that to the heuristic score
                // this would further encourage moving in straight lines.. however for now this will do.
                Pair from1 =cameFrom[n.x][n.y];
                if(from1 == null)
                        return 1;
                Pair from2 =cameFrom[from1.x][from1.y];
                if(from2 == null)
                        return 1;  // TODO: instead of returning 1, i could look at the initial direction of the Mover right now to see if the direction is changed (could get messy though)
                int lastdx = from1.x - from2.x;
                int lastdy = from1.y - from2.y;
                int dx = n.x - from1.x;
                int dy = n.y - from1.y;

                if(lastdx != dx || lastdy != dy){
                        return 4; // moving to this node would change momentum, so return a higher than 1 factor to add this cost in to the heuristic
                }

                return 1;
        }

        private int calculateHeuristic(Pair start) {
                int h = 10 * (Math.abs(start.x - end.x) + Math.abs(start.y - end.y));

                if(avoidZigZagging)
                        h *=calcMomentumFactor(start);

                return h;
        }

}
