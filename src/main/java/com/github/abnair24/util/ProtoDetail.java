package com.github.abnair24.util;

import com.github.abnair24.cache.ProtoCache;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Getter
public class ProtoDetail {

    private final String protoPath;
    private final String packageName;
    private final String messageName;
    private final List<String> protoFiles;
    private final String descriptorBinaryPath;
    private final String fullMessageName;

    public ProtoDetail(String protoPath, String fullMessageName)
    {
        this.protoPath = protoPath;
        this.fullMessageName = fullMessageName;
        this.packageName = findPackageName(fullMessageName);
        this.messageName = findMessageName(fullMessageName, packageName.length());
        this.protoFiles = getAllProtoFiles(protoPath);
        this.descriptorBinaryPath = getBinaryPath();
    }

    private List<String> getAllProtoFiles(String protoPath) {

        List<String> protoFilePaths=null;

        Path path = Paths.get(protoPath);

        try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path, "*.proto"))
        {
            protoFilePaths = StreamSupport
                    .stream(directoryStream.spliterator(), false)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
        catch(IOException ex) {
            log.error("Proto path error : {}",path.toString());
        }

        return protoFilePaths;
    }

    private String findPackageName(String fullMethodName) {
        return fullMethodName.substring(0, fullMethodName.lastIndexOf('.'));
    }

    private String findMessageName(String fullMethodName, int length) {
        return fullMethodName.substring(length + 1);
    }

    private String getBinaryPath() {
        Path binaryPath = ProtoCache.getBinary(protoPath, protoFiles);
        return binaryPath.toAbsolutePath().toString();
    }
}
