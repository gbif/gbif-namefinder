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

import org.apache.lucene.util.AttributeImpl;

import java.io.Serializable;

/**
 * @author markus
 * 
 */
public class PunctuationAttributeImpl extends AttributeImpl implements PunctuationAttribute, Cloneable, Serializable {
  private static final long serialVersionUID = 132127682L;
  private char punct;

  @Override
  public void clear() {
    punct = ' ';
  }

  @Override
  public void copyTo(AttributeImpl target) {
    PunctuationAttributeImpl t = (PunctuationAttributeImpl) target;
    t.setPunctuation(punct);
  }

  /*
   * (non-Javadoc)
   * @see org.apache.lucene.util.AttributeImpl#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }

    if (other instanceof PunctuationAttributeImpl) {
      PunctuationAttributeImpl o = (PunctuationAttributeImpl) other;
      return o.punct == punct;
    }

    return false;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ecat.lucene.analysis.sciname.PunctuationAttribute#getPunctuation()
   */
  @Override
  public char getPunctuation() {
    return punct;
  }

  /*
   * (non-Javadoc)
   * @see org.apache.lucene.util.AttributeImpl#hashCode()
   */
  @Override
  public int hashCode() {
    return punct;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ecat.lucene.analysis.sciname.PunctuationAttribute#setPunctuation(char)
   */
  @Override
  public void setPunctuation(char punct) {
    this.punct = punct;
  }

}
