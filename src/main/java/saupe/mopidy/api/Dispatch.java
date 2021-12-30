package saupe.mopidy.api;

import java.util.function.Consumer;

public class Dispatch<T> {
    private Consumer<T> responseHandler;
    private Action errorHandler;

    public void onResponse(Consumer<T> handler) {
        this.responseHandler = handler;
    }

    public void onError(Action handler) {
        this.errorHandler = handler;
    }

    protected Consumer<T> getResponseHandler() {
        return responseHandler;
    }

    protected Action getErrorHandler() {
        return errorHandler;
    }
}