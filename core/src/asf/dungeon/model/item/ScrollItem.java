package asf.dungeon.model.item;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.fogmap.FogMap;
import asf.dungeon.model.token.CharacterInventory;
import asf.dungeon.model.token.Journal;
import asf.dungeon.model.token.StatusEffect;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/24/2014.
 */
public class ScrollItem extends AbstractItem implements QuickItem, ConsumableItem.TargetsTokens,StackableItem{

        public static enum Type{
                Lightning, // casts lightining on nearest monster
                Fire, // controlling fireball with finger was a cool idea, but too much coding, will just be simple targeting scroll
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
                return type == Type.Lightning ||  type == Type.ScareMonsters;
        }

        @Override
        public void consume(Token token, CharacterInventory.UseItemOutcome out) {
                switch(type){
                        case Lightning:
                                charges--;
                                // cast lightning on nearest monster, if no monster nearby then does nothing
                                //Gdx.app.log("ScrollItem-Lightning","consume(), get visible tokens");
                                Array<Token> visibleTokens = token.floorMap.getVisibleTokens(token);
                                //Gdx.app.log("ScrollItem-Lightning",String.valueOf(visibleTokens));
                                Token closest = null;
                                float closestDistance = Float.MAX_VALUE;
                                for (Token visibleToken : visibleTokens) {
                                        if(visibleToken == token || visibleToken.damage == null || visibleToken.move == null || visibleToken.damage.isDead())
                                                continue;
                                        //Gdx.app.log("ScrollItem-Lightning","testing against:"+visibleToken.getName());
                                        float dist = token.distance(visibleToken);
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
                                        closest.damage.addHealth(-5);
                                }
                                break;
                        case Fire:
                                consume(token, token, out);
                                break;
                        case Ice:
                                consume(token, token, out);
                                break;
                        case Teleportation:
                                consume(token, token, out);
                                break;
                        case Confusion:
                                consume(token, token, out);
                                break;
                        case ScareMonsters:
                                charges--;
                                // nearby monsters run away  (may turn this in to a dropable item if i decide to have that)
                                out.didSomething = true;
                                StatusEffects statusEffects = token.statusEffects;
                                statusEffects.add(StatusEffect.ScaresMonsters, 8);
                                break;
                        default:
                                throw new AssertionError(type);
                }
                identifyItem(token);
        }

        @Override
        public void consume(Token token, Token targetToken, CharacterInventory.UseItemOutcome out) {
                //Gdx.app.log("Scroll Item",token+" targeted "+ targetToken+" with "+this+", num charges: "+charges);
                charges--;
                out.didSomething = true;
                if(type == Type.Ice){
                        // freeze target (or self), disables burning status effect, increases damage received
                        targetToken.statusEffects.add(StatusEffect.Frozen, 6);
                        out.targetToken = targetToken;
                        out.didSomething = true;
                }else if(type == Type.Fire){
                        // freeze target (or self), disables burning status effect, increases damage received
                        targetToken.statusEffects.add(StatusEffect.Burning, 10, 5);
                        out.targetToken = targetToken;
                        out.didSomething = true;
                }else if(type == Type.Teleportation){
                        // teleport target token to random location
                        // TODO: this code allows for teleporting into a locked room
                        // i need to make it so either it wont go into a locked room, or
                        // make it so that when getting locked in is detected that the player dies

                        // TODO: when teleporting Crates, Keys, and NPCs i might want to have additional logic
                        // here to make this stuff teleport to a desired (reachable) area.
                        int tries=0, x,y;
                        Dungeon dungeon = token.dungeon;
                        FloorMap floorMap = token.floorMap;
                        while(tries < 10){
                                x = dungeon.rand.random.nextInt(floorMap.getWidth());
                                y = dungeon.rand.random.nextInt(floorMap.getHeight());
                                if(targetToken.canTeleport(floorMap,x,y, token.direction)){
                                        targetToken.teleport(floorMap, x, y, token.direction);
                                        out.targetToken = targetToken;
                                        out.didSomething = true;
                                        break;
                                }
                                if(++tries >= 10){
                                        out.targetToken = targetToken;
                                       out.didSomething = false;
                                }
                        }
                }else if(type == Type.Confusion){
                        // causes target to become confused, may attack other monsters
                        targetToken.statusEffects.add(StatusEffect.Confused, 20);
                        out.targetToken = targetToken;
                        out.didSomething = true;
                }else{
                        throw new AssertionError(type);
                }
                identifyItem(token);
        }

        @Override
        public boolean canConsume(Token token, Token targetToken) {
                switch(type){

                        case Lightning:
                        case ScareMonsters:
                                return false;
                        case Confusion:
                                if(targetToken == null || targetToken.logic == null)
                                        return false;
                        case Fire:
                        case Ice:
                                if(targetToken == null || targetToken.damage == null || targetToken.move == null || targetToken.statusEffects == null)
                                        return false;
                                if(targetToken.damage.isDead())
                                        return false;
                                // cant consume if target is invisible and not on the same team
                                if(targetToken.logic != null && targetToken.logic.getTeam() != token.logic.getTeam()){
                                        if(targetToken.statusEffects != null && targetToken.statusEffects.has(StatusEffect.Invisibility)){
                                                return false;
                                        }
                                }
                                if(token.floorMap != targetToken.floorMap)
                                        return false;
                                // TODO: need to include LOS check if fogmapping is turned off
                                if(token.fogMapping != null){
                                        FogMap fogMap = token.fogMapping.getCurrentFogMap();
                                        if(!fogMap.isVisible(targetToken.location.x, targetToken.location.y))
                                                return false;
                                }
                                return true;
                        case Teleportation:
                                //Teleportation has similiar rules to the status effect scrolls, however telepotation,
                                // also works on Crates, Loot, and NPCs, this can be used to access keys that cant
                                // otherwise be accessed etc etc
                                if(targetToken == null)
                                        return false;
                                if(targetToken.damage!= null && targetToken.damage.isDead())
                                        return false;
                                // cant consume if target is invisible and not on the same team
                                if(targetToken.logic != null && targetToken.logic.getTeam() != token.logic.getTeam()){
                                        if(targetToken.statusEffects != null && targetToken.statusEffects.has(StatusEffect.Invisibility)){
                                                return false;
                                        }
                                }
                                if(token.floorMap != targetToken.floorMap)
                                        return false;
                                // TODO: need to include LOS check if fogmapping is turned off
                                if(token.fogMapping != null){
                                        FogMap fogMap = token.fogMapping.getCurrentFogMap();
                                        if(!fogMap.isVisible(targetToken.location.x, targetToken.location.y))
                                                return false;
                                }
                                // TODO: need to calculate the would be teleport location here
                                // and then find a way to store this and recall it when consume() is called
                                // for now will just teleporting to the same location
                                if(!targetToken.canTeleport(targetToken.floorMap, targetToken.location.x, targetToken.location.y,targetToken.direction))
                                     return false;

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

        @Override
        public String toString() {
                return "Scroll{"+type +"}";
        }
}
