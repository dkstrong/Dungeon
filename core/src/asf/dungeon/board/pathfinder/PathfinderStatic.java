package asf.dungeon.board.pathfinder;

/*
Self-explanatory. Comes with 2 methods you can use, one for integer positions and another for nodes as positions. paths return are lists of nodes, but using my class should be very simple :)
*/


import asf.dungeon.board.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * http://www.raywenderlich.com/4946/introduction-to-a-pathfinding
 * https://github.com/xSmallDeadGuyx/SimpleAStar
 */
public class PathfinderStatic {
        public static enum PathingPolicy{
                /**
                 * can only horizontal and vertical
                 */
                Manhattan,
                /**
                 * typiaclly only moves horizontal and vertical, but can do diagonal when not cutting a corner
                 */
                ManHattanOptimized,
                /**
                 * can move horizontal, vertical, and diagonal
                 */
                CanCutCorners;
        }
        public static PathingPolicy pathingPolicy = PathingPolicy.Manhattan;
        /**
         * the mover will prefer to maintain its momentum and move in a straight line if possible
         * instead of making zig zag shapes.
         */
        public static boolean avoidZigZagging = true;

        private static Pair end;
        private static int[][] gScore; // cost from start to current
        private static int[][] hScore; // cost form current to goal
        private static int[][] fScore;
        private static Pair[][] cameFrom;
        private static Tile[][] map;

        public static Pair toPair(int i, int j) {
                return new Pair(i, j);
        }

        public static List<Pair> generate(int startX, int startY, int endX, int endY, Tile[][] mapWalls) {
                return generate(toPair(startX, startY), toPair(endX, endY), mapWalls);
        }

        /**
         * @param start
         * @param finish
         * @param tileMap
         * @return list of pair if a path is found, returns null if no path was found.
         */
        public static List<Pair> generate(Pair start, Pair finish, Tile[][] tileMap) {
                List<Pair> openNodes = new ArrayList<Pair>();
                List<Pair> closedNodes = new ArrayList<Pair>();
                map = tileMap;
                end = finish;
                gScore = new int[map.length][map[0].length];
                fScore = new int[map.length][map[0].length];
                hScore = new int[map.length][map[0].length];
                cameFrom = new Pair[map.length][map[0].length];
                openNodes.add(start);
                gScore[start.x][start.y] = 0;
                hScore[start.x][start.y] = calculateHeuristic(start);
                fScore[start.x][start.y] = hScore[start.x][start.y];

                while(openNodes.size() > 0) {
                        Pair current = getLowestNodeIn(openNodes);
                        if(current == null)
                                break;
                        if(current.equals(end))
                                return reconstructPath(current);
                        //System.out.println(current.x + ", " + current.y);

                        openNodes.remove(current);
                        closedNodes.add(current);

                        List<Pair> neighbors = getNeighborNodes(current);
                        for(Pair n : neighbors) {

                                if(closedNodes.contains(n)) {
                                        continue;
                                }


                                int tempGscore = gScore[current.x][current.y] + distanceBetween(n, current);
                                // TODO: to add movement cost i think i just do tempGScore+=map[n.x][n.y].getMovementCost()
                                boolean proceed = false;
                                if(!openNodes.contains(n)) {
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
                return null;
        }

        private static List<Pair> reconstructPath(Pair n) {
                if(cameFrom[n.x][n.y] != null) {
                        List<Pair> path = reconstructPath(cameFrom[n.x][n.y]);
                        path.add(n);
                        return path;
                }
                else {
                        List<Pair> path = new ArrayList<Pair>(); // instead of making a new list, i could just clear existing one
                        path.add(n);
                        return path;
                }
        }

        private static List<Pair> getNeighborNodes(Pair n) {
                List<Pair> found = new ArrayList<Pair>();
                if(!map[n.x + 1][n.y].isBlockMovement()) found.add(toPair(n.x + 1, n.y));
                if(!map[n.x - 1][n.y].isBlockMovement()) found.add(toPair(n.x - 1, n.y));
                if(!map[n.x][n.y + 1].isBlockMovement()) found.add(toPair(n.x, n.y + 1));
                if(!map[n.x][n.y - 1].isBlockMovement()) found.add(toPair(n.x, n.y - 1));
                if(pathingPolicy == PathingPolicy.CanCutCorners) {
                        if(!map[n.x + 1][n.y + 1].isBlockMovement() && (!map[n.x + 1][n.y].isBlockMovement() || !map[n.x][n.y + 1].isBlockMovement())) found.add(toPair(n.x + 1, n.y + 1));
                        if(!map[n.x - 1][n.y + 1].isBlockMovement() && (!map[n.x - 1][n.y].isBlockMovement() || !map[n.x][n.y + 1].isBlockMovement())) found.add(toPair(n.x - 1, n.y + 1));
                        if(!map[n.x - 1][n.y - 1].isBlockMovement() && (!map[n.x - 1][n.y].isBlockMovement() || !map[n.x][n.y - 1].isBlockMovement())) found.add(toPair(n.x - 1, n.y - 1));
                        if(!map[n.x + 1][n.y - 1].isBlockMovement() && (!map[n.x + 1][n.y].isBlockMovement() || !map[n.x][n.y - 1].isBlockMovement())) found.add(toPair(n.x + 1, n.y - 1));
                }else if(pathingPolicy == PathingPolicy.ManHattanOptimized){
                        if(!map[n.x + 1][n.y + 1].isBlockMovement() && (!map[n.x + 1][n.y].isBlockMovement() && !map[n.x][n.y + 1].isBlockMovement())) found.add(toPair(n.x + 1, n.y + 1));
                        if(!map[n.x - 1][n.y + 1].isBlockMovement() && (!map[n.x - 1][n.y].isBlockMovement() && !map[n.x][n.y + 1].isBlockMovement())) found.add(toPair(n.x - 1, n.y + 1));
                        if(!map[n.x - 1][n.y - 1].isBlockMovement() && (!map[n.x - 1][n.y].isBlockMovement() && !map[n.x][n.y - 1].isBlockMovement())) found.add(toPair(n.x - 1, n.y - 1));
                        if(!map[n.x + 1][n.y - 1].isBlockMovement() && (!map[n.x + 1][n.y].isBlockMovement() && !map[n.x][n.y - 1].isBlockMovement())) found.add(toPair(n.x + 1, n.y - 1));
                }
                return found;
        }

        private static Pair getLowestNodeIn(List<Pair> nodes) {
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

        private static int distanceBetween(Pair n1, Pair n2) {
                return (int) Math.round(10 * Math.sqrt(Math.pow(n1.x - n2.x, 2) + Math.pow(n1.y - n2.y, 2)));
        }

        private static boolean willZigZag(Pair n){
                // TODO: i could also look at how long the mover has been traveling
                // in this direction to build up momemntum and add that to the heuristic score
                // this would further encourage moving in straight lines.. however for now this will do.
                Pair from1 =cameFrom[n.x][n.y];
                if(from1 == null)
                        return false;
                Pair from2 =cameFrom[from1.x][from1.y];
                if(from2 == null)
                        return false;
                int lastdx = from1.x - from2.x;
                int lastdy = from1.y - from2.y;
                int dx = n.x - from1.x;
                int dy = n.y - from1.y;

                return lastdx != dx || lastdy != dy;
        }

        private static int calculateHeuristic(Pair start) {
                int h = 10 * (Math.abs(start.x - end.x) + Math.abs(start.y - end.y));

                if(avoidZigZagging && willZigZag(start)){
                        h *=2;
                }

                return h;
        }
}
