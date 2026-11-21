package mx.unam.virtualinsurance.quote;

public interface QuoteStrategy<T> {

	QuoteDecision evaluate(T request);
}
