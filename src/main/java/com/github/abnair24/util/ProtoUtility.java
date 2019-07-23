package com.github.abnair24.util;

import com.github.os72.protocjar.Protoc;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class ProtoUtility {

    public static Path getDescriptorBinary(ProtoDetail protoDetail) throws IOException, InterruptedException {

        Path descFilePath = Files.createTempFile("ProtoDesc", ".desc");
        log.debug("Descriptor Binary path: {} ",descFilePath.toAbsolutePath().toString());

        ImmutableList<String> protocArgs = ImmutableList.<String>builder()
                .add("--include_imports")
                .add("--include_std_types")
                .add("--proto_path=" + protoDetail.getProtoPath())
                .add("--descriptor_set_out=" + descFilePath.toAbsolutePath().toString())
                .addAll(protoDetail.getProtoFiles())
                .build();

        int status = Protoc.runProtoc(protocArgs.toArray(new String[0]));

        if (status != 0) {
            log.error("Binary file generation failed with status : {} " , status);
        }
        return descFilePath;
    }
}
