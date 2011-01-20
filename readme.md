Web Integration Portlet (WIP)
=======================

WIP is an open source Java EE portlet developed by Ippon Technologies under GPL license.
It allows the integration of web pages in a Java EE portail with better fidelity than classical solutions.

Why another Web Integration Portlet ?
-------------------------------------

Others solutions to integrate Web page in a portail lead to lesser quality result or control on the integrated content.

 * Using an iFrame is the best "fidelity" solution but you loose all control on the page and cannot filter for instance what is rendered or in what size it's rendered

 * Others Integration portlets don't work well with modern web pages which append to have a lot of Javascript and CSS and navigation often breaks the hosting portail

Wip allows control and fidelity
-------------------------------
 
The Wip portlet acts like a kind of reverse proxy on the target page. It analyses it and transforms lnks, Stylesheets and Javascript inclusion to bring the best compromise between fidelity and control.

Most of the CSS, Javascript and HTML header information will be correctly managed by WIP at the cost of some little overhead processing.

You'll also be able to intercept processing and get only a part of target page by cropping original content.


To Build
--------
Run :

	mvn clean install

Compatibility
-------------
WIP should be compatible with any JSR 268 compatible portail. We tested it with Liferay 5.2X and 6.0X and GateIn 3.X

Contribution
------------
We releasing this software under GPL license, hopping it will help companies to integrates web site in their portails.
If you find this portlet useful, your contribution to the code is welcome
