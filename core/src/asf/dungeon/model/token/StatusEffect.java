package asf.dungeon.model.token;

/**
* Created by Daniel Strong on 12/9/2014.
*/
public enum StatusEffect {

        Heal(){
                @Override
                protected void begin(Token token) {
                        token.statusEffects.remove(StatusEffect.Poison);
                }

                @Override
                protected void apply(Token token) {
                        token.damage.addHealth(1);
                }
        },
        Poison(){
                @Override
                protected void begin(Token token) {
                        token.statusEffects.remove(StatusEffect.Heal);
                        token.statusEffects.remove(StatusEffect.Paralyze);

                }

                @Override
                protected void apply(Token token) {
                        token.damage.addHealth(-1);
                }
        },
        Frozen(){
                // StatusEffects.update() checks for this and blocks the stack if paralyzed,
                // Attack.sendDamageToAttackTarget also checks for this and increases damage received
                @Override
                protected void begin(Token token) {
                        token.statusEffects.remove(StatusEffect.Burning);
                        token.statusEffects.remove(StatusEffect.Invisibility);
                        token.statusEffects.remove(StatusEffect.Speed);
                }
        },
        Burning(){
                @Override
                protected void begin(Token token) {
                        token.statusEffects.remove(StatusEffect.Frozen);
                        token.statusEffects.remove(StatusEffect.Invisibility);
                        token.statusEffects.remove(StatusEffect.Speed);
                }
                @Override
                protected void apply(Token token) {
                        token.damage.addHealth(-1);
                }
        },
        Paralyze(){
                // StatusEffects.update() checks for this and blocks the stack if paralyzed, Inventory

                @Override
                protected void begin(Token token) {
                        token.statusEffects.remove(StatusEffect.Invisibility);
                        token.statusEffects.remove(StatusEffect.Speed);
                        token.statusEffects.remove(StatusEffect.Poison);
                        token.statusEffects.remove(StatusEffect.MindVision);
                        token.statusEffects.remove(StatusEffect.ItemVision);
                }
        },
        Invisibility(), // Damage checks for this and sets not attackable is invisible
        MindVision(){
                // StatusEffects.update() checks for this, reveals tiles of monster tokens
                @Override
                protected void begin(Token token) {
                        token.statusEffects.remove(StatusEffect.Confused);
                        token.statusEffects.remove(StatusEffect.Blind);
                }
        },
        ItemVision(){
                // StatusEffects.update() checks for tihs, reveals tiles of crates and loot
                @Override
                protected void begin(Token token) {
                        token.statusEffects.remove(StatusEffect.Confused);
                        token.statusEffects.remove(StatusEffect.Blind);
                }
        },
        Blind(){
                // Experience.recalcStats() checks for this, redudes visibility to 1 tile
                @Override
                protected void begin(Token token) {
                        token.statusEffects.remove(StatusEffect.MindVision);
                        token.statusEffects.remove(StatusEffect.ItemVision);
                }
        },
        Might(), // Attack.sendDamageToAttackTarget() checks for this, increases damage dealt and reduces damage received
        Speed(){
                // Experience.recalcStats() checks for this and gives 35% speed increase
                @Override
                protected void begin(Token token) {
                        token.statusEffects.remove(StatusEffect.Paralyze);
                }
        },
        Confused(){
                // Monster FSM checks for this

                @Override
                protected void begin(Token token) {
                        token.statusEffects.remove(StatusEffect.MindVision);
                        token.statusEffects.remove(StatusEffect.ItemVision);
                }
        },
        ScaresMonsters(){
                // TODO: Monster FSM checks for this, runs from this token's location if nearby

                @Override
                protected void begin(Token token) {
                        token.statusEffects.remove(StatusEffect.LuresMonsters);
                }
        },
        LuresMonsters(){
                // Monster.Sleep and Monster.Explore will be lured to this token's location

                @Override
                protected void begin(Token token) {
                        token.statusEffects.remove(StatusEffect.ScaresMonsters);
                }
        };
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
