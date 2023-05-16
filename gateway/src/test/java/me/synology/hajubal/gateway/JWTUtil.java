package me.synology.hajubal.gateway;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 생성, 검증 유틸
 */
@Slf4j
public class JWTUtil {

    private static RSAKey rsaJWK;

    public final String KEY_ID = "JWT_KEY_ID";

    private static final JWTUtil jwtUtil = new JWTUtil();

    private JWTUtil() {
        try {
            rsaJWK = new RSAKeyGenerator(2048)
                .keyID(KEY_ID)
                .generate();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public static JWTUtil getInstance() {
        return jwtUtil;
    }

    /**
     * JWT 생성
     *
     * @param issuer 발생자
     * @param subject 내용
     * @param expirationTime 만료기간 (분)
     * @return
     * @throws JOSEException
     */
    public SignedJWT generateJWT(String issuer, String subject, long expirationTime) throws JOSEException {
        // Create RSA-signer with the private key
        JWSSigner signer = new RSASSASigner(rsaJWK);

        // Prepare JWT with claims set
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(issuer)
                .expirationTime(new Date(new Date().getTime() + expirationTime * 1000 * 60))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.getKeyID()).build(),
                claimsSet);

        // Compute the RSA signature
        signedJWT.sign(signer);

        return signedJWT;
    }

    /**
     * JWT 유효성 검증
     *
     * @param jwt JWT serialized 문자열
     * @return 유효성 여부
     * @throws ParseException jwt serialized 문자열 파싱 예외
     * @throws JOSEException 서명 검증 예외
     */
    public boolean verifyJWT(String jwt) throws ParseException, JOSEException {
        return this.verifyJWT(jwt, rsaJWK.toPublicJWK());
    }

    /**
     * JWT 유효성 검증
     *
     * @param jwt JWT serialized 문자열
     * @param publicKey 공개키
     * @return 유효성 여부
     * @throws ParseException jwt serialized 문자열 파싱 예외
     * @throws JOSEException 서명 검증 예외
     */
    public boolean verifyJWT(String jwt, RSAKey publicKey) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(jwt);

        log.info("VerifyJWT: {}", jwt);

        JWSVerifier verifier = new RSASSAVerifier(publicKey);
        if(!signedJWT.verify(verifier)) {
            log.info("JWT signature verify failure.");
            return false;
        }

        if (!new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime())) {
            log.info("JWT expired.");
            return false;
        }

        return true;
    }

    /**
     * PEM format의 문자열을 JWK로 변환
     *
     * @param publicKeyPEM PME format 문자열
     * @return JWK
     * @throws JOSEException 공개키 추출 예외
     */
    public JWK parseFromPEMEncoded(String publicKeyPEM) throws JOSEException {
        return RSAKey.parseFromPEMEncodedObjects(publicKeyPEM);
    }

    /**
     * RSA key
     *
     * @return RSA key
     */
    public RSAKey getRsaJWK() {
        return rsaJWK;
    }

    /**
     * Base64 인코딩된 public key
     *
     * @return Base64 인코딩된 public key
     * @throws JOSEException 공개키 추출 예외
     */
    public String getPublicKeyEncoded() throws JOSEException {
        return new String(Base64.getEncoder().encode(rsaJWK.toPublicKey().getEncoded()));
    }

    /**
     * 공개키 PEM format
     *
     * @return PEM format 으로 공개키
     * @throws JOSEException 공개키 추출 예외
     */
    public String getPublicKeyPEM() throws JOSEException {
        StringWriter sw = new StringWriter();

        try (JcaPEMWriter writer = new JcaPEMWriter(sw)) {
            writer.writeObject(rsaJWK.toPublicKey());
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return sw.toString();
    }

}
