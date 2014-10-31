package asf.dungeon.board;

/**
 * Created by danny on 10/25/14.
 */
public class CrateToken extends DamageableToken {
        protected CrateToken(Dungeon dungeon, FloorMap floormMap,  int id, String name) {
                super(dungeon, floormMap,id, name, 1);
                this.setDeathDuration(2.5f);
                this.setDeathRemovalCountdown(.25f);

        }

        @Override
        protected void receiveDamageFrom(Token token) {
                applyDamage(1);
        }

        @Override
        protected void onDied() {
                // TODO: spawn item on this location
                dungeon.newLootToken(floorMap, "loot_health_potion", location.x, location.y);
        }
}
