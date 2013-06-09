package lakeskysea;

/**
 * The Product class defines information of a single product
 * @author Sky
 *
 */
public class Product {
    private String name;
    private String price;
    private String vendor;
    
    /**
     * Create a new product information object
     * @param productName
     * @param productPrice
     * @param productVendor
     */
    public Product(String productName, String productPrice, String productVendor) {
        name = productName;
        price = productPrice;
        vendor = productVendor;
    }
    
    /**
     * Override toString() method to implement 'pretty print'
     */
    @Override
    public String toString() {
        return "name:\t\t" + name + "\nprice:\t\t" + price + "\nvendor:\t\t" + vendor + "\n";
    }
}
