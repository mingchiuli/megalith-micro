package wiki.chiu.micro.common.rpc.storage;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

import wiki.chiu.micro.common.exception.ValidationException;

/**
 * Validates uploaded images by walking the JPEG and PNG container structure only: pixels are never
 * decoded and no {@code java.awt}/{@code javax.imageio} class is touched, because the AWT stack
 * cannot be initialized inside a GraalVM native image. Metadata is stripped by copying the essential
 * segments and dropping the ones that only carry user data.
 */
public final class ImageUploadValidator {

    private static final long MAX_PIXELS = 25_000_000L;

    private static final byte[] PNG_SIGNATURE = {
        (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A
    };

    /** Length and type of a PNG chunk. */
    private static final int PNG_CHUNK_HEADER = 8;

    /** CRC of a PNG chunk. */
    private static final int PNG_CHUNK_TRAILER = 4;

    private static final int PNG_IHDR = 0x49484452;

    private static final int PNG_IDAT = 0x49444154;

    private static final int PNG_IEND = 0x49454E44;

    private static final int PNG_EXIF = 0x65455866;

    private static final int PNG_TIME = 0x74494D45;

    private static final int PNG_TEXT = 0x74455874;

    private static final int PNG_COMPRESSED_TEXT = 0x7A545874;

    private static final int PNG_INTERNATIONAL_TEXT = 0x69545874;

    private static final int JPEG_SOI = 0xFFD8;

    private static final int JPEG_EOI = 0xFFD9;

    private static final int JPEG_EOI_MARKER = 0xD9;

    private static final int JPEG_SOS = 0xDA;

    private static final int JPEG_COMMENT = 0xFE;

    private static final int JPEG_APP1 = 0xE1;

    private static final int JPEG_APP2 = 0xE2;

    private static final int JPEG_APP14 = 0xEE;

    private static final int JPEG_APP15 = 0xEF;

    private static final byte[] JPEG_ICC_PROFILE =
        "ICC_PROFILE\0".getBytes(StandardCharsets.US_ASCII);

    private ImageUploadValidator() {
    }

    public static ValidatedImage validate(byte[] content) {
        if (content == null || content.length == 0) {
            throw new ValidationException("uploaded image is empty");
        }

        ImageFormat format = ImageFormat.detect(content);
        byte[] sanitized =
            switch (format) {
                case PNG -> sanitizePng(content);
                case JPEG -> sanitizeJpeg(content);
            };
        return new ValidatedImage(sanitized, format.extension, format.contentType);
    }

    private static byte[] sanitizePng(byte[] content) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(content.length);
        output.writeBytes(PNG_SIGNATURE);
        boolean header = false;
        boolean data = false;
        boolean end = false;
        int position = PNG_SIGNATURE.length;
        while (position < content.length) {
            if (end || position > content.length - (PNG_CHUNK_HEADER + PNG_CHUNK_TRAILER)) {
                throw invalidImage();
            }
            int length = readInt(content, position);
            int remaining = content.length - position - PNG_CHUNK_HEADER - PNG_CHUNK_TRAILER;
            if (length < 0 || length > remaining) {
                throw invalidImage();
            }
            int type = readInt(content, position + 4);
            if (type == PNG_IHDR) {
                if (header || position != PNG_SIGNATURE.length || length != 13) {
                    throw invalidImage();
                }
                header = true;
                requireDimensions(readInt(content, position + 8), readInt(content, position + 12));
            } else if (type == PNG_IDAT) {
                if (!header) {
                    throw invalidImage();
                }
                data = true;
            } else if (type == PNG_IEND) {
                if (!data || length != 0) {
                    throw invalidImage();
                }
                end = true;
            } else if (!header) {
                throw invalidImage();
            }
            requireChunkChecksum(content, position, length);
            if (keepPngChunk(type)) {
                output.write(content, position, length + PNG_CHUNK_HEADER + PNG_CHUNK_TRAILER);
            }
            position += length + PNG_CHUNK_HEADER + PNG_CHUNK_TRAILER;
        }
        if (!end) {
            throw invalidImage();
        }
        return output.toByteArray();
    }

    /** Keeps critical chunks and the ancillary chunks that change how the pixels are rendered. */
    private static boolean keepPngChunk(int type) {
        return type != PNG_TEXT
            && type != PNG_COMPRESSED_TEXT
            && type != PNG_INTERNATIONAL_TEXT
            && type != PNG_EXIF
            && type != PNG_TIME;
    }

    private static byte[] sanitizeJpeg(byte[] content) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(content.length);
        output.write(content, 0, 2);
        boolean frame = false;
        int position = 2;
        while (true) {
            if (position > content.length - 2 || (content[position] & 0xFF) != 0xFF) {
                throw invalidImage();
            }
            int marker = content[position + 1] & 0xFF;
            if (marker == 0xFF) {
                position++;
                continue;
            }
            if (marker == 0x00 || marker == 0xD8 || marker == JPEG_EOI_MARKER) {
                throw invalidImage();
            }
            if (marker == 0x01 || (marker >= 0xD0 && marker <= 0xD7)) {
                position += 2;
                continue;
            }
            int segment = position;
            position += 2;
            if (position > content.length - 2) {
                throw invalidImage();
            }
            int length = readShort(content, position);
            if (length < 2 || length > content.length - position) {
                throw invalidImage();
            }
            if (isFrameMarker(marker)) {
                if (length < 8) {
                    throw invalidImage();
                }
                requireDimensions(readShort(content, position + 3), readShort(content, position + 5));
                frame = true;
            }
            if (keepJpegSegment(marker, content, position, length)) {
                output.write(content, segment, length + 2);
            }
            position += length;
            if (marker == JPEG_SOS) {
                if (!frame) {
                    throw invalidImage();
                }
                break;
            }
        }
        int scan = position;
        int eoi = indexOfMarker(content, scan, JPEG_EOI);
        if (eoi < 0) {
            throw invalidImage();
        }
        output.write(content, scan, eoi + 2 - scan);
        return output.toByteArray();
    }

    /** Keeps JFIF, ICC/Adobe color markers, coding segments, and scan data. */
    private static boolean keepJpegSegment(
        int marker, byte[] content, int lengthOffset, int length) {
        if (marker == JPEG_APP2
            && matchesAt(content, lengthOffset + 2, JPEG_ICC_PROFILE)
            && length >= JPEG_ICC_PROFILE.length + 2) {
            return true;
        }
        if (marker == JPEG_APP14) {
            return true;
        }
        return marker != JPEG_COMMENT && (marker < JPEG_APP1 || marker > JPEG_APP15);
    }

    /** The markers that carry the frame header; {@code 0xC4}, {@code 0xC8}, and {@code 0xCC} are not. */
    private static boolean isFrameMarker(int marker) {
        return marker >= 0xC0 && marker <= 0xCF && marker != 0xC4 && marker != 0xC8 && marker != 0xCC;
    }

    private static void requireDimensions(int width, int height) {
        if (width <= 0 || height <= 0 || (long) width * height > MAX_PIXELS) {
            throw new ValidationException("uploaded image dimensions are invalid");
        }
    }

    private static void requireChunkChecksum(byte[] content, int position, int length) {
        CRC32 checksum = new CRC32();
        checksum.update(content, position + 4, length + 4);
        if ((int) checksum.getValue() != readInt(content, position + PNG_CHUNK_HEADER + length)) {
            throw invalidImage();
        }
    }

    private static int indexOfMarker(byte[] content, int from, int marker) {
        for (int index = from; index <= content.length - 2; index++) {
            if (readShort(content, index) == marker) {
                return index;
            }
        }
        return -1;
    }

    private static int readShort(byte[] content, int offset) {
        return ((content[offset] & 0xFF) << 8) | (content[offset + 1] & 0xFF);
    }

    private static int readInt(byte[] content, int offset) {
        return (readShort(content, offset) << 16) | readShort(content, offset + 2);
    }

    private static boolean startsWith(byte[] content, byte[] prefix) {
        return matchesAt(content, 0, prefix);
    }

    private static boolean matchesAt(byte[] content, int offset, byte[] prefix) {
        if (offset < 0 || content.length - offset < prefix.length) {
            return false;
        }
        for (int index = 0; index < prefix.length; index++) {
            if (content[offset + index] != prefix[index]) {
                return false;
            }
        }
        return true;
    }

    private static ValidationException invalidImage() {
        return new ValidationException("only JPEG and PNG images are allowed");
    }

    private enum ImageFormat {
        JPEG("jpg", "image/jpeg"),
        PNG("png", "image/png");

        private final String extension;
        private final String contentType;

        ImageFormat(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        private static ImageFormat detect(byte[] content) {
            if (startsWith(content, PNG_SIGNATURE)) {
                return PNG;
            }
            if (content.length >= 2 && readShort(content, 0) == JPEG_SOI) {
                return JPEG;
            }
            throw invalidImage();
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
