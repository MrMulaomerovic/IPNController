public class OrderDetail {

    //Note that the getter methods return String for currency values because PayPal API requires the amount in String

    public String productName;
    public float subtotal;
    public float shipping;
    public float tax;
    public float total;


    public OrderDetail(String productName, String subtotal, String shipping, String tax, String total) {
        this.productName = productName;
        this.subtotal = Float.parseFloat(subtotal); //ako budes tako koristio request klase neces morati raditi parsiranje
        //takodjer mozes uraditi validaciju preko klasa jer ce ti ovo pukniti ako budes imao tax kao prazan string ili null
        this.shipping = Float.parseFloat(shipping);
        this.tax = Float.parseFloat(tax);
        this.total = Float.parseFloat(total);
    }

    public String getProductName() {
        return productName;
    }

    public String getSubtotal() {
        return String.format("%.2f", subtotal);
    }

    public String getShipping() {
        return String.format("%.2f", shipping);
    }

    public String getTax() {
        return String.format("%.2f", tax);
    }

    public String getTotal() {
        return String.format("%.2f", total);
    }
}
