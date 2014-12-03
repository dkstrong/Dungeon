package asf.dungeon.model.token.quest;

import asf.dungeon.model.token.Fountain;
import asf.dungeon.model.token.Interactor;

/**
 * Created by Danny on 12/1/2014.
 */
public class FountainQuest extends Quest{


        @Override
        protected void makeDialouges() {
                dialouges = new Dialouge[1];

                dialouges[0] =  new Dialouge(){
                        @Override
                        public boolean testCondition(Interactor interactor) {
                                Fountain fountain = interactor.chattingWith.get(Fountain.class);
                                if(fountain == null) throw new AssertionError("FountainQuest requires Fountain");
                                return  !fountain.isConsumed();
                        }

                        @Override
                        public String getMessage(Interactor interactor) {
                                Fountain fountain = interactor.chattingWith.get(Fountain.class);
                                String colorName = fountain.getFountainColor().name().toLowerCase();
                                return "A mysterious fountain filled with "+colorName+" liquid. It is unclear why it is here or what it does.";
                        }

                        @Override
                        public Choice[] getChoices(Interactor interactor) {
                                Choice c0 = new Choice("Drink from the fountain");
                                c0.setCommand(new Command(){
                                        @Override
                                        public void exec(Interactor interactor) {
                                                interactor.chattingWith.get(Fountain.class).consumeFountain(interactor.token);

                                        }
                                });

                                Choice c1 = new Choice("Too risky");

                                return new Choice[]{c0,c1};
                        }
                };



        }
}
