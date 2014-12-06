package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.fogmap.FogMap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ArrayReflection;

/**
 * Created by Danny on 11/11/2014.
 */
public class StatusEffects implements TokenComponent{
        private final Token token;
        private static transient final Effect[] effectValues = Effect.values();
        private final Array<Float>[] statusEffects;

        //private final Map<Effect, Array<Float>> statusEffects = new EnumMap<Effect, Array<Float>>(Effect.class);

        public StatusEffects(Token token) {
                this.token = token;

                statusEffects = (Array<Float>[]) ArrayReflection.newInstance(Array.class, effectValues.length);

                for (Effect statusEffect : effectValues) {
                        statusEffects[statusEffect.ordinal()] = new Array<Float>(false, 8, Float.class);
                }
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {

        }

        @Override
        public boolean update(float delta) {
                for (Effect statusEffect : effectValues) {
                        Array<Float> durations = statusEffects[statusEffect.ordinal()];
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

                if(hasStatusEffect(Effect.MindVision)){
                        FogMap fogMap = token.getFogMapping().getCurrentFogMap();
                        Array<Token> monsterTokens = token.getFloorMap().getMonsterTokens(token.getLogic().getTeam());
                        for (Token monsterToken : monsterTokens) {
                                fogMap.revealLocation(monsterToken.getLocation().x, monsterToken.getLocation().y);
                        }
                }

                if(hasStatusEffect(Effect.Paralyze))
                        return true;

                return false;
        }

        public void addStatusEffect(Effect statusEffect){
                // TODO: need to implement this
                // shoudl add status effect pemenatly, will not be removed until removeStatusEffect() is called
                throw new UnsupportedOperationException("not yet implemented");
        }
        public void addStatusEffect(Effect statusEffect, float duration){
                addStatusEffect(statusEffect,duration,1);
        }
        public void addStatusEffect(Effect statusEffect, float duration, int value){
                if(duration < this.getStatusEffectDuration(statusEffect)){
                        return;
                }

                Array<Float> durations = statusEffects[statusEffect.ordinal()];
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

                statusEffect.begin(token);
                if(token.getExperience() != null)
                        token.getExperience().recalcStats();
                if(token.listener != null)
                        token.listener.onStatusEffectChange(statusEffect, duration);
        }

        public void removeStatusEffect(Effect statusEffect){
                Array<Float> durations = statusEffects[statusEffect.ordinal()];
                durations.clear();
                statusEffect.end(token);
                token.getExperience().recalcStats();
                if(token.listener != null)
                        token.listener.onStatusEffectChange(statusEffect, 0);
        }

        public void removeNegativeStatusEffects(){
                removeStatusEffect(Effect.Poison);
                removeStatusEffect(Effect.Paralyze);
                removeStatusEffect(Effect.Blind);
        }

        public void removeAllStatusEffects(){
                for (Effect statusEffect : effectValues) {
                        removeStatusEffect(statusEffect);
                }
        }

        public boolean hasStatusEffect(Effect statusEffect){
                return statusEffects[statusEffect.ordinal()].size >0;
        }

        public float getStatusEffectDuration(Effect statusEffect){
                float duration = 0;
                Array<Float> durations = statusEffects[statusEffect.ordinal()];
                for (Float aFloat : durations) {
                        duration+=aFloat;
                }
                return duration;
        }

        public static enum Effect {

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
                Paralyze(), // StatusEffects.update() checks for this and blocks the stack if paralyzed
                Invisibility(), // Damage checks for this and sets not attackable is invisible
                MindVision(), // StatusEffects.update() checks for this, reveals tiles of monster tokens
                Blind(), // Experience.recalcStats() checks for this, redudes visibility to 1 tile
                Speed(); // Experience.recalcStats() checks for this and gives 35% speed increase

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
