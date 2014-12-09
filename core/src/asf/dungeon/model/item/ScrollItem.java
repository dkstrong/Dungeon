package asf.dungeon.model.item;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.token.Inventory;
import asf.dungeon.model.token.Journal;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/24/2014.
 */
public class ScrollItem extends AbstractItem implements QuickItem, ConsumableItem.TargetsTokens,StackableItem{




        public static enum Type{
                Lightning, // casts lightining on nearest monster
                Fire, // fireball that can be controlled with finger
                Ice, // freeze selected target
                Teleportation, // teleports you or another target to a random location in the dungeon
                Confusion, // target monster is confused, may start to attack other monsters
                ScareMonsters, // nearby monsters run away  (may turn this in to a dropable item if i decide to have that)

        }

        public static enum Symbol{
                Air, Earth, Fire, Light, Metal, Shadow, Water, Wood;
        }

        @Override
        public boolean isPrimarilySelfConsume() {
                return type == Type.Lightning || type == Type.Fire || type == Type.ScareMonsters;
        }

        @Override
        public void consume(Token token, Inventory.CharacterInventory.UseItemOutcome out) {
                charges--;
                switch(type){
                        case Lightning:
                                // cast lightning on nearest monster, if no monster nearby then does nothing
                                Gdx.app.log("ScrollItem-Lightning","consume(), get visible tokens");
                                Array<Token> visibleTokens = token.getFloorMap().getVisibleTokens(token);
                                Gdx.app.log("ScrollItem-Lightning",String.valueOf(visibleTokens));
                                Token closest = null;
                                int closestDistance = Integer.MAX_VALUE;
                                for (Token visibleToken : visibleTokens) {
                                        if(visibleToken == token || visibleToken.getDamage() == null || visibleToken.getMove() == null || visibleToken.getDamage().isDead())
                                                continue;
                                        Gdx.app.log("ScrollItem-Lightning","testing against:"+visibleToken.getName());
                                        int dist = visibleToken.getLocation().distance(token.getLocation());
                                        if(dist< closestDistance){
                                                closestDistance = dist;
                                                closest = visibleToken;
                                        }
                                }
                                if(closest == null){
                                        out.didSomething = false;
                                }else{
                                        out.didSomething = true;
                                        out.targetToken = closest;
                                        out.damage = 5;
                                        closest.getDamage().addHealth(-5);
                                }
                                break;
                        case Fire:
                                // fireball that can be controlled with finger
                                out.didSomething = true;
                                break;
                        case Ice:
                                // freeze yourself
                                out.didSomething = true;
                                break;
                        case Teleportation:
                                consume(token, token, out);
                                break;
                        case Confusion:
                                // does nothing
                                out.didSomething = false;
                                break;
                        case ScareMonsters:
                                // nearby monsters run away  (may turn this in to a dropable item if i decide to have that)
                                out.didSomething = true;
                                break;
                        default:
                                throw new AssertionError(type);
                }
                identifyItem(token);
        }

        @Override
        public void consume(Token token, Token targetToken, Inventory.CharacterInventory.UseItemOutcome out) {
                charges--;
                out.didSomething = true;
                if(type == Type.Ice){
                        // freeze target token
                }else if(type == Type.Teleportation){
                        // teleport target token to random location
                        // TODO: this code allows for teleporting into a locked room
                        // i need to make it so either it wont go into a locked room, or
                        // make it so that when getting locked in is detected that the player dies
                        int x,y;
                        Dungeon dungeon = token.dungeon;
                        FloorMap floorMap = token.getFloorMap();
                        do{
                                x = dungeon.rand.random.nextInt(floorMap.getWidth());
                                y = dungeon.rand.random.nextInt(floorMap.getHeight());
                        }while(floorMap.getTile(x,y) == null || !floorMap.getTile(x,y).isFloor() || floorMap.hasTokensAt(x,y));

                        targetToken.teleport(floorMap, x, y, token.getDirection());
                        out.targetToken = targetToken;
                        out.didSomething = true;
                }else if(type == Type.Confusion){
                        // causes target to become confused, may attack other monsters
                }else{
                        throw new AssertionError(type);
                }
                identifyItem(token);
        }

        @Override
        public boolean canConsume(Token token, Token targetToken) {
                switch(type){

                        case Lightning:
                        case Fire:
                        case ScareMonsters:
                                return false;
                        case Ice:
                        case Confusion:
                                if(token == targetToken)
                                        return false;
                        case Teleportation:
                                if(targetToken == null || targetToken.getDamage() == null || targetToken.getMove() == null)
                                        return false;
                                if(targetToken.getDamage().isDead())
                                        return false;
                                if(token.getFloorMap() != targetToken.getFloorMap())
                                        return false;
                                if(token.getFogMapping() != null){
                                        FogMap fogMap = token.getFogMapping().getCurrentFogMap();
                                        if(!fogMap.isVisible(targetToken.getLocation().x, targetToken.getLocation().y))
                                                return false;
                                }
                                return true;
                        default:
                                throw new AssertionError(type);
                }
        }




        private final Dungeon dungeon;
        private final Symbol symbol;
        private final Type type;
        private int charges;

        public ScrollItem(Dungeon dungeon, Type type, int charges) {
                this.dungeon = dungeon;
                this.symbol = dungeon.getMasterJournal().getScrollSymbol(type);
                this.type = type;
                this.charges = charges;
        }

        @Override
        public ModelId getModelId() {
                return ModelId.Scroll;
        }


        @Override
        public String getName() {
                return "Scroll of "+type.name();
        }

        @Override
        public String getDescription() {
                return "This is a "+getName()+". Use it well.";
        }

        @Override
        public String getVagueName() {
                return "Unidentified Scroll";
        }

        @Override
        public String getVagueDescription() {
                return "An unidentified scroll. Who knows what will happen when read out loud?";
        }

        @Override
        public boolean isIdentified(Token token) {
                Journal journal = token.get(Journal.class);
                return journal == null || journal.knows(type);
        }
        @Override
        public void identifyItem(Token token) {
                Journal journal = token.get(Journal.class);
                if (journal != null)
                        journal.learn(this);
        }

        public Type getType() {
                return type;
        }

        public Symbol getSymbol() {
                return symbol;
        }

        @Override
        public void stack(StackableItem other) {
                ScrollItem otherScroll = (ScrollItem) other;
                charges+=otherScroll.charges;
                otherScroll.charges = 0;
        }

        @Override
        public ScrollItem unStack(int numCharges) {
                if(numCharges >= charges) throw new IllegalStateException("numCharges for unstacked item must be less than current charges");
                ScrollItem newScroll = new ScrollItem(dungeon, type, numCharges);
                charges -= numCharges;
                return newScroll;
        }

        @Override
        public int getCharges() {
                return charges;
        }

        @Override
        public boolean canStackWith(StackableItem other) {
                if(other instanceof ScrollItem){
                        ScrollItem otherScroll = (ScrollItem) other;
                        return type == otherScroll.type;
                }
                return false;
        }
}
