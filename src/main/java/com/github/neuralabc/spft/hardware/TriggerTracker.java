package com.github.neuralabc.spft.hardware;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Handles key strokes
 */
public class TriggerTracker implements NativeKeyListener {
    private static final Logger LOG = LoggerFactory.getLogger(TriggerTracker.class);
    /**
     * A NOOP trigger tracker
     */
    public static final TriggerTracker NO_TRIGGERS = new TriggerTracker(Collections.emptyList());
    private final List<String> triggers;
    private final CountDownLatch sync = new CountDownLatch(1);

    public TriggerTracker(List<String> triggers) {
        if (!GlobalScreen.isNativeHookRegistered()) {
            try {
                GlobalScreen.registerNativeHook();
            } catch (NativeHookException ex) {
                LOG.error("Error registering keyboard listener. Triggers will be disabled", ex);
                triggers = Collections.emptyList();
            }
        }
        this.triggers = triggers;
    }

    public void waitNext() throws InterruptedException {
        if (!triggers.isEmpty()) {
            LOG.info("Waiting for next trigger");
            sync.await();
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
        if (triggers.contains(NativeKeyEvent.getKeyText(nativeEvent.getKeyCode()))) {
            LOG.debug("Trigger received");
            sync.countDown();
        }
    }

    public void start() {
        if (!triggers.isEmpty()) {
            GlobalScreen.addNativeKeyListener(this);
        }
    }

    public void stop() {
        GlobalScreen.removeNativeKeyListener(this);
    }
}
