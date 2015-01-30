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

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author markus
 * 
 */
public class SciNameIterator implements Iterator<SciName>, Iterable<SciName> {
  protected final Logger log = LoggerFactory.getLogger(SciNameIterator.class);
  private final TokenStream tokens;
  private CharTermAttribute termAtt;
  private SciNameAttribute sciNameAtt;
  private OffsetAttribute offsetAtt;
  private boolean hasNext;
  private SciName nextName = new SciName();

  public SciNameIterator(TokenStream tokens) {
    super();
    this.tokens = tokens;
    termAtt = tokens.getAttribute(CharTermAttribute.class);
    sciNameAtt = tokens.getAttribute(SciNameAttribute.class);
    offsetAtt = tokens.getAttribute(OffsetAttribute.class);
    nextName();
  }

  /*
   * (non-Javadoc)
   * @see java.util.Iterator#hasNext()
   */
  @Override
  public boolean hasNext() {
    return hasNext;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Iterable#iterator()
   */
  @Override
  public Iterator<SciName> iterator() {
    return this;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Iterator#next()
   */
  @Override
  public SciName next() {
    SciName n = nextName;
    nextName();
    return n;
  }

  private void nextName() {
    nextName.scientificName = termAtt.toString();
    nextName.verbatimName = sciNameAtt.getCitation();
    nextName.score = sciNameAtt.getScore();
    nextName.offsetStart = offsetAtt.startOffset();
    nextName.offsetEnd = offsetAtt.endOffset();
    nextName.novum = sciNameAtt.isNovum();
    // increase token stream after sciname was build as the first token was increased in the constructor already
    try {
      hasNext = tokens.incrementToken();
    } catch (IOException e) {
      log.error("Cant incremement token stream", e);
      hasNext = false;
    }
  }

  /*
   * (non-Javadoc)
   * @see java.util.Iterator#remove()
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing SciNames not supported");
  }
}
