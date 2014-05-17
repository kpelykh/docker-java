package com.kpelykh.docker.client.utils;

import java.net.URI;
import java.net.URISyntaxException;

public final class ResourceUtil {
    private ResourceUtil() {}

    public static boolean isFileResource(String file) throws URISyntaxException {
        URI srcFileURI = new URI(file);
        return srcFileURI.getScheme() == null || "file".equals(srcFileURI.getScheme());
    }
}
