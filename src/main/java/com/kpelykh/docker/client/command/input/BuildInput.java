package com.kpelykh.docker.client.command.input;

import java.io.File;

public class BuildInput {
    private final File dockerFolder;
    private final String tag;
    private final boolean noCache;

    public BuildInput(File dockerFolder, String tag, boolean noCache) {
        this.dockerFolder = dockerFolder;
        this.tag = tag;
        this.noCache = noCache;
    }

    public File getDockerFolder() {
        return dockerFolder;
    }

    public String getTag() {
        return tag;
    }

    public boolean isNoCache() {
        return noCache;
    }
}
