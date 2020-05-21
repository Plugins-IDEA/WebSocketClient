package io.github.whimthen.websocket.utils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionUtil {

	private static Pattern addressPattern;

	public static Pattern getAddressPattern() {
		if (Objects.isNull(addressPattern)) {
			final String regex = "^(ws|wss)://(\\S+)";
			addressPattern = Pattern.compile(regex);
		}
		return addressPattern;
	}

	public static boolean validateAddress(String address) {
		Matcher matcher = getAddressPattern().matcher(address);
		return !matcher.find();
	}

}
