package asf.dungeon.model.token;


import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.item.Item;
import asf.dungeon.model.token.logic.Logic;
import com.badlogic.gdx.utils.Array;

/**
 * Created by danny on 10/22/14.
 */
public class Token  {
        public final Dungeon dungeon;
        private final int id;
        private final ModelId modelId;
        /**
         * the name of this character or item for the interfce
         */
        protected String name;
        /**
         * whether or not other tokens can stand on the same tile as this one. typically crates and characters block pathing, but pickups do not.
         */
        protected boolean blocksPathing = true;

        // state variables
        protected final Pair location = new Pair();                     // the current tile that this token is considered to be standing on
        protected FloorMap floorMap;
        protected Direction direction = Direction.South;          // the direction that this token is facing, this affects certain gameplay mechanics.
        private Array<TokenComponent> components = new Array<TokenComponent>(true, 8, TokenComponent.class);
        protected transient Listener listener;
        // Common Components
        private Logic logic;
        private Experience experience;
        private Command command;
        private Move move;
        private Damage damage;
        private Attack attack;
        private Inventory.Character inventory;
        private FogMapping fogMapping;

        public Token(Dungeon dungeon, int id, String name, ModelId modelId) {
                this.dungeon = dungeon;
                this.id = id;
                this.name = name;
                this.modelId = modelId;
        }

        public <T extends TokenComponent> T get(Class<T> componentClass) {
                for (TokenComponent component : components) {
                        if (componentClass.isAssignableFrom(component.getClass())) {
                                return (T) component;
                        }
                }
                return null;
        }

        public void add(TokenComponent component) {
                components.add(component);
                if (component instanceof Logic) {
                        logic = (Logic)component;
                } else if (component instanceof Damage) {
                        this.damage = (Damage) component;
                } else if (component instanceof FogMapping) {
                        this.fogMapping = (FogMapping) component;
                } else if (component instanceof Command) {
                        this.command = (Command) component;
                } else if (component instanceof Inventory.Character) {
                        this.inventory = (Inventory.Character) component;
                } else if (component instanceof Attack) {
                        this.attack = (Attack) component;
                } else if (component instanceof Move) {
                        this.move = (Move) component;
                }else if(component instanceof  Experience){
                        this.experience = (Experience) component;
                }
        }

        public boolean isValidTeleportLocation(FloorMap fm, int x, int y){
                Tile tile = fm.getTile(x,y);
                if(tile == null || tile.isDoor() || tile.isWall()){
                        return false;
                }
                return true;
        }

        public boolean teleport(FloorMap fm, int x, int y, Direction dir){

                if(!isValidTeleportLocation(fm,x,y)){
                     throw new AssertionError(getName()+"- not a valid teleport location, need to include a check for this earlier in the code");
                }

                floorMap = fm;
                location.set(x,y);
                direction = dir;

                for (TokenComponent c : components) {
                        c.teleport(fm, x, y, direction);
                }

                return true;
        }

        public void incremenetU(float delta) {
                for (int i = 0; i < components.size; i++) {
                        if (components.items[i].update(delta)) {
                                return; // this component consumed the rest of the update
                        }
                }
        }


        /**
         * DO NOT MODIFY
         *
         * @return
         */
        public Pair getLocation() {
                return location;
        }

        public boolean isLocatedAt(Pair loc) {
                return location.equals(loc);
        }

        public boolean isLocatedAt(int x, int y) {
                return location.x == x && location.y == y;
        }
/*
        public float getLocationFloatX() {
                return location.x;
        }

        public float getLocationFloatY() {
                return location.y;
        }*/

        public int getId() {
                return id;
        }

        public void setDirection(Direction direction) {
                this.direction = direction;
        }

        public Direction getDirection() {
                return direction;
        }

        public String getName() {
                return name;
        }

        public ModelId getModelId() {
                return modelId;
        }

        public Logic getLogic() {return logic;}

        public Command getCommand() {
                return command;
        }

        public Move getMove() {
                return move;
        }

        public Damage getDamage() {
                return damage;
        }

        public Attack getAttack() {
                return attack;
        }

        public Inventory.Character getInventory() {
                return inventory;
        }

        public FogMapping getFogMapping() {
                return fogMapping;
        }

        public Experience getExperience() {
                return experience;
        }

        /**
         * if the token prevents other tokens from sharing the same tile.
         *
         * @return
         */
        public boolean isBlocksPathing() {
                return blocksPathing;
        }

        public void setBlocksPathing(boolean blocksPathing) {
                this.blocksPathing = blocksPathing;
        }

        public FloorMap getFloorMap() {
                return floorMap;
        }

        public Listener getListener() {
                return listener;
        }

        public void setListener(Listener listener) {
                this.listener = listener;
        }

        public static interface Listener {

                public void onPathBlocked(Pair nextLocation, Tile nextTile);

                public void onAttack(Token target, Pair targetLocation, boolean ranged);

                public void onAttacked(Token attacker, Token target, Attack.AttackOutcome attackOutcome);

                public void onInventoryChanged();

                public void onUseItem(Item item, Inventory.Character.UseItemOutcome out);

                public void onStatusEffectChange(StatusEffects.Effect effect, float duration);

                /**
                 *
                 * @param journalObject can be PotionItem.Type, ScrollItem.Type, BookItem.Type, or EquipmentItem
                 * @param study if the object was learned via study or not, if not learned by study then it was learned by Tome of identification
                 */
                public void onLearned(Item journalObject, boolean study);


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
                return "Token{" +
                        "id=" + id +
                        ", name='" + name + '\'' +
                        ", location=" + location + "(floor " + floorMap.index + ")" +
                        ", direction=" + direction +
                        '}';
        }
}
