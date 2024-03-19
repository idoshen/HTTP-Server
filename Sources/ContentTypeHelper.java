import java.io.IOException;

public class ContentTypeHelper {
    public static ContentType GetContentType(String file) {
        String[] fileSplitted = file.split("\\.");
        String fileExtension = fileSplitted[fileSplitted.length - 1];
        return getContentTypeFromFileExtension(fileExtension);
    }

    private static ContentType getContentTypeFromFileExtension(String fileExtension) {
        for (ContentType type : ContentType.values()) {
            if (type.name().equalsIgnoreCase(fileExtension)) {
                return type;
            }
        }

        return ContentType.other;
    }

    public enum ContentType {
        html("text/html"),
        jpg("image"),
        png("image"),
        bmp("image"),
        gif("image"),
        ico("icon"),
        other("application/octet-stream");

        private String description;

        ContentType(String description) {
            this.description = description;
        }

        public String GetDescription() {
            return description;
        }
    }
}
