package ch.cern.spark.metrics.filter;

import java.util.Optional;
import java.util.function.Predicate;

import ch.cern.spark.metrics.Metric;
import ch.cern.util.function.AndPredicate;
import ch.cern.util.function.OrPredicate;
import ch.cern.utils.IString;
import ch.cern.utils.StringUtils;

public class MetricPredicateParser {

	private enum Operator {AND, OR};

	public static Predicate<Metric> parse(String input) throws ParseException{
		IString iInput = new IString(StringUtils.removeSpacesOutsideQuotes(input));

		return parse(iInput);
	}

	private static Predicate<Metric> parse(IString input) throws ParseException{
		Predicate<Metric> predicate = getPredicate(input);
		
		Optional<Operator> op = Optional.empty();
		while((op = getOperator(input)).isPresent()) {
			switch (op.get()) {
			case AND:
				predicate = new AndPredicate<Metric>(predicate, getPredicate(input));
				break;
			case OR:
				predicate = new OrPredicate<Metric>(predicate, getPredicate(input));
				break;
			default:
				throw new RuntimeException("Operator does not exist");
			}
		}
		
		return predicate;
	}

	private static Predicate<Metric> getPredicate(IString iInput) throws ParseException {
		if(iInput.getChar() == '(') {
			int closingCharIndex = indexOfClosingParenthesis(iInput);
			
			String predicate = iInput.getString().substring(iInput.getIndex() + 1, closingCharIndex);
			iInput.moveIndex(predicate.length() + 2);
			
			return parse(new IString(predicate));
		}
		
		StringBuilder predicate = new StringBuilder();
		Character c = iInput.getChar();
		while(c != null && c != '&' && c != '|') {
			predicate.append(c);
			
			c = iInput.nextChar();
		}
		
		return parseUniquePredicate(predicate.toString());
	}
	
	private static Predicate<Metric> parseUniquePredicate(String input) throws ParseException {
		String[] parts = input.split("!=");
		if(parts.length == 2)
			return new NotEqualMetricPredicate(parts[0], StringUtils.removeQuotes(parts[1]));
		
		parts = input.split("=");
		if(parts.length == 2)
			return new EqualMetricPredicate(parts[0], StringUtils.removeQuotes(parts[1]));
		
		throw new ParseException("Missing comparison operation = or != at \"" + input + "\"");
	}

	private static int indexOfClosingParenthesis(IString iInput) throws ParseException {
		int counter = -1;
		
		for(int i = iInput.getIndex(); i < iInput.getString().length(); i++) {
			char c = iInput.getString().charAt(i);
			
			if(c == '(')
				counter++;
			else if(c == ')')
				counter--;
			
			if(counter == -1)
				return i;
		}
		
		if(counter == -1)
			return iInput.getString().length() - 1;
		else
			throw new ParseException("Expecting ) at \"" + iInput.getString()  + "\"");
	}

	private static Optional<Operator> getOperator(IString iInput) throws ParseException {
		Optional<Operator> op;
		
		if(iInput.isFinished())
			return Optional.empty();
		
		if(iInput.startsWith("&"))	
			op = Optional.of(Operator.AND);
		else if(iInput.startsWith("|"))
			op = Optional.of(Operator.OR);
		else
			throw new ParseException("Operation \"" + iInput.getChar() + "\" does not exist at \"" + iInput + "\"");
		
		iInput.moveIndex(1);
		
		return op;
	}
	
}
