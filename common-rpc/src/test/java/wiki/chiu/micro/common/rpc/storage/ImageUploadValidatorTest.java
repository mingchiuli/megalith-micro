package wiki.chiu.micro.common.rpc.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import wiki.chiu.micro.common.exception.ValidationException;

class ImageUploadValidatorTest {

  @Test
  void decodesAndReencodesSupportedImages() throws IOException {
    byte[] png = image("png");
    ImageUploadValidator.ValidatedImage validated = ImageUploadValidator.validate(png);

    assertEquals("png", validated.extension());
    assertEquals("image/png", validated.contentType());
    assertTrue(validated.content().length > 0);
    assertTrue(validated.content() != png);
  }

  @Test
  void rejectsUnsupportedAndCorruptContent() throws IOException {
    assertThrows(ValidationException.class, () -> ImageUploadValidator.validate(new byte[0]));
    assertThrows(
        ValidationException.class, () -> ImageUploadValidator.validate("<svg/>".getBytes()));
    assertThrows(ValidationException.class, () -> ImageUploadValidator.validate(image("gif")));
  }

  @Test
  void rejectsImagesWithExcessiveDimensionsBeforeDecodingPixels() throws IOException {
    assertThrows(
        ValidationException.class, () -> ImageUploadValidator.validate(pngHeader(6000, 5000)));
  }

  private static byte[] image(String format) throws IOException {
    BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ImageIO.write(image, format, output);
    return output.toByteArray();
  }

  private static byte[] pngHeader(int width, int height) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try (DataOutputStream data = new DataOutputStream(output)) {
      data.writeLong(0x89504e470d0a1a0aL);
      data.writeInt(13);
      byte[] typeAndData;
      try (ByteArrayOutputStream chunk = new ByteArrayOutputStream();
          DataOutputStream fields = new DataOutputStream(chunk)) {
        fields.writeBytes("IHDR");
        fields.writeInt(width);
        fields.writeInt(height);
        fields.writeByte(8);
        fields.writeByte(2);
        fields.writeByte(0);
        fields.writeByte(0);
        fields.writeByte(0);
        typeAndData = chunk.toByteArray();
      }
      data.write(typeAndData);
      CRC32 crc = new CRC32();
      crc.update(typeAndData);
      data.writeInt((int) crc.getValue());
    }
    return output.toByteArray();
  }
}
