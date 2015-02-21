package asf.dungeon.model.token;


import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.item.KeyItem;
import asf.dungeon.model.token.logic.LocalPlayerLogic;
import asf.dungeon.model.token.logic.Logic;
import asf.dungeon.model.token.logic.fsm.FsmLogic;
import asf.dungeon.model.token.logic.fsm.State;
import asf.dungeon.model.token.quest.Dialouge;
import asf.dungeon.model.token.quest.Quest;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Created by danny on 10/22/14.
 */
public class Token {
        public final Dungeon dungeon;
        private int id;
        public ModelId modelId;
        /**
         * the name of this character or item for the interfce
         */
        public String name;
        /**
         * whether or not other tokens can stand on the same tile as this one. typically crates and characters block pathing, but pickups do not.
         */
        public boolean blocksPathing = true;

        // state variables
        public final Pair location = new Pair();                     // the current tile that this token is considered to be standing on
        public FloorMap floorMap;
        public Direction direction = Direction.South;          // the direction that this token is facing, this affects certain gameplay mechanics.
        private final Array<TokenComponent> components = new Array<TokenComponent>(true, 8, TokenComponent.class);
        public transient Listener listener;
        // Common Components
        public Logic logic;
        public Experience experience;
        public Command command;
        public Interactor interactor;
        public Move move;
        public Damage damage;
        public Attack attack;
        public CharacterInventory inventory;
        public CrateInventory crateInventory;
        public FogMapping fogMapping;
        public StatusEffects statusEffects;
        public Loot loot;
        public Stairs stairs;
        public MonsterTrap monsterTrap;


        public Token(Dungeon dungeon, int id, String name, ModelId modelId) {
                this.dungeon = dungeon;
                this.id = id;
                this.name = name;
                this.modelId = modelId;
        }

        public Token(Dungeon dungeon, String name, ModelId modelId) {
                this.dungeon = dungeon;
                this.name = name;
                this.modelId = modelId;
        }


        public void setId(int id) {
                this.id = id;
        }

        /**
         * DO NOT MODIFY
         * @return
         */
        public Array<TokenComponent> getComponents() {
                return components;
        }

        public <T extends TokenComponent> T get(Class<T> componentClass) {
                for (TokenComponent component : components) {
                        if (componentClass.isAssignableFrom(component.getClass())) {
                                return (T) component;
                        }
                }
                return null;
        }

        public void remove(TokenComponent component){
                if(component == null) return;
                // TODO: if removing a "common" component then need to null it
                // though common componnets typicallly wouldnt be removed
                components.removeValue(component, true);
        }

        public void add(TokenComponent component) {
                if(component == null) return;
                components.add(component);
                if (component instanceof Logic) {
                        this.logic = (Logic) component;
                } else if (component instanceof Interactor) {
                        this.interactor = (Interactor) component;
                } else if (component instanceof Damage) {
                        this.damage = (Damage) component;
                } else if (component instanceof FogMapping) {
                        this.fogMapping = (FogMapping) component;
                } else if (component instanceof Command) {
                        this.command = (Command) component;
                } else if (component instanceof CharacterInventory) {
                        this.inventory = (CharacterInventory) component;
                } else if (component instanceof CrateInventory) {
                        this.crateInventory = (CrateInventory) component;
                } else if (component instanceof Attack) {
                        this.attack = (Attack) component;
                } else if (component instanceof Move) {
                        this.move = (Move) component;
                } else if (component instanceof Experience) {
                        this.experience = (Experience) component;
                } else if(component instanceof StatusEffects){
                        this.statusEffects = (StatusEffects)component;
                }else if(component instanceof Loot){
                        this.loot = (Loot) component;
                }else if(component instanceof Stairs){
                        this.stairs = (Stairs) component;
                }else if(component instanceof MonsterTrap){
                        this.monsterTrap = (MonsterTrap) component;
                }
        }

        public boolean canSpawn(FloorMap fm, int x, int y, Direction dir){
                if (fm.hasTokensAt(x,y)) return false;
                return canTeleport(fm, x, y, dir);
        }

        public boolean canTeleport(FloorMap fm, int x, int y, Direction dir) {
                Tile tile = fm.getTile(x, y);
                if (tile == null || tile.isDoor() || tile.blockMovement) return false;

                for (TokenComponent c : components) {
                        if(c instanceof Teleportable && !((Teleportable) c ).canTeleport(fm,x, y, dir))
                                return false;
                }
                return true;
        }

        public boolean teleport(FloorMap fm, int x, int y, Direction dir) {

                if (!canTeleport(fm, x, y, dir)) {
                        // TODO: idealy i should be able to remove this if check, proper canTeleport checks should be built in to the calling code
                        throw new AssertionError(name + "- not a valid teleport location, need to include a check for this earlier in the code");
                }

                floorMap = fm;
                location.set(x, y);
                direction = dir;

                for (TokenComponent c : components) {
                        if(c instanceof Teleportable) ((Teleportable) c ).teleport(fm,x, y, direction);
                }

                return true;
        }

        public void updateComponents(float delta) {
                for (int i = 0; i < components.size; i++) {
                        if (components.items[i].update(delta)) {
                                return; // this component consumed the rest of the update
                        }
                }
        }

        public boolean isLocatedAt(Pair loc) {
                return location.equals(loc);
        }

        public boolean isLocatedAt(int x, int y) {
                return location.x == x && location.y == y;
        }

        public float distance(Token other){
                if(move == null){
                        if(other.move == null) return distance(other.location);
                        return other.move.getFloatLocation().dst(location.x, location.y);
                }else{
                        if(other.move == null) return move.getFloatLocation().dst(other.location.x, other.location.y);
                        return move.getFloatLocation().dst(other.move.getFloatLocation());
                }
        }

        public float distance(Pair loc){
                float x = getFloatLocationX();
                float y = getFloatLocationY();
                final float x_d = x - loc.x;
                final float y_d = y - loc.y;
                return (float)Math.sqrt(x_d * x_d + y_d * y_d);
        }

        public float distance(Vector2 loc){
                float x = getFloatLocationX();
                float y = getFloatLocationY();
                final float x_d = x - loc.x;
                final float y_d = y - loc.y;
                return (float)Math.sqrt(x_d * x_d + y_d * y_d);
        }

        public float getFloatLocationX(){
                if(move == null) return location.x;
                return move.getFloatLocation().x;
        }

        public float getFloatLocationY(){
                if(move == null) return location.y;
                return move.getFloatLocation().y;
        }

        public int getId() {
                return id;
        }

        public static interface Listener {

                public void onPathBlocked(Pair nextLocation, Tile nextTile);

                public void onAttack(Token target, Pair targetLocation, boolean ranged);

                public void onAttacked(Token attacker, Token target, Attack.AttackOutcome attackOutcome);

                public void onInventoryChanged();

                public void onUseItem(Item item, CharacterInventory.UseItemOutcome out);

                public void onStatusEffectChange(StatusEffect effect, float duration);

                /**
                 * @param journalObject can be PotionItem.Type, ScrollItem.Type, BookItem.Type, or EquipmentItem
                 * @param study         if the object was learned via study or not, if not learned by study then it was learned by Tome of identification
                 */
                public void onLearned(Object journalObject, boolean study);

                public void onInteract(Quest quest, Dialouge dialouge);

                public void onFsmStateChange(FsmLogic fsm, State oldState, State newState);
        }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Token token = (Token) o;

                if (id != token.id) return false;

                return true;
        }

        @Override
        public int hashCode() {
                return id;
        }

        @Override
        public String toString() {
                return name;
        }

        public char toCharacter(){
                if(logic != null){
                        if(logic instanceof LocalPlayerLogic) return '@';
                        if(get(Quest.class) != null) return 'q';
                        return '#';
                }
                if(loot!= null){
                        if(loot.getItem() instanceof KeyItem){
                                KeyItem.Type keyType = ((KeyItem) loot.getItem()).getType();
                                if(keyType == KeyItem.Type.Red) return 'r';
                                else if(keyType == KeyItem.Type.Gold) return 'g';
                                else if(keyType == KeyItem.Type.Silver) return 's';
                                else return '?';
                        }
                        return '$';
                }
                if(crateInventory != null){
                        if(crateInventory.getItemToDrop() instanceof KeyItem){
                                KeyItem.Type keyType = ((KeyItem) crateInventory.getItemToDrop()).getType();
                                if(keyType == KeyItem.Type.Red) return 'r';
                                else if(keyType == KeyItem.Type.Gold) return 'g';
                                else if(keyType == KeyItem.Type.Silver) return 's';
                                else return '?';
                        }
                        return '$';
                }

                if(stairs != null){
                        if(stairs.isStairsUp())
                                return '^';
                        else
                                return '&';
                }

                if(get(Torch.class) != null) return 't';

                if(get(Fountain.class) != null) return 'f';

                return '?';
        }
}
