package asf.dungeon.model.token.quest;

import asf.dungeon.model.ModelId;
import asf.dungeon.model.item.PotionItem;
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.model.token.Chat;
import asf.dungeon.model.token.Interactor;

/**
 * Created by Danny on 12/1/2014.
 */
public class PotionQuest extends Quest {
        public PotionQuest() {
                dialouges = new Dialouge[3];

                // player provided health potion to npc
                dialouges[1] = new Dialouge() {
                        @Override
                        public boolean testCondition(Interactor interactor, Chat chat) {
                                return (interactor.getChatProgress(chat) == 1 && !chat.token.getDamage().isDead());
                        }

                        @Override
                        public String getMessage(Interactor interactor) {
                                if(interactor.token.getInventory().isFull())
                                        return "I will gladly hold up my end of the bargain, return to me when you have room in your inventory.";
                                else
                                        return "As promised, here is the sword. Unfortunately I have not been able to identify it yet.";

                        }

                        @Override
                        public Choice[] getChoices(Interactor interactor) {
                                if(interactor.token.getInventory().isFull()){
                                        return new Choice[]{new Choice("I will return.")};
                                }else{
                                        Choice c0 =  new Choice("[Take Sword]");
                                        c0.setCommand(new Command() {
                                                @Override
                                                public void exec(Interactor interactor, Chat chat) {
                                                        WeaponItem weapon = new WeaponItem(ModelId.Sword, "Traveler's Sword", 3);
                                                        interactor.token.getInventory().add(weapon);
                                                        interactor.setChatProgress(chat, 3);
                                                }
                                        });
                                        return new Choice[]{c0};
                                }


                        }
                };

                // player provided poison to npc
                dialouges[2] = new Dialouge() {
                        @Override
                        public boolean testCondition(Interactor interactor, Chat chat) {
                                return(interactor.getChatProgress(chat) == 2 && !chat.token.getDamage().isDead());
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
                                        public boolean testCondition(Interactor interactor, Chat chat) {
                                                return true;
                                        }

                                        @Override
                                        public String getMessage(Interactor interactor) {
                                                return "I.. I think.. I think I just drank poison.";
                                        }

                                        @Override
                                        public Choice[] getChoices(Interactor interactor) {
                                                Choice c0 = new Choice(" ... ");
                                                c0.setCommand(new Command() {
                                                        @Override
                                                        public void exec(Interactor interactor, Chat chat) {
                                                                interactor.setChatProgress(chat, 3);
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
                        public boolean testCondition(Interactor interactor, Chat chat) {
                                return (interactor.getChatProgress(chat) == 0 && !chat.token.getDamage().isDead());
                        }

                        @Override
                        public String getMessage(Interactor interactor) {
                                return "Please help me. I am very low on health and do not have a health potion. I will trade you this sword if you could help me out.";
                        }

                        @Override
                        public Choice[] getChoices(Interactor interactor) {
                                final PotionItem health = interactor.token.getInventory().getPotionItem(PotionItem.Type.Health);
                                Choice c0;
                                if(health != null && health.isIdentified(interactor.token)){
                                        c0 = new Choice("[Give Health Potion]");
                                        c0.setCommand(new Command() {
                                                @Override
                                                public void exec(Interactor interactor, Chat chat) {
                                                        PotionItem givePotion;
                                                        if(health.getCharges() ==1){
                                                                interactor.token.getInventory().discard(health);
                                                                givePotion = health;
                                                        }else{
                                                                // TODO: there needs to be Inventory.unStackOrDiscard(health)
                                                                givePotion = health.unStack(1);
                                                                if(interactor.token.getListener() != null)
                                                                        interactor.token.getListener().onInventoryChanged();
                                                        }

                                                        chat.token.getInventory().add(givePotion);
                                                        chat.token.getCommand().consumeItem(givePotion);
                                                        interactor.setChatProgress(chat, 1);
                                                }
                                        });
                                        c0.setNextDialogue(dialouges[1]);
                                }else{
                                        c0 = null;
                                }

                                final PotionItem poison = interactor.token.getInventory().getPotionItem(PotionItem.Type.Poison);
                                Choice c1;
                                if(poison != null && poison.isIdentified(interactor.token)){
                                        c1 = new Choice("I don't have a Health Potion but I can give you this... [Give Poison Potion]");
                                        c1.setCommand(new Command() {
                                                @Override
                                                public void exec(Interactor interactor, Chat chat) {
                                                        PotionItem givePotion;
                                                        if(poison.getCharges() ==1){
                                                                interactor.token.getInventory().discard(poison);
                                                                givePotion = poison;
                                                        }else{
                                                                givePotion = poison.unStack(1);
                                                                if(interactor.token.getListener() != null)
                                                                        interactor.token.getListener().onInventoryChanged();
                                                        }


                                                        chat.token.getInventory().add(givePotion);

                                                        boolean chanceCursed = interactor.token.dungeon.rand.bool(.75f - (interactor.token.getExperience().getLuck()/100f));
                                                        WeaponItem weapon = new WeaponItem(ModelId.Sword, "Traveler's Sword", chanceCursed ? 1 : 3);
                                                        weapon.setCursed(chanceCursed);
                                                        chat.token.getInventory().add(weapon);

                                                        chat.token.getCommand().consumeItem(givePotion);


                                                        interactor.setChatProgress(chat, 2);
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
