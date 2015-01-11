package asf.dungeon.model.token;

/**
 * Created by Daniel Strong on 1/11/2015.
 */
public class MonsterTrap implements TokenComponent{
        public final Token token;
        private boolean triggered;
        private float triggerU = 1.45f;

        public MonsterTrap(Token token) {
                this.token = token;
        }

        @Override
        public boolean update(float delta) {
                if(triggered){
                        triggerU -= delta;
                }
                return triggerU > 0;
        }

        public void trigger(Token triggeredBy){
                triggered = true;
                token.direction = token.location.direction(triggeredBy.location);
                token.attack.restartWeaponCooldown();
        }

        public boolean isTriggered(){
                return triggered;
        }

        public boolean isAwake(){
                return triggerU <= 0;
        }

        public float getTriggerU() {
                return triggerU;
        }

}
