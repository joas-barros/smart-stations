package device.src.model;

import java.io.Serializable;
import java.util.zip.CRC32;

public class IntegrityPacket implements Serializable {
    private byte[] data;
    private long checksum;

    public IntegrityPacket(byte[] data) {
        this.data = data;
        this.checksum = calculateChecksum(data);
    }

    private long calculateChecksum(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    public boolean isValid() {
        return calculateChecksum(this.data) == this.checksum;
    }

    public byte[] getData() {
        return data;
    }
}
