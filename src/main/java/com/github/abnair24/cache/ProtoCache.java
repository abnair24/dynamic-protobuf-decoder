package com.github.abnair24.cache;

import com.github.abnair24.exception.CacheLoadingException;
import com.github.abnair24.exception.DescriptorBinaryException;
import com.github.abnair24.util.ProtoUtility;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProtoCache {

    private static final LoadingCache<DescriptorProtos.FileDescriptorProto, Descriptors.FileDescriptor> fdCache =
            CacheBuilder
            .newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats()
            .build(new CacheLoader<DescriptorProtos.FileDescriptorProto, Descriptors.FileDescriptor>() {
                @Override
                public Descriptors.FileDescriptor load(DescriptorProtos.FileDescriptorProto key) throws Exception {
                    return ProtoUtility.findDependentFileDescriptors(key);
                }
            });

    private static Cache<String, Path> descriptorBinaryCache = CacheBuilder
            .newBuilder()
            .maximumSize(100)
            .expireAfterWrite(30,TimeUnit.MINUTES)
            .recordStats()
            .build();

    public static Descriptors.FileDescriptor getFileDescriptor(
            DescriptorProtos.FileDescriptorProto fileDescriptorProto) throws CacheLoadingException {

        Descriptors.FileDescriptor fileDescriptor;
        try {
            fileDescriptor = fdCache.get(fileDescriptorProto);
            log.info("Stats:{}", fdCache.stats());
        } catch (ExecutionException ex) {
            throw new CacheLoadingException(ex);
        }
        return fileDescriptor;
    }

    public static Set<DescriptorProtos.FileDescriptorProto> getAllFileDescriptorFromCache() {
        return fdCache.asMap().keySet();
    }

    public static Path getBinary(final String protoPath, final List<String> protoFiles) {

        Path path = descriptorBinaryCache.getIfPresent("descriptor.desc");
        log.info("DescriptorCache Stats: {}",descriptorBinaryCache.stats());

        try {
            if (path == null) {
                path = ProtoUtility.generateDescriptorBinary(protoPath, protoFiles);
                descriptorBinaryCache.put("descriptor.desc", path);
            }
        } catch (DescriptorBinaryException ex) {
            log.error(ex.getMessage());
            throw new RuntimeException(ex);
        }
        return path;
    }
}
