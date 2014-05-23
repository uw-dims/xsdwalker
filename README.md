xsdwalker
=========

Traverse a set of XML Schema documents (.xsd) building up a list of
all dependencies/imports.  This list then printed out as an 'uber xsd'
file, suitable for processing by the Java xjc tool.

Example, using a recent release of Mitre's STIX schema files
(http://stix.mitre.org):

% xsdwalker.sh [-v] /directory/containing/stix_v1.1.1/xsdfiles/

which produces ./stix_v1.1.1.uber.xsd (and a report file
./stix_v1.1.1.txt).  The verbose flag '-v' prints progress to stdout. Then

% xjc stix_v1.1.1.uber.xsd

or the Maven/Ant equivalent.

Without this 'pre-processing' of the available .xsd files and their
(recursive) import dependencies, xjc may complain of duplicate
documents, etc.


