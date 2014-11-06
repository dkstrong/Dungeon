package asf.dungeon.model;

/**
 * Created by Danny on 11/6/2014.
 */
public enum StatusEffect{

        Heal(){
                @Override
                protected void apply(CharacterToken token) {

                        token.addHealth(1);
                }
        },
        Poison(){
                @Override
                protected void apply(CharacterToken token) {
                        token.addHealth(-1);
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
        protected void begin(CharacterToken token){

        }

        /**
         * called on each interval
         * @param token
         */
        protected void apply(CharacterToken token){

        }

        /**
         * called when the status effect is removed
         * @param token
         */
        protected void end(CharacterToken token){

        }

        public static final StatusEffect[] values = StatusEffect.values();

}
