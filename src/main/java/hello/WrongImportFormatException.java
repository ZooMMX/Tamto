package hello;

import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Created by octavioruiz on 14/02/16.
 */
public class WrongImportFormatException extends Exception {
    public WrongImportFormatException(String msg, Exception t) {
        super(msg, t);
    }
}
