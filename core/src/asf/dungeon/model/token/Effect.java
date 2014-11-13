package asf.dungeon.model.token;

/**
* Created by Danny on 11/13/2014.
*/
public enum Effect {

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
