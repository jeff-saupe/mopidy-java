package danbroid.mopidy.model;

import lombok.Getter;
import lombok.Setter;

public abstract class Base {
	private String __model__ = getClass().getSimpleName();

	@Getter @Setter
	private transient Object extra;	//extra field attaching non-json derived data
}