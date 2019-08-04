package com.github.abnair24.dynamicMessage;

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
public class DynamicMessageProcessor {

    private final Descriptors.Descriptor descriptor;
    private DynamicMessage dynamicMessage;

    public DynamicMessageProcessor(Descriptors.Descriptor descriptor) {
        this.descriptor = descriptor;
    }

    public DynamicMessage toDynamicMessage(byte[] inputData) {
        try {
            dynamicMessage = DynamicMessage.parseFrom(descriptor, inputData);
        } catch (InvalidProtocolBufferException e) {
            log.error("Dynamic message parsing failed: {}", e.getMessage());
        }
        return dynamicMessage;
    }

    public DynamicMessage toDynamicMessage(String json) throws JsonFormatException {
        Message.Builder builder;
        try {
            JsonFormat.Parser parser = JsonFormat.parser();
            builder = DynamicMessage.newBuilder(descriptor);
            parser.merge(json, builder);
        } catch (InvalidProtocolBufferException ex) {
            throw new JsonFormatException(ex);
        }
        return ((DynamicMessage.Builder) builder).build();
    }

    public <T> DynamicMessage toDynamicMessage(T requestObject) {
        try {
            String json = new GsonBuilder().create().toJson(requestObject);
            dynamicMessage = toDynamicMessage(json);
        } catch (JsonFormatException ex) {
            log.error("Json format invalid");
            throw new RuntimeException(ex);
        }
        return dynamicMessage;
    }

    public String toJson(Message message) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(message);
    }

    public JsonObject toJsonObject(Message message) throws InvalidProtocolBufferException {

        return toClassObject(message, JsonObject.class);
//
//        JsonParser jsonParser = new JsonParser();
//
//        String response = toJson(message);
//        return jsonParser.parse(response).getAsJsonObject();
    }

    public <T> T toClassObject(Message message, Class<T> outputClass) throws InvalidProtocolBufferException {
        String response = toJson(message);
        T out = new Gson().fromJson(response, outputClass);
        return out;
    }

    public JsonObject parse(byte[] inputData) throws InvalidProtocolBufferException{
        DynamicMessage message = toDynamicMessage(inputData);
        return toJsonObject(message);
    }

    public <T> T parse(byte[] inputData, Class<T> outputClass) throws InvalidProtocolBufferException{
        DynamicMessage message = toDynamicMessage(inputData);
        return toClassObject(message,outputClass);
    }

    public <In,Out> Out parse(Class<In> requestMessage, Class<Out> OutputClass) throws InvalidProtocolBufferException {
        DynamicMessage message= toDynamicMessage(requestMessage);
        return toClassObject(message,OutputClass);
    }
}
