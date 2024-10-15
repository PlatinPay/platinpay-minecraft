package cc.platinpay.minecraft.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.PublicKey;
import java.util.Base64;

public class TokenManager {

    private final File publicKeyFile;

    public TokenManager(File pluginFolder) {
        this.publicKeyFile = new File(pluginFolder, "public_key.pem");
    }

    public void setPublicKey(PublicKey publicKey) throws IOException {
        byte[] encodedKey = publicKey.getEncoded();
        String pemKey = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(encodedKey) +
                "\n-----END PUBLIC KEY-----";

        try (FileWriter writer = new FileWriter(publicKeyFile)) {
            writer.write(pemKey);
        }
    }

    public PublicKey loadPublicKey() throws Exception {
        if (!publicKeyFile.exists()) {
            throw new IOException("Public key file not found.");
        }

        String pem = Files.readString(publicKeyFile.toPath());
        String base64Key = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return java.security.KeyFactory.getInstance("Ed25519")
                .generatePublic(new java.security.spec.X509EncodedKeySpec(decodedKey));
    }
}