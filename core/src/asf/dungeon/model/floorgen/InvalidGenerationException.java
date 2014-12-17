package asf.dungeon.model.floorgen;

/**
 * Thrown whena floor generator is in the middle of generating a floor and realizes
 * the floor layout is invalid/unbeatable and should be regenerated from the start.
 *
 * Floor Generators should be made to always generate a valid floor on the first run.
 * This exception should be mainly for catching programming errors and fixing them,
 * it shouldnt be relied on as a standard way to generate floors.
 *
 * Created by Daniel Strong on 12/16/2014.
 */
public class InvalidGenerationException extends RuntimeException{
        public InvalidGenerationException() {
        }

        public InvalidGenerationException(String message) {
                super(message);
        }

        public InvalidGenerationException(String message, Throwable cause) {
                super(message, cause);
        }

        public InvalidGenerationException(Throwable cause) {
                super(cause);
        }

        public InvalidGenerationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
                super(message, cause, enableSuppression, writableStackTrace);
        }
}
