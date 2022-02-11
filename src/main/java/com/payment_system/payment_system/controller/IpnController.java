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

import lombok.extern.java.Log;

@Log
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

	@PostMapping(path = "/ipn", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<String> ipn(@RequestParam Map<String, String> body) {
		for (Map.Entry entry : body.entrySet())
		{
		    log.info("key: " + entry.getKey() + "; value: " + entry.getValue());
		}
		String event = body.get("event");
		String apiMode = body.get("api_mode");

		if (ipnPassphrase != null || ipnPassphrase != "") {
			String receivedSignature = body.get("sha_sign");
			String expectedSignature = digistoreSignature(ipnPassphrase, body, false, false);

			log.info("receivedSignature: " + receivedSignature);
			log.info("expectedSignature: " + expectedSignature);
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

				// EDIT HERE: Add the Java code to store your order in your database

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

			case "on_payment_missed": {

				String orderId = body.get("order_id");

				boolean isTestMode = apiMode != "live";

				// EDIT HERE: Add the Java code to cancel the subscription with the
				// missed payment. If the payment continues, a new
				// "on_payment" call is run.

				return ResponseEntity.ok("OK");
			}
			case "on refund": {

				String orderId = body.get("order_id");

				boolean isTestMode = apiMode != "live";

				// EDIT HERE: Add the Java code to cancel and undeliver the order.

				return ResponseEntity.ok("OK");
			}

			case "on_chargeback": {

				String orderId = body.get("order_id");

				boolean isTestMode = apiMode != "live";

				// EDIT HERE: Add the Java code to cancel and undeliver the order.

				return ResponseEntity.ok("OK");
			}

			case "on_rebill_resumed": {

				String orderId = body.get("order_id");

				boolean isTestMode = apiMode != "live";

				// EDIT HERE: Add the Java code to handle a resumed rebilling.
				// IMPORTANT: This event does not mean, that a payment has been completed.
				// It just means, the a payment will be tried, if it is due.

				return ResponseEntity.ok("OK");
			}

			case "on_rebill_cancelled": {

				String orderId = body.get("order_id");

				boolean isTestMode = apiMode != "live";

				// EDIT HERE: Add the Java code to handle stopped rebillings.
				// IMPORTANT: This event is sent at the point of time, when the customer's
				// cancellation of therebilling is processed. Please cancel the
				// access to the paid conentent using the "on_payment_missed" event.

				return ResponseEntity.ok("OK");
			}

			case "on_affiliation": {

				String email = body.get("email");
				String digistoreId = body.get("affiliate_name");
				String promolink = body.get("affiliate_link");
				String language = body.get("language");

				String firstName = body.get("address_first_name");
				String lastName = body.get("address_last_name");

				String addressStreet = body.get("address_street_name");
				String addressStreetNo = body.get("address_street_number");
				String addressCity = body.get("address_city");
				String addressState = body.get("address_state");
				String addressZipcode = body.get("address_zipcode");
				String addressPhoneNo = body.get("address_phone_no");

				String productId = body.get("product_id");
				String productName = body.get("product_name");
				String merchantId = body.get("merchant_id");

				boolean isTestMode = apiMode != "live";

				// EDIT HERE: Add the Java code to handle new affiliations

				return ResponseEntity.ok("OK");
			}

			default:

				return ResponseEntity.ok("OK"); // Unknow test

		}

	}
}
