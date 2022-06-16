/**
 * An "Additional Data," or AD, is what I am calling the abstraction over capabilities on Forge and
 * cardinal components on Fabric.
 * <p>
 * For each AD in here, the mod provides an interface or abstract class that mirrors it.
 * Implementing the given interface on whatever type that AD attaches to (items, blocks) will automatically attach an
 * appropriate instance of an AD to it as a capability/CC by scanning the registry.
 * For example, {@link at.petrak.hexcasting.api.item.ColorizerItem ColorizerItem}.
 * <p>
 * I do not know why we don't just implement the AD interface directly. Ask Wire.
 */
package at.petrak.hexcasting.api.addldata;
