package pebble

import com.mitchellbosecke.pebble.error.PebbleException
import com.mitchellbosecke.pebble.extension.Filter
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import org.ocpsoft.prettytime.PrettyTime

class TimeAgoFilter implements Filter {

    TimeAgoFilter() {
    }

    @Override
    Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
        if (!input) return ""

        PrettyTime prettyTime = new PrettyTime(context.getLocale())
        if (input instanceof Date) {
            return prettyTime.format((Date) input)
        }
        if (input instanceof Long)
            return prettyTime.format(new Date(input))
        throw new IllegalArgumentException("input must be an Date or long")
    }

    @Override
    List<String> getArgumentNames() {
        return null
    }
}
