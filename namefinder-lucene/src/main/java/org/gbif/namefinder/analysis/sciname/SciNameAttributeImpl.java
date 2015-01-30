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

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.util.AttributeImpl;

/**
 * @author markus
 * 
 */
public class SciNameAttributeImpl extends AttributeImpl implements SciNameAttribute, Cloneable, Serializable {
  private static final long serialVersionUID = 1321442422L;
  private boolean novum = false;
  private int score = 0;
  private String citation;

  public void appendCitation(String suffix) {
    this.citation = this.citation + suffix;
  }

  @Override
  public void clear() {
    novum = false;
    score = 0;
    citation = null;
  }

  @Override
  public void copyTo(AttributeImpl target) {
    SciNameAttribute t = (SciNameAttribute) target;
    t.setAttributes(novum, score, citation);
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

    if (other instanceof SciNameAttributeImpl) {
      SciNameAttributeImpl o = (SciNameAttributeImpl) other;
      return o.novum == novum && o.score == score && StringUtils.equals(o.citation, citation);
    }

    return false;
  }

  public String getCitation() {
    return citation;
  }

  public int getScore() {
    return score;
  }

  /*
   * (non-Javadoc)
   * @see org.apache.lucene.util.AttributeImpl#hashCode()
   */
  @Override
  public int hashCode() {
    int code = score;
    code = code * 31 + citation.hashCode();
    code = code * 2 - (novum ? 321 : 81);
    return code;
  }

  public boolean isNovum() {
    return novum;
  }

  public void setAttributes(boolean novum, int score, String citation) {
    this.novum = novum;
    this.score = score;
    this.citation = citation;
  }

  public void setCitation(String citation) {
    this.citation = citation;
  }

  public void setNovum(boolean novum) {
    this.novum = novum;
  }

  public void setScore(int score) {
    this.score = score;
  }
}
