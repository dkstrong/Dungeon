package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.FogMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Danny on 11/11/2014.
 */
public class FogMapping implements TokenComponent {
        private final Token token;
        private Map<FloorMap, FogMap> fogMaps;

        public FogMapping(Token token) {
                this.token = token;
                fogMaps = new HashMap<FloorMap, FogMap>(16);
                fogMaps.put(token.getFloorMap(), new FogMap(token.getFloorMap(), token));
        }

        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                FogMap fogMap = fogMaps.get(token.getFloorMap());
                if (fogMap == null) {
                        fogMap = new FogMap(token.getFloorMap(), token);
                        fogMaps.put(token.getFloorMap(), fogMap);
                }
                fogMap.update();
                return true;
        }

        @Override
        public boolean update(float delta) {
                return false;
        }

        protected void computeFogMap() {
                fogMaps.get(token.getFloorMap()).update();
        }

        public FogMap getFogMap(FloorMap floorMap) {
                return fogMaps.get(floorMap);
        }

        public FogMap getCurrentFogMap(){
                return getFogMap(token.getFloorMap());
        }
}
