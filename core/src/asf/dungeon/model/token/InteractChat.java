package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;

/**
 * Created by Danny on 11/30/2014.
 */
public class InteractChat implements Interact {
        private String message;
        private String[] choices;


        @Override
        public boolean interact(Interactor interactor) {
                return true;
        }

        public boolean makeChoice(Interactor interactor, int choiceIndex){
                return false;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {

        }

        @Override
        public boolean update(float delta) {
                return false;
        }

        public String getMessage(Interactor interactor) {
                return message;
        }

        public String[] getChoices(Interactor interactor) {
                return choices;
        }

        public void setMessage(String message) {
                this.message = message;
        }

        public void setChoices(String[] choices) {
                this.choices = choices;
        }



}
