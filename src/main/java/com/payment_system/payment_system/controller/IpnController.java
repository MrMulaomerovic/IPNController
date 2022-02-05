package com.payment_system.payment_system.controller;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.util.comparator.Comparators;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IpnController {

	private String digistoreSignature(
			String shaPassphrase,
			Map<String, String> parameters,
			boolean convertKeysToUppercase,
			boolean doHtmlDecode) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		convertKeysToUppercase = false;
		doHtmlDecode = false;

		String algorythm = "sha512";
		boolean sortCaseSensetive = true;

		if (shaPassphrase == null || shaPassphrase == "") {
			return "no_signature_passphrase_provided";
		}

		parameters.put("sha_sign", null);
		parameters.put("SHASIGN", null);

		if (convertKeysToUppercase) {
			sortCaseSensetive = false;
		}

		Set<String> keys = parameters.keySet();
		Set<String> keysToSort = new HashSet<>();
		for (String key : keys) {
			keysToSort.add(sortCaseSensetive ? key : key.toUpperCase());
		}

		// $keys_to_sort, SORT_STRING
		keysToSort = keysToSort
				.stream()
				.sorted()
				.collect(Collectors.toSet());
		keys
				.stream()
				.sorted()
				.collect(Collectors.toSet());

		String shaString = "";
		for (String key : keys) {
			String value = parameters.get(key);

			if (doHtmlDecode) {
				value = StringEscapeUtils.unescapeHtml4(value);
			}

			boolean isEmpty = value != null && value != "";
			if (isEmpty) {
				continue;
			}

			String upperkey = convertKeysToUppercase
					? key.toUpperCase()
					: key;

			shaString += upperkey + "=" + value + shaPassphrase;
		}

		String digestString =  new String(DigestUtils.sha512(shaString), Charset.defaultCharset());
		String shaSign = StringUtils.upperCase(digestString);
		return shaSign;
	}

	@GetMapping("/test")
	public ResponseEntity<Void> test() {
		Map<String, String> map = new HashMap<>();
		map.put("sha_sign", "abc");
		map.put("SHASIGN", "ABC");
		try {
			String result = digistoreSignature("passphrase", map, false, false);
			System.out.println(result);
			System.out.println("");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ResponseEntity.ok().body(null);
	}

}
