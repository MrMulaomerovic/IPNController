import com.paypal.base.rest.PayPalRESTException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import javax.servlet.http.*;

@WebServlet("authorize_payment")
public class AuthorizePaymentServlet extends HttpServlet {

  //Gledaj da redovno koristis formatter koda - Ctrl + Alt + l komanda (kod ce ti ljepse izgledati)
  //mozes potraziti "Save actions" plugin koji to omogucava da dodas automatsko formatiranje na Save
  private static final long serialVersionUID = 1L;

  //Pogledaj lombok dependency - on pomaze da se izgenerisu sve ove defaultne stvari
  //Imas anotacija @Getter, @Setter, @NoArgConstructor, @AllArgsConstructor
  //To ti pomaze da ne moras rucno dodavati ove sitnice
  public AuthorizePaymentServlet() {

  }

  //Malo istrazi kako da umjesto HttpServletRequest -a koristis model klasu
  //Npr za ovaj request bi imao klasu ProductRequest sa odgovarajucim propertijima
  //https://stackoverflow.com/questions/39136164/whats-the-difference-usage-of-model-and-httpservletrequest-in-springmvccan-htt
  /*
  @Getter
  @Setter
  public class ProductRequest{
    private String product;
    private Double subtotal;
    private String shipping;
    private Double tax;
    private Double total;
  }
  */
  //S klasom ne bi morao raditi rucno mapiranje ovako request.getParameter("name") vec bi imao automatski sve mapirano

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
//protected void doPost(ProductRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String product = request.getParameter("product");
    String subtotal = request.getParameter("subtotal");
    String shipping = request.getParameter("shipping");
    String tax = request.getParameter("tax");
    String total = request.getParameter("total");

    OrderDetail orderDetail = new OrderDetail(product, subtotal, shipping, tax, total);
    //OrderDetail orderDetail = new OrderDetail(request.getProduct(), request.getSubtotal(), request.getShipping(), request.getTax(), request.getTotal());

    try {
      PaymentServices paymentServices = new PaymentServices();
      String approvalLink = paymentServices.authorizePayment(orderDetail);

      response.sendRedirect(approvalLink);
    } catch (PayPalRESTException ex) {
      request.setAttribute("errorMessage", ex.getMessage());
      ex.printStackTrace();
      request.getRequestDispatcher("error.jsp").forward(request, response);
    }

  }

}
