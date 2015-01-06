package asf.dungeon.model.token.logic.fsm;

import asf.dungeon.model.Direction;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.fogmap.LOS;
import asf.dungeon.model.token.Command;
import asf.dungeon.model.token.StatusEffect;
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
                public void begin(FsmLogic fsm, Token token, Command command) {
                        if (fsm.sector == null || fsm.sector.contains(token.location)) {
                                // monster haas no sector, or already is in sector, just sit still
                                command.setLocation(token.location);
                        } else {
                                // move to a point in sector
                                FloorMap floorMap = token.floorMap;
                                int x, y, tries = 0;
                                do {
                                        if (++tries > 20) {
                                                x = token.location.x;
                                                y = token.location.y;
                                                break;
                                        }
                                        x = fsm.sector.getRandomX(fsm.rand);
                                        y = fsm.sector.getRandomY(fsm.rand);
                                } while (floorMap.getTile(x, y) == null || !floorMap.getTile(x, y).isFloor() || floorMap.hasTokensAt(x, y));
                                command.setLocation(x, y);
                        }
                }

                @Override
                public void update(FsmLogic fsm, Token token, Command command, float delta) {
                        checkForTargets(fsm, token, command);
                }


        },
        Explore {
                @Override
                public void begin(FsmLogic fsm, Token token, Command command) {
                        command.setLocation(token.location);
                }

                @Override
                public void update(FsmLogic fsm, Token token, Command command, float delta) {
                        // TODO: i think how explore should work is that it moves to random nearby locations,
                        // but after 30 seconds or so go in to sleep/return to sector
                        if (token.isLocatedAt(token.command.getLocation())) {
                                // Wander aimlessly
                                FloorMap floorMap = token.floorMap;
                                int x, y, tries = 0;
                                do {
                                        if (++tries > 20) {
                                                x = token.location.x;
                                                y = token.location.y;
                                                return;
                                        }
                                        x = token.dungeon.rand.random.nextInt(floorMap.getWidth());
                                        y = token.dungeon.rand.random.nextInt(floorMap.getHeight());
                                } while (floorMap.getTile(x, y) == null || !floorMap.getTile(x, y).isFloor() || floorMap.hasTokensAt(x, y));
                                command.setLocation(x, y);
                        } else {
                                // If see a valid target, then switch to chase state
                                checkForTargets(fsm, token, command);
                        }


                }
        },
        Lured {
                @Override
                public void begin(FsmLogic fsm, Token token, Command command) {
                        super.begin(fsm, token, command);
                }

                @Override
                public void update(FsmLogic fsm, Token token, Command command, float delta) {
                        if (fsm.target.statusEffects == null || !fsm.target.statusEffects.has(StatusEffect.LuresMonsters)) {
                                fsm.setState(Explore);
                                return;
                        }

                        command.setLocation(fsm.target.location);

                        checkForTargets(fsm, token, command);
                }
        },
        ChaseKeepDistance {
                @Override
                public void begin(FsmLogic fsm, Token token, Command command) {
                        moveToSafeDistance(fsm, token, command, token.attack.getWeapon().getRange());
                }

                @Override
                public void update(FsmLogic fsm, Token token, Command command, float delta) {

                        if (checkForConfused(fsm, token, command))
                                return;
                        if (!fsm.target.damage.isAttackable()) {
                                fsm.setState(Explore);
                        } else if (!token.attack.isOnAttackCooldown()) {
                                fsm.setState(Chase);
                        } else if (command.getTargetToken() == null && !token.move.isMoving() && command.getLocation().equals(token.location)) {
                                // if finished moving while keeping distance and attack cooldown still isnt finished
                                // then go in to attack target token mode
                                command.setTargetToken(fsm.target);
                                if (command.getTargetToken() == null)
                                        fsm.setState(Sleep);
                        } else if (command.getTargetToken() != null) {
                                // if elected to hold position because already at a good range, then continualy check
                                // distance to target and move back if needed
                                moveToSafeDistance(fsm, token, command, token.attack.getWeapon().getRange());
                        }


                }
        },
        Chase {
                @Override
                public void begin(FsmLogic fsm, Token token, Command command) {
                        command.setTargetToken(fsm.target);
                        if (command.getTargetToken() == null)
                                fsm.setState(Explore);
                }

                @Override
                public void update(FsmLogic fsm, Token token, Command command, float delta) {
                        if (!fsm.target.damage.isAttackable()) {
                                fsm.setState(Explore);
                                return;
                        }

                        if (checkForConfused(fsm, token, command))
                                return;

                        if (token.attack.getWeapon().isRanged()) {
                                if (token.attack.isOnAttackCooldown()) {
                                        fsm.setState(ChaseKeepDistance);
                                }
                        }


                }
        };

        @Override
        public void begin(FsmLogic fsm, Token token, Command command) {

        }

        @Override
        public void end(FsmLogic fsm, Token token, Command command) {

        }

        @Override
        public void update(FsmLogic fsm, Token token, Command command, float delta) {

        }

        private static boolean checkForConfused(FsmLogic fsm, Token token, Command command) {
                if (fsm.target.logic != null && token.statusEffects != null) {
                        // ensure monster is chasing the appropriate team based on its confused state
                        if (fsm.target.logic.getTeam() == token.logic.getTeam() && !token.statusEffects.has(StatusEffect.Confused)) {
                                fsm.setState(Explore);
                                return true;
                        } else if (fsm.target.logic.getTeam() != token.logic.getTeam() && token.statusEffects.has(StatusEffect.Confused)) {
                                fsm.setState(Explore);
                                return true;
                        }
                }
                return false;

        }

        private static boolean checkForTargets(FsmLogic fsm, Token token, Command command) {
                FloorMap fm = token.floorMap;

                Array<Token> attackableTokens = fm.getTokens();
                for (Token t : attackableTokens) {
                        if (fsm.getCurrentState() != Lured &&
                                token.statusEffects != null &&
                                token.statusEffects.has(StatusEffect.LuresMonsters) &&
                                !token.statusEffects.has(StatusEffect.Confused)) {
                                fsm.target = t;
                                fsm.setState(Lured);
                                return true;
                        }

                        if (t.logic == null || t.damage == null || !t.damage.isAttackable())
                                continue;

                        if (token.statusEffects != null && token.statusEffects.has(StatusEffect.Confused)) {
                                if (t.logic.getTeam() != token.logic.getTeam())
                                        continue;
                        } else {
                                if (t.logic.getTeam() == token.logic.getTeam())
                                        continue;
                        }

                        float distance = token.distance(t);
                        // TODO: sight radius is an integer/manhatten value, may want to alter this check some..
                        if(distance > token.damage.getSightRadius())
                                continue;

                        if(!LOS.hasLineOfSightManual(token.floorMap, token.location.x, token.location.y, t.location.x, t.location.y))
                                continue;

                        fsm.target = t;
                        fsm.setState(Chase);

                }
                return false;
        }

        private static void moveToSafeDistance(FsmLogic fsm, Token token, Command command, float safeDistance) {
                // TODO: this seems to work ok as is. Ideally id like ot use multAdd instead of multAddFree
                // so the mosnter wont move out of their own range when moving diagonally. though when i use multAdd
                // the monster tends to want to stay horizontal/vertical with the player and ends up doing a bad
                // job of keeping distance
                //
                // TODO: when backed in to a corner the mosnter can kind of "flicker" inbetween two states, i think
                // this is caused by ChaseKeepDistance toggling between targeting and untargetting the player.
                float distance = token.distance(fsm.target);

                if (distance > safeDistance - .25f) {
                        return;
                }

                Direction dir = fsm.target.location.direction(token.location);
                int range = (int) safeDistance;
                FloorMap fm = token.floorMap;
                do {
                        fsm.pair.set(fsm.target.location).multAddFree(dir, range);
                        if (!fm.isLocationBlocked(fsm.pair)) break;
                        //fsm.pair.set(fsm.target.getLocation()).multAdd(dir, range, false);
                        //if(!fm.isLocationBlocked(fsm.pair)) break;

                        range--;
                } while (range > 0);

                if (range > 0) {
                        command.setLocation(fsm.pair);
                } else {
                        // backed in to a corner, give command to just hold location
                        command.setLocation(token.location);
                }

        }

}
