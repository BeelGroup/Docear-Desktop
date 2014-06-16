package org.freeplane.plugin.docear.util;



/**
 * @author genzmehr@docear.org
 *
 */
public class Version implements Comparable<Version>, Cloneable {
	private static final String SUFFIX_SEPARATOR = "_";
	private static final String NUMBER_SEPARATOR = ".";

	public static enum VersionSuffix {
		experimental,
		alpha,
		beta,
		stable	
	}
	
	private int hi = 0;
	private int mid = 0;
	private int lo = 0;
	private VersionSuffix suffix = null;
	
	/**
	 * @param version
	 * @exception IllegalVersionFormatException when the version argument could not be parsed correctly
	 */
	public Version(String version) {
		this(parseString(version));
	}
	
	public Version(int hi, int mid, int lo) {
		this(new Object[]{hi, mid, lo, null});
	}
	
	public Version(int hi, int mid, int lo, VersionSuffix suffix) {
		this(new Object[]{hi, mid, lo, suffix});
	}
	
	protected Version(Object[] values) {
		this.hi = (Integer)values[0];
		this.mid = (Integer)values[1];
		this.lo = (Integer)values[2];
		
		if(this.hi < 0 || this.mid < 0 || this.lo < 0) {
			throw new IndexOutOfBoundsException("negative number passed as version string to Version()");
		}
		
		this.suffix = (VersionSuffix)values[3];
	}
	
	public static Version parseVersionString(String str) throws IllegalVersionFormatException {
		Object[] values = parseString(str);
		return new Version((Integer)values[0], (Integer)values[1], (Integer)values[2], (VersionSuffix)values[3]);
	}
	
	private static Object[] parseString(String str) throws IllegalVersionFormatException {
		try {
			Object[] values = new Object[4];
			
			//first separate suffix from numbers
			String[] split = str.split(SUFFIX_SEPARATOR);
			if(split.length > 1) {
				values[3] = parseSuffix(split[1]);
			}
			
			//split number components
			split = split[0].split(NUMBER_SEPARATOR);
		
			int i;
			for (i = 0; i < Math.min(values.length-1, split.length); i++) {
				values[i] = Integer.parseInt(split[i]);
			}
			//initialize all remaining number values with 0
			if(i<3) {
				for (; i < values.length-1; i++) {
					values[i] = 0;
				}
			}
			
			return values;
		}
		catch (Exception e) {
			throw new IllegalVersionFormatException(e);
		}		
	}
	
	private static VersionSuffix parseSuffix(String string) {
		return VersionSuffix.valueOf(string);
	}

	public int getHighNumber() {
		return hi;
	}
	
	public int getMiddleNumber() {
		return mid;
	}
	
	public int getLowNumber() {
		return lo;
	}
	
	public VersionSuffix getSuffix() {
		return suffix;
	}
	
	public boolean equals(Object o) {
		if(o instanceof Version) {
			return compareTo((Version) o) == 0;
		}
		return false;
	}
	
	@Override
	public int compareTo(Version other) {
		if(hi > other.getHighNumber()) {
			return 4;
		}
		if(hi < other.getHighNumber()) {
			return -4;
		}
		
		if(mid > other.getMiddleNumber()) {
			return 3;
		}
		if(mid < other.getMiddleNumber()) {
			return -3;
		}
		
		if(lo > other.getLowNumber()) {
			return 2;
		}
		if(lo < other.getLowNumber()) {
			return -2;
		}
		//no suffix is considered a stable version
		int m = VersionSuffix.stable.ordinal();
		int o = VersionSuffix.stable.ordinal();
		
		if(suffix != null) {
			m = suffix.ordinal();
		}
		if(other.getSuffix() != null) {
			o = other.getSuffix().ordinal();
		}
		
		if(m > o) {
			return 1;
		}
		
		if(m < o) {
			return -1;
		}
		
		return 0;
	}
		
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(hi);
		sb.append(NUMBER_SEPARATOR);
		sb.append(mid);
		sb.append(NUMBER_SEPARATOR);
		sb.append(lo);
		if(suffix != null) {
			sb.append(SUFFIX_SEPARATOR);
			sb.append(suffix.name());
		}
		return sb.toString();
	}
	
	public static class IllegalVersionFormatException extends RuntimeException {
		
		private static final long serialVersionUID = -894663791757608858L;

		IllegalVersionFormatException(String msg) {
			super(msg);
		}
		
		IllegalVersionFormatException(Throwable cause) {
			super(cause);
		}
		
	}

	
}
