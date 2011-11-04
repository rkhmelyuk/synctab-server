package com.khmlabs.synctab.helper

import org.htmlcleaner.HtmlCleaner
import org.htmlcleaner.TagNode

class UrlInfoRetriever {

    private String pageUrl
    private String pageTitle
    private String pageDescription
    private String pageFavicon

    private Map<String, String> metaAttributes = new HashMap<String, String>()

    public UrlInfoRetriever(String url) {
        this.pageUrl = url
    }

    public UrlInfo retrieve() throws IOException {
        final URL pageURL = new URL(pageUrl)
        final URLConnection siteConnection = pageURL.openConnection()

        final StringBuffer headContents = new StringBuffer()
        BufferedReader dis = new BufferedReader(new InputStreamReader(siteConnection.getInputStream()));

        String inputLine;
        while ((inputLine = dis.readLine()) != null) {
            if (inputLine.contains("</head>")) {
                inputLine = inputLine.substring(0, inputLine.indexOf("</head>") + 7);
                inputLine = inputLine.concat("<body></body></html>");
                headContents.append(inputLine + "\r\n");
                break;
            }
            headContents.append(inputLine + "\r\n");
        }

        URL realURL = siteConnection.getURL()
        pageUrl = realURL.toExternalForm()

        String headContentsStr = headContents.toString()
        HtmlCleaner cleaner = new HtmlCleaner()

        TagNode pageData = cleaner.clean(headContentsStr)

        readTitle(pageData)
        retrieveMetadata(pageData)
        retrieveFavicon(realURL, pageData)

        final UrlInfo result = new UrlInfo()

        result.link = getRealUrl()
        result.title = getTitle()
        result.favicon = getFavicon()

        return result
    }

    private void retrieveFavicon(URL pageURL, TagNode pageData) {
        final TagNode[] links = pageData.getElementsByName("link", true);
        for (TagNode link: links) {
            if (link.hasAttribute("rel")) {
                final String rel = link.getAttributeByName("rel").toLowerCase();

                if (rel.indexOf("icon") != -1) {
                    String href = link.getAttributeByName("href");
                    if (href != null) {
                        pageFavicon = handleRelativeHref(pageURL, href);
                    }
                }
            }
        }

        if (pageFavicon == null || pageFavicon.length() == 0) {
            pageFavicon = handleRelativeHref(pageURL, "/favicon.ico");
        }
    }

    private void readTitle(TagNode pageData) {
        final TagNode[] titles = pageData.getElementsByName("title", true)
        if (titles.length > 0) {
            TagNode title = titles[0]
            pageTitle = title.getText().toString()
        }
    }

    private void retrieveMetadata(TagNode pageData) {
        //open only the meta tags
        final TagNode[] metaData = pageData.getElementsByName("meta", true)
        for (TagNode metaElement: metaData) {
            if (metaElement.hasAttribute("property")) {
                final String name = metaElement.getAttributeByName("property").toLowerCase()
                if (supportedOGProperty(name)) {
                    metaAttributes.put(
                            new String(name.substring(3)),
                            metaElement.getAttributeByName("content"))
                }
            }
            else if (metaElement.hasAttribute("name")) {
                final String name = metaElement.getAttributeByName("name").toLowerCase()
                if (supportedOGProperty(name)) {
                    metaAttributes.put(
                            new String(name.substring(3)),
                            metaElement.getAttributeByName("content"))
                }
                else if (name.equals("description")) {
                    pageDescription = metaElement.getAttributeByName("content")
                }
            }
        }
    }

    boolean supportedOGProperty(String name) {
        return (name.equals("og:description") || name.equals("og:title"))
    }

    private String handleRelativeHref(URL pageURL, String href) {
        href = href.trim()
        if (!href.startsWith("http://") && !href.startsWith("https://")) {
            if (!href.startsWith("/")) {
                href = "/" + href
            }
            if (!href.startsWith("//")) {
                // relative
                String path = pageURL.getProtocol() + "://" + pageURL.getHost()

                int port = pageURL.getPort()
                if (port > 0 && port != 80 && port != 443) {
                    path += ":" + Integer.toString(port)
                }

                href = path + href
            }
            else {
                href = "http:" + href;
            }
        }

        return href
    }

    public String getContent(String property) {
        return metaAttributes.get(property)
    }

    public String getRealUrl() {
        return pageUrl
    }

    public String getTitle() {
        String title = getContent("title")
        if (title == null || title.length() == 0) {
            return pageTitle
        }
        return title
    }

    public String getDescription() {
        String description = getContent("description")
        if (description == null || description.length() == 0) {
            return pageDescription
        }
        return description
    }

    public String getFavicon() {
        return pageFavicon
    }
}
