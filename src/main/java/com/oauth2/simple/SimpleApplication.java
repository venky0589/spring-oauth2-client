package com.oauth2.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.oauth2.simple.dto.ServerKey;
import com.oauth2.simple.dto.ServerKeysCollection;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Base64.getEncoder;

@SpringBootApplication

public class SimpleApplication
	//	implements CommandLineRunner
{
	public static Key pub =null;
	public static Key pvt=null;

	public static void main(String[] args)  {
//		try {
//			keyPairGenerator();
//		}catch(Exception e)
//		{
//
//		}
	SpringApplication.run(SimpleApplication.class, args);

		try {
//			keyPairGenerator();
//
			//loadPublicKey("/tmp/oauth2.pub");
			//loadPrivateKey();
			//System.out.println(getPublicKeyJkws("/tmp/test/my_key.pub"));
			//generateToken("Test");
//			//getPrivateKey("/tmp/private_key.der");
//			//getPublicKey("/tmp/public_key.der");
		}  catch (Exception e) {
			e.printStackTrace();
		}


	}
	WireMockServer wireMockServer=null;

	static ObjectMapper mapper=new ObjectMapper();
	@EventListener
	public void handleContextRefreshedEvent(ContextStoppedEvent event)
	{
		WireMock.reset();
		wireMockServer.stop();



	}

	private final static String ID_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
			".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsIm" +
			"p0aSI6ImQzNWRmMTRkLTA5ZjYtNDhmZi04YTkzLTdjNmYwMzM5MzE1OSIsImlhdCI6MTU0M" +
			"Tk3MTU4MywiZXhwIjoxNTQxOTc1MTgzfQ.QaQOarmV8xEUYV7yvWzX3cUE_4W1luMcWCwpr" +
			"oqqUrg";
	//@Override
	public void run(String... args) throws Exception {
		System.out.println("WIremock started");
		wireMockServer = new WireMockServer(wireMockConfig().port(8077)
				.extensions(new ResponseTemplateTransformer(true),new CustomResponseDefTrans())); //No-args constructor will start on port 8080, no HTTPS
		wireMockServer.start();
		wireMockServer.stubFor(get(urlPathMatching("/oauth/authorize?.*"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/html")
						.withBodyFile("login.html")
						.withTransformers("nonce-transformer")
				));

		wireMockServer.stubFor(post(urlPathEqualTo("/login"))
				.willReturn(temporaryRedirect("{{formData request.body 'form' urlDecode=true}}{{{form.redirectUri}}}?code={{{randomValue length=30 type='ALPHANUMERIC'}}}&state={{{form.state}}}")));

		wireMockServer.stubFor(post(urlPathEqualTo("/oauth/token"))
				.willReturn(
//						.withStatus(200)
//						.withHeader("Content-Type", "application/json")
//						.withBody("{\"token_type\": \"Bearer\",\"access_token\":\""+generateToken("test")+"\",\"id_token\":\""+generateToken("test")+"\"}")
						okJson("{}")
								.withTransformers("nonce-transformer")
						//okJson("{\"token_type\": \"Bearer\",\"access_token\":\""+generateToken("test", "test")+"\",\"id_token\":\""+generateToken("test", "test")+"\"}")
				));
		wireMockServer.stubFor(get(urlPathEqualTo("/.well-known/jwks.json"))
				.willReturn(okJson("{\n" +
						"\"keys\": [{\n" +
						"    \"kty\": \"RSA\",\n" +
						"    \"e\": \"AQAB\",\n" +
						"    \"use\": \"sig\",\n" +
						"    \"kid\": \"kPpF21pmMFChqnl75abKxMN_ePADtCW-ofMr5IpS5pA\",\n" +
						"    \"alg\": \"RS256\",\n" +
						"    \"n\": \"mZqUNf5URcBJoBRlu5TmainpOCWK8monMQ5JyuUho-RkVWrO5hyM30PVj5ThN8x43VP1QCKYhMAdV6YftcAKRfkrQFlgK5bHfMHY4rneUqd38E5bVhOy1okizkzzZm41JJEsjHnI05Qg4lPoirCwGJ-IMk5LdmztfquSnSDYfEmA6NsFVnzo0FZcQXRzZnFbnwtBbCmJgeL6z3M8TJ4qlgKab6wl4ufX99IsedyrhHsD239TK1jjcgKZ-u-KahVdJ0sntMmFKm7rNMXnlrOKrvPTM81MhhXo06lW-Eo_LmWOVuKK_v4dMzuhWP4OkNE8wWa5DGFuYTtZePDjyB8SRw\"\n" +
						"}]\n" +
						"}\n")));
			//	.willReturn(okJson(mapper.writeValueAsString(getPublicKeyJkws("/tmp/test/certificatename.der")))));

		wireMockServer.stubFor(get(urlPathEqualTo("/userinfo"))
				.willReturn(okJson("{\"sub\":\"krishna@test.com\",\"email\":\"bwatkins@test.com\",\"name\":\"bwatkins@test.com\"}")));


	}

//	public static String HEADER="{\n" +
//			"\"kty\": \"RSA\",\n" +
//			"\"kid\": \"rLU9KJYwgBmNMTrV6wucfWmifqWMdn\",\n" +
//			"\"use\": \"sig\",\n" +
//			"\"alg\": \"RS256\",\n" +
//			"\"n\": \""+Base64.getEncoder().encodeToString("secret".getBytes())+"\",\n" +
//			"\"e\": \"secret\"\n" +
//			"}";

public static String accessToken(String user) throws Exception {
	String jwtToken = Jwts.builder()
			.setSubject(user)
			.setAudience("Test")
			//.setPayload("{\"sub\":\""+user+"\",\"authorization\":\"nothing\"}")
			.setExpiration(Date.from(Instant.now().plusSeconds(36000)))
			.compact();

	return jwtToken;
}
	public static String generateToken(String user, String nonce) throws Exception {
		//ServerKeysCollection header=getPublicKeyJkws("/tmp/test/certificatename.der");

		//String abc=mapper.writeValueAsString(header.getKeys()[0]);
		//System.out.println("HEADER:" +mapper.writeValueAsString(header));
		//Map<String, Object> map = mapper.readValue(abc,Map.class);
		Map<String,Object> header=new HashMap<>();
		header.put("alg","RS256");
		header.put("type","JWT");
		//map.put("n",Base64.getEncoder().encodeToString("secret".getBytes()));
		Map<String,Object> claims=new HashMap<String,Object>();
		claims.put("iss","https://wiremock.org");
		claims.put("iat",Instant.now().toEpochMilli()/1000);
		claims.put("sub",user);
		claims.put("aud","wm");
		if(nonce!=null)
		{
			claims.put("nonce",nonce);
		}
		String jwtToken = Jwts.builder()
				.setSubject(user)
				.setAudience("Test")
				.setHeader(header)
				//.setPayload("{}")
				.setClaims(claims)
				.setExpiration(Date.from(Instant.now().plusSeconds(36000)))
				.signWith(SignatureAlgorithm.RS256, loadPrivateKey())//getPrivateKeyAE("/home/venky/Desktop/Personal/krishna/ui/mock-auth.key"))
				.compact();

		return jwtToken;
	}

//	public static PrivateKey getPrivateKeyAE(String keyFilePath) throws IOException {
//		File privateKeyFile = new File(keyFilePath);
//		PEMParser parser = new PEMParser(new FileReader(privateKeyFile));
//		//PEMKeyPair pemKeyPair = (PEMKeyPair) parser.readObject();
//		//PEMEncryptedKeyPair pemKeyPair = (PEMEncryptedKeyPair) parser.readObject();
//		PrivateKeyInfo infor= (PrivateKeyInfo) parser.readObject();
//
//		return infor.getPrivateKey();
//		//KeyPair kp = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
//		//parser.close();
//		//return kp.getPrivate();
//	}
//	public static void keyPairGenerator() throws NoSuchAlgorithmException, IOException {
//		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
//		kpg.initialize(2048);
//		KeyPair kp = kpg.generateKeyPair();
//		pub = kp.getPublic();
//		pvt = kp.getPrivate();
//		System.err.println("Private key format: " + pvt.getFormat());
//// prints "Private key format: PKCS#8" on my machine
//
//		System.err.println("Public key format: " + pub.getFormat());
//// prints "Public key format: X.509" on my machine
//		String outFile = "/tmp/oauth2";
//		OutputStream out = new FileOutputStream(outFile + ".key");
//		out.write(pvt.getEncoded());
//		out.close();
//
//		out = new FileOutputStream(outFile + ".pub");
//		out.write(("-----BEGIN CERTIFICATE-----\n"+formatCert(getEncoder().encodeToString(pub.getEncoded()))+"-----END CERTIFICATE-----").getBytes());
//		out.close();
//
//	}

	public static RSAPrivateKey readPrivateKey(File file) throws Exception {
		String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());

		String privateKeyPEM = key
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replaceAll(System.lineSeparator(), "")
				.replace("-----END PRIVATE KEY-----", "");
		System.out.println(privateKeyPEM);
		byte[] encoded = Base64.decodeBase64(privateKeyPEM);

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
		return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
	}

	public static PrivateKey loadPrivateKey() throws Exception {
		return readPrivateKey(new File("/home/venky/Desktop/Personal/krishna/ui/private_key.pem"));//		File fl=new File("/tmp/test/my_key");
//		byte[] bytes = Files.readAllBytes(Path.of(fl.getAbsolutePath()));
//
//		/* Generate private key. */
//		PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(getDecoder().decode(bytes));
//		KeyFactory kf = KeyFactory.getInstance("RSA");
//		PrivateKey pvt = kf.generatePrivate(ks);
//		return pvt;

	}
	public static PublicKey loadPublicKey(String paths) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		/* Read all the public key bytes */
		Path path = Paths.get(paths);
		byte[] bytes = Files.readAllBytes(path);

		/* Generate public key. */
		X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PublicKey pub = kf.generatePublic(ks);

		return pub;
	}
//
//	public static PrivateKey getPrivateKey(String filename) throws Exception {
//
//		byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
//
//		PKCS8EncodedKeySpec spec =
//				new PKCS8EncodedKeySpec(keyBytes);
//		KeyFactory kf = KeyFactory.getInstance("RSA");
//		return kf.generatePrivate(spec);
//	}
//
//	public static PublicKey getPublicKey(String filename) throws Exception {
//
//		byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
//
//		X509EncodedKeySpec spec =
//				new X509EncodedKeySpec(keyBytes);
//		KeyFactory kf = KeyFactory.getInstance("RSA");
//		return kf.generatePublic(spec);
//	}

	/*public static ServerKeysCollection getPublicKeyJkws(String publicKeyPath) throws CertificateException, IOException {
//		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//		InputStream inStream=new FileInputStream(publicKeyPath);
//		X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inStream);
//		//Certificate certificate = certificateFactory.generateCertificate(new ClassPathResource()<>).getInputStream());
		//String encodedCert = Base64.getEncoder().encodeToString(certificate.getEncoded());
		String encodedCert= null;
		try {
			encodedCert = new String(Base64.getEncoder().encodeToString(getPublicKey(publicKeyPath).getEncoded()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		ServerKey serverKey = ServerKey.builder()
				.alg("RS256")
				.kty("RSA")
				.use("sig")
				.kid("")
				.x5t("")
				.n("")
				.e("")
				.x5c(new String[]{encodedCert})
				.build();
		return ServerKeysCollection.builder().keys(new ServerKey[]{serverKey}).build();
	}*/
	public static ServerKeysCollection getPublicKeyJkws(String publicKeyPath) throws CertificateException, IOException {
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		Certificate certificate = certificateFactory.generateCertificate(new FileInputStream(publicKeyPath));
		String encodedCert = getEncoder().encodeToString(certificate.getEncoded());
//		if(pub==null)
//		{
//			try {
//				keyPairGenerator();
//			}catch(Exception e)
//			{
//
//			}
//		}
//		String encodedCert = Base64.getEncoder().encodeToString(pub.getEncoded());
		ServerKey serverKey = ServerKey.builder()
				.alg("RS256")
				.kty("RSA")
				.use("sig")
				.kid("")
				.x5t("")
				.n("")
				.e("")
				.x5c(new String[]{encodedCert})
				.build();
		return ServerKeysCollection.builder().keys(new ServerKey[]{serverKey}).build();
	}

	/**
	 * Inserts newline characters every 64 characters in the provided base64 encoded pem string
	 * @param base64EncodedCert the body of the base64 encoded certificate
	 * @return the properly formatted PEM string
	 */
	private static String formatCert(String base64EncodedCert)
	{
		ArrayList<String> certLines = new ArrayList<>();
		while (base64EncodedCert.length() > 64)
		{
			//peel off the next 64 characters
			certLines.add(base64EncodedCert.substring(0, 64));
			base64EncodedCert = base64EncodedCert.substring(64);
		}
		certLines.add(base64EncodedCert);

		for (int i = 0; i < certLines.size(); i++)
		{
			//add newline to the end of each line
			certLines.set(i, certLines.get(i).concat("\n"));
		}

		String pemBody = "";
		for (int i = 0; i < certLines.size(); i++)
		{
			//add all lines back together
			pemBody += certLines.get(i);
		}

		return pemBody;
	}

}
