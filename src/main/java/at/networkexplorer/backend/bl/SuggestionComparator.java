package at.networkexplorer.backend.bl;

import java.io.File;
import java.util.Comparator;
import java.util.regex.Pattern;

public class SuggestionComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        return o1.split(Pattern.quote("/")).length-o2.split(Pattern.quote("/")).length;
    }
}
