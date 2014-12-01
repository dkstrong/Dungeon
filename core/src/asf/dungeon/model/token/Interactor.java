package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/30/2014.
 */
public class Interactor implements TokenComponent{
        protected final Token token;
        public Token interactingWith;

        public Interactor(Token token) {
                this.token = token;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {
                interactingWith = null;
        }

        @Override
        public boolean update(float delta) {

                if(!isInteracting()) return false;

                int chatChoice = token.getCommand().getChatChoice();
                Gdx.app.log("Interactor","received chat choice: "+chatChoice);
                if(chatChoice >=0){
                        InteractChat chat = interactingWith.get(InteractChat.class);
                        boolean action = chat.makeChoice(this, chatChoice);
                        if(action){
                                if(token.listener != null)
                                        token.listener.onInteract(chat);
                                return true;
                        }else{
                                Gdx.app.log("Interactor","done interacting");
                                interactingWith = null;
                                token.getCommand().setLocation(token.getLocation());
                                return true;
                        }
                }

                return true;
        }

        protected boolean interact(Pair nextLocation) {
                Array<Token> tokensAt = token.floorMap.getTokensAt(nextLocation);
                for (Token t : tokensAt) {
                        Interact interact = t.get(Interact.class);
                        if(interact == null) continue;
                        boolean action = interact.interact(this);
                        if(action) {
                                interactingWith = t;
                                if(token.listener != null)
                                        token.listener.onInteract(interact);
                                return true;
                        }
                }
                return false;
        }

        public boolean isInteracting(){
                return interactingWith != null;
        }
}
