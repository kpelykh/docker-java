package com.kpelykh.docker.client.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.io.filefilter.FileFilterUtils.*;

public class CompressArchiveUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompressArchiveUtil.class);

	public static File archiveTARFiles(File baseDir, String archiveNameWithOutExtension) throws IOException {

		File tarFile = null;
		
        tarFile = new File(FileUtils.getTempDirectoryPath(), archiveNameWithOutExtension + ".tar");

        Collection<File> files =
                FileUtils.listFiles(
                        baseDir,
                        new RegexFileFilter("^(.*?)"),
                        and(directoryFileFilter(), notFileFilter(nameFileFilter(baseDir.getName()))));

        byte[] buf = new byte[1024];
        int len;

        {
            TarArchiveOutputStream tos = new TarArchiveOutputStream(new FileOutputStream(tarFile));
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            for (File file : files) {
                TarArchiveEntry tarEntry = new TarArchiveEntry(file);
                tarEntry.setName(StringUtils.substringAfter(file.toString(), baseDir.getPath()));
                Set<PosixFilePermission> filePerm = null;
                try {
                    filePerm = Files.getPosixFilePermissions(Paths.get(file.toURI()));
                    tarEntry.setMode(new Permission('-' + PosixFilePermissions.toString(filePerm)).getOctalCode());
                } catch (IOException e) {
                    LOGGER.trace("Could't read Posix file permissions for " + file.getPath());
                }

                tos.putArchiveEntry(tarEntry);

                if (!file.isDirectory()) {
                    FileInputStream fin = new FileInputStream(file);
                    BufferedInputStream in = new BufferedInputStream(fin);

                    while ((len = in.read(buf)) != -1) {
                        tos.write(buf, 0, len);
                    }

                    in.close();
                }
                tos.closeArchiveEntry();

            }
            tos.close();
        }

		
		return tarFile;
	}
}
