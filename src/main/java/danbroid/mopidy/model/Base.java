package danbroid.mopidy.model;

import lombok.Getter;
import lombok.Setter;

public abstract class Base {
	private String __model__ = getClass().getSimpleName();

	@Getter @Setter
	private transient Object extra;	// Extra field attaching non-JSON derived data
}