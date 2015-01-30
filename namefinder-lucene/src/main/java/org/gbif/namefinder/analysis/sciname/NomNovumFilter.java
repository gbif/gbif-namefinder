package org.gbif.namefinder.analysis.sciname;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class NomNovumFilter extends TokenFilter {

  private SciNameAttribute sciNameAtt;

  public NomNovumFilter(TokenStream input) {
    super(input);
    sciNameAtt = addAttribute(SciNameAttribute.class);
  }

  /*
   * (non-Javadoc)
   * @see org.apache.lucene.analysis.TokenStream#incrementToken()
   */
  @Override
  public final boolean incrementToken() throws IOException {
    // return the first non-stop word found
    while (input.incrementToken()) {
      // token with payload. Try to convert into SciNamePayload
      if (sciNameAtt.isNovum()) {
        return true;
      }
    }
    // reached EOS -- return false
    return false;
  }

}
