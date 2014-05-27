xsdwalker
=========

Traverse a set of XML Schema documents (.xsd) building up a list of
all dependencies/imports.  This list then printed out as an 'uber xsd'
file, suitable for processing by the Java 'xjc' JAXB tool (or its
Ant/Maven equivalent).  Build is via Maven.

Example, using a recent (as of May 2014) release of Mitre's STIX
schema file set (https://stix.mitre.org/language/version1.1.1/stix_v1.1.1.zip):

% xsdwalker.sh [-v] /path/to/stix_v1.1.1/

which produces ./stix_v1.1.1.uber.xsd (and a report file
./stix_v1.1.1.txt).  The verbose flag '-v' prints progress to stdout. Then

% xjc stix_v1.1.1.uber.xsd

To exclude subsets of .xsd files from the uber xsd file, use the -e
option.  An example, relating again to the STIX .xsd bundle:

% xsdwalker.sh [-v] /path/to/stix_v1.1.1/ -e /path/to/stix_v1.1.1/extensions

For the STIX schema set above, we likely have to exclude the
'extensions' .xsd files, else xjc complain about a myriad of issues.

The XSDWalker utility takes the guess work out of deciding which .xsd
files to offer to xjc and which ones to not offer (since they will
produce duplication and xjc complaints).


