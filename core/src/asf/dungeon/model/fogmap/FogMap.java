package asf.dungeon.model.fogmap;

import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Tile;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.utils.Array;

/**
 * adapted from SensorArea in Monkey Trap by Paul Speed
 */
public class FogMap {
        private FloorMap floorMap;
        private Token token;
        private FogState[][] fog;
        //
        private int xCenter;
        private int yCenter;
        private int radius;
        private byte[][] stepResult;
        private Array<Step> pending = new Array<Step>(64);

        public FogMap(FloorMap floorMap, Token token) {
                this.floorMap = floorMap;
                this.token = token;
                fog = new FogState[floorMap.getWidth()][floorMap.getHeight()];
                for(int x=0; x<fog.length; x++){
                        for(int y=0; y<fog[x].length; y++){
                                fog[x][y] = FogState.Dark;
                        }
                }
        }


        public boolean isVisible(int x, int y){
                return fog[x][y] == FogState.Visible;
        }

        public boolean isVisited(int x, int y){
                if(x <0 || x>= fog.length || y< 0 || y>=fog[0].length){
                        return false;
                }
                return fog[x][y] != FogState.Dark;
        }

        public FogState getFogState(int x, int y){
                return fog[x][y];
        }

        public void update(){
                if(token.getFloorMap() != floorMap){
                        // if token not currently on the floor fo this fog map,
                        // then just set all visible fog in to visited
                        for(int x=0; x<fog.length; x++){
                                for(int y=0; y<fog[x].length; y++){
                                        if(fog[x][y] == FogState.Visible) {
                                                fog[x][y] = FogState.Visited;
                                        }
                                }
                        }
                        return;
                }
                xCenter = token.getLocation().x;
                yCenter = token.getLocation().y;
                radius = 6; // TODO: get vision radius fom the token.

                if (stepResult == null) {
                        int size = radius * 2 + 1;
                        stepResult = new byte[size][size];
                } else {
                        for (int i = 0; i < stepResult.length; i++) {
                                for (int j = 0; j < stepResult[i].length; j++) {
                                        stepResult[i][j] = 0;
                                }
                        }
                }

                pending.clear();
                pending.add(new Step(xCenter, yCenter, 0));

                while (pending.size > 0) {
                        Step step = pending.removeIndex(0);
                        calcStep(step.x, step.y, step.depth);
                }

                for (int x = 0; x < fog.length; x++) {
                        for (int y = 0; y < fog[x].length; y++) {
                                if (isVisibleMapCoord(x, y)) {
                                        fog[x][y] = FogState.Visible;
                                } else if (fog[x][y] == FogState.Visible) {
                                        fog[x][y] = FogState.Visited;
                                }
                        }
                }

        }


        protected boolean isSolid(int xMapCoord, int yMapCoord){
                // TODO: i feel like this should fix it so stairs dont block vision while
                // standing on them.. but it seems to cause fuckery

                Tile tile = floorMap.getTile(xMapCoord, yMapCoord);
                if(tile != null && tile.isStairs()){
                        if(xMapCoord == xCenter && yMapCoord == yCenter){
                                return false;
                        }
                }
                return tile == null || tile.isBlockVision();
        }

        private boolean isVisibleMapCoord(int xMapCoord, int yMapCoord) {
                if (xMapCoord < xCenter - radius || xMapCoord > xCenter + radius)
                        return false;
                if (yMapCoord < yCenter - radius || yMapCoord > yCenter + radius)
                        return false;
                return isStepVisible(xMapCoord - (xCenter - radius), yMapCoord - (yCenter - radius));
        }

        private boolean isStepChecked(int xLocal, int yLocal) {
                return stepResult[xLocal][yLocal] != 0;
        }

        private boolean isStepVisible(int xLocal, int yLocal) {
                return stepResult[xLocal][yLocal] == 1;
        }

        private void calcStep(int stepX, int stepY, int stepDepth) {
                int xDelta = stepX - xCenter;
                int yDelta = stepY - yCenter;

                int distSq = xDelta * xDelta + yDelta * yDelta;
                int radiusSq = radius * radius;
                if (distSq > radiusSq)
                        return; // tile is beyond visibility radius., ignore

                int xLocal = stepX - (xCenter - radius);
                int yLocal = stepY - (yCenter - radius);

                if (isStepChecked(xLocal, yLocal))
                        return;  // already checked this tile on another step

                if (isSolid(stepX, stepY)) {
                        stepResult[xLocal][yLocal]= 1;
                        return; // this tile blocks sight, we reveal it so the wall can be seen, and then stop branching
                } else if (stepDepth <= 2) {
                        stepResult[xLocal][yLocal]= 1;  // Tile is so close it must be visible
                } else if (xDelta == 0 && stepDepth == Math.abs(yDelta)) {
                        stepResult[xLocal][yLocal]= 1; // We are exactly north or south by depth tile must be visible
                } else if (yDelta == 0 && stepDepth == Math.abs(xDelta)) {
                        stepResult[xLocal][yLocal]= 1; // We are exactly east or west by depth so tile must be visible
                } else {
                        if (hasLineOfSight(stepX, stepY)) {
                                stepResult[xLocal][yLocal]= 1; // raycast determined that this tile is visible
                        } else {
                                stepResult[xLocal][yLocal]= -1;
                                return; // this tile is not visible, do not branch
                        }
                }

                pending.add(new Step(stepX, stepY + 1, stepDepth + 1)); // North
                pending.add(new Step(stepX, stepY - 1, stepDepth + 1)); // South
                pending.add(new Step(stepX + 1, stepY, stepDepth + 1)); // East
                pending.add(new Step(stepX - 1, stepY, stepDepth + 1)); // West

        }

        private static class Step {
                int x, y, depth;

                private Step(int x, int y, int depth) {
                        this.x = x;
                        this.y = y;
                        this.depth = depth;
                }
        }

        private boolean hasLineOfSight(int xMapCoord, int yMapCoord) {
                float xDelta = xMapCoord - xCenter;
                float yDelta = yMapCoord - yCenter;
                if (xDelta == 0 || yDelta == 0) {
                        // We would have found this a different way if we could see it.
                        return false;
                }

                float xDist = Math.abs(xDelta);
                float yDist = Math.abs(yDelta);
                int count;
                if (xDist == yDist) {
                        // We can move one square at a time, no problem.
                        count = (int) xDist;
                        xDelta = xDelta / count;
                        yDelta = yDelta / count;
                } else {
                        // We will make extra steps to make sure to always cross the adjacent borders.  Sometimes
                        // we will hit the same cell more than once but that's a small price to pay for simpler math.
                        count = (int) Math.max(xDist, yDist) * 3;
                        xDelta = xDelta / count;
                        yDelta = yDelta / count;
                }

                for (int i = 1; i < count; i++) {
                        float x = xCenter + 0.5f + i * xDelta;
                        float y = yCenter + 0.5f + i * yDelta;
                        if (isSolid((int) x, (int) y)) {
                                return false;
                        }
                }
                return true;
        }

        private boolean hasLineOfSightAlternate(int xMapCoord, int yMapCoord) {
                int sx, sy, xNext, yNext;
                float denom, dist;
                float xDelta = xMapCoord - xCenter;
                float yDelta = yMapCoord - yCenter;
                if (xCenter < xMapCoord) {
                        sx = 1;
                } else
                        sx = -1;


                if (yCenter < yMapCoord)
                        sy = 1;
                else
                        sy = -1;

                xNext = xCenter;
                yNext = yCenter;
                denom = (float) Math.sqrt(xDelta * xDelta + yDelta * yDelta);
                while (xNext != xMapCoord && yNext != yMapCoord) {
                        if (this.isSolid(xNext, yNext)) {
                                return false;
                        }
                        // Line-to-point distance formula < 0.5
                        if (Math.abs(yDelta * (xNext - xCenter + sx) - xDelta * (yNext - yCenter)) / denom < 0.5f)
                                xNext += sx;
                        else if (Math.abs(yDelta * (xNext - xCenter) - xDelta * (yNext - yCenter + sy)) / denom < 0.5f)
                                yNext += sy;
                        else {
                                xNext += sx;
                                yNext += sy;
                        }
                }

                return true;
        }


}