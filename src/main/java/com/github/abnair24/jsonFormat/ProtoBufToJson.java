package com.github.abnair24.jsonFormat;

import com.github.abnair24.exception.JsonFormatException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtoBufToJson {

    private final Descriptors.Descriptor descriptor;
    private DynamicMessage dynamicMessage;
    private JsonFormater jsonFormater;

    public ProtoBufToJson(Descriptors.Descriptor descriptor) {
        this.descriptor = descriptor;
        jsonFormater = new JsonFormater();
    }

    public JsonObject protobufToJsonObject(byte[] inputData) {

        try {
            dynamicMessage = DynamicMessage.parseFrom(descriptor,inputData);
        } catch (InvalidProtocolBufferException e) {
            log.error("Dynamic message parsing failed: {}",e.getMessage());
        }
        return jsonFormater.toJsonObject(dynamicMessage);
    }

    public String protobufToJson(byte[] inputData) throws Exception {
        try {
            dynamicMessage =DynamicMessage.parseFrom(descriptor,inputData);
        } catch (InvalidProtocolBufferException e) {
            log.error("Dynamic message parsing failed: {}",e.getMessage());
        }
        return jsonFormater.toJson(dynamicMessage);
    }

    public <T> T toClassObject(Message message, Class<T> outputClass) throws Exception {
        String response = jsonFormater.toJson(message);
        T out = new Gson().fromJson(response,outputClass);
        return out;
    }

    public DynamicMessage fromJsonToDynamicMessage(String jsonRequest) throws JsonFormatException {
        DynamicMessage.Builder builder;
        try {
            JsonFormat.Parser jsonParser = JsonFormat.parser();
            builder = DynamicMessage.newBuilder(descriptor);
            jsonParser.merge(jsonRequest, builder);

        } catch (InvalidProtocolBufferException ex) {
            throw new JsonFormatException(ex);
        }

        return builder.build();
    }

    public <T> DynamicMessage fromObjectToDynamicMessage(T requestObject) {
        DynamicMessage dynamicMessage;
        try {
            String requestObjectAsJson = new GsonBuilder().create().toJson(requestObject);
            dynamicMessage = fromJsonToDynamicMessage(requestObjectAsJson);
        } catch (JsonFormatException ex) {
            log.error("Json format invalid");
            throw new RuntimeException(ex);
        }
        return dynamicMessage;
    }
}
