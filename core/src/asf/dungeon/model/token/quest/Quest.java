package asf.dungeon.model.token.quest;

import asf.dungeon.model.token.Interactor;
import asf.dungeon.model.token.TokenComponent;

/**
 * Created by Danny on 12/1/2014.
 */
public class Quest implements TokenComponent{
        // by not storing a reference to the Token this compononent is attached to and making it
        // immutable i can reuse the same quest on multiple tokens
        protected transient Dialouge[] dialouges;

        protected void makeDialouges(){

        }

        public Dialouge initialDialouge(Interactor interactor){
                if(dialouges == null) makeDialouges();
                for (Dialouge dialouge : dialouges) {
                        if(dialouge.testCondition(interactor)){
                                return dialouge;
                        }
                }
                return null;
        }

        public Dialouge makeChoice(Interactor interactor, Choice choice){
                Command c = choice.getCommand();
                if(c != null ) c.exec(interactor);
                return choice.getNextDialogue();
        }


        @Override
        public boolean update(float delta) {
                return false;
        }
}
