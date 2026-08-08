package wiki.chiu.micro.common.rpc.storage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import wiki.chiu.micro.common.exception.ValidationException;

public final class ImageUploadValidator {

  private static final long MAX_PIXELS = 25_000_000L;

  private ImageUploadValidator() {}

  public static ValidatedImage validate(byte[] content) {
    if (content == null || content.length == 0) {
      throw new ValidationException("uploaded image is empty");
    }

    try (ImageInputStream input =
        ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
      if (input == null) {
        throw invalidImage();
      }
      Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext()) {
        throw invalidImage();
      }

      ImageReader reader = readers.next();
      try {
        reader.setInput(input, true, true);
        ImageFormat format = ImageFormat.from(reader.getFormatName());
        int width = reader.getWidth(0);
        int height = reader.getHeight(0);
        if (width <= 0 || height <= 0 || (long) width * height > MAX_PIXELS) {
          throw new ValidationException("uploaded image dimensions are invalid");
        }

        BufferedImage image = reader.read(0);
        ByteArrayOutputStream output = new ByteArrayOutputStream(content.length);
        if (!ImageIO.write(image, format.writerName, output)) {
          throw invalidImage();
        }
        return new ValidatedImage(output.toByteArray(), format.extension, format.contentType);
      } finally {
        reader.dispose();
      }
    } catch (IOException | RuntimeException exception) {
      if (exception instanceof ValidationException validationException) {
        throw validationException;
      }
      throw new ValidationException("uploaded image is invalid");
    }
  }

  private static ValidationException invalidImage() {
    return new ValidationException("only JPEG and PNG images are allowed");
  }

  private enum ImageFormat {
    JPEG("jpeg", "jpg", "image/jpeg"),
    PNG("png", "png", "image/png");

    private final String writerName;
    private final String extension;
    private final String contentType;

    ImageFormat(String writerName, String extension, String contentType) {
      this.writerName = writerName;
      this.extension = extension;
      this.contentType = contentType;
    }

    private static ImageFormat from(String value) {
      return switch (value.toLowerCase(Locale.ROOT)) {
        case "jpeg", "jpg" -> JPEG;
        case "png" -> PNG;
        default -> throw invalidImage();
      };
    }
  }

  public record ValidatedImage(byte[] content, String extension, String contentType) {

    public ValidatedImage {
      content = content.clone();
    }

    @Override
    public byte[] content() {
      return content.clone();
    }
  }
}
