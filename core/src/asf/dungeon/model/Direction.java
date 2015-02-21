package asf.dungeon.model;

/**
 * Created by danny on 10/22/14.
 */
public enum Direction {
        North(180),
        South(0),
        East(90),
        West(270),
        NorthEast(135),
        NorthWest(225),
        SouthEast(45),
        SouthWest(315);

        public final transient int degrees;

        Direction(int degrees) {
                this.degrees = degrees;
        }

        public boolean isOpposite(Direction dir) {
                switch (dir) {
                        case North:
                                return this == South;
                        case South:
                                return this == North;
                        case East:
                                return this == West;
                        case West:
                                return this == East;
                        case NorthEast:
                                return this == SouthWest;
                        case NorthWest:
                                return this == SouthEast;
                        case SouthEast:
                                return this == NorthWest;
                        case SouthWest:
                                return this == NorthEast;
                }

                throw new AssertionError(dir);
        }

        public Direction opposite() {
                switch (this) {
                        case North:
                                return South;
                        case South:
                                return North;
                        case East:
                                return West;
                        case West:
                                return East;
                        case NorthEast:
                                return SouthWest;
                        case NorthWest:
                                return SouthEast;
                        case SouthEast:
                                return NorthWest;
                        case SouthWest:
                                return NorthEast;
                }

                throw new AssertionError(this);
        }

        public Direction rotate(int degrees) {
                return Direction.getDirection(this.degrees+degrees);
        }

        public int range(Direction dir){
                return Math.abs(degrees - dir.degrees);
        }

        public static Direction getDirection(int degrees) {
                while(degrees >=360) degrees-=360;
                while(degrees <0) degrees+=360;

                if(degrees == North.degrees) return North;
                if(degrees == East.degrees) return East;
                if(degrees == South.degrees) return South;
                if(degrees == West.degrees) return West;

                if(degrees == NorthEast.degrees) return NorthEast;
                if(degrees == NorthWest.degrees) return NorthWest;
                if(degrees == SouthEast.degrees) return SouthEast;
                if(degrees == SouthWest.degrees) return SouthWest;

                throw new AssertionError(degrees);
        }

        public boolean isCardinal(){ return this == North || this== East || this == West || this==South;}

        public boolean isDiagonal() {
                return this == NorthEast || this == NorthWest || this == SouthEast || this == SouthWest;
        }

}
