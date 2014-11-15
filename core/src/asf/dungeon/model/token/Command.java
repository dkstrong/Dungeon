package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.item.Consumable;
import asf.dungeon.model.item.Item;

/**
 * This component is how the token receives commands from the player or the ai
 *
 * player and ai should not interact with the token directly.
 */
public class Command implements TokenComponent{
        private final Token token;
        private final Pair location = new Pair();                      // the location that this targetToken wants to move to, targetToken will move and attack through tiles along the way to get to its destination
        private Token targetToken;                    // alternative to location and continousMoveDir, will constantly try to move to location of this targetToken

        protected Tile canUseKeyOnTile;
        private Tile useKeyOnTile;

        protected Consumable consumeItem;

        public Command(Token token) {
                this.token = token;
        }

        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                location.set(x,y);
                targetToken = null;
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
                setLocation(location.x, location.y);
        }

        public void setLocation(int x, int y) {
                canUseKeyOnTile = null;
                if(location.equals(x,y))
                        return;
                this.location.set(x,y);
                targetToken = null;
                useKeyOnTile = null;


        }

        public void setUseKeyOnTile(Pair location){
                if(canUseKeyOnTile != null){
                        Tile tile = token.getFloorMap().getTile(location);
                        if(canUseKeyOnTile == tile && tile.isDoor() && tile.isDoorLocked()){
                                this.location.set(location);
                                useKeyOnTile =tile;
                                targetToken = null;
                                canUseKeyOnTile = null;
                        }
                }else{
                        useKeyOnTile = null;
                }
        }

        public boolean consumeItem(Item item){
                if(item instanceof Consumable){
                        if (consumeItem != null || token.getDamage().isDead())
                                return false; // already consuming an item, or dead
                        consumeItem = (Consumable) item;
                        return true;
                }
                return false;

        }

        public Token getTargetToken() {
                return targetToken;
        }

        public void setTargetToken(Token targetToken) {
                if(this.targetToken == targetToken){
                        return;
                }

                if(targetToken == null){
                        this.targetToken = null;
                        //location.set(token.getLocation());
                        return;
                }

                if(token.getFogMapping() != null){
                        FogMap fogMap = token.getFogMapping().getFogMap(token.getFloorMap());
                        if(!fogMap.isVisible(targetToken.location.x, targetToken.location.y)){
                                this.targetToken = null;
                                return;
                        }
                }
                this.targetToken = targetToken;
                location.set(targetToken.getLocation());
                useKeyOnTile = null;
                canUseKeyOnTile = null;
        }

        public boolean isUseKey() {
                return useKeyOnTile != null;
        }

        public Tile getUseKeyOnTile() {
                return useKeyOnTile;
        }
}
