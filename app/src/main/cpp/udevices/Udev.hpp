//
// Created by ductt on 12/19/2018.
//

#ifndef EVENTINJECTOR_UDEV_HPP
#define EVENTINJECTOR_UDEV_HPP


#include <string>

#include <linux/uinput.h>

//typedef int (*onEventReceived)(void*, input_event*);
class Udev;
class IDevEventListener {
public:
    virtual void eventReceived(Udev*, input_event*) =0;
    virtual void eventCodeReceived(Udev*, int code, float value) =0;
};

class UdevEventLogging: public IDevEventListener {
public:
    virtual void eventReceived(Udev*, input_event* event);
    virtual void eventCodeReceived(Udev*, int code, float value);
};

struct UdevException : public std::runtime_error {
public:
    UdevException(const char* msg) : std::runtime_error(msg) {}
    UdevException(std::string msg) : std::runtime_error(msg.c_str()) {}
};

struct UdevTimeout : public std::runtime_error {
public:
    UdevTimeout() : std::runtime_error("Timeout expired") {}
};

class Udev {
protected:
    int fd;
    std::string name;
    std::string path;
    input_event event;
    bool has_event(uint ev); // ev = EV_KEY / EV_ASB / EV_SYN
    bool has_keys(uint key);
    bool has_abs(uint code);
    IDevEventListener* eventCb;
    //virtual void create_virtualdev();   // pure virtual
public:
    Udev(std::string path);
    Udev(std::string name, bool realdev);
    Udev(std::string path, std::string name); // TODO : using only this constructor
    virtual ~Udev();
    static Udev* create(std::string path);
    static Udev* scan_for_devname(std::string, std::string);
    void send_event(int type, int code, int value);
    std::string getName();
    std::string getPath();
    void open(int flags);
    virtual void open();
    void close();
    virtual void poll(input_event* ev, int timeoutms, short selectevent);
    void poll(input_event* ev, int timeoutms);
    void poll(input_event* ev);
    void register_event_callback(IDevEventListener* cb);
    //virtual void continue_poll(IUdevMapper* mapper, int timeoutms);
    //virtual void stop_poll();
    bool has_gamepad;
    bool has_dpad;
    bool has_mtouch;
    bool has_motion;
    static bool has_event(int fd, uint ev); // ev = EV_KEY / EV_ASB / EV_SYN
    static bool has_keys(int fd, uint key);
    static bool has_abs(int fd, uint code);

};


#endif //EVENTINJECTOR_UDEV_HPP
