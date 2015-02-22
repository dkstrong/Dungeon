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
public class Interactor implements TokenComponent, TeleportValidator {
        public final Token token;
        public transient Token chattingWith;
        private final ObjectIntMap<Token> chatProgress = new ObjectIntMap<Token>(1);

        public Interactor(Token token) {
                this.token = token;
        }

        @Override
        public boolean isGoodSpawnLocation(FloorMap fm, int x, int y, Direction dir) {
                return true;
        }

        @Override
        public boolean canTeleport(FloorMap fm, int x, int y, Direction direction){
                return chattingWith == null;
        }

        protected boolean interact(Pair nextLocation) {
                Array<Token> tokensAt = token.floorMap.getTokensAt(nextLocation);
                for (Token t : tokensAt) {
                        Quest quest = t.get(Quest.class);
                        if(quest == null) continue;
                        chattingWith = t;
                        Dialouge currentDialogue = quest.initialDialouge(this);
                        token.command.setLocation(token.location);
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

                Choice chatChoice = token.command.getChatChoice();
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
                                //token.command.setLocation(token.location);
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
