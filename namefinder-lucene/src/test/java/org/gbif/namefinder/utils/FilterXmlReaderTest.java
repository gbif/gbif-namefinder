package org.gbif.namefinder.utils;

import org.gbif.utils.file.InputStreamUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.LinkedList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class FilterXmlReaderTest {
	  @Test
	  public void testTokenizer() throws Exception {
		String text = "<html><head> <title>Help, Asteraceae</title><meta>donno</meta><meta attr=\"irregular xml\"></head><body id=\"main\">Is it (Felica) or Felis (Felica) foo found yeah<<<<<<<<<<<you dont see this>>>>BUT THIS YOU DO>>>>. Maybe this is Potentilla vulgaris L. ? </body><html>>";
	    LinkedList<String> expected = new LinkedList<String>();
	    BufferedReader br = new BufferedReader(new FilterXmlReader(new StringReader(text)));
	    assertEquals("    Help, Asteraceae  donno    Is it (Felica) or Felis (Felica) foo found yeah >>>BUT THIS YOU DO>>>>. Maybe this is Potentilla vulgaris L. ?   >",br.readLine());
	  }


  @Test
  public void testTokenizerMikesFile() throws Exception {
    InputStreamUtils streamUtils = new InputStreamUtils();
    InputStream in = streamUtils.classpathStream("sources/biebersteiniaceae/document.txt");
    BufferedReader br = new BufferedReader(new FilterXmlReader(new InputStreamReader(in, "UTF8")));
    String line;
    while ((line = br.readLine()) != null) {
      System.out.println(line);
      assertFalse(line.contains(">"));
      assertFalse(line.contains("<"));

    }
  }
}
