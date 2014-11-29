package asf.dungeon.model;

import asf.dungeon.model.token.Token;
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

        /**
         * how the mover can move about the tile map
         */
        public PathingPolicy pathingPolicy = PathingPolicy.Manhattan;
        /**
         * the mover will prefer to maintain its momentum and move in a straight line if possible
         * instead of making zig zag shapes.
         *
         * this sometimes produces weird results with CanDiagonalIfNotCuttingCorner and CanCutCorners, really only mean too be used with Manhattan
         */
        public boolean avoidZigZagging = true;

        /**
         * if path exceeds this length then pathfinding will be forced to return false
         */
        public int maxPathSize = Integer.MAX_VALUE;


        private Pair end;
        private final int[][] gScore; // cost from start to current
        private final int[][] hScore; // cost form current to goal
        private final int[][] fScore;
        private final Pair[][] cameFrom;
        private final FloorMap floorMap;
        private final Tile[][] map;
        // temp arrays for pathfinding
        private static transient final Array<Pair> openNodes = new Array<Pair>(true, 32, Pair.class);
        private static transient final Array<Pair> closedNodes = new Array<Pair>(true, 32, Pair.class);
        private static transient final Array<Pair> found = new Array<Pair>(12); // neighbors that have been found


        public Pathfinder(FloorMap floorMap) {
                this.floorMap = floorMap;
                this.map = this.floorMap.tiles;
                gScore = new int[map.length][map[0].length];
                fScore = new int[map.length][map[0].length];
                hScore = new int[map.length][map[0].length];
                cameFrom = new Pair[map.length][map[0].length];
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

        public boolean generate(int startX, int startY, int endX, int endY, Array<Pair> storePath) {
                return generate(toPair(startX, startY), toPair(endX, endY), storePath);
        }

        public boolean generate(Pair start, Pair finish, Array<Pair> storePath) {
                //System.out.println(String.format("last open size: %s, last closed size: %s", openNodes.size, closedNodes.size));
                openNodes.clear();
                closedNodes.clear();
                end = finish;
                clearVars(gScore);
                clearVars(fScore);
                clearVars(hScore);
                clearVars(cameFrom);
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
                if(x == end.x && y == end.y) // target location is always walkable, this allows walking in to locked doors to unlock them
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
                if(closedNodes.size < 5){
                        // only check tokens if closedNodes < 5. far away tokens are likely to be changed by the time you get there so its not so important to check for them
                        Array<Token> tokensAt = floorMap.getTokensAt(n1);
                        for (Token t : tokensAt) {
                                if(t.getDamage() != null && t.getDamage().isDead())
                                        continue;
                                if(t.isBlocksPathing()){
                                        if(t.getMove() == null)
                                                movementCost += 30; // crate, should avoid
                                        else
                                                movementCost+=20; // monster, not as important to avoid
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
