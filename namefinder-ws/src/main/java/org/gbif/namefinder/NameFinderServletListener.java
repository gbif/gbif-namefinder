package org.gbif.namefinder;


import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class NameFinderServletListener extends GuiceServletContextListener {

  class NameFinderServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
      serve("*").with(NameIndexerServlet.class);
    }
  }

  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new NameFinderServletModule());
  }

}
