package saupe.mopidy;

import saupe.mopidy.api.Call;

import java.util.LinkedList;
import java.util.Queue;

public class CallBuilder implements Runnable {
    private final Queue<Call<?>> calls = new LinkedList<>();
    private CallHandler handler;

    public synchronized void queue(Call<?> call) {
        if (call != null) {
            calls.add(call);
        }
    }

    public synchronized void dequeue() {
        calls.poll();
    }

    @Override
    public void run() {
        while (calls.size() > 0) {
            Call<?> call = calls.peek();
            if (call != null) {
                call.call();

                while (true) {
                    if (call.isResponseReceived()) {
                        break;
                    }
                }

                dequeue();
            }
        }

        if (handler != null) {
            handler.onFinished();
        }
    }

    public void start() {
        run();
    }

    public CallBuilder setHandler(CallHandler handler) {
        this.handler = handler;
        return this;
    }
}