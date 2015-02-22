package asf.dungeon.model.floorgen;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.token.Token;

/**
 * a group of tokens that should spawn togethor with certain offsets from the origin (0,0)
 *
 * once spawnIfPossible() is called (and it returns true) then you need to call
 * setTokens() again to supply new token instances, then eventually throw out the SpawnGroup
 * instance itself.
 *
 * Created by Daniel Strong on 2/21/2015.
 */
public class SpawnGroup {
        public Direction currentDir;
        public Token[] tokens;

        /**
         *  must call this before calling spawn. after a succesful spawning of a spawngroup
         *  then setTokens needs to be called again (supplying different tokens of course)
         * @param tokens
         */
        public void setTokens(Token... tokens){
                currentDir = Direction.South;
                this.tokens = tokens;
        }

        /**
         * attempts to spawn the group at this location
         *
         * if there is some reason the group can not be spawned here it will return false
         * and nothing will happen.
         *
         * Once this method is called and returns true, then this spawn group should be thrown away
         * and not used any more
         * @param dungeon
         * @param floorMap
         * @param x
         * @param y
         * @param dir
         * @return
         */
        public boolean spawnIfPossible(Dungeon dungeon, FloorMap floorMap, int x, int y, Direction dir){
                for (Token token : tokens) {
                        token.location.rotate(currentDir, dir);
                        int degrees = dir.degrees - currentDir.degrees;
                        token.direction = token.direction.rotate(degrees);
                }
                currentDir = dir;

                for (Token token : tokens) {
                        final int tokenX = x+token.location.x;
                        final int tokenY = y+token.location.y;
                        final Direction tokenDir = token.direction;

                        if(!token.isGoodSpawnLocation(floorMap,tokenX, tokenY, tokenDir)){
                                return false;
                        }
                }

                for (Token token : tokens) {
                        final int tokenX = x+token.location.x;
                        final int tokenY = y+token.location.y;
                        dungeon.addToken(token, floorMap, tokenX, tokenY);
                }


                return true;

        }


}
