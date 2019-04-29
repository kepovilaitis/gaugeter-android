package lt.kepo.gaugeter.tools;

import java.io.DataInputStream;
import java.io.IOException;

public class ByteParser {

    public boolean parseHeader(DataInputStream inFromModule, int headerIteration) throws IOException, NullPointerException {
        byte[] symbol = new byte[1];
        symbol[0] = inFromModule.readByte();

        if ("|".equals(new String(symbol)))
            headerIteration++;

        if (headerIteration == 2)
            return true;

        return parseHeader(inFromModule, headerIteration);
    }

    public int parseInt(DataInputStream inputStream) throws IOException {
        byte[] bytes = new byte[4];

        bytes[0] = inputStream.readByte();
        bytes[1] = inputStream.readByte();
        bytes[2] = inputStream.readByte();
        bytes[3] = inputStream.readByte();

        return toInt32(bytes);
    }

    public float parseFloat(DataInputStream inputStream) throws IOException {
        byte[] bytes = new byte[4];

        bytes[0] = inputStream.readByte();
        bytes[1] = inputStream.readByte();
        bytes[2] = inputStream.readByte();
        bytes[3] = inputStream.readByte();

        return toFloatBytes(bytes);
    }

    private int toInt32(byte[] bytes) {
        return (bytes[3]) << 24 |
                (bytes[2] & 0xff) << 16 |
                (bytes[1] & 0xff) << 8 |
                (bytes[0] & 0xff);
    }

    private float toFloatBytes(byte[] bytes) {
        int accum = 0;

        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8)
            accum |= (bytes[shiftBy / 8] & 0xff) << shiftBy;

        return Float.intBitsToFloat(accum);
    }
}
