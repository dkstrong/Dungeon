package asf.dungeon.model.token.logic.fsm;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Sector;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.Logic;

/**
 * Created by Danny on 11/20/2014.
 */
public class FSMLogic implements Logic {
        protected Token token;
        protected int team;
        protected Sector sector;
        private State initialState;
        private State currentState;

        public FSMLogic(int team, Sector sector, State initialState) {
                this.team = team;
                this.sector = sector;
                this.initialState = initialState;
        }

        protected void setState(State state) {
                if (currentState != null) currentState.end(this, token, token.getCommand());
                currentState = state;
                currentState.begin(this, token, token.getCommand());
        }

        @Override
        public void setToken(Token token) {
                this.token = token;
        }

        @Override
        public int getTeam() {
                return team;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {

        }

        @Override
        public boolean update(float delta) {
                if (currentState == null) setState(initialState);
                currentState.update(this, token, token.getCommand());
                return false;
        }
}
