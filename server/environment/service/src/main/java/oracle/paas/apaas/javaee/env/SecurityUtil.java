package oracle.paas.apaas.javaee.env;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import oracle.paas.platform.config.SMConfig;
import weblogic.security.internal.SerializedSystemIni;

/**
 * Created by ramgarg on 8/31/2016.
 */
public class SecurityUtil {

    private static Logger logger = Logger.getLogger(SecurityUtil.class.getName());
    private static String basedir;
    private static String domainHome;


    public static String generateSecurityDatFile() throws IOException {
        domainHome = SMConfig.getDomainHome();
        basedir = (domainHome == null ? "" : domainHome) + "/tmp";
        String serialIniDatFilename = "";
        Path baseDir = Paths.get(basedir);
        Path accTempDir = null;

        ByteArrayOutputStream baos = null;
        final int BUFFER = 2048;
        byte buffer[] = new byte[BUFFER];
        FileInputStream in = null;
        try {
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }
            accTempDir = Files.createTempDirectory(baseDir, "accs");
            String accTempSecurityDir = accTempDir + File.separator + "security";
            Files.createDirectory(Paths.get(accTempSecurityDir));

            SerializedSystemIni.getEncryptionService(accTempDir.toString());
            serialIniDatFilename = accTempSecurityDir  + File.separator + "SerializedSystemIni.dat";
            Path secDatFilePath = Paths.get(serialIniDatFilename);

            setFilePermissions(accTempDir);
            setFilePermissions(Paths.get(accTempSecurityDir));
            setFilePermissions(secDatFilePath);

            logger.log(Level.INFO, "App tools file generated." + serialIniDatFilename);

            in = new FileInputStream(serialIniDatFilename);
            baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            zos.putNextEntry(new ZipEntry(secDatFilePath.getFileName().toString()));
            int length;
            while ((length = in.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.flush();
            zos.closeEntry();
            zos.close();
            String zipPath = accTempDir + File.separator + "appTools.zip";
            writeToZip(zipPath , new ByteArrayInputStream(baos.toByteArray()));
            return zipPath;

        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Exception while generating SerializedSystemIni.dat", ioe);
            throw ioe;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception while generating SerializedSystemIni.dat", e);
            throw e;
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void setFilePermissions(Path filePath) throws IOException {
        //using PosixFilePermission to set file permissions 777
        Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
        //add owners permission
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        //add group permissions
        perms.add(PosixFilePermission.GROUP_READ);
        //perms.add(PosixFilePermission.GROUP_WRITE);
        //perms.add(PosixFilePermission.GROUP_EXECUTE);
        //add others permissions
        perms.add(PosixFilePermission.OTHERS_READ);
        //perms.add(PosixFilePermission.OTHERS_WRITE);
        //perms.add(PosixFilePermission.OTHERS_EXECUTE);

        Files.setPosixFilePermissions(filePath, perms);
    }

    public static void writeToZip(String filepath, InputStream inputStream) {

        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(new File(filepath));
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            System.out.println("Done!");

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
