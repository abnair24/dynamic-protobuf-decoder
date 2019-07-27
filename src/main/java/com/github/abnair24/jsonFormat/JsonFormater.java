package com.github.abnair24.jsonFormat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonFormater {

    private JsonFormat.Printer printer;

    public JsonFormater() {
        printer = JsonFormat.printer();
    }

    public String toJson(Message message) throws Exception {
        return printer.print(message);
    }

    public JsonObject toJsonObject(Message message) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = null;
        try {
            jsonObject = jsonParser.parse(printer.print(message)).getAsJsonObject();
        } catch (InvalidProtocolBufferException e) {
            log.error("Json parser failed : {}",e.getMessage());
        }
        return jsonObject;
    }

    public <Out> Out toClassObject(Message message, Class<Out> outputClass) throws Exception {
        String response = printer.print(message);
        Out out = new Gson().fromJson(response,outputClass);
        return out;
    }


}
