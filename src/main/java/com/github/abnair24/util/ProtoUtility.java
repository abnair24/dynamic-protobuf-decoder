package com.github.abnair24.util;

import com.github.abnair24.exception.DescriptorBinaryException;
import com.github.os72.protocjar.Protoc;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class ProtoUtility {

    public static Path generateDescriptorBinary(ProtoDetail protoDetail) throws DescriptorBinaryException {
        int status;
        Path descFilePath;

        try {
            descFilePath = Files.createTempFile("ProtoDesc", ".desc");

            log.debug("Descriptor Binary path: {} ", descFilePath.toAbsolutePath().toString());

            ImmutableList<String> protocArgs = ImmutableList.<String>builder()
                    .add("--include_imports")
                    .add("--include_std_types")
                    .add("--proto_path=" + protoDetail.getProtoPath())
                    .add("--descriptor_set_out=" + descFilePath.toAbsolutePath().toString())
                    .addAll(protoDetail.getProtoFiles())
                    .build();

            status = Protoc.runProtoc(protocArgs.toArray(new String[0]));

            if (status != 0) {
                log.error("Binary file generation failed with status : {} ", status);

                throw new RuntimeException("Protoc binaray file generation failed");
            }
        }catch (IOException | InterruptedException ex) {
            log.error("Protoc invokation failed");

            throw new DescriptorBinaryException(ex);
        }
        return descFilePath;
    }
}
