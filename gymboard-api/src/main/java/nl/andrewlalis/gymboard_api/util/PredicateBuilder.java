package nl.andrewlalis.gymboard_api.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class PredicateBuilder {
	private enum Type {
		AND,
		OR
	}

	private final Type type;
	private final CriteriaBuilder criteriaBuilder;
	private final List<Predicate> predicates;

	public PredicateBuilder(Type type, CriteriaBuilder cb) {
		this.type = type;
		this.criteriaBuilder = cb;
		this.predicates = new ArrayList<>();
	}

	public PredicateBuilder with(Predicate predicate) {
		this.predicates.add(predicate);
		return this;
	}

	public Predicate build() {
		Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
		return switch (type) {
			case OR -> this.criteriaBuilder.or(predicatesArray);
			case AND -> this.criteriaBuilder.and(predicatesArray);
		};
	}

	public static PredicateBuilder and(CriteriaBuilder cb) {
		return new PredicateBuilder(Type.AND, cb);
	}

	public static PredicateBuilder or(CriteriaBuilder cb) {
		return new PredicateBuilder(Type.OR, cb);
	}
}
