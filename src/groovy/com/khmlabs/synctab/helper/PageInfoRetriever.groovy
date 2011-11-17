package com.khmlabs.synctab.helper

import org.htmlcleaner.HtmlCleaner
import org.htmlcleaner.TagNode
import com.khmlabs.synctab.Util
import java.nio.charset.Charset
import org.apache.log4j.Logger

/**
 * Retrieves page information by url.
 * Next information is retrieved:
 *  - real page url (post-redirects url)
 *  - page title
 *  - page description
 *  - page favicon url
 */
class PageInfoRetriever {

    private static final Logger log = Logger.getLogger(PageInfoRetriever.class)

    private String pageUrl
    private String pageTitle
    private String pageDescription
    private String pageFavicon

    private Map<String, String> metaAttributes = new HashMap<String, String>()

    public PageInfoRetriever(String url) {
        this.pageUrl = url
    }

    /**
     * Retrieve page information.
     * @return the page information.
     * @throws IOException error to read a page.
     */
    public UrlInfo retrieve() throws IOException {
        final URL pageURL = new URL(pageUrl)
        final URLConnection siteConnection = pageURL.openConnection()
        final String headContent = readHeadContent(siteConnection)
        final URL realURL = siteConnection.getURL()

        pageUrl = realURL.toExternalForm()

        final HtmlCleaner cleaner = new HtmlCleaner()
        final TagNode pageData = cleaner.clean(headContent)

        readTitle(pageData)
        retrieveMetadata(pageData)
        retrieveFavicon(realURL, pageData)

        final UrlInfo result = new UrlInfo()

        result.link = getRealUrl()
        result.title = getTitle()
        result.favicon = getFavicon()

        println result.title

        return result
    }

    private String readHeadContent(URLConnection siteConnection) {
        Charset charset = getCharset(siteConnection);
        BufferedReader dis = new BufferedReader(new InputStreamReader(siteConnection.getInputStream(), charset));
        final StringBuffer headContents = new StringBuffer()
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
        return headContents.toString()
    }

    private Charset getCharset(URLConnection siteConnection) {

        try {
            String contentType = siteConnection.getContentType()
            if (contentType) {
                contentType = contentType.toLowerCase()
                String charsetName = Util.extractCharsetName(contentType)
                if (charsetName) {
                    return Charset.forName(charsetName)
                }
            }
        }
        catch (Exception e) {
            log.error("Error to get a charset")
        }

        return Charset.defaultCharset()
    }

    private void retrieveFavicon(URL pageURL, TagNode pageData) {
        final TagNode[] links = pageData.getElementsByName("link", true);
        for (TagNode link: links) {
            if (link.hasAttribute("rel")) {
                final String rel = link.getAttributeByName("rel").toLowerCase();

                if (rel.indexOf("icon") != -1) {
                    String href = link.getAttributeByName("href");
                    if (href != null) {
                        pageFavicon = Util.handleRelativeLink(pageURL, href);
                    }
                }
            }
        }

        if (pageFavicon == null || pageFavicon.length() == 0) {
            pageFavicon = Util.handleRelativeLink(pageURL, "/favicon.ico");
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
