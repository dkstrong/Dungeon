package asf.dungeon.model.token.logic.fsm;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.token.Command;
import asf.dungeon.model.token.Token;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/20/2014.
 */
public enum Monster implements State {

        DoNothing {

        },
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
                                                break;
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
                        moveToSafeDistance(fsm, token, command, token.getAttack().getWeapon().getRange());
                }

                @Override
                public void update(FSMLogic fsm, Token token, Command command, float delta) {
                        if (!fsm.target.getDamage().isAttackable()) {
                                fsm.setState(Sleep);
                        } else if (!token.getAttack().isOnAttackCooldown()) {
                                fsm.setState(Chase);
                        } else if (command.getTargetToken() == null && !token.getMove().isMoving() && command.getLocation().equals(token.getLocation())) {
                                // if finished moving while keeping distance and attack cooldown still isnt finished
                                // then go in to attack target token mode
                                command.setTargetToken(fsm.target);
                                if(command.getTargetToken() == null)
                                        fsm.setState(Sleep);
                        } else if (command.getTargetToken() != null) {
                                // if elected to hold position because already at a good range, then continualy check
                                // distance to target and move back if needed
                                moveToSafeDistance(fsm, token, command, token.getAttack().getWeapon().getRange());
                        }


                }
        },
        Chase {
                @Override
                public void begin(FSMLogic fsm, Token token, Command command) {
                        command.setTargetToken(fsm.target);
                        if(command.getTargetToken() == null)
                                fsm.setState(Sleep);
                }

                @Override
                public void update(FSMLogic fsm, Token token, Command command, float delta) {
                        if (!fsm.target.getDamage().isAttackable()) {
                                fsm.setState(Sleep);
                                return;
                        }

                        if (token.getAttack().getWeapon().isRanged()) {
                                if (token.getAttack().isOnAttackCooldown()) {
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

        private static void moveToSafeDistance(FSMLogic fsm, Token token, Command command, float safeDistance) {
                // TODO: this seems to work ok as is. Ideally id like ot use multAdd instead of multAddFree
                // so the mosnter wont move out of their own range when moving diagonally. though when i use multAdd
                // the monster tends to want to stay horizontal/vertical with the player and ends up doing a bad
                // job of keeping distance
                //
                // TODO: when backed in to a corner the mosnter can kind of "flicker" inbetween two states, i think
                // this is caused by ChaseKeepDistance toggling between targeting and untargetting the player.
                float distance = token.getDistance(fsm.target);

                if (distance > safeDistance - .25f) {
                        return;
                }

                Direction dir = fsm.target.getLocation().direction(token.getLocation());
                int range = (int) safeDistance;
                FloorMap fm = token.getFloorMap();
                do {
                        fsm.pair.set(fsm.target.getLocation()).multAddFree(dir, range);
                        if(!fm.isLocationBlocked(fsm.pair)) break;
                        //fsm.pair.set(fsm.target.getLocation()).multAdd(dir, range, false);
                        //if(!fm.isLocationBlocked(fsm.pair)) break;

                        range--;
                } while (range > 0);

                if(range > 0){
                        command.setLocation(fsm.pair);
                }else{
                        // backed in to a corner, give command to just hold location
                        command.setLocation(token.getLocation());
                }

        }

}
