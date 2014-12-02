package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;
import asf.dungeon.model.token.quest.Choice;
import asf.dungeon.model.token.quest.Dialouge;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;

/**
 * Created by Danny on 11/30/2014.
 */
public class Interactor implements TokenComponent{
        public final Token token;
        public Token chattingWith;
        private Dialouge  currentDialogue;
        private final ObjectIntMap<Chat> chatProgress = new ObjectIntMap<Chat>(1);


        public Interactor(Token token) {
                this.token = token;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {
                chattingWith = null;
        }

        @Override
        public boolean update(float delta) {

                if(!isInteracting()) return false;

                Choice chatChoice = token.getCommand().getChatChoice();
                Gdx.app.log("Interactor","received chat choice: "+chatChoice);
                if(chatChoice !=null){
                        Chat chat = chattingWith.get(Chat.class);
                        currentDialogue = chat.makeChoice(this, currentDialogue, chatChoice);
                        if(currentDialogue != null){
                                if(token.listener != null)
                                        token.listener.onInteract(chat, currentDialogue);
                                return true;
                        }else{
                                Gdx.app.log("Interactor","done interacting");
                                chattingWith = null;
                                //token.getCommand().setLocation(token.getLocation());
                                return true;
                        }
                }

                return true;
        }

        protected boolean interact(Pair nextLocation) {
                Array<Token> tokensAt = token.floorMap.getTokensAt(nextLocation);
                for (Token t : tokensAt) {
                        Chat chat = t.get(Chat.class);
                        if(chat == null) continue;
                        currentDialogue = chat.initiateChat(this);
                        System.out.println("currentDialogue: "+ currentDialogue);
                        token.getCommand().setLocation(token.getLocation());
                        if(currentDialogue != null) {
                                chattingWith = t;
                                if(token.listener != null)
                                        token.listener.onInteract(chat, currentDialogue);
                                return true;
                        }
                }
                return false;
        }

        public boolean isInteracting(){
                return chattingWith != null;
        }

        public int getChatProgress(Chat chat){
                return chatProgress.get(chat, 0);
        }

        public void setChatProgress(Chat chat, int progress){
                chatProgress.put(chat, progress);
        }


}
