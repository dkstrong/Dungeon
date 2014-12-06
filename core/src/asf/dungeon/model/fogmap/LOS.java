package asf.dungeon.model.fogmap;

import asf.dungeon.model.FloorMap;

/**
 * Created by Danny on 11/23/2014.
 */
public class LOS {

        /**
         * checks for LOS between two points of a floormap, only considers vision blocking
         * by vision blocking tiles, does not consider vision radius of the vanatage point or anything else
         * @param floorMap
         * @param xVantage
         * @param yVantage
         * @param xTarget
         * @param yTarget
         * @return
         */
        public static boolean hasLineOfSight(FloorMap floorMap, int xVantage, int yVantage, int xTarget, int yTarget) {
                float xDelta = xTarget - xVantage;
                float yDelta = yTarget - yVantage;
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
                        float x = xVantage + 0.5f + i * xDelta;
                        float y = yVantage + 0.5f + i * yDelta;
                        if (floorMap.isLocationVisionBlocked(xVantage, yVantage, (int) x, (int) y)) {
                                return false;
                        }
                }
                return true;
        }

        public static boolean hasLineOfSightAlternate(FloorMap floorMap, int xVantage, int yVantage, int xTarget, int yTarget) {
                int sx, sy, xNext, yNext;
                float denom, dist;
                float xDelta = xTarget - xVantage;
                float yDelta = yTarget - yVantage;
                if (xVantage < xTarget) {
                        sx = 1;
                } else
                        sx = -1;


                if (yVantage < yTarget)
                        sy = 1;
                else
                        sy = -1;

                xNext = xVantage;
                yNext = yVantage;
                denom = (float) Math.sqrt(xDelta * xDelta + yDelta * yDelta);
                while (xNext != xTarget && yNext != yTarget) {
                        if (floorMap.isLocationVisionBlocked(xVantage, yVantage, xNext, yNext)) {
                                return false;
                        }
                        // Line-to-point distance formula < 0.5
                        if (Math.abs(yDelta * (xNext - xVantage + sx) - xDelta * (yNext - yVantage)) / denom < 0.5f)
                                xNext += sx;
                        else if (Math.abs(yDelta * (xNext - xVantage) - xDelta * (yNext - yVantage + sy)) / denom < 0.5f)
                                yNext += sy;
                        else {
                                xNext += sx;
                                yNext += sy;
                        }
                }

                return true;
        }


        private LOS(){

        }
}
