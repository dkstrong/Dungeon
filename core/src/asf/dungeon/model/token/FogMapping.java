package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.fogmap.FogMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Danny on 11/11/2014.
 */
public class FogMapping implements TokenComponent, TeleportListener {
        private final Token token;
        private Map<FloorMap, FogMap> fogMaps;

        public FogMapping(Token token) {
                this.token = token;
                fogMaps = new HashMap<FloorMap, FogMap>(16);
        }

        @Override
        public void onTeleport(FloorMap fm, int x, int y, Direction direction) {
                FogMap fogMap = fogMaps.get(fm);
                if (fogMap == null) {
                        fogMap = new FogMap(fm,token);
                        fogMaps.put(fm, fogMap);
                }
                fogMap.update();
        }

        @Override
        public boolean update(float delta) {
                return false;
        }

        protected void computeFogMap() {
                fogMaps.get(token.floorMap).update();
        }

        public FogMap getFogMap(FloorMap floorMap) {
                return fogMaps.get(floorMap);
        }

        public FogMap getCurrentFogMap(){
                return getFogMap(token.floorMap);
        }
}
