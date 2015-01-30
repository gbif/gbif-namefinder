GBIF Name Finder
--------------------------

This is a name finder implementation based on lucene working with dictionaries of known name parts.

It contains the lucene module and a webservice module with a single servlet that exposes the name finder
according to the [Name Finding API](https://code.google.com/p/taxon-name-processing/wiki/NameFindingAPI).

See also:
 - https://code.google.com/p/taxon-name-processing/wiki/nameRecognition


To start up the webservice on port 8080 you simply run mvn from the namefinder-ws module directory. The name dictionaries
loaded by default from the classpath are taken from the Catalog of Life 2010.

Example calls:
 - http://localhost:8080/?format=json&type=text&input=AGAD2%20Ageratina%20adenophora%20(Spreng.)%20King%20&%20H.%20Rob.%20crofton%20weed%20NW%20L48%20(I),%20HI%20(I)
 - http://localhost:8080/?format=text&type=url&input=https://taxon-name-processing.googlecode.com/svn/trunk/finding/testdata/bacteria/document.txt


For the future make the dictionaries and port configurable ...