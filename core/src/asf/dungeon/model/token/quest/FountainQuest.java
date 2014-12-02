package asf.dungeon.model.token.quest;

import asf.dungeon.model.token.Fountain;
import asf.dungeon.model.token.Interactor;
import asf.dungeon.model.token.StatusEffects;

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
                                return "A mysterious fountain filled with blue liquid. It is unclear why it is here or what it does.";
                        }

                        @Override
                        public Choice[] getChoices(Interactor interactor) {
                                Choice c0 = new Choice("Drink from the fountain");
                                c0.setCommand(new Command(){
                                        @Override
                                        public void exec(Interactor interactor) {
                                                interactor.token.get(StatusEffects.class).addStatusEffect(StatusEffects.Effect.Heal,5, 5);
                                                interactor.chattingWith.get(Fountain.class).setConsumed(true);

                                        }
                                });

                                Choice c1 = new Choice("Too risky");

                                return new Choice[]{c0,c1};
                        }
                };



        }
}
