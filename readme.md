Web Integration Portlet (WIP)
=======================

WIP is an open source JavaEE portlet developed by Ippon Technologies under GNU Lesser General Public License.
It allows the integration of web applications in a JavaEE portal with greater control on what is rendered and how it is rendered.

Why another Web Integration Portlet ?
-------------------------------------

Others solutions for such integration have some important limitations :

 * Portlets based on iFrame integration are very simple to deploy but you cannot control what is rendered in the remote application and how it is rendered

 * Other open-source reverse-proxy based portlets are not actively maintained or very tightly tied to a specific portal (ie. uPortal)

 * Commercial products come with a high cost and don't offer the same low-level control on portal integration

WIP allows control and fidelity
-------------------------------
 
The WIP portlet acts as a reverse proxy between the target application and the portal. It analyses contents and transforms links, stylesheets and Javascript inclusion to bring the same integration level than with a true portlet application.

Most of the CSS, Javascript and HTML header information will be correctly managed by WIP at the cost of some little overhead processing.

WIP is bundled with Apache HttpComponents 4.2 and takes advantage of its EhCache integration in order to provide the best performances and availability.

You'll also be able to control content processing and specify what part of the application you want to integrate with XSLT or XPath.


Test it yourself
--------
Follow our 30 minutes [tutorial](https://github.com/ippontech/wip/wiki/Tutorial) to see for yourself the main features of WIP !

Compatibility
-------------
WIP should work on any JSR 268 portal. We specifically tested it with Liferay 6.1 and GateIn 3.3

Contribution
------------
We are releasing this software under GNU Lesser General Public License, hoping it will help you to integrate web applications in your portals.
If you find this portlet useful, your contribution to the code is welcomed.
