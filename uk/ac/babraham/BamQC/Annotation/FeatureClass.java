/**
 * Copyright Copyright 2014 Simon Andrews
 *
 *    This file is part of BamQC.
 *
 *    BamQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    BamQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with BamQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package uk.ac.babraham.BamQC.Annotation;

import java.util.Hashtable;
import java.util.Iterator;

import net.sf.samtools.SAMRecord;


public class FeatureClass {

	private AnnotationSet annotationSet;

	private Hashtable<String, FeatureSubclass> subClasses = new Hashtable<String, FeatureSubclass>();
	
	
	public FeatureClass (AnnotationSet a) {
			annotationSet = a;
	}
	
	public void addFeature (Feature f) {
		if (! subClasses.containsKey(f.subclass())) {
			subClasses.put(f.subclass(), new FeatureSubclass(annotationSet));
		}
		
		subClasses.get(f.subclass()).addFeature(f);
		
	}
	
	public void processSequence (SAMRecord r) {
		// Just pass this on to all of the subclasses
		
		Iterator<FeatureSubclass> it = subClasses.values().iterator();
		
		while (it.hasNext()) {
			it.next().processSequence(r);
		}
	}
	
	public String [] getSubclassNames () {
		return subClasses.keySet().toArray(new String[0]);
	}
	
	public FeatureSubclass getSubclassForName(String name) {
		if (subClasses.containsKey(name)) {
			return subClasses.get(name);
		}
		return null;
	}
	
}
