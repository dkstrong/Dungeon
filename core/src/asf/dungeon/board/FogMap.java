package asf.dungeon.board;

import com.badlogic.gdx.utils.Array;

/**
 * adapted from SensorArea in Monkey Trap
 */
public class FogMap {
        private FloorMap floorMap;
        private Token token;
        private FogState[][] fog;
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

                if(token.floorMap != floorMap){
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



                xCenter =token.location.x;
                yCenter = token.location.y;
                radius = 6;

                if( stepResult == null ) {
                        int size = radius * 2 + 1;
                        stepResult = new byte[size][size];
                } else {
                        for( int i = 0; i < stepResult.length; i++ ) {
                                for(int j=0; j <stepResult[i].length; j++){
                                        stepResult[i][j] = 0;
                                }
                        }
                }

                pending.clear();
                pending.add(new Step(xCenter, yCenter, 0));

                while(pending.size>0){
                        Step step = pending.removeIndex(0);
                        calcStep(step.x, step.y, step.depth);
                }

                for(int x=0; x<fog.length; x++){
                        for(int y=0; y<fog[x].length; y++){
                                if(isVisibleMapCoord(x, y)){
                                        fog[x][y] = FogState.Visible;
                                }else if(fog[x][y] == FogState.Visible) {
                                        fog[x][y] = FogState.Visited;
                                }
                        }
                }
        }



        private boolean isSolid(int xMapCoord, int yMapCoord){
                // TODO: i feel like this should fix it so stairs dont block vision while
                // standing on them.. but it seems to cause fuckery


                FloorTile tile = floorMap.getTile(xMapCoord, yMapCoord);
                if(tile != null && tile.isStairs()){
                        if(xMapCoord == xCenter && yMapCoord == yCenter){
                                return false;
                        }
                }
                return tile == null || tile.isBlockVision();
        }

        private boolean isVisibleMapCoord(int xMapCoord, int yMapCoord) {
                if( xMapCoord < xCenter - radius || xMapCoord > xCenter + radius )
                        return false;
                if( yMapCoord < yCenter - radius || yMapCoord > yCenter + radius )
                        return false;
                return isStepVisible(xMapCoord - (xCenter - radius), yMapCoord - (yCenter - radius));
        }

        private boolean isStepChecked(int xLocal, int yLocal){
                return stepResult[xLocal][yLocal] != 0;
        }

        private boolean isStepVisible(int xLocal, int yLocal){
                return stepResult[xLocal][yLocal] == 1;
        }

        private void setStepVisible(int xLocal, int yLocal, boolean visible){
                if(visible){
                        stepResult[xLocal][yLocal] = 1;
                }else{
                        stepResult[xLocal][yLocal] = -1;
                }
        }





        private void calcStep(int stepX, int stepY, int stepDepth){
                int xDelta = stepX - xCenter;
                int yDelta = stepY - yCenter;

                // See if we've exceeded our maximum range then we're done
                int distSq = xDelta * xDelta + yDelta * yDelta;
                int radiusSq= radius*radius;
                if( distSq > radiusSq )
                        return;

                // See if we've visited this spot before
                int xLocal = stepX - (xCenter - radius);
                int yLocal = stepY - (yCenter - radius);

                if( isStepChecked(xLocal, yLocal) )
                        return;

                setStepVisible(xLocal, yLocal, false); // set not visible so this wont be checked again

                // Now see if it is solid
                if( isSolid(stepX, stepY) ) {
                        setStepVisible(xLocal, yLocal, true); // put this here so the wall itself will be revealed
                        return;
                }

                // See if it is visible
                if( stepDepth <= 2 ) {
                        // No way this can't be visible
                        setStepVisible(xLocal, yLocal, true);
                } else if( xDelta == 0 && stepDepth == Math.abs(yDelta) ) {
                        // We are exactly north or south by depth so we must be
                        // visible
                        setStepVisible(xLocal, yLocal, true);
                } else if( yDelta == 0 && stepDepth == Math.abs(xDelta) ) {
                        // We are exactly east or west by depth so we must be visible
                        setStepVisible(xLocal, yLocal, true);
                } else {
                        // Cast a ray to the point and see if we are blocked
                        if( isViewUnobstructed(stepX, stepY) ) {
                                setStepVisible(xLocal, yLocal, true);
                        } else {
                                // We should not branch
                                return;
                        }
                }

                // Now scan out in the various directions... we'll
                // let the top of the loop sort out if they are
                // outside the range, blocked, etc..
                pending.add(new Step(stepX, stepY + 1, stepDepth + 1)); // North
                pending.add(new Step(stepX, stepY - 1, stepDepth + 1)); // South
                pending.add(new Step(stepX+1, stepY , stepDepth + 1)); // East
                pending.add(new Step(stepX-1, stepY , stepDepth + 1)); // West

        }

        private static class Step{
                int x, y, depth;

                private Step(int x, int y, int depth) {
                        this.x = x;
                        this.y = y;
                        this.depth = depth;
                }
        }

        private boolean isViewUnobstructed(int xWorld, int yWorld) {
                float xDelta = xWorld - xCenter;
                float yDelta = yWorld - yCenter;
                if( xDelta == 0 || yDelta == 0 ) {
                        // We would have found this a different way if we could see it.
                        return false;
                }

                float xDist = Math.abs(xDelta);
                float yDist = Math.abs(yDelta);
                int count;
                if( xDist == yDist ) {
                        // We can move one square at a time, no problem.
                        count = (int)xDist;
                        xDelta = xDelta / count;
                        yDelta = yDelta / count;
                } else {
                        // We will make extra steps to make sure to
                        // always cross the adjacent borders.  Sometimes
                        // we will hit the same cell more than once but that's
                        // a small price to pay for simpler math.
                        count = (int)Math.max(xDist, yDist) * 3;
                        xDelta = xDelta / count;
                        yDelta = yDelta / count;
                }

                for( int i = 1; i < count; i++ ) {
                        float x = xCenter + 0.5f + i * xDelta;
                        float y = yCenter + 0.5f + i * yDelta;
                        if( isSolid((int)x, (int)y) ) {
                                return false;
                        }
                }
                return true;
        }

}
