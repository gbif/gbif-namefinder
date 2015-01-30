GBIF Name Finder
--------------------------

This is a name finder implementation based on lucene working with dictionaries of known name parts.

It contains the lucene module and a webservice module with a single servlet that exposes the name finder
according to the [Name Finding API](https://code.google.com/p/taxon-name-processing/wiki/NameFindingAPI).

See also:
 - https://code.google.com/p/taxon-name-processing/wiki/nameRecognition


To start up the webservice on port 8080 you simply run mvn from the namefinder-ws module directory. The name dictionaries
loaded by default from the classpath are taken from the Catalog of Life 2010.

For the future make the dictionaries and port configurable...