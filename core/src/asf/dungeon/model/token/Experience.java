package asf.dungeon.model.token;

import asf.dungeon.model.Direction;

/**
 * Created by Danny on 11/11/2014.
 */
public class Experience implements TokenComponent{
        private final Token token;
        private int xp;
        private int levelRating = 1;
        private int strengthRating = 1;
        private int speedRating = 1;
        private int defenseRating = 1;

        public Experience(Token token) {
                this.token = token;
        }

        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                return true;
        }

        @Override
        public boolean update(float delta) {
                return false;
        }

        public void setStats(int levelRating, int strengthRating, int speedRating, int defenseRating){
                this.setLevelRating(levelRating);
                this.setStrengthRating(strengthRating);
                this.setSpeedRating(speedRating);
                this.setDefenseRating(defenseRating);
        }

        public void setLevelRating(int levelRating) {
                this.levelRating = levelRating;
        }

        public int getLevelRating() {
                return levelRating;
        }

        public void setStrengthRating(int strengthRating) {

                this.strengthRating = strengthRating;

                // TODO: changing experience ratings should effect derrived stats
                //if (this.isAttacking())
                //        throw new IllegalStateException("can not change this value while attacking");
                //this.attackDuration = 2;


        }

        public int getStrengthRating() {
                return strengthRating;
        }

        public void setSpeedRating(int speedRating) {
                this.speedRating = speedRating;
                // TODO: changing experience ratings should effect derrived stats
                //moveSpeed = speedRating;
                //attackRange = Math.round(moveSpeed/2f);
                //attackCooldownDuration = moveSpeed/6f;

        }

        public int getSpeedRating() {
                return speedRating;
        }

        public void setDefenseRating(int defenseRating) {
                this.defenseRating = defenseRating;
        }

        public int getDefenseRating() {
                return defenseRating;
        }

        public int getXp() {
                return xp;
        }

        public void setXp(int xp) {
                this.xp = xp;
        }
}
