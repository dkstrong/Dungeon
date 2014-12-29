package asf.dungeon.model.token.quest;

import asf.dungeon.model.token.Interactor;

/**
 * Created by Daniel Strong on 12/1/2014.
 */
public class SignPostQuest extends Quest{

        private String signPostMessage;

        public SignPostQuest(String signPostMessage) {
                this.signPostMessage = signPostMessage;
        }

        @Override
        protected void makeDialouges() {
                dialouges = new Dialouge[1];

                dialouges[0] =  new Dialouge(){
                        @Override
                        public boolean testCondition(Interactor interactor) {
                                return true;
                        }

                        @Override
                        public String getMessage(Interactor interactor) {
                                return signPostMessage;
                        }

                        @Override
                        public Choice[] getChoices(Interactor interactor) {
                                Choice c0 = new Choice("Close");
                                return new Choice[]{c0};
                        }
                };
        }
}
