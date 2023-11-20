import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


import io.github.bonigarcia.wdm.WebDriverManager;

//@Listeners(MongoListener.class)

public class WebScrap {
	
	//maintain one reference called mongocollection at global level, create collection which will hold all docs
	MongoCollection<Document> webCollection;
	
	WebDriver driver;
	//pre condition to connect to mongodb
	@BeforeSuite
	public void connectMongoDB() {
		
		Logger monoLogger = Logger.getLogger("org.mongodb.driver");
		//create client
		MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
		//database already created in mongodb using use scrapDB command
		MongoDatabase database = mongoClient.getDatabase("scrapDB");
		//create collection
		webCollection = database.getCollection("web");
		//​We can add drop in beforeSuite to flush
		}
	
	@BeforeTest
	public void setUp() {
		WebDriverManager.chromedriver().setup();
		ChromeOptions co = new ChromeOptions();
		co.addArguments("--headless");
		driver = new ChromeDriver(co);
		
	}
	@DataProvider
	public Object[][] getWebData()
	{
		return new Object[][] {
			{"https://www.amazon.com"},
			//{"https://www.walmart.com"},
			{"https://www.flipkart.com"},
			//{"https://www.snapdeal.com"}
			
		};
	}
	
	
	@Test(dataProvider = "getWebData")
	
	public void webScraperTest(String appUrl)
	{
		driver.get(appUrl);
		String url = driver.getCurrentUrl();
		String title = driver.getTitle();
		int linksCount = driver.findElements(By.tagName("a")).size();
		int imagesCount = driver.findElements(By.tagName("img")).size();
		List<WebElement> linksList = driver.findElements(By.tagName("a"));	
		//to stores the attribute values of href in an arraylist
		List<String> linksAttrList = new ArrayList<String>();
		
		List<WebElement> imagesList = driver.findElements(By.tagName("img"));
		List<String> imageSrcList = new ArrayList<String>();
		
		Document d1 = new Document();
		d1.append("url", url);
		d1.append("title", title);
		d1.append("totalLinks", linksCount);
		d1.append("totalImages", imagesCount);
		
		for(WebElement ele: linksList)
		{
			String hrefValue = ele.getAttribute("href");
			linksAttrList.add(hrefValue);
			
		}
		
		d1.append("linksAttribute", linksAttrList);
		
		for(WebElement ele: imagesList)
		{
			String srcValue = ele.getAttribute("src");
			imageSrcList.add(srcValue);
			
		}
		
		d1.append("SrcValue", imageSrcList);
		
		List<Document> docslist = new ArrayList<Document>();
		docslist.add(d1);
		
		webCollection.insertMany(docslist);
		
	}
	
	
	@AfterTest
	public void tearDown()
	{
		driver.quit();
	}
	
	

}
