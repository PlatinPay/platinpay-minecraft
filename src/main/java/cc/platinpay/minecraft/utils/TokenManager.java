package cc.platinpay.minecraft.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class TokenManager {

    private final File publicKeyFile;

    public TokenManager(File pluginFolder) {
        this.publicKeyFile = new File(pluginFolder, "public_key.pem");
    }

    public void setPublicKey(PublicKey publicKey) throws IOException {
        byte[] encodedKey = publicKey.getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(encodedKey);

        try (FileWriter writer = new FileWriter(publicKeyFile)) {
            writer.write(base64Key);
        }
    }

    public PublicKey loadPublicKey() throws Exception {
        if (!publicKeyFile.exists()) {
            throw new IOException("Public key file not found.");
        }

        String base64Key = Files.readString(publicKeyFile.toPath(), StandardCharsets.UTF_8).trim();

        byte[] decodedKey = Base64.getDecoder().decode(base64Key);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");

        return keyFactory.generatePublic(keySpec);
    }

    public Boolean publicKeyExists() {
        return publicKeyFile.exists();
    }
}