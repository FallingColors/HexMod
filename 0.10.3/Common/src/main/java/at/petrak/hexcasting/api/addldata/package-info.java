/**
 * An "Additional Data," or AD, is what I am calling the abstraction over capabilities on Forge and
 * cardinal components on Fabric.
 * <p>
 * An {@code ADFooBar} in this package will be implemented by a {@code CCFooBar} on Fabric.
 * On Forge, there are a set of private records that implement them.
 * <p>
 * The point is, this provides an interface for interacting with however whatever platform sticks extra info on stuff.
 */
package at.petrak.hexcasting.api.addldata;
