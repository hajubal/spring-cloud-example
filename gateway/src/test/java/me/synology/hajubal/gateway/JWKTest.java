package me.synology.hajubal.gateway;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.gen.OctetSequenceKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

public class JWKTest {

    @Test
    void jwk() throws JOSEException, NoSuchAlgorithmException {

        // 비대칭키 JWK
        KeyPairGenerator rsaKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
        rsaKeyPairGenerator.initialize(2048);

        KeyPair keyPair = rsaKeyPairGenerator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey1 = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID("rsa-kid1")
                .build();


        RSAKey rsaKey2 = new RSAKeyGenerator(2048)
                .keyID("rsa-kid2")
                .keyUse(KeyUse.SIGNATURE)
                .keyOperations(Set.of(KeyOperation.SIGN))
                .algorithm(JWSAlgorithm.RS512)
                .generate();

        // 대칭키 JWK
        SecretKey secretKey = new SecretKeySpec(
                Base64.getDecoder().decode("bCzY/M48bbkwBEWjmNSIEPfwApcvXOnkCxORBEbPr+4="), "AES");

        OctetSequenceKey octetSequenceKey1 = new OctetSequenceKey.Builder(secretKey)
                .keyID("secret-kid1")
                .keyUse(KeyUse.SIGNATURE)
                .keyOperations(Set.of(KeyOperation.SIGN))
                .algorithm(JWSAlgorithm.HS256)
                .build();

        OctetSequenceKey octetSequenceKey2 = new OctetSequenceKeyGenerator(256)
                .keyID("secret-kid2")
                .keyUse(KeyUse.SIGNATURE)
                .keyOperations(Set.of(KeyOperation.SIGN))
                .algorithm(JWSAlgorithm.HS384)
                .generate();


        String kId;
//        kId = rsaKey1.getKeyID();
//        kId = rsaKey2.getKeyID();
        kId = octetSequenceKey1.getKeyID();
//        kId = octetSequenceKey2.getKeyID();

        JWSAlgorithm alg;
//        alg = (JWSAlgorithm)rsaKey1.getAlgorithm();
//        alg = (JWSAlgorithm)rsaKey2.getAlgorithm();
        alg = (JWSAlgorithm) octetSequenceKey1.getAlgorithm();
//        alg = (JWSAlgorithm)octetSequenceKey2.getAlgorithm();
//
        KeyType type;
        //type = KeyType.RSA;
        type = KeyType.OCT;

        jwkSet(kId, alg, type, rsaKey1, rsaKey2, octetSequenceKey1, octetSequenceKey2);
    }

    private static void jwkSet(String kid, JWSAlgorithm alg, KeyType type, JWK... jwk) throws KeySourceException {

        JWKSet jwkSet = new JWKSet(List.of(jwk));
        JWKSource<SecurityContext> jwkSource = (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);

        JWKMatcher jwkMatcher = new JWKMatcher.Builder()
                .keyType(type)
                .keyID(kid)
                .keyUses(KeyUse.SIGNATURE)
                .algorithms(alg)
                .build();

        JWKSelector jwkSelector = new JWKSelector(jwkMatcher);
        List<JWK> jwks = jwkSource.get(jwkSelector, null);

        if (!jwks.isEmpty()) {

            JWK jwk1 = jwks.get(0);

            KeyType keyType = jwk1.getKeyType();
            System.out.println("keyType = " + keyType);

            String keyID = jwk1.getKeyID();
            System.out.println("keyID = " + keyID);

            Algorithm algorithm = jwk1.getAlgorithm();
            System.out.println("algorithm = " + algorithm);

        }

        System.out.println("jwks = " + jwks);
    }
    
    @Test
    void genTest() throws Exception {
        JWTGenerator jwtGenerator = new JWTGenerator("id");

        SignedJWT signedJWT = jwtGenerator.generateJWT("issuer", "subject", 60);

        String jwt = signedJWT.serialize();

        System.out.println("signedJWT = " + jwt);

        String publicKey = jwtGenerator.getPublicKey();

        System.out.println("publicKey = " + publicKey);

        byte[] decode = Base64.getDecoder().decode(publicKey);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decode);
        RSAPublicKey resPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);

        RSAKey rsaKey = new RSAKey.Builder(resPublicKey).build();

        signedJWT = SignedJWT.parse(jwt);

        JWSVerifier verifier = new RSASSAVerifier(rsaKey.toPublicJWK());

        System.out.println("signedJWT.verify(verifier) = " + signedJWT.verify(verifier));
    }

    @Test
    void genPEMTest() throws Exception {
        JWTGenerator jwtGenerator = new JWTGenerator("id");

        SignedJWT signedJWT = jwtGenerator.generateJWT("issuer", "subject", 60);

        String jwt = signedJWT.serialize();

        System.out.println("signedJWT = " + jwt);

        PublicKey publicKey = jwtGenerator.getRsaJWK().toPublicKey();

        StringWriter sw = new StringWriter();

        try (JcaPEMWriter writer = new JcaPEMWriter(sw)) {
            writer.writeObject(publicKey);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(sw);

        JWK jwk = RSAKey.parseFromPEMEncodedObjects(sw.toString());

        signedJWT = SignedJWT.parse(jwt);

        JWSVerifier verifier = new RSASSAVerifier(jwk.toRSAKey());

        System.out.println("signedJWT.verify(verifier) = " + signedJWT.verify(verifier));
    }

    @Test
    void keyTest() throws Exception {

        String jwt = "eyJraWQiOiJpZCIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJpc3N1ZXIiLCJzdWIiOiJzdWJqZWN0IiwiZXhwIjoxNjc3MTM2MzAyfQ.XS0ky6jMp7BSxwTlqecLFqwwuslCJamCuFG9WKE955dMVYdQGVcDT3XuFO9gQC6FV9OtCIYgnMBIdoH_qpmNUiSomevMpsEyZJB-B--eMAAuMTsi3DSjRub4YlSYN2iiRYvdHHSbhS2K_P2HoxUFlLTMY2M4CG1HpRMJxL92DRthiWLnIVMhGY2PUZ3AnJXxqg8BCP-D9wepNzE1hN5fcXlsg9NJo6mqqbI1JM9_VvFVd81ZfLBt436pJs2G7bHJn0Nv8-efVsVazcaDrSKsSTr1XavryGzU-vmYOx0IUWctbH5zDU4TN73nlso6XbUmA5FuixWeHBqrqEVBcPD1SQ";

        System.out.println("signedJWT = " + jwt);

        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyQpf88HpsghAxzz+Q2ZvtJlETpb5NzQN97IfiO6lTe8pdIWfUgR9vxXjyssLLPvECAA5fNLILdY02+ruqWn3oHVh09nlsD2i6V2viZegnBLGI1VxSnKAWIEc+tEJ7ly9vahRAbWQThJYsfzJQ25P1Ak1sxOMUEuwK1YrfmpB2eqKJI0ifNq4Ma4nX9Y/0xSRh7O7/C9KsmXHH9ur3N3+LCDIlqkcMQmLu54S9oif1p4/P2YqqGh5jE6JVogj2HS937wvGyw83LPV9yF4t4pmUj2zJyMOYvpYrD4RD/s7eNov4tG6T6HBuXXyBvFAxxtzWIFo5DRloBtABhqFjhPZrwIDAQAB";

        System.out.println("publicKey = " + publicKey);

        byte[] decode = Base64.getDecoder().decode(publicKey);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decode);
        RSAPublicKey resPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);

        RSAKey rsaKey = new RSAKey.Builder(resPublicKey).build();

        SignedJWT signedJWT = SignedJWT.parse(jwt);

        JWSVerifier verifier = new RSASSAVerifier(rsaKey.toPublicJWK());

        System.out.println("signedJWT.verify(verifier) = " + signedJWT.verify(verifier));
    }
}
