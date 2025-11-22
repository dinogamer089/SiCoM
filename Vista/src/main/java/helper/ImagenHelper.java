package helper;

import java.util.Base64;

/**
 * Convierte la imagen binaria de la BD a un dataURL
 * para ponerlo directo en <img src="..."> en JSF
 */
public final class ImagenHelper {

    private ImagenHelper() { }

    public static String toDataUrl(String mime, byte[] bin) {
        if (mime == null || bin == null) {
            return null;
        }
        return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bin);
    }
}
