/**
 * Developer: Kadvin Date: 15/1/23 下午2:24
 */
package net.happyonroad.event;

import net.happyonroad.component.core.Component;

/**
 * the service package is loading
 */
public class ExtensionLoadingEvent extends ExtensionEvent {
    private static final long serialVersionUID = -1757774726942039005L;

    public ExtensionLoadingEvent(Component source) {
        super(source);
    }
}
