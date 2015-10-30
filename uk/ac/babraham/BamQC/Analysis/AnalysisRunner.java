/**
 * Copyright Copyright 2010-14 Simon Andrews
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
package uk.ac.babraham.BamQC.Analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.BamQCConfig;
import uk.ac.babraham.BamQC.AnnotationParsers.AnnotationParser;
import uk.ac.babraham.BamQC.AnnotationParsers.GFF3AnnotationParser;
import uk.ac.babraham.BamQC.AnnotationParsers.GTFAnnotationParser;
import uk.ac.babraham.BamQC.AnnotationParsers.GenomeParser;
import uk.ac.babraham.BamQC.DataTypes.Genome.AnnotationSet;
import uk.ac.babraham.BamQC.Dialogs.ProgressTextDialog;
import uk.ac.babraham.BamQC.Modules.QCModule;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;
import uk.ac.babraham.BamQC.Sequence.SequenceFormatException;


public class AnalysisRunner implements Runnable {
	
	private static Logger log = Logger.getLogger(AnalysisRunner.class);	

	private SequenceFile file;
	private QCModule [] modules;
	private List<AnalysisListener> listeners = new ArrayList<AnalysisListener>();
	private int percentComplete = 0;
	
	public AnalysisRunner (SequenceFile file) {
		this.file = file;
	}
	
	public void addAnalysisListener (AnalysisListener l) {
		if (l != null && !listeners.contains(l)) {
			listeners.add(l);
		}
	}

	public void removeAnalysisListener (AnalysisListener l) {
		if (l != null && listeners.contains(l)) {
			listeners.remove(l);
		}
	}

	
	public void startAnalysis (QCModule [] modules) {
		this.modules = modules;
		for (int i=0;i<modules.length;i++) {
			modules[i].reset();
		}
		AnalysisQueue.getInstance().addToQueue(this);
	}

	@Override
	public void run() {

		Iterator<AnalysisListener> i = listeners.iterator();
		while (i.hasNext()) {
			i.next().analysisStarted(file);
		}

		
		AnnotationSet annotationSet = null;

		if(BamQCConfig.getInstance().genome != null) {
			ProgressTextDialog ptd = new ProgressTextDialog("");
			GenomeParser parser = new GenomeParser();
			parser.addProgressListener(ptd);
			
			try {
				parser.parseGenome(BamQCConfig.getInstance().genome);
			} catch (Exception e) {
				log.warn("The annotation genome " + BamQCConfig.getInstance().genome + " seems corrupted!");
				Iterator<AnalysisListener> i2 = listeners.iterator();
				while (i2.hasNext()) {
					i2.next().analysisExceptionReceived(file, e);
				}
				return;
			}
			annotationSet = parser.genome().annotationSet();

		} else if (BamQCConfig.getInstance().gff_file != null) {	
				annotationSet = new AnnotationSet();
				
				ProgressTextDialog ptd = new ProgressTextDialog("");
				AnnotationParser parser;
				if (BamQCConfig.getInstance().gff_file.getName().toLowerCase().endsWith("gtf")) {
					parser = new GTFAnnotationParser();
				}
				else {
					parser = new GFF3AnnotationParser();
				}
				parser.addProgressListener(ptd);
				
				try {
					parser.parseAnnotation(annotationSet, BamQCConfig.getInstance().gff_file);
				}
				catch (Exception e) {
					log.warn("The annotation file " + BamQCConfig.getInstance().gff_file.getName() + " seems corrupted!");
					Iterator<AnalysisListener> i2 = listeners.iterator();
					while (i2.hasNext()) {
						i2.next().analysisExceptionReceived(file, e);
					}
					return;
				}
		} else { 
			// use an empty AnnotationSet.
			annotationSet = new AnnotationSet();
		}	
		
		
//		// this is used to test the imported annotation set
//		System.out.println("print chromosomes");
//		Chromosome[] chrs = annotationSet.chromosomeFactory().getAllChromosomes();
//		for(int j=0; j<chrs.length; j++) {
//			System.out.println(chrs[j].name());
//		}
//		System.out.println("print features");
//		Feature[] features = annotationSet.getAllFeatures();
//		for(int j=0; j<features.length; j++) {
//			System.out.println(features[j]);
//		}
		
		
		

		for (int m=0;m<modules.length;m++) {
			modules[m].processFile(file);
		}
		
		int seqCount = 0;
		while (file.hasNext()) {
			seqCount++;
			SAMRecord seq;
			try {
				seq = file.next();
			}
			catch (SequenceFormatException e) {
				i = listeners.iterator();
				while (i.hasNext()) {
					i.next().analysisExceptionReceived(file,e);
				}
				return;
			}
			
			annotationSet.processSequence(seq);
			
			
			for (int m=0;m<modules.length;m++) {
				// This test is redundant and adds complexity. 
				// If the module does not process the sequences, then just call the method processSequence anyway, and leave this method unimplemented. 
				// In the worse case we are doing the same thing by calling the method needsToSeeSequences(). 
				// If the k modules have to parse seq, then we avoid n*k calls of needsToSeeSequences().
				// The parameter passing is by reference and needsToSeeSequences() returns a value anyway. So not a big deal in that direction either.
				//if (modules[m].needsToSeeSequences()) {
					modules[m].processSequence(seq);
				//}
			}
			
			if (seqCount % 1000 == 0) {
				int percent = file.getPercentComplete();
				if (percent >= percentComplete+5) {
					percentComplete = percent;
					i = listeners.iterator();
					while (i.hasNext()) {
						i.next().analysisUpdated(file, seqCount, percentComplete);
					}
					try {
						Thread.sleep(10);
					} 
					catch (InterruptedException e) {}
				}
			}
		}
		
		// Let's flush the residual cache accumulated during the annotation set parsing. 
		annotationSet.flushCache();
		
		
		// Now send the compiled annotation around the modules which 
		// need to see it
		for (int m=0;m<modules.length;m++) {
			// This test is also redundant and adds complexity (although less time consuming than the previous test needsToSeeSequences().
			// If the module does not process the annotationSet, then just call the method anyway, and leave this method unimplemented.
			// In the worse case we are doing the same thing by calling the method needsToSeeAnnotation().
			// If the k modules have to parse AnnotationSet, then we avoid k calls of needsToSeeSequences().
			// The parameter passing is by reference and needsToSeeAnnotation() returns a value anyway. So not a big deal in that direction either.
			//if (modules[m].needsToSeeAnnotation()) {
				modules[m].processAnnotationSet(annotationSet);
			//}
		}
		
		
		i = listeners.iterator();
		while (i.hasNext()) {
			i.next().analysisComplete(file,modules);
		}

	}
	
}
