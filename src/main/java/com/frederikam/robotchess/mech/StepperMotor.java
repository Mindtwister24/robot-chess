package com.frederikam.robotchess.mech;

import com.google.common.util.concurrent.AtomicDouble;
import com.pi4j.component.motor.impl.GpioStepperMotorComponent;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

import static com.frederikam.robotchess.Launcher.gpio;

public class StepperMotor {

    private final GpioStepperMotorComponent motor;
    private AtomicDouble position = new AtomicDouble(0);

    public StepperMotor(Pin pin1, Pin pin2,
                        Pin pin3, Pin pin4,
                        int stepsPerRevolution) {
        GpioPinDigitalOutput pin11 = gpio.provisionDigitalOutputPin(pin1, PinState.LOW);
        GpioPinDigitalOutput pin21 = gpio.provisionDigitalOutputPin(pin3, PinState.LOW);
        GpioPinDigitalOutput pin31 = gpio.provisionDigitalOutputPin(pin2, PinState.LOW);
        GpioPinDigitalOutput pin41 = gpio.provisionDigitalOutputPin(pin4, PinState.LOW);

        final GpioPinDigitalOutput[] pins = {
                pin11,
                pin21,
                pin31,
                pin41};

        // this will ensure that the motor is stopped when the program terminates
        gpio.setShutdownOptions(true, PinState.LOW, pins);

        // create motor component
        motor = new GpioStepperMotorComponent(pins);

        // create byte array to demonstrate a single-step sequencing
        // (This is the most basic method, turning on a single electromagnet every time.
        //  This sequence requires the least amount of energy and generates the smoothest movement.)
        byte[] singleStepForwardSeq = new byte[4];
        singleStepForwardSeq[0] = (byte) 0b0001;
        singleStepForwardSeq[1] = (byte) 0b0010;
        singleStepForwardSeq[2] = (byte) 0b0100;
        singleStepForwardSeq[3] = (byte) 0b1000;

        motor.setStepsPerRevolution(stepsPerRevolution);
        motor.setStepSequence(singleStepForwardSeq);
    }

    StepperMotor(Pin pin1, Pin pin2, Pin pin3, Pin pin4) {
        this(pin1, pin2, pin3, pin4, 400);
    }

    public void step(double steps, int interval) {
        double startPos = position.get();
        position.addAndGet(steps);
        motor.setStepInterval(interval);

        // Calculate the steps that we need, with respect to mitigating rounding errors
        int roundedSteps = (int) (Math.floor(steps) - Math.floor(startPos));
        motor.step(roundedSteps);
    }

    public void stepTo(double newPosition, int interval) {
        step(newPosition - position.get(), interval);
    }

    public double getPosition() {
        return position.get();
    }
}