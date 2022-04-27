package saupe.mopidy.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import saupe.mopidy.misc.JsonKeywords;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Call<T> {

    public enum CallState {
        NOT_CALLED, ONGOING, DONE
    }

    private final Gson gson = new Gson();

    @Getter
    private final Dispatch<T> dispatch = new Dispatch<>();

    @Getter
    @Setter
    private CallState state = CallState.NOT_CALLED;

    /**
     * ID of request
     */
    @Getter
    private int id;

    /**
     * The request data
     */
    @Getter
    private final JsonObject request;

    /**
     * The params field of the request
     */
    private final JsonObject params;

    /**
     * The Java type of the result field of the response
     */
    private TypeToken<T> resultType;

    /**
     * Timestamp when this call was dispatched
     */
    @Getter
    @Setter
    private long timestamp;

    public Call(String method) {
        params = new JsonObject();

        request = new JsonObject();
        request.addProperty(JsonKeywords.JSONRPC, "2.0");
        request.addProperty(JsonKeywords.METHOD, method);
        request.add(JsonKeywords.PARAMS, params);
    }

    public Call<T> setResultType(TypeToken<T> resultType) {
        this.resultType = resultType;
        return this;
    }

    public Call<T> setResultType(Class<T> resultType) {
        return setResultType(TypeToken.get(resultType));
    }

    public final void onResult(JsonElement response) {
        setState(CallState.DONE);

        try {
            T result = parseResult(response);
            if (dispatch.getResponseHandler() != null) {
                dispatch.getResponseHandler().accept(result);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (dispatch.getErrorHandler() != null) {
                dispatch.getErrorHandler().execute();
            }
        }
    }

    public void onError(int code, String message, JsonElement data) {
        setState(CallState.DONE);

        log.error("code: " + code + " message: " + message + " data: " + data);
        if (dispatch.getErrorHandler() != null) {
            dispatch.getErrorHandler().execute();
        }
    }

    @Override
    public String toString() {
        return request.toString();
    }

    protected T parseResult(JsonElement response) {
        if (resultType == null || resultType.getRawType().isAssignableFrom(Void.class))
            return null;
        return gson.fromJson(response, resultType.getType());
    }

    protected Call<T> addParam(String name, String value) {
        params.addProperty(name, value);
        return this;
    }

    protected Call<T> addParam(String name, Boolean value) {
        params.addProperty(name, value);
        return this;
    }

    protected Call<T> addParam(String name, Number value) {
        params.addProperty(name, value);
        return this;
    }

    protected Call<T> addParam(String name, JsonElement value) {
        params.add(name, value);
        return this;
    }

    public void setId(int id) {
        this.id = id;
        getRequest().addProperty(JsonKeywords.ID, id);
    }

}