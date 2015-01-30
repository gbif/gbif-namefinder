package org.gbif.namefinder.analysis.sciname;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class ScoreThresholdFilter extends TokenFilter {

  private int threshold;
  private SciNameAttribute sciNameAtt;

  public ScoreThresholdFilter(TokenStream input, int threshold) {
    super(input);
    this.threshold = threshold;
    sciNameAtt = addAttribute(SciNameAttribute.class);
  }

  @Override
  public final boolean incrementToken() throws IOException {
    // return the first non-stop word found
    while (input.incrementToken()) {
      // token with payload. Try to convert into SciNamePayload
      if (sciNameAtt.getScore() >= threshold) {
        return true;
      }
    }
    // reached EOS -- return false
    return false;
  }
}
