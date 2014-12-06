package asf.dungeon.model.token.logic.fsm;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.Tile;
import asf.dungeon.model.token.Command;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/20/2014.
 */
public enum Monster implements State {

        Sleep {
                @Override
                public void begin(FSMLogic fsm, Token token, Command command) {
                        if (fsm.sector == null || fsm.sector.contains(token.getLocation())) {
                                // monster haas no sector, or already is in sector, just sit still
                                command.setLocation(token.getLocation());
                        } else {
                                // move to a point in sector
                                FloorMap floorMap = token.getFloorMap();
                                int x, y, tries = 0;
                                do {
                                        if (++tries > 20) {
                                                x = token.getLocation().x;
                                                y = token.getLocation().y;
                                                return;
                                        }
                                        x = fsm.sector.getRandomX(fsm.rand);
                                        y = fsm.sector.getRandomY(fsm.rand);
                                } while (floorMap.getTile(x, y) == null || !floorMap.getTile(x, y).isFloor() || floorMap.hasTokensAt(x, y));
                                command.setLocation(x, y);
                        }
                }

                @Override
                public void update(FSMLogic fsm, Token token, Command command, float delta) {
                        Array<Token> tokensInSector;
                        if (fsm.sector != null && fsm.sector.contains(token.getLocation())) {
                                tokensInSector = token.getFloorMap().getTokensAt(fsm.sector);
                        } else {
                                tokensInSector = token.getFloorMap().getTokensInExtent(token.getLocation(), token.getDamage().getSightRadius());
                        }

                        chaseToken(fsm, token, command, tokensInSector);

                }


        },

        ChaseKeepDistance {
                @Override
                public void begin(FSMLogic fsm, Token token, Command command) {
                        // if you have a ranged weapon, then the monster should try to stay at maximum range while on attack cooldown.
                        int distance = fsm.target.getLocation().distance(token.getLocation());
                        if (distance == token.getAttack().getAttackRange()) {
                                // sit still, good location
                                command.setLocation(token.getLocation());
                        } else {
                                Gdx.app.log("MonsterFSM", "keep distance");
                                // move to the closest location whose distance == attack range
                                // TODO: this can easily choose an "invalid" location such as a wall tile,
                                // need to do additional checks to make sure a valid floor tile is chosen by limiting the move range until
                                // shrink the "keep distance" range until a valid tile is found to stand on
                                Direction dir = fsm.target.getLocation().direction(token.getLocation());
                                FloorMap fm = token.getFloorMap();
                                int r = token.getAttack().getAttackRange();
                                Tile tile;
                                do{
                                        fsm.pair.set(fsm.target.getLocation()).add(dir, r--);
                                        tile = fm.getTile(fsm.pair);
                                }while((tile == null || fm.isLocationBlocked(fsm.pair)) && r>0);
                                if(r==0 ){
                                        command.setLocation(token.getLocation());
                                }else{
                                        command.setLocation(fsm.pair);
                                }

                        }


                }

                @Override
                public void update(FSMLogic fsm, Token token, Command command, float delta) {
                        if (!token.getAttack().isOnAttackCooldown()) {
                                fsm.setState(Chase);
                        }
                }
        },
        Chase {
                @Override
                public void begin(FSMLogic fsm, Token token, Command command) {
                        command.setTargetToken(fsm.target);
                }

                @Override
                public void update(FSMLogic fsm, Token token, Command command, float delta) {
                        if (fsm.target.getDamage().isDead()) {
                                fsm.setState(Sleep);
                                return;
                        }

                        if (token.getAttack().getWeapon().isRanged()) {
                                if (token.getAttack().isOnAttackCooldown() ) {
                                        fsm.setState(ChaseKeepDistance);
                                }
                        }


                }
        },
        Explore {
                @Override
                public void begin(FSMLogic fsm, Token token, Command command) {
                        command.setLocation(token.getLocation());
                }

                @Override
                public void update(FSMLogic fsm, Token token, Command command, float delta) {
                        if (token.isLocatedAt(token.getCommand().getLocation())) {
                                // Wander aimlessly
                                FloorMap floorMap = token.getFloorMap();
                                int x, y, tries = 0;
                                do {
                                        if (++tries > 20) {
                                                x = token.getLocation().x;
                                                y = token.getLocation().y;
                                                return;
                                        }
                                        x = token.dungeon.rand.random.nextInt(floorMap.getWidth());
                                        y = token.dungeon.rand.random.nextInt(floorMap.getHeight());
                                } while (floorMap.getTile(x, y) == null || !floorMap.getTile(x, y).isFloor() || floorMap.hasTokensAt(x, y));
                                command.setLocation(x, y);
                        } else {
                                // If see a valid target, then switch to chase state
                                Array<Token> tokensInSector = token.getFloorMap().getTokensInExtent(token.getLocation(), token.getDamage().getSightRadius());
                                chaseToken(fsm, token, command, tokensInSector);
                        }


                }
        };

        @Override
        public void begin(FSMLogic fsm, Token token, Command command) {

        }

        @Override
        public void end(FSMLogic fsm, Token token, Command command) {

        }

        @Override
        public void update(FSMLogic fsm, Token token, Command command, float delta) {

        }

        private static void chaseToken(FSMLogic fsm, Token token, Command command, Array<Token> tokensInSector) {
                for (Token t : tokensInSector) {
                        if (t.getLogic() != null && t.getLogic().getTeam() != fsm.getTeam() && t.getDamage() != null && t.getDamage().isAttackable()) {
                                if (t.getFogMapping() == null) {
                                        // target does not have fogmapping, assume vision is possible
                                        fsm.target = t;
                                        fsm.setState(Chase);
                                        return;
                                } else {
                                        // target has fogmapping, so use it to ensure vision is possible
                                        boolean canSeeMe = t.getFogMapping().getCurrentFogMap().isVisible(token.getLocation().x, token.getLocation().y);
                                        if (canSeeMe) {
                                                fsm.target = t;
                                                fsm.setState(Chase);
                                                return;
                                        }
                                }

                        }
                }
        }
}
