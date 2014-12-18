package asf.dungeon.model.token.quest;

import asf.dungeon.model.token.Interactor;

/**
 * Created by Danny on 12/1/2014.
 */
public class TorchQuest extends Quest{


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
                                Torch torch = interactor.chattingWith.get(Torch.class);
                                if(torch.isIgnited()){
                                        return "This unusual torch burns brightly.";
                                }else{
                                        return "An unlit torch.";
                                }

                        }

                        @Override
                        public Choice[] getChoices(Interactor interactor) {
                                Torch torch = interactor.chattingWith.get(Torch.class);
                                Choice c0 = new Choice(torch.isIgnited() ? "Extinguish the torch" : "Light the torch");
                                c0.setCommand(new Command(){
                                        @Override
                                        public void exec(Interactor interactor) {
                                                interactor.chattingWith.get(Torch.class).toggleIgnited();
                                        }
                                });
                                Choice c1 = new Choice("Leave it");
                                return new Choice[]{c0,c1};
                        }
                };



        }
}
