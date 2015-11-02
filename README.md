GBIF Name Finder
--------------------------

This is a [Name Finding API](https://code.google.com/p/taxon-name-processing/wiki/NameFindingAPI) implementation based on lucene working with file based dictionaries of known name parts.
A name finder implementation detects scientific organism names in plain text and reports the name together with the location within the full text.
For example the [bioline source document](https://github.com/gbif/gbif-namefinder/blob/master/namefinder-lucene/src/test/resources/sources/bioline/document.txt) contains these [scientific names](https://github.com/gbif/gbif-namefinder/blob/master/namefinder-lucene/src/test/resources/sources/bioline/expected.txt), mostly with an abbreviated genus.

Development on the project has been stalled and it is currently not in use by GBIF!

See also:
 - [PubIndex](https://github.com/gbif/pubindex): a project that indexes scientific names in journal articles using the name finder API
 - https://code.google.com/p/taxon-name-processing/wiki/nameRecognition


### namefinder-lucene
This module contains the core lucene code for finding scientific names in plain text. The main entrance point is the [SciNameAnalyzer class](https://github.com/gbif/gbif-namefinder/blob/master/namefinder-lucene/src/main/java/org/gbif/namefinder/analysis/sciname/SciNameAnalyzer.java) that can be used to extract scientific names from plain text.

### namefinder-ws
The webservice module exposes the lucene analyzer through the standard [Name Finding API](https://code.google.com/p/taxon-name-processing/wiki/NameFindingAPI).
To start up the webservice on port 8080 you simply run mvn from the namefinder-ws module directory. 
The name dictionaries loaded by default from the classpath are taken from the Catalog of Life 2010.

Example calls:
 - <http://localhost:8080/?format=json&type=text&input=AGAD2%20Ageratina%20adenophora%20(Spreng.)%20King%20&%20H.%20Rob.%20crofton%20weed%20NW%20L48%20(I),%20HI%20(I)>
 - <http://localhost:8080/?format=text&type=url&input=https://taxon-name-processing.googlecode.com/svn/trunk/finding/testdata/bacteria/document.txt>

