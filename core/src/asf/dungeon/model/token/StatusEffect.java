package asf.dungeon.model.token;

/**
* Created by Daniel Strong on 12/9/2014.
*/
public enum StatusEffect {

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
                        token.getStatusEffects().remove(StatusEffect.Burning);
                }
        },
        Burning(){
                @Override
                protected void begin(Token token) {
                        token.getStatusEffects().remove(StatusEffect.Frozen);
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
        Confused(), // Monster FSM checks for this
        ScaresMonsters(), // TODO: Monster FSM checks for this, runs from this token's location if nearby
        LuresMonsters(); // Monster.Sleep and Monster.Explore will be lured to this token's location
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
