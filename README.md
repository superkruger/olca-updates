openLCA – modules
=================
This project provides the update functionality for [openLCA](http://openlca.org). 
Since version version 1.6 the [openLCA application](https://github.com/GreenDelta/olca-app) 
is using this component to update existing databases. 
Updates themselves are written in python and define their meta data in a manifest file. 


Installation
------------
In order to install the update module, you need to have a [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
and [Maven 3](https://maven.apache.org/install.html) installed. [Download](https://github.com/GreenDelta/olca-modules/archive/master.zip) 
the repository (or get it via git), navigate to the root folder and type the 
following command in your console:

	cd  olca-updates
	mvn install

This will build the module from source and install it into your local 
Maven repository. 

License
-------
Unless stated otherwise, all source code of the openLCA project is licensed 
under the [Mozilla Public License, v. 2.0](http://mozilla.org/MPL/2.0/). Please 
see the LICENSE.txt file in the root directory of the source code.
 
