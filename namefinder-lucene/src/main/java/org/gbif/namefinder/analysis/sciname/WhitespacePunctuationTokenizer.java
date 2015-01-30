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

import java.io.Reader;

import org.apache.lucene.analysis.util.CharTokenizer;

/**
 * A tokenizer splitting on whitespace or punctuation chars.
 * The char(s) responsible for tokenizing is added to an PunctuationAttribute
 * 
 * @author markus
 * 
 */
public class WhitespacePunctuationTokenizer extends CharTokenizer {
  private PunctuationAttribute punctAtt;

  public WhitespacePunctuationTokenizer(Reader input) {
    super(input);
    punctAtt = addAttribute(PunctuationAttribute.class);
  }

  @Override
  protected boolean isTokenChar(int cint) {
    char c = (char) cint;
    if (Character.isWhitespace(c)) {
      punctAtt.setPunctuation(' ');
      return false;
    } else if (c == '.' || c == ',' || c == ';' || c == ':' || c == '!' || c == '?' || c == '¿' || c == '<' || c == '>') {
      punctAtt.setPunctuation(c);
      return false;
    }
    return true;
  }
}
