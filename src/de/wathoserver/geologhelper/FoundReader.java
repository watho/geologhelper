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
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class FoundReader {
	File foundDir;
	private File[] founds;

	/**
	 * 
	 * @param rootDir
	 *            Geolog-data directory
	 */
	public FoundReader(File rootDir) throws FileNotFoundException {
		if (rootDir == null || !rootDir.exists() || !rootDir.isDirectory()) {
			throw new FileNotFoundException("Invalid geolog dir: " + rootDir);
		} else {
			foundDir = new File(rootDir, "found");
		}
		
	}

	public void readFoundCaches() throws IOException {
		founds = foundDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		for (File found : founds) {
			addGeocache(new Geocache(new File(found, "cache.txt"), new File(
					found, "note.txt")));
		}
	}

	public int getNumberOfFounds() {
		return founds.length;
	}

	protected abstract void addGeocache(Geocache cache);
}
