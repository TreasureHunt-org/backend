package org.treasurehunt.common.util;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FIleUploadUtil {

    public static void saveFile(String uploadDir, String filename, MultipartFile multipartFile) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String cleanFilename = StringUtils.cleanPath(filename);  // Prevent path traversal attacks
        Path filePath = uploadPath.resolve(cleanFilename);

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioException) {
            throw new IOException("Could not save image file: " + cleanFilename, ioException);
        }
    }

    /**
     * Deletes a file from the specified directory
     *
     * @param uploadDir the directory where the file is located
     * @param filename the name of the file to delete
     * @return true if the file was successfully deleted, false otherwise
     */
    public static boolean deleteFile(String uploadDir, String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            String cleanFilename = StringUtils.cleanPath(filename);
            Path filePath = uploadPath.resolve(cleanFilename);

            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
}
