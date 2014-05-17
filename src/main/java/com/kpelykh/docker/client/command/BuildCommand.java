package com.kpelykh.docker.client.command;

import com.google.common.base.Preconditions;
import com.kpelykh.docker.client.DockerException;
import com.kpelykh.docker.client.command.input.BuildInput;
import com.kpelykh.docker.client.utils.CompressArchiveUtil;
import com.kpelykh.docker.client.utils.ResourceUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BuildCommand implements DockerCommand<BuildInput> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BuildCommand.class);
    private final Client client;
    private final String restEndpointUrl;

    public BuildCommand(Client client, String restEndpointUrl) {
        this.client = client;
        this.restEndpointUrl = restEndpointUrl;
    }

    @Override
    public ClientResponse execute(BuildInput input) throws DockerException {
        Preconditions.checkNotNull(input.getDockerFolder(), "Folder is null");
        Preconditions.checkArgument(input.getDockerFolder().exists(), "Folder %s doesn't exist", input.getDockerFolder());
        Preconditions.checkState(new File(input.getDockerFolder(), "Dockerfile").exists(), "Dockerfile doesn't exist in " + input.getDockerFolder());

        //We need to use Jersey HttpClient here, since ApacheHttpClient4 will not add boundary filed to
        //Content-Type: multipart/form-data; boundary=Boundary_1_372491238_1372806136625

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("t", input.getTag());
        if (input.isNoCache()) {
            params.add("nocache", "true");
        }

        // ARCHIVE TAR
        String archiveNameWithOutExtension = UUID.randomUUID().toString();

        File dockerFolderTar = null;

        try {
            File dockerFile = new File(input.getDockerFolder(), "Dockerfile");
            List<String> dockerFileContent = FileUtils.readLines(dockerFile);

            if (dockerFileContent.size() <= 0) {
                throw new DockerException(String.format("Dockerfile %s is empty", dockerFile));
            }

            List<File> filesToAdd = new ArrayList<File>();
            filesToAdd.add(dockerFile);

            for (String cmd : dockerFileContent) {
                if (StringUtils.startsWithIgnoreCase(cmd.trim(), "ADD")) {
                    String addArgs[] = StringUtils.split(cmd, " \t");
                    if (addArgs.length != 3) {
                        throw new DockerException(String.format("Wrong format on line [%s]", cmd));
                    }

                    String addSrcParameter = addArgs[1];
                    boolean localFile = isFileResource(addSrcParameter);

                    if(localFile) {
                        addLocalFile(addSrcParameter, input.getDockerFolder(), filesToAdd);
                    }
                }
            }

            dockerFolderTar = CompressArchiveUtil.archiveTARFiles(input.getDockerFolder(), filesToAdd, archiveNameWithOutExtension);

        } catch (IOException ex) {
            FileUtils.deleteQuietly(dockerFolderTar);
            throw new DockerException("Error occurred while preparing Docker context folder.", ex);
        }

        return sendRemoteRequest(params, dockerFolderTar);
    }

    /**
     * Checks if <src> parameter of ADD instruction is a local file or directory.
     *
     * @param addSrcParameter Src parameter of ADD instruction
     * @return Flag
     * @throws DockerException
     */
    private boolean isFileResource(String addSrcParameter) throws DockerException {
        try {
            return ResourceUtil.isFileResource(addSrcParameter);
        }
        catch(URISyntaxException e) {
            throw new DockerException("Source file has to be a valid file, directory or remote file URL", e);
        }
    }

    /**
     * Adds local file read from ADD instruction to list of files to copy to remote Docker.
     *
     * @param addSrcParameter Src parameter of ADD instruction
     * @param dockerFolder Docker folder
     * @param filesToAdd List of files to add
     * @throws IOException
     * @throws DockerException
     */
    private void addLocalFile(String addSrcParameter, File dockerFolder, List<File> filesToAdd) throws IOException, DockerException {
        File src = new File(addSrcParameter);
        if (!src.isAbsolute()) {
            src = new File(dockerFolder, addSrcParameter).getCanonicalFile();
        }

        if (!src.exists()) {
            throw new DockerException(String.format("Source file %s doesn't exist", src));
        }
        if (src.isDirectory()) {
            filesToAdd.addAll(FileUtils.listFiles(src, null, true));
        } else {
            filesToAdd.add(src);
        }
    }

    private ClientResponse sendRemoteRequest(MultivaluedMap<String, String> params, File dockerFolderTar) throws DockerException {
        WebResource webResource = client.resource(restEndpointUrl + "/build").queryParams(params);

        try {
            LOGGER.trace("POST: {}", webResource);
            return webResource
                    .type("application/tar")
                    .accept(MediaType.TEXT_PLAIN)
                    .post(ClientResponse.class, FileUtils.openInputStream(dockerFolderTar));
        } catch (UniformInterfaceException exception) {
            if (exception.getResponse().getStatus() == 500) {
                throw new DockerException("Server error", exception);
            } else {
                throw new DockerException(exception);
            }
        } catch (IOException e) {
            throw new DockerException(e);
        } finally {
            FileUtils.deleteQuietly(dockerFolderTar);
        }
    }
}
