package org.docear.metadata.extractors;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
			
			
			Document doc = response.parse();
			
			Iterator<Element> bibtexLinks = doc.select("a.gs_nta").iterator();
			if(!bibtexLinks.hasNext()){
				System.out.println();
			}
			for(int i = 0; i < maxResults; i++){
				if(bibtexLinks.hasNext()){
					Element bibtexLink = bibtexLinks.next();
					response = getConnection(BaseURL + bibtexLink.attr("href"))						           
					           .cookies(cookies)						           
					           .execute();
					String bibtex = response.body();
					//System.out.println(bibtex); //todo delete
					result.add(new ScholarMetaData(i, bibtex, query));
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
							Response captchaResponse = getConnection(BaseURL + "/sorry/" + formURL)
															.data(formData)
															.ignoreHttpErrors(true)
															.referrer(e.getUrl())
															.cookies(imgCookie)
															.execute();
							
							Map<String, String> cookies = getCookies(cookieFileName);
							cookies.putAll(captchaResponse.cookies());
							saveCookies(cookies, cookieFileName);
							return true;
						}
					}
				}												
			}
		}catch(IOException ex){
			logger.info(e.getMessage(), e);
		}
		return false;
	}

	private Map<String, String> getCookies(String fileName) throws IOException {		
		Map<String, String> cookies = readCookies(fileName);
		if(cookies == null){
			cookies = requestNewCookie(fileName);				
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
