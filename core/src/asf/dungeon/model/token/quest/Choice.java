package asf.dungeon.model.token.quest;

/**
 * Created by Danny on 12/1/2014.
 */
public class Choice {
        private Condition condition;
        private String text;
        private Command command;
        private Dialouge nextDialogue;

        public Choice(String text) {
                this.text = text;
        }

        protected void setCondition(Condition condition) {
                this.condition = condition;
        }

        protected void setText(String text) {
                this.text = text;
        }

        protected void setCommand(Command command) {
                this.command = command;
        }

        protected void setNextDialogue(Dialouge nextDialogue) {
                this.nextDialogue = nextDialogue;
        }

        public String getText() {
                return text;
        }

        public Dialouge getNextDialogue() {
                return nextDialogue;
        }

        public Command getCommand() {
                return command;
        }

        public Condition getCondition() {
                return condition;
        }
}
