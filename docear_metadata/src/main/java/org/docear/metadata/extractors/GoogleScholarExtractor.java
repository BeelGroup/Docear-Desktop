package org.docear.metadata.extractors;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import org.docear.metadata.data.MetaData;
import org.docear.metadata.data.ScholarMetaData;
import org.docear.metadata.data.ScholarMetaData.ScholarSource;
import org.docear.metadata.events.CaptchaEvent;
import org.docear.metadata.events.FetchedResultsEvent;
import org.docear.metadata.events.MetaDataListener;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class GoogleScholarExtractor extends HtmlDataExtractor {
	
	protected static String BaseURL = "http://scholar.google.com";	
	private String language = "en";		
	private String cookieFileName = "GoogleScholarCookie.xml";
	private Boolean triedNewCookie = false;
	
	public enum ScholarConfigKeys implements ExtractorConfigKey{
		LANGUAGE;
	}
	
	public GoogleScholarExtractor(){};
	
	public GoogleScholarExtractor(Map<ExtractorConfigKey, Object> config) throws MalformedConfigException {
		super(config);
		this.readConfig(config);
	}
	
	public GoogleScholarExtractor(Map<ExtractorConfigKey, Object> config, MetaDataListener listener) throws MalformedConfigException {
		super(config, listener);
		this.readConfig(config);
	}
	
	public Collection<MetaData> search(final String query) {
		ArrayList<MetaData> result = new ArrayList<MetaData>();		
		try{
			Map<String, String> cookies = getCookies(cookieFileName);
			Response response = getConnection(BaseURL + "/scholar")
					.data("q", query, "hl" , this.language)
					.cookies(cookies)					
					.execute();
			if(this.debuglogging){
				logger.info("1. Response URL: "  + response.url().toString());
				logger.info("1. Response headers: "  + response.headers().toString());
				logger.info("1. Response body: "  + response.body().toString());
				logger.info("1. Response cookies: "  + response.cookies().toString());
			}
			Document doc = response.parse();

			//File input = new File("C:\\Users\\Anwender\\Desktop\\Neues Textdokument (2).html");
			//Document doc = Jsoup.parse(input, "UTF-8", BaseURL);
			
			Elements captchaElements = doc.select("noscript > iframe");
			
			if(!captchaElements.isEmpty()){
				String captchaUrl = captchaElements.first().attr("abs:src");
				String token = handleReCaptchaRequest(captchaUrl);
				if(token != null && !token.isEmpty()){
					HashMap<String, String> formData = new HashMap<String, String>();
					
					for(Element inputElement : doc.select("input")){	
						Attributes inputAttributes = inputElement.attributes();
						if(inputAttributes.hasKey("value")){
							formData.put(inputAttributes.get("name"), inputAttributes.get("value"));
						}											
					}
					for(Element inputElement : doc.select("textarea")){	
						Attributes inputAttributes = inputElement.attributes();
						formData.put(inputAttributes.get("name"), token);																
					}
					Response captchaResponse = getConnection(BaseURL + "/scholar")
							.data("q", query, "hl" , this.language)
							.data(formData)
							.cookies(cookies)					
							.execute();
					if(this.debuglogging){
						logger.info("5. Response URL: "  + captchaResponse.url().toString());
						logger.info("5. Response headers: "  + captchaResponse.headers().toString());
						logger.info("5. Response body: "  + captchaResponse.body().toString());
						logger.info("5. Response cookies: "  + captchaResponse.cookies().toString());
					}
					doc = captchaResponse.parse();					
				}
			}
			
			Iterator<Element> bibtexLinks = doc.select("a.gs_nta").iterator();
			if(!bibtexLinks.hasNext()){
				System.out.println();
			}
			for(int i = 0; i < maxResults; i++){
				if(bibtexLinks.hasNext()){
					Element bibtexLink = bibtexLinks.next();
					try{
						logger.info("trying bibtex link: " + bibtexLink.attr("href"));
						System.out.println(bibtexLink.attr("href"));
						URL url = new URL(new URL(BaseURL), bibtexLink.attr("href"));
						response = getConnection(url.toString())						           
						           .cookies(cookies)						           
						           .execute();
						String bibtex = response.body();					
						result.add(new ScholarMetaData(i, bibtex, query));					
					} catch (IOException e) {
						System.out.println(e.getMessage());
						logger.info("Exception: " + e.getMessage(), e);
					}
				}
			}			
		}catch(HttpStatusException e){
			logger.info(e.getMessage(), e);
			if(e.getStatusCode() == 503){
				if(handleCaptchaRequest(e)) return search(query);
			}
			else if(e.getStatusCode() == 403 && !triedNewCookie){
				if(requestNewCookie(cookieFileName) != null) return search(query);
			}			
		} catch (IOException e) {
			System.out.println(e.getMessage());
			logger.info(e.getMessage(), e);
		}	
		FetchedResultsEvent event = new FetchedResultsEvent(result);
		for(MetaDataListener listener : this.getListeners()){
			listener.onFinishedRequest(event);
		}
		if(triedNewCookie){
			triedNewCookie = false;
		}
		return result;
	}
	
	private String handleReCaptchaRequest(String captchaUrl){
		try{	
			Response response = getConnection(captchaUrl).ignoreHttpErrors(true).execute();
			if(this.debuglogging){
				logger.info("2. Response URL: "  + response.url().toString());
				logger.info("2. Response headers: "  + response.headers().toString());
				logger.info("2. Response body: "  + response.body().toString());
				logger.info("2. Response cookies: "  + response.cookies().toString());
			}
			Document doc = response.parse();
			
			Elements imageElements = doc.select("center > img");
			
			if(!imageElements.isEmpty()){
				String imageUrl = imageElements.first().attr("abs:src");
				
				Response imgResponse = getConnection(imageUrl).execute();				
				if(this.debuglogging){
					logger.info("3. Response URL: "  + imgResponse.url().toString());
					logger.info("3. Response headers: "  + imgResponse.headers().toString());
					logger.info("3. Response body: "  + imgResponse.body().toString());
					logger.info("3. Response cookies: "  + imgResponse.cookies().toString());
				}
				BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgResponse.bodyAsBytes()));
				
				String captcha = sendCaptchaEvent(img);
				
				if(captcha != null && !captcha.isEmpty()){
					HashMap<String, String> formData = new HashMap<String, String>();
					
					for(Element inputElement : doc.select("input")){	
						Attributes inputAttributes = inputElement.attributes();
						if(inputAttributes.hasKey("value")){
							formData.put(inputAttributes.get("name"), inputAttributes.get("value"));
						}
						else{
							formData.put(inputAttributes.get("name"), captcha);
						}						
					}
					
					Response captchaResponse = getConnection(captchaUrl)
							.data(formData)
							.ignoreHttpErrors(true)							
							.followRedirects(false)
							.execute();
					
					Document captchaDoc = captchaResponse.parse();
					System.out.println();
					Elements tokenElements = captchaDoc.select("textarea");
					if(!tokenElements.isEmpty()){
						if(this.debuglogging){
							logger.info("4. Response URL: "  + captchaResponse.url().toString());
							logger.info("4. Response headers: "  + captchaResponse.headers().toString());
							logger.info("4. Response body: "  + captchaResponse.body().toString());
							logger.info("4. Response cookies: "  + captchaResponse.cookies().toString());
						}
						String token = tokenElements.first().text();
						return token;
					}
					else{
						return handleReCaptchaRequest(captchaUrl);
					}
				}
			}			
		}catch(IOException ex){
			logger.info(ex.getMessage(), ex);
		} 
		return null;
	}

	private boolean handleCaptchaRequest(HttpStatusException e) {
		try{
			Response response = getConnection(e.getUrl()).ignoreHttpErrors(true).execute();	

			final Document doc = response.parse();
			
			Iterator<Element> imgElements = doc.select("img").iterator();
			if(imgElements.hasNext()){
				Element imgElement = imgElements.next();
				if(imgElement.hasAttr("src")){
					String imgURL = imgElement.attr("src");
					Response imgResponse = getConnection(BaseURL + imgURL).execute();
					final Map<String, String> imgCookie = imgResponse.cookies();
					BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgResponse.bodyAsBytes()));
					String captcha = sendCaptchaEvent(img);
					if(captcha != null && !captcha.isEmpty()){
						Iterator<Element> formElements = doc.select("form").iterator();
						if(formElements.hasNext()){
							Element formElement = formElements.next();
							String formURL = "";
							if(formElement.hasAttr("action")){
								formURL = formElement.attr("action");
							}
							HashMap<String, String> formData = new HashMap<String, String>();
							Elements inputElements = formElement.select("input");
							for(Element inputElement : inputElements){
								if(!inputElement.attr("name").equals("captcha")){
									formData.put(inputElement.attr("name"), inputElement.attr("value"));
								}
								else{
									formData.put(inputElement.attr("name"), captcha);
								}
							}
							URI uri = new URI(e.getUrl());							
							Response captchaResponse = getConnection(uri.getScheme() + "://" + uri.getHost() + "/sorry/" + formURL)
															.data(formData)
															.ignoreHttpErrors(true)
															.referrer(e.getUrl())
															.cookies(imgCookie)
															.followRedirects(false)
															.execute();
							if(captchaResponse.statusCode() == 302 && captchaResponse.hasHeader("Location")){
								Map<String, String> cookies = getCookies(cookieFileName);
								Response abuseResponse = getConnection(captchaResponse.header("Location"))
															.ignoreHttpErrors(true)
															.referrer(e.getUrl())
															.cookies(cookies)
															.followRedirects(false)
															.execute();
								Map<String, String> abuseCookies = abuseResponse.cookies();
								cookies.putAll(abuseCookies);
								saveCookies(cookies, cookieFileName);
								System.out.println("Redirect Captcha");
							}
							else{
								Map<String, String> cookies = requestNewCookie(cookieFileName);
								saveCookies(cookies, cookieFileName);
								System.out.println("Normal Captcha");
							}
							return true;
						}
					}
				}												
			}
		}catch(IOException ex){
			logger.info(e.getMessage(), e);
		} catch (URISyntaxException e1) {
			logger.info(e.getMessage(), e);
		}
		return false;
	}

	private String sendCaptchaEvent(BufferedImage img) throws IOException {
		String captcha = null;
		if(getListeners().size() <= 0){
			ImageIO.write(img, "jpg", new File(getPath("captcha.jpg")));						
			System.out.println("Enter Captcha here : ");					
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));					    
			captcha = bufferRead.readLine();
		}
		else{
			CaptchaEvent event = new CaptchaEvent(ScholarSource.GOOGLESCHOLAR, img);
			for(MetaDataListener listener : this.getListeners()){
				listener.onCaptchaRequested(event);
			}
			if(!event.isCanceled() && event.getSolvedCaptcha() != null && !event.getSolvedCaptcha().isEmpty()){
				captcha = event.getSolvedCaptcha();
			}
		}
		return captcha;
	}

	private Map<String, String> getCookies(String fileName) throws IOException {		
		Map<String, String> cookies = readCookies(fileName);
		if(cookies == null){
			cookies = requestNewCookie(fileName);				
		}
		else{
			String gsp = cookies.get("GSP");
			if(!gsp.endsWith(":CF=4")){
				cookies.put("GSP", gsp + ":CF=4"); // :CF=4 enables the export to BibTex Link in the result list
			}
		}
		return cookies;
	}

	private Map<String, String> requestNewCookie(String fileName) {		
		Map<String, String> cookies = null;
		try{
			Response response = getConnection(BaseURL).ignoreHttpErrors(true).execute();					
			cookies = response.cookies();
			String gsp = cookies.get("GSP");
			cookies.put("GSP", gsp + ":CF=4"); // :CF=4 enables the export to BibTex Link in the result list
			saveCookies(cookies, fileName);
		}catch(IOException e){
			logger.info(e.getMessage(), e);
		}
		return cookies;
	}

	@Override
	protected void readConfig(Map<ExtractorConfigKey, Object> config) throws MalformedConfigException{
		super.readConfig(config);
		try{
			for(ExtractorConfigKey key : config.keySet()){
				if(key instanceof ScholarConfigKeys){
					ScholarConfigKeys scholarConfigKey = (ScholarConfigKeys)key;
					switch(scholarConfigKey){				
						case LANGUAGE:
							this.language = (String) config.get(ScholarConfigKeys.LANGUAGE);
							break;			
						
						default:
							break;							
					}
				}
			}
		}catch(ClassCastException e){
			logger.error("Could not cast config parameter.", e);
			throw new MalformedConfigException();
		}
	}

	public Collection<MetaData> call() throws Exception {		
		return search(searchValue);
	}

	@Override
	public void setConfig(Map<ExtractorConfigKey, Object> config) throws MalformedConfigException {		
		super.setConfig(config);
		this.readConfig(config);
	}
	
	
}
