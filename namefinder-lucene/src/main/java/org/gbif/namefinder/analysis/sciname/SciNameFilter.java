package org.gbif.namefinder.analysis.sciname;

import org.gbif.api.vocabulary.Rank;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

public class SciNameFilter extends TokenFilter {

  class NamePart {

    public Rank rank;
    public String marker;
    public String namepart;
  }

  // lucene attributes
  private CharTermAttribute termAtt;
  private OffsetAttribute offsetAtt;
  private SciNameAttribute sciNameAtt;
  private PunctuationAttribute punctAtt;
  // authority terms & list of ambigous terms
  private Collection<String> supraGenera = new HashSet<String>();
  private Collection<String> genera = new HashSet<String>();
  private Collection<String> generaAmbigous = new HashSet<String>();
  private Collection<String> epitheta = new HashSet<String>();
  private Collection<String> epithetaAmbigous = new HashSet<String>();
  private final int maxYear = Calendar.getInstance().get(Calendar.YEAR) + 2;
  // current match
  private StringBuffer verbatim = new StringBuffer();
  private int score;
  private int startOffset = -1;
  private int endOffset = -1;
  private boolean abbreviated = false;
  private String genus;
  private String subgenus;
  private LinkedList<NamePart> parts = new LinkedList<NamePart>();
  // name ended. Force release if any
  private boolean forceRelease = false;
  // patterns
  private static final Pattern RANK_MARKER;

  static {
    String allMarker = "(notho)? *(" + StringUtils.join(Rank.RANK_MARKER_MAP.keySet(), "|") + ")\\.?";
    RANK_MARKER = Pattern.compile("^" + allMarker + "$");
  }

  // indexing state
  private Map<String, String> genusAbbreviations = new HashMap<String, String>();
  private int sciNameCounter = 0;
  private String IGNORE_CHARS = "«»?×\"+()[]";

  public SciNameFilter(TokenStream input, Collection<String> authoritySupraGenera, Collection<String> authorityGenera,
    Collection<String> authorityEpitheta, Collection<String> ambigGenera) {
    super(input);
    // new attribute
    sciNameAtt = addAttribute(SciNameAttribute.class);
    // existing ones
    offsetAtt = getAttribute(OffsetAttribute.class);
    termAtt = getAttribute(CharTermAttribute.class);
    punctAtt = getAttribute(PunctuationAttribute.class);

    // dicts
    this.supraGenera = authoritySupraGenera;
    this.genera = authorityGenera;
    this.generaAmbigous = ambigGenera;
    this.epitheta = authorityEpitheta;
  }


  private void concatVerbatim(String text) {
    if (verbatim.length() > 0) {
      verbatim.append(" ");
    }
    verbatim.append(text);
    if (startOffset < 0) {
      startOffset = offsetAtt.startOffset();
    }
    endOffset = offsetAtt.endOffset();
  }

  /*
   * (non-Javadoc)
   * @see org.apache.lucene.analysis.TokenStream#incrementToken()
   */
  @Override
  public final boolean incrementToken() throws IOException {
    // return the first non-stop word found
    while (input.incrementToken()) {
      // do all lookups with lower case strings
      String normedTerm = normalizeTerm(termAtt.toString());
      // we might get punctuation characters in the beginning or end of the term
      if (punctAtt.getPunctuation() != ' ') {
        forceRelease = true;
      }

      if (genus == null) {
        // we are looking for a new scientific name
        if (genera.contains(normedTerm)) {
          genus = normedTerm;
          keepAbreviatedGenus(normedTerm);
          concatVerbatim(termAtt.toString());
          if (!isCapitalized(termAtt)) {
            score = -10;
          }
          if (!forceRelease) {
            continue;
          }
        } else if (normedTerm.length() <= 2 && Character.isLetter(normedTerm.charAt(0)) && isCapitalized(termAtt)
                   && punctAtt.getPunctuation() != '>' && punctAtt.getPunctuation() != '<') {
          // an abbreviated genus name?
          if (genusAbbreviations.containsKey(normedTerm)) {
            abbreviated = true;
            genus = genusAbbreviations.get(normedTerm);
            concatVerbatim(termAtt.toString() + (punctAtt.getPunctuation() == ' ' ? "" : punctAtt.getPunctuation()));
            // we expect a punctuation char after the abbreviated name, so dont force a release
            forceRelease = false;
            continue;
          }

        } else if (supraGenera.contains(normedTerm)) {
          // suprageneric name. Its always a monomial, so stop here
          genus = normedTerm;
          concatVerbatim(termAtt.toString());
          if (!isCapitalized(termAtt)) {
            score = -10;
          }
          release();
          return true;
        } else if (Rank.inferRank(normedTerm, null, null, null, null) != Rank.UNRANKED
                   && StringUtils.isAlpha(normedTerm) && StringUtils.isAsciiPrintable(normedTerm)) {
          // suprageneric name. Its always a monomial, so stop here
          if (isCapitalized(termAtt)) {
            score = -10;
          } else {
            score = -20;
          }
          genus = normedTerm;
          concatVerbatim(termAtt.toString());
          release();
          return true;
        } else {
          // this token is nothing at all...
          forceRelease = false;
        }
      } else {
        if (subgenus == null && parts.isEmpty() && normedTerm.startsWith("(") && normedTerm.endsWith(")")) {
          // a subgenus in brackets?
          normedTerm = StringUtils.substring(normedTerm, 1, -1).trim();
          if (genera.contains(normedTerm)) {
            subgenus = normedTerm;
            concatVerbatim(termAtt.toString());
            if (!forceRelease) {
              continue;
            }
          }
        } else {
          // an (infra)specific epitheton or marker?
          if (RANK_MARKER.matcher(normedTerm).matches()) {
            NamePart p = new NamePart();
            p.marker = normedTerm;
            parts.add(p);
            // dont force a release in case we have a dot punctuation
            if (punctAtt.getPunctuation() == '.') {
              forceRelease = false;
            }
            concatVerbatim(termAtt.toString() + (punctAtt.getPunctuation() == ' ' ? "" : punctAtt.getPunctuation()));
            if (!forceRelease) {
              continue;
            }
          } else {
            // known epitheton?
            if (epitheta.contains(normedTerm)) {
              // check if last name part was a marker only - in that case reuse
              NamePart p = parts.peekLast();
              if (p == null || p.namepart != null) {
                p = new NamePart();
                parts.add(p);
              }
              p.namepart = normedTerm;
              concatVerbatim(termAtt.toString());
              if (!forceRelease) {
                continue;
              }
            }
          }
        }
        // if we reach here its not part of the name anymore, but at least a genus exists
        // only release abbreviated binomials, not genera on their own
        if (!abbreviated || !parts.isEmpty()) {
          // time to release the name
          release();
          return true;
        }

        // reset
        clearMatches();
      }
    }

    // reached EOS -- return false
    return false;
  }

  /*
    reset the filter state to no matches
  */
  private void clearMatches() {
    abbreviated = false;
    startOffset = -1;
    endOffset = -1;
    verbatim = new StringBuffer();
    genus = null;
    subgenus = null;
    parts.clear();
    score = 0;
    forceRelease = false;
  }

  private boolean isCapitalized(CharTermAttribute term) {
    return StringUtils.capitalize(term.toString()).equals(term.toString());
  }

  private void keepAbreviatedGenus(String genus) {
    genusAbbreviations.put(genus.substring(0, 1), genus);
    genusAbbreviations.put(genus.substring(0, 2), genus);
  }

  private String normalizeTerm(String term) {
    return term.toLowerCase().replaceAll("æ", "ae").replaceAll("œ", "oe");
  }

  private void release() {
    // the interpreted name
    StringBuffer name = new StringBuffer(StringUtils.capitalize(genus));
    if (abbreviated) {
      score += 5;
    } else {
      score += genus.length() * 5;
      if (generaAmbigous.contains(genus)) {
        score -= 25;
      }
    }
    if (subgenus != null) {
      name.append(" (" + StringUtils.capitalize(subgenus) + ")");
      score += 10 + subgenus.length() * 3;
    }
    for (NamePart p : parts) {
      if (p.marker != null) {
        name.append(" " + p.marker + ".");
        score += 25;
      }
      if (p.namepart != null) {
        name.append(" " + p.namepart);
        score += 10 + p.namepart.length() * 3;
      }
    }
    if (score > 100) {
      score = 100;
    } else if (score < 0) {
      score = 0;
    }
    termAtt.setEmpty();
    termAtt.append(name);
    offsetAtt.setOffset(startOffset, endOffset);
    sciNameAtt.setAttributes(false, score, verbatim.toString());
    // reset
    clearMatches();
  }
}
