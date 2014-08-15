/*******************************************************************************
 * Copyright 2011 WaTho
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.wathoserver.geologhelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

public class Geocache {

	public static final String NAME = "Name";
	public static final String GCID = "GCid";
	public static final String OWNER = "Owner";
	public static final String OWNERID = "Ownerid";
	// public static final String COORDINATES = "Coordinates";
	public static final String LAT = "Lat";
	public static final String LON = "Lon";
	public static final String TYPE = "Type";
	public static final String CONTAINER = "Container";
	public static final String DIFFICULTY = "Difficulty";
	public static final String TERRAIN = "Terrain";
	public static final String HIDDEN = "Hidden";
	public static final String COUNTRY = "Country";
	public static final String STATE = "State";
	public static final String ATTRIBUTES = "Attributes";
	public static final String STATUS = "Status";
	public static final String FOUND = "Found";
	public static final String AUXSORT = "AuxSort";
	public static final String ELEVATION = "Elevation";
	public static final String UP = "Up";
	public static final String CATEGORY = "Category";
	public static final String ALTCATEGORY1 = "AltCategory1";
	public static final String ALTCATEGORY2 = "AltCategory2";
	public static final String ACCESS = "Access";
	public static final String SEARCH = "Search";
	public static final String OVERALLTIME = "OverallTime";
	public static final String JUDGEMENT = "Judgement";
	public static final String FTF = "FTF";
	public static final String OPTIONAL1 = "Optional1";
	public static final String OPTIONAL2 = "Optional2";
	public static final String OPTIONAL3 = "Optional3";
	public static final String NCID = "NCId";
	public static final String OCID = "OCId";
	public static final String OCCACHEID = "OCCacheId";
	public static final String[] CACHE_PROP_KEYS = { NAME, GCID, OWNER, OWNERID, LAT, LON, TYPE, CONTAINER, DIFFICULTY,
		TERRAIN, HIDDEN, COUNTRY, STATE, ATTRIBUTES, STATUS };
	public static final String[] NOTE_PROP_KEYS = { FOUND, AUXSORT, ELEVATION, UP, CATEGORY, ALTCATEGORY1,
			ALTCATEGORY2, ACCESS, SEARCH, OVERALLTIME, JUDGEMENT, FTF, OPTIONAL1, OPTIONAL2, OPTIONAL3, NCID, OCID,
			OCCACHEID };
	// Attributes from cache.txt
	// : 007-19 - Kaiserbad Bansin
	// String name;
	// // : GC1EWDT
	// String gcid;
	// // : Agent-007 und seine Lütte
	// String owner;
	// // 1054879
	// String ownerid;
	// // : N 53° 58.312 E 014° 08.780
	// String coordinatesStr;
	// // Traditional Cache
	// String type;
	// // Micro
	// String container;
	// // 1.5
	// String difficulty;
	// // 1
	// String terrain;
	// // 27.07.2008
	// String hidden;
	// // Germany
	// String country;
	// // Mecklenburg-Vorpommern
	// String state;
	// // Attributes: horses-no, climbing-no, stroller-yes, stealth-yes,
	// // available-yes, bicycles-yes, kids-yes, wheelchair-yes, dogs-yes,
	// // attribute-blank, attribute-blank, attribute-blank
	// String attributes;
	// // ok
	// String status;
	//
	// // Attributes from note.txt
	// // 02.08.2009
	// String foundStr;
	// Date found;
	// // 03
	// String auxSortStr;
	// int auxSort;
	// // hike.gif
	// String accessStr;
	//
	// // Team +ThePDMs
	// String Optional1;

	// File cache.txt
	File cache;
	Properties cacheProps;
	// File note.txt
	File note;
	Properties noteProps;

	boolean changed = false;

	enum accessEnum {
		HIKE, BIKE, CAR, BUS, COUCH
	};

	public Geocache(final File cache, final File note) throws IOException {
		this.cache = cache;
		cacheProps = new Properties();
		final FileInputStream inProps = new FileInputStream(cache);
		cacheProps.load(inProps);
		inProps.close();
		this.note = note;
		noteProps = new Properties();
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(note);
			noteProps.load(fileInputStream);
		} finally {
			fileInputStream.close();
		}

	}

	public String getCacheAttribute(final String key) {
		String value = cacheProps.getProperty(key);
		if (value == null) {
			value = noteProps.getProperty(key);
		}
		if (value == null) {
			throw new IllegalArgumentException("Key " + key + " not found.");
		}
		return value;
	}

	public void setCacheAttribute(final String key, final String value) {
		// System.out.println("Set attr " + key + "=" + value);
		for (final String k : CACHE_PROP_KEYS) {
			if (k.equals(key)) {
				// System.out.println("key found in cache.txt " + key + "="
				// + value);
				changed = changed || !cacheProps.getProperty(key).equals(value);
				cacheProps.setProperty(key, value);
				// FIXME
				try {
					saveChanges();
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
		}
		for (final String k : NOTE_PROP_KEYS) {
			if (k.equals(key)) {
				// System.out.println("key found in note.txt "
				// + (!noteProps.getProperty(key).equals(value)));
				changed = changed || !noteProps.getProperty(key).equals(value);
				noteProps.setProperty(key, value);
				// FIXME
				try {
					saveChanges();
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
		}
	}

	public void saveChanges() throws IOException {
		// Saving of cache.txt still not necessary
		// PrintWriter pw = new PrintWriter(cache, "ISO-8859-15");
		// for (final String key : CACHE_PROP_KEYS) {
		// pw.println(key + ": " + cacheProps.getProperty(key));
		// }
		// pw.close();
		// Cant use prop.store() because of invalid format
		final PrintWriter pw = new PrintWriter(note, "ISO-8859-15");
		for (final String key : NOTE_PROP_KEYS) {
			pw.println(key + ": " + noteProps.getProperty(key));
		}
		pw.close();
	}

	/**
	 * Returns wether this geocache is a new one. (Currently that means the
	 * AuxSort-key is not set)
	 *
	 * @return
	 */
	public boolean isNew() {
		return this.getCacheAttribute(AUXSORT) == null || this.getCacheAttribute(AUXSORT).length() == 0;
	}

	public boolean isChanged() {
		System.out.println(changed);
		return changed;
	}

	public void setChanged(final boolean changed) {
		this.changed = changed;
	}

	public String getName() {
		return getCacheAttribute(NAME);
	}

	public void setName(final String name) {
		setCacheAttribute(NAME, name);
	}
}
