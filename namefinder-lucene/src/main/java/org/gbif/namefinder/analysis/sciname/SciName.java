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

/**
 * @author markus
 * 
 */
public class SciName {
  public String scientificName;
  public String verbatimName;
  public int offsetStart;
  public int offsetEnd;
  public boolean novum;
  public int score;

  @Override
  public String toString() {
    return String.format("%s [%s] %s-%s %s%%", scientificName, verbatimName, offsetStart, offsetEnd, score);
  }
}
