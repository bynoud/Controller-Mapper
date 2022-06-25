
#ifndef EVENTINJECTOR_STOPPABLE_CPP
#define EVENTINJECTOR_STOPPABLE_CPP

#include <iostream>
#include <assert.h>
#include <chrono>
#include <future>


#include <linux/input.h>
#include "Udev.hpp"

/*
 * Class that encapsulates promise and future object and
 * provides API to set exit signal for the thread
 */
class Stoppable
{
protected:
    std::promise<void> exitSignal;
    std::future<void> futureObj;

    // Task need to provide defination  for this function
    // It will be called by thread function
    virtual void run() = 0;
    bool bRunning = 0;


    //Checks if thread is requested to stop
    bool stopRequested()
    {
        // checks if value in future object is available
        return !(futureObj.wait_for(std::chrono::milliseconds(0)) == std::future_status::timeout);
    }

public:
    Stoppable() :
            futureObj(exitSignal.get_future())
    {

    }
    Stoppable(Stoppable && obj) : exitSignal(std::move(obj.exitSignal)), futureObj(std::move(obj.futureObj))
    {
        //std::cout << "Move Constructor is called" << std::endl;
    }

    Stoppable & operator=(Stoppable && obj)
    {
        //std::cout << "Move Assignment is called" << std::endl;
        exitSignal = std::move(obj.exitSignal);
        futureObj = std::move(obj.futureObj);
        return *this;
    }



    // Thread function to be executed by thread
    void start() {
        bRunning = 1;
        run();
        bRunning = 0;
    }

    bool isRunning() { return bRunning; }

    // Request the thread to stop by setting value in promise object
    void stop()
    {
        exitSignal.set_value();
    }
};



class StoppablePoll: public Stoppable {
protected:
    Udev* dev;
    //onEventReceived onEventCallback;

    // the polling check for stop request everytime it received event
    // or after timeout of 1second
    virtual void run() {
        assert(dev != NULL);
        input_event ev = input_event();
        while (!stopRequested()) {
            try {
                dev->poll(&ev, 1000);
            } catch (UdevTimeout e) {
                // check cancel requested
            } catch (std::exception e) {
                throw e;
            }
        }
    }
public:
    StoppablePoll() : StoppablePoll(NULL, NULL) {};
    StoppablePoll(Udev* device) : StoppablePoll(device, NULL) {};
    StoppablePoll(Udev* device, IDevEventListener* callback) : Stoppable() {
        if (device!=NULL) {
            set_device(device);
            if (callback!=NULL) set_callback(callback);
        }
    };

    virtual StoppablePoll* set_device(Udev* device) {
        dev = device;
        return this;
    }

    StoppablePoll* set_callback(IDevEventListener* callback) {
        assert(dev!=NULL);
        dev->register_event_callback(callback);
        return this;
    }

};

#endif // EVENTINJECTOR_STOPPABLE_CPP