package org.freeplane.plugin.workspace.controller;

public interface IExpansionStateHandler {
	/**
	 * try to expand all tree nodes that were previously expanded
	 */
	public abstract void restoreExpansionStates();
	public abstract void reset();
	public abstract void addPathKey(String key);
}
