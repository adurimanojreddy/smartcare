package com.smartcare.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Renders a QR code PNG (base64-encoded) for a given payload, e.g. an
 * appointment code (Module 10). The QR payload is just the appointment
 * code string - the server re-validates the appointment on scan rather
 * than trusting any embedded status/date, avoiding stale/forged check-ins.
 */
@Component
public class QrCodeGenerator {

    private static final int SIZE = 300;

    public String generateBase64Png(String payload) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(payload, BarcodeFormat.QR_CODE, SIZE, SIZE);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Failed to generate QR code", e);
        }
    }
}
