# Getting to Philosophy
Counts the # of clicks to get from a given Wikipedia article to the Philosophy article by following the below rules:

  1. Click on the first non-parenthesized, non-italicized link
  2. Ignore external links, links to the current page, or red links
  3. Stop when reaching "Philosophy", a page with no links or a page that does not exist, or when a loop occurs

Netbeans+Maven project. Uses Hibernate with Postgres to cache results, but if the database is not initialized in the code it will work properly without using the cache. Uses only english wikipedia articles. Accesses wikipedia content through the mediawiki API, which saves bandwidth by allowing us to request only the small part of the article that we need, rather than the entire html page. 

Includes unit tests using Junit and mockito.
