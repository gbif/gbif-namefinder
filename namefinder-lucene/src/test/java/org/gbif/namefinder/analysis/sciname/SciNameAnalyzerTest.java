/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.namefinder.analysis.sciname;

import org.gbif.utils.file.InputStreamUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 */
public class SciNameAnalyzerTest {

  private static final InputStreamUtils isu = new InputStreamUtils();

  private TokenStream getTokens(Reader input) throws IOException {
    SciNameAnalyzer ana = new SciNameAnalyzer();
    TokenStream tokens = ana.tokenStream(null, input);
    tokens.reset();
    return tokens;
  }
  @Test
  public void testAbbreviatedNames() throws Exception {
    String text =
      "A survey of the Abies conifers found in Europe. A. is not a abbreviated genus. A. alba, A. betula, A.picea and Picea picea is something else.";
    Reader input = new StringReader(text);
    LinkedList<String> expected = new LinkedList<String>();
    expected.add("Abies");
    expected.add("Abies alba");
    expected.add("Abies betula");
    expected.add("Abies picea");
    expected.add("Picea picea");

    TokenStream tokens = getTokens(input);
    SciNameIterator iter = new SciNameIterator(tokens);
    for (SciName sn : iter) {
      //      System.out.println(sn);
      assertEquals(expected.poll(), sn.scientificName);
    }
    tokens.end();
    tokens.close();
  }

  /**
   * Test Biebersteiniaceae eFlora example html that proved to have problems with names found across html tags
   * Source: http://www.efloras.org/florataxon.aspx?flora_id=2&taxon_id=20048
   */
  @Test
  public void testBiebersteiniaceae() throws Exception {
    Reader input = new InputStreamReader(isu.classpathStream("sources/biebersteiniaceae/document.txt"), "UTF-8");
    TokenStream tokens = getTokens(input);
    SciNameIterator iter = new SciNameIterator(tokens);
    int count = 0;
    for (SciName sn : iter) {
      System.out.println(sn);
      count++;
    }
    System.out.println("Biebersteiniaceae names found: " + count);
    assertTrue(count == 14);
    tokens.end();
    tokens.close();
  }

  /**
   * Test bioline html file taken from http://www.bioline.org.br/abstract?id=fb95003
   */
  @Test
  public void testHtml() throws Exception {
    Reader input = new InputStreamReader(isu.classpathStream("sources/bioline/document.txt"), "UTF-8");
    // input = new InputStreamReader(new FileInputStream(new File("/Users/markus/Desktop/bioline-fb95003.html")), "UTF-8");
    TokenStream tokens = getTokens(input);
    SciNameIterator iter = new SciNameIterator(tokens);
    int count = 0;
    int countMugil = 0;
    for (SciName sn : iter) {
      System.out.println(sn);
      count++;
      if (sn.scientificName.startsWith("Mugil ")) {
        countMugil++;
      }
    }
    System.out.println("BIOLINE names found: " + count);
    assertTrue(count == 49);
    assertTrue(countMugil == 12);
    tokens.end();
    tokens.close();
  }

  @Test
  public void testSimpleText() throws Exception {
    System.out.println(StringUtils.isAllUpperCase("G"));
    System.out.println(StringUtils.isAllUpperCase("G"));
    String text =
      "Help, Asteraceae or is (Felinia) or Felis (Felinia) foordi found. I can't see any of these famous Abies alba anywhere around here, can you? Maybe this is Potentilla vulgaris L. ? You can't be sure, my dear. Paris is a pretty town too, isn't it? They have big numbers of Passer domesticus subsp. domesticus, the most frequent subspecies of Passer domesticus (Linnaeus, 1758)";
    Reader input = new StringReader(text);
    LinkedList<String> expected = new LinkedList<String>();
    expected.add("Asteraceae");
    expected.add("Felis (Felinia) foordi");
    expected.add("Abies alba");
    expected.add("Potentilla vulgaris");
    expected.add("Passer domesticus subsp. domesticus");
    expected.add("Passer domesticus");
    TokenStream tokens = getTokens(input);
    SciNameIterator iter = new SciNameIterator(tokens);
    for (SciName sn : iter) {
      //      System.out.println(sn);
      assertEquals(expected.poll(), sn.scientificName);
    }
    tokens.end();
    tokens.close();
  }
}
