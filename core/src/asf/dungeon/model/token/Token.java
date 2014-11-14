package asf.dungeon.model.token;


import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Item;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.token.logic.LogicProvider;
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
        private Target target;
        private Move move;
        private Damage damage;
        private Attack attack;
        private Inventory inventory;
        private FogMapping fogMapping;

        public Token(Dungeon dungeon, FloorMap floorMap, int id, String name, ModelId modelId) {
                this.dungeon = dungeon;
                this.floorMap = floorMap;
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
                if (component instanceof LogicProvider) {
                        LogicProvider lp = (LogicProvider)component;
                        lp.setToken(this);
                } else if (component instanceof Damage) {
                        this.damage = (Damage) component;
                } else if (component instanceof FogMapping) {
                        this.fogMapping = (FogMapping) component;
                } else if (component instanceof Target) {
                        this.target = (Target) component;
                } else if (component instanceof Inventory) {
                        this.inventory = (Inventory) component;
                } else if (component instanceof Attack) {
                        this.attack = (Attack) component;
                } else if (component instanceof Move) {
                        this.move = (Move) component;
                }
        }

        public final boolean teleportToLocation(int x, int y) {
                return teleportToLocation(x, y, Direction.South);
        }

        /**
         * teleports token to this new location, if the new location
         * can not be moved to (normally because it is already occupied) then the move will not work.
         * <p/>
         * this generally is used for setting up the dungeon, could eventually be used for stairways to lead to other rooms.
         *
         * @param x
         * @param y
         * @param direction
         * @return true if moved, false if did not move
         */
        public boolean teleportToLocation(int x, int y, Direction direction) {

                if (blocksPathing && floorMap.isLocationBlocked(x, y)) {
                        return false;
                }

                Tile tile = floorMap.tiles[x][y];
                if (tile.isDoor()) {
                        return false;
                }
                location.set(x, y);
                this.direction = direction;

                for (TokenComponent c : components) {
                        if (!c.teleportToLocation(x, y, direction))
                                throw new AssertionError("could not teleport due to error in "+c.getClass());
                }

                return true;
        }

        public boolean teleportToFloor(int f) {
                if (f == floorMap.index)
                        return false;
                boolean down = f > floorMap.index;
                floorMap = dungeon.generateFloor(f);
                Pair stairLoc;
                if (down) {
                        stairLoc = floorMap.getLocationOfUpStairs();
                } else {
                        stairLoc = floorMap.getLocationOfDownStairs();
                }

                boolean valid = teleportToLocation(stairLoc.x, stairLoc.y, Direction.North);
                if (!valid) {
                        // TODO: if there is something in the way of teleporting here (eg a character standing on the up stairs), then teleportToLocation()
                        // i need a way to handle this
                        throw new AssertionError("It seems I coudlnt teleport to the location of the stairs");
                }
                dungeon.moveTokenToFloor(this, floorMap);

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

        public Target getTarget() {
                return target;
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

        public Inventory getInventory() {
                return inventory;
        }

        public FogMapping getFogMapping() {
                return fogMapping;
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

                public void onAttack(Token target, boolean ranged);

                public void onAttacked(Token attacker, Token target, int damage, boolean dodge);

                public void onInventoryAdd(Item item);

                public void onInventoryRemove(Item item);

                public void onConsumeItem(Item.Consumable item);

                public void onStatusEffectChange(StatusEffects.Effect effect, float duration);


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
