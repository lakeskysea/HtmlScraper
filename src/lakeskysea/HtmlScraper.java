package lakeskysea;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Defines a html scraper 
 * @author Sky
 *
 */
public class HtmlScraper {
    private String url = "http://www.sears.com";
    private String queryField = "search";
    private final static UrlValidator urlValidator = new UrlValidator();
    public String keyword = null;

    /**
     * Create a new HtmlScraper instance. 
     * 
     * @param url The target url of the scraper 
     */
    public HtmlScraper(String url) {
        {
            LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        }

        if (urlValidator.isValid(url)) {
            if(url.charAt(url.length() - 1) == '/') {
                url = url.substring(0, url.length() - 1);
            }
            this.url = url;
        } else {
            throw new IllegalArgumentException("input url is not valid");
        }
    }
    
    /**
     * Get a list of product info result given the keyword and page number.
     * 
     * @param keyword The keyword of the product 
     * @param pageNum The page number of the product searching result that need to be scraped.
     * @return A list of product with the information of name, price and vendor.
     */
    public ArrayList<Product> getResults(String keyword, int pageNum) {
        
        ArrayList<Product> result = new ArrayList<Product>();
        
        Document doc = getDocumentByKeyword(keyword);
        
        String queryString = "";
        Elements ele = doc.select("select#pagination option");
        
        if(!ele.isEmpty()) { 
            queryString = ele.first().attr("value");
        } else {
            // no such products
            return result;
        }
        
        
        Pattern p = Pattern.compile("pageNum=\\d+");
        
        Matcher m = p.matcher(queryString);
        ;
        if (m.find()) {
            queryString = m.replaceAll("pageNum="+pageNum);
        }
        
        String urlWithQuery = url + "/" + queryString;
        
        doc = getDocumentByUrl(urlWithQuery);
        
        Elements parent = doc.select("div#cardsHolder div.cardContainer");
        
        for(Element productContainer : parent){
            String productName = "", productPrice = "", productVendor = "";
            
            Elements currElement = productContainer.select(".cardProdTitle a");
            if(!currElement.isEmpty()) { productName = currElement.attr("title"); }
            currElement = productContainer.select(".youPay span.pricing");
            if(!currElement.isEmpty()) { productPrice = currElement.text(); }
            currElement = productContainer.select("div#mrkplc p");
            if(!currElement.isEmpty()) { 
                productVendor = currElement.first().text().replace("Sold by ", "").replace("learn more", ""); 
            } else {
                currElement = productContainer.select("span#vendorName1");
                if(!currElement.isEmpty()) { productVendor = currElement.text().replace("Sold by ", "").replace("learn more", ""); System.out.println("================hahahahahhahahaha"); }
            }
            
            Product product = new Product(productName, productPrice, productVendor);
            result.add(product);
        }
        
        return result;
        
    }
    
    /**
     * Return a document that is the result of searching by a keyword
     * @param keyword The returned document is generated after searched by this keyword 
     * @return A Document containing all the information of this html page.
     */
    public Document getDocumentByKeyword(String keyword) {
        
        List<BasicNameValuePair> params = Arrays.asList(new BasicNameValuePair(queryField, keyword));
        String urlWithQuery = url + "/" + URLEncodedUtils.format(params, "UTF-8");
        
        return getDocumentByUrl(urlWithQuery);
    }
    
    /**
     * Return a document that is the result of accessing the given url
     * @param url The url of the target document
     * @return A document containing all the information of the target html page.
     */
    public Document getDocumentByUrl (String url) {
        Document doc = null;
        
        final WebClient webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        
        try {
            final HtmlPage page = webClient.getPage(url);
            String finalUrl = page.getUrl().toString();
            doc = Jsoup.connect(finalUrl).get();
        } catch (FailingHttpStatusCodeException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Connection failed.");
            e.printStackTrace();
        }
        
        return doc;
    }

    /**
     * Get the total number of result given a searching keyword
     * @param keyword The keyword to be searched in order to get the result
     * @return The number of results. will display as (500+) if greater than 500.
     */
    public String getResultsNumber(String keyword) {

        Document doc = getDocumentByKeyword(keyword);

        return doc.select("a#utilAllProd span.utilNumbProd").text();

    }

    /**
     * Display the number of results of a searching keyword, 
     * or a list of product result information of a searching keyword,
     * depending on the number of input parameter.
     * 
     * @param args
     * Display the number of results of a searching keyword:
     * args[0]: the keyword to be searched
     * 
     * Display the number of product result information of a searching keyword:
     * args[0]: the keyword to be searched
     * args[1]: the page number of the results to be retrieved from website.
     */
    public static void main(String[] args) {
        
        if(args.length != 1 && args.length != 2) {
            System.out.println("Incorrect number of parameters. Please input 1 or 2 parameter, or take a look at the document.");
            return;
        }
        
        HtmlScraper hs = new HtmlScraper("http://www.sears.com");
        
        System.out.println("Initiating HtmlUnit...");
        System.out.println("This could take up to 30 seconds");
        
        if(args.length == 1) {
            // get the number of result. parameter is the searching keyword
            String keyword = args[0];
            String result = hs.getResultsNumber(keyword);
            System.out.println("Total number of results:" + result);
        } else {
            // 2 parameters. display the product result list
            String keyword = args[0];
            Integer pageNum = 1;
            try {
                pageNum = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Second parameter should be an integer.");
                return;
            }
            
            ArrayList<Product> productList = hs.getResults(keyword, pageNum);
            
            if(productList.isEmpty()) {
                System.out.println("no product found.");
                return;
            }
            
            for(Product product : productList) {
                System.out.println(product);
            }
            
        }
    }
}
