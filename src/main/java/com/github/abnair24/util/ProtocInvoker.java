package com.github.abnair24.util;

import com.github.os72.protocjar.Protoc;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ProtocInvoker {

    public int invoke(ImmutableList<String> protocArgs) throws IOException, InterruptedException {
        return Protoc.runProtoc(protocArgs.toArray(new String[0]));
    }
}
