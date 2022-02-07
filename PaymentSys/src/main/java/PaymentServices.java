import com.paypal.api.payments.*;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import java.util.*;

//Generalno ne izgleda lose, moras malo obratiti paznju na nekoliko stvari koje sam naveo, ali sve u svemu nije lose
public class PaymentServices extends Payment {

    //Istrazi .properties fajlove (u spring bootu imaju, vjerovatno imaju i ovdje)
    //Omogucavaju ti da spremis ove vrijednosti u genericki fajl i mozes imati vise okruzenja i vise fajlove (staging, dev, prod)
    //Sve ove fiksne vrijednosti mozes staviti u properties
    private static final String CLIENT_ID = "Ac-xs6R9CuZtKdAQXZmXU7AN5Oe4zXuthBRtYlDxb9ujmViZxCsz-i6z8qDXI0lM9byuYONypd7KV586";
    private static final String CLIENT_SECRET = "EBgH4ka8KR5jcUN-dIsqJlMrW91KgOSMYFjSN1TP3K8fZ32GdI2idMFzzm7QA3Q2r9x5yk0tM6yWtq18";
    private static final String MODE = "live";



    public String authorizePayment(OrderDetail orderDetail)
            throws PayPalRESTException {

        Payer payer = getPayerInformation();
        RedirectUrls redirectUrls = getRedirectURLs();
        List<Transaction> listTransaction = getTransactionInformation(orderDetail);

        //Gledaj da u ovakvim slucajevima koristis konstruktor umjesto settera

        Payment requestPayment = new Payment();
        requestPayment.setTransactions(listTransaction);
        requestPayment.setRedirectUrls(redirectUrls);
        requestPayment.setPayer(payer);
        requestPayment.setIntent("authorize");

        APIContext apiContext = new APIContext(CLIENT_ID, CLIENT_SECRET, MODE);

        Payment approvedPayment = requestPayment.create(apiContext);

        System.out.println("=== CREATED PAYMENT: ====");
        System.out.println(approvedPayment);

        return getApprovalLink(approvedPayment);

    }

    private Payer getPayerInformation() {
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        PayerInfo payerInfo = new PayerInfo();
        payerInfo.setFirstName("Jhon")
                .setLastName("Doe")
                .setEmail("john.doe@comp.com");

        payer.setPayerInfo(payerInfo);

        return payer;
    }

    private RedirectUrls getRedirectURLs() {
        //Ovo takodjer moze biti dodano kroz konstruktor, manje ces koda imati i bit ce preglednije
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("http://localhost:8080/PaymentSys/cancel.jsp");
        redirectUrls.setReturnUrl("http://localhost:8080/PaymentSys/review_payment");

        return redirectUrls;
    }

    //Generalno gledaj da imas sto manje helper metoda, da instanciranje radis kroz konstruktore
    private List<Transaction> getTransactionInformation(OrderDetail orderDetail) {
        Details details = new Details();
        details.setShipping(orderDetail.getShipping());
        details.setSubtotal(orderDetail.getSubtotal());
        details.setTax(orderDetail.getTax());

        Amount amount = new Amount();
        amount.setCurrency("EUR");
        amount.setTotal(orderDetail.getTotal());
        amount.setDetails(details);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(orderDetail.getProductName());

        ItemList itemList = new ItemList();
        List<Item> items = new ArrayList<>();

        Item item = new Item();
        item.setCurrency("EUR");
        item.setName(orderDetail.getProductName());
        item.setPrice(orderDetail.getSubtotal());
        item.setTax(orderDetail.getTax());
        item.setQuantity("1");

        items.add(item);
        itemList.setItems(items);
        transaction.setItemList(itemList);

        List<Transaction> listTransaction = new ArrayList<>();
        listTransaction.add(transaction);

        return listTransaction;
    }

    private String getApprovalLink(Payment approvedPayment) {
        List<Links> links = approvedPayment.getLinks();
        String approvalLink = null;

        for (Links link : links) {
            if (link.getRel().equalsIgnoreCase("approval_url")) {
                approvalLink = link.getHref();
                break;
            }
        }

        return approvalLink;
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        //Konstruktor je uvijek bolje i pravilnija opcija
        Payment payment = new Payment().setId(paymentId);

        APIContext apiContext = new APIContext(CLIENT_ID, CLIENT_SECRET, MODE);

        return payment.execute(apiContext, paymentExecution);
    }

    public Payment getPaymentDetails(String paymentId) throws PayPalRESTException {
        APIContext apiContext = new APIContext(CLIENT_ID, CLIENT_SECRET, MODE);
        return Payment.get(apiContext, paymentId);
    }
}

