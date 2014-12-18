package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;
import asf.dungeon.model.token.quest.Choice;
import asf.dungeon.model.token.quest.Dialouge;
import asf.dungeon.model.token.quest.Quest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;

/**
 * Created by Danny on 11/30/2014.
 */
public class Interactor implements TokenComponent{
        public final Token token;
        public transient Token chattingWith;
        private final ObjectIntMap<Token> chatProgress = new ObjectIntMap<Token>(1);

        public Interactor(Token token) {
                this.token = token;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {
                // TODO: if chattingWith, then teleport should fail
        }

        protected boolean interact(Pair nextLocation) {
                Array<Token> tokensAt = token.floorMap.getTokensAt(nextLocation);
                for (Token t : tokensAt) {
                        Quest quest = t.get(Quest.class);
                        if(quest == null) continue;
                        chattingWith = t;
                        Dialouge currentDialogue = quest.initialDialouge(this);
                        token.getCommand().setLocation(token.getLocation());
                        if(currentDialogue != null) {
                                if(token.listener != null)
                                        token.listener.onInteract(quest, currentDialogue);
                                return true;
                        }else{
                                chattingWith = null;
                        }
                }
                return false;
        }

        @Override
        public boolean update(float delta) {

                if(!isInteracting()) return false;

                Choice chatChoice = token.getCommand().getChatChoice();
                //Gdx.app.log("Interactor","received chat choice: "+chatChoice);
                if(chatChoice !=null){
                        Quest quest = chattingWith.get(Quest.class);
                        Dialouge currentDialogue = quest.makeChoice(this, chatChoice);
                        if(currentDialogue != null){
                                if(token.listener != null)
                                        token.listener.onInteract(quest, currentDialogue);
                                return true;
                        }else{
                                //Gdx.app.log("Interactor","done interacting with "+chattingWith.getName());
                                chattingWith = null;
                                //token.getCommand().setLocation(token.getLocation());
                                return true;
                        }
                }

                return true;
        }



        public boolean isInteracting(){
                return chattingWith != null;
        }

        public int getChatProgress(Token chattingWith){
                return chatProgress.get(chattingWith, 0);
        }

        public void setChatProgress(Token chattingWith, int progress){
                chatProgress.put(chattingWith, progress);
        }


}
