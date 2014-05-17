package com.kpelykh.docker.client.command;

import com.kpelykh.docker.client.DockerException;
import com.sun.jersey.api.client.ClientResponse;

public interface DockerCommand<T> {
    ClientResponse execute(T input) throws DockerException;
}
