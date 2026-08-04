package com.example.servermanager.web;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public final class TlsCertificateGenerator {
	private static final String PROVIDER = "BC";
	private static final SecureRandom RANDOM = new SecureRandom();

	private TlsCertificateGenerator() {}

	public static void generate(Path privateKeyPath, Path certificatePath) throws Exception {
		installProvider();
		Files.createDirectories(privateKeyPath.getParent());
		Files.createDirectories(certificatePath.getParent());

		KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", PROVIDER);
		generator.initialize(new ECGenParameterSpec("secp256r1"), RANDOM);
		KeyPair keyPair = generator.generateKeyPair();

		Instant now = Instant.now();
		X500Name subject = new X500Name("CN=Server Manager Local Certificate");
		JcaX509v3CertificateBuilder builder =
				new JcaX509v3CertificateBuilder(
						subject,
						new BigInteger(160, RANDOM).abs(),
						Date.from(now.minus(5, ChronoUnit.MINUTES)),
						Date.from(now.plus(3650, ChronoUnit.DAYS)),
						subject,
						keyPair.getPublic());
		builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
		builder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature));
		builder.addExtension(
				Extension.extendedKeyUsage,
				false,
				new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
		builder.addExtension(
				Extension.subjectAlternativeName,
				false,
				new GeneralNames(
						new GeneralName[] {
							new GeneralName(GeneralName.dNSName, "localhost"),
							new GeneralName(GeneralName.iPAddress, "127.0.0.1"),
							new GeneralName(GeneralName.iPAddress, "::1")
						}));

		ContentSigner signer =
				new JcaContentSignerBuilder("SHA256withECDSA")
						.setProvider(PROVIDER)
						.build(keyPair.getPrivate());
		X509CertificateHolder holder = builder.build(signer);
		X509Certificate certificate =
				new JcaX509CertificateConverter().setProvider(PROVIDER).getCertificate(holder);
		certificate.verify(keyPair.getPublic());

		Path temporaryKey = privateKeyPath.resolveSibling(privateKeyPath.getFileName() + ".tmp");
		Path temporaryCertificate =
				certificatePath.resolveSibling(certificatePath.getFileName() + ".tmp");
		try {
			writePem(temporaryKey, keyPair.getPrivate());
			writePem(temporaryCertificate, certificate);
			moveReplacing(temporaryKey, privateKeyPath);
			moveReplacing(temporaryCertificate, certificatePath);
		} finally {
			Files.deleteIfExists(temporaryKey);
			Files.deleteIfExists(temporaryCertificate);
		}
	}

	private static void installProvider() {
		if (Security.getProvider(PROVIDER) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	private static void writePem(Path path, Object object) throws IOException {
		try (Writer writer =
						Files.newBufferedWriter(
								path,
								StandardCharsets.US_ASCII,
								StandardOpenOption.CREATE,
								StandardOpenOption.TRUNCATE_EXISTING,
								StandardOpenOption.WRITE);
				JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
			pemWriter.writeObject(object);
		}
	}

	private static void moveReplacing(Path source, Path target) throws IOException {
		try {
			Files.move(
					source,
					target,
					StandardCopyOption.ATOMIC_MOVE,
					StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException ignored) {
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
