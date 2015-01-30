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

package org.gbif.namefinder.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author markus
 * 
 */
public class LowerCaseInputStreamTest {
  @Test
  public void testStream() throws Exception {
    String text = "Help, Asteraceae or is (Felica) or Felis (Felica) foo found. I can't see any of these famous Abies alba anywhere around here, can you? Maybe this is Potentilla vulgaris L. ? You can't be sure, my dear. Paris is a pretty town too, isn't it? They have big numbers of Passer domesticus subsp. domesticus, the most frequent subspecies of Passer domesticus (Linnaeus, 1758)";
    InputStream input = new StringBufferInputStream(text);
    InputStream lowerIn = new LowerCaseInputStream(input, "UTF-8");

    BufferedReader br = new BufferedReader(new InputStreamReader(lowerIn, "UTF-8"));
    assertEquals(
        "help, asteraceae or is (felica) or felis (felica) foo found. i can't see any of these famous abies alba anywhere around here, can you? maybe this is potentilla vulgaris l. ? you can't be sure, my dear. paris is a pretty town too, isn't it? they have big numbers of passer domesticus subsp. domesticus, the most frequent subspecies of passer domesticus (linnaeus, 1758)",
        br.readLine());

  }
}
