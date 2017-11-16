package io.transwarp.flume.interceptor;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchAndReplaceAndFilterInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(SearchAndReplaceAndFilterInterceptor.class);

    private final boolean searchAndReplace;
    private final Pattern searchPattern;
    private final String replaceString;
    private final Charset charset;
    private final boolean filterByRow;
    private final Pattern regex;
    private final boolean filterByCol;
    private final String colSeparator;
    private final String index;

    // 构造函数
    private SearchAndReplaceAndFilterInterceptor(boolean searchAndReplace,
                                                 Pattern searchPattern,
                                                 String replaceString,
                                                 Charset charset,
                                                 boolean filterByRow,
                                                 Pattern regex,
                                                 boolean filterByCol,
                                                 String colSeparator,
                                                 String index) {
        this.searchAndReplace = searchAndReplace;
        this.searchPattern = searchPattern;
        this.replaceString = replaceString;
        this.charset = charset;
        this.filterByRow = filterByRow;
        this.regex = regex;
        this.filterByCol = filterByCol;
        this.colSeparator = colSeparator;
        this.index = index;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void close() {

    }

    // 主要逻辑实现
    @Override
    public Event intercept(Event event) {
        if (filterByRow) {
            if (regex.matcher(new String(event.getBody())).find()) {
                if (filterByCol) {
                    String line = new String(event.getBody(), charset);
                    String[] arr1 = line.split(colSeparator);
                    String[] arr2 = index.split(",");
                    if (Integer.parseInt(arr2[arr2.length-1]) > arr1.length) {
                        logger.error("下标的最大列数超过实际最大列数");
                        return event;
                    }
                    String newLine = "";
                    for (int i = 0; i < arr2.length; ++i) {
                        int tmp = Integer.parseInt(arr2[i])-1;
                        newLine += arr1[tmp];
                        if (i != arr2.length-1) {
                            newLine += colSeparator;
                        }
                    }
                    event.setBody(newLine.getBytes(charset));
                    if (searchAndReplace) {
                        String origBody = new String(event.getBody(), charset);
                        Matcher matcher = searchPattern.matcher(origBody);
                        String newBody = matcher.replaceAll(replaceString);
                        event.setBody(newBody.getBytes(charset));
                        return event;
                    } else {
                        return event;
                    }
                } else {
                    if (searchAndReplace) {
                        String origBody = new String(event.getBody(), charset);
                        Matcher matcher = searchPattern.matcher(origBody);
                        String newBody = matcher.replaceAll(replaceString);
                        event.setBody(newBody.getBytes(charset));
                        return event;
                    } else {
                        return event;
                    }
                }
            } else {
                return null;
            }
        } else {
            if (filterByCol) {
                String line = new String(event.getBody(), charset);
                String[] arr1 = line.split(colSeparator);
                String[] arr2 = index.split(",");
                if (Integer.parseInt(arr2[arr2.length-1]) > arr1.length) {
                    logger.error("下标的最大列数超过实际最大列数");
                    return event;
                }
                String newLine = "";
                for (int i = 0; i < arr2.length; ++i) {
                    int tmp = Integer.parseInt(arr2[i])-1;
                    newLine += arr1[tmp];
                }
                event.setBody(newLine.getBytes(charset));
                if (searchAndReplace) {
                    String origBody = new String(event.getBody(), charset);
                    Matcher matcher = searchPattern.matcher(origBody);
                    String newBody = matcher.replaceAll(replaceString);
                    event.setBody(newBody.getBytes(charset));
                    return event;
                } else {
                    return event;
                }
            } else {
                if (searchAndReplace) {
                    String origBody = new String(event.getBody(), charset);
                    Matcher matcher = searchPattern.matcher(origBody);
                    String newBody = matcher.replaceAll(replaceString);
                    event.setBody(newBody.getBytes(charset));
                    return event;
                } else {
                    return event;
                }
            }
        }
    }

    @Override
    public List<Event> intercept(List<Event> events) {
        List<Event> out = Lists.newArrayList();
        for (Event event : events) {
            Event outEvent = intercept(event);
            if (outEvent != null) {
                out.add(outEvent);
            }
        }
        return out;
    }

    // 定义相关flume常量
    public static class Builder implements Interceptor.Builder {
        private static final String SEARCH_AND_REPLACE_KEY = "searchAndReplace";
        private static final String SEARCH_PATTERN_KEY = "searchPattern";
        private static final String REPLACE_STRING_KEY = "replaceString";
        private static final String CHARSET_KEY = "charset";
        private static final String FILTER_BY_ROW_KEY = "filterByRow";
        private static final String REGEX_KEY = "regex";
        private static final String FILTER_BY_COL_KEY = "filterByCol";
        private static final String COL_SEPARATOR_KEY = "colSeparator";
        private static final String INDEX_KEY = "index";

        private boolean searchAndReplace;
        private Pattern searchRegex;
        private String replaceString;
        private Charset charset = Charsets.UTF_8;
        private boolean filterByRow;
        private Pattern regex;
        private boolean filterByCol;
        private String colSeparator;
        private String index;

        @Override
        public void configure(Context context) {
            searchAndReplace = context.getBoolean(SEARCH_AND_REPLACE_KEY);
            String searchPattern = context.getString(SEARCH_PATTERN_KEY);
            replaceString = context.getString(REPLACE_STRING_KEY);
            searchRegex = Pattern.compile(searchPattern);
            if (context.containsKey(CHARSET_KEY)) {
                // May throw IllegalArgumentException for unsupported charsets.
                charset = Charset.forName(context.getString(CHARSET_KEY));
            }
            filterByRow = context.getBoolean(FILTER_BY_ROW_KEY);
            String regexString = context.getString(REGEX_KEY);
            regex = Pattern.compile(regexString);
            filterByCol = context.getBoolean(FILTER_BY_COL_KEY);
            colSeparator = context.getString(COL_SEPARATOR_KEY);
            index = context.getString(INDEX_KEY);
        }

        @Override
        public Interceptor build() {
            return new SearchAndReplaceAndFilterInterceptor(
                    searchAndReplace,
                    searchRegex,
                    replaceString,
                    charset,
                    filterByRow,
                    regex,
                    filterByCol,
                    colSeparator,
                    index);
        }
    }
}