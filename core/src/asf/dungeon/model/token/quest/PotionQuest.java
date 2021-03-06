package asf.dungeon.model.token.quest;

import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.model.token.Interactor;
import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 12/1/2014.
 */
public class PotionQuest extends Quest {

        private WeaponItem makeSword(Interactor interactor, boolean uncursed){
                if(uncursed){
                        WeaponItem weapon = new WeaponItem(interactor.token.dungeon, 3,2,1);
                        weapon.name = "Traveler's Sword";
                        weapon.vagueDescription = "A mysterious sword offered to you by a lost traveler. Who knows what it may do once equipped?";

                        return weapon;
                }else{
                        boolean chanceCursed = interactor.token.dungeon.rand.bool(.75f - (interactor.token.experience.getLuck()/100f));
                        WeaponItem weapon = new WeaponItem(interactor.token.dungeon, chanceCursed ? 1 : 3,2,1);
                        weapon.name = "Traveler's Sword";
                        weapon.vagueDescription = "A mysterious sword offered to you by a lost traveler. Who knows what it may do once equipped?";
                        weapon.cursed = chanceCursed;
                        return weapon;
                }
        }
        @Override
        protected void makeDialouges() {
                dialouges = new Dialouge[3];

                // player provided health potion to npc
                dialouges[1] = new Dialouge() {
                        @Override
                        public boolean testCondition(Interactor interactor) {
                                Token chattingWith = interactor.chattingWith;
                                return (interactor.getChatProgress(chattingWith) == 1 && !chattingWith.damage.isDead());
                        }

                        @Override
                        public String getMessage(Interactor interactor) {
                                if(interactor.token.inventory.isFull())
                                        return "I will gladly hold up my end of the bargain, return to me when you have room in your inventory.";
                                else
                                        return "As promised, here is the sword. Unfortunately I have not been able to identify it yet.";

                        }

                        @Override
                        public Choice[] getChoices(Interactor interactor) {
                                if(interactor.token.inventory.isFull()){
                                        return new Choice[]{new Choice("I will return.")};
                                }else{
                                        Choice c0 =  new Choice("[Take Sword]");
                                        c0.setCommand(new Command() {
                                                @Override
                                                public void exec(Interactor interactor) {
                                                        interactor.token.inventory.add(makeSword(interactor, true));
                                                        interactor.setChatProgress(interactor.chattingWith, 3);
                                                }
                                        });
                                        return new Choice[]{c0};
                                }


                        }
                };

                // player provided poison to npc
                dialouges[2] = new Dialouge() {
                        @Override
                        public boolean testCondition(Interactor interactor) {
                                return(interactor.getChatProgress(interactor.chattingWith) == 2 && !interactor.chattingWith.damage.isDead());
                        }

                        @Override
                        public String getMessage(Interactor interactor) {
                                String poisonColor = interactor.token.dungeon.getMasterJournal().getPotionColor(PotionItem.Type.Poison).name().toLowerCase();
                                return "What is this "+poisonColor+" colored potion? I've not seen this before..";
                        }

                        @Override
                        public Choice[] getChoices(Interactor interactor) {
                                Choice c0 = new Choice(" ... ");
                                c0.setNextDialogue(new Dialouge() {
                                        @Override
                                        public boolean testCondition(Interactor interactor) {
                                                return true;
                                        }

                                        @Override
                                        public String getMessage(Interactor interactor) {
                                                return "I think... I think I just drank poison.";
                                        }

                                        @Override
                                        public Choice[] getChoices(Interactor interactor) {
                                                Choice c0 = new Choice(" ... ");
                                                c0.setCommand(new Command() {
                                                        @Override
                                                        public void exec(Interactor interactor) {
                                                                interactor.setChatProgress(interactor.chattingWith, 3);
                                                        }
                                                });
                                                return new Choice[]{c0};
                                        }
                                });

                                return new Choice[]{c0};
                        }
                };

                // initial dialouge
                dialouges[0] =  new Dialouge(){
                        @Override
                        public boolean testCondition(Interactor interactor) {
                                if(interactor.token.inventory != null && !interactor.token.inventory.canChangeEquipment()) return false;
                                return (interactor.getChatProgress(interactor.chattingWith) == 0 && !interactor.chattingWith.damage.isDead());
                        }

                        @Override
                        public String getMessage(Interactor interactor) {
                                return "Please help me. I am very low on health and do not have a health potion. I will trade you this sword if you could help me out.";
                        }

                        @Override
                        public Choice[] getChoices(Interactor interactor) {
                                final PotionItem health = interactor.token.inventory.getPotionItem(PotionItem.Type.Health);
                                Choice c0;
                                if(health != null && health.isIdentified(interactor.token)){
                                        c0 = new Choice("[Give Health Potion]");
                                        c0.setCommand(new Command() {
                                                @Override
                                                public void exec(Interactor interactor) {
                                                        PotionItem givePotion = interactor.token.inventory.discardOrUnstack(health, 1);
                                                        if(givePotion == null) throw new AssertionError("potion should now be null");
                                                        interactor.chattingWith.inventory.add(givePotion);
                                                        interactor.chattingWith.command.consumeItem(givePotion);
                                                        interactor.setChatProgress(interactor.chattingWith, 1);
                                                }
                                        });
                                        c0.setNextDialogue(dialouges[1]);
                                }else{
                                        c0 = null;
                                }

                                final PotionItem poison = interactor.token.inventory.getPotionItem(PotionItem.Type.Poison);
                                Choice c1;
                                if(poison != null && poison.isIdentified(interactor.token)){
                                        c1 = new Choice("I don't have a Health Potion but I can give you this... [Give Poison Potion]");
                                        c1.setCommand(new Command() {
                                                @Override
                                                public void exec(Interactor interactor) {
                                                        PotionItem givePotion = interactor.token.inventory.discardOrUnstack(poison, 1);
                                                        if(givePotion == null) throw new AssertionError("potion should now be null");
                                                        interactor.chattingWith.inventory.add(givePotion);

                                                        WeaponItem weapon = makeSword(interactor, false);
                                                        interactor.chattingWith.inventory.add(weapon);
                                                        interactor.chattingWith.inventory.setItemToDrop(weapon);

                                                        interactor.chattingWith.command.consumeItem(givePotion);


                                                        interactor.setChatProgress(interactor.chattingWith, 2);
                                                }
                                        });
                                        c1.setNextDialogue(dialouges[2]);
                                }else{
                                        c1 = null;
                                }

                                Choice c2 = new Choice("I'm sorry but I can not help you.");

                                return new Choice[]{c0,c1, c2};
                        }
                };
        }
}
