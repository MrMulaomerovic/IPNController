package com.payment_system.payment_system.controller;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IpnController {

	@Value("${digistore.ipn.passphrase}")
	private String ipnPassphrase;

	private String digistoreSignature(
			String shaPassphrase,
			Map<String, String> parameters,
			boolean convertKeysToUppercase,
			boolean doHtmlDecode) {
		convertKeysToUppercase = false;
		doHtmlDecode = false;

		boolean sortCaseSensetive = true;

		if (shaPassphrase == null || shaPassphrase == "") {
			return "no_signature_passphrase_provided";
		}

		parameters.remove("sha_sign");
		parameters.remove("SHASIGN");

		if (convertKeysToUppercase) {
			sortCaseSensetive = false;
		}

		Set<String> keys = parameters.keySet();
		Set<String> keysToSort = new HashSet<>();
		for (String key : keys) {
			keysToSort.add(sortCaseSensetive ? key : key.toUpperCase());
		}

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

			boolean isEmpty = value == null && value == "";
			if (isEmpty) {
				continue;
			}

			String upperkey = convertKeysToUppercase
					? key.toUpperCase()
					: key;

			shaString += upperkey + "=" + value + shaPassphrase;
		}

		String shaSign = DigestUtils.sha512Hex(shaString).toUpperCase();
		return shaSign;
	}

	@PostMapping(path = "/ipn", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
	public ResponseEntity<String> ipn(@RequestParam Map<String, String> body) {
		String event = body.get("event");
		String apiMode = body.get("api_mode");

		if (ipnPassphrase != "") {
			String receivedSignature = body.get("sha_sign");
			String expectedSignature = digistoreSignature(ipnPassphrase, body, false, false);

			if (!receivedSignature.equals(expectedSignature)) {
				return ResponseEntity.badRequest().body("ERROR: invalid sha signature");
			}
		}

		switch (event) {
			case "on_payment": {
				String orderId = body.get("order_id");

				String productId = body.get("product_id");
				String productName = body.get("product_name");
				String billingType = body.get("billing_type");

				switch (billingType) {
					case "single_payment": {
						String numberPayments = "0";
						String paySequenceNo = "0";
						break;
					}
					case "installment": {
						String numberPayments = body.get("order_item_number_of_installments");
						String paySequenceNo = body.get("pay_sequence_no");
						break;
					}
					case "subscription": {
						String numberPayments = "0";
						String paySequenceNo = body.get("pay_sequence_no");
						break;
					}
				}

				String email = body.get("email");
				String firstName = body.get("address_first_name");
				String lastName = body.get("address_last_name");
				String addressStreet = body.get("address_street_name");
				String addressStreetNo = body.get("address_street_number");
				String addressCity = body.get("address_city");
				String addressState = body.get("address_state");
				String addressZipcode = body.get("address_zipcode");
				String addressPhoneNo = body.get("address_phone_no");

				boolean isTestMode = apiMode != "live";

				boolean doTransferMemberShipDataToDigistore = false;
				if (doTransferMemberShipDataToDigistore) {
					return ResponseEntity.ok("OK");
				}

				String username = "some_username";
				String password = "some_password";
				String loginUrl = "http://domain.com/login";
				String thankyouUrl = "http://domain.com/thank_you";

				String showOn = "all"; // e.g.: 'all', 'invoice', 'invoice,receipt_page,order_confirmation_email' -
										// seperate multiple targets by comma
				String hideOn = "invoice"; // e.g.: 'none', 'invoice', 'invoice,receipt_page,order_confirmation_email' -
											// seperate multiple targets by comma

				String headline = "Your access data"; // displayed above the membership access data

				return ResponseEntity.ok("OK"
						+ "thankyou_url: " + username
						+ " password: " + loginUrl
						+ " headline: " + showOn
						+ " hide_on: " + hideOn);
			}
		}

		return ResponseEntity.ok(null);
	}

}
