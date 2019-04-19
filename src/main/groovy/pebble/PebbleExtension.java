package pebble;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Extension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.extension.Function;

import java.util.HashMap;
import java.util.Map;

public class PebbleExtension extends AbstractExtension implements Extension {
    private Map<String, Filter> filters = new HashMap<>();
    private Map<String, Function> functions = new HashMap<>();

    public PebbleExtension() {
        this.filters.put("time_ago", new TimeAgoFilter());
    }

    @Override
    public Map<String, Filter> getFilters() {
        return filters;
    }

    @Override
    public Map<String, Function> getFunctions() {
        return functions;
    }
}
