package asf.dungeon.model;

/**
 * Created by danny on 10/22/14.
 */
public enum Direction {
        North, South, East, West,
        NorthEast, NorthWest, SouthEast, SouthWest;


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

        public Direction opposite(){
                switch (this) {
                        case North:
                                return  South;
                        case South:
                                return  North;
                        case East:
                                return  West;
                        case West:
                                return  East;
                        case NorthEast:
                                return  SouthWest;
                        case NorthWest:
                                return  SouthEast;
                        case SouthEast:
                                return  NorthWest;
                        case SouthWest:
                                return  NorthEast;
                }

                throw new AssertionError(this);
        }

        public boolean isDiagonal(){
                return ordinal() > 3;
        }

        public boolean isNorth(){
                return this == North || this == NorthEast || this == NorthWest;
        }

        public boolean isEast(){
                return this == East || this == NorthEast || this == SouthEast;
        }

        public boolean isWest(){
                return this == West || this == NorthWest || this == SouthWest;
        }

        public boolean isSouth(){
                return this == South || this == SouthEast || this == SouthWest;
        }



}
