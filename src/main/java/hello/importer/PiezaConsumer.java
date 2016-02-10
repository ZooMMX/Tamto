package hello.importer;

import hello.Pieza;

import java.util.function.Consumer;

/**
 * Created by octavioruizcastillo on 06/02/16.
 */
public interface PiezaConsumer extends Consumer<Pieza> {
    @Override
    void accept(Pieza pieza);

    void onException(Exception e);
}
