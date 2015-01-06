package asf.dungeon.model.token;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Daniel Strong on 12/19/2014.
 */
public class SpikeTrap implements TokenComponent{
        private Token token;
        private byte triggered;
        private transient Listener listener;

        public SpikeTrap(Token token) {
                this.token = token;
                token.blocksPathing = false;
        }

        @Override
        public boolean update(float delta) {

                return false;
        }


        protected void setHidden(){
                if(triggered ==0) return;
                triggered =0;
                if(listener!=null) listener.onSpikeTrapHidden();
        }

        protected void setTriggered(){
                if(triggered ==1) return;
                triggered = 1;
                if(listener!=null) listener.onSpikeTrapTriggered();

                Array<Token> victimTokens = token.floorMap.getTokens();
                for (int i = 0; i < victimTokens.size; i++) {
                        Token t = victimTokens.items[i];
                        if(t == token) continue;
                        if(t.damage == null) continue;
                        if(!t.isLocatedAt(token.location)) continue;
                        sendDamageToVictim(t);
                }

        }

        private void sendDamageToVictim(Token victimToken){
                Attack.AttackOutcome out = new Attack.AttackOutcome();

                if(victimToken.inventory != null){
                        victimToken.inventory.resetCombatTimer();
                }
                out.damage = 5;
                out.dodge = false;
                out.critical =false;
                victimToken.damage.setHitDuration(2, token);
                victimToken.damage.addHealth(-out.damage);
                if(victimToken.experience !=null && victimToken.listener != null) {
                        victimToken.listener.onAttacked(token, victimToken, out);
                }

        }

        protected void setDefused(){
                if(triggered ==-1) return;
                triggered = -1;
                if(listener!=null) listener.onSpikeTrapDefused();
        }

        public boolean isHidden() {return triggered == 0;}

        public boolean isTriggered() {
                return triggered==1;
        }

        public boolean isDefused() {
                return triggered==-1;
        }

        public Listener getListener() {
                return listener;
        }

        public void setListener(Listener listener) {
                this.listener = listener;
        }

        public interface Listener{
                public void onSpikeTrapHidden();
                public void onSpikeTrapTriggered();
                public void onSpikeTrapDefused();
        }

}
