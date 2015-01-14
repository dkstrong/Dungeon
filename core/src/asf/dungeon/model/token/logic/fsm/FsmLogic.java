package asf.dungeon.model.token.logic.fsm;

import asf.dungeon.model.Direction;
import asf.dungeon.model.DungeonRand;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Sector;
import asf.dungeon.model.token.Teleportable;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.Logic;

/**
 * Created by Danny on 11/20/2014.
 */
public class FsmLogic implements Logic, Teleportable {
        private State initialState;
        protected Token token;
        protected DungeonRand rand;
        /**
         * the team that this agent beleongs to, agents do not attack agents on the same team,
         * but are hostile to all other teams.
         */
        protected int team;
        /**
         * the home sector of this agent (if it has one)
         */
        protected Sector sector;
        /**
         * a float value that is reusable for States to use
         */
        protected float count;
        /**
         * contains the general target token that is being targetted by the state machine, will be force
         * set to null when teleportation happens because teleportation should kill targetting.
         */
        protected Token target;
        /**
         * temp storage/reusable pair for States to use
         */
        protected final Pair pair = new Pair();
        private State currentState;

        public FsmLogic(int team, Sector sector, State initialState) {
                this.team = team;
                this.sector = sector;
                this.initialState = initialState;
        }

        @Override
        public boolean canTeleport(FloorMap fm, int x, int y, Direction direction) {
                return true;
        }

        @Override
        public void teleport(FloorMap fm, int x, int y, Direction direction) {
                target = null;

        }

        protected void setState(State state) {
                if (currentState != null) currentState.end(this, token, token.command);
                currentState = state;
                currentState.begin(this, token, token.command);
        }

        protected State getCurrentState() {
                return currentState;
        }

        @Override
        public void setToken(Token token) {
                this.token = token;
                rand = token.dungeon.rand;
        }

        @Override
        public int getTeam() {
                return team;
        }

        @Override
        public boolean update(float delta) {
                if (currentState == null) setState(initialState);
                currentState.update(this, token, token.command, delta);
                return false;
        }


}
