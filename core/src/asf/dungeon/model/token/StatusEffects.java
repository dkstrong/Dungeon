package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ArrayReflection;

/**
 * Created by Danny on 11/11/2014.
 */
public class StatusEffects implements TokenComponent{
        private final Token token;
        private final Array<Float>[] statusEffects;

        //private final Map<Effect, Array<Float>> statusEffects = new EnumMap<Effect, Array<Float>>(Effect.class);

        public StatusEffects(Token token) {
                this.token = token;

                statusEffects = (Array<Float>[]) ArrayReflection.newInstance(Array.class, Effect.values.length);

                for (Effect statusEffect : Effect.values) {
                        statusEffects[statusEffect.ordinal()] = new Array<Float>(false, 8, Float.class);
                }
        }

        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                return true;
        }

        @Override
        public boolean update(float delta) {
                for (Effect statusEffect : Effect.values) {
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
                                        if(token.listener != null)
                                                token.listener.onStatusEffectChange(statusEffect, 0);
                                }
                        }
                }
                return false;
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

                System.out.println(durations);

                statusEffect.begin(token);
                if(token.listener != null)
                        token.listener.onStatusEffectChange(statusEffect, duration);
        }

        public void removeStatusEffect(Effect statusEffect){
                Array<Float> durations = statusEffects[statusEffect.ordinal()];
                durations.clear();
                statusEffect.end(token);
                if(token.listener != null)
                        token.listener.onStatusEffectChange(statusEffect, 0);
        }

        public void removeNegativeStatusEffects(){
                removeStatusEffect(Effect.Poison);
                removeStatusEffect(Effect.Paralyze);
        }

        public void removeAllStatusEffects(){
                for (Effect statusEffect : Effect.values) {
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

        /**
        * Created by Danny on 11/13/2014.
        */
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
                Paralyze(),
                Invisibility(),
                MindVision(),
                Speed();

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

                public static final Effect[] values = Effect.values();

        }
}
