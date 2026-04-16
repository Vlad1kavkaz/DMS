package ru.vlad.satellitedb.util;

import javafx.scene.image.Image;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Iterator;

public final class SatelliteImageUtil {

    private static final int MAX_WIDTH = 420;
    private static final int MAX_HEIGHT = 260;
    private static final float JPEG_QUALITY = 0.82f;

    private SatelliteImageUtil() {
    }

    public static byte[] loadAndCompress(File file) {
        try {
            BufferedImage source = ImageIO.read(file);
            if (source == null) {
                throw new IllegalArgumentException("Не удалось прочитать изображение");
            }

            BufferedImage resized = resize(source, MAX_WIDTH, MAX_HEIGHT);
            return writeJpeg(resized, JPEG_QUALITY);
        } catch (Exception e) {
            throw new IllegalArgumentException("Не удалось обработать изображение: " + e.getMessage(), e);
        }
    }

    public static Image toFxImage(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return new Image(new ByteArrayInputStream(bytes));
    }

    private static BufferedImage resize(BufferedImage source, int maxWidth, int maxHeight) {
        int width = source.getWidth();
        int height = source.getHeight();

        double scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
        scale = Math.min(scale, 1.0);

        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));

        BufferedImage result = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        return result;
    }

    private static byte[] writeJpeg(BufferedImage image, float quality) throws Exception {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("JPEG writer не найден");
        }

        ImageWriter writer = writers.next();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);

            ImageWriteParam params = writer.getDefaultWriteParam();
            if (params.canWriteCompressed()) {
                params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                params.setCompressionQuality(quality);
            }

            writer.write(null, new IIOImage(image, null, null), params);
        } finally {
            writer.dispose();
        }

        return out.toByteArray();
    }
}