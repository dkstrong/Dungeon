package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.fogmap.FogMap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;

/**
 * Created by Danny on 11/11/2014.
 */
public class StatusEffects implements TokenComponent{
        private final Token token;
        public static transient final StatusEffect[] effectValues = StatusEffect.values();
        private final FloatArray[] statusEffects;


        public StatusEffects(Token token) {
                this.token = token;

                statusEffects = new FloatArray[effectValues.length];

                for (StatusEffect statusEffect : effectValues) {
                        statusEffects[statusEffect.ordinal()] = new FloatArray(false, 8);
                }
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {

        }

        @Override
        public boolean update(float delta) {
                for (StatusEffect statusEffect : effectValues) {
                        FloatArray durations = statusEffects[statusEffect.ordinal()];
                        if(durations.size ==0){
                                continue;
                        }
                        durations.items[0] -= delta;
                        if(durations.items[0] <=0){
                                statusEffect.apply(token);
                                if(durations.size>1){
                                        durations.items[1] += durations.items[0]; // carry over the remaining duration to the next on the list
                                }
                                durations.removeIndex(0);

                                if(durations.size ==0){
                                        statusEffect.end(token);
                                        if(token.getExperience() != null)
                                                token.getExperience().recalcStats();
                                        if(token.listener != null)
                                                token.listener.onStatusEffectChange(statusEffect, 0);
                                }
                        }

                }

                if(hasStatusEffect(StatusEffect.MindVision)){
                        FogMap fogMap = token.getFogMapping().getCurrentFogMap();
                        Array<Token> monsterTokens = token.getFloorMap().getMonsterTokens(token.getLogic().getTeam());
                        for (Token monsterToken : monsterTokens) {
                                fogMap.revealLocation(monsterToken.getLocation().x, monsterToken.getLocation().y);
                        }
                }

                if(hasStatusEffect(StatusEffect.ItemVision)){
                        // TODO: and loot tiles?
                        FogMap fogMap = token.getFogMapping().getCurrentFogMap();
                        Array<Token> crateAndLootTokens = token.getFloorMap().getCrateTokens();
                        for (Token t : crateAndLootTokens) {
                                fogMap.revealLocationWithMagic(t.getLocation().x, t.getLocation().y);
                        }
                }

                if(hasStatusEffect(StatusEffect.Paralyze) || hasStatusEffect(StatusEffect.Frozen))
                        return true;

                return false;
        }

        /**
         * adds a status effect with no duration (NaN), this means it wont be removed until removeStatusEffect is called.
         *
         * note that status effects without durations will never call apply(), this means that Health and Poison wont do anything
         * if added this way
         * @param statusEffect
         */
        public void addStatusEffect(StatusEffect statusEffect){
                FloatArray durations = statusEffects[statusEffect.ordinal()];
                durations.clear();
                durations.add(Float.NaN);
                // following the logic from the bottom of addStatusEffect(Effect,float,int)

                if(token.getExperience() != null)
                        token.getExperience().recalcStats();
                if(token.listener != null)
                        token.listener.onStatusEffectChange(statusEffect, Float.NaN);
                statusEffect.begin(token);
        }
        public void addStatusEffect(StatusEffect statusEffect, float duration){
                addStatusEffect(statusEffect,duration,1);
        }
        public void addStatusEffect(StatusEffect statusEffect, float duration, int value){
                float currentDuration = getStatusEffectDuration(statusEffect);
                if(duration < currentDuration || Float.isNaN(currentDuration)){
                        return;
                }

                FloatArray durations = statusEffects[statusEffect.ordinal()];
                durations.clear();
                if(value >1){
                        float subDuration = duration/(value-1);
                        durations.add(0f);
                        for(int i=1; i<value; i++){
                                durations.add(subDuration);
                        }

                }else{
                        durations.add(duration);
                }


                if(token.getExperience() != null)
                        token.getExperience().recalcStats();
                if(token.listener != null)
                        token.listener.onStatusEffectChange(statusEffect, duration);
                statusEffect.begin(token);
        }

        public void removeStatusEffect(StatusEffect statusEffect){
                FloatArray durations = statusEffects[statusEffect.ordinal()];
                durations.clear();
                statusEffect.end(token);
                if(token.getExperience() != null)
                        token.getExperience().recalcStats();
                if(token.listener != null)
                        token.listener.onStatusEffectChange(statusEffect, 0);
        }

        public void removeNegativeStatusEffects(){
                removeStatusEffect(StatusEffect.Poison);
                removeStatusEffect(StatusEffect.Paralyze);
                removeStatusEffect(StatusEffect.Blind);
        }

        public void removeAllStatusEffects(){
                for (StatusEffect statusEffect : effectValues) {
                        removeStatusEffect(statusEffect);
                }
        }

        public boolean hasStatusEffect(StatusEffect statusEffect){
                return statusEffects[statusEffect.ordinal()].size >0;
        }

        public float getStatusEffectDuration(StatusEffect statusEffect){
                float duration = 0;
                FloatArray durations = statusEffects[statusEffect.ordinal()];
                for(int i=0; i<durations.size; i++){
                        duration+=durations.items[i];
                }
                return duration;
        }

        public static enum StatusEffect {

                Heal(){
                        @Override
                        protected void apply(Token token) {
                                token.getDamage().addHealth(1);
                        }
                },
                Poison(){
                        @Override
                        protected void apply(Token token) {
                                token.getDamage().addHealth(-1);
                        }
                },
                Frozen(){
                        // StatusEffects.update() checks for this and blocks the stack if paralyzed,
                        // Attack.sendDamageToAttackTarget also checks for this and increases damage received
                        @Override
                        protected void begin(Token token) {
                                token.getStatusEffects().removeStatusEffect(StatusEffect.Burning);
                        }
                },
                Burning(){
                        @Override
                        protected void begin(Token token) {
                                token.getStatusEffects().removeStatusEffect(StatusEffect.Frozen);
                        }
                        @Override
                        protected void apply(Token token) {
                                token.getDamage().addHealth(-1);
                        }
                },
                Paralyze(), // StatusEffects.update() checks for this and blocks the stack if paralyzed, Inventory
                Invisibility(), // Damage checks for this and sets not attackable is invisible
                MindVision(), // StatusEffects.update() checks for this, reveals tiles of monster tokens
                ItemVision(), // StatusEffects.update() checks for tihs, reveals tiles of crates and loot
                Blind(), // Experience.recalcStats() checks for this, redudes visibility to 1 tile
                Might(), // Attack.sendDamageToAttackTarget() checks for this, increases damage dealt and reduces damage received
                Speed(), // Experience.recalcStats() checks for this and gives 35% speed increase
                Confused(),// TODO: Monster FSM checks for this, changes the fsm team to a random team number, if logic is local player then i need to come up with something else to do.
                ScaresMonsters(), // TODO: Monster FSM checks for this, runs from this token's location if nearby
                LuresMonsters(); // TODO: Monster FSM checks for this, moves towards tokens location if within some large range
                /**
                 * called when the status effect is added
                 * @param token
                 */
                protected void begin(Token token){

                }

                /**
                 * called on each interval
                 * @param token
                 */
                protected void apply(Token token){

                }

                /**
                 * called when the status effect is removed
                 * @param token
                 */
                protected void end(Token token){

                }

        }
}
