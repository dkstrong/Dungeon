package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.fogmap.LOS;
import asf.dungeon.model.item.ConsumableItem;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.token.quest.Choice;

/**
 * This component is how the token receives commands from the player or the ai
 *
 * player and ai should not initiateChat with the token directly.
 */
public class Command implements TokenComponent{
        private final Token token;
        private final Pair location = new Pair();                      // the location that this targetToken wants to move to, targetToken will move and attack through tiles along the way to get to its destination
        private Token targetToken;                    // alternative to location and continousMoveDir, will constantly try to move to location of this targetToken

        protected Tile canUseKeyOnTile;
        private Tile useKeyOnTile;

        protected ConsumableItem consumeItem;
        protected Item targetItem;

        private Choice chatChoice;

        public Command(Token token) {
                this.token = token;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {
                location.set(x,y);
                targetToken = null;
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

                if(location.equals(x,y))
                        return;
                canUseKeyOnTile = null;
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

        public boolean consumeItem(ConsumableItem item){
                if (consumeItem != null || token.getDamage().isDead())
                        return false; // already consuming an item, or dead
                //Gdx.app.log("Command",token+" consume "+item);
                consumeItem = (ConsumableItem) item;
                return true;

        }

        public boolean consumeItem(ConsumableItem item, Item targetItem){
                boolean valid = consumeItem(item);
                if(valid)
                        this.targetItem = targetItem;
                return valid;
        }

        public boolean consumeItem(ConsumableItem item, Token targetToken){
                boolean valid = consumeItem(item);
                if(valid){
                        setTargetToken(targetToken);
                }
                //Gdx.app.log("Command",token+" consume "+item+" on target "+targetToken);
                return valid;
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

                if(targetToken.getStatusEffects() != null){
                        if(targetToken.getStatusEffects().has(StatusEffect.Invisibility)){
                                this.targetToken = null;
                                return;
                        }
                }


                if(token.getFogMapping() != null){
                        FogMap fogMap = token.getFogMapping().getFogMap(token.getFloorMap());
                        if(!fogMap.isVisible(targetToken.location.x, targetToken.location.y)){
                                this.targetToken = null;
                                return;
                        }
                }else{
                        // same LOS fallback used in Attack
                        if(!LOS.hasLineOfSightManual(token.getFloorMap(), token.location.x, token.location.y, targetToken.location.x, targetToken.location.y)){
                                this.targetToken = null;
                                return ;
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


        public boolean hasChatChoice(){
                return chatChoice !=null;
        }
        public Choice getChatChoice() {
                return chatChoice;
        }

        public void setChatChoice(Choice chatChoice) {
                this.chatChoice = chatChoice;
        }
}
