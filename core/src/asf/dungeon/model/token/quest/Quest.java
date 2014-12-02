package asf.dungeon.model.token.quest;

import asf.dungeon.model.token.Chat;
import asf.dungeon.model.token.Interactor;

/**
 * Created by Danny on 12/1/2014.
 */
public class  Quest {
        protected Dialouge[] dialouges;

        public Dialouge initialDialouge(Interactor interactor, Chat chat){
                for (Dialouge dialouge : dialouges) {
                        if(dialouge.testCondition(interactor, chat)){
                                return dialouge;
                        }
                }
                return null;
        }

        public Dialouge makeChoice(Interactor interactor, Chat chat, Dialouge dialouge, Choice choice){
                Command c = choice.getCommand();
                if(c != null ) c.exec(interactor, chat);
                return choice.getNextDialogue();
        }


}
