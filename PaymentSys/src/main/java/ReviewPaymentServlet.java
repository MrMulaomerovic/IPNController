import com.paypal.api.payments.*;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.PayPalRESTException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import javax.servlet.http.*;


@WebServlet("/review_payment")
public class ReviewPaymentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public ReviewPaymentServlet() {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String paymentId = request.getParameter("paymentId");
        String payerId = request.getParameter("PayerID");

        try {
            PaymentServices paymentServices = new PaymentServices();
            Payment payment = paymentServices.getPaymentDetails(paymentId);

            PayerInfo payerInfo = payment.getPayer().getPayerInfo();
            Transaction transaction = payment.getTransactions().get(0);
            ShippingAddress shippingAddress = transaction.getItemList().getShippingAddress();

            request.setAttribute("payer", payerInfo);
            request.setAttribute("transaction", transaction);
            request.setAttribute("shippingAddress", shippingAddress);

            String url = "rewiew.jsp?paymentId=" + paymentId + "&PayerID=" + paymentId;

            request.getRequestDispatcher(url).forward(request, response);

        } catch (PayPalRESTException exception) {
            request.setAttribute("errorMessage", exception.getMessage());
            exception.printStackTrace();
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }

    }
}
