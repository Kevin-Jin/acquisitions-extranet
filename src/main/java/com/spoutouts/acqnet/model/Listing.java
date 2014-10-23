package com.spoutouts.acqnet.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Listing {
	public static class ListingInfo {
		public enum PrivacyLevel {
			TEASER(1), CIM(2);

			private static final Map<Byte, PrivacyLevel> reverseLookup;

			static {
				Map<Byte, PrivacyLevel> mutableReverseLookup = new HashMap<>();
				for (PrivacyLevel priv : values())
					mutableReverseLookup.put(Byte.valueOf(priv.byteValue()), priv);
				reverseLookup = Collections.unmodifiableMap(mutableReverseLookup);
			}

			public static PrivacyLevel valueOf(byte bVal) {
				return reverseLookup.get(bVal);
			}

			private final byte bVal;

			private PrivacyLevel(int val) {
				this.bVal = (byte) val;
			}

			public byte byteValue() {
				return bVal;
			}
		}

		public PrivacyLevel priv;
	}

	public final Map<String, ListingInfo> optInfo;

	public Listing() {
		optInfo = new HashMap<>();
	}

	public void commit() {
		//TODO: update ElasticSearch and SQL
		//separate indices for different PrivacyLevels
	}
}
