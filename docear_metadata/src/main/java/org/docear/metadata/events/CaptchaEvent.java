package org.docear.metadata.events;

import java.awt.image.BufferedImage;

import org.docear.metadata.data.MetaDataSource;

public class CaptchaEvent extends MetaDataEvent {
	
	private MetaDataSource source;
	private BufferedImage captcha;
	private String solvedCaptcha;
	private Boolean canceled = false;	
	
	public CaptchaEvent(MetaDataSource source, BufferedImage captcha) {
		super();
		this.source = source;
		this.captcha = captcha;		
	}
	
	public BufferedImage getCaptcha() {
		return captcha;
	}
	public void setCaptcha(BufferedImage captcha) {
		this.captcha = captcha;
	}
	public MetaDataSource getSource() {
		return source;
	}
	public void setSource(MetaDataSource source) {
		this.source = source;
	}

	public String getSolvedCaptcha() {
		return solvedCaptcha;
	}

	public void setSolvedCaptcha(String solvedCaptcha) {
		this.solvedCaptcha = solvedCaptcha;
	}

	public Boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(Boolean canceled) {
		this.canceled = canceled;
	}

}
