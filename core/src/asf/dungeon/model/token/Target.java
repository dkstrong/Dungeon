package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.Pair;

/**
 * Created by Danny on 11/11/2014.
 */
public class Target implements TokenComponent{
        private final Token t;
        private final Pair location = new Pair();                      // the location that this target wants to move to, target will move and attack through tiles along the way to get to its destination
        private Token target;                    // alternative to location and continousMoveDir, will constantly try to move to location of this target


        public Target(Token token) {
                this.t = token;
        }

        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                location.set(x,y);
                target = null;
                return true;
        }

        @Override
        public boolean update(float delta) {
                return false;
        }

        public Pair getLocation() {
                return location;
        }

        public void setLocation(Pair location) {
                this.location.set(location);
        }
        public void setLocation(int x, int y) {
                this.location.set(x,y);
        }

        public Token getToken() {
                return target;
        }

        public void setToken(Token target) {
                if(this.target == target){
                        return;
                }

                if(target == null){
                        this.target = null;
                        //location.set(t.getLocation());
                        return;
                }

                if(t.getFogMapping() != null){
                        FogMap fogMap = t.getFogMapping().getFogMap(t.getFloorMap());
                        if(!fogMap.isVisible(target.location.x, target.location.y)){
                                this.target = null;
                                return;
                        }
                }
                this.target = target;
                location.set(target.getLocation());
        }


}
