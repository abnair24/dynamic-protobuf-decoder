package com.github.abnair24.jsonFormat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonFormater {

    public static String toJson(Message message) throws Exception {
        JsonFormat.Printer printer = JsonFormat.printer();
        return printer.print(message);
    }

    public static JsonObject toJsonObject(Message message) {
        JsonFormat.Printer printer = JsonFormat.printer();
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = null;
        try {
            jsonObject = jsonParser.parse(printer.print(message)).getAsJsonObject();
        } catch (InvalidProtocolBufferException e) {
            log.error("Json parser failed : {}",e.getMessage());
        }

        return jsonObject;
    }
}
