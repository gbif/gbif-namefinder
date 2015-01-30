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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * @author markus
 * 
 */
public class LowerCaseInputStream extends InputStream {
  private BufferedReader br;
  private byte[] bytes;
  private int pos;
  private String encoding;

  public LowerCaseInputStream(InputStream source) {
    this(source, "UTF-8");
  }

  public LowerCaseInputStream(InputStream source, String encoding) {
    super();
    try {
      this.encoding = encoding;
      br = new BufferedReader(new InputStreamReader(source, encoding));
      pollNewLine();
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("Unsupported encoding" + encoding, e);
    }
  }

  /**
   * 
   */
  private void pollNewLine() {
    String line;
    try {
      pos = 0;
      line = br.readLine();
      System.out.println(line);
      if (line == null) {
        bytes = null;
      } else {
        line = line.toLowerCase() + "\n";
        bytes = line.getBytes(encoding);
      }
    } catch (IOException e) {
      System.out.println(e);
      bytes = null;
    }
  }

  /*
   * (non-Javadoc)
   * @see java.io.InputStream#read()
   */
  @Override
  public int read() throws IOException {
    if (bytes == null || bytes.length == pos) {
      pollNewLine();
    }
    // still no luck? EOF
    if (bytes == null) {
      return -1;
    }
    System.out.println(pos);
    byte b = bytes[pos];
    pos++;
    return b;
  }
}
