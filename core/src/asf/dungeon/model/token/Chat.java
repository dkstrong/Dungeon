package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.token.quest.Choice;
import asf.dungeon.model.token.quest.Dialouge;
import asf.dungeon.model.token.quest.Quest;

/**
 * TODO: instead of having this Chat Token Compnent, the Quest itself should be the token componnet
 * Created by Danny on 11/30/2014.
 */
@Deprecated
public class Chat implements TokenComponent {

        public final Token token;
        private Quest quest;

        public Chat(Token token) {
                this.token = token;
        }

        public Dialouge initiateChat(Interactor interactor) {
                return quest.initialDialouge(interactor, this);
        }

        public Dialouge makeChoice(Interactor interactor, Dialouge dialouge, Choice choice){
                return quest.makeChoice(interactor, this, dialouge, choice);
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {

        }

        @Override
        public boolean update(float delta) {
                return false;
        }

        public void setQuest(Quest quest){
                this.quest = quest;
        }





}
