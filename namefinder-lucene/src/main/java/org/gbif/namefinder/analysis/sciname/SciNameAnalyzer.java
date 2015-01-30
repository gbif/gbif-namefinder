package org.gbif.namefinder.analysis.sciname;

import org.gbif.utils.file.FileUtils;
import org.gbif.utils.file.InputStreamUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SciNameAnalyzer extends Analyzer {

  protected final Logger log = LoggerFactory.getLogger(SciNameAnalyzer.class);

  public static final String CLASSPATH_PATH = "finderdicts/";
  public static final String SG = "suprageneric.txt";
  public static final String G = "genera.txt";
  public static final String SP = "epitheta.txt";
  public static final String AMB = "genera_ambigous.txt";
  private static final InputStreamUtils isu = new InputStreamUtils();
  private Set<String> supragenera = new HashSet<String>();
  private Set<String> genera = new HashSet<String>();
  private Set<String> epitheta = new HashSet<String>();
  private Set<String> generaAmbigous = new HashSet<String>();

  private final int threshold;
  private static final int DEFAULT_THRESHOLD = 20;

  public SciNameAnalyzer() throws IOException {
    this(DEFAULT_THRESHOLD);
    log.debug("Loaded analyzer dictionaries from classpath");
  }

  public SciNameAnalyzer(int threshold) throws IOException {
    this(isu.classpathStream(CLASSPATH_PATH + SG), isu.classpathStream(CLASSPATH_PATH + G),
      isu.classpathStream(CLASSPATH_PATH + SP), isu.classpathStream(CLASSPATH_PATH + AMB), threshold);
    log.debug("Loaded analyzer dictionaries from classpath");
  }

  public SciNameAnalyzer(File dictionaryFolder) throws IOException {
    this(new FileInputStream(new File(dictionaryFolder, SG)), new FileInputStream(new File(dictionaryFolder, G)),
      new FileInputStream(new File(dictionaryFolder, SP)), new FileInputStream(new File(dictionaryFolder, AMB)), DEFAULT_THRESHOLD);
    log.debug("Loaded analyzer dictionaries from folder " + dictionaryFolder.getAbsolutePath());
  }

  private SciNameAnalyzer(InputStream suprageneric, InputStream genera, InputStream epitheta, InputStream generaAmbigous, int threshold)
    throws IOException {
    this(FileUtils.streamToSet(suprageneric), FileUtils.streamToSet(genera), FileUtils.streamToSet(epitheta),
      FileUtils.streamToSet(generaAmbigous), threshold);
    log.debug("Loaded analyzer dictionaries from input streams");
  }

  public SciNameAnalyzer(Set<String> supragenera, Set<String> genera, Set<String> epitheta, Set<String> generaAmbigous,
    int threshold) {
    this.supragenera = supragenera;
    this.genera = genera;
    this.epitheta = epitheta;
    this.generaAmbigous = generaAmbigous;
    this.threshold = threshold;
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
      Tokenizer source = new WhitespacePunctuationTokenizer(reader);
      TokenStream filter = new SciNameFilter(source, supragenera, genera, epitheta, generaAmbigous);
      filter = new ScoreThresholdFilter(filter, threshold);
      return new TokenStreamComponents(source, filter);
  }

  public int getThreshold() {
    return threshold;
  }

}
