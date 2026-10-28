package wiki.chiu.micro.common.rpc.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.exception.ValidationException;

class ImageUploadValidatorTest {

    private static final byte[] METADATA = "megalith-metadata".getBytes(StandardCharsets.US_ASCII);

    private static final byte[] PNG_SIGNATURE = {
        (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A
    };

    /** Signature plus the length, type, and payload of the fixed IHDR chunk. */
    private static final int PNG_IHDR_END = 8 + 12 + 13;

    /** Offset of the CRC that protects the IHDR payload. */
    private static final int PNG_IHDR_CHECKSUM = 8 + 8 + 13;

    @Test
    void acceptsSupportedImagesWithoutDecodingPixels() throws IOException {
        byte[] png = image("png");
        ImageUploadValidator.ValidatedImage validated = ImageUploadValidator.validate(png);

        assertEquals("png", validated.extension());
        assertEquals("image/png", validated.contentType());
        assertArrayEquals(png, validated.content());
        assertTrue(validated.content() != png);
        ImageUploadValidator.validate(validated.content());
    }

    @Test
    void acceptsJpegImages() throws IOException {
        ImageUploadValidator.ValidatedImage validated = ImageUploadValidator.validate(image("jpeg"));

        assertEquals("jpg", validated.extension());
        assertEquals("image/jpeg", validated.contentType());
        assertTrue(validated.content().length > 0);
    }

    @Test
    void stripsPngChunksThatOnlyCarryUserMetadata() throws IOException {
        byte[] png = withPngTextChunk(image("png"), METADATA);
        ImageUploadValidator.ValidatedImage validated = ImageUploadValidator.validate(png);

        assertTrue(validated.content().length < png.length);
        assertFalse(contains(validated.content(), METADATA));
    }

    @Test
    void stripsJpegSegmentsThatOnlyCarryUserMetadata() throws IOException {
        byte[] jpeg = withJpegExifSegment(image("jpeg"), METADATA);
        ImageUploadValidator.ValidatedImage validated = ImageUploadValidator.validate(jpeg);

        assertTrue(validated.content().length < jpeg.length);
        assertFalse(contains(validated.content(), METADATA));
    }

    @Test
    void preservesJpegColorProfileMarkers() throws IOException {
        byte[] iccProfile = "ICC_PROFILE\0profile".getBytes(StandardCharsets.US_ASCII);
        byte[] jpeg =
            withJpegSegment(
                withJpegSegment(image("jpeg"), 0xE2, iccProfile),
                0xEE,
                "Adobe".getBytes(StandardCharsets.US_ASCII));

        ImageUploadValidator.ValidatedImage validated = ImageUploadValidator.validate(jpeg);

        assertTrue(contains(validated.content(), iccProfile));
        assertTrue(contains(validated.content(), "Adobe".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    void rejectsUnsupportedAndCorruptContent() throws IOException {
        assertThrows(ValidationException.class, () -> ImageUploadValidator.validate(new byte[0]));
        assertThrows(
            ValidationException.class,
            () -> ImageUploadValidator.validate("<svg/>".getBytes(StandardCharsets.UTF_8)));
        assertThrows(ValidationException.class, () -> ImageUploadValidator.validate(image("gif")));
        assertThrows(
            ValidationException.class,
            () -> ImageUploadValidator.validate(new byte[] {(byte) 0xFF, (byte) 0xD8}));
        assertThrows(
            ValidationException.class,
            () -> ImageUploadValidator.validate(truncated(image("png"), 12)));
        assertThrows(
            ValidationException.class,
            () -> ImageUploadValidator.validate(truncated(image("jpeg"), 2)));
        assertThrows(ValidationException.class, () -> ImageUploadValidator.validate(scanOnlyJpeg()));
    }

    @Test
    void rejectsImagesWithExcessiveDimensionsBeforeDecodingPixels() throws IOException {
        assertThrows(
            ValidationException.class, () -> ImageUploadValidator.validate(pngHeader(6000, 5000)));
        assertThrows(
            ValidationException.class, () -> ImageUploadValidator.validate(jpegHeader(6000, 5000)));
    }

    @Test
    void rejectsPngWithBrokenChunkChecksum() throws IOException {
        byte[] corrupt = image("png");
        corrupt[PNG_IHDR_CHECKSUM] ^= 0x01;

        assertThrows(ValidationException.class, () -> ImageUploadValidator.validate(corrupt));
    }

    @Test
    void rejectsPngWithTrailingData() throws IOException {
        byte[] png = image("png");
        byte[] appended = splice(png, png.length, METADATA);

        assertThrows(ValidationException.class, () -> ImageUploadValidator.validate(appended));
    }

    @Test
    void dropsJpegTrailingData() throws IOException {
        byte[] jpeg = image("jpeg");
        byte[] appended = splice(jpeg, jpeg.length, METADATA);
        ImageUploadValidator.ValidatedImage validated = ImageUploadValidator.validate(appended);

        assertTrue(validated.content().length < appended.length);
        assertFalse(contains(validated.content(), METADATA));
    }

    private static byte[] image(String format) throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, format, output);
        return output.toByteArray();
    }

    private static byte[] pngHeader(int width, int height) throws IOException {
        return splice(PNG_SIGNATURE, PNG_SIGNATURE.length, pngChunk("IHDR", ihdrData(width, height)));
    }

    private static byte[] jpegHeader(int width, int height) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(output)) {
            data.writeShort(0xFFD8);
            data.writeShort(0xFFC0);
            data.writeShort(11);
            data.writeByte(8);
            data.writeShort(height);
            data.writeShort(width);
            data.writeByte(1);
            data.writeByte(1);
            data.writeByte(0x11);
            data.writeByte(0);
            data.writeShort(0xFFDA);
            writeScanHeader(data);
            data.writeShort(0xFFD9);
        }
        return output.toByteArray();
    }

    /** A start-of-scan segment without any frame header, which has no usable dimensions. */
    private static byte[] scanOnlyJpeg() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(output)) {
            data.writeShort(0xFFD8);
            data.writeShort(0xFFDA);
            writeScanHeader(data);
            data.writeShort(0xFFD9);
        }
        return output.toByteArray();
    }

    private static void writeScanHeader(DataOutputStream data) throws IOException {
        data.writeShort(8);
        data.writeByte(1);
        data.writeByte(1);
        data.writeByte(0);
        data.writeByte(0);
        data.writeByte(0x3F);
        data.writeByte(0);
    }

    private static byte[] withPngTextChunk(byte[] png, byte[] text) throws IOException {
        return splice(png, PNG_IHDR_END, pngChunk("tEXt", text));
    }

    private static byte[] withJpegExifSegment(byte[] jpeg, byte[] payload) throws IOException {
        return withJpegSegment(jpeg, 0xE1, payload);
    }

    private static byte[] withJpegSegment(byte[] jpeg, int marker, byte[] payload)
        throws IOException {
        ByteArrayOutputStream segment = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(segment)) {
            data.writeShort(0xFF00 | marker);
            data.writeShort(payload.length + 2);
            data.write(payload);
        }
        return splice(jpeg, 2, segment.toByteArray());
    }

    private static byte[] pngChunk(String type, byte[] data) throws IOException {
        byte[] typeBytes = type.getBytes(StandardCharsets.US_ASCII);
        CRC32 checksum = new CRC32();
        checksum.update(typeBytes);
        checksum.update(data);
        ByteArrayOutputStream chunk = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(chunk)) {
            output.writeInt(data.length);
            output.write(typeBytes);
            output.write(data);
            output.writeInt((int) checksum.getValue());
        }
        return chunk.toByteArray();
    }

    private static byte[] ihdrData(int width, int height) throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        try (DataOutputStream fields = new DataOutputStream(data)) {
            fields.writeInt(width);
            fields.writeInt(height);
            fields.writeByte(8);
            fields.writeByte(2);
            fields.writeByte(0);
            fields.writeByte(0);
            fields.writeByte(0);
        }
        return data.toByteArray();
    }

    private static byte[] splice(byte[] content, int offset, byte[] insertion) {
        byte[] result = new byte[content.length + insertion.length];
        System.arraycopy(content, 0, result, 0, offset);
        System.arraycopy(insertion, 0, result, offset, insertion.length);
        System.arraycopy(content, offset, result, offset + insertion.length, content.length - offset);
        return result;
    }

    private static byte[] truncated(byte[] content, int length) {
        return Arrays.copyOf(content, content.length - length);
    }

    private static boolean contains(byte[] content, byte[] needle) {
        outer:
        for (int index = 0; index <= content.length - needle.length; index++) {
            for (int offset = 0; offset < needle.length; offset++) {
                if (content[index + offset] != needle[offset]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }
}
