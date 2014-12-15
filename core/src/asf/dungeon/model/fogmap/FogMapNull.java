package asf.dungeon.model.fogmap;

import asf.dungeon.model.FloorMap;
import asf.dungeon.model.token.Token;

/**
 * Created by Daniel Strong on 12/14/2014.
 */
public class FogMapNull extends FogMap{
        public FogMapNull(FloorMap floorMap, Token token) {
                super(floorMap, token);
        }

        @Override
        public boolean isVisible(int x, int y) {
                return true;
        }

        @Override
        public FogState getFogState(int x, int y) {
                return FogState.Visible;
        }
}
