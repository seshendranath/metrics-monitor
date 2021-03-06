package ch.cern.spark.metrics.filter;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ch.cern.spark.metrics.Metric;

public class EqualMetricPredicate implements Predicate<Metric> {

	private String key;
	private Pattern value;

	public EqualMetricPredicate(String key, String value) throws ParseException {
		this.key = key;
		
		try {
			this.value = Pattern.compile(value);
		}catch(PatternSyntaxException e) {
			throw new ParseException(e.getDescription());
		}
	}

	@Override
	public boolean test(Metric metricInput) {
		Predicate<Metric> exist = metric -> metric.getIDs().containsKey(key);
		Predicate<Metric> match = metric -> value.matcher(metric.getIDs().get(key)).matches();
		
		return exist.and(match).test(metricInput);
	}
	
	@Override
	public String toString() {
		return key + " == \"" + value + "\"";
	}

}
