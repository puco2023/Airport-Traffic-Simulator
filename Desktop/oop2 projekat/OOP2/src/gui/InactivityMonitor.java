package gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.function.BooleanSupplier;
class InactivityMonitor {

    private static final long INACTIVITY_TIME = 60_000;
    private static final long WARNING_TIME = 5_000;

    private final Frame owner;
    private final Runnable onTimeout;

    private AWTEventListener userActivityListener;
    private volatile long lastUserActionTime;
    private volatile boolean running = true;
    private Thread thread;
    private Dialog dialog;
    private Label label;
    private BooleanSupplier isAirportBlinking;
    private BooleanSupplier isSimulationPaused;
    private BooleanSupplier isSimulationRunning;
    InactivityMonitor(Frame owner, Runnable onTimeout,BooleanSupplier isAirportBlinking, BooleanSupplier isPaused,BooleanSupplier isSimulationRunning) {
        this.owner = owner;
        this.onTimeout = onTimeout;
        this.isAirportBlinking=isAirportBlinking;
        this.isSimulationPaused = isPaused;
        this.isSimulationRunning=isSimulationRunning;
    }

    void start() {
        startUserActivityTracking();
        startInactivityThread();
    }

    void stop() {
        running = false;
        if (dialog != null) {
            dialog.dispose();
        }
    }
    private void registerUserAction() {
        lastUserActionTime = System.currentTimeMillis();
        closeInactivityDialog();
    }
    private void startInactivityThread() {
        lastUserActionTime = System.currentTimeMillis();
        thread = new Thread(() -> {
            while (running) {
                if (isAirportBlinking.getAsBoolean() || ((isSimulationRunning.getAsBoolean() && !isSimulationPaused.getAsBoolean()))) {
                    lastUserActionTime = System.currentTimeMillis();
                    try {
                        Thread.sleep(200);
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                    continue;
                }
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - lastUserActionTime;
                long remainingTime = INACTIVITY_TIME - elapsedTime;
                if (remainingTime <= 0) {
                    EventQueue.invokeLater(onTimeout);
                    break;
                }
                if (remainingTime <= WARNING_TIME) {
                    int remainingSeconds =(int) Math.ceil(remainingTime / 1000.0);
                    EventQueue.invokeLater(() -> {
                        showInactivityDialog(
                            remainingSeconds
                        );
                    });
                }
                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException e) {
                    break;
                }
            }
        });


        thread.setName("Inactivity thread");

        thread.start();
    }

    private void showInactivityDialog(
            int remainingSeconds) {

        if (dialog == null) {

            dialog = new Dialog(owner,"Inactivity warning",false);
            dialog.setLayout(new BorderLayout(10, 10));
            label = new Label("",Label.CENTER);
            Button continueButton = new Button("Continue");
            continueButton.addActionListener(e -> {
                registerUserAction();
            });
            dialog.add(label,BorderLayout.CENTER);

            dialog.add(
                continueButton,
                BorderLayout.SOUTH
            );
            dialog.setSize(420,140);
            dialog.setLocationRelativeTo(owner);
        }
        label.setText("The application will close in "+ remainingSeconds+ " seconds.");


        if (!dialog.isVisible()) {

            dialog.setVisible(true);
        }
    }

    private void closeInactivityDialog() {

        EventQueue.invokeLater(() -> {

            if (dialog != null && dialog.isVisible()) {

                dialog.setVisible(false);
            }
        });
    }

    private void startUserActivityTracking() {

        userActivityListener = event -> {
            if (dialog != null
                    && dialog.isVisible()) {

                return;
            }

            boolean mouseClicked =
                    event instanceof MouseEvent
                    && event.getID() == MouseEvent.MOUSE_PRESSED;

            boolean keyPressed =
                    event instanceof KeyEvent
                    && event.getID() == KeyEvent.KEY_PRESSED;


            if (!mouseClicked && !keyPressed) {
                return;
            }


            Object source = event.getSource();

            if (source instanceof Component) {

                Component component =
                        (Component) source;

                if (belongsToThisWindow(component)) {

                    registerUserAction();
                }
            }
        };


        Toolkit.getDefaultToolkit()
                .addAWTEventListener(
                    userActivityListener,
                    AWTEvent.MOUSE_EVENT_MASK
                    | AWTEvent.KEY_EVENT_MASK

                );
    }

    private boolean belongsToThisWindow(
            Component component) {

        while (component != null) {

            if (component == owner) {
                return true;
            }

            component = component.getParent();
        }

        return false;
    }
}
