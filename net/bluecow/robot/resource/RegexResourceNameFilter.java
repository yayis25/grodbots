/*
 * Created on Apr 10, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.resource;

import java.util.regex.Pattern;

/**
 * A resource name filter that accepts resources based on regular expressions.
 * Can be used to either include matching resource paths or exclude matching
 * resource paths.
 *
 * @author fuerth
 * @version $Id$
 */
public class RegexResourceNameFilter implements ResourceNameFilter {

    /**
     * Compiled version of the regex this instance matches on.
     */
    private final Pattern pattern;
    
    /**
     * Controls whether this filter accepts pathnames matching the pattern
     * and rejects all others, or rejects pathnames matching the pattern
     * and accepts all others.
     */
    private final boolean filterOut;
    
    /**
     * Creates a new filter with the given regular expression and filtering
     * mode.
     * 
     * @param regex The regular expression to match on.  Must not be null.
     * @param filterOut The matching mode.  False means that pathnames will
     * be accepted iff they match given regex; true means that pathnames
     * will be accepted iff they do not match the regex.
     */
    public RegexResourceNameFilter(String regex, boolean filterOut) {
        this.pattern = Pattern.compile(regex);
        this.filterOut = filterOut;
    }
    
    public boolean accepts(String path) {
        boolean match = pattern.matcher(path).matches();
        if (filterOut) {
            return !match;
        } else {
            return match;
        }
    }

}
