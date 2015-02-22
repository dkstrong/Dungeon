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
public class Command implements TokenComponent, TeleportListener {
        private final Token token;
        private final Pair location = new Pair();                      // the location that this targetToken wants to move to, targetToken will move and attack through tiles along the way to get to its destination
        private Token targetToken;                    // alternative to location and continousMoveDir, will constantly try to move to location of this targetToken

        protected Tile canUseKeyOnTile;
        private Tile useKeyOnTile;

        protected ConsumableItem consumeItem;
        protected Token targetItemToken;
        protected Item targetItem;

        private transient Choice chatChoice;

        public Command(Token token) {
                this.token = token;
        }

        @Override
        public void onTeleport(FloorMap fm, int x, int y, Direction direction) {
                setLocation(x,y);
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
                this.location.set(x,y);
                targetToken = null;
                useKeyOnTile = null;
                canUseKeyOnTile = null;
                token.inventory.showDoorLockedMessage = true;
        }

        public void setUseKeyOnTile(Pair location){
                if(canUseKeyOnTile != null){
                        Tile tile = token.floorMap.getTile(location);
                        if(canUseKeyOnTile == tile && tile.isDoor() && tile.isDoorLocked()){
                                this.location.set(location);
                                useKeyOnTile =tile;
                                targetToken = null;
                                canUseKeyOnTile = null;
                                token.inventory.showDoorLockedMessage = true;
                        }
                }else{
                        useKeyOnTile = null;
                }
        }

        public boolean consumeItem(ConsumableItem item){
                if (consumeItem != null || token.damage.isDead())
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
                if(valid)
                        this.targetItemToken = targetToken;

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
                        return;
                }

                if(targetToken.damage == null || !targetToken.damage.isAttackable()){
                        this.targetToken = null;
                        return;
                }

                if(targetToken.logic != null && targetToken.logic.getTeam() == token.logic.getTeam()){
                        this.targetToken = null;
                        return;
                }


                if(token.fogMapping != null){
                        FogMap fogMap = token.fogMapping.getFogMap(token.floorMap);
                        if(!fogMap.isVisible(targetToken.location.x, targetToken.location.y)){
                                this.targetToken = null;
                                return;
                        }
                }else{
                        // same LOS fallback used in Attack
                        if(!LOS.hasLineOfSightManual(token.floorMap, token.location.x, token.location.y, targetToken.location.x, targetToken.location.y)){
                                this.targetToken = null;
                                return ;
                        }
                }
                this.targetToken = targetToken;
                location.set(targetToken.location);
                useKeyOnTile = null;
                canUseKeyOnTile = null;
                token.inventory.showDoorLockedMessage = true;

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
