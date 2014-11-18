package passio;

import java.io.UnsupportedEncodingException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.util.Arrays;

import javax.crypto.Mac;

import javax.crypto.spec.SecretKeySpec;

public class PassioEntity {

	private String name;
	private String value;

	private byte[] signingKey;

	public PassioEntity(String name, String value, byte[] signingKey) {
		this.name = name;
		this.value = value;
		this.signingKey = signingKey;
	}

	public PassioEntity(String name, byte[] signingKey) {
		this(name, "", signingKey);
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public byte[] getSigningKey() {
		return signingKey;
	}

	public boolean setValue(String newValue, byte[] expectedMac) {
		byte[] actualMac = computeMAC(newValue);

		boolean macMatches = Arrays.equals(expectedMac, actualMac);
		if (!macMatches) return false;

		value = newValue;
		return true;
	}

	public byte[] getMAC() {
		return computeMAC(value);
	}

	private byte[] computeMAC(String value) {
		SecretKeySpec keySpec = new SecretKeySpec(signingKey, "HmacSHA1");
		Mac mac;
		try {
			mac = Mac.getInstance("HmacSHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new PassioException("Cannot calculate HmacSHA1.", e);
		}

		try {
			mac.init(keySpec);
		} catch (InvalidKeyException e) {
			throw new PassioException("Failed to initialize MAC with provided key.", e);
		}

		byte[] valueBytes;
		try {
			valueBytes = value.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new PassioException("Cannot decode value string.", e);
		}

		return mac.doFinal(valueBytes);
	}

}
