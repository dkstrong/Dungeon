package asf.dungeon.model.token.quest;

import asf.dungeon.model.token.Chat;
import asf.dungeon.model.token.Interactor;

/**
 * Created by Danny on 12/1/2014.
 */
public interface Dialouge {

        public boolean testCondition(Interactor interactor, Chat chat);

        public String getMessage(Interactor interactor);

        public Choice[] getChoices(Interactor interactor);
}
