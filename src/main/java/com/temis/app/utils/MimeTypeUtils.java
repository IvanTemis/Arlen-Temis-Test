package com.temis.app.utils;

public class MimeTypeUtils {
    
    public static String getMimeTypeFromFileType(String filePath, String fileType, String mimeTypeFromStorage) {
        if ("application/octet-stream".equalsIgnoreCase(mimeTypeFromStorage)) {
            String extension = filePath.substring(filePath.lastIndexOf('.') + 1).toUpperCase();
            switch (extension) {
                case "JPG":
                case "JPEG":
                    return "image/jpeg";
                case "PNG":
                    return "image/png";
                case "PDF":
                    return "application/pdf";
                default:
                    return null; // Tipo no soportado
            }
        }
        return mimeTypeFromStorage;
    }
}