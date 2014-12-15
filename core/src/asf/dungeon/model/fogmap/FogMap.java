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
        private static transient int xCenter;
        private static transient int yCenter;
        private transient int radius; // not statis for effecient resue of stepResult
        private transient byte[][] stepResult; // not statis for effecient resue of stepResult
        private static final transient Array<Step> pending = new Array<Step>(true, 64, Step.class);
        private static final transient Array<Step> stepPool = new Array<Step>(false, 64, Step.class);

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

        public void revealMap(){
                for(int x=0; x<fog.length; x++){
                        for(int y=0; y<fog[x].length; y++){
                                if(fog[x][y] == FogState.Dark ){
                                        fog[x][y] = FogState.Visible;
                                }
                        }
                }
        }

        public void revealMapWithMagic(){
                for(int x=0; x<fog.length; x++){
                        for(int y=0; y<fog[x].length; y++){
                                if(fog[x][y] == FogState.Dark ){
                                        fog[x][y] = FogState.MagicMapped;
                                }
                        }
                }
        }

        public void revealLocationWithMagic(int xMapCoord, int yMapCoord){
                if(xMapCoord <0 || xMapCoord>= fog.length || yMapCoord< 0 || yMapCoord>=fog[0].length){
                        return;
                }
                fog[xMapCoord][yMapCoord] = FogState.MagicMapped;
        }

        public void revealLocation(int xMapCoord, int yMapCoord){
                if(xMapCoord <0 || xMapCoord>= fog.length || yMapCoord< 0 || yMapCoord>=fog[0].length){
                        return;
                }
                fog[xMapCoord][yMapCoord] = FogState.Visible;
        }


        public boolean isVisible(int x, int y){
                return fog[x][y] == FogState.Visible;
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

                if (stepResult == null || radius != token.getDamage().getSightRadius()) {
                        radius = token.getDamage().getSightRadius();
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
                pending.add(obtainStep(xCenter, yCenter, 0));

                while (pending.size > 0) {
                        Step step = pending.removeIndex(0);
                        freeStep(step);
                        calcStep(step.x, step.y, step.depth);
                }

                for (int x = 0; x < fog.length; x++) {
                        for (int y = 0; y < fog[x].length; y++) {
                                if (isStepVisibleMapCoord(x, y)) {
                                        fog[x][y] = FogState.Visible;
                                } else if (fog[x][y] == FogState.Visible) {
                                        fog[x][y] = FogState.Visited;
                                }
                        }
                }
        }



        private boolean isStepVisibleMapCoord(int xMapCoord, int yMapCoord) {
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

        private void setVisibleAdjacent(int xLocal, int yLocal, int xWorld, int yWorld){

                // TODO: need to do something similiar for wall tiles in the corner of rooms

                // if this is a door tile, set the adjcant wall tiles to visible
                if(!floorMap.getTile(xWorld, yWorld).isDoor()){
                    return;
                }

                if(xLocal -1 >=0){
                        Tile west = floorMap.getTile(xWorld-1, yWorld);
                        if(west != null && west.isWall())
                                stepResult[xLocal -1][yLocal] = 1;
                }

                if(xLocal +1 < stepResult.length){
                        Tile east = floorMap.getTile(xWorld+1, yWorld);
                        if(east != null && east.isWall())
                                stepResult[xLocal +1][yLocal] = 1;
                }

                if(yLocal -1 >= 0){
                        Tile south = floorMap.getTile(xWorld, yWorld-1);
                        if(south != null && south.isWall())
                                stepResult[xLocal][yLocal-1] = 1;
                }

                if(yLocal +1 < stepResult[0].length){
                        Tile north = floorMap.getTile(xWorld, yWorld+1);
                        if(north != null && north.isWall())
                                stepResult[xLocal][yLocal+1] = 1;
                }

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


                if(floorMap.isLocationVisionBlocked(xCenter,yCenter, stepX, stepY)){
                        stepResult[xLocal][yLocal]= 1;
                        setVisibleAdjacent(xLocal, yLocal, stepX, stepY);
                        return; // this tile blocks sight, we reveal it so the wall can be seen, and then stop branching
                } else if (stepDepth <= 2) {
                        stepResult[xLocal][yLocal]= 1;  // Tile is so close it must be visible
                } else if (xDelta == 0 && stepDepth == Math.abs(yDelta)) {
                        stepResult[xLocal][yLocal]= 1; // We are exactly north or south by depth tile must be visible
                } else if (yDelta == 0 && stepDepth == Math.abs(xDelta)) {
                        stepResult[xLocal][yLocal]= 1; // We are exactly east or west by depth so tile must be visible
                } else {
                        if (LOS.hasLineOfSight(floorMap, xCenter, yCenter, stepX, stepY)) {
                                stepResult[xLocal][yLocal]= 1; // raycast determined that this tile is visible
                        } else {
                                stepResult[xLocal][yLocal]= -1;
                                return; // this tile is not visible, do not branch
                        }
                }

                pending.add(obtainStep(stepX, stepY + 1, stepDepth + 1)); // North
                pending.add(obtainStep(stepX, stepY - 1, stepDepth + 1)); // South
                pending.add(obtainStep(stepX + 1, stepY, stepDepth + 1)); // East
                pending.add(obtainStep(stepX - 1, stepY, stepDepth + 1)); // West

        }

        private Step obtainStep(int x, int y, int depth){
                Step step = stepPool.size == 0 ? new Step() : stepPool.pop();
                step.x= x;
                step.y = y;
                step.depth = depth;
                return step;
        }

        private void freeStep(Step step){
                stepPool.add(step);
        }

        private static class Step {
                int x, y, depth;
        }




}
