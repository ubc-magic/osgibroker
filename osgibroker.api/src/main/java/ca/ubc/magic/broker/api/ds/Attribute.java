/*
 * OSGiBroker Project
 * Copyright (c) UBC Media and Graphics Interdisciplinary Centre (MAGIC) 2009
 * http://www.magic.ubc.ca/
 * 
 */
package ca.ubc.magic.broker.api.ds;

/**
 * 
 * Typed or untyped (string) name/values
 * 
 * http://code.google.com/apis/base/attrs-queries.html#attTypes
 * 
 * @author mike
 * 
 */
public class Attribute {
	
	/**
	 * Supported atttribute types.
	 * text is equivalent to string, data is opaque data
	 * @author mike
	 *
	 */
	public enum Type {
		int32, int64, real, string, text, data
	}
	
	private String name;
	private Type type;
	private String value;

	/**
	 * Creates a new attributes with given name and value (default type = text)
	 * 
	 * @param name
	 *            attribute name
	 * @param value
	 *            attribute value
	 */
	public Attribute(String name, String value) {
		this(name, Type.string, value);
	}

	public Attribute(String name, Type type, String value) {
		this.name = name;
		this.type = type;
		this.value = value;
	}

	/**
	 * Gets the attribute name
	 * 
	 * @return attribute name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the attribute value
	 * 
	 * @param name
	 *            new attribute value
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the attribute type
	 * 
	 * @return attribute type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Sets the attribute type
	 * 
	 * @param type
	 *            attribute type
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Gets the attribute value
	 * 
	 * @return attribute value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the attribute value
	 * 
	 * @param value
	 *            attribute value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	public boolean equals(Object o) {
		if (!o.getClass().equals(this.getClass()))
			return false;
		Attribute other = (Attribute) o;

		if (this.name.equals(other.name) && this.type.equals(other.type)
				&& this.value.equals(other.value))
			return true;
		return false;
	}

	public int hashCode() {
		return 0;
	}
}
