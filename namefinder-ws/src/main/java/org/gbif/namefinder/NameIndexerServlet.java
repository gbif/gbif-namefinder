/**
 * 
 */
package org.gbif.namefinder;

import org.gbif.namefinder.analysis.sciname.SciName;
import org.gbif.namefinder.analysis.sciname.SciNameAnalyzer;
import org.gbif.namefinder.analysis.sciname.SciNameIterator;
import org.gbif.namefinder.utils.FilterXmlReader;
import org.gbif.utils.HttpUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.lucene.analysis.TokenStream;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NameIndexerServlet extends HttpServlet {
  private final static Logger log = LoggerFactory.getLogger(NameIndexerServlet.class);

  private final static String CALLBACK = "callback";
  private final static String INPUT = "input";
  private final static String TYPE = "type";
  private final static String FORMAT = "format";
  private final static byte THRESHOLD = 25;
  private HttpClient client;
  private final ObjectMapper mapper = new ObjectMapper();
  protected SciNameAnalyzer analyzer;

  @Override
  public void destroy() {
    super.destroy();
  }

  private static String para(HttpServletRequest req, String parameter, String defaultValue) {
    // lookup parameter names case insensitive
    Map<String, String> paramLookup = new HashMap<String, String>();

    Enumeration paramNames = req.getParameterNames();
    while (paramNames.hasMoreElements()) {
      String param = (String) paramNames.nextElement();
      paramLookup.put(param.toLowerCase(), param);
    }
    String normedParam = paramLookup.get(parameter.toLowerCase());
    String p = null;
    if (normedParam != null) {
      p = StringUtils.trimToNull(req.getParameter(normedParam));
    }
    if (p == null) {
      return defaultValue;
    }
    return p;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws WsException {
    String callback = para(req, CALLBACK, null);
    String input = para(req, INPUT, null);
    String type = para(req, TYPE, "url");
    String format = para(req, FORMAT, null);

    PrintWriter out = null;
    Reader contentReader = null;
    try {
      out = resp.getWriter();
      if (StringUtils.trimToNull(input) == null) {
        // resp.sendError(400, msg);
        resp.setContentType("text/html");
        printHelp(out);
      } else {
        if (type.equalsIgnoreCase("url")) {
          // get document from URL. Must be text or html
          HttpGet httpget = new HttpGet(input);
          try {
            // execute
            HttpResponse response = client.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
              String charset = "UTF-8";
              contentReader = new InputStreamReader(entity.getContent(), charset);
              // TODO: check content type to see if we should use FilterXmlReader (html, xml, rdf, etc) or not (plain text)
              // will this cause problems with offset values if the tags are removed ???
              if (false) {
                contentReader = new FilterXmlReader(contentReader);
              } else {
                contentReader = new BufferedReader(contentReader);
              }
            }

          } catch (RuntimeException ex) {

            // In case of an unexpected exception you may want to abort
            // the HTTP request in order to shut down the underlying
            // connection and release it back to the connection manager.
            httpget.abort();
            throw ex;
          }
        } else {
          // pure text in input
          contentReader = new StringReader(input);
        }

        // PROCESS TEXT
        long time = System.currentTimeMillis();
        TokenStream stream = nameTokenStream(contentReader);
        stream.reset();
        SciNameIterator iter = new SciNameIterator(stream);

        if (format != null && format.equalsIgnoreCase("json")) {
          resp.setContentType("application/json");
          streamAsJSON(iter, out, callback);
        } else if (format != null && format.equalsIgnoreCase("xml")) {
          resp.setContentType("text/xml");
          streamAsXML(iter, out);
        } else {
          resp.setContentType("text/plain");
          streamAsText(iter, out);
        }
        stream.end();
        stream.close();
        log.info("Indexing finished in " + (System.currentTimeMillis() - time) + " msecs");
      }

    } catch (IOException e1) {
      log.error("IOException", e1);
      e1.printStackTrace();
    } finally {
      // close reader
      if (contentReader != null) {
        // Closing the reader will close the input stream will trigger http connection release in case of URL input
        try {
          contentReader.close();
        } catch (IOException e) {
          log.error("IOException", e);
        }
      }
      // flush response
      out.flush();
      out.close();
    }

  }

  @Override
  public void init() throws ServletException {
    super.init();
    try {
      // httpclient
      client = HttpUtil.newMultithreadedClient(10000, 10, 10);

      // CoL2010 analyzer
      analyzer = new SciNameAnalyzer(THRESHOLD);
    } catch (Exception e) {
      throw new ServletException("Cannot create IndexingManager", e);
    }
  }

  private TokenStream nameTokenStream(Reader reader) throws IOException {
    return analyzer.tokenStream(null, reader);
  }

  private void printHelp(PrintWriter out) {
    out.write("<html><body><h2>Name Finder API</h2>" +
        "<dl>" +
        "<dt>" + INPUT + "</dt>" +
        "<dd>The text to be searched, either supplied directly as plain text or indirectly by supplying an accessible" +
        "url pointing to a document to be indexed. Depending on the service the document can be text, html, xml, pdf, doc, xls, ppt or more." +
        "Some formats are hard to extract though and beware of pdfs which only contains scan images, but no text." +
        "</dd>" +

        "<dt>" + TYPE + "</dt>" +
        "<dd>The type of input, currently either plain text or a url to some public online document. Can be any of <span class='param'>text, url</span>." +
        "The service will try to derive the type if none is given." +
        "</dd>" +

        "<dt>" + FORMAT + "</dt>" +
        "<dd>response format. Can be any of <span class='param'>xml, json, text</span></dd>" +

        "<dt>" + CALLBACK + "</dt>" +
        "<dd>optional javascript callback handler to support json-p</dd>" +
        "</dl>" +
        "</body></html>");
  }

  private void streamAsJSON(SciNameIterator iter, PrintWriter out, String callback) throws IOException {

    if (callback != null && callback.length() > 1) {
      out.write(callback + "({\"names\":[\n");
    } else {
      out.write("{\"names\":[\n");
    }

    // new token stream API with attributes in lucene 3
    boolean first = true;
    for (SciName n : iter) {
      if (first) {
        first = false;
      } else {
        out.write(",");
      }
      out.print(mapper.writeValueAsString(n));
    }

    if (callback != null && callback.length() > 1) {
      out.print("]})");
    } else {
      out.print("]}");
    }
  }

  private void streamAsText(SciNameIterator iter, PrintWriter out) throws IOException {
    out.write("\n#ScientificName\tVerbatimString\tScore\tOffsetStart\tOffsetEnd\tNovum");

    for (SciName n : iter) {
      if (n == null) {
        continue;
      }
      out.write("\n" + n.scientificName.replaceAll("[\\t\\r\\n]", " ") + "\t" + n.verbatimName.replaceAll("[\\t\\r\\n]", " ") + "\t" + n.score
          + "\t"
          + n.offsetStart + "\t" + n.offsetEnd + "\t" + n.novum);
    }
  }

  private void streamAsXML(SciNameIterator iter, PrintWriter out) throws IOException {

    out.print("<names xmlns='http://globalnames.org/namefinder' xmlns:dwc='http://rs.tdwg.org/dwc/terms/'>");

    for (SciName n : iter) {
      if (n == null) {
        continue;
      }
      out.write("<name>\n");
      out.write(" <verbatimString>" + StringEscapeUtils.escapeXml(n.verbatimName) + "</verbatimString>\n");
      out.write(" <score>" + n.score + "</score>\n");
      out.write(" <dwc:scientificName>" + StringEscapeUtils.escapeXml(n.scientificName) + "</dwc:scientificName>\n");
      out.write(" <offset start='" + n.offsetStart + "' end='" + n.offsetEnd + "'/>\n");
      out.write("</name>\n");
    }
    out.write("</names>");
  }

}
